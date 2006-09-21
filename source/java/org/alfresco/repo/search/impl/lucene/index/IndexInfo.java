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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.zip.CRC32;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.search.IndexerException;
import org.alfresco.repo.search.impl.lucene.FilterIndexReaderByNodeRefs2;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.GUID;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.alfresco.repo.search.impl.lucene.analysis.AlfrescoStandardAnalyser;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.InputStream;
import org.apache.lucene.store.OutputStream;
import org.apache.lucene.store.RAMDirectory;

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
 * NOTE: Public methods lock as required, the private methods assume that the appropriate locks have been obtained.
 * 
 * TODO: Write element status into individual directories. This would be enough for recovery if both index files are lost or corrupted.
 * 
 * TODO: Tidy up index status at start up or after some time. How long would you leave a merge to run?
 * 
 * @author Andy Hind
 */
public class IndexInfo
{
    private static Logger s_logger = Logger.getLogger(IndexInfo.class);

    private static final boolean useNIOMemoryMapping = true;

    /**
     * The default name for the file that holds the index information
     */
    private static String INDEX_INFO = "IndexInfo";

    /**
     * The default name for the back up file that holds the index information
     */
    private static String INDEX_INFO_BACKUP = "IndexInfoBackup";

    private static String INDEX_INFO_DELETIONS = "IndexInfoDeletions";

    private static String OLD_INDEX = "index";

    /**
     * Is this index shared by more than one repository? We can make many lock optimisations if the index is not shared.
     */
    private boolean indexIsShared = false;

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
    private Map<String, IndexWriter> indexWriters = Collections.synchronizedMap(new HashMap<String, IndexWriter>());

    /**
     * Index Readers for deltas
     */
    private Map<String, IndexReader> indexReaders = Collections.synchronizedMap(new HashMap<String, IndexReader>());

    /**
     * Map of state transitions
     */
    private EnumMap<TransactionStatus, Transition> transitions = new EnumMap<TransactionStatus, Transition>(
            TransactionStatus.class);

    private ConcurrentLinkedQueue<String> deleteQueue = new ConcurrentLinkedQueue<String>();

    private Cleaner cleaner = new Cleaner();

    private Thread cleanerThread;

    private Merger merger = new Merger();

    private Thread mergerThread;

    private Directory emptyIndex = new RAMDirectory();

    private static HashMap<File, IndexInfo> indexInfos = new HashMap<File, IndexInfo>();

    private int maxDocsForInMemoryMerge = 10000;

    private int writerMinMergeDocs = 1000;

    private int writerMergeFactor = 5;

    private int writerMaxMergeDocs = 1000000;

    private boolean writerUseCompoundFile = true;

    private int mergerMinMergeDocs = 1000;

    private int mergerMergeFactor = 5;

    private int mergerMaxMergeDocs = 1000000;

    private boolean mergerUseCompoundFile = true;

    private int mergerTargetOverlays = 5;

    // TODO: Something to control the maximum number of overlays

    private boolean enableCleanerThread = true;

    private boolean enableMergerThread = true;

    static
    {
        System.setProperty("disableLuceneLocks", "true");
    }

    public static synchronized IndexInfo getIndexInfo(File file)
    {
        IndexInfo indexInfo = indexInfos.get(file);
        if (indexInfo == null)
        {
            indexInfo = new IndexInfo(file);
            indexInfos.put(file, indexInfo);
        }
        return indexInfo;
    }

    /**
     * Construct an index in the given directory.
     * 
     * @param indexDirectory
     */
    private IndexInfo(File indexDirectory)
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
        if (version == 0)
        {
            // Check if an old style index exists

            final File oldIndex = new File(this.indexDirectory, OLD_INDEX);
            if (IndexReader.indexExists(oldIndex))
            {
                getWriteLock();
                try
                {
                    doWithFileLock(new LockWork<Object>()
                    {
                        public Object doWork() throws Exception
                        {
                            IndexWriter writer;
                            try
                            {
                                writer = new IndexWriter(oldIndex, new AlfrescoStandardAnalyser(), false);
                                writer.setUseCompoundFile(writerUseCompoundFile);
                                writer.minMergeDocs = writerMinMergeDocs;
                                writer.mergeFactor = writerMergeFactor;
                                writer.maxMergeDocs = writerMaxMergeDocs;
                                writer.optimize();
                                long docs = writer.docCount();
                                writer.close();

                                indexEntries.put(OLD_INDEX, new IndexEntry(IndexType.INDEX, OLD_INDEX, "",
                                        TransactionStatus.COMMITTED, "", docs, 0, false));

                                writeStatus();
                            }
                            catch (IOException e)
                            {
                                throw new IndexerException("Failed to optimise old index");
                            }
                            return null;
                        }
                    });
                }
                finally
                {
                    releaseWriteLock();
                }

            }
        }

        else if (version == -1)
        {
            getWriteLock();
            try
            {
                doWithFileLock(new LockWork<Object>()
                {
                    public Object doWork() throws Exception
                    {
                        setStatusFromFile();

                        if (!indexIsShared)
                        {
                            HashSet<String> deletable = new HashSet<String>();
                            // clean up
                            for (IndexEntry entry : indexEntries.values())
                            {
                                switch (entry.getStatus())
                                {
                                case ACTIVE:
                                case MARKED_ROLLBACK:
                                case NO_TRANSACTION:
                                case PREPARING:
                                case ROLLEDBACK:
                                case ROLLINGBACK:
                                case MERGE_TARGET:
                                case UNKNOWN:
                                case PREPARED:
                                case DELETABLE:
                                    if (s_logger.isInfoEnabled())
                                    {
                                        s_logger.info("Deleting index entry " + entry);
                                    }
                                    entry.setStatus(TransactionStatus.DELETABLE);
                                    deletable.add(entry.getName());
                                    break;
                                case COMMITTED_DELETING:
                                case MERGE:
                                    if (s_logger.isInfoEnabled())
                                    {
                                        s_logger.info("Resetting merge to committed " + entry);
                                    }
                                    entry.setStatus(TransactionStatus.COMMITTED);
                                    break;
                                case COMMITTING:
                                    // do the commit
                                    if (s_logger.isInfoEnabled())
                                    {
                                        s_logger.info("Committing " + entry);
                                    }
                                    entry.setStatus(TransactionStatus.COMMITTED);
                                    mainIndexReader = null;
                                    break;
                                case COMMITTED:
                                default:
                                    // nothing to do
                                    break;
                                }
                            }
                            for (String id : deletable)
                            {
                                indexEntries.remove(id);
                                deleteQueue.add(id);
                            }
                            synchronized (cleaner)
                            {
                                cleaner.notify();
                            }
                            synchronized (merger)
                            {
                                merger.notify();
                            }
                            writeStatus();
                        }
                        return null;
                    }

                });
            }
            finally
            {
                releaseWriteLock();
            }
        }
        // TODO: Add unrecognised folders for deletion.

        if (enableCleanerThread)
        {
            cleanerThread = new Thread(cleaner);
            cleanerThread.setDaemon(true);
            cleanerThread.setName("Index cleaner thread "+indexDirectory);
            cleanerThread.start();
        }

       
        if (enableMergerThread)
        {
            mergerThread = new Thread(merger);
            mergerThread.setDaemon(true);
            mergerThread.setName("Index merger thread "+indexDirectory);
            mergerThread.start();
        }

        IndexWriter writer;
        try
        {
            writer = new IndexWriter(emptyIndex, new AlfrescoStandardAnalyser(), true);
            writer.setUseCompoundFile(writerUseCompoundFile);
            writer.minMergeDocs = writerMinMergeDocs;
            writer.mergeFactor = writerMergeFactor;
            writer.maxMergeDocs = writerMaxMergeDocs;
        }
        catch (IOException e)
        {
            throw new IndexerException("Failed to create an empty in memory index!");
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
        if (id == null)
        {
            throw new IndexerException("\"null\" is not a valid identifier for a transaction");
        }

        // No read lock required as the delta should be bound to one thread only
        // Index readers are simply thread safe
        IndexReader reader = indexReaders.get(id);
        if (reader == null)
        {
            // close index writer if required
            closeDeltaIndexWriter(id);
            // Check the index knows about the transaction
            reader = buildAndRegisterDeltaReader(id);
            indexReaders.put(id, reader);
        }
        return reader;
    }

    private IndexReader buildAndRegisterDeltaReader(String id) throws IOException
    {
        IndexReader reader;
        File location = ensureDeltaIsRegistered(id);
        // Create a dummy index reader to deal with empty indexes and not persist these.
        if (IndexReader.indexExists(location))
        {
            reader = IndexReader.open(location);
        }
        else
        {
            reader = IndexReader.open(emptyIndex);
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
    private File ensureDeltaIsRegistered(String id) throws IOException
    {
        if (id == null)
        {
            throw new IndexerException("\"null\" is not a valid identifier for a transaction");
        }

        // A write lock is required if we have to update the local index entries.
        // There should only be one thread trying to access this delta.
        File location = new File(indexDirectory, id);
        getReadLock();
        try
        {
            if (!indexEntries.containsKey(id))
            {
                releaseReadLock();
                // release to upgrade to write lock
                getWriteLock();
                try
                {
                    // Make sure the index exists
                    if (!indexEntries.containsKey(id))
                    {
                        indexEntries.put(id, new IndexEntry(IndexType.DELTA, id, "", TransactionStatus.ACTIVE, "", 0,
                                0, false));
                    }
                }
                finally
                {
                    // Downgrade lock
                    getReadLock();
                    releaseWriteLock();
                }
            }
        }
        finally
        {
            // Release the lock
            releaseReadLock();
        }
        return location;
    }

    private IndexWriter makeDeltaIndexWriter(File location, Analyzer analyzer) throws IOException
    {
        if (!IndexReader.indexExists(location))
        {
            IndexWriter creator = new IndexWriter(location, analyzer, true);
            creator.setUseCompoundFile(writerUseCompoundFile);
            creator.minMergeDocs = writerMinMergeDocs;
            creator.mergeFactor = writerMergeFactor;
            creator.maxMergeDocs = writerMaxMergeDocs;
            return creator;
        }
        return null;
    }

    public IndexWriter getDeltaIndexWriter(String id, Analyzer analyzer) throws IOException
    {
        if (id == null)
        {
            throw new IndexerException("\"null\" is not a valid identifier for a transaction");
        }

        // No read lock required as the delta should be bound to one thread only
        IndexWriter writer = indexWriters.get(id);
        if (writer == null)
        {
            // close index writer if required
            closeDeltaIndexReader(id);
            File location = ensureDeltaIsRegistered(id);
            writer = makeDeltaIndexWriter(location, analyzer);
            if (writer == null)
            {
                writer = new IndexWriter(location, analyzer, false);
                writer.setUseCompoundFile(writerUseCompoundFile);
                writer.minMergeDocs = writerMinMergeDocs;
                writer.mergeFactor = writerMergeFactor;
                writer.maxMergeDocs = writerMaxMergeDocs;
            }
            indexWriters.put(id, writer);
        }
        return writer;
    }

    public void closeDeltaIndexReader(String id) throws IOException
    {
        if (id == null)
        {
            throw new IndexerException("\"null\" is not a valid identifier for a transaction");
        }

        // No lock required as the delta applied to one thread. The delta is still active.
        IndexReader reader = indexReaders.remove(id);
        if (reader != null)
        {
            reader.close();
        }
    }

    public void closeDeltaIndexWriter(String id) throws IOException
    {
        if (id == null)
        {
            throw new IndexerException("\"null\" is not a valid identifier for a transaction");
        }

        // No lock required as the delta applied to one thread. The delta is still active.
        IndexWriter writer = indexWriters.remove(id);
        if (writer != null)
        {
            writer.close();
        }
    }

    public void closeDelta(String id) throws IOException
    {
        if (id == null)
        {
            throw new IndexerException("\"null\" is not a valid identifier for a transaction");
        }
        closeDeltaIndexReader(id);
        closeDeltaIndexWriter(id);
    }

    public Set<NodeRef> getDeletions(String id) throws IOException
    {
        if (id == null)
        {
            throw new IndexerException("\"null\" is not a valid identifier for a transaction");
        }
        // Check state
        Set<NodeRef> deletions = new HashSet<NodeRef>();
        File location = new File(indexDirectory, id);
        File file = new File(location, INDEX_INFO_DELETIONS);
        if (!file.exists())
        {
            return Collections.<NodeRef> emptySet();
        }
        DataInputStream is = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
        int size = is.readInt();
        for (int i = 0; i < size; i++)
        {
            String ref = is.readUTF();
            deletions.add(new NodeRef(ref));
        }
        is.close();
        return deletions;

    }

    public void setPreparedState(String id, Set<NodeRef> toDelete, long documents, boolean deleteNodesOnly)
            throws IOException
    {
        if (id == null)
        {
            throw new IndexerException("\"null\" is not a valid identifier for a transaction");
        }
        // Check state
        if (toDelete.size() > 0)
        {
            File location = new File(indexDirectory, id);
            if (!location.exists())
            {
                if (!location.mkdirs())
                {
                    throw new IndexerException("Failed to make index directory " + location);
                }
            }
            DataOutputStream os = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(location,
                    INDEX_INFO_DELETIONS))));
            os.writeInt(toDelete.size());
            for (NodeRef ref : toDelete)
            {
                os.writeUTF(ref.toString());
            }
            os.flush();
            os.close();
        }
        getWriteLock();
        try
        {
            IndexEntry entry = indexEntries.get(id);
            if (entry == null)
            {
                throw new IndexerException("Invalid index delta id " + id);
            }
            if ((entry.getStatus() != TransactionStatus.PREPARING)
                    && (entry.getStatus() != TransactionStatus.COMMITTING))
            {
                throw new IndexerException("Deletes and doc count can only be set on a preparing index");
            }
            entry.setDocumentCount(documents);
            entry.setDeletions(toDelete.size());
            entry.setDeletOnlyNodes(deleteNodesOnly);
        }
        finally
        {
            releaseWriteLock();
        }
    }

    public IndexReader getMainIndexReferenceCountingReadOnlyIndexReader() throws IOException
    {
        getReadLock();
        try
        {
            if (indexIsShared && !checkVersion())
            {
                releaseReadLock();
                getWriteLock();
                try
                {
                    mainIndexReader = null;
                }
                finally
                {
                    getReadLock();
                    releaseWriteLock();
                }
            }

            if (mainIndexReader == null)
            {
                releaseReadLock();
                getWriteLock();
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
                    getReadLock();
                    releaseWriteLock();
                }
            }
            ReferenceCounting refCount = (ReferenceCounting) mainIndexReader;
            refCount.incrementReferenceCount();
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("Main index reader references = " + refCount.getReferenceCount());
            }
            return mainIndexReader;
        }
        finally
        {
            releaseReadLock();
        }
    }

    public IndexReader getMainIndexReferenceCountingReadOnlyIndexReader(String id, Set<NodeRef> deletions,
            boolean deleteOnlyNodes) throws IOException
    {
        if (id == null)
        {
            throw new IndexerException("\"null\" is not a valid identifier for a transaction");
        }
        getReadLock();
        try
        {
            if (indexIsShared && !checkVersion())
            {
                releaseReadLock();
                getWriteLock();
                try
                {
                    mainIndexReader = null;
                }
                finally
                {
                    getReadLock();
                    releaseWriteLock();
                }
            }

            if (mainIndexReader == null)
            {
                releaseReadLock();
                getWriteLock();
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
                    getReadLock();
                    releaseWriteLock();
                }
            }
            ReferenceCounting refCount = (ReferenceCounting) mainIndexReader;
            refCount.incrementReferenceCount();
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("Main index reader references = " + refCount.getReferenceCount());
            }
            // Combine the index delta with the main index
            // Make sure the index is written to disk
            // TODO: Should use the in memory index but we often end up forcing to disk anyway.
            // Is it worth it?
            // luceneIndexer.flushPending();
            
            IndexReader deltaReader =  buildAndRegisterDeltaReader(id);
            IndexReader reader = new MultiReader(new IndexReader[] {
                    new FilterIndexReaderByNodeRefs2(mainIndexReader, deletions, deleteOnlyNodes), deltaReader });
            reader = ReferenceCountingReadOnlyIndexReaderFactory.createReader("MainReader"+id, reader);
            ReferenceCounting refCounting = (ReferenceCounting)reader;
            refCounting.incrementReferenceCount();
            refCounting.setInvalidForReuse();
            return reader;
        }
        finally
        {
            releaseReadLock();
        }
    }

    public void setStatus(final String id, final TransactionStatus state, final Set<Term> toDelete, final Set<Term> read)
            throws IOException
    {
        if (id == null)
        {
            throw new IndexerException("\"null\" is not a valid identifier for a transaction");
        }
        final Transition transition = getTransition(state);
        getWriteLock();
        try
        {
            if (transition.requiresFileLock())
            {
                doWithFileLock(new LockWork<Object>()
                {
                    public Object doWork() throws Exception
                    {
                        if (s_logger.isDebugEnabled())
                        {
                            s_logger.debug("Start Index " + id + " state = " + state);
                        }
                        dumpInfo();
                        transition.transition(id, toDelete, read);
                        if (s_logger.isDebugEnabled())
                        {
                            s_logger.debug("End Index " + id + " state = " + state);
                        }
                        dumpInfo();
                        return null;
                    }

                });
            }
            else
            {
                if (s_logger.isDebugEnabled())
                {
                    s_logger.debug("Start Index " + id + " state = " + state);
                }
                dumpInfo();
                transition.transition(id, toDelete, read);
                if (s_logger.isDebugEnabled())
                {
                    s_logger.debug("End Index " + id + " state = " + state);
                }
                dumpInfo();
            }
        }
        finally
        {
            releaseWriteLock();
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
        transitions.put(TransactionStatus.DELETABLE, new DeletableTransition());
        transitions.put(TransactionStatus.ACTIVE, new ActiveTransition());
    }

    private interface Transition
    {
        void transition(String id, Set<Term> toDelete, Set<Term> read) throws IOException;

        boolean requiresFileLock();
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
            }
            else
            {
                throw new IndexerException("Invalid transition for "
                        + id + " from " + entry.getStatus() + " to " + TransactionStatus.PREPARING);
            }
        }

        public boolean requiresFileLock()
        {
            return !TransactionStatus.PREPARING.isTransient();
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
                if ((entry.getDeletions() + entry.getDocumentCount()) > 0)
                {
                    LinkedHashMap<String, IndexEntry> reordered = new LinkedHashMap<String, IndexEntry>();
                    boolean addedPreparedEntry = false;
                    for (IndexEntry current : indexEntries.values())
                    {
                        if (!current.getStatus().canBeReordered())
                        {
                            reordered.put(current.getName(), current);
                        }
                        else if (!addedPreparedEntry)
                        {
                            reordered.put(entry.getName(), entry);
                            reordered.put(current.getName(), current);
                            addedPreparedEntry = true;
                        }
                        else if (current.getName().equals(entry.getName()))
                        {
                            // skip as we are moving it
                        }
                        else
                        {
                            reordered.put(current.getName(), current);
                        }
                    }

                    if (indexEntries.size() != reordered.size())
                    {
                        indexEntries = reordered;
                        dumpInfo();
                        throw new IndexerException("Concurrent modification error");
                    }
                    indexEntries = reordered;
                }
                entry.setStatus(TransactionStatus.PREPARED);
                if ((entry.getDeletions() + entry.getDocumentCount()) > 0)
                {
                    writeStatus();
                }
            }
            else
            {
                throw new IndexerException("Invalid transition for "
                        + id + " from " + entry.getStatus() + " to " + TransactionStatus.PREPARED);
            }
        }

        public boolean requiresFileLock()
        {
            return !TransactionStatus.PREPARED.isTransient();
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
            }
            else
            {
                throw new IndexerException("Invalid transition for "
                        + id + " from " + entry.getStatus() + " to " + TransactionStatus.COMMITTING);
            }
        }

        public boolean requiresFileLock()
        {
            return !TransactionStatus.COMMITTING.isTransient();
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
                if ((entry.getDocumentCount() + entry.getDeletions()) == 0)
                {
                    indexEntries.remove(id);
                }
                else
                {
                    entry.setStatus(TransactionStatus.COMMITTED);
                    // TODO: optimise to index for no deletions
                    // have to allow for this in the application of deletions,
                    writeStatus();
                    if (mainIndexReader != null)
                    {
                        if (s_logger.isDebugEnabled())
                        {
                            s_logger.debug("... invalidating main index reader");
                        }
                        ((ReferenceCounting) mainIndexReader).setInvalidForReuse();
                        mainIndexReader = null;
                    }

                    synchronized (merger)
                    {
                        merger.notify();
                    }
                }
            }
            else
            {
                throw new IndexerException("Invalid transition for "
                        + id + " from " + entry.getStatus() + " to " + TransactionStatus.COMMITTED);
            }
        }

        public boolean requiresFileLock()
        {
            return !TransactionStatus.COMMITTED.isTransient();
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

        public boolean requiresFileLock()
        {
            return !TransactionStatus.ROLLINGBACK.isTransient();
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

        public boolean requiresFileLock()
        {
            return !TransactionStatus.ROLLEDBACK.isTransient();
        }
    }

    private class DeletableTransition implements Transition
    {
        public void transition(String id, Set<Term> toDelete, Set<Term> read) throws IOException
        {
            IndexEntry entry = indexEntries.get(id);
            if (entry == null)
            {
                throw new IndexerException("Unknown transaction " + id);
            }

            if (TransactionStatus.DELETABLE.follows(entry.getStatus()))
            {
                indexEntries.remove(id);
                deleteQueue.add(id);
                synchronized (cleaner)
                {
                    cleaner.notify();
                }
                writeStatus();
            }
            else
            {
                throw new IndexerException("Invalid transition for "
                        + id + " from " + entry.getStatus() + " to " + TransactionStatus.DELETABLE);
            }
        }

        public boolean requiresFileLock()
        {
            return !TransactionStatus.DELETABLE.isTransient();
        }
    }

    private class ActiveTransition implements Transition
    {
        public void transition(String id, Set<Term> toDelete, Set<Term> read) throws IOException
        {
            IndexEntry entry = indexEntries.get(id);
            if (entry != null)
            {
                if (entry.getStatus() != TransactionStatus.ACTIVE)
                {
                    throw new IndexerException("TX Already active " + id);
                }
            }

            if (TransactionStatus.ACTIVE.follows(null))
            {
                indexEntries
                        .put(id, new IndexEntry(IndexType.DELTA, id, "", TransactionStatus.ACTIVE, "", 0, 0, false));
            }
            else
            {
                throw new IndexerException("Invalid transition for "
                        + id + " from " + entry.getStatus() + " to " + TransactionStatus.ACTIVE);
            }
        }

        public boolean requiresFileLock()
        {
            return !TransactionStatus.ACTIVE.isTransient();
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
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("... invalidating sub reader " + id);
            }
            ReferenceCounting referenceCounting = (ReferenceCounting) reader;
            referenceCounting.setInvalidForReuse();
            hasInvalid = true;
        }
        if (hasInvalid)
        {
            if (mainIndexReader != null)
            {
                if (s_logger.isDebugEnabled())
                {
                    s_logger.debug("... invalidating main index reader");
                }
                ((ReferenceCounting) mainIndexReader).setInvalidForReuse();
            }
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
                    if (entry.getType() == IndexType.INDEX)
                    {
                        reader = new MultiReader(new IndexReader[] { reader, subReader });
                    }
                    else if (entry.getType() == IndexType.DELTA)
                    {
                        reader = new MultiReader(new IndexReader[] {
                                new FilterIndexReaderByNodeRefs2(reader, getDeletions(entry.getName()), entry
                                        .isDeletOnlyNodes()), subReader });
                    }
                }
            }
        }
        if (reader == null)
        {
            reader = IndexReader.open(emptyIndex);
        }
        reader = ReferenceCountingReadOnlyIndexReaderFactory.createReader("MainReader", reader);
        return reader;
    }

    private IndexReader getReferenceCountingIndexReader(String id) throws IOException
    {
        IndexReader reader = referenceCountingReadOnlyIndexReaders.get(id);
        if (reader == null)
        {
            File location = new File(indexDirectory, id);
            if (IndexReader.indexExists(location))
            {
                reader = IndexReader.open(location);
            }
            else
            {
                reader = IndexReader.open(emptyIndex);
            }
            reader = ReferenceCountingReadOnlyIndexReaderFactory.createReader(id, reader);
            referenceCountingReadOnlyIndexReaders.put(id, reader);
        }
        ReferenceCounting referenceCounting = (ReferenceCounting) reader;
        referenceCounting.incrementReferenceCount();
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
            try
            {
                return checkVersion(indexInfoBackupChannel);
            }
            catch (IOException ee)
            {
                return false;
            }
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
                // Not all state is saved some is specific to this index so we need to add the transient stuff.
                // Until things are committed they are not shared unless it is prepared
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

                    long documentCount = buffer.getLong();
                    crc32.update((int) (documentCount >>> 32) & 0xFFFFFFFF);
                    crc32.update((int) (documentCount >>> 0) & 0xFFFFFFFF);

                    long deletions = buffer.getLong();
                    crc32.update((int) (deletions >>> 32) & 0xFFFFFFFF);
                    crc32.update((int) (deletions >>> 0) & 0xFFFFFFFF);

                    byte deleteOnlyNodesFlag = buffer.get();
                    crc32.update(deleteOnlyNodesFlag);
                    boolean isDeletOnlyNodes = deleteOnlyNodesFlag == 1;

                    if (!status.isTransient())
                    {
                        newIndexEntries.put(name, new IndexEntry(indexType, name, parentName, status, mergeId,
                                documentCount, deletions, isDeletOnlyNodes));
                    }
                }
                long onDiskCRC32 = buffer.getLong();
                if (crc32.getValue() == onDiskCRC32)
                {
                    for (IndexEntry entry : indexEntries.values())
                    {
                        if (entry.getStatus().isTransient())
                        {
                            newIndexEntries.put(entry.getName(), entry);
                        }
                    }
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
        byte[] bytes = new byte[size];
        buffer.get(bytes);
        char[] chars = new char[size];
        for (int i = 0; i < size; i++)
        {
            chars[i] = (char) bytes[i];
        }
        crc32.update(bytes);
        return new String(chars);
    }

    private void writeString(ByteBuffer buffer, CRC32 crc32, String string) throws UnsupportedEncodingException
    {
        char[] chars = string.toCharArray();
        byte[] bytes = new byte[chars.length];
        for (int i = 0; i < chars.length; i++)
        {
            if (chars[i] > 0xFF)
            {
                throw new UnsupportedEncodingException();
            }
            bytes[i] = (byte) chars[i];
        }
        buffer.putInt(bytes.length);
        buffer.put(bytes);
        crc32.update(bytes);
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

            buffer.putLong(entry.getDocumentCount());
            crc32.update((int) (entry.getDocumentCount() >>> 32) & 0xFFFFFFFF);
            crc32.update((int) (entry.getDocumentCount() >>> 0) & 0xFFFFFFFF);

            buffer.putLong(entry.getDeletions());
            crc32.update((int) (entry.getDeletions() >>> 32) & 0xFFFFFFFF);
            crc32.update((int) (entry.getDeletions() >>> 0) & 0xFFFFFFFF);

            buffer.put(entry.isDeletOnlyNodes() ? (byte) 1 : (byte) 0);
            crc32.update(entry.isDeletOnlyNodes() ? new byte[] { (byte) 1 } : new byte[] { (byte) 0 });
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

    private long getBufferSize() throws IOException
    {
        long size = 0;
        size += 8;
        size += 4;
        for (IndexEntry entry : indexEntries.values())
        {
            String entryType = entry.getType().toString();
            size += (entryType.length()) + 4;
            size += (entry.getName().length()) + 4;
            size += (entry.getParentName().length()) + 4;
            String entryStatus = entry.getStatus().toString();
            size += (entryStatus.length()) + 4;
            size += (entry.getMergeId().length()) + 4;
            size += 8;
            size += 8;
            size += 1;
        }
        size += 8;
        return size;
    }

    public interface LockWork<Result>
    {
        public Result doWork() throws Exception;
    }

    public <R> R doWithWriteLock(LockWork<R> lockWork)
    {
        getWriteLock();
        try
        {
            return doWithFileLock(lockWork);
        }
        finally
        {
            releaseWriteLock();
        }
    }

    private <R> R doWithFileLock(LockWork<R> lockWork)
    {
        FileLock fileLock = null;
        R result = null;
        try
        {
            if (indexIsShared)
            {
                long start = 0l;
                if (s_logger.isDebugEnabled())
                {
                    s_logger.debug(" ... waiting for file lock");
                    start = System.nanoTime();
                }
                fileLock = indexInfoChannel.lock();
                if (s_logger.isDebugEnabled())
                {
                    long end = System.nanoTime();
                    s_logger.debug(" ... got file lock in " + ((end - start)/10e6f) + " ms");
                }
                if (!checkVersion())
                {
                    setStatusFromFile();
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
                    if (s_logger.isDebugEnabled())
                    {
                        s_logger.debug(" ... released file lock");
                    }
                }
                catch (IOException e)
                {
                }
            }
        }
    }

    /**
     * Helper to print out index information
     * 
     * @param args
     */
    public static void main(String[] args)
    {

        String indexLocation = args[0];
        IndexInfo ii = new IndexInfo(new File(indexLocation));
        while (true)
        {
            ii.readWriteLock.writeLock().lock();
            try
            {
                System.out.println("Entry List for " + indexLocation);
                System.out.println("   Size = " + ii.indexEntries.size());
                int i = 0;
                for (IndexEntry entry : ii.indexEntries.values())
                {
                    System.out.println("\t" + (i++) + "\t" + entry.toString());
                }
            }
            finally
            {
                ii.releaseWriteLock();
            }
        }
    }

    /**
     * Clean up support.
     * 
     * @author Andy Hind
     */
    private class Cleaner implements Runnable
    {

        public void run()
        {
            boolean runnable = true;
            while (runnable)
            {
                String id = null;
                HashSet<String> fails = new HashSet<String>();
                while ((id = deleteQueue.poll()) != null)
                {
                    if (s_logger.isDebugEnabled())
                    {
                        s_logger.debug("Expunging " + id + " remaining " + deleteQueue.size());
                    }
                    // try and delete
                    File location = new File(indexDirectory, id);
                    if (!deleteDirectory(location))
                    {
                        if (s_logger.isDebugEnabled())
                        {
                            s_logger.debug("DELETE FAILED");
                        }
                        // try again later
                        fails.add(id);
                    }
                }
                deleteQueue.addAll(fails);
                synchronized (this)
                {
                    try
                    {
                        // wait for more deletes
                        this.wait();
                    }
                    catch (InterruptedException e)
                    {
                        runnable = false;
                    }
                }
            }

        }

        private boolean deleteDirectory(File file)
        {
            File[] children = file.listFiles();
            if (children != null)
            {
                for (int i = 0; i < children.length; i++)
                {
                    File child = children[i];
                    if (child.isDirectory())
                    {
                        deleteDirectory(child);
                    }
                    else
                    {
                        if (child.exists() && !child.delete() && child.exists())
                        {
                            return false;
                        }
                    }
                }
            }
            if (file.exists() && !file.delete() && file.exists())
            {
                return false;
            }
            return true;
        }

    }

    /**
     * Supported by one thread.
     * 
     * 1) If the first index is a delta we can just change it to an index.
     * 
     * There is now here to apply the deletions
     * 
     * 2) Merge indexes
     * 
     * Combine indexes together according to the target index merge strategy. This is a trade off to make an optimised index but not spend too much time merging and optimising
     * small merges.
     * 
     * 3) Apply next deletion set to indexes
     * 
     * Apply the deletions for the first delta to all the other indexes. Deletes can be applied with relative impunity. If any are applied they take effect as required.
     * 
     * 1) 2) and 3) are mutually exclusive try in order
     * 
     * This could be supported in another thread
     * 
     * 4) Merge deltas
     * 
     * Merge two index deltas together. Starting at the end. Several merges can be going on at once.
     * 
     * a) Find merge b) Set state c) apply deletions to the previous delta d) update state e) add deletions to the previous delta deletion list f) update state
     * 
     */

    private enum MergeAction
    {
        NONE, MERGE_INDEX, APPLY_DELTA_DELETION, MERGE_DELTA
    }

    private class Merger implements Runnable
    {
        public void run()
        {
            boolean running = true;

            while (running)
            {
                try
                {
                    // Get the read local to decide what to do
                    // Single JVM to start with
                    MergeAction action = MergeAction.NONE;

                    getReadLock();
                    try
                    {
                        if (indexIsShared && !checkVersion())
                        {
                            releaseReadLock();
                            getWriteLock();
                            try
                            {
                                // Sync with disk image if required
                                doWithFileLock(new LockWork<Object>()
                                {
                                    public Object doWork() throws Exception
                                    {
                                        return null;
                                    }
                                });
                            }
                            finally
                            {
                                try
                                {
                                    getReadLock();
                                }
                                finally
                                {
                                    releaseWriteLock();
                                }
                            }
                        }

                        int indexes = 0;
                        boolean mergingIndexes = false;
                        int deltas = 0;
                        boolean applyingDeletions = false;

                        for (IndexEntry entry : indexEntries.values())
                        {
                            if (entry.getType() == IndexType.INDEX)
                            {
                                indexes++;
                                if (entry.getStatus() == TransactionStatus.MERGE)
                                {
                                    mergingIndexes = true;
                                }
                            }
                            else if (entry.getType() == IndexType.DELTA)
                            {
                                if (entry.getStatus() == TransactionStatus.COMMITTED)
                                {
                                    deltas++;
                                }
                                if (entry.getStatus() == TransactionStatus.COMMITTED_DELETING)
                                {
                                    applyingDeletions = true;
                                }
                            }
                        }

                        if (s_logger.isDebugEnabled())
                        {
                            s_logger.debug("Indexes = " + indexes);
                            s_logger.debug("Merging = " + mergingIndexes);
                            s_logger.debug("Deltas = " + deltas);
                            s_logger.debug("Deleting = " + applyingDeletions);
                        }

                        if (!mergingIndexes && !applyingDeletions)
                        {

                            if ((indexes > mergerMergeFactor) || (deltas > mergerTargetOverlays))
                            {
                                if (indexes > deltas)
                                {
                                    // Try merge
                                    action = MergeAction.MERGE_INDEX;
                                }
                                else
                                {
                                    // Try delete
                                    action = MergeAction.APPLY_DELTA_DELETION;

                                }
                            }
                        }
                    }

                    catch (IOException e)
                    {
                        s_logger.error(e);
                    }
                    finally
                    {
                        releaseReadLock();
                    }

                    if (action == MergeAction.APPLY_DELTA_DELETION)
                    {
                        mergeDeletions();
                    }
                    else if (action == MergeAction.MERGE_INDEX)
                    {
                        mergeIndexes();
                    }

                    synchronized (this)
                    {
                        try
                        {
                            this.wait();
                        }
                        catch (InterruptedException e)
                        {
                            // No action - could signal thread termination
                        }
                    }
                }
                catch (Throwable t)
                {
                    s_logger.error(t);
                }
            }

        }

        void mergeDeletions()
        {
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("Deleting ...");
            }

            // lock for deletions
            final LinkedHashMap<String, IndexEntry> toDelete;

            getWriteLock();
            try
            {
                toDelete = doWithFileLock(new LockWork<LinkedHashMap<String, IndexEntry>>()
                {
                    public LinkedHashMap<String, IndexEntry> doWork() throws Exception
                    {
                        LinkedHashMap<String, IndexEntry> set = new LinkedHashMap<String, IndexEntry>();

                        for (IndexEntry entry : indexEntries.values())
                        {
                            if ((entry.getType() == IndexType.INDEX) && (entry.getStatus() == TransactionStatus.MERGE))
                            {
                                return set;
                            }
                            if ((entry.getType() == IndexType.DELTA)
                                    && (entry.getStatus() == TransactionStatus.COMMITTED_DELETING))
                            {
                                return set;
                            }
                        }
                        // Check it is not deleting
                        for (IndexEntry entry : indexEntries.values())
                        {
                            if (entry.getType() == IndexType.DELTA)
                            {
                                if (entry.getStatus() == TransactionStatus.COMMITTED)
                                {
                                    entry.setStatus(TransactionStatus.COMMITTED_DELETING);
                                    set.put(entry.getName(), entry);
                                }
                                else if (entry.getStatus() == TransactionStatus.PREPARED)
                                {
                                    break;
                                }
                            }
                        }
                        if (set.size() > 0)
                        {
                            writeStatus();
                        }
                        return set;

                    }

                });
            }
            finally
            {
                getReadLock();
                releaseWriteLock();
            }

            LinkedHashMap<String, IndexEntry> indexes = new LinkedHashMap<String, IndexEntry>();
            try
            {
                for (IndexEntry entry : indexEntries.values())
                {
                    if (entry.getStatus() == TransactionStatus.COMMITTED_DELETING)
                    {
                        break;
                    }
                    indexes.put(entry.getName(), entry);
                }
            }
            finally
            {
                releaseReadLock();
            }

            // Build readers

            boolean fail = false;

            final HashSet<String> invalidIndexes = new HashSet<String>();

            final HashMap<String, Long> newIndexCounts = new HashMap<String, Long>();

            try
            {
                LinkedHashMap<String, IndexReader> readers = new LinkedHashMap<String, IndexReader>();
                for (IndexEntry entry : indexes.values())
                {
                    File location = new File(indexDirectory, entry.getName());
                    IndexReader reader;
                    if (IndexReader.indexExists(location))
                    {
                        reader = IndexReader.open(location);
                    }
                    else
                    {
                        reader = IndexReader.open(emptyIndex);
                    }
                    readers.put(entry.getName(), reader);
                }

                for (IndexEntry currentDelete : toDelete.values())
                {
                    Set<NodeRef> deletions = getDeletions(currentDelete.getName());
                    for (String key : readers.keySet())
                    {
                        IndexReader reader = readers.get(key);
                        for (NodeRef nodeRef : deletions)
                        {
                            if (currentDelete.isDeletOnlyNodes())
                            {
                                Searcher searcher = new IndexSearcher(reader);

                                TermQuery query = new TermQuery(new Term("ID", nodeRef.toString()));
                                Hits hits = searcher.search(query);
                                if (hits.length() > 0)
                                {
                                    for (int i = 0; i < hits.length(); i++)
                                    {
                                        Document doc = hits.doc(i);
                                        if (doc.getField("ISCONTAINER") == null)
                                        {
                                            reader.delete(hits.id(i));
                                            invalidIndexes.add(key);
                                            // There should only be one thing to delete
                                            // break;
                                        }
                                    }
                                }
                                searcher.close();

                            }
                            else
                            {
                                if (reader.delete(new Term("ID", nodeRef.toString())) > 0)
                                {
                                    invalidIndexes.add(key);
                                }
                            }
                        }

                    }
                    File location = new File(indexDirectory, currentDelete.getName());
                    IndexReader reader;
                    if (IndexReader.indexExists(location))
                    {
                        reader = IndexReader.open(location);
                    }
                    else
                    {
                        reader = IndexReader.open(emptyIndex);
                    }
                    readers.put(currentDelete.getName(), reader);
                }

                for (String key : readers.keySet())
                {
                    IndexReader reader = readers.get(key);
                    // TODO:Set the new document count
                    newIndexCounts.put(key, new Long(reader.numDocs()));
                    reader.close();
                }
            }
            catch (IOException e)
            {
                s_logger.error(e);
                fail = true;
            }

            final boolean wasDeleted = !fail;
            getWriteLock();
            try
            {
                doWithFileLock(new LockWork<Object>()
                {
                    public Object doWork() throws Exception
                    {
                        for (IndexEntry entry : toDelete.values())
                        {
                            entry.setStatus(TransactionStatus.COMMITTED);
                            if (wasDeleted)
                            {
                                entry.setType(IndexType.INDEX);
                                entry.setDeletions(0);
                            }

                        }

                        for (String key : newIndexCounts.keySet())
                        {
                            Long newCount = newIndexCounts.get(key);
                            IndexEntry entry = indexEntries.get(key);
                            entry.setDocumentCount(newCount);
                        }

                        writeStatus();

                        for (String id : invalidIndexes)
                        {
                            IndexReader reader = referenceCountingReadOnlyIndexReaders.remove(id);
                            if (reader != null)
                            {
                                ReferenceCounting referenceCounting = (ReferenceCounting) reader;
                                referenceCounting.setInvalidForReuse();
                                if (s_logger.isDebugEnabled())
                                {
                                    s_logger.debug("... invalidating sub reader after merge" + id);
                                }
                            }
                        }
                        if (invalidIndexes.size() > 0)
                        {
                            if (mainIndexReader != null)
                            {
                                if (s_logger.isDebugEnabled())
                                {
                                    s_logger.debug("... invalidating main index reader after merge");
                                }
                                ((ReferenceCounting) mainIndexReader).setInvalidForReuse();
                            }
                            mainIndexReader = null;
                        }

                        if (s_logger.isDebugEnabled())
                        {
                            for (String id : toDelete.keySet())
                            {
                                s_logger.debug("...applied deletion for " + id);
                            }
                            for (String id : invalidIndexes)
                            {
                                s_logger.debug("...invalidated index " + id);
                            }
                            s_logger.debug("...deleting done");
                        }

                        dumpInfo();

                        return null;
                    }

                });
            }
            finally
            {
                releaseWriteLock();
            }

            // TODO: Flush readers etc

        }

        void mergeIndexes()
        {

            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("Merging...");
            }

            final LinkedHashMap<String, IndexEntry> toMerge;

            getWriteLock();
            try
            {
                toMerge = doWithFileLock(new LockWork<LinkedHashMap<String, IndexEntry>>()
                {
                    public LinkedHashMap<String, IndexEntry> doWork() throws Exception
                    {
                        LinkedHashMap<String, IndexEntry> set = new LinkedHashMap<String, IndexEntry>();

                        for (IndexEntry entry : indexEntries.values())
                        {
                            if ((entry.getType() == IndexType.INDEX) && (entry.getStatus() == TransactionStatus.MERGE))
                            {
                                return set;
                            }
                            if ((entry.getType() == IndexType.DELTA)
                                    && (entry.getStatus() == TransactionStatus.COMMITTED_DELETING))
                            {
                                return set;
                            }
                        }

                        ArrayList<IndexEntry> mergeList = new ArrayList<IndexEntry>();
                        for (IndexEntry entry : indexEntries.values())
                        {
                            if ((entry.getType() == IndexType.INDEX)
                                    && (entry.getStatus() == TransactionStatus.COMMITTED))
                            {
                                mergeList.add(entry);
                            }
                        }

                        int position = findMergeIndex(1, mergerMaxMergeDocs, mergerMergeFactor, mergeList);
                        String firstMergeId = mergeList.get(position).getName();

                        long count = 0;
                        String guid = null;
                        if (position >= 0)
                        {
                            guid = GUID.generate();
                            for (int i = position; i < mergeList.size(); i++)
                            {
                                IndexEntry entry = mergeList.get(i);
                                count += entry.getDocumentCount();
                                set.put(entry.getName(), entry);
                                entry.setStatus(TransactionStatus.MERGE);
                                entry.setMergeId(guid);
                            }
                        }

                        if (set.size() > 0)
                        {
                            IndexEntry target = new IndexEntry(IndexType.INDEX, guid, "",
                                    TransactionStatus.MERGE_TARGET, guid, count, 0, false);
                            set.put(guid, target);
                            // rebuild merged index elements
                            LinkedHashMap<String, IndexEntry> reordered = new LinkedHashMap<String, IndexEntry>();
                            for (IndexEntry current : indexEntries.values())
                            {
                                if (current.getName().equals(firstMergeId))
                                {
                                    reordered.put(target.getName(), target);
                                }
                                reordered.put(current.getName(), current);
                            }
                            indexEntries = reordered;
                            writeStatus();
                        }
                        return set;

                    }

                });
            }
            finally
            {
                releaseWriteLock();
            }

            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("....Merging..." + (toMerge.size() - 1));
            }

            boolean fail = false;

            try
            {
                if (toMerge.size() > 0)
                {
                    int count = 0;
                    IndexReader[] readers = new IndexReader[toMerge.size() - 1];
                    RAMDirectory ramDirectory = null;
                    IndexWriter writer = null;
                    long docCount = 0;
                    File outputLocation = null;
                    for (IndexEntry entry : toMerge.values())
                    {
                        File location = new File(indexDirectory, entry.getName());
                        if (entry.getStatus() == TransactionStatus.MERGE)
                        {
                            IndexReader reader;
                            if (IndexReader.indexExists(location))
                            {
                                reader = IndexReader.open(location);
                            }
                            else
                            {
                                reader = IndexReader.open(emptyIndex);
                            }
                            readers[count++] = reader;
                            docCount += entry.getDocumentCount();
                        }
                        else if (entry.getStatus() == TransactionStatus.MERGE_TARGET)
                        {
                            outputLocation = location;
                            if (docCount < maxDocsForInMemoryMerge)
                            {
                                ramDirectory = new RAMDirectory();
                                writer = new IndexWriter(ramDirectory, new AlfrescoStandardAnalyser(), true);
                            }
                            else
                            {
                                writer = new IndexWriter(location, new AlfrescoStandardAnalyser(), true);
                               
                            }
                            writer.setUseCompoundFile(mergerUseCompoundFile);
                            writer.minMergeDocs = mergerMinMergeDocs;
                            writer.mergeFactor = mergerMergeFactor;
                            writer.maxMergeDocs = mergerMaxMergeDocs;
                        }
                    }
                    writer.addIndexes(readers);
                    writer.close();

                    if (ramDirectory != null)
                    {
                        String[] files = ramDirectory.list();
                        Directory directory = FSDirectory.getDirectory(outputLocation, true);
                        for (int i = 0; i < files.length; i++)
                        {
                            // make place on ram disk
                            OutputStream os = directory.createFile(files[i]);
                            // read current file
                            InputStream is = ramDirectory.openFile(files[i]);
                            // and copy to ram disk
                            int len = (int) is.length();
                            byte[] buf = new byte[len];
                            is.readBytes(buf, 0, len);
                            os.writeBytes(buf, len);
                            // graceful cleanup
                            is.close();
                            os.close();
                        }
                        ramDirectory.close();
                        directory.close();
                    }

                    for (IndexReader reader : readers)
                    {
                        reader.close();
                    }
                }
            }
            catch (Throwable e)
            {
                s_logger.error(e);
                fail = true;
            }

            final boolean wasMerged = !fail;
            getWriteLock();
            try
            {
                doWithFileLock(new LockWork<Object>()
                {
                    public Object doWork() throws Exception
                    {
                        HashSet<String> toDelete = new HashSet<String>();
                        for (IndexEntry entry : toMerge.values())
                        {
                            if (entry.getStatus() == TransactionStatus.MERGE)
                            {
                                if (wasMerged)
                                {
                                    if (s_logger.isDebugEnabled())
                                    {
                                        s_logger.debug("... deleting as merged " + entry.getName());
                                    }
                                    toDelete.add(entry.getName());
                                }
                                else
                                {
                                    if (s_logger.isDebugEnabled())
                                    {
                                        s_logger.debug("... committing as merge failed " + entry.getName());
                                    }
                                    entry.setStatus(TransactionStatus.COMMITTED);
                                }
                            }
                            else if (entry.getStatus() == TransactionStatus.MERGE_TARGET)
                            {
                                if (wasMerged)
                                {
                                    if (s_logger.isDebugEnabled())
                                    {
                                        s_logger.debug("... committing merge target " + entry.getName());
                                    }
                                    entry.setStatus(TransactionStatus.COMMITTED);
                                }
                                else
                                {
                                    if (s_logger.isDebugEnabled())
                                    {
                                        s_logger.debug("... deleting merge target as merge failed " + entry.getName());
                                    }
                                    toDelete.add(entry.getName());
                                }
                            }
                        }
                        for (String id : toDelete)
                        {
                            indexEntries.remove(id);
                            deleteQueue.add(id);
                        }
                        
                        dumpInfo();

                        writeStatus();

                        clearOldReaders();

                        synchronized (cleaner)
                        {
                            cleaner.notify();
                        }
                        
                        return null;
                    }

                });
            }
            finally
            {
                releaseWriteLock();
            }

            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("..done merging");
            }

        }

        private final int findMergeIndex(long min, long max, int factor, List<IndexEntry> entries) throws IOException
        {
            // TODO: Support max
            if (entries.size() <= factor)
            {
                return -1;
            }

            int total = 0;
            for (int i = factor; i < entries.size(); i++)
            {
                total += entries.get(i).getDocumentCount();
            }

            for (int i = factor - 1; i > 0; i--)
            {
                total += entries.get(i).getDocumentCount();
                if (total < entries.get(i - 1).getDocumentCount())
                {
                    return i;
                }
            }
            return 0;
        }
    }

    private void dumpInfo()
    {
        if (s_logger.isDebugEnabled())
        {
            readWriteLock.writeLock().lock();
            try
            {
                s_logger.debug("");
                s_logger.debug("Entry List");
                for (IndexEntry entry : indexEntries.values())
                {
                    s_logger.debug("        " + entry.toString());
                }
            }
            finally
            {
                readWriteLock.writeLock().unlock();
            }
        }

    }

    private void getWriteLock()
    {
        String threadName = null;
        long start = 0l;
        if (s_logger.isDebugEnabled())
        {
            threadName = Thread.currentThread().getName();
            s_logger.debug("Waiting for WRITE lock  - " + threadName);
            start = System.nanoTime();
        }
        readWriteLock.writeLock().lock();
        if (s_logger.isDebugEnabled())
        {
            long end = System.nanoTime();
            s_logger.debug("...GOT WRITE LOCK  - " + threadName + " -  in " + ((end - start)/10e6f) + " ms");
        }
    }

    private void releaseWriteLock()
    {
        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("RELEASED WRITE LOCK  - " + Thread.currentThread().getName());
        }
        readWriteLock.writeLock().unlock();
    }

    private void getReadLock()
    {
        String threadName = null;
        long start = 0l;
        if (s_logger.isDebugEnabled())
        {
            threadName = Thread.currentThread().getName();
            s_logger.debug("Waiting for READ lock  - " + threadName);
            start = System.nanoTime();
        }
        readWriteLock.readLock().lock();
        if (s_logger.isDebugEnabled())
        {
            long end = System.nanoTime();
            s_logger.debug("...GOT READ LOCK  - " + threadName + " -  in " + ((end - start)/10e6f) + " ms");
        }
    }

    private void releaseReadLock()
    {
        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("RELEASED READ LOCK  - " + Thread.currentThread().getName());
        }
        readWriteLock.readLock().unlock();
    }

    public String toString()
    {
        return indexDirectory.toString();
    }

    public boolean isEnableCleanerThread()
    {
        return enableCleanerThread;
    }

    public void setEnableCleanerThread(boolean enableCleanerThread)
    {
        this.enableCleanerThread = enableCleanerThread;
    }

    public boolean isEnableMergerThread()
    {
        return enableMergerThread;
    }

    public void setEnableMergerThread(boolean enableMergerThread)
    {
        this.enableMergerThread = enableMergerThread;
    }

    public boolean isIndexIsShared()
    {
        return indexIsShared;
    }

    public void setIndexIsShared(boolean indexIsShared)
    {
        this.indexIsShared = indexIsShared;
    }

    public int getMaxDocsForInMemoryMerge()
    {
        return maxDocsForInMemoryMerge;
    }

    public void setMaxDocsForInMemoryMerge(int maxDocsForInMemoryMerge)
    {
        this.maxDocsForInMemoryMerge = maxDocsForInMemoryMerge;
    }

    public int getMergerMaxMergeDocs()
    {
        return mergerMaxMergeDocs;
    }

    public void setMergerMaxMergeDocs(int mergerMaxMergeDocs)
    {
        this.mergerMaxMergeDocs = mergerMaxMergeDocs;
    }

    public int getMergerMergeFactor()
    {
        return mergerMergeFactor;
    }

    public void setMergerMergeFactor(int mergerMergeFactor)
    {
        this.mergerMergeFactor = mergerMergeFactor;
    }

    public int getMergerMinMergeDocs()
    {
        return mergerMinMergeDocs;
    }

    public void setMergerMinMergeDocs(int mergerMinMergeDocs)
    {
        this.mergerMinMergeDocs = mergerMinMergeDocs;
    }

    public int getMergerTargetOverlays()
    {
        return mergerTargetOverlays;
    }

    public void setMergerTargetOverlays(int mergerTargetOverlays)
    {
        this.mergerTargetOverlays = mergerTargetOverlays;
    }

    public boolean isMergerUseCompoundFile()
    {
        return mergerUseCompoundFile;
    }

    public void setMergerUseCompoundFile(boolean mergerUseCompoundFile)
    {
        this.mergerUseCompoundFile = mergerUseCompoundFile;
    }

    public int getWriterMaxMergeDocs()
    {
        return writerMaxMergeDocs;
    }

    public void setWriterMaxMergeDocs(int writerMaxMergeDocs)
    {
        this.writerMaxMergeDocs = writerMaxMergeDocs;
    }

    public int getWriterMergeFactor()
    {
        return writerMergeFactor;
    }

    public void setWriterMergeFactor(int writerMergeFactor)
    {
        this.writerMergeFactor = writerMergeFactor;
    }

    public int getWriterMinMergeDocs()
    {
        return writerMinMergeDocs;
    }

    public void setWriterMinMergeDocs(int writerMinMergeDocs)
    {
        this.writerMinMergeDocs = writerMinMergeDocs;
    }

    public boolean isWriterUseCompoundFile()
    {
        return writerUseCompoundFile;
    }

    public void setWriterUseCompoundFile(boolean writerUseCompoundFile)
    {
        this.writerUseCompoundFile = writerUseCompoundFile;
    }
    
    
}
