/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
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
package org.alfresco.repo.node.index;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.NodeStatus;
import org.alfresco.repo.search.Indexer;
import org.alfresco.repo.search.impl.lucene.LuceneIndexerImpl;
import org.alfresco.repo.search.impl.lucene.fts.FullTextSearchIndexer;
import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.repo.transaction.TransactionUtil.TransactionWork;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.CacheMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Ensures that the FTS indexing picks up on any outstanding documents that
 * require indexing.
 * <p>
 * This component must be used as a singleton (one per VM) and may only be
 * called to reindex once.  It will start a thread that processes all available
 * transactions and keeps checking to ensure that the index is up to date with
 * the latest database changes.
 * <p>
 * <b>The following points are important:</b>
 * <ul>
 *   <li>
 *       By default, the Hibernate L2 cache is used during processing.
 *       This can be disabled by either disabling the L2 cache globally
 *       for the server (not recommended) or by setting the
 *       {@link #setL2CacheMode(String) l2CacheMode} property.  If the
 *       database is static then the L2 cache usage can be set to use
 *       the <code>NORMAL</code> mode.  <code>REFRESH</code> should be
 *       used where the server will still be accessed from some clients
 *       despite the database changing.
 *   </li>
 *   <li>
 *       This process should not run continuously on a live
 *       server as it would be performing unecessary work.
 *       If it was left running, however, it would not
 *       lead to data corruption or such-like.  Use the
 *       {@link #setRunContinuously(boolean) runContinuously} property
 *       to change this behaviour.
 *   </li>
 * </ul>
 * 
 * @author Derek Hulley
 */
public class FullIndexRecoveryComponent extends HibernateDaoSupport implements IndexRecovery
{
    public static final String QUERY_GET_NEXT_CHANGE_TXN_IDS = "node.GetNextChangeTxnIds";
    public static final String QUERY_GET_CHANGED_NODE_STATUSES = "node.GetChangedNodeStatuses";
    public static final String QUERY_GET_DELETED_NODE_STATUSES = "node.GetDeletedNodeStatuses";
    public static final String QUERY_GET_CHANGED_NODE_STATUSES_COUNT = "node.GetChangedNodeStatusesCount";
    
    private static final String START_TXN_ID = "000";
    
    private static Log logger = LogFactory.getLog(FullIndexRecoveryComponent.class);
    
    /** ensures that this process is kicked off once per VM */
    private static boolean started = false;
    /** The current transaction ID being processed */
    private static String currentTxnId = START_TXN_ID;
    /** kept to notify the thread that it should quite */
    private boolean killThread = false;
    
    /** provides transactions to atomically index each missed transaction */
    private TransactionService transactionService;
    /** the component to index the node hierarchy */
    private Indexer indexer;
    /** the FTS indexer that we will prompt to pick up on any un-indexed text */
    private FullTextSearchIndexer ftsIndexer;
    /** the component providing searches of the indexed nodes */
    private SearchService searcher;
    /** the component giving direct access to <b>node</b> instances */
    private NodeService nodeService;
    /** the stores to reindex */
    private List<StoreRef> storeRefs;
    /** set this to run the index recovery component */
    private boolean executeFullRecovery;
    /** set this on to keep checking for new transactions and never stop */
    private boolean runContinuously;
    /** set the time to wait between checking indexes */
    private long waitTime;
    /** controls how the L2 cache is used */
    private CacheMode l2CacheMode;
    
    /**
     * @return Returns the ID of the current (or last) transaction processed
     */
    public static String getCurrentTransactionId()
    {
        return currentTxnId;
    }

    public FullIndexRecoveryComponent()
    {
        this.storeRefs = new ArrayList<StoreRef>(2);
        
        this.killThread = false;
        this.executeFullRecovery = false;
        this.runContinuously = false;
        this.waitTime = 1000L;
        this.l2CacheMode = CacheMode.REFRESH;

        // ensure that we kill the thread when the VM is shutting down
        Runnable shutdownRunnable = new Runnable()
        {
            public void run()
            {
                killThread = true;
            };  
        };
        Thread shutdownThread = new Thread(shutdownRunnable);
        Runtime.getRuntime().addShutdownHook(shutdownThread);
    }

    /**
     * @return Returns true if the component has already been started
     */
    public static boolean isStarted()
    {
        return started;
    }

    /**
     * @param transactionService provide transactions to index each missed transaction
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * @param indexer the indexer that will be index
     */
    public void setIndexer(Indexer indexer)
    {
        this.indexer = indexer;
    }
    
    /**
     * @param ftsIndexer the FTS background indexer
     */
    public void setFtsIndexer(FullTextSearchIndexer ftsIndexer)
    {
        this.ftsIndexer = ftsIndexer;
    }

    /**
     * @param searcher component providing index searches
     */
    public void setSearcher(SearchService searcher)
    {
        this.searcher = searcher;
    }

    /**
     * @param nodeService provides information about nodes for indexing
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Set the stores that need reindexing
     * 
     * @param storeRefStrings a list of strings representing store references
     */
    public void setStores(List<String> storeRefStrings)
    {
        storeRefs.clear();
        for (String storeRefStr : storeRefStrings)
        {
            StoreRef storeRef = new StoreRef(storeRefStr);
            storeRefs.add(storeRef);
        }
    }

    /**
     * Set this to <code>true</code> to initiate the full index recovery.
     * <p>
     * This used to default to <code>true</code> but is now false.  Set this
     * if the potentially long-running process of checking and fixing the
     * indexes must be started.
     * 
     * @param executeFullRecovery
     */
    public void setExecuteFullRecovery(boolean executeFullRecovery)
    {
        this.executeFullRecovery = executeFullRecovery;
    }

    /**
     * Set this to ensure that the process continuously checks for new transactions.
     * If not, it will permanently terminate once it catches up with the current
     * transactions.
     * 
     * @param runContinuously true to never cease looking for new transactions
     */
    public void setRunContinuously(boolean runContinuously)
    {
        this.runContinuously = runContinuously;
    }

    /**
     * Set the time to wait between checking for new transaction changes in the database.
     * 
     * @param waitTime the time to wait in milliseconds
     */
    public void setWaitTime(long waitTime)
    {
        this.waitTime = waitTime;
    }

    /**
     * Set the hibernate cache mode by name
     * 
     * @see org.hibernate.CacheMode
     */
    public void setL2CacheMode(String l2CacheModeStr)
    {
        if (l2CacheModeStr.equals("GET"))
        {
            l2CacheMode = CacheMode.GET;
        }
        else if (l2CacheModeStr.equals("IGNORE"))
        {
            l2CacheMode = CacheMode.IGNORE;
        }
        else if (l2CacheModeStr.equals("NORMAL"))
        {
            l2CacheMode = CacheMode.NORMAL;
        }
        else if (l2CacheModeStr.equals("PUT"))
        {
            l2CacheMode = CacheMode.PUT;
        }
        else if (l2CacheModeStr.equals("REFRESH"))
        {
            l2CacheMode = CacheMode.REFRESH;
        }
        else
        {
            throw new IllegalArgumentException("Unrecognised Hibernate L2 cache mode: " + l2CacheModeStr);
        }
    }

    /**
     * Ensure that the index is up to date with the current state of the persistence layer.
     * The full list of unique transaction change IDs is retrieved and used to detect
     * which are not present in the index.  All the node changes and deletions for the
     * remaining transactions are then indexed.
     */
    public synchronized void reindex()
    {
        if (FullIndexRecoveryComponent.started)
        {
            throw new AlfrescoRuntimeException
                    ("Only one FullIndexRecoveryComponent may be used per VM and it may only be called once");
        }
        
        // ensure that we don't redo this work
        FullIndexRecoveryComponent.started = true;
        
        // work to mark the stores for full text reindexing
        TransactionWork<Object> ftsReindexWork = new TransactionWork<Object>()
        {
            public Object doWork()
            {
                // reindex each store
                for (StoreRef storeRef : storeRefs)
                {
                    // check if the store exists
                    if (!nodeService.exists(storeRef))
                    {
                        // store does not exist
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Skipping reindex of non-existent store: " + storeRef);
                        }
                        continue;
                    }
                    
                    // prompt FTS to reindex the store
                    ftsIndexer.requiresIndex(storeRef);
                }
                // done
                if (logger.isDebugEnabled())
                {
                    logger.debug("Prompted FTS index on stores: " + storeRefs);
                }
                return null;
            }
        };
        TransactionUtil.executeInNonPropagatingUserTransaction(transactionService, ftsReindexWork);

        // start full index recovery, if necessary
        if (!this.executeFullRecovery)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Full index recovery is off - quitting");
            }
        }
        else
        {
            // set the state of the reindex
            FullIndexRecoveryComponent.currentTxnId = START_TXN_ID;
            
            // start a stateful thread that will begin processing the reindexing the transactions
            Runnable runnable = new ReindexRunner();
            Thread reindexThread = new Thread(runnable);
            // make it a daemon thread
            reindexThread.setDaemon(true);
            // it should not be a high priority
            reindexThread.setPriority(Thread.MIN_PRIORITY);
            // start it
            reindexThread.start();
            
            if (logger.isDebugEnabled())
            {
                logger.debug("Full index recovery thread started: \n" +
                        "   continuous: " + runContinuously + "\n" +
                        "   stores: " + storeRefs);
            }
        }
    }
    
    /**
     * Stateful thread runnable that executes reindex calls.
     * 
     * @see FullIndexRecoveryComponent#reindexNodes()
     * 
     * @author Derek Hulley
     */
    private class ReindexRunner implements Runnable
    {
        public void run()
        {
            // keep this thread going permanently
            while (!killThread)
            {
                try
                {
                    // reindex nodes
                    List<String> txnsIndexed = FullIndexRecoveryComponent.this.reindexNodes();
                    // reindex missing content
                    @SuppressWarnings("unused")
                    int missingContentCount = FullIndexRecoveryComponent.this.reindexMissingContent();
                    // check if the process should terminate
                    if (txnsIndexed.size() == 0 && !runContinuously)
                    {
                        // the thread has caught up with all the available work and should not
                        // run continuously
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Thread quitting - no more available indexing to do: \n" +
                                    "   last txn: " + FullIndexRecoveryComponent.getCurrentTransactionId());
                        }
                        break;
                    }
                    // brief pause
                    synchronized(FullIndexRecoveryComponent.this)
                    {
                        FullIndexRecoveryComponent.this.wait(waitTime);
                    }
                }
                catch (InterruptedException e)
                {
                    // ignore
                }
                catch (Throwable e)
                {
                    if (killThread)
                    {
                        // the shutdown may have caused the exception - ignore it
                    }
                    else
                    {
                        // we are still a go; report it
                        logger.error("Reindex failure", e);
                    }
                }
            }
        }
    }

    /**
     * @return Returns the number of documents reindexed
     */
    private int reindexMissingContent()
    {
        int count = 0;
        for (StoreRef storeRef : storeRefs)
        {
            count += reindexMissingContent(storeRef);
        }
        return count;
    }
    
    /**
     * @param storeRef the store to check for missing content
     * @return Returns the number of documents reindexed
     */
    private int reindexMissingContent(StoreRef storeRef)
    {
        SearchParameters sp = new SearchParameters();
        sp.addStore(storeRef);

        // search for it in the index
        String query = "TEXT:" + LuceneIndexerImpl.NOT_INDEXED_CONTENT_MISSING;
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery(query);
        ResultSet results = null;
        try
        {
            results = searcher.query(sp);
            
            int count = 0;
            // loop over the results and get the details of the nodes that have missing content
            List<ChildAssociationRef> assocRefs = results.getChildAssocRefs();
            for (ChildAssociationRef assocRef : assocRefs)
            {
                final NodeRef childNodeRef = assocRef.getChildRef();
                // prompt for a reindex - it might fail again, but we just keep plugging away
                TransactionWork<Object> reindexWork = new TransactionWork<Object>()
                {
                    public Object doWork()
                    {
                        indexer.updateNode(childNodeRef);
                        return null;
                    }
                };
                TransactionUtil.executeInNonPropagatingUserTransaction(transactionService, reindexWork);
                count++;
            }
            // done
            if (logger.isDebugEnabled())
            {
                logger.debug("Reindexed missing content: \n" +
                        "   store: " + storeRef + "\n" +
                        "   node count: " + count);
            }
            return count;
        }
        finally
        {
            if (results != null)
            {
                results.close();
            }
        }
    }
    
    /**
     * @return Returns the transaction ID just reindexed, i.e. where some work was performed
     */
    private List<String> reindexNodes()
    {
        // get a list of all transactions still requiring a check
        List<String> txnsToCheck = getNextChangeTxnIds(FullIndexRecoveryComponent.currentTxnId);
        
        // loop over each transaction
        for (String changeTxnId : txnsToCheck)
        {
            reindexNodes(changeTxnId);
        }
        
        // done
        return txnsToCheck;
    }
    
    /**
     * Reindexes changes specific to the change transaction ID.
     * <p>
     * <b>All exceptions are absorbed.</b>
     */
    private void reindexNodes(final String changeTxnId)
    {
        /*
         * This must execute each within its own transaction.
         * The cache size is therefore not an issue.
         */
        TransactionWork<Object> reindexWork = new TransactionWork<Object>()
        {
            public Object doWork() throws Exception
            {
                // perform the work in a Hibernate callback
                HibernateCallback callback = new ReindexCallback(changeTxnId);
                getHibernateTemplate().execute(callback);
                // done
                return null;
            }
        };
        try
        {
            TransactionUtil.executeInNonPropagatingUserTransaction(transactionService, reindexWork);
        }
        catch (Throwable e)
        {
            logger.error("Transaction reindex failed: \n" +
                    "   txn: " + changeTxnId,
                    e);
        }
        finally
        {
            // Up the current transaction now, in case the process fails at this point.
            // This will prevent the transaction from being processed again.
            // This applies to failures as well, which should be dealt with externally
            //      and having the entire process start again, e.g. such as a system reboot
            currentTxnId = changeTxnId;
        }
    }
    
    /**
     * Stateful inner class that implements a single reindex call for a given store
     * and transaction.
     * <p>
     * It must be called within its own transaction.
     * 
     * @author Derek Hulley
     */
    private class ReindexCallback implements HibernateCallback
    {
        private final String changeTxnId;
        
        public ReindexCallback(String changeTxnId)
        {
            this.changeTxnId = changeTxnId;
        }
        
        /**
         * Changes the L2 cache usage before reindexing for each store
         * 
         * @see #reindexNodes(StoreRef, String)
         */
        public Object doInHibernate(Session session)
        {
            // set the way the L2 cache is used
            getSession().setCacheMode(l2CacheMode);
            
            // reindex each store
            for (StoreRef storeRef : storeRefs)
            {
                if (!nodeService.exists(storeRef))
                {
                    // the store is not present
                    continue;
                }
                // reindex for store
                reindexNodes(storeRef, changeTxnId);
            }
            // done
            return null;
        }
        
        private void reindexNodes(StoreRef storeRef, String changeTxnId)
        {
            // check if we need to perform this operation
            SearchParameters sp = new SearchParameters();
            sp.addStore(storeRef);

            // search for it in the index
            String query = "TX:\"" + changeTxnId + "\"";
            sp.setLanguage(SearchService.LANGUAGE_LUCENE);
            sp.setQuery(query);
            ResultSet results = null;
            try
            {
                results = searcher.query(sp);
                // did the index have any of these changes?
                if (results.length() > 0)
                {
                    // the transaction has an entry in the index - assume that it was
                    // atomically correct
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Transaction present in index - no indexing required: \n" +
                                "   store: " + storeRef + "\n" +
                                "   txn: " + changeTxnId);
                    }
                    return;
                }
            }
            finally
            {
                if (results != null)
                {
                    results.close();
                }
            }
            // the index has no record of this
            // were there any changes, or is it all just deletions?
            int changedCount = getChangedNodeStatusesCount(storeRef, changeTxnId);
            if (changedCount == 0)
            {
                // no nodes were changed in the transaction, i.e. they are only deletions
                // the index is quite right not to have any entries for the transaction
                if (logger.isDebugEnabled())
                {
                    logger.debug("Transaction only has deletions - no indexing required: \n" +
                            "   store: " + storeRef + "\n" +
                            "   txn: " + changeTxnId);
                }
                return;
            }

            // process the deletions relevant to the txn and the store
            List<NodeStatus> deletedNodeStatuses = getDeletedNodeStatuses(storeRef, changeTxnId);
            for (NodeStatus status : deletedNodeStatuses)
            {
                NodeRef nodeRef = new NodeRef(storeRef, status.getKey().getGuid());
                // only the child node ref is relevant
                ChildAssociationRef assocRef = new ChildAssociationRef(
                        ContentModel.ASSOC_CHILDREN,
                        null,
                        null,
                        nodeRef);
                indexer.deleteNode(assocRef);
            }
            
            // process additions
            List<NodeStatus> changedNodeStatuses = getChangedNodeStatuses(storeRef, changeTxnId);
            for (NodeStatus status : changedNodeStatuses)
            {
                NodeRef nodeRef = new NodeRef(storeRef, status.getKey().getGuid());
                // get the primary assoc for the node
                ChildAssociationRef primaryAssocRef = nodeService.getPrimaryParent(nodeRef);
                // reindex
                indexer.createNode(primaryAssocRef);
            }
            
            // done
            if (logger.isDebugEnabled())
            {
                logger.debug("Transaction reindexed: \n" +
                        "   store: " + storeRef + "\n" +
                        "   txn: " + changeTxnId + "\n" +
                        "   deletions: " + deletedNodeStatuses.size() + "\n" +
                        "   modifications: " + changedNodeStatuses.size());
            }
        }
    };

    /**
     * Retrieve all transaction IDs that are greater than the given transaction ID.
     * 
     * @param currentTxnId the transaction ID that must be less than all returned results
     * @return Returns an ordered list of transaction IDs 
     */
    @SuppressWarnings("unchecked")
    public List<String> getNextChangeTxnIds(final String currentTxnId)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_NEXT_CHANGE_TXN_IDS);
                query.setString("currentTxnId", currentTxnId)
                     .setReadOnly(true);
                return query.list();
            }
        };
        List<String> queryResults = (List<String>) getHibernateTemplate().execute(callback);
        // done
        return queryResults;
    }

    @SuppressWarnings("unchecked")
    public int getChangedNodeStatusesCount(final StoreRef storeRef, final String changeTxnId)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_CHANGED_NODE_STATUSES_COUNT);
                query.setString("storeProtocol", storeRef.getProtocol())
                     .setString("storeIdentifier", storeRef.getIdentifier())
                     .setString("changeTxnId", changeTxnId)
                     .setReadOnly(true);
                return query.uniqueResult();
            }
        };
        Integer changeCount = (Integer) getHibernateTemplate().execute(callback);
        // done
        return changeCount.intValue();
    }

    @SuppressWarnings("unchecked")
    public List<NodeStatus> getChangedNodeStatuses(final StoreRef storeRef, final String changeTxnId)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_CHANGED_NODE_STATUSES);
                query.setString("storeProtocol", storeRef.getProtocol())
                     .setString("storeIdentifier", storeRef.getIdentifier())
                     .setString("changeTxnId", changeTxnId)
                     .setReadOnly(true);
                return query.list();
            }
        };
        List<NodeStatus> queryResults = (List) getHibernateTemplate().execute(callback);
        // done
        return queryResults;
    }

    @SuppressWarnings("unchecked")
    public List<NodeStatus> getDeletedNodeStatuses(final StoreRef storeRef, final String changeTxnId)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_DELETED_NODE_STATUSES);
                query.setString("storeProtocol", storeRef.getProtocol())
                     .setString("storeIdentifier", storeRef.getIdentifier())
                     .setString("changeTxnId", changeTxnId)
                     .setReadOnly(true);
                return query.list();
            }
        };
        List<NodeStatus> queryResults = (List) getHibernateTemplate().execute(callback);
        // done
        return queryResults;
    }
}