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
package org.alfresco.repo.node.index;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.Transaction;
import org.alfresco.repo.search.Indexer;
import org.alfresco.repo.search.impl.lucene.AbstractLuceneQueryParser;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParser;
import org.alfresco.repo.search.impl.lucene.LuceneResultSetRow;
import org.alfresco.repo.search.impl.lucene.fts.FullTextSearchIndexer;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.repo.transaction.TransactionServiceImpl;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.NodeRef.Status;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.VmShutdownListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

/**
 * Abstract helper for reindexing.
 * 
 * @see #reindexImpl()
 * @see #getIndexerWriteLock()
 * @see #isShuttingDown()
 * 
 * @author Derek Hulley
 */
public abstract class AbstractReindexComponent implements IndexRecovery
{
    private static Log logger = LogFactory.getLog(AbstractReindexComponent.class);
    private static Log loggerOnThread = LogFactory.getLog(AbstractReindexComponent.class.getName() + ".threads");
    
    /** kept to notify the thread that it should quit */
    private static VmShutdownListener vmShutdownListener = new VmShutdownListener("IndexRecovery");
    
    /** provides transactions to atomically index each missed transaction */
    protected TransactionServiceImpl transactionService;
    /** the component to index the node hierarchy */
    protected Indexer indexer;
    /** the FTS indexer that we will prompt to pick up on any un-indexed text */
    protected FullTextSearchIndexer ftsIndexer;
    /** the component providing searches of the indexed nodes */
    protected SearchService searcher;
    /** the component giving direct access to <b>store</b> instances */
    protected NodeService nodeService;
    /** the component giving direct access to <b>transaction</b> instances */
    protected NodeDAO nodeDAO;
    /** the component that holds the reindex worker threads */
    private ThreadPoolExecutor threadPoolExecutor;
    
    private TenantService tenantService;
    private Set<String> storeProtocolsToIgnore = new HashSet<String>(7);
    private Set<StoreRef> storesToIgnore = new HashSet<StoreRef>(7);
    
    private volatile boolean shutdown;
    private final WriteLock indexerWriteLock;
    
    public AbstractReindexComponent()
    {
        shutdown = false;
        ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        indexerWriteLock = readWriteLock.writeLock();
    }
    
    /**
     * Convenience method to get a common write lock.  This can be used to avoid
     * concurrent access to the work methods.
     */
    protected WriteLock getIndexerWriteLock()
    {
        return indexerWriteLock;
    }
    
    /**
     * Programmatically notify a reindex thread to terminate
     * 
     * @param shutdown true to shutdown, false to reset
     */
    public void setShutdown(boolean shutdown)
    {
        this.shutdown = shutdown;
    }
    
    /**
     * 
     * @return Returns true if the VM shutdown hook has been triggered, or the instance
     *      was programmatically {@link #shutdown shut down}
     */
    protected boolean isShuttingDown()
    {
        return shutdown || vmShutdownListener.isVmShuttingDown();
    }

    /**
     * No longer required
     */
    public void setAuthenticationComponent(AuthenticationComponent authenticationComponent)
    {
        logger.warn("Bean property 'authenticationComponent' is no longer required on 'AbstractReindexComponent'.");
    }

    /**
     * Set the low-level transaction component to use
     * 
     * @param transactionComponent provide transactions to index each missed transaction
     */
    public void setTransactionService(TransactionServiceImpl transactionService)
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
     * @param nodeDAO provides access to transaction-related queries
     */
    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }

    /**
     * Set the thread pool to use when doing asynchronous reindexing.  Use <tt>null</tt>
     * to have the calling thread do the indexing.
     * 
     * @param threadPoolExecutor        a pre-configured thread pool for the reindex work
     * 
     * @since 2.1.4
     */
    public void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor)
    {
        this.threadPoolExecutor = threadPoolExecutor;
    }
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    /**
     * @param storeProtocolsToIgnore    a list of store protocols that will be ignored
     *                                  by the index check code e.g. 'deleted' in 'deleted://MyStore'
     * @since 3.4
     */
    public void setStoreProtocolsToIgnore(List<String> storeProtocolsToIgnore)
    {
        for (String storeProtocolToIgnore : storeProtocolsToIgnore)
        {
            this.storeProtocolsToIgnore.add(storeProtocolToIgnore);
        }
    }
    
    /**
     * @param storesToIgnore            a list of store references that will be ignored
     *                                  by the index check code e.g. 'test://TestOne'
     */
    public void setStoresToIgnore(List<String> storesToIgnore)
    {
        for (String storeToIgnore : storesToIgnore)
        {
            StoreRef storeRef = new StoreRef(storeToIgnore);
            this.storesToIgnore.add(storeRef);
        }
    }
    
    /**
     * Find out if a store is ignored by the indexing code
     * 
     * @param storeRef                  the store to check
     * @return                          Returns <tt>true</tt> if the store reference provided is not indexed
     */
    public boolean isIgnorableStore(StoreRef storeRef)
    {
        storeRef = tenantService.getBaseName(storeRef);                 // Convert to tenant-safe check
        return storesToIgnore.contains(storeRef) || storeProtocolsToIgnore.contains(storeRef.getProtocol());
    }
    
    /**
     * Determines if calls to {@link #reindexImpl()} should be wrapped in a transaction or not.
     * The default is <b>true</b>.
     * 
     * @return      Returns <tt>true</tt> if an existing transaction is required for reindexing.
     */
    protected boolean requireTransaction()
    {
        return true;
    }
    
    /**
     * Perform the actual work.  This method will be called as the system user
     * and within an existing transaction.  This thread will only ever be accessed
     * by a single thread per instance.
     *
     */
    protected abstract void reindexImpl();
    
    /**
     * If this object is currently busy, then it just nothing
     */
    public final void reindex()
    {
        PropertyCheck.mandatory(this, "ftsIndexer", this.ftsIndexer);
        PropertyCheck.mandatory(this, "indexer", this.indexer);
        PropertyCheck.mandatory(this, "searcher", this.searcher);
        PropertyCheck.mandatory(this, "nodeService", this.nodeService);
        PropertyCheck.mandatory(this, "nodeDaoService", this.nodeDAO);
        PropertyCheck.mandatory(this, "transactionComponent", this.transactionService);
        PropertyCheck.mandatory(this, "storesToIgnore", this.storesToIgnore);
        PropertyCheck.mandatory(this, "storeProtocolsToIgnore", this.storeProtocolsToIgnore);
        
        if (indexerWriteLock.tryLock())
        {
            try
            {
                // started
                if (logger.isDebugEnabled())
                {
                    logger.debug("Reindex work started: " + this);
                }
                
                AuthenticationUtil.pushAuthentication();
                // authenticate as the system user
                AuthenticationUtil.setRunAsUserSystem();
                RetryingTransactionCallback<Object> reindexWork = new RetryingTransactionCallback<Object>()
                {
                    public Object execute() throws Exception
                    {
                        reindexImpl();
                        return null;
                    }
                };
                if (requireTransaction())
                {
                    transactionService.getRetryingTransactionHelper().doInTransaction(reindexWork, true);
                }
                else
                {
                    reindexWork.execute();
                }
            }
            catch (Throwable e)
            {
                throw new AlfrescoRuntimeException("Reindex failure for " + this.getClass().getName(), e);
            }
            finally
            {
                try { indexerWriteLock.unlock(); } catch (Throwable e) {}
                AuthenticationUtil.popAuthentication();
            }
            // done
            if (logger.isDebugEnabled())
            {
                logger.debug("Reindex work completed: " + this);
            }
        }
        else
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Bypassed reindex work - already busy: " + this);
            }
        }
    }

    public enum InIndex
    {
        YES, NO, INDETERMINATE;
    }
    
    private static final String KEY_STORE_REFS = "StoreRefCacheMethodInterceptor.StoreRefs";
    @SuppressWarnings("unchecked")
    /**
     * Helper method that caches ADM store references to prevent repeated and unnecessary calls to the
     * NodeService for this list.
     */
    private Set<StoreRef> getAdmStoreRefs()
    {
        Set<StoreRef> storeRefs = (Set<StoreRef>) AlfrescoTransactionSupport.getResource(KEY_STORE_REFS);
        if (storeRefs != null)
        {
            return storeRefs;
        }
        else
        {
            storeRefs = new HashSet<StoreRef>(nodeService.getStores());
            Iterator<StoreRef> storeRefsIterator = storeRefs.iterator();
            while (storeRefsIterator.hasNext())
            {
                // Remove AVM stores
                StoreRef storeRef = storeRefsIterator.next();
                if (storeRef.getProtocol().equals(StoreRef.PROTOCOL_AVM))
                {
                    storeRefsIterator.remove();
                }
            }
            
            storeRefsIterator = storeRefs.iterator();
            while (storeRefsIterator.hasNext())
            {
                // Remove stores to ignore
                StoreRef storeRef = storeRefsIterator.next();
                if (isIgnorableStore(storeRef))
                {
                    storeRefsIterator.remove();
                }
            }
            
            // Bind it in
            AlfrescoTransactionSupport.bindResource(KEY_STORE_REFS, storeRefs);
        }
        return storeRefs;
    }
    
    /**
     * Determines if a given transaction is definitely in the index or not.
     * 
     * @param txn       a specific transaction
     * @return          Returns <tt>true</tt> if the transaction is definitely in the index
     */
    @SuppressWarnings("unchecked")
    public InIndex isTxnPresentInIndex(final Transaction txn)
    {
        if (txn == null)
        {
            return InIndex.YES;
        }

        final Long txnId = txn.getId();
        if (logger.isTraceEnabled())
        {
            logger.trace("Checking for transaction in index: " + txnId);
        }
        

        // Let's scan the changes for this transaction, and group together changes for applicable stores
        List<NodeRef.Status> nodeStatuses = nodeDAO.getTxnChanges(txnId);        
        Set<StoreRef> admStoreRefs = getAdmStoreRefs();
        Map<StoreRef, List<NodeRef.Status>> storeStatusMap = new HashMap<StoreRef, List<Status>>(admStoreRefs.size() * 2);
        for (NodeRef.Status nodeStatus : nodeStatuses)
        {
            StoreRef storeRef = nodeStatus.getNodeRef().getStoreRef(); 
            if (admStoreRefs.contains(storeRef))
            {
                List<NodeRef.Status> storeStatuses = storeStatusMap.get(storeRef);
                if (storeStatuses == null)
                {
                    storeStatuses = new LinkedList<Status>();
                    storeStatusMap.put(storeRef, storeStatuses);
                }
                storeStatuses.add(nodeStatus);
            }
        }

        // Default decision is indeterminate, unless all established to be in index (YES) or one established to be missing (NO)
        InIndex result = InIndex.INDETERMINATE;

        // Check if the txn ID is present in every applicable store's index
        for (Map.Entry<StoreRef, List<NodeRef.Status>> entry : storeStatusMap.entrySet())
        {
            StoreRef storeRef = entry.getKey();
            List<NodeRef.Status> storeStatuses = entry.getValue();

            // Establish the number of deletes and updates for this storeRef
            int deleteCount = 0;
            int updateCount = 0;
            for (NodeRef.Status nodeStatus : storeStatuses)
            {
                if (nodeStatus.isDeleted())
                {
                    deleteCount++;
                }
                else
                {
                    updateCount++;
                }                
            }
            
            if (updateCount > 0)
            {
                // Check the index
                if (isTxnIdPresentInIndex(storeRef, txn))
                {
                    result = InIndex.YES;                    
                }
                // There were updates, but there is no sign in the indexes
                else
                {
                    result = InIndex.NO;
                    break;
                }
            }
            // There were deleted nodes only. Check that all the deleted nodes were removed from the index otherwise it
            // is out of date. If all nodes have been removed from the index then the result is that the index is OK            
            // ETWOTWO-1387
            // ALF-1989 - even if the nodes have not been found it is no good to use for AUTO index checking 
            else if (deleteCount > 0 && !haveNodesBeenRemovedFromIndex(storeRef, storeStatuses, txn))
            {
                result = InIndex.NO;
                break;
            }
        }

        // done
        if (logger.isDebugEnabled())
        {           
            if (result == InIndex.NO)
            {
                logger.debug("Transaction " + txnId + " not present in indexes");

                logger.debug(nodeStatuses.size() + " nodes in DB transaction");
                for (NodeRef.Status nodeStatus : nodeStatuses)
                {
                    NodeRef nodeRef = nodeStatus.getNodeRef();
                    if (nodeStatus.isDeleted())
                    {
                        logger.debug("  DELETED TX " + nodeStatus.getChangeTxnId() + ": " + nodeRef);
                    }
                    else
                    {
                        logger.debug("  UPDATED / MOVED TX " + nodeStatus.getChangeTxnId() + ": " + nodeRef);
                        logger.debug("    " + nodeService.getProperties(nodeRef));
                    }
                    ResultSet results = null;
                    SearchParameters sp = new SearchParameters();
                    sp.setLanguage(SearchService.LANGUAGE_LUCENE);
                    sp.addStore(nodeRef.getStoreRef());
                    try
                    {
                        sp.setQuery("ID:" + LuceneQueryParser.escape(nodeRef.toString()));
    
                        results = searcher.query(sp);
                        for (ResultSetRow row : results)
                        {
                            StringBuilder builder = new StringBuilder(1024).append("  STILL INDEXED: {");
                            Document lrsDoc = ((LuceneResultSetRow) row).getDocument();
                            Iterator<Field> fields = ((List<Field>) lrsDoc.getFields()).iterator();
                            if (fields.hasNext())
                            {
                                Field field = fields.next();
                                builder.append(field.name()).append("=").append(field.stringValue());
                                while (fields.hasNext())
                                {
                                    field = fields.next();
                                    builder.append(", ").append(field.name()).append("=").append(field.stringValue());
                                }
                            }
                            builder.append("}");
                            logger.debug(builder.toString());
                        }
                    }
                    finally
                    {
                        if (results != null) { results.close(); }
                    }
                    try
                    {
                        sp.setQuery("FTSREF:" + LuceneQueryParser.escape(nodeRef.toString()));
    
                        results = searcher.query(sp);
                        for (ResultSetRow row : results)
                        {
                            StringBuilder builder = new StringBuilder(1024).append("  FTSREF: {");
                            Document lrsDoc = ((LuceneResultSetRow) row).getDocument();
                            Iterator<Field> fields = ((List<Field>) lrsDoc.getFields()).iterator();
                            if (fields.hasNext())
                            {
                                Field field = fields.next();
                                builder.append(field.name()).append("=").append(field.stringValue());
                                while (fields.hasNext())
                                {
                                    field = fields.next();
                                    builder.append(", ").append(field.name()).append("=").append(field.stringValue());
                                }
                            }
                            builder.append("}");
                            logger.debug(builder.toString());
                        }
                    }
                    finally
                    {
                        if (results != null) { results.close(); }
                    }
                }
            }
            else
            {
                if (logger.isTraceEnabled())
                {
                    logger.trace("Transaction " + txnId + " present in indexes: " + result);
                }                
            }
        }
        return result;
    }
    
    public InIndex isTxnPresentInIndex(final Transaction txn, final boolean readThrough)
    {
        return transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<InIndex>()
        {
            @Override
            public InIndex execute() throws Throwable
            {
                return isTxnPresentInIndex(txn);
            }
        }, true, readThrough);
    }

    /**
     * @return                  Returns true if the given transaction is present in the index
     */
    private boolean isTxnIdPresentInIndex(StoreRef storeRef, Transaction txn)
    {
        long txnId = txn.getId();
        String changeTxnId = txn.getChangeTxnId();
        // do the most update check, which is most common
        ResultSet results = null;
        try
        {
            SearchParameters sp = new SearchParameters();
            sp.addStore(storeRef);
            // search for it in the index, sorting with youngest first, fetching only 1
            sp.setLanguage(SearchService.LANGUAGE_LUCENE);
            sp.setQuery("TX:" + AbstractLuceneQueryParser.escape(changeTxnId));
            sp.setLimit(1);
            
            results = searcher.query(sp);
            
            if (results.length() > 0)
            {
                if (logger.isTraceEnabled())
                {
                    logger.trace("Index has results for txn " + txnId + " for store " + storeRef);
                }
                return true;        // there were updates/creates and results for the txn were found
            }
            else
            {
                if (logger.isTraceEnabled())
                {
                    logger.trace("Transaction " + txnId + " not in index for store " + storeRef + ".  Possibly out of date.");
                }
                return false;
            }
        }
        finally
        {
            if (results != null) { results.close(); }
        }
    }
    
    private boolean haveNodesBeenRemovedFromIndex(final StoreRef storeRef, List<NodeRef.Status> nodeStatuses, final Transaction txn)
    {
        final Long txnId = txn.getId();
        // there have been deletes, so we have to ensure that none of the nodes deleted are present in the index
        boolean foundNodeRef = false;
        for (NodeRef.Status nodeStatus : nodeStatuses)
        {
            NodeRef nodeRef = nodeStatus.getNodeRef();
            if (logger.isTraceEnabled())
            {
                logger.trace(
                        
                        "Searching for node in index: \n" +
                        "   node: " + nodeRef + "\n" +
                        "   txn: " + txnId);
            }
            // we know that these are all deletions
            ResultSet results = null;
            try
            {
                SearchParameters sp = new SearchParameters();
                sp.addStore(storeRef);
                // search for it in the index, sorting with youngest first, fetching only 1
                sp.setLanguage(SearchService.LANGUAGE_LUCENE);
                sp.setQuery("ID:" + AbstractLuceneQueryParser.escape(nodeRef.toString()));
                sp.setLimit(1);

                results = searcher.query(sp);
              
                if (results.length() > 0)
                {
                    foundNodeRef = true;
                    break;
                }
            }
            finally
            {
                if (results != null) { results.close(); }
            }
        }
        if (foundNodeRef)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(" --> Node found (Index out of date)");
            }
        }
        else
        {
            // No nodes found
            if (logger.isTraceEnabled())
            {
                logger.trace(" --> Node not found (OK)");
            }
        }
        return !foundNodeRef;
    }
    
    /**
     * Marker exception to neatly handle VM-driven termination of a reindex
     * 
     * @author Derek Hulley
     * @since 2.1.4
     */
    public static class ReindexTerminatedException extends RuntimeException
    {
        private static final long serialVersionUID = -7928720932368892814L;
    }
    
    /**
     * Callback to notify caller whenever a node has been indexed
     * 
     * @see 
     * @author Derek Hulley
     * @since 2.1.4
     */
    protected interface ReindexNodeCallback
    {
        void reindexedNode(NodeRef nodeRef);
    }
    
    protected void reindexTransaction(Long txnId, boolean isFull)
    {
        reindexTransaction(txnId, null, isFull);
    }
    
    /**
     * Perform a full reindexing of the given transaction on the current thread.
     * The calling thread must be in the context of a read-only transaction.
     * 
     * @param txnId         the transaction identifier
     * @param callback      the callback to notify of each node indexed
     * 
     * @throws ReindexTerminatedException if the VM is shutdown during the reindex
     */
    protected void reindexTransaction(final long txnId, ReindexNodeCallback callback, boolean isFull)
    {
        ParameterCheck.mandatory("txnId", txnId);
        if (logger.isDebugEnabled())
        {
            logger.debug("Reindexing transaction: " + txnId);
        }
        if (AlfrescoTransactionSupport.getTransactionReadState() != TxnReadState.TXN_READ_ONLY)
        {
            throw new AlfrescoRuntimeException("Reindex work must be done in the context of a read-only transaction");
        }
        
        // The indexer will 'read through' to the latest database changes for the rest of this transaction
        indexer.setReadThrough(true);

        // get the node references pertinent to the transaction - We need to 'read through' here too
        List<Pair<NodeRef.Status, ChildAssociationRef>> nodePairs = transactionService.getRetryingTransactionHelper().doInTransaction(
                new RetryingTransactionCallback<List<Pair<NodeRef.Status, ChildAssociationRef>>>()
                {

                    @Override
                    public List<Pair<NodeRef.Status, ChildAssociationRef>> execute() throws Throwable
                    {
                        List<NodeRef.Status> nodeStatuses = nodeDAO.getTxnChanges(txnId);
                        List<Pair<NodeRef.Status, ChildAssociationRef>> nodePairs = new ArrayList<Pair<Status,ChildAssociationRef>>(nodeStatuses.size());
                        for (NodeRef.Status nodeStatus : nodeStatuses)
                        {
                            if (nodeStatus == null)
                            {
                                // it's not there any more
                                continue;
                            }
                            
                            ChildAssociationRef parent = nodeStatus.isDeleted() ? null : nodeService.getPrimaryParent(nodeStatus.getNodeRef());
                            nodePairs.add(new Pair<NodeRef.Status, ChildAssociationRef>(nodeStatus, parent));
                        }
                        return nodePairs;
                    }
                }, true, true);

        // reindex each node
        int nodeCount = 0;
        for (Pair<NodeRef.Status, ChildAssociationRef> nodePair: nodePairs)
        {
            NodeRef.Status nodeStatus = nodePair.getFirst();
            NodeRef nodeRef = nodeStatus.getNodeRef();

            if (nodeStatus.isDeleted())                                 // node deleted
            {
                if(isFull == false)
                {
                    // only the child node ref is relevant
                    ChildAssociationRef assocRef = new ChildAssociationRef(
                            ContentModel.ASSOC_CHILDREN,
                            null,
                            null,
                            nodeRef);
                    indexer.deleteNode(assocRef);
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("DELETE: " + nodeRef);
                    }
                }
            }
            else                                                        // node created
            {
                if(isFull)
                {
                    ChildAssociationRef assocRef = new ChildAssociationRef(
                            ContentModel.ASSOC_CHILDREN,
                            null,
                            null,
                            nodeRef);
                    indexer.createNode(assocRef);
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("CREATE: " + nodeRef);
                    }
                }
                else
                {
                    // reindex - force a cascade reindex if possible (to account for a possible move)
                    ChildAssociationRef parent = nodePair.getSecond();
                    if (parent == null)
                    {
                        indexer.updateNode(nodeRef);
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("UPDATE: " + nodeRef);
                        }
                    }
                    else
                    {
                        indexer.createChildRelationship(parent);
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("MOVE: " + nodeRef + ", " + parent);
                        }
                    }
                }
            }
            // Make the callback
            if (callback != null)
            {
                callback.reindexedNode(nodeRef);
            }
            // Check for VM shutdown every 100 nodes
            if (++nodeCount % 100 == 0 && isShuttingDown())
            {
                // We can't fail gracefully and run the risk of committing a half-baked transaction
                logger.info("Reindexing of transaction " + txnId + " terminated by VM shutdown.");
                throw new ReindexTerminatedException();
            }
        }
        // done
    }
    
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger();
    /**
     * Runnable that does reindex work for a given transaction but waits on a queue before
     * triggering the commit phase.
     * <p>
     * This class uses <code>Object</code>'s default equality and hashcode generation.
     * 
     * @author Derek Hulley
     * @since 2.1.4
     */
    private class ReindexWorkerRunnable extends TransactionListenerAdapter implements Runnable, ReindexNodeCallback
    {
        private final int id;
        private final int uidHashCode;
        private final List<Long> txnIds;
        private long lastIndexedTimestamp;
        private boolean atHeadOfQueue;
        private boolean isFull;
        
        private ReindexWorkerRunnable(List<Long> txnIds, boolean isFull)
        {
            this.id = ID_GENERATOR.addAndGet(1);
            if (ID_GENERATOR.get() > 1000)
            {
                ID_GENERATOR.set(0);
            }
            this.uidHashCode = id * 13 + 11;
            this.txnIds = txnIds;
            this.isFull = isFull;
            this.atHeadOfQueue = false;
            recordTimestamp();
        }
        
        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder(128);
            sb.append("ReindexWorkerRunnable")
              .append("[id=").append(id)
              .append("[txnIds=").append(txnIds)
              .append("]");
            return sb.toString();
        }
        
        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof ReindexWorkerRunnable))
            {
                return false;
            }
            ReindexWorkerRunnable that = (ReindexWorkerRunnable) obj;
            return this.id == that.id;
        }

        @Override
        public int hashCode()
        {
            return uidHashCode;
        }

        /**
         * @return      the time that the last node was indexed (nanoseconds)
         */
        public synchronized long getLastIndexedTimestamp()
        {
            return lastIndexedTimestamp;
        }
        
        private synchronized void recordTimestamp()
        {
            this.lastIndexedTimestamp = System.nanoTime();
        }
        
        private synchronized boolean isAtHeadOfQueue()
        {
            return atHeadOfQueue;
        }
        
        private synchronized void waitForHeadOfQueue()
        {
            try { wait(100L); } catch (InterruptedException e) {}
        }

        public synchronized void setAtHeadOfQueue()
        {
            this.notifyAll();
            this.atHeadOfQueue = true;
        }
        
        public void run()
        {
            RetryingTransactionCallback<Object> reindexCallback = new RetryingTransactionCallback<Object>()
            {
                public Object execute() throws Throwable
                {
                    // The first thing is to ensure that beforeCommit will be called
                    AlfrescoTransactionSupport.bindListener(ReindexWorkerRunnable.this);
                    // Now reindex
                    for (Long txnId : txnIds)
                    {
                        if (loggerOnThread.isDebugEnabled())
                        {
                            String msg = String.format(
                                    "   -> Reindexer %5d reindexing %10d",
                                    id, txnId.longValue());
                            loggerOnThread.debug(msg);
                        }
                        reindexTransaction(txnId, ReindexWorkerRunnable.this, isFull);
                    }
                    // Done
                    return null;
                }
            };
            // Timestamp for when we start
            recordTimestamp();
            try
            {
                if (loggerOnThread.isDebugEnabled())
                {
                    int txnIdsSize = txnIds.size();
                    String msg = String.format(
                            "Reindexer %5d starting [%10d, %10d] on %s.",
                            id,
                            (txnIdsSize == 0 ? -1 : txnIds.get(0)),
                            (txnIdsSize == 0 ? -1 : txnIds.get(txnIdsSize-1)),
                            Thread.currentThread().getName());
                    loggerOnThread.debug(msg);
                }
                // Do the work
                transactionService.getRetryingTransactionHelper().doInTransaction(reindexCallback, true, true);
            }
            catch (ReindexTerminatedException e)
            {
                // This is OK
                String msg = String.format(
                        "Reindexer %5d terminated: %s.",
                        id,
                        e.getMessage());
                loggerOnThread.warn(msg);
                loggerOnThread.warn(getStackTrace(e));
            }
            catch (Throwable e)
            {
                String msg = String.format(
                        "Reindexer %5d failed with error: %s.",
                        id,
                        e.getMessage());
                loggerOnThread.error(msg);
                loggerOnThread.warn(getStackTrace(e));
            }
            finally
            {
                // Triple check that we get the queue state right
                removeFromQueueAndProdHead();
            }
        }
        
        public String getStackTrace(Throwable t)
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw, true);
            t.printStackTrace(pw);
            pw.flush();
            sw.flush();
            return sw.toString();
        }

        
        public synchronized void reindexedNode(NodeRef nodeRef)
        {
            recordTimestamp();
        }
        
        /**
         * Removes this instance from the queue and notifies the HEAD
         */
        private void removeFromQueueAndProdHead()
        {
            try
            {
                reindexThreadLock.writeLock().lock();
                // Remove self from head of queue
                reindexThreadQueue.remove(this);
            }
            finally
            {
                reindexThreadLock.writeLock().unlock();
            }
            // Now notify the new head object
            ReindexWorkerRunnable newPeek = peekHeadReindexWorker();
            if (newPeek != null)
            {
                newPeek.setAtHeadOfQueue();
            }
            if (loggerOnThread.isDebugEnabled())
            {
                String msg = String.format(
                        "Reindexer %5d removed from queue.  Current HEAD is %s.",
                        id, newPeek);
                loggerOnThread.debug(msg);
            }
        }
        
        @Override
        public void beforeCommit(boolean readOnly)
        {
            // Do whatever reindexing work we can in parallel before final queue serialization
            indexer.flushPending();

            // Do the queue ordering in the prepare phase so we don't deadlock with throttling!
            handleQueue();
        }

        @Override
        public void afterCommit()
        {
            // Lucene can now get on with the commit.  We didn't have ordering at this level
            // and the IndexInfo locks are handled by Lucene.  So we let the thread go and
            // the other worker threads can get on with it.
            // Record the fact that the thread is on the final straight.  From here on, no
            // more work notifications will be possible so the timestamp needs to spoof it.
            recordTimestamp();
        }

        @Override
        public void afterRollback()
        {
            handleQueue();
            
            // Lucene can now get on with the commit.  We didn't have ordering at this level
            // and the IndexInfo locks are handled by Lucene.  So we let the thread go and
            // the other worker threads can get on with it.
            // Record the fact that the thread is on the final straight.  From here on, no
            // more work notifications will be possible so the timestamp needs to spoof it.
            recordTimestamp();            
        }
        /**
         * Lucene will do its final commit once this has been allowed to proceed.
         */
        private void handleQueue()
        {
            while (true)
            {
                // Quick check to see if we're at the head of the queue
                ReindexWorkerRunnable peek = peekHeadReindexWorker();
                // Release the current queue head to finish (this might be this instance)
                if (peek != null)
                {
                    peek.setAtHeadOfQueue();
                }
                // Check kill switch
                if (peek == null || isAtHeadOfQueue())
                {
                    // Going to close
                    break;
                }
                else
                {
                    // This thread is not at the head of the queue so just wait
                    // until someone notifies us to carry on
                    waitForHeadOfQueue();
                    // Loop again
                }
            }
        }
    }
        
    /**
     * FIFO queue to control the ordering of transaction commits.  Synchronization around this object is
     * controlled by the read-write lock.
     */
    private LinkedBlockingQueue<ReindexWorkerRunnable> reindexThreadQueue = new LinkedBlockingQueue<ReindexWorkerRunnable>();
    private ReentrantReadWriteLock reindexThreadLock = new ReentrantReadWriteLock(true);
    
    /**
     * Read-safe method to peek at the head of the queue
     */
    private ReindexWorkerRunnable peekHeadReindexWorker()
    {
        try
        {
            reindexThreadLock.readLock().lock();
            return reindexThreadQueue.peek();
        }
        finally
        {
            reindexThreadLock.readLock().unlock();
        }
    }
    
    /**
     * Performs indexing off the current thread, which may return quickly if there are threads immediately
     * available in the thread pool.
     * <p>
     * Commits are guaranteed to occur in the order in which this reindex jobs are added to the queue.
     *
     * @see #reindexTransaction(long)
     * @see #waitForAsynchronousReindexing()
     * @since 2.1.4
     */
    protected void reindexTransactionAsynchronously(final List<Long> txnIds, final boolean isFull)
    {
        // Bypass if there is no thread pool
        if (threadPoolExecutor == null || threadPoolExecutor.getMaximumPoolSize() < 2)
        {
            if (loggerOnThread.isDebugEnabled())
            {
                String msg = String.format(
                        "Reindexing inline: %s.",
                        txnIds.toString());
                loggerOnThread.debug(msg);
            }
            RetryingTransactionCallback<Object> reindexCallback = new RetryingTransactionCallback<Object>()
            {
                public Object execute() throws Throwable
                {
                    for (Long txnId : txnIds)
                    {
                        if (loggerOnThread.isDebugEnabled())
                        {
                            String msg = String.format(
                                    "Reindex %10d.",
                                    txnId.longValue());
                            loggerOnThread.debug(msg);
                        }
                        reindexTransaction(txnId, null, isFull);
                    }
                    return null;
                }
            };
            transactionService.getRetryingTransactionHelper().doInTransaction(reindexCallback, true, true);
            return;
        }
        
        ReindexWorkerRunnable runnable = new ReindexWorkerRunnable(txnIds, isFull);
        try
        {
            reindexThreadLock.writeLock().lock();
            // Add the runnable to the queue to ensure ordering
            reindexThreadQueue.add(runnable);
        }
        finally
        {
            reindexThreadLock.writeLock().unlock();
        }
        // Ship it to a thread.
        // We don't do this in the lock - but the situation should be avoided by having the blocking
        // queue size less than the maximum pool size
        threadPoolExecutor.execute(runnable);
    }
    
    /**
     * Wait for all asynchronous indexing to finish before returning.  This is useful if the calling thread
     * wants to ensure that all reindex work has finished before continuing.
     */
    protected synchronized void waitForAsynchronousReindexing()
    {
        ReindexWorkerRunnable lastRunnable = null;
        long lastTimestamp = Long.MAX_VALUE;
        
        ReindexWorkerRunnable currentRunnable = peekHeadReindexWorker();
        while (currentRunnable != null && !isShuttingDown())
        {
            // Notify the runnable that it is at the head of the queue
            currentRunnable.setAtHeadOfQueue();
            // Give the thread chance to commit
            synchronized(this)
            {
                try { wait(100); } catch (InterruptedException e) {}
            }
            
            long currentTimestamp = currentRunnable.getLastIndexedTimestamp();
            // The head of the queue holds proceedings, so it can't be allowed to continue forever
            // Allow 60s of inactivity.  We don't anticipate more than a few milliseconds between
            // timestamp advances for the reindex threads so this checking is just for emergencies
            // to prevent the queue from getting locked up.
            if (lastRunnable == currentRunnable)
            {
                if (currentTimestamp - lastTimestamp > 600E9)
                {
                    
                    try
                    {
                        reindexThreadLock.writeLock().lock();
                        // Double check
                        ReindexWorkerRunnable checkCurrentRunnable = reindexThreadQueue.peek();
                        if (lastRunnable != checkCurrentRunnable)
                        {
                            // It's moved on - just in time
                        }
                        else
                        {
                            loggerOnThread.warn("Detected reindex thread inactivity: " + currentRunnable);
                            //reindexThreadQueue.remove(currentRunnable);
                            //currentRunnable.kill();
                        }
                        // Reset
                        lastRunnable = null;
                        lastTimestamp = Long.MAX_VALUE;
                        // Peek at the queue and check again
                        currentRunnable  = reindexThreadQueue.peek();
                    }
                    finally
                    {
                        reindexThreadLock.writeLock().unlock();
                    }
                    continue;
                }
                // Swap timestamps
                lastRunnable = currentRunnable;
                lastTimestamp = currentTimestamp;
            }
            else
            {
                // Swap timestamps
                lastRunnable = currentRunnable;
                lastTimestamp = currentTimestamp;
            }
            currentRunnable = peekHeadReindexWorker();
        }
    }
}