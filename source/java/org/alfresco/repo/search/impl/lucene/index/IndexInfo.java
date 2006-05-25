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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.zip.CRC32;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.search.impl.lucene.LuceneAnalyser;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.util.GUID;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
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
    ReentrantReadWriteLock entriesReadWriteLock = new ReentrantReadWriteLock();

    /**
     * Lock for switching over the main cached reader
     */
    ReentrantReadWriteLock mainIndexReaderReadWriteLock = new ReentrantReadWriteLock();

    /**
     * Read only index readers that also do reference counting.
     */
    private HashMap<String, IndexWriter> referenceCountingReadOnlyIndexReaders = new HashMap<String, IndexWriter>();

    /**
     * Index writers
     */
    private HashMap<String, IndexWriter> indexWriters = new HashMap<String, IndexWriter>();

    /**
     * Index Readers
     */
    private HashMap<String, IndexReader> indexReaders = new HashMap<String, IndexReader>();

    private DictionaryService dictionaryService;

    public IndexInfo(File indexDirectory)
    {
        super();
        this.indexDirectory = indexDirectory;

        if (!this.indexDirectory.exists())
        {
            if (this.indexDirectory.mkdirs())
            {
                throw new AlfrescoRuntimeException("Failed to create index directory");
            }
        }
        if (!this.indexDirectory.isDirectory())
        {
            throw new AlfrescoRuntimeException("The index must be held in a directory");
        }

        File indexInfoFile = new File(this.indexDirectory, INDEX_INFO);
        File indexInfoBackupFile = new File(this.indexDirectory, INDEX_INFO_BACKUP);
        if (createFile(indexInfoFile) && createFile(indexInfoBackupFile))
        {
            version = 0;
        }

        this.indexInfoRAF = openFile(indexInfoFile);
        this.indexInfoChannel = this.indexInfoRAF.getChannel();

        this.indexInfoBackupRAF = openFile(indexInfoBackupFile);
        this.indexInfoBackupChannel = this.indexInfoBackupRAF.getChannel();

        if (version == -1)
        {
            entriesReadWriteLock.writeLock().lock();
            try
            {
                doWithFileLock(new LockWork<Object>()
                {
                    public Object doWork() throws Exception
                    {
                        checkStatus();
                        return null;
                    }

                });
            }
            finally
            {
                entriesReadWriteLock.writeLock().unlock();
            }
        }
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public DictionaryService getDictionaryService()
    {
        return dictionaryService;
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

    private File ensureDeltaExistsAndIsRegistered(String id) throws IOException
    {
        File location = new File(indexDirectory, id);
        if (!IndexReader.indexExists(location))
        {
            IndexWriter creator = new IndexWriter(location, new LuceneAnalyser(dictionaryService), true);
            creator.setUseCompoundFile(true);
            creator.close();
        }
        entriesReadWriteLock.readLock().lock();
        try
        {
            if (!indexEntries.containsKey(id))
            {
                entriesReadWriteLock.writeLock().lock();
                try
                {
                    indexEntries.put(id, new IndexEntry(IndexType.DELTA, id, "", TransactionStatus.ACTIVE, ""));
                }
                finally
                {
                    entriesReadWriteLock.writeLock().unlock();
                }
            }
        }
        finally
        {
            entriesReadWriteLock.readLock().unlock();
        }
        return location;
    }

    public IndexWriter getDeltaIndexWriter(String id) throws IOException
    {
        IndexWriter writer = indexWriters.get(id);
        if (writer == null)
        {
            // close index writer if required
            closeDeltaIndexReader(id);
            File location = ensureDeltaExistsAndIsRegistered(id);
            writer = new IndexWriter(location, new LuceneAnalyser(dictionaryService), false);
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

    public IndexReader getMainIndexReferenceCountingReadOnlyIndexReader()
    {
        return null;
    }

    public IndexReader getMainIndexReferenceCountingReadOnlyIndexReader(String id)
    {
        return null;
    }

    public void setStatus(String id, TransactionStatus status, Set<Term> toDelete)
    {

    }

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
    private void checkStatus() throws IOException
    {
        try
        {
            setStatusFromFile(indexInfoChannel);
        }
        catch (IOException e)
        {
            // The first data file is corrupt so we fall back to the back up
            System.out.println("BACKUP");
            setStatusFromFile(indexInfoBackupChannel);
        }
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

    public interface LockWork<Result>
    {
        public Result doWork() throws Exception;
    }

    public <R> R doWithFileLock(LockWork<R> lockWork)
    {
        FileLock fileLock = null;
        R result = null;
        try
        {
            fileLock = indexInfoChannel.lock();
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

    public static void main(String[] args)
    {
        int repeat = 100;
        final IndexInfo ii = new IndexInfo(new File("c:\\indexTest"));
        ii.indexEntries.clear();

        for (int i = 0; i < 100; i++)
        {
            String guid = GUID.generate();
            ii.indexEntries.put(guid, new IndexEntry(IndexType.INDEX, guid, GUID.generate(),
                    TransactionStatus.COMMITTED, ""));
        }

        long totalTime = 0;
        long count = 0;

        while (true)
        {
            long start = System.nanoTime();

            for (int i = 0; i < repeat; i++)
            {
                ii.entriesReadWriteLock.writeLock().lock();
                try
                {
                    ii.doWithFileLock(new LockWork<Object>()
                    {
                        public Object doWork() throws Exception
                        {

                            ii.version = 0;
                            ii.checkStatus();
                            ii.writeStatus();
                            return null;
                        }
                    });
                }
                finally
                {
                    ii.entriesReadWriteLock.writeLock().unlock();
                }
            }

            long end = System.nanoTime();

            totalTime += (end - start);
            count += repeat;
            float average = count * 1000000000f / totalTime;

            System.out.println("Repeated "
                    + repeat + " in " + ((end - start) / 1000000000.0) + "    average = " + average);
        }
    }
}
