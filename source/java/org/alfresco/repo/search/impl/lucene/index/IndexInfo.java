/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.search.impl.lucene.index;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.zip.CRC32;

import javax.swing.plaf.multi.MultiInternalFrameUI;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.search.IndexerException;
import org.alfresco.repo.search.impl.lucene.ClosingIndexSearcher;
import org.alfresco.repo.search.impl.lucene.FilterIndexReaderByNodeRefs;
import org.alfresco.repo.search.impl.lucene.LuceneIndexer;
import org.alfresco.util.GUID;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;

/**
 * The information that makes up an index.
 * 
 * IndexInfoVersion
 * 
 * Repeated information of the form
 * <ol>
 * <li> Index Type.
 * <li> sub-directory name.
 * <li> Status
 * <ol>
 * <li>Indexes, sub indexes, and overlays must be committed. Status is ACTIVE, MERGING, COMPLETING_INDEX
 * <li>Delta: Transaction status
 * <li>Overlay: Transaction status
 * </ol>
 * </ol>
 * 
 * Merges always take place to new indexes so we can detect merge failure or partial merges. Or we do not know what has merged.
 * 
 * Incomplete delete merging does not matter - the overlay would still exist and be treated as such. So a document may be deleted in the index as well as in the applied overlay. It
 * is still correctly deleted.
 * 
 * NOTE: Public methods locak as required, the private methods assume that the appropriate locks have been obtained.
 * 
 * TODO: Write element status into individual directories. This would be enough for recovery if both index files are lost or corrupted.
 * 
 * TODO: Tidy up index status at start up or after some time. How long would you leave a merge to run?
 * 
 * @author Andy Hind
 */
public class IndexInfo
{

    private static final boolean useNIOMemoryMapping = true;

    /**
     * The default name for the file that holds the index information
     */
    private static String INDEX_INFO = "IndexInfo";

    /**
     * The default name for the back up file that holds the index information
     */
    private static String INDEX_INFO_BACKUP = "IndexInfoBackup";

    /**
     * Is this index shared by more than one repository? We can make many lock optimisations if the index is not shared.
     */
    private boolean indexIsShared;

    /**
     * The directory that holds the index
     */
    private File indexDirectory;

    /**
     * The file holding the index information
     */
    private RandomAccessFile indexInfoRAF;

    /**
     * And its file channel
     */
    private FileChannel indexInfoChannel;

    /**
     * The file holding the backup index information.
     */

    private RandomAccessFile indexInfoBackupRAF;

    /**
     * And its file channel
     */
    private FileChannel indexInfoBackupChannel;

    /**
     * The file version. Negative is not yet written.
     */
    private long version = -1;

    /**
     * The index entries that make up this index. Map entries are looked up by name. These are maintained in order so document order is maintained.
     */
    private LinkedHashMap<String, IndexEntry> indexEntries = new LinkedHashMap<String, IndexEntry>();

    /**
     * Lock for the index entries
     */
    private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    /**
     * Read only index readers that also do reference counting.
     */
    private HashMap<String, IndexReader> referenceCountingReadOnlyIndexReaders = new HashMap<String, IndexReader>();

    /**
     * Main index reader
     */
    private IndexReader mainIndexReader;

    /**
     * Index writers for deltas
     */
    private HashMap<String, IndexWriter> indexWriters = new HashMap<String, IndexWriter>();

    /**
     * Index Readers for deltas
     */
    private HashMap<String, IndexReader> indexReaders = new HashMap<String, IndexReader>();

    /**
     * Map of state transitions
     */
    private EnumMap<TransactionStatus, Transition> transitions = new EnumMap<TransactionStatus, Transition>(
            TransactionStatus.class);

    /**
     * Construct an index in the given directory.
     * 
     * @param indexDirectory
     */
    public IndexInfo(File indexDirectory)
    {
        super();
        initialiseTransitions();

        this.indexDirectory = indexDirectory;

        // Make sure the directory exists
        if (!this.indexDirectory.exists())
        {
            if (!this.indexDirectory.mkdirs())
            {
                throw new AlfrescoRuntimeException("Failed to create index directory");
            }
        }
        if (!this.indexDirectory.isDirectory())
        {
            throw new AlfrescoRuntimeException("The index must be held in a directory");
        }

        // Create the info files.
        File indexInfoFile = new File(this.indexDirectory, INDEX_INFO);
        File indexInfoBackupFile = new File(this.indexDirectory, INDEX_INFO_BACKUP);
        if (createFile(indexInfoFile) && createFile(indexInfoBackupFile))
        {
            // a spanking new index
            version = 0;
        }

        // Open the files and channels
        this.indexInfoRAF = openFile(indexInfoFile);
        this.indexInfoChannel = this.indexInfoRAF.getChannel();

        this.indexInfoBackupRAF = openFile(indexInfoBackupFile);
        this.indexInfoBackupChannel = this.indexInfoBackupRAF.getChannel();

        // Read info from disk if this is not a new index.
        if (version == -1)
        {
            readWriteLock.writeLock().lock();
            try
            {
                doWithFileLock(new LockWork<Object>()
                {
                    public Object doWork() throws Exception
                    {
                        setStatusFromFile();
                        return null;
                    }

                });
            }
            finally
            {
                readWriteLock.writeLock().unlock();
            }
        }
    }

    /**
     * This method should only be called from one thread.
     * 
     * @param id
     * @return
     * @throws IOException
     */
    public IndexReader getDeltaIndexReader(String id) throws IOException
    {
        IndexReader reader = indexReaders.get(id);
        if (reader == null)
        {
            // close index writer if required
            closeDeltaIndexWriter(id);
            File location = ensureDeltaExistsAndIsRegistered(id);
            reader = IndexReader.open(location);
            indexReaders.put(id, reader);
        }
        return reader;
    }

    /**
     * The delta information does not need to be saved to disk.
     * 
     * @param id
     * @return
     * @throws IOException
     */
    private File ensureDeltaExistsAndIsRegistered(String id) throws IOException
    {
        File location = new File(indexDirectory, id);
        if (!IndexReader.indexExists(location))
        {
            IndexWriter creator = new IndexWriter(location, new StandardAnalyzer(), true);
            creator.setUseCompoundFile(true);
            creator.close();
        }
        readWriteLock.readLock().lock();
        try
        {
            if (!indexEntries.containsKey(id))
            {
                readWriteLock.readLock().unlock();
                // release to upgrade to write lock
                readWriteLock.writeLock().lock();
                try
                {
                    if (!indexEntries.containsKey(id))
                    {
                        indexEntries.put(id, new IndexEntry(IndexType.DELTA, id, "", TransactionStatus.ACTIVE, ""));
                    }
                }
                finally
                {
                    // Downgrade
                    readWriteLock.readLock().lock();
                    readWriteLock.writeLock().unlock();
                }
            }
        }
        finally
        {
            readWriteLock.readLock().unlock();
        }
        return location;
    }

    public IndexWriter getDeltaIndexWriter(String id, Analyzer analyzer) throws IOException
    {
        IndexWriter writer = indexWriters.get(id);
        if (writer == null)
        {
            // close index writer if required
            closeDeltaIndexReader(id);
            File location = ensureDeltaExistsAndIsRegistered(id);
            writer = new IndexWriter(location, analyzer, false);
            indexWriters.put(id, writer);
        }
        return writer;
    }

    public void closeDeltaIndexReader(String id) throws IOException
    {
        IndexReader reader = indexReaders.get(id);
        if (reader != null)
        {
            reader.close();
            indexReaders.remove(id);
        }
    }

    public void closeDeltaIndexWriter(String id) throws IOException
    {
        IndexWriter writer = indexWriters.get(id);
        if (writer != null)
        {
            writer.close();
            indexWriters.remove(id);
        }
    }

    public IndexReader getMainIndexReferenceCountingReadOnlyIndexReader() throws IOException
    {
        readWriteLock.readLock().lock();
        try
        {
            if (mainIndexReader == null)
            {
                readWriteLock.readLock().unlock();
                readWriteLock.writeLock().lock();
                try
                {
                    if (mainIndexReader == null)
                    {
                        // Sync with disk image if required
                        doWithFileLock(new LockWork<Object>()
                        {
                            public Object doWork() throws Exception
                            {
                                return null;
                            }

                        });
                        mainIndexReader = createMainIndexReader();
                    }
                }
                finally
                {
                    readWriteLock.readLock();
                    readWriteLock.writeLock().unlock();
                }
            }
            return mainIndexReader;
        }
        finally
        {
            readWriteLock.readLock().unlock();
        }
    }

    public IndexReader getMainIndexReferenceCountingReadOnlyIndexReader(LuceneIndexer luceneIndexer) throws IOException
    {
        readWriteLock.readLock().lock();
        try
        {
            if (mainIndexReader == null)
            {
                readWriteLock.readLock().unlock();
                readWriteLock.writeLock().lock();
                try
                {
                    if (mainIndexReader == null)
                    {
                        // Sync with disk image if required
                        doWithFileLock(new LockWork<Object>()
                        {
                            public Object doWork() throws Exception
                            {
                                return null;
                            }

                        });
                        mainIndexReader = createMainIndexReader();
                    }
                }
                finally
                {
                    readWriteLock.readLock();
                    readWriteLock.writeLock().unlock();
                }
            }
            // Combine the index delta with the main index
            // Make sure the index is written to disk
            // TODO: Should use the in memory index but we often end up forcing to disk anyway.
            // Is it worth it?
            luceneIndexer.flushPending();
            IndexReader deltaReader = getDeltaIndexReader(luceneIndexer.getDeltaId());
            IndexReader reader = new MultiReader(new IndexReader[] {
                    new FilterIndexReaderByNodeRefs(mainIndexReader, luceneIndexer.getDeletions()), deltaReader });
            return reader;
        }
        finally
        {
            readWriteLock.readLock().unlock();
        }
    }

    public void setStatus(final String id, final TransactionStatus state, final Set<Term> toDelete, final Set<Term> read)
            throws IOException
    {
        final Transition transition = getTransition(state);
        readWriteLock.writeLock().lock();
        try
        {
            doWithFileLock(new LockWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    transition.transition(id, toDelete, read);
                    return null;
                }

            });
        }
        finally
        {
            readWriteLock.writeLock().unlock();
        }
    }

    //
    // Internal support for status management
    //

    private Transition getTransition(TransactionStatus state)
    {
        Transition transition = transitions.get(state);
        if (transition != null)
        {
            return transition;
        }
        else
        {
            throw new IndexerException("Invalid state " + state);
        }

    }

    private void initialiseTransitions()
    {

        transitions.put(TransactionStatus.PREPARING, new PreparingTransition());
        transitions.put(TransactionStatus.PREPARED, new PreparedTransition());
        transitions.put(TransactionStatus.COMMITTING, new CommittingTransition());
        transitions.put(TransactionStatus.COMMITTED, new CommittedTransition());
        transitions.put(TransactionStatus.ROLLINGBACK, new RollingBackTransition());
        transitions.put(TransactionStatus.ROLLEDBACK, new RolledBackTransition());

    }

    private interface Transition
    {
        void transition(String id, Set<Term> toDelete, Set<Term> read) throws IOException;
    }

    private class PreparingTransition implements Transition
    {
        public void transition(String id, Set<Term> toDelete, Set<Term> read) throws IOException
        {
            IndexEntry entry = indexEntries.get(id);
            if (entry == null)
            {
                throw new IndexerException("Unknown transaction " + id);
            }

            if (TransactionStatus.PREPARING.follows(entry.getStatus()))
            {
                entry.setStatus(TransactionStatus.PREPARING);
                writeStatus();
            }
            else
            {
                throw new IndexerException("Invalid transition for "
                        + id + " from " + entry.getStatus() + " to " + TransactionStatus.PREPARING);
            }
        }
    }

    private class PreparedTransition implements Transition
    {
        public void transition(String id, Set<Term> toDelete, Set<Term> read) throws IOException
        {
            IndexEntry entry = indexEntries.get(id);
            if (entry == null)
            {
                throw new IndexerException("Unknown transaction " + id);
            }

            if (TransactionStatus.PREPARED.follows(entry.getStatus()))
            {
                entry.setStatus(TransactionStatus.PREPARED);
                writeStatus();
            }
            else
            {
                throw new IndexerException("Invalid transition for "
                        + id + " from " + entry.getStatus() + " to " + TransactionStatus.PREPARED);
            }
        }
    }

    private class CommittingTransition implements Transition
    {
        public void transition(String id, Set<Term> toDelete, Set<Term> read) throws IOException
        {
            IndexEntry entry = indexEntries.get(id);
            if (entry == null)
            {
                throw new IndexerException("Unknown transaction " + id);
            }

            if (TransactionStatus.COMMITTING.follows(entry.getStatus()))
            {
                entry.setStatus(TransactionStatus.COMMITTING);
                writeStatus();
            }
            else
            {
                throw new IndexerException("Invalid transition for "
                        + id + " from " + entry.getStatus() + " to " + TransactionStatus.COMMITTING);
            }
        }
    }

    private class CommittedTransition implements Transition
    {
        public void transition(String id, Set<Term> toDelete, Set<Term> read) throws IOException
        {
            IndexEntry entry = indexEntries.get(id);
            if (entry == null)
            {
                throw new IndexerException("Unknown transaction " + id);
            }

            if (TransactionStatus.COMMITTED.follows(entry.getStatus()))
            {
                // Do the deletions

                entry.setStatus(TransactionStatus.COMMITTED);
                writeStatus();
            }
            else
            {
                throw new IndexerException("Invalid transition for "
                        + id + " from " + entry.getStatus() + " to " + TransactionStatus.COMMITTED);
            }
        }
    }

    private class RollingBackTransition implements Transition
    {
        public void transition(String id, Set<Term> toDelete, Set<Term> read) throws IOException
        {
            IndexEntry entry = indexEntries.get(id);
            if (entry == null)
            {
                throw new IndexerException("Unknown transaction " + id);
            }

            if (TransactionStatus.ROLLINGBACK.follows(entry.getStatus()))
            {
                entry.setStatus(TransactionStatus.ROLLINGBACK);
                writeStatus();
            }
            else
            {
                throw new IndexerException("Invalid transition for "
                        + id + " from " + entry.getStatus() + " to " + TransactionStatus.ROLLINGBACK);
            }
        }
    }

    private class RolledBackTransition implements Transition
    {
        public void transition(String id, Set<Term> toDelete, Set<Term> read) throws IOException
        {
            IndexEntry entry = indexEntries.get(id);
            if (entry == null)
            {
                throw new IndexerException("Unknown transaction " + id);
            }

            if (TransactionStatus.ROLLEDBACK.follows(entry.getStatus()))
            {
                entry.setStatus(TransactionStatus.ROLLEDBACK);
                writeStatus();
            }
            else
            {
                throw new IndexerException("Invalid transition for "
                        + id + " from " + entry.getStatus() + " to " + TransactionStatus.ROLLEDBACK);
            }
        }
    }

    //
    //
    // Internal methods for implementation support
    // ===========================================
    //
    // These methods should all be called with the appropriate locks.
    //
    //

    private static boolean createFile(File file)
    {

        if (!file.exists())
        {
            try
            {
                file.createNewFile();
                return true;
            }
            catch (IOException e)
            {
                throw new AlfrescoRuntimeException("Failed to create info file", e);
            }
        }
        return false;
    }

    private static RandomAccessFile openFile(File file)
    {
        try
        {
            if (useNIOMemoryMapping)
            {
                return new RandomAccessFile(file, "rw");
            }
            else
            {
                return new RandomAccessFile(file, "rws");
            }
        }
        catch (FileNotFoundException e)
        {
            throw new AlfrescoRuntimeException("Failed to open index info file", e);
        }
    }

    /**
     * Check status must be called holding the file lock.
     * 
     * @param raf
     * @throws IOException
     */
    private void setStatusFromFile() throws IOException
    {
        try
        {
            setStatusFromFile(indexInfoChannel);
        }
        catch (IOException e)
        {
            // The first data file is corrupt so we fall back to the back up
            setStatusFromFile(indexInfoBackupChannel);
        }
        clearOldReaders();
    }

    private void clearOldReaders() throws IOException
    {
        // Find valid
        HashSet<String> valid = new HashSet<String>();
        for (String id : indexEntries.keySet())
        {
            IndexEntry entry = indexEntries.get(id);
            if (entry.getStatus().isCommitted())
            {
                valid.add(id);
            }
        }
        // Find current invalid
        HashSet<String> inValid = new HashSet<String>();
        for (String id : referenceCountingReadOnlyIndexReaders.keySet())
        {
            if (!valid.contains(id))
            {
                inValid.add(id);
            }
        }
        // Clear invalid
        boolean hasInvalid = false;
        for (String id : inValid)
        {
            IndexReader reader = referenceCountingReadOnlyIndexReaders.remove(id);
            ReferenceCounting referenceCounting = (ReferenceCounting) reader;
            referenceCounting.setInvalidForReuse();
            hasInvalid = true;
        }
        if (hasInvalid)
        {
            mainIndexReader = null;
        }
    }

    private IndexReader createMainIndexReader() throws IOException
    {
        IndexReader reader = null;
        for (String id : indexEntries.keySet())
        {
            IndexEntry entry = indexEntries.get(id);
            if (entry.getStatus().isCommitted())
            {
                IndexReader subReader = getReferenceCountingIndexReader(id);
                if (reader == null)
                {
                    reader = subReader;
                }
                else
                {
                    reader = new MultiReader(new IndexReader[] { reader, subReader });
                }
            }
        }
        return reader;
    }

    private IndexReader getReferenceCountingIndexReader(String id) throws IOException
    {
        IndexReader reader = referenceCountingReadOnlyIndexReaders.get(id);
        if (reader == null)
        {
            File location = new File(indexDirectory, id);
            reader = IndexReader.open(location);
            reader = ReferenceCountingReadOnlyIndexReaderFactory.createReader(reader);
        }
        return reader;
    }

    private boolean checkVersion() throws IOException
    {
        try
        {
            return checkVersion(indexInfoChannel);
        }
        catch (IOException e)
        {
            // The first data file is corrupt so we fall back to the back up
            return checkVersion(indexInfoBackupChannel);
        }
    }

    private boolean checkVersion(FileChannel channel) throws IOException
    {
        if (channel.size() > 0)
        {
            channel.position(0);
            ByteBuffer buffer;

            if (useNIOMemoryMapping)
            {
                MappedByteBuffer mbb = channel.map(MapMode.READ_ONLY, 0, 8);
                mbb.load();
                buffer = mbb;
            }
            else
            {
                buffer = ByteBuffer.wrap(new byte[8]);
                channel.read(buffer);
                buffer.position(0);
            }

            buffer.position(0);
            long onDiskVersion = buffer.getLong();
            return (version == onDiskVersion);
        }
        return (version == 0);
    }

    private void setStatusFromFile(FileChannel channel) throws IOException
    {
        if (channel.size() > 0)
        {
            channel.position(0);
            ByteBuffer buffer;

            if (useNIOMemoryMapping)
            {
                MappedByteBuffer mbb = channel.map(MapMode.READ_ONLY, 0, channel.size());
                mbb.load();
                buffer = mbb;
            }
            else
            {
                buffer = ByteBuffer.wrap(new byte[(int) channel.size()]);
                channel.read(buffer);
                buffer.position(0);
            }

            buffer.position(0);
            long onDiskVersion = buffer.getLong();
            if (version != onDiskVersion)
            {
                CRC32 crc32 = new CRC32();
                crc32.update((int) (onDiskVersion >>> 32) & 0xFFFFFFFF);
                crc32.update((int) (onDiskVersion >>> 0) & 0xFFFFFFFF);
                int size = buffer.getInt();
                crc32.update(size);
                LinkedHashMap<String, IndexEntry> newIndexEntries = new LinkedHashMap<String, IndexEntry>();
                for (int i = 0; i < size; i++)
                {
                    String indexTypeString = readString(buffer, crc32);
                    IndexType indexType;
                    try
                    {
                        indexType = IndexType.valueOf(indexTypeString);
                    }
                    catch (IllegalArgumentException e)
                    {
                        throw new IOException("Invalid type " + indexTypeString);
                    }

                    String name = readString(buffer, crc32);

                    String parentName = readString(buffer, crc32);

                    String txStatus = readString(buffer, crc32);
                    TransactionStatus status;
                    try
                    {
                        status = TransactionStatus.valueOf(txStatus);
                    }
                    catch (IllegalArgumentException e)
                    {
                        throw new IOException("Invalid status " + txStatus);
                    }

                    String mergeId = readString(buffer, crc32);

                    newIndexEntries.put(name, new IndexEntry(indexType, name, parentName, status, mergeId));
                }
                long onDiskCRC32 = buffer.getLong();
                if (crc32.getValue() == onDiskCRC32)
                {
                    version = onDiskVersion;
                    indexEntries = newIndexEntries;
                }
                else
                {
                    throw new IOException("Invalid file check sum");
                }
            }
        }

    }

    private String readString(ByteBuffer buffer, CRC32 crc32) throws UnsupportedEncodingException
    {
        int size = buffer.getInt();
        char[] chars = new char[size];
        for (int i = 0; i < size; i++)
        {
            chars[i] = buffer.getChar();
        }
        String string = new String(chars);

        crc32.update(string.getBytes("UTF-8"));
        return string;
    }

    private void writeString(ByteBuffer buffer, CRC32 crc32, String string) throws UnsupportedEncodingException
    {
        char[] chars = string.toCharArray();
        buffer.putInt(chars.length);

        for (int i = 0; i < chars.length; i++)
        {
            buffer.putChar(chars[i]);
        }
        crc32.update(string.getBytes("UTF-8"));
    }

    private void writeStatus() throws IOException
    {
        version++;
        writeStatusToFile(indexInfoChannel);
        writeStatusToFile(indexInfoBackupChannel);
    }

    private void writeStatusToFile(FileChannel channel) throws IOException
    {
        long size = getBufferSize();

        ByteBuffer buffer;
        if (useNIOMemoryMapping)
        {
            MappedByteBuffer mbb = channel.map(MapMode.READ_WRITE, 0, size);
            mbb.load();
            buffer = mbb;
        }
        else
        {
            channel.truncate(size);
            buffer = ByteBuffer.wrap(new byte[(int) size]);
        }

        buffer.position(0);

        buffer.putLong(version);
        CRC32 crc32 = new CRC32();
        crc32.update((int) (version >>> 32) & 0xFFFFFFFF);
        crc32.update((int) (version >>> 0) & 0xFFFFFFFF);

        buffer.putInt(indexEntries.size());
        crc32.update(indexEntries.size());

        for (IndexEntry entry : indexEntries.values())
        {
            String entryType = entry.getType().toString();
            writeString(buffer, crc32, entryType);

            writeString(buffer, crc32, entry.getName());

            writeString(buffer, crc32, entry.getParentName());

            String entryStatus = entry.getStatus().toString();
            writeString(buffer, crc32, entryStatus);

            writeString(buffer, crc32, entry.getMergeId());
        }
        buffer.putLong(crc32.getValue());

        if (useNIOMemoryMapping)
        {
            ((MappedByteBuffer) buffer).force();
        }
        else
        {
            buffer.rewind();
            channel.position(0);
            channel.write(buffer);
        }
    }

    private long getBufferSize()
    {
        long size = 0;
        size += 8;
        size += 4;
        for (IndexEntry entry : indexEntries.values())
        {
            String entryType = entry.getType().toString();
            size += (entryType.length() * 2) + 4;
            size += (entry.getName().length() * 2) + 4;
            size += (entry.getParentName().length() * 2) + 4;
            String entryStatus = entry.getStatus().toString();
            size += (entryStatus.length() * 2) + 4;
            size += (entry.getMergeId().length() * 2) + 4;
        }
        size += 8;
        return size;
    }

    private interface LockWork<Result>
    {
        public Result doWork() throws Exception;
    }

    private <R> R doWithFileLock(LockWork<R> lockWork)
    {
        FileLock fileLock = null;
        R result = null;
        try
        {
            if (indexIsShared)
            {
                fileLock = indexInfoChannel.lock();
                if (!checkVersion())
                {
                    setStatusFromFile();
                    clearOldReaders();
                }
            }
            result = lockWork.doWork();
            return result;
        }
        catch (Throwable exception)
        {

            // Re-throw the exception
            if (exception instanceof RuntimeException)
            {
                throw (RuntimeException) exception;
            }
            else
            {
                throw new RuntimeException("Error during run with lock.", exception);
            }
        }
        finally
        {
            if (fileLock != null)
            {
                try
                {
                    fileLock.release();
                }
                catch (IOException e)
                {
                }
            }
        }
    }

    public static void main(String[] args) throws IOException
    {
        System.setProperty("disableLuceneLocks", "true");

        int repeat = 100;
        final IndexInfo ii = new IndexInfo(new File("c:\\indexTest"));

        long totalTimeA = 0;
        long countA = 0;

        while (true)
        {
            long start = System.nanoTime();
            ii.indexEntries.clear();
            for (int i = 0; i < 100; i++)
            {
                String guid = GUID.generate();
                ii.indexEntries.put(guid, new IndexEntry(IndexType.DELTA, guid, GUID.generate(),
                        TransactionStatus.ACTIVE, ""));
                ii.getDeltaIndexReader(guid);
                ii.setStatus(guid, TransactionStatus.PREPARING, null, null);
                ii.setStatus(guid, TransactionStatus.PREPARED, null, null);
                ii.setStatus(guid, TransactionStatus.COMMITTING, null, null);
                ii.setStatus(guid, TransactionStatus.COMMITTED, null, null);
            }

            long end = System.nanoTime();

            totalTimeA += (end - start);
            countA += repeat;
            float average = countA * 1000000000f / totalTimeA;

            System.out.println("Repeated "
                    + repeat + " in " + ((end - start) / 1000000000.0) + "    average = " + average);
        }
    }
}
