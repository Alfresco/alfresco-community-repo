/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.zip.CRC32;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.search.IndexerException;
import org.alfresco.repo.search.impl.lucene.FilterIndexReaderByStringId;
import org.alfresco.repo.search.impl.lucene.LuceneConfig;
import org.alfresco.repo.search.impl.lucene.LuceneXPathHandler;
import org.alfresco.repo.search.impl.lucene.analysis.AlfrescoStandardAnalyser;
import org.alfresco.repo.search.impl.lucene.query.PathQuery;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.alfresco.util.TraceableThreadFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.FilterIndexReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.LogDocMergePolicy;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.SerialMergeScheduler;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.IndexReader.FieldOption;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.RAMDirectory;
import org.safehaus.uuid.UUID;
import org.saxpath.SAXPathException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

import com.werken.saxpath.XPathReader;

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
public class IndexInfo implements IndexMonitor
{
    public static synchronized void destroy()
    {
        timer.cancel();
        timer = new Timer(true);
        for(IndexInfo indexInfo : indexInfos.values())
        {
           indexInfo.destroyInstance();
        }
        indexInfos.clear();
        ReferenceCountingReadOnlyIndexReaderFactory.destroy();
    }
    
    public void destroyInstance()
    {
        getWriteLock();
        try
        {
            if(mainIndexReader != null)
            {
                try
                {
                    ((ReferenceCounting) mainIndexReader).setInvalidForReuse();
                }
                catch (IOException e)
                {
                    // OK filed to close
                }
                mainIndexReader = null;

                for(IndexReader reader : referenceCountingReadOnlyIndexReaders.values())
                {
                    ReferenceCounting referenceCounting = (ReferenceCounting) reader;
                    try
                    {
                        referenceCounting.setInvalidForReuse();
                    }
                    catch (IOException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

            for(IndexReader reader : indexReaders.values())
            {
                try
                {
                    reader.close();
                }
                catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            indexReaders.clear();

            for(IndexWriter writer : indexWriters.values())
            {
                try
                {
                    writer.close();
                }
                catch (CorruptIndexException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            indexWriters.clear();
            
            if(indexInfoRAF != null)
            {
                try
                {
                    indexInfoRAF.close();
                }
                catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
            if(indexInfoBackupRAF != null)
            {
                try
                {
                    indexInfoBackupRAF.close();
                }
                catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
            // TODO: should set some running flag .... to abort ungoing stuff
            // at the moment it will die ungracefully ....
        }
        finally
        {
            releaseWriteLock();
        }
    }
    
    public static final String MAIN_READER = "MainReader";

    private static Timer timer = new Timer(true);

    /**
     * The logger.
     */
    private static Log s_logger = LogFactory.getLog(IndexInfo.class);

    /**
     * Use NIO memory mapping to wite the index control file.
     */
    private static boolean useNIOMemoryMapping = true;

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
     * The directory relative to the root path
     */
    private String relativePath;

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
    private final ReentrantReadWriteLock readWriteLock;
    
    private ReentrantReadWriteLock readOnlyLock = new ReentrantReadWriteLock();

    /**
     * Read only index readers that also do reference counting.
     */
    private HashMap<String, IndexReader> referenceCountingReadOnlyIndexReaders = new HashMap<String, IndexReader>();

    /**
     * Main index reader
     */
    private IndexReader mainIndexReader;
    private Map<String, IndexReader> mainIndexReaders = new HashMap<String, IndexReader>();

    /**
     * Index writers for deltas
     */
    private Map<String, IndexWriter> indexWriters = new ConcurrentHashMap<String, IndexWriter>(51);

    /**
     * Index Readers for deltas
     */
    private Map<String, IndexReader> indexReaders = new ConcurrentHashMap<String, IndexReader>(51);

    /**
     * Map of state transitions
     */
    private EnumMap<TransactionStatus, Transition> transitions = new EnumMap<TransactionStatus, Transition>(TransactionStatus.class);

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
    // private Thread cleanerThread;
    /**
     * The class the supports index merging and applying deletions from deltas to indexes and deltas that go before it.
     */
    private Merger merger = new Merger();

    /**
     * The thread that carries out index merging and applying deletions from deltas to indexes and deltas that go before
     * it.
     */
    // private Thread mergerThread;
    /**
     * A shared empty index to use if non exist.
     */
    private Directory emptyIndex = new RAMDirectory();

    /**
     * The index infor files that make up the index
     */
    private static HashMap<File, IndexInfo> indexInfos = new HashMap<File, IndexInfo>();

    // Properties that control lucene indexing
    // --------------------------------------

    // Properties for indexes that are created by transactions ...

    private int maxDocsForInMemoryMerge = 10000;
    
    private int maxDocsForInMemoryIndex = 10000;

    private double maxRamInMbForInMemoryMerge = 16.0;
    
    private double maxRamInMbForInMemoryIndex = 16.0;

    private int writerMaxBufferedDocs = IndexWriter.DISABLE_AUTO_FLUSH;

    private double writerRamBufferSizeMb = 16.0;

    private int writerMergeFactor = 5;

    private int writerMaxMergeDocs = 1000000;

    private boolean writerUseCompoundFile = true;

    // Properties for indexes created by merging

    private int mergerMaxBufferedDocs = IndexWriter.DISABLE_AUTO_FLUSH;

    private double mergerRamBufferSizeMb = 16.0;

    private int mergerMergeFactor = 5;

    private int mergerMaxMergeDocs = 1000000;

    private boolean mergerUseCompoundFile = true;

    private int mergerTargetOverlays = 5;
    
    private int mergerTargetIndexes = 5;
    
    private int mergerTargetOverlaysBlockingFactor = 1;

    private Object mergerTargetLock = new Object();
    
    // To avoid deadlock (a thread with multiple deltas never proceeding to commit) we track whether each thread is
    // already in the prepare phase.
    private static ThreadLocal<IndexInfo> thisThreadPreparing = new ThreadLocal<IndexInfo>();

    // Common properties for indexers

    private long writeLockTimeout = IndexWriter.WRITE_LOCK_TIMEOUT;

    private int maxFieldLength = IndexWriter.DEFAULT_MAX_FIELD_LENGTH;

    private int termIndexInterval = IndexWriter.DEFAULT_TERM_INDEX_INTERVAL;

    /**
     * Control if the merger thread is active
     */

    private ThreadPoolExecutor threadPoolExecutor;

    private LuceneConfig config;

    private List<ApplicationListener> applicationListeners = new LinkedList<ApplicationListener>();

    static
    {
        // We do not require any of the lucene in-built locking.
        FSDirectory.setDisableLocks(true);
    }
    
    /**
     * 
     */
    public void delete(final String deltaId)
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
                            if(!entry.getName().equals(deltaId))
                            {
                                entry.setStatus(TransactionStatus.DELETABLE);
                                deletable.add(entry.getName());
                            }
                        }
                        // Delete entries that are not required
						invalidateMainReadersFromFirst(deletable);
                        for (String id : deletable)
                        {
                            indexEntries.remove(id);
                        }
                        
                        clearOldReaders();

                        cleaner.schedule();

                        merger.schedule();

                        // persist the new state
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
                    }
                    return null;
                }

                public boolean canRetry()
                {
                    return false;
                }

            });
        }
        finally
        {
            releaseWriteLock();
        }
        if(s_logger.isDebugEnabled())
        {
           s_logger.debug("Index "+ indexDirectory+" deleted");
        }
        
    }

    /**
     * Get the IndexInfo object based in the given directory. There is only one object per directory per JVM.
     * 
     * @param file
     * @return
     * @throws IndexerException
     */
    public static synchronized IndexInfo getIndexInfo(File file, LuceneConfig config) throws IndexerException
    {
        File canonicalFile;
        try
        {
            canonicalFile = file.getCanonicalFile();
            IndexInfo indexInfo = indexInfos.get(canonicalFile);
            if (indexInfo == null)
            {
                indexInfo = new IndexInfo(canonicalFile, config);
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
    private IndexInfo(File indexDirectory, LuceneConfig config)
    {
        super();
        initialiseTransitions();
        this.config = config;

        if (config != null)
        {
            this.readWriteLock = new ReentrantReadWriteLock(config.getFairLocking());            
            this.maxFieldLength = config.getIndexerMaxFieldLength();
            this.threadPoolExecutor = config.getThreadPoolExecutor();
            IndexInfo.useNIOMemoryMapping = config.getUseNioMemoryMapping();
            this.maxDocsForInMemoryMerge = config.getMaxDocsForInMemoryMerge();
            this.maxRamInMbForInMemoryMerge = config.getMaxRamInMbForInMemoryMerge();
            this.maxDocsForInMemoryIndex = config.getMaxDocsForInMemoryIndex();
            this.maxRamInMbForInMemoryIndex = config.getMaxRamInMbForInMemoryIndex();
            this.writerMaxBufferedDocs = config.getWriterMaxBufferedDocs();
            this.writerRamBufferSizeMb = config.getWriterRamBufferSizeMb();
            this.writerMergeFactor = config.getWriterMergeFactor();
            this.writerMaxMergeDocs = config.getWriterMaxMergeDocs();
            this.mergerMaxBufferedDocs = config.getMergerMaxBufferedDocs();
            this.mergerRamBufferSizeMb = config.getMergerRamBufferSizeMb();
            this.mergerMergeFactor = config.getMergerMergeFactor();
            this.mergerMaxMergeDocs = config.getMergerMaxMergeDocs();
            this.termIndexInterval = config.getTermIndexInterval();
            this.mergerTargetOverlays = config.getMergerTargetOverlayCount();
            this.mergerTargetIndexes = config.getMergerTargetIndexCount();
            this.mergerTargetOverlaysBlockingFactor = config.getMergerTargetOverlaysBlockingFactor();
            // Work out the relative path of the index
            try
            {
                String indexRoot = new File(config.getIndexRootLocation()).getCanonicalPath();
                this.relativePath = indexDirectory.getCanonicalPath().substring(indexRoot.length() + 1);
            }
            catch (IOException e)
            {
                throw new AlfrescoRuntimeException("Failed to determine index relative path", e);
            }
        }
        else
        {
            this.readWriteLock = new ReentrantReadWriteLock(false);            

            // need a default thread pool ....
            TraceableThreadFactory threadFactory = new TraceableThreadFactory();
            threadFactory.setThreadDaemon(true);
            threadFactory.setThreadPriority(5);

            threadPoolExecutor = new ThreadPoolExecutor(10, 10, 90, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), threadFactory, new ThreadPoolExecutor.CallerRunsPolicy());

            // Create a 'fake' relative path
            try
            {
                this.relativePath = indexDirectory.getCanonicalPath();
                int sepIndex = this.relativePath.indexOf(File.separator);
                if (sepIndex != -1)
                {
                    if (this.relativePath.length() > sepIndex + 1)
                    {
                        this.relativePath = this.relativePath.substring(sepIndex + 1);
                    }
                    else
                    {
                        this.relativePath = "";
                    }
                }
            }
            catch (IOException e)
            {
                throw new AlfrescoRuntimeException("Failed to determine index relative path", e);
            }

        }

        // Create an empty in memory index
        IndexWriter writer;
        try
        {
            writer = new IndexWriter(emptyIndex, new AlfrescoStandardAnalyser(), true, MaxFieldLength.LIMITED);
            writer.setUseCompoundFile(writerUseCompoundFile);
            writer.setMaxBufferedDocs(writerMaxBufferedDocs);
            writer.setRAMBufferSizeMB(writerRamBufferSizeMb);
            writer.setMergeFactor(writerMergeFactor);
            writer.setMaxMergeDocs(writerMaxMergeDocs);
            writer.setWriteLockTimeout(writeLockTimeout);
            writer.setMaxFieldLength(maxFieldLength);
            writer.setTermIndexInterval(termIndexInterval);
            writer.setMergeScheduler(new SerialMergeScheduler());
            writer.setMergePolicy(new LogDocMergePolicy());
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
                                writer = new IndexWriter(oldIndex, new AlfrescoStandardAnalyser(), false, MaxFieldLength.LIMITED);
                                writer.setUseCompoundFile(writerUseCompoundFile);
                                writer.setMaxBufferedDocs(writerMaxBufferedDocs);
                                writer.setRAMBufferSizeMB(writerRamBufferSizeMb);
                                writer.setMergeFactor(writerMergeFactor);
                                writer.setMaxMergeDocs(writerMaxMergeDocs);
                                writer.setWriteLockTimeout(writeLockTimeout);
                                writer.setMaxFieldLength(maxFieldLength);
                                writer.setTermIndexInterval(termIndexInterval);
                                writer.setMergeScheduler(new SerialMergeScheduler());
                                writer.setMergePolicy(new LogDocMergePolicy());
                                writer.optimize();
                                long docs = writer.numDocs();
                                writer.close();

                                IndexEntry entry = new IndexEntry(IndexType.INDEX, OLD_INDEX, "", TransactionStatus.COMMITTED, "", docs, 0, false);
                                indexEntries.put(OLD_INDEX, entry);

                                writeStatus();

                                // The index exists and we should initialise the single reader
                                registerReferenceCountingIndexReader(entry.getName(), buildReferenceCountingIndexReader(entry.getName(), entry.getDocumentCount()));
                            }
                            catch (IOException e)
                            {
                                throw new IndexerException("Failed to optimise old index");
                            }
                            return null;
                        }

                        public boolean canRetry()
                        {
                            return false;
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
                                    registerReferenceCountingIndexReader(entry.getName(), buildReferenceCountingIndexReader(entry.getName(), entry.getDocumentCount()));
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
                                    registerReferenceCountingIndexReader(entry.getName(), buildReferenceCountingIndexReader(entry.getName(), entry.getDocumentCount()));
                                    break;
                                // States that require no action
                                case COMMITTED:
                                    registerReferenceCountingIndexReader(entry.getName(), buildReferenceCountingIndexReader(entry.getName(), entry.getDocumentCount()));
                                    break;
                                default:
                                    // nothing to do
                                    break;
                                }
                            }
                            // Delete entries that are not required
                            invalidateMainReadersFromFirst(deletable);
                            for (String id : deletable)
                            {
                                indexEntries.remove(id);
                            }
                            clearOldReaders();

                            cleaner.schedule();

                            merger.schedule();

                            // persist the new state
                            writeStatus();
                        }
                        return null;
                    }

                    public boolean canRetry()
                    {
                        return false;
                    }

                });
            }
            finally
            {
                releaseWriteLock();
            }
        }
        // Need to do with file lock - must share info about other readers to support this with shared indexer
        // implementation

        getWriteLock();
        try
        {
            LockWork<Object> work = new DeleteUnknownGuidDirectories();
            doWithFileLock(work);
        }
        finally
        {
            releaseWriteLock();
        }

        // Run the cleaner around every 20 secods - this just makes the request to the thread pool
        timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                cleaner.schedule();
            }
        }, 0, 20000);

        publishDiscoveryEvent();
    }

    private class DeleteUnknownGuidDirectories implements LockWork<Object>
    {
        public boolean canRetry()
        {
            return true;
        }

        public Object doWork() throws Exception
        {
            setStatusFromFile();

            // If the index is not shared we can do some easy clean
            // up
            if (!indexIsShared)
            {
                // Safe to tidy up all files that look like guids that we do not know about
                File[] files = indexDirectory.listFiles();
                if (files != null)
                {
                    for (File file : files)
                    {
                        if (file.isDirectory())
                        {
                            String id = file.getName();
                            if (!indexEntries.containsKey(id) && isGUID(id))
                            {
                                if (s_logger.isDebugEnabled())
                                {
                                    s_logger.debug("Deleting unused index directory " + id);
                                }
                                deleteQueue.add(id);
                            }
                        }
                    }
                }

            }
            return null;
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
        // only register on write to avoid any locking for transactions that only ever read
        File location = getDeltaLocation(id);
        // File location = ensureDeltaIsRegistered(id);
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

    private File getDeltaLocation(String id) throws IOException
    {
        File file = new File(indexDirectory, id).getCanonicalFile();
        return file;
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
        File location = getDeltaLocation(id);
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
                        indexEntries.put(id, new IndexEntry(IndexType.DELTA, id, "", TransactionStatus.ACTIVE, "", 0, 0, false));
                    }

                }
                finally
                { // Downgrade lock
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
            writer = new IndexWriter(location, analyzer, true, MaxFieldLength.LIMITED);
        }
        else
        {
            writer = new IndexWriter(location, analyzer, false, MaxFieldLength.LIMITED);
        }
        writer.setUseCompoundFile(writerUseCompoundFile);
        writer.setMaxBufferedDocs(writerMaxBufferedDocs);
        writer.setRAMBufferSizeMB(writerRamBufferSizeMb);
        writer.setMergeFactor(writerMergeFactor);
        writer.setMaxMergeDocs(writerMaxMergeDocs);
        writer.setWriteLockTimeout(writeLockTimeout);
        writer.setMaxFieldLength(maxFieldLength);
        writer.setTermIndexInterval(termIndexInterval);
        writer.setMergeScheduler(new SerialMergeScheduler());
        writer.setMergePolicy(new LogDocMergePolicy());
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
     * Get the deletions for a given index (there is no check if they should be applied that is up to the calling layer)
     * 
     * @param id
     * @return
     * @throws IOException
     */
    public Set<String> getDeletions(String id) throws IOException
    {
        if (id == null)
        {
            throw new IndexerException("\"null\" is not a valid identifier for a transaction");
        }
        // Check state
        Set<String> deletions = new HashSet<String>();
        File location = new File(indexDirectory, id).getCanonicalFile();
        File file = new File(location, INDEX_INFO_DELETIONS).getCanonicalFile();
        if (!file.exists())
        {
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("No deletions for " + id);
            }
            return Collections.<String> emptySet();
        }
        DataInputStream is = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
        int size = is.readInt();
        for (int i = 0; i < size; i++)
        {
            String ref = is.readUTF();
            deletions.add(ref);
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
    public void setPreparedState(String id, Set<String> toDelete, long documents, boolean deleteNodesOnly) throws IOException
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
            DataOutputStream os = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(location, INDEX_INFO_DELETIONS).getCanonicalFile())));
            os.writeInt(toDelete.size());
            for (String ref : toDelete)
            {
                os.writeUTF(ref);
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
            if ((entry.getStatus() != TransactionStatus.PREPARING) && (entry.getStatus() != TransactionStatus.COMMITTING))
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

    private void invalidateMainReadersFromFirst(Set<String> ids) throws IOException
    {
        boolean found = false;
        for (String id : indexEntries.keySet())
        {
            if (!found && ids.contains(id))
            {
                found = true;
            }
            if (found)
            {
                IndexReader main = mainIndexReaders.remove(id);
                if (main != null)
                {
                    ((ReferenceCounting) main).setInvalidForReuse();
                }
            }
        }

        if (found)
        {
            if(mainIndexReader != null)
            {
                ((ReferenceCounting) mainIndexReader).setInvalidForReuse();
                mainIndexReader = null;
            }
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
                    if (mainIndexReader != null)
                    {
                        ((ReferenceCounting)mainIndexReader).setInvalidForReuse();
                    }
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

                            public boolean canRetry()
                            {
                                return true;
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
            mainIndexReader.incRef();
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("Main index reader references = " + ((ReferenceCounting) mainIndexReader).getReferenceCount());
            }

            // ALF-10040: Wrap with a one-off CachingIndexReader (with cache disabled) so that LeafScorer behaves and passes through SingleFieldSelectors to the main index readers
            IndexReader reader = ReferenceCountingReadOnlyIndexReaderFactory.createReader(MAIN_READER + GUID.generate(), mainIndexReader, false, config);
            ReferenceCounting refCounting = (ReferenceCounting) reader;
            reader.incRef();
            refCounting.setInvalidForReuse();
            return reader;
        }
        catch (RuntimeException e)
        {
            e.printStackTrace();
            throw e;
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
    public IndexReader getMainIndexReferenceCountingReadOnlyIndexReader(String id, Set<String> deletions, boolean deleteOnlyNodes) throws IOException
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
                    if (mainIndexReader != null)
                    {
                        ((ReferenceCounting)mainIndexReader).setInvalidForReuse();
                    }
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

                            public boolean canRetry()
                            {
                                return true;
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
                reader = new MultiReader(new IndexReader[] { mainIndexReader, deltaReader }, false);
            }
            else
            {
                IndexReader filterReader = new FilterIndexReaderByStringId("main+id", mainIndexReader, deletions, deleteOnlyNodes);
                reader = new MultiReader(new IndexReader[] { filterReader, deltaReader }, false);
                // Cancel out extra incRef made by MultiReader
                filterReader.decRef();
            }

            // The reference count would have been incremented automatically by MultiReader /
            // FilterIndexReaderByStringId
            deltaReader.decRef();
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("Main index reader references = " + ((ReferenceCounting) mainIndexReader).getReferenceCount());
            }
            reader = ReferenceCountingReadOnlyIndexReaderFactory.createReader(MAIN_READER + id, reader, false, config);
            ReferenceCounting refCounting = (ReferenceCounting) reader;
            reader.incRef();
            refCounting.setInvalidForReuse();
            return reader;
        }
        finally
        {
            releaseReadLock();
        }
    }

    private boolean shouldBlock()
    {
        int pendingDeltas = 0;
        int maxDeltas = mergerTargetOverlaysBlockingFactor * mergerTargetOverlays;
        for (IndexEntry entry : indexEntries.values())
        {
            if (entry.getType() == IndexType.DELTA)
            {
                TransactionStatus status = entry.getStatus();
                if (status == TransactionStatus.PREPARED || status == TransactionStatus.COMMITTING
                        || status.isCommitted())
                {
                    if (++pendingDeltas > maxDeltas)
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void setStatus(final String id, final TransactionStatus state, final Set<Term> toDelete, final Set<Term> read) throws IOException
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
                // we may need to block for some deltas to be merged / rolled back
                IndexInfo alreadyPreparing = thisThreadPreparing.get(); 
                if (state == TransactionStatus.PREPARED)
                {
                    // To avoid deadlock (a thread with multiple deltas never proceeding to commit) we don't block if
                    // this thread is already in the prepare phase
                    if (alreadyPreparing != null)
                    {
                        if (s_logger.isDebugEnabled())
                        {
                            s_logger.debug("Can't throttle - " + Thread.currentThread().getName() + " already preparing");
                        }                                                
                    }
                    else
                    {
                        while (shouldBlock())
                        {
                            synchronized (mergerTargetLock)
                            {
                                if (s_logger.isDebugEnabled())
                                {
                                    s_logger.debug("THROTTLING: " + Thread.currentThread().getName() + " " + indexEntries.size());
                                }
                                releaseWriteLock();
                                try
                                {
                                    mergerTargetLock.wait();
                                }
                                catch (InterruptedException e)
                                {
                                }
                            }
                            getWriteLock();
                        }
                        thisThreadPreparing.set(this);
                    }
                }
                else
                {
                    // Only clear the flag when the outermost thread exits prepare
                    if (alreadyPreparing == this)
                    {
                        thisThreadPreparing.set(null);
                    }
                }

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

                        public boolean canRetry()
                        {
                            return true;
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
                throw new IndexerException("Invalid transition for " + id + " from " + entry.getStatus() + " to " + TransactionStatus.PREPARING);
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
						invalidateMainReadersFromFirst(Collections.singleton(current.getName()));
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
                throw new IndexerException("Invalid transition for " + id + " from " + entry.getStatus() + " to " + TransactionStatus.PREPARED);
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
                throw new IndexerException("Invalid transition for " + id + " from " + entry.getStatus() + " to " + TransactionStatus.COMMITTING);
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
            IndexEntry entry = indexEntries.get(id);
            tl.set(buildReferenceCountingIndexReader(id, entry.getDocumentCount()));
        }

        /**
         * This has to be protected to allow for retry
         */
        public void transition(String id, Set<Term> toDelete, Set<Term> read) throws IOException
        {
            IndexEntry entry = indexEntries.get(id);
            if (entry == null)
            {
                // We could be retrying - see if the index reader is known or the directory is left
                if (referenceCountingReadOnlyIndexReaders.get(id) == null)
                {
                    File location = new File(indexDirectory, id).getCanonicalFile();
                    if (!location.exists())
                    {
                        throw new IndexerException("Unknown transaction " + id);
                    }
                }

                clearOldReaders();
                cleaner.schedule();
            }

            if (TransactionStatus.COMMITTED.follows(entry.getStatus()))
            {
                // Do the deletions
                invalidateMainReadersFromFirst(Collections.singleton(id));
                if ((entry.getDocumentCount() + entry.getDeletions()) == 0)
                {
                    registerReferenceCountingIndexReader(id, tl.get());
                    indexEntries.remove(id);
                    if (s_logger.isDebugEnabled())
                    {
                        s_logger.debug("Removed commit with no new docs and no deletions");
                    }
                    clearOldReaders();
                    cleaner.schedule();
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

                    merger.schedule();
                }

            }
            else
            {
                throw new IndexerException("Invalid transition for " + id + " from " + entry.getStatus() + " to " + TransactionStatus.COMMITTED);
            }
            notifyListeners("CommittedTransactions", 1);
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
                throw new IndexerException("Invalid transition for " + id + " from " + entry.getStatus() + " to " + TransactionStatus.ROLLINGBACK);
            }
        }

        public boolean requiresFileLock()
        {
            return !TransactionStatus.ROLLINGBACK.isTransient();
        }
    }

    private class RolledBackTransition implements Transition
    {
        ThreadLocal<IndexReader> tl = new ThreadLocal<IndexReader>();

        public void beforeWithReadLock(String id, Set<Term> toDelete, Set<Term> read) throws IOException
        {
            closeDelta(id);
            IndexEntry entry = indexEntries.get(id);
            tl.set(buildReferenceCountingIndexReader(id, entry.getDocumentCount()));
        }

        public void transition(String id, Set<Term> toDelete, Set<Term> read) throws IOException
        {
            IndexEntry entry = indexEntries.get(id);
            if (entry == null)
            {
                // We could be retrying - see if the index reader is known or the directory is left
                if (referenceCountingReadOnlyIndexReaders.get(id) == null)
                {
                    File location = new File(indexDirectory, id).getCanonicalFile();
                    if (!location.exists())
                    {
                        throw new IndexerException("Unknown transaction " + id);
                    }
                }

                clearOldReaders();
                cleaner.schedule();
            }

            if (TransactionStatus.ROLLEDBACK.follows(entry.getStatus()))
            {
                entry.setStatus(TransactionStatus.ROLLEDBACK);
                writeStatus();

                registerReferenceCountingIndexReader(id, tl.get());
                indexEntries.remove(id);
                if (s_logger.isDebugEnabled())
                {
                    s_logger.debug("Removed rollback");
                }
                clearOldReaders();
                cleaner.schedule();
            }
            else
            {
                throw new IndexerException("Invalid transition for " + id + " from " + entry.getStatus() + " to " + TransactionStatus.ROLLEDBACK);
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
                // We could be retrying - see if the index reader is known or the directory is left
                if (referenceCountingReadOnlyIndexReaders.get(id) == null)
                {
                    File location = new File(indexDirectory, id).getCanonicalFile();
                    if (!location.exists())
                    {
                        throw new IndexerException("Unknown transaction " + id);
                    }
                }

                clearOldReaders();
                cleaner.schedule();
            }

            if (TransactionStatus.DELETABLE.follows(entry.getStatus()))
            {
                invalidateMainReadersFromFirst(Collections.singleton(id));
                indexEntries.remove(id);
                writeStatus();
                clearOldReaders();
                cleaner.schedule();
            }
            else
            {
                throw new IndexerException("Invalid transition for " + id + " from " + entry.getStatus() + " to " + TransactionStatus.DELETABLE);
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
                indexEntries.put(id, new IndexEntry(IndexType.DELTA, id, "", TransactionStatus.ACTIVE, "", 0, 0, false));
            }
            else
            {
                throw new IndexerException("Invalid transition for " + id + " from " + entry.getStatus() + " to " + TransactionStatus.ACTIVE);
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

    private void clearInvalid(Set<String> inValid) throws IOException
    {
        boolean hasInvalid = false;
        for (String id : inValid)
        {
            IndexReader reader = referenceCountingReadOnlyIndexReaders.remove(id);
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("... invalidating sub reader " + id);
            }
            if (reader != null)
            {
                ReferenceCounting referenceCounting = (ReferenceCounting) reader;
                referenceCounting.setInvalidForReuse();
                deletableReaders.add(reader);
                hasInvalid = true;
            }
        }
        if (hasInvalid)
        {
            for (String id : inValid)
            {
                IndexReader main = mainIndexReaders.remove(id);
                if (main != null)
                {
                    ((ReferenceCounting) main).setInvalidForReuse();
                }
            }
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
        IndexReader oldReader = null;
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
                    boolean oldReaderIsSubReader = oldReader == null;
                    oldReader = reader;
                    reader = mainIndexReaders.get(id);
                    if (reader == null)
                    {
                        if (entry.getType() == IndexType.INDEX)
                        {
                            reader = new MultiReader(new IndexReader[] { oldReader, subReader }, false);
                        }
                        else if (entry.getType() == IndexType.DELTA)
                        {
                            try
                            {
                                IndexReader filterReader = new FilterIndexReaderByStringId(id, oldReader, getDeletions(entry.getName()), entry.isDeletOnlyNodes());
                                reader = new MultiReader(new IndexReader[] { filterReader, subReader }, false);
                                // Cancel out the incRef on the filter reader
                                filterReader.decRef();
                            }
                            catch (IOException ioe)
                            {
                                s_logger.error("Failed building filter reader beneath " + entry.getName(), ioe);
                                throw ioe;
                            }
                        }
                        reader = ReferenceCountingReadOnlyIndexReaderFactory.createReader(id+"multi", reader, true, config);
                        mainIndexReaders.put(id, reader);
                    }
                }
            }
        }
        if (reader == null)
        {
            reader = IndexReader.open(emptyIndex);
        }
		else
		{
	        // Keep this reader open whilst it is referenced by mainIndexReaders / referenceCountingReadOnlyIndexReaders
	        reader.incRef();
	    }
		
        reader = ReferenceCountingReadOnlyIndexReaderFactory.createReader(MAIN_READER, reader, false, config);
        return reader;
    }

    private IndexReader getReferenceCountingIndexReader(String id) throws IOException
    {
        IndexReader reader = referenceCountingReadOnlyIndexReaders.get(id);
        if (reader == null)
        {
            throw new IllegalStateException("Indexer should have been pre-built for " + id);
        }
        return reader;
    }

    private void registerReferenceCountingIndexReader(String id, IndexReader reader) throws IOException
    {
        clearInvalid(Collections.singleton(id));
        ReferenceCounting referenceCounting = (ReferenceCounting) reader;
        if (!referenceCounting.getId().equals(id))
        {
            throw new IllegalStateException("Registering " + referenceCounting.getId() + " as " + id);
        }
        referenceCountingReadOnlyIndexReaders.put(id, reader);
    }

    private double getSizeInMb(File file)
    {
        long size = getSize(file);
        return size/1024.0d/1024.0d;
    }
    
    private long getSize(File file)
    {
        long size = 0l;
        if (file == null)
        {
            return size;
        }
        if (file.isFile())
        {
            return file.length();
        }
        else
        {
            File[] files = file.listFiles();
            if(files == null)
            {
                return size;
            }
            for (File current : files)
            {
                if (current.isDirectory())
                {
                    size += getSize(current);
                }
                else
                {
                    size += current.length();
                }
            }
        }
        return size;
    }

    private IndexReader buildReferenceCountingIndexReader(String id, long size) throws IOException
    {
        IndexReader reader;
        File location = new File(indexDirectory, id).getCanonicalFile();
        double folderSize = getSizeInMb(location);
        if (IndexReader.indexExists(location))
        {
            if ((size < maxDocsForInMemoryIndex) && (folderSize < maxRamInMbForInMemoryIndex))
            {
                RAMDirectory rd = new RAMDirectory(location);
                reader = IndexReader.open(rd);
            }
            else
            {
                reader = IndexReader.open(location);
            }
        }
        else
        {
            reader = IndexReader.open(emptyIndex);
        }
        reader = ReferenceCountingReadOnlyIndexReaderFactory.createReader(id, reader, true, config);
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
                        newIndexEntries.put(name, new IndexEntry(indexType, name, parentName, status, mergeId, documentCount, deletions, isDeletOnlyNodes));
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
        // We have a state that allows more transactions. Notify waiting threads
        if (!shouldBlock())
        {
            synchronized (mergerTargetLock)
            {
                mergerTargetLock.notifyAll();
            }            
        }
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

        public boolean canRetry();
    }

    public <R> R doReadOnly(LockWork<R> lockWork)
    {

        readOnlyLock.writeLock().lock();
        try
        {
            getReadLock();
            try
            {
                return doWithFileLock(lockWork);
            }
            finally
            {
                releaseReadLock();
            }
        }
        finally
        {
            readOnlyLock.writeLock().unlock();
        }
    }

    private static final int CHANNEL_OPEN_RETRIES = 5;

    private <R> R doWithFileLock(LockWork<R> lockWork)
    {
        try
        {
            return doWithFileLock(lockWork, CHANNEL_OPEN_RETRIES);
        }
        catch (Throwable e)
        {
            // Re-throw the exception
            if (e instanceof RuntimeException)
            {
                throw (RuntimeException) e;
            }
            else
            {
                throw new RuntimeException("Error during run with lock.", e);
            }
        }
    }

    /**
     * Specific exception to catch channel close issues.
     * 
     * @author Derek Hulley
     * @since 2.1.3
     */
    private static class IndexInfoChannelException extends IOException
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1588898991653057286L;

        public IndexInfoChannelException(String msg)
        {
            super(msg);
        }
    }

    /**
     * An iterative method that retries the operation in the event of the channel being closed.
     * 
     * @param retriesRemaining
     *            the number of retries remaining
     * @return Returns the lock work result
     */
    private <R> R doWithFileLock(LockWork<R> lockWork, int retriesRemaining) throws Throwable
    {
        FileLock fileLock = null;
        R result = null;
        long start = 0L;
        try
        {
            // Check that the channel is open
            if (!indexInfoChannel.isOpen())
            {
                if (lockWork.canRetry())
                {
                    throw new IndexInfoChannelException("Channel is closed.  Manually triggering reopen attempts");
                }
                else
                {
                    reopenChannels();
                }
            }

            if (indexIsShared)
            {
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
        catch (IOException e)
        {
            if (!lockWork.canRetry())
            {
                // We've done our best
                s_logger.warn("This operation can not retry upon an IOException - it has to roll back to its previous state");
                throw e;
            }
            if (retriesRemaining == 0)
            {
                // We've done our best
                s_logger.warn("No more channel open retries remaining");
                throw e;
            }
            else
            {
                // Attempt to reopen the channel
                if (s_logger.isDebugEnabled())
                {
                    s_logger.debug("\n" + "Channel is closed.  Will attempt to open it. \n" + "   Retries remaining: " + retriesRemaining);
                }
                try
                {
                    reopenChannels();
                    // Loop around and try again
                    return doWithFileLock(lockWork, --retriesRemaining);
                }
                catch (Throwable ee)
                {
                    // Report this error, but throw the original
                    s_logger.error("Channel reopen failed on index info files in: " + this.indexDirectory, ee);
                    throw e;
                }
            }
        }
        finally
        {
            if (fileLock != null)
            {
                try
                {
                    fileLock.release();
                    long end = System.nanoTime();
                    if (s_logger.isDebugEnabled())
                    {
                        s_logger.debug(" ... released file lock after " + ((end - start) / 10e6f) + " ms");
                    }
                }
                catch (IOException e)
                {
                    s_logger.warn("Failed to release file lock: " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Reopens all the channels. The channels are closed first. This method is synchronized.
     */
    private synchronized void reopenChannels() throws Throwable
    {
        try
        {
            indexInfoRAF.close();
        }
        catch (IOException e)
        {
            s_logger.warn("Failed to close indexInfoRAF", e);
        }
        try
        {
            indexInfoBackupRAF.close();
        }
        catch (IOException e)
        {
            s_logger.warn("Failed to close indexInfoRAF", e);
        }
        File indexInfoFile = new File(this.indexDirectory, INDEX_INFO);
        File indexInfoBackupFile = new File(this.indexDirectory, INDEX_INFO_BACKUP);

        // Open the files and channels for the index info file and the backup
        this.indexInfoRAF = openFile(indexInfoFile);
        this.indexInfoChannel = this.indexInfoRAF.getChannel();

        this.indexInfoBackupRAF = openFile(indexInfoBackupFile);
        this.indexInfoBackupChannel = this.indexInfoBackupRAF.getChannel();
    }

    /**
     * Helper to print out index information
     * 
     * @param args
     * @throws Throwable
     */
    public static void main(String[] args) throws Throwable
    {
        for (int i = 0; i < args.length; i++)
        {
            File indexLocation = new File(args[i]);
            if (!indexLocation.exists())
            {
                System.err.println("Index directory doesn't exist: " + indexLocation);
                continue;
            }
            readIndexInfo(indexLocation);
        }
    }

    static Query getPathQuery(String path) throws SAXPathException
    {
        ApplicationContext ac = ApplicationContextHelper.getApplicationContext();
        XPathReader reader = new XPathReader();
        LuceneXPathHandler handler = new LuceneXPathHandler();
        handler.setNamespacePrefixResolver((NamespaceService) ac.getBean("namespaceService"));
        handler.setDictionaryService((DictionaryService) ac.getBean("dictionaryService"));
        reader.setXPathHandler(handler);
        reader.parse(path);
        PathQuery pathQuery = handler.getQuery();
        pathQuery.setRepeats(false);
        return pathQuery;
    }

    private static void readIndexInfo(File indexLocation) throws Throwable
    {
        long start;
        long end;
        IndexInfo ii = new IndexInfo(indexLocation, null);

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
        IndexReader reader = ii.getMainIndexReferenceCountingReadOnlyIndexReader();
        System.out.println(reader.getFieldNames(FieldOption.ALL));

        TermEnum te = reader.terms();
        while (te.next())
        {
            if (te.term().field().contains("FTS"))
            {
                System.out.println(te.term());
            }
        }
        // @{http://www.alfresco.org/model/content/1.0}name:product363_ocmwbeersel

        IndexSearcher searcher = new IndexSearcher(reader);
        Query query = new TermQuery(new Term("@{http://www.alfresco.org/model/content/1.0}name", "product363_ocmwbeersel"));
        start = System.nanoTime();
        Hits hits = searcher.search(query);
        end = System.nanoTime();
        System.out.println("@{http://www.alfresco.org/model/content/1.0}name:product363_ocmwbeersel = " + hits.length() + " in " + ((end - start) / 1e9));
        searcher.close();

        searcher = new IndexSearcher(reader);
        query = new WildcardQuery(new Term("@{http://www.alfresco.org/model/content/1.0}name", "b*"));
        start = System.nanoTime();
        hits = searcher.search(query);
        end = System.nanoTime();
        System.out.println("@{http://www.alfresco.org/model/content/1.0}name:b* = " + hits.length() + " in " + ((end - start) / 1e9));
        searcher.close();

        searcher = new IndexSearcher(reader);
        query = new TermQuery(new Term("@{http://www.alfresco.org/model/content/1.0}name", "be"));
        start = System.nanoTime();
        hits = searcher.search(query);
        end = System.nanoTime();
        System.out.println("@{http://www.alfresco.org/model/content/1.0}name:be = " + hits.length() + " in " + ((end - start) / 1e9));
        searcher.close();
        //
        // searcher = new IndexSearcher(reader);
        // query = new WildcardQuery(new Term("@{http://www.travelmuse.com/wcm}DestinationPhoto", "*"));
        // start = System.nanoTime();
        // hits = searcher.search(query);
        // end = System.nanoTime();
        // System.out.println("@{http://www.travelmuse.com/wcm}DestinationPhoto:* = " + hits.length() + " in " + ((end -
        // start) / 1e9));
        // searcher.close();
        //
        // searcher = new IndexSearcher(reader);
        // query = new WildcardQuery(new Term("@{http://www.travelmuse.com/wcm}DestinationPhoto", "*"));
        // start = System.nanoTime();
        // hits = searcher.search(query);
        // end = System.nanoTime();
        // System.out.println("@{http://www.travelmuse.com/wcm}DestinationPhoto:* = " + hits.length() + " in " + ((end -
        // start) / 1e9));
        // searcher.close();
        //
        // searcher = new IndexSearcher(reader);
        // query = new TermQuery(new Term("@{http://www.travelmuse.com/wcm}ThemeName", "bambino"));
        // start = System.nanoTime();
        // hits = searcher.search(query);
        // end = System.nanoTime();
        // System.out.println("@{http://www.travelmuse.com/wcm}ThemeName:bambino = " + hits.length() + " in " + ((end -
        // start) / 1e9));
        // searcher.close();
        //
        // searcher = new IndexSearcher(reader);
        // query = new TermQuery(new Term("@{http://www.travelmuse.com/wcm}ThemeName", "bambino"));
        // start = System.nanoTime();
        // hits = searcher.search(query);
        // end = System.nanoTime();
        // System.out.println("@{http://www.travelmuse.com/wcm}ThemeName:bambino = " + hits.length() + " in " + ((end -
        // start) / 1e9));
        // searcher.close();
        //
        // searcher = new IndexSearcher(reader);
        // query = new TermQuery(new Term("@{http://www.travelmuse.com/wcm}ActivityName", "bambino"));
        // start = System.nanoTime();
        // hits = searcher.search(query);
        // end = System.nanoTime();
        // System.out.println("@{http://www.travelmuse.com/wcm}ActivityName:bambino = " + hits.length() + " in " + ((end
        // - start) / 1e9));
        // searcher.close();
        //
        // searcher = new IndexSearcher(reader);
        // query = new TermQuery(new Term("@{http://www.travelmuse.com/wcm}ActivityName", "bambino"));
        // start = System.nanoTime();
        // hits = searcher.search(query);
        // end = System.nanoTime();
        // System.out.println("@{http://www.travelmuse.com/wcm}ActivityName:bambino = " + hits.length() + " in " + ((end
        // - start) / 1e9));
        // searcher.close();
        //
        // searcher = new IndexSearcher(reader);
        // query = new TermQuery(new Term("@{http://www.travelmuse.com/wcm}EditorialItemTitle", "bambino"));
        // start = System.nanoTime();
        // hits = searcher.search(query);
        // end = System.nanoTime();
        // System.out.println("@{http://www.travelmuse.com/wcm}EditorialItemTitle:bambino = " + hits.length() + " in " +
        // ((end - start) / 1e9));
        // searcher.close();
        //
        // searcher = new IndexSearcher(reader);
        // query = new TermQuery(new Term("@{http://www.travelmuse.com/wcm}EditorialItemTitle", "bambino"));
        // start = System.nanoTime();
        // hits = searcher.search(query);
        // end = System.nanoTime();
        // System.out.println("@{http://www.travelmuse.com/wcm}EditorialItemTitle:bambino = " + hits.length() + " in " +
        // ((end - start) / 1e9));
        // searcher.close();
        //
        // searcher = new IndexSearcher(reader);
        // query = new TermQuery(new Term("@{http://www.travelmuse.com/wcm}PoiName", "bambino"));
        // start = System.nanoTime();
        // hits = searcher.search(query);
        // end = System.nanoTime();
        // System.out.println("@{http://www.travelmuse.com/wcm}PoiName:bambino = " + hits.length() + " in " + ((end -
        // start) / 1e9));
        // searcher.close();
        //
        // searcher = new IndexSearcher(reader);
        // query = new TermQuery(new Term("@{http://www.travelmuse.com/wcm}PoiName", "bambino"));
        // start = System.nanoTime();
        // hits = searcher.search(query);
        // end = System.nanoTime();
        // System.out.println("@{http://www.travelmuse.com/wcm}PoiName:bambino = " + hits.length() + " in " + ((end -
        // start) / 1e9));
        // searcher.close();
        //
        // searcher = new IndexSearcher(reader);
        // query = new TermQuery(new Term("@{http://www.travelmuse.com/wcm}PropertyName", "bambino"));
        // start = System.nanoTime();
        // hits = searcher.search(query);
        // end = System.nanoTime();
        // System.out.println("@{http://www.travelmuse.com/wcm}PropertyName:bambino = " + hits.length() + " in " + ((end
        // - start) / 1e9));
        // searcher.close();
        //
        // searcher = new IndexSearcher(reader);
        // query = new TermQuery(new Term("@{http://www.travelmuse.com/wcm}PropertyName", "bambino"));
        // start = System.nanoTime();
        // hits = searcher.search(query);
        // end = System.nanoTime();
        // System.out.println("@{http://www.travelmuse.com/wcm}PropertyName:bambino = " + hits.length() + " in " + ((end
        // - start) / 1e9));
        // searcher.close();
        //
        // searcher = new IndexSearcher(reader);
        // query = new TermQuery(new Term("@{http://www.alfresco.org/model/content/1.0}content", "bambino"));
        // start = System.nanoTime();
        // hits = searcher.search(query);
        // end = System.nanoTime();
        // System.out.println("@{http://www.alfresco.org/model/content/1.0}content:bambino = " + hits.length() + " in "
        // + ((end - start) / 1e9));
        // searcher.close();
        //
        // searcher = new IndexSearcher(reader);
        // query = new TermQuery(new Term("@{http://www.alfresco.org/model/content/1.0}content", "bambino"));
        // start = System.nanoTime();
        // hits = searcher.search(query);
        // end = System.nanoTime();
        // System.out.println("@{http://www.alfresco.org/model/content/1.0}content:bambino = " + hits.length() + " in "
        // + ((end - start) / 1e9));
        // searcher.close();
        //        
        // searcher = new IndexSearcher(reader);
        // query = getPathQuery("/www/avm_webapps/ROOT/editorial//*");
        // start = System.nanoTime();
        // hits = searcher.search(query);
        // end = System.nanoTime();
        // System.out.println("/www/avm_webapps/ROOT/editorial//* = " + hits.length() + " in " + ((end - start) / 1e9));
        // searcher.close();
        //        
        // searcher = new IndexSearcher(reader);
        // query = getPathQuery("/www/avm_webapps/ROOT/editorial//*");
        // start = System.nanoTime();
        // hits = searcher.search(query);
        // end = System.nanoTime();
        // System.out.println("/www/avm_webapps/ROOT/editorial//* = " + hits.length() + " in " + ((end - start) / 1e9));
        // searcher.close();
        //
        //        
        // searcher = new IndexSearcher(reader);
        // query = getPathQuery("/www/avm_webapps/ROOT/tag//*");
        // start = System.nanoTime();
        // hits = searcher.search(query);
        // end = System.nanoTime();
        // System.out.println("/www/avm_webapps/ROOT/tag//* = " + hits.length() + " in " + ((end - start) / 1e9));
        // searcher.close();
        //        
        // searcher = new IndexSearcher(reader);
        // query = getPathQuery("/www/avm_webapps/ROOT/tag//*");
        // start = System.nanoTime();
        // hits = searcher.search(query);
        // end = System.nanoTime();
        // System.out.println("/www/avm_webapps/ROOT/tag//* = " + hits.length() + " in " + ((end - start) / 1e9));
        // searcher.close();
        //        
        // searcher = new IndexSearcher(reader);
        // query = getPathQuery("/www/avm_webapps/ROOT/poi//*");
        // start = System.nanoTime();
        // hits = searcher.search(query);
        // end = System.nanoTime();
        // System.out.println("/www/avm_webapps/ROOT/poi//* = " + hits.length() + " in " + ((end - start) / 1e9));
        // searcher.close();
        //        
        // searcher = new IndexSearcher(reader);
        // query = getPathQuery("/www/avm_webapps/ROOT/poi//*");
        // start = System.nanoTime();
        // hits = searcher.search(query);
        // end = System.nanoTime();
        // System.out.println("/www/avm_webapps/ROOT/poi//* = " + hits.length() + " in " + ((end - start) / 1e9));
        // searcher.close();
        //        
        // searcher = new IndexSearcher(reader);
        // query = getPathQuery("/www/avm_webapps/ROOT/property//*");
        // start = System.nanoTime();
        // hits = searcher.search(query);
        // end = System.nanoTime();
        // System.out.println("/www/avm_webapps/ROOT/property//* = " + hits.length() + " in " + ((end - start) / 1e9));
        // searcher.close();
        //        
        // searcher = new IndexSearcher(reader);
        // query = getPathQuery("/www/avm_webapps/ROOT/property//*");
        // start = System.nanoTime();
        // hits = searcher.search(query);
        // end = System.nanoTime();
        // System.out.println("/www/avm_webapps/ROOT/property//* = " + hits.length() + " in " + ((end - start) / 1e9));
        // searcher.close();
        //        
        // searcher = new IndexSearcher(reader);
        // query = getPathQuery("/www/avm_webapps/ROOT/web-reviews//*");
        // start = System.nanoTime();
        // hits = searcher.search(query);
        // end = System.nanoTime();
        // System.out.println("/www/avm_webapps/ROOT/web-reviews//* = " + hits.length() + " in " + ((end - start) /
        // 1e9));
        // searcher.close();
        //        
        // searcher = new IndexSearcher(reader);
        // query = getPathQuery("/www/avm_webapps/ROOT/web-reviews//*");
        // start = System.nanoTime();
        // hits = searcher.search(query);
        // end = System.nanoTime();
        // System.out.println("/www/avm_webapps/ROOT/web-reviews//* = " + hits.length() + " in " + ((end - start) /
        // 1e9));
        // searcher.close();

        // TermEnum terms = reader.terms(new Term("@{http://www.alfresco.org/model/user/1.0}members", ""));
        // while (terms.next() && terms.term().field().equals("@{http://www.alfresco.org/model/user/1.0}members"))
        // {
        // System.out.println("F = " + terms.term().field() + " V = " + terms.term().text() + " F = " +
        // terms.docFreq());
        // if (terms.term().text().equals("xirmsi"))
        // {
        // System.out.println("Matched");
        // }
        // }
        // terms.close();

    }

    /**
     * Clean up support.
     * 
     * @author Andy Hind
     */
    private class Cleaner extends AbstractSchedulable
    {

        String getLogName()
        {
            return "Index cleaner";
        }

        void runImpl()
        {

            Iterator<IndexReader> i = deletableReaders.iterator();
            while (i.hasNext())
            {
                IndexReader reader = i.next();
                ReferenceCounting refCounting = (ReferenceCounting) reader;
                if (refCounting.getReferenceCount() == 0)
                {
                    if (s_logger.isDebugEnabled())
                    {
                        s_logger.debug("Deleting no longer referenced " + refCounting.getId());
                        s_logger.debug("... queued delete for " + refCounting.getId());
                        s_logger.debug("... " + ReferenceCountingReadOnlyIndexReaderFactory.getState(refCounting.getId()));
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
                    i.remove();
                }
                else if (s_logger.isTraceEnabled() && refCounting.getCreationTime() < System.currentTimeMillis() - 120000)
                {
                    for (Throwable t : refCounting.getReferences())
                    {
                        s_logger.trace(t.getMessage(), t);
                    }
                }
                    
            }

            Iterator<String> j = deleteQueue.iterator();
            while (j.hasNext())
            {
                String id = j.next();
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
                    }
                    else
                    {
                        j.remove();
                    }
                }
                catch (IOException ioe)
                {
                    s_logger.warn("Failed to delete file - invalid canonical file", ioe);
                }
            }
        }

        ExitState recoverImpl()
        {
            return ExitState.DONE;
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

    private enum ScheduledState
    {
        UN_SCHEDULED, SCHEDULED, FAILED, RECOVERY_SCHEDULED
    }

    private enum ExitState
    {
        DONE, RESCHEDULE;
    }

    private abstract class AbstractSchedulable implements Schedulable, Runnable
    {
        ScheduledState scheduledState = ScheduledState.UN_SCHEDULED;
        
        

        public synchronized void schedule()
        {
            switch (scheduledState)
            {
            case FAILED:
                scheduledState = ScheduledState.RECOVERY_SCHEDULED;
                threadPoolExecutor.execute(this);
                break;
            case UN_SCHEDULED:
                scheduledState = ScheduledState.SCHEDULED;
                threadPoolExecutor.execute(this);
                break;
            case RECOVERY_SCHEDULED:
            case SCHEDULED:
            default:
                // Nothing to do
                break;
            }
        }

        synchronized void done()
        {
            switch (scheduledState)
            {
            case RECOVERY_SCHEDULED:
            case SCHEDULED:
                scheduledState = ScheduledState.UN_SCHEDULED;
                break;
            case FAILED:
            case UN_SCHEDULED:
            default:
                throw new IllegalStateException();
            }
        }

        private synchronized void rescheduleRecovery()
        {
            switch (scheduledState)
            {
            case RECOVERY_SCHEDULED:
                threadPoolExecutor.execute(this);
                break;
            case SCHEDULED:
            case FAILED:
            case UN_SCHEDULED:
            default:
                throw new IllegalStateException();
            }
        }

        private synchronized void fail()
        {
            switch (scheduledState)
            {
            case RECOVERY_SCHEDULED:
            case SCHEDULED:
                scheduledState = ScheduledState.FAILED;
                break;
            case FAILED:
            case UN_SCHEDULED:
            default:
                throw new IllegalStateException();
            }
        }

        public void run()
        {
            try
            {
                switch (scheduledState)
                {
                case RECOVERY_SCHEDULED:
                    ExitState reschedule = recoverImpl();
                    s_logger.error(getLogName() + " has recovered - resuming ... ");
                    if (reschedule == ExitState.RESCHEDULE)
                    {
                        rescheduleRecovery();
                        break;
                    }
                case SCHEDULED:
                    if (s_logger.isDebugEnabled())
                    {
                        s_logger.debug(getLogName() + " running ... ");
                    }
                    runImpl();
                    done();
                    break;
                case FAILED:
                case UN_SCHEDULED:
                default:
                    throw new IllegalStateException();
                }
            }
            catch (Throwable t)
            {
                try
                {
                    if (s_logger.isWarnEnabled())
                    {
                        s_logger.warn(getLogName() + " failed with ", t);
                    }
                    recoverImpl();
                    if (s_logger.isWarnEnabled())
                    {
                        s_logger.warn(getLogName() + " recovered from ", t);
                    }
                    done();
                }
                catch (Throwable rbt)
                {
                    fail();
                    s_logger.error(getLogName() + " failed to recover - suspending ", rbt);
                }
            }
        }

        abstract void runImpl() throws Exception;

        abstract ExitState recoverImpl() throws Exception;

        abstract String getLogName();
    }

    private class Merger extends AbstractSchedulable
    {
        String getLogName()
        {
            return "Index merger";
        }

        @Override
        void done()
        {
            // Reschedule if we need to, based on the current index state, that may have changed since we last got the
            // read lock
            getReadLock();
            try
            {
                synchronized (this)
                {
                    if (decideMergeAction() != MergeAction.NONE)
                    {
                        if (s_logger.isDebugEnabled())
                        {
                            s_logger.debug(getLogName() + " rescheduling ... ");
                        }
                        switch (scheduledState)
                        {
                        case RECOVERY_SCHEDULED:
                            scheduledState = ScheduledState.SCHEDULED;
                        case SCHEDULED:
                            threadPoolExecutor.execute(this);
                            break;
                        case FAILED:
                        case UN_SCHEDULED:
                        default:
                            throw new IllegalStateException();
                        }
                    }
                    else
                    {
                        if (s_logger.isDebugEnabled())
                        {
                            s_logger.debug(getLogName() + " done ");
                        }
                        super.done();
                    }
                }
            }
            finally
            {
                releaseReadLock();
            }
        }

        void runImpl() throws IOException
        {

            // Get the read lock to decide what to do
            // Single JVM to start with
            MergeAction action;

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

                            public boolean canRetry()
                            {
                                return true;
                            }
                        });
                    }
                    finally
                    {
                        getReadLock();
                        releaseWriteLock();
                    }
                }

                action = decideMergeAction();
            }

            catch (IOException e)
            {
                s_logger.error("Error reading index file", e);
                return;
            }
            finally
            {
                releaseReadLock();
            }

            if (s_logger.isDebugEnabled())
            {
                s_logger.debug(getLogName() + " Merger applying MergeAction." + action.toString());
            }                
            if (action == MergeAction.APPLY_DELTA_DELETION)
            {
                mergeDeletions();
            }
            else if (action == MergeAction.MERGE_INDEX)
            {
                mergeIndexes();
            }
            if (s_logger.isDebugEnabled())
            {
                dumpInfo();
            }                
        }

        /**
         * @param action
         */
        private MergeAction decideMergeAction()
        {
            MergeAction action = MergeAction.NONE;
            int indexes = 0;
            boolean mergingIndexes = false;
            int deltas = 0;
            boolean applyingDeletions = false;

            for (IndexEntry entry : indexEntries.values())
            {
                if (entry.getType() == IndexType.INDEX)
                {
                    indexes++;
                    if ((entry.getStatus() == TransactionStatus.MERGE) || (entry.getStatus() == TransactionStatus.MERGE_TARGET))
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
                        deltas++;
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
                if (indexes > mergerTargetIndexes) 
                {
                    // Try merge
                    action = MergeAction.MERGE_INDEX;
                }
                else if (deltas > mergerTargetOverlays)
                {
                    // Try delete
                    action = MergeAction.APPLY_DELTA_DELETION;
                }
            }
            return action;
        }

        ExitState recoverImpl()
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
                                case UNKNOWN:
                                case PREPARED:
                                case DELETABLE:
                                case COMMITTING:
                                case COMMITTED:
                                default:
                                    if (s_logger.isInfoEnabled())
                                    {
                                        s_logger.info("Roll back merge: leaving index entry " + entry);
                                    }
                                    break;
                                // States which are in mid-transition which we
                                // can roll back to the committed state
                                case COMMITTED_DELETING:
                                case MERGE:
                                    if (s_logger.isInfoEnabled())
                                    {
                                        s_logger.info("Roll back merge: Resetting merge and committed_deleting to committed " + entry);
                                    }
                                    entry.setStatus(TransactionStatus.COMMITTED);
                                    break;
                                case MERGE_TARGET:
                                    if (s_logger.isInfoEnabled())
                                    {
                                        s_logger.info("Roll back merge: Deleting merge target " + entry);
                                    }
                                    entry.setStatus(TransactionStatus.DELETABLE);
                                    deletable.add(entry.getName());
                                    break;
                                }

                                // Check we have a reader registered
                                if (referenceCountingReadOnlyIndexReaders.get(entry.getName()) == null)
                                {
                                    registerReferenceCountingIndexReader(entry.getName(), buildReferenceCountingIndexReader(entry.getName(), entry.getDocumentCount()));
                                }
                            }

                            if (mainIndexReader != null)
                            {
                                ReferenceCounting rcMain = (ReferenceCounting) mainIndexReader;
                                if (rcMain.isInvalidForReuse())
                                {
                                    mainIndexReader = null;
                                }
                            }

                            // Delete entries that are not required
							invalidateMainReadersFromFirst(deletable);                            
                            for (String id : deletable)
                            {
                                indexEntries.remove(id);
                            }
                            clearOldReaders();

                            cleaner.schedule();

                            // persist the new state
                            writeStatus();
                        }
                        return null;
                    }

                    public boolean canRetry()
                    {
                        return false;
                    }

                });
            }
            finally
            {
                releaseWriteLock();
            }
            return ExitState.DONE;
        }

        void mergeDeletions() throws IOException
        {
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("Deleting ...");
            }

            // lock for deletions
            final LinkedHashMap<String, IndexEntry> toDelete;
            LinkedHashMap<String, IndexEntry> indexes;

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
                            if ((entry.getType() == IndexType.INDEX) && (entry.getStatus() == TransactionStatus.MERGE_TARGET))
                            {
                                return set;
                            }
                            if ((entry.getType() == IndexType.DELTA) && (entry.getStatus() == TransactionStatus.COMMITTED_DELETING))
                            {
                                return set;
                            }
                        }
                        // Check it is not deleting
                        BREAK: for (IndexEntry entry : indexEntries.values())
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
                                    break BREAK;
                                }
                            }
                        }
                        if (set.size() > 0)
                        {
                            writeStatus();
                        }
                        return set;

                    }

                    public boolean canRetry()
                    {
                        return false;
                    }

                });
            }
            finally
            {
                getReadLock();
                releaseWriteLock();
            }

            try
            {
                indexes = new LinkedHashMap<String, IndexEntry>();
                BREAK: for (IndexEntry entry : indexEntries.values())
                {
                    if (entry.getStatus() == TransactionStatus.COMMITTED_DELETING)
                    {
                        break BREAK;
                    }
                    indexes.put(entry.getName(), entry);
                }
            }
            finally
            {
                releaseReadLock();
            }

            if (toDelete.size() == 0)
            {
                return;
            }
            // Build readers

            int size = 2 * (toDelete.size() + indexes.size());
            final HashSet<String> invalidIndexes = new HashSet<String>(size);

            final HashMap<String, Long> newIndexCounts = new HashMap<String, Long>(size);

            LinkedHashMap<String, IndexReader> readers = new LinkedHashMap<String, IndexReader>(size);
            for (IndexEntry currentDelete : toDelete.values())
            {
                Set<String> deletions = getDeletions(currentDelete.getName());
                if (!deletions.isEmpty())
                {
                    for (String key : indexes.keySet())
                    {
                        IndexReader reader = getReferenceCountingIndexReader(key);
                        Searcher searcher = new IndexSearcher(reader);
                        try
                        {
                            for (String stringRef : deletions)
                            {
                                TermQuery query = new TermQuery(new Term("ID", stringRef));
                                Hits hits = searcher.search(query);
                                if (hits.length() > 0)
                                {
                                    IndexReader writeableReader = readers.get(key);
                                    if (writeableReader == null)
                                    {
                                        File location = new File(indexDirectory, key).getCanonicalFile();
                                        if (IndexReader.indexExists(location))
                                        {
                                            writeableReader = IndexReader.open(location);
                                        }
                                        else
                                        {
                                            continue;
                                        }
                                        readers.put(key, writeableReader);
                                    }
                                    
                                    if (currentDelete.isDeletOnlyNodes())
                                    {
                                        Searcher writeableSearcher = new IndexSearcher(writeableReader);        
                                        hits = writeableSearcher.search(query);
                                        if (hits.length() > 0)
                                        {
                                            for (int i = 0; i < hits.length(); i++)
                                            {
                                                Document doc = hits.doc(i);
                                                if (doc.getField("ISCONTAINER") == null)
                                                {
                                                    writeableReader.deleteDocument(hits.id(i));
                                                    invalidIndexes.add(key);
                                                    // There should only be one thing to
                                                    // delete
                                                    // break;
                                                }
                                            }
                                        }
                                        writeableSearcher.close();
                                    }
                                    else
                                    {
                                        int deletedCount = 0;
                                        try
                                        {
                                            deletedCount = writeableReader.deleteDocuments(new Term("ID", stringRef));
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
                                                s_logger.debug("Deleted " + deletedCount + " from " + key + " for id " + stringRef + " remaining docs " + writeableReader.numDocs());
                                            }
                                            invalidIndexes.add(key);
                                        }
                                    }
                                }
                            }
                        }
                        finally
                        {
                            searcher.close();
                        }    
                    }
                }
                // The delta we have just processed now must be included when we process the deletions of its successor
                indexes.put(currentDelete.getName(), currentDelete);
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

            // Prebuild all readers for affected indexes
            // Register them in the commit.

            final HashMap<String, IndexReader> newReaders = new HashMap<String, IndexReader>();

            for (String id : invalidIndexes)
            {
                IndexReader reader = buildReferenceCountingIndexReader(id, newIndexCounts.get(id));
                newReaders.put(id, reader);
            }

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
                            entry.setType(IndexType.INDEX);
                            entry.setDeletions(0);
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

                        // Invalidate all main index readers from the first invalid index onwards
						invalidateMainReadersFromFirst(invalidIndexes);


                        if (s_logger.isDebugEnabled())
                        {
                            for (String id : toDelete.keySet())
                            {
                                s_logger.debug("...applied deletion for " + id);
                            }
                            s_logger.debug("...deleting done");
                        }

                        dumpInfo();

                        notifyListeners("MergedDeletions", toDelete.size());

                        return null;
                    }

                    public boolean canRetry()
                    {
                        return false;
                    }

                });

            }
            finally
            {
                releaseWriteLock();
            }
        }

        void mergeIndexes() throws IOException
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
                            if ((entry.getType() == IndexType.INDEX) && (entry.getStatus() == TransactionStatus.MERGE_TARGET))
                            {
                                return set;
                            }
                            if ((entry.getType() == IndexType.DELTA) && (entry.getStatus() == TransactionStatus.COMMITTED_DELETING))
                            {
                                return set;
                            }
                        }

                        ArrayList<IndexEntry> mergeList = new ArrayList<IndexEntry>();
                        for (IndexEntry entry : indexEntries.values())
                        {
                            if ((entry.getType() == IndexType.INDEX) && (entry.getStatus() == TransactionStatus.COMMITTED))
                            {
                                mergeList.add(entry);
                            }
                        }

                        int position = findMergeIndex(1, mergerMaxMergeDocs, mergerTargetIndexes, mergeList);
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
                            IndexEntry target = new IndexEntry(IndexType.INDEX, guid, "", TransactionStatus.MERGE_TARGET, guid, count, 0, false);
                            set.put(guid, target);
                            // rebuild merged index elements
                            LinkedHashMap<String, IndexEntry> reordered = new LinkedHashMap<String, IndexEntry>();
							invalidateMainReadersFromFirst(Collections.singleton(firstMergeId));
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

                    public boolean canRetry()
                    {
                        return false;
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

            if (toMerge.size() == 0)
            {
                return;
            }

            String mergeTargetId = null;

            long docCount = 0;

            if (toMerge.size() > 0)
            {
                int count = 0;
                IndexReader[] readers = new IndexReader[toMerge.size() - 1];
                RAMDirectory ramDirectory = null;
                IndexWriter writer = null;

                File outputLocation = null;
                double mergeSize = 0;
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
                        mergeSize += getSizeInMb(location);
                    }
                    else if (entry.getStatus() == TransactionStatus.MERGE_TARGET)
                    {
                        mergeTargetId = entry.getName();
                        outputLocation = location;
                        if ((docCount < maxDocsForInMemoryMerge) && (mergeSize < maxRamInMbForInMemoryMerge))
                        {
                            ramDirectory = new RAMDirectory();
                            writer = new IndexWriter(ramDirectory, new AlfrescoStandardAnalyser(), true, MaxFieldLength.UNLIMITED);
                        }
                        else
                        {
                            writer = new IndexWriter(location, new AlfrescoStandardAnalyser(), true, MaxFieldLength.UNLIMITED);

                        }
                        writer.setUseCompoundFile(mergerUseCompoundFile);
                        writer.setMaxBufferedDocs(mergerMaxBufferedDocs);
                        writer.setRAMBufferSizeMB(mergerRamBufferSizeMb);
                        writer.setMergeFactor(mergerMergeFactor);
                        writer.setMaxMergeDocs(mergerMaxMergeDocs);
                        writer.setWriteLockTimeout(writeLockTimeout);
                        writer.setMergeScheduler(new SerialMergeScheduler());
                        writer.setMergePolicy(new LogDocMergePolicy());
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

            final String finalMergeTargetId = mergeTargetId;
            IndexReader newReader = null;
            getReadLock();
            try
            {
                newReader = buildReferenceCountingIndexReader(mergeTargetId, docCount);
            }
            finally
            {
                releaseReadLock();
            }

            final IndexReader finalNewReader = newReader;

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
                                if (s_logger.isDebugEnabled())
                                {
                                    s_logger.debug("... deleting as merged " + entry.getName());
                                }
                                toDelete.add(entry.getName());
                            }
                            else if (entry.getStatus() == TransactionStatus.MERGE_TARGET)
                            {

                                if (s_logger.isDebugEnabled())
                                {
                                    s_logger.debug("... committing merge target " + entry.getName());
                                }
                                entry.setStatus(TransactionStatus.COMMITTED);

                            }
                        }
                        invalidateMainReadersFromFirst(toDelete);
                        for (String id : toDelete)
                        {
                            indexEntries.remove(id);
                        }

                        registerReferenceCountingIndexReader(finalMergeTargetId, finalNewReader);

                        notifyListeners("MergedIndexes", toMerge.size());

                        dumpInfo();

                        writeStatus();

                        clearOldReaders();

                        cleaner.schedule();

                        return null;
                    }

                    public boolean canRetry()
                    {
                        return false;
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

        private final int findMergeIndex(long min, long max, int target, List<IndexEntry> entries) throws IOException
        {
            // TODO: Support max
            if (entries.size() <= target)
            {
                return -1;
            }

            int total = 0;
            for (int i = target; i < entries.size(); i++)
            {
                total += entries.get(i).getDocumentCount();
            }

            for (int i = target - 1; i > 0; i--)
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
        readOnlyLock.readLock().lock();
        try
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
        finally
        {
            readOnlyLock.readLock().unlock();
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

    private boolean isGUID(String guid)
    {
        try
        {
            UUID id = new UUID(guid);
            // We have a valid guid.
            return true;
        }
        catch (NumberFormatException e)
        {
            // Not a valid GUID
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.lucene.index.IndexMonitor#getRelativePath()
     */
    public String getRelativePath()
    {
        return this.relativePath;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.lucene.index.IndexMonitor#getStatusSnapshot()
     */
    public Map<String, Integer> getStatusSnapshot()
    {
        Map<String, Integer> snapShot = new TreeMap<String, Integer>();
        readWriteLock.writeLock().lock();
        try
        {
            for (IndexEntry entry : indexEntries.values())
            {
                String stateKey = entry.getType() + "-" + entry.getStatus();
                Integer count = snapShot.get(stateKey);
                snapShot.put(stateKey, count == null ? 1 : count + 1);
            }
            return snapShot;
        }
        finally
        {
            readWriteLock.writeLock().unlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.lucene.index.IndexMonitor#getActualSize()
     */
    public long getActualSize() throws IOException
    {
        getReadLock();
        try
        {
            int size = 0;
            for (IndexEntry entry : this.indexEntries.values())
            {
                File location = new File(this.indexDirectory, entry.getName()).getCanonicalFile();
                File[] contents = location.listFiles();
                for (File file : contents)
                {
                    if (file.isFile())
                    {
                        size += file.length();
                    }
                }
            }
            return size;
        }
        finally
        {
            releaseReadLock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.lucene.index.IndexMonitor#getUsedSize()
     */
    public long getUsedSize() throws IOException
    {
        getReadLock();
        try
        {
            return sizeRecurse(this.indexDirectory);
        }
        finally
        {
            releaseReadLock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.lucene.index.IndexMonitor#getNumberOfDocuments()
     */
    public int getNumberOfDocuments() throws IOException
    {
        IndexReader reader = getMainIndexReferenceCountingReadOnlyIndexReader();
        try
        {
            return reader.numDocs();
        }
        finally
        {
            reader.close();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.lucene.index.IndexMonitor#getNumberOfFields()
     */
    public int getNumberOfFields() throws IOException
    {

        IndexReader reader = getMainIndexReferenceCountingReadOnlyIndexReader();
        try
        {
            return reader.getFieldNames(IndexReader.FieldOption.ALL).size();
        }
        finally
        {
            reader.close();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.lucene.index.IndexMonitor#getNumberOfIndexedFields()
     */
    public int getNumberOfIndexedFields() throws IOException
    {
        IndexReader reader = getMainIndexReferenceCountingReadOnlyIndexReader();
        try
        {
            return reader.getFieldNames(IndexReader.FieldOption.INDEXED).size();
        }
        finally
        {
            reader.close();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.lucene.index.IndexMonitor#addApplicationListener(org.springframework.context.
     *      ApplicationListener)
     */
    public void addApplicationListener(ApplicationListener listener)
    {
        this.applicationListeners.add(listener);
    }

    private long sizeRecurse(File fileOrDir)
    {
        long size = 0;
        if (fileOrDir.isDirectory())
        {
            File[] files = fileOrDir.listFiles();
            for (File file : files)
            {
                size += sizeRecurse(file);
            }
        }
        else
        {
            size = fileOrDir.length();
        }
        return size;
    }

    private void publishDiscoveryEvent()
    {
        if (this.config == null)
        {
            return;
        }
        final IndexEvent discoveryEvent = new IndexEvent(this, "Discovery", 1);
        final ConfigurableApplicationContext applicationContext = this.config.getApplicationContext();
        try
        {
            applicationContext.publishEvent(discoveryEvent);
        }
        catch (IllegalStateException e)
        {
            // There's a possibility that the application context hasn't fully refreshed yet, so register a listener
            // that will fire when it has
            applicationContext.addApplicationListener(new ApplicationListener()
            {

                public void onApplicationEvent(ApplicationEvent event)
                {
                    if (event instanceof ContextRefreshedEvent)
                    {
                        applicationContext.publishEvent(discoveryEvent);
                    }
                }
            });
        }
    }

    private void notifyListeners(String description, int count)
    {
        if (!this.applicationListeners.isEmpty())
        {
            IndexEvent event = new IndexEvent(this, description, count);
            for (ApplicationListener listener : this.applicationListeners)
            {
                listener.onApplicationEvent(event);
            }
        }
    }

    interface Schedulable
    {
        void schedule();
    }
}
