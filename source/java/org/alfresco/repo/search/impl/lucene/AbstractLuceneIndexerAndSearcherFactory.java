/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.search.impl.lucene;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.node.NodeBulkLoader;
import org.alfresco.repo.search.IndexerException;
import org.alfresco.repo.search.MLAnalysisMode;
import org.alfresco.repo.search.QueryRegisterComponent;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.search.impl.lucene.index.IndexInfo;
import org.alfresco.repo.search.transaction.SimpleTransaction;
import org.alfresco.repo.search.transaction.SimpleTransactionManager;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.store.Lock;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * This class is resource manager LuceneIndexers and LuceneSearchers. It supports two phase commit inside XA
 * transactions and outside transactions it provides thread local transaction support. TODO: Provide pluggable support
 * for a transaction manager TODO: Integrate with Spring transactions
 * 
 * @author andyh
 */

public abstract class AbstractLuceneIndexerAndSearcherFactory implements LuceneIndexerAndSearcher, XAResource, ApplicationContextAware
{
    private static Log logger = LogFactory.getLog(AbstractLuceneIndexerAndSearcherFactory.class);

    private int queryMaxClauses;

    private int indexerBatchSize;

    protected Map<String, LuceneQueryLanguageSPI> queryLanguages = new HashMap<String, LuceneQueryLanguageSPI>();

    /**
     * A map of active global transactions . It contains all the indexers a transaction has used, with at most one
     * indexer for each store within a transaction
     */

    private Map<Xid, Map<StoreRef, LuceneIndexer>> activeIndexersInGlobalTx = new HashMap<Xid, Map<StoreRef, LuceneIndexer>>();

    /**
     * Suspended global transactions.
     */
    private Map<Xid, Map<StoreRef, LuceneIndexer>> suspendedIndexersInGlobalTx = new HashMap<Xid, Map<StoreRef, LuceneIndexer>>();

    /**
     * The key under which this instance's map of indexers is stored in a (non-global) transaction
     */
    private final String indexersKey = "AbstractLuceneIndexerAndSearcherFactory." + GUID.generate();

    /**
     * The default timeout for transactions TODO: Respect this
     */

    private int timeout = DEFAULT_TIMEOUT;

    /**
     * Default time out value set to 10 minutes.
     */
    private static final int DEFAULT_TIMEOUT = 600000;

    protected TenantService tenantService;

    private String indexRootLocation;

    private QueryRegisterComponent queryRegister;

    /** the maximum transformation time to allow atomically, defaulting to 20ms */
    private long maxAtomicTransformationTime = 20;

    private int indexerMaxFieldLength = IndexWriter.DEFAULT_MAX_FIELD_LENGTH;

    private long writeLockTimeout;

    private long commitLockTimeout;

    private String lockDirectory;

    private MLAnalysisMode defaultMLIndexAnalysisMode = MLAnalysisMode.EXACT_LANGUAGE_AND_ALL;

    private MLAnalysisMode defaultMLSearchAnalysisMode = MLAnalysisMode.EXACT_LANGUAGE_AND_ALL;

    private ThreadPoolExecutor threadPoolExecutor;

    private NodeBulkLoader bulkLoader;

    private int maxDocIdCacheSize = 10000;

    private int maxDocsForInMemoryMerge = 10000;

    private int maxDocsForInMemoryIndex = 10000;

    private double maxRamInMbForInMemoryMerge = 16.0;

    private double maxRamInMbForInMemoryIndex = 16.0;

    private int maxDocumentCacheSize = 100;

    private int maxIsCategoryCacheSize = -1;

    private int maxLinkAspectCacheSize = 10000;

    private int maxParentCacheSize = 10000;

    private int maxPathCacheSize = 10000;

    private int maxTypeCacheSize = 10000;

    private int mergerMaxMergeDocs = 1000000;

    private int mergerMergeFactor = 5;

    private int mergerMergeBlockingFactor = 1;

    private int mergerMaxBufferedDocs = IndexWriter.DISABLE_AUTO_FLUSH;

    private double mergerRamBufferSizeMb = 16.0;

    private int mergerTargetIndexCount = 5;

    private int mergerTargetOverlayCount = 5;

    private int mergerTargetOverlaysBlockingFactor = 1;

    private int termIndexInterval = IndexWriter.DEFAULT_TERM_INDEX_INTERVAL;

    private boolean useNioMemoryMapping = true;

    private int writerMaxMergeDocs = 1000000;

    private int writerMergeFactor = 5;

    private int writerMaxBufferedDocs = IndexWriter.DISABLE_AUTO_FLUSH;

    private double writerRamBufferSizeMb = 16.0;

    private boolean cacheEnabled = true;

    private boolean postSortDateTime;

    private ConfigurableApplicationContext applicationContext;

    /**
     * Private constructor for the singleton TODO: FIt in with IOC
     */

    public AbstractLuceneIndexerAndSearcherFactory()
    {
        super();
    }

    /*
     * (non-Javadoc)
     * @seeorg.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.
     * ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.lucene.LuceneConfig#getApplicationContext()
     */
    public ConfigurableApplicationContext getApplicationContext()
    {
        return this.applicationContext;
    }

    /**
     * Set the directory that contains the indexes
     * 
     * @param indexRootLocation
     */

    public void setIndexRootLocation(String indexRootLocation)
    {
        this.indexRootLocation = indexRootLocation;
    }

    /**
     * Set the tenant service
     * 
     * @param tenantService
     */
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    /**
     * Set the query register
     * 
     * @param queryRegister
     */
    public void setQueryRegister(QueryRegisterComponent queryRegister)
    {
        this.queryRegister = queryRegister;
    }

    /**
     * Get the query register.
     * 
     * @return - the query register.
     */
    public QueryRegisterComponent getQueryRegister()
    {
        return queryRegister;
    }

    /**
     * Set the maximum average transformation time allowed to a transformer in order to have the transformation
     * performed in the current transaction. The default is 20ms.
     * 
     * @param maxAtomicTransformationTime
     *            the maximum average time that a text transformation may take in order to be performed atomically.
     */
    public void setMaxAtomicTransformationTime(long maxAtomicTransformationTime)
    {
        this.maxAtomicTransformationTime = maxAtomicTransformationTime;
    }

    /**
     * Get the max time for an atomic transform
     * 
     * @return - milliseconds as a long
     */
    public long getMaxTransformationTime()
    {
        return maxAtomicTransformationTime;
    }

    public NodeBulkLoader getBulkLoader()
    {
        return bulkLoader;
    }

    public void setBulkLoader(NodeBulkLoader bulkLoader)
    {
        this.bulkLoader = bulkLoader;
    }

    /**
     * Check if we are in a global transactoin according to the transaction manager
     * 
     * @return - true if in a global transaction
     */

    private boolean inGlobalTransaction()
    {
        try
        {
            return SimpleTransactionManager.getInstance().getTransaction() != null;
        }
        catch (SystemException e)
        {
            return false;
        }
    }

    /**
     * Get the local transaction - may be null oif we are outside a transaction.
     * 
     * @return - the transaction
     * @throws IndexerException
     */
    private SimpleTransaction getTransaction() throws IndexerException
    {
        try
        {
            return SimpleTransactionManager.getInstance().getTransaction();
        }
        catch (SystemException e)
        {
            throw new IndexerException("Failed to get transaction", e);
        }
    }

    /**
     * Get an indexer for the store to use in the current transaction for this thread of control.
     * 
     * @param storeRef -
     *            the id of the store
     */
    public LuceneIndexer getIndexer(StoreRef storeRef) throws IndexerException
    {
        storeRef = tenantService.getName(storeRef);

        // register to receive txn callbacks
        // TODO: make this conditional on whether the XA stuff is being used
        // directly on not
        AlfrescoTransactionSupport.bindLucene(this);

        if (inGlobalTransaction())
        {
            SimpleTransaction tx = getTransaction();
            // Only find indexers in the active list
            Map<StoreRef, LuceneIndexer> indexers = activeIndexersInGlobalTx.get(tx);
            if (indexers == null)
            {
                if (suspendedIndexersInGlobalTx.containsKey(tx))
                {
                    throw new IndexerException("Trying to obtain an index for a suspended transaction.");
                }
                indexers = new HashMap<StoreRef, LuceneIndexer>();
                activeIndexersInGlobalTx.put(tx, indexers);
                try
                {
                    tx.enlistResource(this);
                }
                // TODO: what to do in each case?
                catch (IllegalStateException e)
                {
                    throw new IndexerException("", e);
                }
                catch (RollbackException e)
                {
                    throw new IndexerException("", e);
                }
                catch (SystemException e)
                {
                    throw new IndexerException("", e);
                }
            }
            LuceneIndexer indexer = indexers.get(storeRef);
            if (indexer == null)
            {
                indexer = createIndexer(storeRef, getTransactionId(tx, storeRef));
                indexers.put(storeRef, indexer);
            }
            return indexer;
        }
        else
        // A thread local transaction
        {
            return getThreadLocalIndexer(storeRef);
        }

    }

    @SuppressWarnings("unchecked")
    private LuceneIndexer getThreadLocalIndexer(StoreRef storeRef)
    {
        Map<StoreRef, LuceneIndexer> indexers = (Map<StoreRef, LuceneIndexer>) AlfrescoTransactionSupport.getResource(indexersKey);
        if (indexers == null)
        {
            indexers = new HashMap<StoreRef, LuceneIndexer>();
            AlfrescoTransactionSupport.bindResource(indexersKey, indexers);
        }
        LuceneIndexer indexer = indexers.get(storeRef);
        if (indexer == null)
        {
            indexer = createIndexer(storeRef, GUID.generate());
            indexers.put(storeRef, indexer);
        }
        return indexer;
    }

    /**
     * Get the transaction identifier used to store it in the transaction map.
     * 
     * @param tx
     * @return - the transaction id
     */
    @SuppressWarnings("unchecked")
    private String getTransactionId(Transaction tx, StoreRef storeRef)
    {
        if (tx instanceof SimpleTransaction)
        {
            SimpleTransaction simpleTx = (SimpleTransaction) tx;
            return simpleTx.getGUID();
        }
        else if (TransactionSynchronizationManager.isSynchronizationActive())
        {
            Map<StoreRef, LuceneIndexer> indexers = (Map<StoreRef, LuceneIndexer>) AlfrescoTransactionSupport.getResource(indexersKey);
            if (indexers != null)
            {
                LuceneIndexer indexer = indexers.get(storeRef);
                if (indexer != null)
                {
                    return indexer.getDeltaId();
                }
            }
        }
        return null;
    }

    /**
     * Encapsulate creating an indexer
     * 
     * @param storeRef
     * @param deltaId
     * @return - the indexer made by the concrete implemntation
     */
    protected abstract LuceneIndexer createIndexer(StoreRef storeRef, String deltaId);

    /**
     * Encapsulate creating a searcher over the main index
     */
    public LuceneSearcher getSearcher(StoreRef storeRef, boolean searchDelta) throws SearcherException
    {
        storeRef = tenantService.getName(storeRef);

        String deltaId = null;
        LuceneIndexer indexer = null;
        if (searchDelta)
        {
            deltaId = getTransactionId(getTransaction(), storeRef);
            if (deltaId != null)
            {
                indexer = getIndexer(storeRef);
            }
        }
        LuceneSearcher searcher = getSearcher(storeRef, indexer);
        return searcher;
    }

    /**
     * Get node-based searcher (for "selectNodes / selectProperties")
     */
    protected abstract SearchService getNodeSearcher() throws SearcherException;

    /**
     * Get a searcher over the index and the current delta
     * 
     * @param storeRef
     * @param deltaId
     * @return - the searcher made by the concrete implementation.
     * @throws SearcherException
     */

    protected abstract LuceneSearcher getSearcher(StoreRef storeRef, LuceneIndexer indexer) throws SearcherException;

    /*
     * XAResource implementation
     */

    public void commit(Xid xid, boolean onePhase) throws XAException
    {
        try
        {
            // TODO: Should be remembering overall state
            // TODO: Keep track of prepare responses
            Map<StoreRef, LuceneIndexer> indexers = activeIndexersInGlobalTx.get(xid);
            if (indexers == null)
            {
                if (suspendedIndexersInGlobalTx.containsKey(xid))
                {
                    throw new XAException("Trying to commit indexes for a suspended transaction.");
                }
                else
                {
                    // nothing to do
                    return;
                }
            }

            if (onePhase)
            {
                if (indexers.size() == 0)
                {
                    return;
                }
                else if (indexers.size() == 1)
                {
                    for (LuceneIndexer indexer : indexers.values())
                    {
                        indexer.commit();
                    }
                    return;
                }
                else
                {
                    throw new XAException("Trying to do one phase commit on more than one index");
                }
            }
            else
            // two phase
            {
                for (LuceneIndexer indexer : indexers.values())
                {
                    indexer.commit();
                }
                return;
            }
        }
        finally
        {
            activeIndexersInGlobalTx.remove(xid);
        }
    }

    public void end(Xid xid, int flag) throws XAException
    {
        Map<StoreRef, LuceneIndexer> indexers = activeIndexersInGlobalTx.get(xid);
        if (indexers == null)
        {
            if (suspendedIndexersInGlobalTx.containsKey(xid))
            {
                throw new XAException("Trying to commit indexes for a suspended transaction.");
            }
            else
            {
                // nothing to do
                return;
            }
        }
        if (flag == XAResource.TMSUSPEND)
        {
            activeIndexersInGlobalTx.remove(xid);
            suspendedIndexersInGlobalTx.put(xid, indexers);
        }
        else if (flag == TMFAIL)
        {
            activeIndexersInGlobalTx.remove(xid);
            suspendedIndexersInGlobalTx.remove(xid);
        }
        else if (flag == TMSUCCESS)
        {
            activeIndexersInGlobalTx.remove(xid);
        }
    }

    public void forget(Xid xid) throws XAException
    {
        activeIndexersInGlobalTx.remove(xid);
        suspendedIndexersInGlobalTx.remove(xid);
    }

    public int getTransactionTimeout() throws XAException
    {
        return timeout;
    }

    public boolean isSameRM(XAResource xar) throws XAException
    {
        return (xar instanceof AbstractLuceneIndexerAndSearcherFactory);
    }

    public int prepare(Xid xid) throws XAException
    {
        // TODO: Track state OK, ReadOnly, Exception (=> rolled back?)
        Map<StoreRef, LuceneIndexer> indexers = activeIndexersInGlobalTx.get(xid);
        if (indexers == null)
        {
            if (suspendedIndexersInGlobalTx.containsKey(xid))
            {
                throw new XAException("Trying to commit indexes for a suspended transaction.");
            }
            else
            {
                // nothing to do
                return XAResource.XA_OK;
            }
        }
        boolean isPrepared = true;
        boolean isModified = false;
        for (LuceneIndexer indexer : indexers.values())
        {
            try
            {
                isModified |= indexer.isModified();
                indexer.prepare();
            }
            catch (IndexerException e)
            {
                isPrepared = false;
            }
        }
        if (isPrepared)
        {
            if (isModified)
            {
                return XAResource.XA_OK;
            }
            else
            {
                return XAResource.XA_RDONLY;
            }
        }
        else
        {
            throw new XAException("Failed to prepare: requires rollback");
        }
    }

    public Xid[] recover(int arg0) throws XAException
    {
        // We can not rely on being able to recover at the moment
        // Avoiding for performance benefits at the moment
        // Assume roll back and no recovery - in the worst case we get an unused
        // delta
        // This should be there to avoid recovery of partial commits.
        // It is difficult to see how we can mandate the same conditions.
        return new Xid[0];
    }

    public void rollback(Xid xid) throws XAException
    {
        // TODO: What to do if all do not roll back?
        try
        {
            Map<StoreRef, LuceneIndexer> indexers = activeIndexersInGlobalTx.get(xid);
            if (indexers == null)
            {
                if (suspendedIndexersInGlobalTx.containsKey(xid))
                {
                    throw new XAException("Trying to commit indexes for a suspended transaction.");
                }
                else
                {
                    // nothing to do
                    return;
                }
            }
            for (LuceneIndexer indexer : indexers.values())
            {
                indexer.rollback();
            }
        }
        finally
        {
            activeIndexersInGlobalTx.remove(xid);
        }
    }

    public boolean setTransactionTimeout(int timeout) throws XAException
    {
        this.timeout = timeout;
        return true;
    }

    public void start(Xid xid, int flag) throws XAException
    {
        Map<StoreRef, LuceneIndexer> active = activeIndexersInGlobalTx.get(xid);
        Map<StoreRef, LuceneIndexer> suspended = suspendedIndexersInGlobalTx.get(xid);
        if (flag == XAResource.TMJOIN)
        {
            // must be active
            if ((active != null) && (suspended == null))
            {
                return;
            }
            else
            {
                throw new XAException("Trying to rejoin transaction in an invalid state");
            }

        }
        else if (flag == XAResource.TMRESUME)
        {
            // must be suspended
            if ((active == null) && (suspended != null))
            {
                suspendedIndexersInGlobalTx.remove(xid);
                activeIndexersInGlobalTx.put(xid, suspended);
                return;
            }
            else
            {
                throw new XAException("Trying to rejoin transaction in an invalid state");
            }

        }
        else if (flag == XAResource.TMNOFLAGS)
        {
            if ((active == null) && (suspended == null))
            {
                return;
            }
            else
            {
                throw new XAException("Trying to start an existing or suspended transaction");
            }
        }
        else
        {
            throw new XAException("Unkown flags for start " + flag);
        }

    }

    /*
     * Thread local support for transactions
     */

    /**
     * Commit the transaction
     */

    @SuppressWarnings("unchecked")
    public void commit() throws IndexerException
    {
        Map<StoreRef, LuceneIndexer> indexers = null;
        try
        {
            indexers = (Map<StoreRef, LuceneIndexer>) AlfrescoTransactionSupport.getResource(indexersKey);
            if (indexers != null)
            {
                for (LuceneIndexer indexer : indexers.values())
                {
                    try
                    {
                        indexer.commit();
                    }
                    catch (IndexerException e)
                    {
                        rollback();
                        throw e;
                    }
                }
            }
        }
        finally
        {
            if (indexers != null)
            {
                indexers.clear();
                AlfrescoTransactionSupport.unbindResource(indexersKey);
            }
        }
    }

    /**
     * Prepare the transaction TODO: Store prepare results
     * 
     * @return - the tx code
     */
    @SuppressWarnings("unchecked")
    public int prepare() throws IndexerException
    {
        boolean isPrepared = true;
        boolean isModified = false;
        Map<StoreRef, LuceneIndexer> indexers = (Map<StoreRef, LuceneIndexer>) AlfrescoTransactionSupport.getResource(indexersKey);
        if (indexers != null)
        {
            for (LuceneIndexer indexer : indexers.values())
            {
                try
                {
                    isModified |= indexer.isModified();
                    indexer.prepare();
                }
                catch (IndexerException e)
                {
                    isPrepared = false;
                    throw new IndexerException("Failed to prepare: requires rollback", e);
                }
            }
        }
        if (isPrepared)
        {
            if (isModified)
            {
                return XAResource.XA_OK;
            }
            else
            {
                return XAResource.XA_RDONLY;
            }
        }
        else
        {
            throw new IndexerException("Failed to prepare: requires rollback");
        }
    }

    /**
     * Roll back the transaction
     */
    @SuppressWarnings("unchecked")
    public void rollback()
    {
        Map<StoreRef, LuceneIndexer> indexers = (Map<StoreRef, LuceneIndexer>) AlfrescoTransactionSupport.getResource(indexersKey);

        if (indexers != null)
        {
            for (LuceneIndexer indexer : indexers.values())
            {
                try
                {
                    indexer.rollback();
                }
                catch (IndexerException e)
                {

                }
            }
            indexers.clear();
            AlfrescoTransactionSupport.unbindResource(indexersKey);
        }
    }

    @SuppressWarnings("unchecked")
    public void flush()
    {
        // TODO: Needs fixing if we expose the indexer in JTA
        Map<StoreRef, LuceneIndexer> indexers = (Map<StoreRef, LuceneIndexer>) AlfrescoTransactionSupport.getResource(indexersKey);

        if (indexers != null)
        {
            for (LuceneIndexer indexer : indexers.values())
            {
                indexer.flushPending();
            }
        }
    }

    public String getIndexRootLocation()
    {
        return indexRootLocation;
    }

    public int getIndexerBatchSize()
    {
        return indexerBatchSize;
    }

    /**
     * Set the batch six to use for background indexing
     * 
     * @param indexerBatchSize
     */
    public void setIndexerBatchSize(int indexerBatchSize)
    {
        this.indexerBatchSize = indexerBatchSize;
    }

    /**
     * Get the directory where any lock files are written (by default there are none)
     * 
     * @return - the path to the directory
     */
    public String getLockDirectory()
    {
        return lockDirectory;
    }

    public void setLockDirectory(String lockDirectory)
    {
        this.lockDirectory = lockDirectory;
        // Set the lucene lock file via System property
        // org.apache.lucene.lockDir
        System.setProperty("org.apache.lucene.lockDir", lockDirectory);
        // Make sure the lock directory exists
        File lockDir = new File(lockDirectory);
        if (!lockDir.exists())
        {
            lockDir.mkdirs();
        }
        // clean out any existing locks when we start up

        File[] children = lockDir.listFiles();
        if (children != null)
        {
            for (int i = 0; i < children.length; i++)
            {
                File child = children[i];
                if (child.isFile())
                {
                    if (child.exists() && !child.delete() && child.exists())
                    {
                        throw new IllegalStateException("Failed to delete " + child);
                    }
                }
            }
        }
    }

    public int getQueryMaxClauses()
    {
        return queryMaxClauses;
    }

    /**
     * Set the max number of queries in a llucen boolean query
     * 
     * @param queryMaxClauses
     */
    public void setQueryMaxClauses(int queryMaxClauses)
    {
        this.queryMaxClauses = queryMaxClauses;
        BooleanQuery.setMaxClauseCount(this.queryMaxClauses);
    }

    /**
     * Set the lucene write lock timeout
     * 
     * @param timeout
     */
    public void setWriteLockTimeout(long timeout)
    {
        this.writeLockTimeout = timeout;
    }

    /**
     * Set the lucene commit lock timeout (no longer used with lucene 2.1)
     * 
     * @param timeout
     */
    public void setCommitLockTimeout(long timeout)
    {
        this.commitLockTimeout = timeout;
    }

    /**
     * Get the commit lock timout.
     * 
     * @return - the timeout
     */
    public long getCommitLockTimeout()
    {
        return commitLockTimeout;
    }

    /**
     * Get the write lock timeout
     * 
     * @return - the timeout in ms
     */
    public long getWriteLockTimeout()
    {
        return writeLockTimeout;
    }

    /**
     * Set the lock poll interval in ms
     * 
     * @param time
     */
    public void setLockPollInterval(long time)
    {
        Lock.LOCK_POLL_INTERVAL = time;
    }

    /**
     * Get the max number of tokens in the field
     * 
     * @return - the max tokens considered.
     */
    public int getIndexerMaxFieldLength()
    {
        return indexerMaxFieldLength;
    }

    /**
     * Set the max field length.
     * 
     * @param indexerMaxFieldLength
     */
    public void setIndexerMaxFieldLength(int indexerMaxFieldLength)
    {
        this.indexerMaxFieldLength = indexerMaxFieldLength;
    }

    public ThreadPoolExecutor getThreadPoolExecutor()
    {
        return this.threadPoolExecutor;
    }

    public void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor)
    {
        this.threadPoolExecutor = threadPoolExecutor;
    }

    /**
     * This component is able to <i>safely</i> perform backups of the Lucene indexes while the server is running.
     * <p>
     * It can be run directly by calling the {@link #backup() } method, but the convenience {@link LuceneIndexBackupJob}
     * can be used to call it as well.
     * 
     * @author Derek Hulley
     */
    public static class LuceneIndexBackupComponent /* implements InitializingBean */
    {
        ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

        boolean executing = false;

        private static String BACKUP_TEMP_NAME = ".indexbackup_temp";

        private TransactionService transactionService;

        private Set<LuceneIndexerAndSearcher> factories;

        private NodeService nodeService;

        private String targetLocation;

        private boolean checkConfiguration = true;

        /**
         * Default constructor
         */
        public LuceneIndexBackupComponent()
        {
        }

        /**
         * If false do not check the index configuration.
         * 
         * @param checkConfiguration
         */
        public void setCheckConfiguration(boolean checkConfiguration)
        {
            this.checkConfiguration = checkConfiguration;
        }

        /**
         * Provides transactions in which to perform the work
         * 
         * @param transactionService
         */
        public void setTransactionService(TransactionService transactionService)
        {
            this.transactionService = transactionService;
        }

        /**
         * Set the Lucene index factory that will be used to control the index locks
         * 
         * @param factories
         *            the index factories
         */
        public void setFactories(Set<LuceneIndexerAndSearcher> factories)
        {
            this.factories = factories;
        }

        /**
         * Used to retrieve the stores
         * 
         * @param nodeService
         *            the node service
         */
        public void setNodeService(NodeService nodeService)
        {
            this.nodeService = nodeService;
        }

        /**
         * Set the directory to which the backup will be copied
         * 
         * @param targetLocation
         *            the backup directory
         */
        public void setTargetLocation(String targetLocation)
        {
            this.targetLocation = targetLocation;
        }

        /**
         * Backup the Lucene indexes
         */
        public void backup()
        {
            rwLock.readLock().lock();
            try
            {
                if (executing)
                {
                    return;
                }
            }
            finally
            {
                rwLock.readLock().unlock();
            }

            rwLock.writeLock().lock();
            try
            {
                if (executing)
                {
                    return;
                }
                executing = true;
            }
            finally
            {
                rwLock.writeLock().unlock();
            }

            try
            {
                RetryingTransactionCallback<Object> backupWork = new RetryingTransactionCallback<Object>()
                {
                    public Object execute() throws Exception
                    {
                        backupImpl();
                        return null;
                    }
                };
                transactionService.getRetryingTransactionHelper().doInTransaction(backupWork);
            }
            finally
            {
                rwLock.writeLock().lock();
                try
                {
                    executing = false;
                }
                finally
                {
                    rwLock.writeLock().unlock();
                }
            }
        }

        private void backupImpl()
        {
            // create the location to copy to
            File targetDir = new File(targetLocation);
            if (targetDir.exists() && !targetDir.isDirectory())
            {
                throw new AlfrescoRuntimeException("Target location is a file and not a directory: " + targetDir);
            }
            File targetParentDir = targetDir.getParentFile();
            if (targetParentDir == null)
            {
                throw new AlfrescoRuntimeException("Target location may not be a root directory: " + targetDir);
            }
            File tempDir = new File(targetParentDir, BACKUP_TEMP_NAME);

            for (LuceneIndexerAndSearcher factory : factories)
            {
                ReadOnlyWork<Object> backupWork = new BackUpReadOnlyWork(factory, tempDir, targetDir);
                
                if (logger.isDebugEnabled())
                {
                    logger.debug("Backing up Lucene indexes: \n" + "   Target directory: " + targetDir);
                }
               
                factory.doReadOnly(backupWork);

                if (logger.isDebugEnabled())
                {
                    logger.debug("Backed up Lucene indexes: \n" + "   Target directory: " + targetDir);
                }
            }
        }

        static class BackUpReadOnlyWork implements ReadOnlyWork<Object>
        {
            LuceneIndexerAndSearcher factory;

            File tempDir;

            File targetDir;

            BackUpReadOnlyWork(LuceneIndexerAndSearcher factory, File tempDir, File targetDir)
            {
                this.factory = factory;
                this.tempDir = tempDir;
                this.targetDir = targetDir;
            }

            public Object doWork()
            {
                try
                {
                    File indexRootDir = new File(factory.getIndexRootLocation());
                    // perform the copy
                    backupDirectory(indexRootDir, tempDir, targetDir);
                    return null;
                }
                catch (Throwable e)
                {
                    throw new AlfrescoRuntimeException("Failed to copy Lucene index root: \n"
                            + "   Index root: " + factory.getIndexRootLocation() + "\n" + "   Target: " + targetDir, e);
                }
            }

            /**
             * Makes a backup of the source directory via a temporary folder.
             */
            private void backupDirectory(File sourceDir, File tempDir, File targetDir) throws Exception
            {
                if (!sourceDir.exists())
                {
                    // there is nothing to copy
                    return;
                }
                // delete the files from the temp directory
                if (tempDir.exists())
                {
                    deleteDirectory(tempDir);
                    if (tempDir.exists())
                    {
                        throw new AlfrescoRuntimeException("Temp directory exists and cannot be deleted: " + tempDir);
                    }
                }
                // copy to the temp directory
                copyDirectory(sourceDir, tempDir, true);
                // check that the temp directory was created
                if (!tempDir.exists())
                {
                    throw new AlfrescoRuntimeException("Copy to temp location failed");
                }
                // delete the target directory
                deleteDirectory(targetDir);
                if (targetDir.exists())
                {
                    throw new AlfrescoRuntimeException("Failed to delete older files from target location");
                }
                // rename the temp to be the target
                tempDir.renameTo(targetDir);
                // make sure the rename worked
                if (!targetDir.exists())
                {
                    throw new AlfrescoRuntimeException("Failed to rename temporary directory to target backup directory");
                }
            }

            /**
             * Note files can alter due to background processes so file not found is Ok
             * 
             * @param srcDir
             * @param destDir
             * @param preserveFileDate
             * @throws IOException
             */
            private void copyDirectory(File srcDir, File destDir, boolean preserveFileDate) throws IOException
            {
                if (destDir.exists())
                {
                    throw new IOException("Destination should be created from clean");
                }
                else
                {
                    if (!destDir.mkdirs())
                    {
                        throw new IOException("Destination '" + destDir + "' directory cannot be created");
                    }
                    if (preserveFileDate)
                    {
                        // OL if file not found so does not need to check
                        destDir.setLastModified(srcDir.lastModified());
                    }
                }
                if (!destDir.canWrite())
                {
                    throw new IOException("No acces to destination directory" + destDir);
                }

                File[] files = srcDir.listFiles();
                if (files != null)
                {
                    for (int i = 0; i < files.length; i++)
                    {
                        File currentCopyTarget = new File(destDir, files[i].getName());
                        if (files[i].isDirectory())
                        {
                            // Skip any temp index file
                            if (files[i].getName().equals(tempDir.getName()))
                            {
                                // skip any temp back up directories
                            }
                            else if (files[i].getName().equals(targetDir.getName()))
                            {
                                // skip any back up directories
                            }
                            else
                            {
                                copyDirectory(files[i], currentCopyTarget, preserveFileDate);
                            }
                        }
                        else
                        {
                            copyFile(files[i], currentCopyTarget, preserveFileDate);
                        }
                    }
                }
                else
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Skipping transient directory " + srcDir);
                    }
                }
            }

            private void copyFile(File srcFile, File destFile, boolean preserveFileDate) throws IOException
            {
                try
                {
                    if (destFile.exists())
                    {
                        throw new IOException("File shoud not exist " + destFile);
                    }

                    FileInputStream input = new FileInputStream(srcFile);
                    try
                    {
                        FileOutputStream output = new FileOutputStream(destFile);
                        try
                        {
                            copy(input, output);
                        }
                        finally
                        {
                            try
                            {
                                output.close();
                            }
                            catch (IOException io)
                            {

                            }
                        }
                    }
                    finally
                    {
                        try
                        {
                            input.close();
                        }
                        catch (IOException io)
                        {

                        }
                    }

                    // check copy
                    if (srcFile.length() != destFile.length())
                    {
                        throw new IOException("Failed to copy full from '" + srcFile + "' to '" + destFile + "'");
                    }
                    if (preserveFileDate)
                    {
                        destFile.setLastModified(srcFile.lastModified());
                    }
                }
                catch (FileNotFoundException fnfe)
                {
                    // ignore as files can go
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Skipping transient file " + srcFile);
                    }
                }
            }

            public int copy(InputStream input, OutputStream output) throws IOException
            {
                byte[] buffer = new byte[2048 * 4];
                int count = 0;
                int n = 0;
                while ((n = input.read(buffer)) != -1)
                {
                    output.write(buffer, 0, n);
                    count += n;
                }
                return count;
            }

            public void deleteDirectory(File directory) throws IOException
            {
                if (!directory.exists())
                {
                    return;
                }
                if (!directory.isDirectory())
                {
                    throw new IllegalArgumentException("Not a directory " + directory);
                }

                File[] files = directory.listFiles();
                if (files == null)
                {
                    throw new IOException("Failed to delete director - no access" + directory);
                }

                for (int i = 0; i < files.length; i++)
                {
                    File file = files[i];

                    if (file.isDirectory())
                    {
                        deleteDirectory(file);
                    }
                    else
                    {
                        if (!file.delete())
                        {
                            throw new IOException("Unable to delete file: " + file);
                        }
                    }
                }

                if (!directory.delete())
                {
                    throw new IOException("Unable to delete directory " + directory);
                }
            }

        }

        public void afterPropertiesSetXXX() throws Exception
        {
            RetryingTransactionCallback<Object> backupWork = new RetryingTransactionCallback<Object>()
            {
                public Object execute() throws Exception
                {
                    File targetDir = new File(targetLocation).getCanonicalFile();

                    List<StoreRef> stores;
                    try
                    {
                        stores = nodeService.getStores();
                    }
                    catch (Exception e)
                    {
                        return null;
                    }
                    Set<String> protocols = new HashSet<String>();
                    protocols.add(StoreRef.PROTOCOL_AVM);
                    protocols.add(StoreRef.PROTOCOL_ARCHIVE);
                    protocols.add(StoreRef.PROTOCOL_WORKSPACE);
                    protocols.add("locks");
                    for (StoreRef store : stores)
                    {
                        protocols.add(store.getProtocol());
                    }

                    for (LuceneIndexerAndSearcher factory : factories)
                    {
                        File indexRootDir = new File(factory.getIndexRootLocation()).getCanonicalFile();

                        if (indexRootDir.getCanonicalPath().startsWith(targetDir.getCanonicalPath()))
                        {
                            throw new IllegalArgumentException("Backup directory can not contain or be an index directory");
                        }
                        if (targetDir.getCanonicalPath().startsWith(indexRootDir.getCanonicalPath()))
                        {
                            for (String name : protocols)
                            {
                                File test = new File(indexRootDir, name);
                                if (targetDir.getCanonicalPath().startsWith(test.getCanonicalPath()))
                                {
                                    throw new IllegalArgumentException("Backup directory can not be in index directory and match a store protocol name " + targetDir);
                                }
                            }
                        }
                        // if the back up directory exists make sure it only contains directories that are store
                        // protocols

                        if (targetDir.exists())
                        {
                            for (File file : targetDir.listFiles())
                            {
                                if (file.isFile())
                                {
                                    throw new IllegalArgumentException("Existing index backup does not look like the expected structure. It constains a file "
                                            + file.getCanonicalPath());
                                }
                                if (!protocols.contains(file.getName()))
                                {
                                    throw new IllegalArgumentException(
                                            "Existing index backup does not look like the expected structure. It constains a directory with a name that does not match a store protocol "
                                                    + file.getCanonicalPath());

                                }
                            }
                        }

                    }
                    return null;
                }
            };

            if (checkConfiguration)
            {
                transactionService.getRetryingTransactionHelper().doInTransaction(backupWork, true);
            }

        }
    }

    /**
     * Job that lock uses the {@link LuceneIndexBackupComponent} to perform safe backups of the Lucene indexes.
     * 
     * @author Derek Hulley
     */
    public static class LuceneIndexBackupJob implements Job
    {

        /** KEY_LUCENE_INDEX_BACKUP_COMPONENT = 'luceneIndexBackupComponent' */
        public static final String KEY_LUCENE_INDEX_BACKUP_COMPONENT = "luceneIndexBackupComponent";

        /**
         * Locks the Lucene indexes and copies them to a backup location
         */
        public void execute(JobExecutionContext context) throws JobExecutionException
        {
            JobDataMap jobData = context.getJobDetail().getJobDataMap();
            LuceneIndexBackupComponent backupComponent = (LuceneIndexBackupComponent) jobData.get(KEY_LUCENE_INDEX_BACKUP_COMPONENT);
            if (backupComponent == null)
            {
                throw new JobExecutionException("Missing job data: " + KEY_LUCENE_INDEX_BACKUP_COMPONENT);
            }
            // perform the backup
            backupComponent.backup();
        }
    }

    public MLAnalysisMode getDefaultMLIndexAnalysisMode()
    {
        return defaultMLIndexAnalysisMode;
    }

    /**
     * Set the ML analysis mode at index time.
     * 
     * @param mode
     */
    public void setDefaultMLIndexAnalysisMode(MLAnalysisMode mode)
    {
        // defaultMLIndexAnalysisMode = MLAnalysisMode.getMLAnalysisMode(mode);
        defaultMLIndexAnalysisMode = mode;
    }

    public MLAnalysisMode getDefaultMLSearchAnalysisMode()
    {
        return defaultMLSearchAnalysisMode;
    }

    /**
     * Set the ML analysis mode at search time
     * 
     * @param mode
     */
    public void setDefaultMLSearchAnalysisMode(MLAnalysisMode mode)
    {
        // defaultMLSearchAnalysisMode = MLAnalysisMode.getMLAnalysisMode(mode);
        defaultMLSearchAnalysisMode = mode;
    }

    public int getMaxDocIdCacheSize()
    {
        return maxDocIdCacheSize;
    }

    public void setMaxDocIdCacheSize(int maxDocIdCacheSize)
    {
        this.maxDocIdCacheSize = maxDocIdCacheSize;
    }

    public int getMaxDocsForInMemoryMerge()
    {
        return maxDocsForInMemoryMerge;
    }

    public void setMaxDocsForInMemoryMerge(int maxDocsForInMemoryMerge)
    {
        this.maxDocsForInMemoryMerge = maxDocsForInMemoryMerge;
    }

    public int getMaxDocumentCacheSize()
    {
        return maxDocumentCacheSize;
    }

    public void setMaxDocumentCacheSize(int maxDocumentCacheSize)
    {
        this.maxDocumentCacheSize = maxDocumentCacheSize;
    }

    public int getMaxIsCategoryCacheSize()
    {
        return maxIsCategoryCacheSize;
    }

    public void setMaxIsCategoryCacheSize(int maxIsCategoryCacheSize)
    {
        this.maxIsCategoryCacheSize = maxIsCategoryCacheSize;
    }

    public int getMaxLinkAspectCacheSize()
    {
        return maxLinkAspectCacheSize;
    }

    public void setMaxLinkAspectCacheSize(int maxLinkAspectCacheSize)
    {
        this.maxLinkAspectCacheSize = maxLinkAspectCacheSize;
    }

    public int getMaxParentCacheSize()
    {
        return maxParentCacheSize;
    }

    public void setMaxParentCacheSize(int maxParentCacheSize)
    {
        this.maxParentCacheSize = maxParentCacheSize;
    }

    public int getMaxPathCacheSize()
    {
        return maxPathCacheSize;
    }

    public void setMaxPathCacheSize(int maxPathCacheSize)
    {
        this.maxPathCacheSize = maxPathCacheSize;
    }

    public int getMaxTypeCacheSize()
    {
        return maxTypeCacheSize;
    }

    public void setMaxTypeCacheSize(int maxTypeCacheSize)
    {
        this.maxTypeCacheSize = maxTypeCacheSize;
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

    public int getMergerMergeBlockingFactor()
    {
        return mergerMergeBlockingFactor;
    }

    public void setMergerMergeBlockingFactor(int mergerMergeBlockingFactor)
    {
        this.mergerMergeBlockingFactor = mergerMergeBlockingFactor;
    }

    public int getMergerMaxBufferedDocs()
    {
        return mergerMaxBufferedDocs;
    }

    public void setMergerMaxBufferedDocs(int mergerMaxBufferedDocs)
    {
        this.mergerMaxBufferedDocs = mergerMaxBufferedDocs;
    }

    public int getMergerTargetIndexCount()
    {
        return mergerTargetIndexCount;
    }

    public void setMergerTargetIndexCount(int mergerTargetIndexCount)
    {
        this.mergerTargetIndexCount = mergerTargetIndexCount;
    }

    public int getMergerTargetOverlayCount()
    {
        return mergerTargetOverlayCount;
    }

    public void setMergerTargetOverlayCount(int mergerTargetOverlayCount)
    {
        this.mergerTargetOverlayCount = mergerTargetOverlayCount;
    }

    public int getMergerTargetOverlaysBlockingFactor()
    {
        return mergerTargetOverlaysBlockingFactor;
    }

    public void setMergerTargetOverlaysBlockingFactor(int mergerTargetOverlaysBlockingFactor)
    {
        this.mergerTargetOverlaysBlockingFactor = mergerTargetOverlaysBlockingFactor;
    }

    public int getTermIndexInterval()
    {
        return termIndexInterval;
    }

    public void setTermIndexInterval(int termIndexInterval)
    {
        this.termIndexInterval = termIndexInterval;
    }

    public boolean getUseNioMemoryMapping()
    {
        return useNioMemoryMapping;
    }

    public void setUseNioMemoryMapping(boolean useNioMemoryMapping)
    {
        this.useNioMemoryMapping = useNioMemoryMapping;
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

    public int getWriterMaxBufferedDocs()
    {
        return writerMaxBufferedDocs;
    }

    public void setWriterMaxBufferedDocs(int writerMaxBufferedDocs)
    {
        this.writerMaxBufferedDocs = writerMaxBufferedDocs;
    }

    public boolean isCacheEnabled()
    {
        return cacheEnabled;
    }

    public void setCacheEnabled(boolean cacheEnabled)
    {
        this.cacheEnabled = cacheEnabled;
    }

    public boolean getPostSortDateTime()
    {
        return postSortDateTime;
    }

    public void setPostSortDateTime(boolean postSortDateTime)
    {
        this.postSortDateTime = postSortDateTime;
    }

    public void registerQueryLanguage(LuceneQueryLanguageSPI queryLanguage)
    {
        this.queryLanguages.put(queryLanguage.getName().toLowerCase(), queryLanguage);
    }

    /**
     * @return the maxDocsForInMemoryIndex
     */
    public int getMaxDocsForInMemoryIndex()
    {
        return maxDocsForInMemoryIndex;
    }

    /**
     * @param maxDocsForInMemoryIndex
     *            the maxDocsForInMemoryIndex to set
     */
    public void setMaxDocsForInMemoryIndex(int maxDocsForInMemoryIndex)
    {
        this.maxDocsForInMemoryIndex = maxDocsForInMemoryIndex;
    }

    /**
     * @return the maxRamInMbForInMemoryMerge
     */
    public double getMaxRamInMbForInMemoryMerge()
    {
        return maxRamInMbForInMemoryMerge;
    }

    /**
     * @param maxRamInMbForInMemoryMerge
     *            the maxRamInMbForInMemoryMerge to set
     */
    public void setMaxRamInMbForInMemoryMerge(double maxRamInMbForInMemoryMerge)
    {
        this.maxRamInMbForInMemoryMerge = maxRamInMbForInMemoryMerge;
    }

    /**
     * @return the maxRamInMbForInMemoryIndex
     */
    public double getMaxRamInMbForInMemoryIndex()
    {
        return maxRamInMbForInMemoryIndex;
    }

    /**
     * @param maxRamInMbForInMemoryIndex
     *            the maxRamInMbForInMemoryIndex to set
     */
    public void setMaxRamInMbForInMemoryIndex(double maxRamInMbForInMemoryIndex)
    {
        this.maxRamInMbForInMemoryIndex = maxRamInMbForInMemoryIndex;
    }

    /**
     * @return the mergerRamBufferSizeMb
     */
    public double getMergerRamBufferSizeMb()
    {
        return mergerRamBufferSizeMb;
    }

    /**
     * @param mergerRamBufferSizeMb
     *            the mergerRamBufferSizeMb to set
     */
    public void setMergerRamBufferSizeMb(double mergerRamBufferSizeMb)
    {
        this.mergerRamBufferSizeMb = mergerRamBufferSizeMb;
    }

    /**
     * @return the writerRamBufferSizeMb
     */
    public double getWriterRamBufferSizeMb()
    {
        return writerRamBufferSizeMb;
    }

    /**
     * @param writerRamBufferSizeMb
     *            the writerRamBufferSizeMb to set
     */
    public void setWriterRamBufferSizeMb(double writerRamBufferSizeMb)
    {
        this.writerRamBufferSizeMb = writerRamBufferSizeMb;
    }

    protected LuceneQueryLanguageSPI getQueryLanguage(String name)
    {
        return this.queryLanguages.get(name);
    }

    protected abstract List<StoreRef> getAllStores();

    public <R> R doReadOnly(ReadOnlyWork<R> lockWork)
    {
        // get all the available stores
        List<StoreRef> storeRefs = getAllStores();

        IndexInfo.LockWork<R> currentLockWork = null;

        for (int i = storeRefs.size() - 1; i >= 0; i--)
        {
            if (currentLockWork == null)
            {
                currentLockWork = new CoreReadOnlyWork<R>(getIndexer(storeRefs.get(i)), lockWork);
            }
            else
            {
                currentLockWork = new NestingReadOnlyWork<R>(getIndexer(storeRefs.get(i)), currentLockWork);
            }
        }

        if (currentLockWork != null)
        {
            try
            {
                return currentLockWork.doWork();
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

        }
        else
        {
            return null;
        }
    }

    private static class NestingReadOnlyWork<R> implements IndexInfo.LockWork<R>
    {
        IndexInfo.LockWork<R> lockWork;

        LuceneIndexer indexer;

        NestingReadOnlyWork(LuceneIndexer indexer, IndexInfo.LockWork<R> lockWork)
        {
            this.indexer = indexer;
            this.lockWork = lockWork;
        }

        public R doWork() throws Exception
        {
            return indexer.doReadOnly(lockWork);
        }

        public boolean canRetry()
        {
            return false;
        }
    }

    private static class CoreReadOnlyWork<R> implements IndexInfo.LockWork<R>
    {
        ReadOnlyWork<R> lockWork;

        LuceneIndexer indexer;

        CoreReadOnlyWork(LuceneIndexer indexer, ReadOnlyWork<R> lockWork)
        {
            this.indexer = indexer;
            this.lockWork = lockWork;
        }

        public R doWork() throws Exception
        {
            return indexer.doReadOnly(new IndexInfo.LockWork<R>()
            {
                public R doWork()
                {
                    try
                    {
                        return lockWork.doWork();
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
                }

                public boolean canRetry()
                {
                    return false;
                }
            });
        }

        public boolean canRetry()
        {
            return false;
        }
    }

    public static void main(String[] args) throws IOException
    {
        // delete a directory ....
        if (args.length != 1)
        {
            return;
        }
        File file = new File(args[0]);
        deleteDirectory(file);
    }

    public static void deleteDirectory(File directory) throws IOException
    {
        if (!directory.exists())
        {
            return;
        }
        if (!directory.isDirectory())
        {
            throw new IllegalArgumentException("Not a directory " + directory);
        }

        File[] files = directory.listFiles();
        if (files == null)
        {
            throw new IOException("Failed to delete director - no access" + directory);
        }

        for (int i = 0; i < files.length; i++)
        {
            File file = files[i];

            System.out.println(".");
            // System.out.println("Deleting "+file.getCanonicalPath());
            if (file.isDirectory())
            {
                deleteDirectory(file);
            }
            else
            {
                if (!file.delete())
                {
                    throw new IOException("Unable to delete file: " + file);
                }
            }
        }

        if (!directory.delete())
        {
            throw new IOException("Unable to delete directory " + directory);
        }
    }
}
