/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
import org.alfresco.repo.search.impl.lucene.analysis.AlfrescoStandardAnalyser;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
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
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.RAMDirectory;

/**
 * The information that makes up an index. IndexInfoVersion Repeated information of the form
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
 * Merges always take place to new indexes so we can detect merge failure or partial merges. Or we do not know what has
 * merged. Incomplete delete merging does not matter - the overlay would still exist and be treated as such. So a
 * document may be deleted in the index as well as in the applied overlay. It is still correctly deleted. NOTE: Public
 * methods lock as required, the private methods assume that the appropriate locks have been obtained. TODO: Write
 * element status into individual directories. This would be enough for recovery if both index files are lost or
 * corrupted. TODO: Tidy up index status at start up or after some time. How long would you leave a merge to run?
 * <p>
 * The index structure is duplicated to two files. If one is currupted the second is used.
 * <p>
 * TODO:
 * <p>
 * <ol>
 * <li> make the index sharing configurable
 * <li> use a thread pool for deletions, merging and index deletions
 * <li> something to control the maximum number of overlays to limit the number of things layered together for searching
 * <li> look at lucene locking again post 2.0, to see if it is improved
 * <li> clean up old data files (that are not old index entries) - should be a config option
 * </ol>
 * 
 * @author Andy Hind
 */
public class IndexInfo
{
    /**
     * The logger.
     */
    private static Log s_logger = LogFactory.getLog(IndexInfo.class);

    /**
     * Use NIO memory mapping to wite the index control file.
     */
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
     * The default name for the index deletions file
     */
    private static String INDEX_INFO_DELETIONS = "IndexInfoDeletions";

    /**
     * What to look for to detect the previous index implementation.
     */
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
     * The index entries that make up this index. Map entries are looked up by name. These are maintained in order so
     * document order is maintained.
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

    /**
     * The queue of files and folders to delete
     */
    private ConcurrentLinkedQueue<String> deleteQueue = new ConcurrentLinkedQueue<String>();

    /**
     * A queue of reference counting index readers. We wait for these to become unused (ref count falls to zero) then
     * the data can be removed.
     */
    private ConcurrentLinkedQueue<IndexReader> deletableReaders = new ConcurrentLinkedQueue<IndexReader>();

    /**
     * The call that is responsible for deleting old index information from disk.
     */
    private Cleaner cleaner = new Cleaner();

    /**
     * The thread that deletes old index data
     */
    private Thread cleanerThread;

    /**
     * The class the supports index merging and applying deletions from deltas to indexes and deltas that go before it.
     */
    private Merger merger = new Merger();

    /**
     * The thread that carries out index merging and applying deletions from deltas to indexes and deltas that go before
     * it.
     */
    private Thread mergerThread;

    /**
     * A shared empty index to use if non exist.
     */
    private Directory emptyIndex = new RAMDirectory();

    /**
     * The index infor files that make up the index
     */
    private static HashMap<File, IndexInfo> indexInfos = new HashMap<File, IndexInfo>();

    // Properties that cotrol lucene indexing
    // --------------------------------------

    // Properties for indexes that are created by transactions ...

    private int maxDocsForInMemoryMerge = 10000;

    private int writerMinMergeDocs = 1000;

    private int writerMergeFactor = 5;

    private int writerMaxMergeDocs = 1000000;

    private boolean writerUseCompoundFile = true;

    // Properties for indexes created by merging

    private int mergerMinMergeDocs = 1000;

    private int mergerMergeFactor = 5;

    private int mergerMaxMergeDocs = 1000000;

    private boolean mergerUseCompoundFile = true;

    private int mergerTargetOverlays = 5;

    // Common properties for indexers

    private long writeLockTimeout = IndexWriter.WRITE_LOCK_TIMEOUT;

    private long commitLockTimeout = IndexWriter.COMMIT_LOCK_TIMEOUT;

    private int maxFieldLength = IndexWriter.DEFAULT_MAX_FIELD_LENGTH;

    private int termIndexInterval = IndexWriter.DEFAULT_TERM_INDEX_INTERVAL;

    /**
     * Control if the cleaner thread is active
     */

    private boolean enableCleanerThread = true;

    /**
     * Control if the merger thread is active
     */

    private boolean enableMergerThread = true;

    static
    {
        // We do not require any of the lucene in-built locking.
        FSDirectory.setDisableLocks(true);
    }

    /**
     * Get the IndexInfo object based in the given directory. There is only one object per directory per JVM.
     * 
     * @param file
     * @return
     * @throws IndexerException
     */
    public static synchronized IndexInfo getIndexInfo(File file) throws IndexerException
    {
        File canonicalFile;
        try
        {
            canonicalFile = file.getCanonicalFile();
            IndexInfo indexInfo = indexInfos.get(canonicalFile);
            if (indexInfo == null)
            {
                indexInfo = new IndexInfo(canonicalFile);
                indexInfos.put(canonicalFile, indexInfo);
                if (s_logger.isDebugEnabled())
                {
                    s_logger.debug("Made " + indexInfo + " for " + file.getAbsolutePath());
                }
            }

            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("Got " + indexInfo + " for " + file.getAbsolutePath());
            }
            return indexInfo;
        }
        catch (IOException e)
        {
            throw new IndexerException("Failed to transform a file into is canonical form", e);
        }

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

        
        // Create an empty in memory index
        IndexWriter writer;
        try
        {
            writer = new IndexWriter(emptyIndex, new AlfrescoStandardAnalyser(), true);
            writer.setUseCompoundFile(writerUseCompoundFile);
            writer.setMaxBufferedDocs(writerMinMergeDocs);
            writer.setMergeFactor(writerMergeFactor);
            writer.setMaxMergeDocs(writerMaxMergeDocs);
            writer.setCommitLockTimeout(commitLockTimeout);
            writer.setWriteLockTimeout(writeLockTimeout);
            writer.setMaxFieldLength(maxFieldLength);
            writer.setTermIndexInterval(termIndexInterval);
            writer.close();
        }
        catch (IOException e)
        {
            throw new IndexerException("Failed to create an empty in memory index!");
        }
        
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
            // If both files required creation this is a new index
            version = 0;
        }

        // Open the files and channels for the index info file and the backup
        this.indexInfoRAF = openFile(indexInfoFile);
        this.indexInfoChannel = this.indexInfoRAF.getChannel();

        this.indexInfoBackupRAF = openFile(indexInfoBackupFile);
        this.indexInfoBackupChannel = this.indexInfoBackupRAF.getChannel();

        // If the index found no info files (i.e. it is new), check if there is
        // an old style index and covert it.
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
                                writer.setMaxBufferedDocs(writerMinMergeDocs);
                                writer.setMergeFactor(writerMergeFactor);
                                writer.setMaxMergeDocs(writerMaxMergeDocs);
                                writer.setCommitLockTimeout(commitLockTimeout);
                                writer.setWriteLockTimeout(writeLockTimeout);
                                writer.setMaxFieldLength(maxFieldLength);
                                writer.setTermIndexInterval(termIndexInterval);
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

        // The index exists
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

                        // If the index is not shared we can do some easy clean
                        // up
                        if (!indexIsShared)
                        {
                            HashSet<String> deletable = new HashSet<String>();
                            // clean up
                            for (IndexEntry entry : indexEntries.values())
                            {
                                switch (entry.getStatus())
                                {
                                // states which can be deleted
                                // We could check prepared states can be
                                // committed.
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
                                // States which are in mid-transition which we
                                // can roll back to the committed state
                                case COMMITTED_DELETING:
                                case MERGE:
                                    if (s_logger.isInfoEnabled())
                                    {
                                        s_logger.info("Resetting merge to committed " + entry);
                                    }
                                    entry.setStatus(TransactionStatus.COMMITTED);
                                    registerReferenceCountingIndexReader(entry.getName(),
                                            buildReferenceCountingIndexReader(entry.getName()));
                                    break;
                                // Complete committing (which is post database
                                // commit)
                                case COMMITTING:
                                    // do the commit
                                    if (s_logger.isInfoEnabled())
                                    {
                                        s_logger.info("Committing " + entry);
                                    }
                                    entry.setStatus(TransactionStatus.COMMITTED);
                                    registerReferenceCountingIndexReader(entry.getName(),
                                            buildReferenceCountingIndexReader(entry.getName()));
                                    mainIndexReader = null;
                                    break;
                                // States that require no action
                                case COMMITTED:
                                    registerReferenceCountingIndexReader(entry.getName(),
                                            buildReferenceCountingIndexReader(entry.getName()));
                                    break;
                                default:
                                    // nothing to do
                                    break;
                                }
                            }
                            // Delete entries that are not required
                            for (String id : deletable)
                            {
                                indexEntries.remove(id);
                            }
                            clearOldReaders();
                            synchronized (cleaner)
                            {
                                cleaner.notify();
                            }
                            synchronized (merger)
                            {
                                merger.notify();
                            }
                            // persist the new state
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
            cleanerThread.setName("Index cleaner thread " + indexDirectory);
            cleanerThread.start();
        }

        if (enableMergerThread)
        {
            mergerThread = new Thread(merger);
            mergerThread.setDaemon(true);
            mergerThread.setName("Index merger thread " + indexDirectory);
            mergerThread.start();
        }

 

    }

    /**
     * This method should only be called from one thread as it is bound to a transaction.
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
        // Create a dummy index reader to deal with empty indexes and not
        // persist these.
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

        // A write lock is required if we have to update the local index
        // entries.
        // There should only be one thread trying to access this delta.
        File location = new File(indexDirectory, id).getCanonicalFile();
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

    /**
     * Make a lucene index writer
     * 
     * @param location
     * @param analyzer
     * @return
     * @throws IOException
     */
    private IndexWriter makeDeltaIndexWriter(File location, Analyzer analyzer) throws IOException
    {
        IndexWriter writer;
        if (!IndexReader.indexExists(location))
        {
            writer = new IndexWriter(location, analyzer, true);
        }
        else
        {
            writer = new IndexWriter(location, analyzer, false);
        }
        writer.setUseCompoundFile(writerUseCompoundFile);
        writer.setMaxBufferedDocs(writerMinMergeDocs);
        writer.setMergeFactor(writerMergeFactor);
        writer.setMaxMergeDocs(writerMaxMergeDocs);
        writer.setCommitLockTimeout(commitLockTimeout);
        writer.setWriteLockTimeout(writeLockTimeout);
        writer.setMaxFieldLength(maxFieldLength);
        writer.setTermIndexInterval(termIndexInterval);
        return writer;

    }

    /**
     * Manage getting a lucene index writer for transactional data - looks after registration and checking there is no
     * active reader.
     * 
     * @param id
     * @param analyzer
     * @return
     * @throws IOException
     */
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
            indexWriters.put(id, writer);
        }
        return writer;
    }

    /**
     * Manage closing and unregistering an index reader.
     * 
     * @param id
     * @throws IOException
     */
    public void closeDeltaIndexReader(String id) throws IOException
    {
        if (id == null)
        {
            throw new IndexerException("\"null\" is not a valid identifier for a transaction");
        }

        // No lock required as the delta applied to one thread. The delta is
        // still active.
        IndexReader reader = indexReaders.remove(id);
        if (reader != null)
        {
            reader.close();
        }
    }

    /**
     * Manage closing and unregistering an index writer .
     * 
     * @param id
     * @throws IOException
     */
    public void closeDeltaIndexWriter(String id) throws IOException
    {
        if (id == null)
        {
            throw new IndexerException("\"null\" is not a valid identifier for a transaction");
        }

        // No lock required as the delta applied to one thread. The delta is
        // still active.
        IndexWriter writer = indexWriters.remove(id);
        if (writer != null)
        {
            writer.close();
        }
    }

    /**
     * Make sure the writer and reader for TX data are closed.
     * 
     * @param id
     * @throws IOException
     */
    public void closeDelta(String id) throws IOException
    {
        if (id == null)
        {
            throw new IndexerException("\"null\" is not a valid identifier for a transaction");
        }
        closeDeltaIndexReader(id);
        closeDeltaIndexWriter(id);
    }

    /**
     * Get the deletions for a given index (there is not check if thery should be applied that is up to the calling
     * layer)
     * 
     * @param id
     * @return
     * @throws IOException
     */
    public Set<NodeRef> getDeletions(String id) throws IOException
    {
        if (id == null)
        {
            throw new IndexerException("\"null\" is not a valid identifier for a transaction");
        }
        // Check state
        Set<NodeRef> deletions = new HashSet<NodeRef>();
        File location = new File(indexDirectory, id).getCanonicalFile();
        File file = new File(location, INDEX_INFO_DELETIONS).getCanonicalFile();
        if (!file.exists())
        {
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("No deletions for " + id);
            }
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
        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("There are " + deletions.size() + " deletions for " + id);
        }
        return deletions;

    }

    /**
     * Set the aux data for the index entry for a transactional unit of work.
     * 
     * @param id -
     *            the tx id
     * @param toDelete -
     *            noderefs that should be deleted from previous indexes (not this one)
     * @param documents -
     *            the number of docs in the index
     * @param deleteNodesOnly -
     *            should deletions on apply to nodes (ie not to containers)
     * @throws IOException
     */
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
            File location = new File(indexDirectory, id).getCanonicalFile();
            if (!location.exists())
            {
                if (!location.mkdirs())
                {
                    throw new IndexerException("Failed to make index directory " + location);
                }
            }
            // Write deletions
            DataOutputStream os = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(location,
                    INDEX_INFO_DELETIONS).getCanonicalFile())));
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

    /**
     * Get the main reader for committed index data
     * 
     * @return
     * @throws IOException
     */
    public IndexReader getMainIndexReferenceCountingReadOnlyIndexReader() throws IOException
    {
        getReadLock();
        try
        {
            // Check if we need to rebuild the main indexer as it is invalid.
            // (it is shared and quick version check fails)
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

            // Build if required
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
            // Manage reference counting
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

    /**
     * Get the main index reader augmented with the specified TX data As above but we add the TX data
     * 
     * @param id
     * @param deletions
     * @param deleteOnlyNodes
     * @return
     * @throws IOException
     */
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
            // TODO: Should use the in memory index but we often end up forcing
            // to disk anyway.
            // Is it worth it?
            // luceneIndexer.flushPending();

            IndexReader deltaReader = buildAndRegisterDeltaReader(id);
            IndexReader reader = null;
            if (deletions == null || deletions.size() == 0)
            {
                reader = new MultiReader(new IndexReader[] { mainIndexReader, deltaReader });
            }
            else
            {
                reader = new MultiReader(new IndexReader[] {
                        new FilterIndexReaderByNodeRefs2("main+id", mainIndexReader, deletions, deleteOnlyNodes),
                        deltaReader });
            }
            reader = ReferenceCountingReadOnlyIndexReaderFactory.createReader("MainReader" + id, reader);
            ReferenceCounting refCounting = (ReferenceCounting) reader;
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

        getReadLock();
        try
        {
            transition.beforeWithReadLock(id, toDelete, read);
            releaseReadLock();
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
                getReadLock();
                releaseWriteLock();
            }
        }
        finally
        {
            releaseReadLock();
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

    /**
     * Initialise the definitions for the available transitions.
     */
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

    /**
     * API for transitions
     * 
     * @author andyh
     */
    private interface Transition
    {
        void beforeWithReadLock(String id, Set<Term> toDelete, Set<Term> read) throws IOException;

        void transition(String id, Set<Term> toDelete, Set<Term> read) throws IOException;

        boolean requiresFileLock();
    }

    /**
     * Transition to the perparing state
     * 
     * @author andyh
     */
    private class PreparingTransition implements Transition
    {
        public void beforeWithReadLock(String id, Set<Term> toDelete, Set<Term> read) throws IOException
        {
            // Nothing to do
        }

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

    /**
     * Transition to the prepared state.
     * 
     * @author andyh
     */
    private class PreparedTransition implements Transition
    {
        public void beforeWithReadLock(String id, Set<Term> toDelete, Set<Term> read) throws IOException
        {

        }

        public void transition(String id, Set<Term> toDelete, Set<Term> read) throws IOException
        {
            IndexEntry entry = indexEntries.get(id);
            if (entry == null)
            {
                throw new IndexerException("Unknown transaction " + id);
            }

            if (TransactionStatus.PREPARED.follows(entry.getStatus()))
            {

                LinkedHashMap<String, IndexEntry> reordered = new LinkedHashMap<String, IndexEntry>();
                boolean addedPreparedEntry = false;
                for (String key : indexEntries.keySet())
                {
                    IndexEntry current = indexEntries.get(key);

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

                entry.setStatus(TransactionStatus.PREPARED);
                writeStatus();

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
        public void beforeWithReadLock(String id, Set<Term> toDelete, Set<Term> read) throws IOException
        {

        }

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

        ThreadLocal<IndexReader> tl = new ThreadLocal<IndexReader>();

        public void beforeWithReadLock(String id, Set<Term> toDelete, Set<Term> read) throws IOException
        {
            // Make sure we have set up the reader for the data
            // ... and close it so we do not up the ref count
            closeDelta(id);
            tl.set(buildReferenceCountingIndexReader(id));
        }

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
                    registerReferenceCountingIndexReader(id, tl.get());
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
        public void beforeWithReadLock(String id, Set<Term> toDelete, Set<Term> read) throws IOException
        {

        }

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
        public void beforeWithReadLock(String id, Set<Term> toDelete, Set<Term> read) throws IOException
        {

        }

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
        public void beforeWithReadLock(String id, Set<Term> toDelete, Set<Term> read) throws IOException
        {

        }

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
                synchronized (cleaner)
                {
                    cleaner.notify();
                }
                writeStatus();
                clearOldReaders();
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
        public void beforeWithReadLock(String id, Set<Term> toDelete, Set<Term> read) throws IOException
        {

        }

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
        // Find current invalid
        HashSet<String> inValid = new HashSet<String>();
        for (String id : referenceCountingReadOnlyIndexReaders.keySet())
        {
            if (!indexEntries.containsKey(id))
            {
                if (s_logger.isDebugEnabled())
                {
                    s_logger.debug(id + " is now INVALID ");
                }
                inValid.add(id);
            }
            else
            {
                if (s_logger.isDebugEnabled())
                {
                    s_logger.debug(id + " is still part of the index ");
                }
            }
        }
        // Clear invalid
        clearInvalid(inValid);
    }

    private void clearInvalid(HashSet<String> inValid) throws IOException
    {
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
            deletableReaders.add(reader);
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
                        try
                        {
                            reader = new MultiReader(new IndexReader[] {
                                    new FilterIndexReaderByNodeRefs2(id, reader, getDeletions(entry.getName()), entry
                                            .isDeletOnlyNodes()), subReader });
                        }
                        catch (IOException ioe)
                        {
                            s_logger.error("Failed building filter reader beneath " + entry.getName(), ioe);
                            throw ioe;
                        }
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
            throw new IllegalStateException("Indexer should have been pre-built for " + id);
        }
        ReferenceCounting referenceCounting = (ReferenceCounting) reader;
        referenceCounting.incrementReferenceCount();
        return reader;
    }

    private void registerReferenceCountingIndexReader(String id, IndexReader reader)
    {
        ReferenceCounting referenceCounting = (ReferenceCounting) reader;
        if(!referenceCounting.getId().equals(id))
        {
            throw new IllegalStateException("Registering "+referenceCounting.getId()+ " as "+id);
        }
        referenceCountingReadOnlyIndexReaders.put(id, reader);
    }

    private IndexReader buildReferenceCountingIndexReader(String id) throws IOException
    {
        IndexReader reader;
        File location = new File(indexDirectory, id).getCanonicalFile();
        if (IndexReader.indexExists(location))
        {
            reader = IndexReader.open(location);
        }
        else
        {
            reader = IndexReader.open(emptyIndex);
        }
        reader = ReferenceCountingReadOnlyIndexReaderFactory.createReader(id, reader);
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
                // Not all state is saved some is specific to this index so we
                // need to add the transient stuff.
                // Until things are committed they are not shared unless it is
                // prepared
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
                    s_logger.debug(" ... got file lock in " + ((end - start) / 10e6f) + " ms");
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
                // Add any closed index readers we were waiting for
                HashSet<IndexReader> waiting = new HashSet<IndexReader>();
                IndexReader reader;
                while ((reader = deletableReaders.poll()) != null)
                {
                    ReferenceCounting refCounting = (ReferenceCounting) reader;
                    if (refCounting.getReferenceCount() == 0)
                    {
                        if (s_logger.isDebugEnabled())
                        {
                            s_logger.debug("Deleting no longer referenced " + refCounting.getId());
                            s_logger.debug("... queued delete for " + refCounting.getId());
                            s_logger.debug("... "
                                    + ReferenceCountingReadOnlyIndexReaderFactory.getState(refCounting.getId()));
                        }
                        getReadLock();
                        try
                        {
                            if (indexEntries.containsKey(refCounting.getId()))
                            {
                                s_logger.error("ERROR - deleting live reader - " + refCounting.getId());
                            }
                        }
                        finally
                        {
                            releaseReadLock();
                        }
                        deleteQueue.add(refCounting.getId());
                    }
                    else
                    {
                        waiting.add(reader);
                    }
                }
                deletableReaders.addAll(waiting);

                String id = null;
                HashSet<String> fails = new HashSet<String>();
                while ((id = deleteQueue.poll()) != null)
                {
                    try
                    {
                        if (s_logger.isDebugEnabled())
                        {
                            s_logger.debug("Expunging " + id + " remaining " + deleteQueue.size());
                            s_logger.debug("... " + ReferenceCountingReadOnlyIndexReaderFactory.getState(id));
                        }
                        // try and delete
                        File location = new File(indexDirectory, id).getCanonicalFile();
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
                    catch (IOException ioe)
                    {
                        s_logger.warn("Failed to delete file - invalid canonical file", ioe);
                        fails.add(id);
                    }
                }
                deleteQueue.addAll(fails);
                synchronized (this)
                {
                    try
                    {
                        // wait for more deletes
                        if (deleteQueue.size() > 0)
                        {
                            this.wait(20000);
                        }
                        else
                        {
                            this.wait();
                        }
                    }
                    catch (InterruptedException e)
                    {
                        runnable = false;
                        s_logger.warn("Cleaner thread for " + indexDirectory + "stopped by interruption.");
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
     * Supported by one thread. 1) If the first index is a delta we can just change it to an index. There is now here to
     * apply the deletions 2) Merge indexes Combine indexes together according to the target index merge strategy. This
     * is a trade off to make an optimised index but not spend too much time merging and optimising small merges. 3)
     * Apply next deletion set to indexes Apply the deletions for the first delta to all the other indexes. Deletes can
     * be applied with relative impunity. If any are applied they take effect as required. 1) 2) and 3) are mutually
     * exclusive try in order This could be supported in another thread 4) Merge deltas Merge two index deltas together.
     * Starting at the end. Several merges can be going on at once. a) Find merge b) Set state c) apply deletions to the
     * previous delta d) update state e) add deletions to the previous delta deletion list f) update state
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
                        s_logger.error("Error reading index file", e);
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
                            if (action == MergeAction.NONE)
                            {
                                this.wait();
                            }
                        }
                        catch (InterruptedException e)
                        {
                            // No action - could signal thread termination
                        }
                    }
                }
                catch (Throwable t)
                {
                    s_logger.error("??", t);
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
                            // skip indexes at the start
                            if (entry.getType() == IndexType.DELTA)
                            {
                                if (entry.getStatus() == TransactionStatus.COMMITTED)
                                {
                                    entry.setStatus(TransactionStatus.COMMITTED_DELETING);
                                    set.put(entry.getName(), entry);
                                }
                                else
                                {
                                    // If not committed we stop as we can not
                                    // span non committed.
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
                    File location = new File(indexDirectory, entry.getName()).getCanonicalFile();
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
                                            reader.deleteDocument(hits.id(i));
                                            invalidIndexes.add(key);
                                            // There should only be one thing to
                                            // delete
                                            // break;
                                        }
                                    }
                                }
                                searcher.close();

                            }
                            else
                            {
                                int deletedCount = 0;
                                try
                                {
                                    deletedCount = reader.deleteDocuments(new Term("ID", nodeRef.toString()));
                                }
                                catch (IOException ioe)
                                {
                                    if (s_logger.isDebugEnabled())
                                    {
                                        s_logger.debug("IO Error for " + key);
                                        throw ioe;
                                    }
                                }
                                if (deletedCount > 0)
                                {
                                    if (s_logger.isDebugEnabled())
                                    {
                                        s_logger.debug("Deleted "
                                                + deletedCount + " from " + key + " for id " + nodeRef.toString()
                                                + " remaining docs " + reader.numDocs());
                                    }
                                    invalidIndexes.add(key);
                                }
                            }
                        }

                    }
                    File location = new File(indexDirectory, currentDelete.getName()).getCanonicalFile();
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

                // Close all readers holding the write lock - so no one tries to
                // read
                getWriteLock();
                try
                {
                    for (String key : readers.keySet())
                    {
                        IndexReader reader = readers.get(key);
                        // TODO:Set the new document count
                        newIndexCounts.put(key, new Long(reader.numDocs()));
                        reader.close();
                    }
                }
                finally
                {
                    releaseWriteLock();
                }
            }
            catch (IOException e)
            {
                s_logger.error("Failed to merge deletions", e);
                fail = true;
            }

            // Prebuild all readers for affected indexes
            // Register them in the commit.

            final HashMap<String, IndexReader> newReaders = new HashMap<String, IndexReader>();
            try
            {
                for (String id : invalidIndexes)
                {
                    IndexReader reader = buildReferenceCountingIndexReader(id);
                    newReaders.put(id, reader);
                }
            }
            catch (IOException ioe)
            {
                s_logger.error("Failed build new readers", ioe);
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
                                    s_logger.debug("... invalidating sub reader after applying deletions" + id);
                                }
                            }
                        }
                        for (String id : invalidIndexes)
                        {
                            IndexReader newReader = newReaders.get(id);
                            registerReferenceCountingIndexReader(id, newReader);
                        }
                        if (invalidIndexes.size() > 0)
                        {
                            if (mainIndexReader != null)
                            {
                                if (s_logger.isDebugEnabled())
                                {
                                    s_logger.debug("... invalidating main index reader after applying deletions");
                                }
                                ((ReferenceCounting) mainIndexReader).setInvalidForReuse();
                            }
                            else
                            {
                                if (s_logger.isDebugEnabled())
                                {
                                    s_logger.debug("... no main index reader to invalidate after applying deletions");
                                }
                            }
                            mainIndexReader = null;
                        }

                        if (s_logger.isDebugEnabled())
                        {
                            for (String id : toDelete.keySet())
                            {
                                s_logger.debug("...applied deletion for " + id);
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

            String mergeTargetId = null;

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
                        File location = new File(indexDirectory, entry.getName()).getCanonicalFile();
                        if (entry.getStatus() == TransactionStatus.MERGE)
                        {
                            IndexReader reader;
                            if (IndexReader.indexExists(location))
                            {
                                reader = IndexReader.open(location);
                            }
                            else
                            {
                                s_logger.error("Index is missing " + entry.getName());
                                reader = IndexReader.open(emptyIndex);
                            }
                            readers[count++] = reader;
                            docCount += entry.getDocumentCount();
                        }
                        else if (entry.getStatus() == TransactionStatus.MERGE_TARGET)
                        {
                            mergeTargetId = entry.getName();
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
                            writer.setMaxBufferedDocs(mergerMinMergeDocs);
                            writer.setMergeFactor(mergerMergeFactor);
                            writer.setMaxMergeDocs(mergerMaxMergeDocs);
                            writer.setCommitLockTimeout(commitLockTimeout);
                            writer.setWriteLockTimeout(writeLockTimeout);
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
                            IndexOutput os = directory.createOutput(files[i]);
                            // read current file
                            IndexInput is = ramDirectory.openInput(files[i]);
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
                s_logger.error("Failed to merge indexes", e);
                fail = true;
            }

            final String finalMergeTargetId = mergeTargetId;
            IndexReader newReader = null;
            getReadLock();
            try
            {
                try
                {
                    newReader = buildReferenceCountingIndexReader(mergeTargetId);
                }
                catch (IOException e)
                {
                    s_logger.error("Failed to open reader for merge target", e);
                    fail = true;
                }
            }
            finally
            {
                releaseReadLock();
            }

            final IndexReader finalNewReader = newReader;
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
                        }

                        registerReferenceCountingIndexReader(finalMergeTargetId, finalNewReader);

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
            int count = 0;
            StringBuilder builder = new StringBuilder();
            readWriteLock.writeLock().lock();
            try
            {
                builder.append("\n");
                builder.append("Entry List\n");
                for (IndexEntry entry : indexEntries.values())
                {
                    builder.append(++count + "        " + entry.toString()).append("\n");
                }
                s_logger.debug(builder.toString());
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
            s_logger.debug("...GOT WRITE LOCK  - " + threadName + " -  in " + ((end - start) / 10e6f) + " ms");
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
            s_logger.debug("...GOT READ LOCK  - " + threadName + " -  in " + ((end - start) / 10e6f) + " ms");
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
        StringBuilder builder = new StringBuilder();
        builder.append(indexDirectory.toString());
        builder.append(" ");
        builder.append(super.toString());
        return builder.toString();
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
