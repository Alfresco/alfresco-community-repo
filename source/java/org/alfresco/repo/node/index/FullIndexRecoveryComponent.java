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

import java.util.List;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.Transaction;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParser;
import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.repo.transaction.TransactionUtil.TransactionWork;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.NodeRef.Status;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Component to check and recover the indexes.
 * 
 * @author Derek Hulley
 */
public class FullIndexRecoveryComponent extends AbstractReindexComponent
{
    private static final String ERR_STORE_NOT_UP_TO_DATE = "index.recovery.store_not_up_to_date";
    private static final String MSG_RECOVERY_STARTING = "index.recovery.starting";
    private static final String MSG_RECOVERY_COMPLETE = "index.recovery.complete";
    private static final String MSG_RECOVERY_PROGRESS = "index.recovery.progress";
    private static final String MSG_RECOVERY_TERMINATED = "index.recovery.terminated";
    
    private static Log logger = LogFactory.getLog(FullIndexRecoveryComponent.class);
    
    public static enum RecoveryMode
    {
        /** Do nothing - not even a check */
        NONE,
        /** Perform a quick check on the state of the indexes only */
        VALIDATE,
        /** Performs a quick validation and then starts a full pass-through on failure */
        AUTO,
        /** Performs a full pass-through of all recorded transactions to ensure that the indexes are up to date */
        FULL;
    }
    
    private RecoveryMode recoveryMode;
    
    public FullIndexRecoveryComponent()
    {
        recoveryMode = RecoveryMode.VALIDATE;
    }

    /**
     * Set the type of recovery to perform.
     * 
     * @param recoveryMode one of the {@link RecoveryMode } values
     */
    public void setRecoveryMode(String recoveryMode)
    {
        this.recoveryMode = RecoveryMode.valueOf(recoveryMode);
    }
    
    @Override
    protected void reindexImpl()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Performing index recovery for type: " + recoveryMode);
        }
        
        // do we just ignore
        if (recoveryMode == RecoveryMode.NONE)
        {
            return;
        }
        // check the level of cover required
        boolean fullRecoveryRequired = false;
        if (recoveryMode == RecoveryMode.FULL)                  // no validate required
        {
            fullRecoveryRequired = true;
        }
        else                                                    // validate first
        {
            List<StoreRef> storeRefs = nodeService.getStores();
            for (StoreRef storeRef : storeRefs)
            {
                // get the last txn ID in the database
                Transaction txn = nodeDaoService.getLastTxn(storeRef);
                boolean lastChangeTxnIdInIndex = isTxnIdPresentInIndex(storeRef, txn);
                if (lastChangeTxnIdInIndex)
                {
                    // this store is good
                    continue;
                }
                // this store isn't up to date
                String msg = I18NUtil.getMessage(ERR_STORE_NOT_UP_TO_DATE, storeRef);
                logger.warn(msg);
                // the store is out of date - validation failed
                if (recoveryMode == RecoveryMode.VALIDATE)
                {
                    // next store
                    continue;
                }
                else if (recoveryMode == RecoveryMode.AUTO)
                {
                    fullRecoveryRequired = true;
                }
            }
        }
        
        // put the server into read-only mode for the duration
        boolean allowWrite = !transactionService.isReadOnly();
        try
        {
            // set the server into read-only mode
            transactionService.setAllowWrite(false);
            
            // do we need to perform a full recovery
            if (fullRecoveryRequired)
            {
                performFullRecovery();
            }
        }
        finally
        {
            // restore read-only state
            transactionService.setAllowWrite(allowWrite);
        }
        
    }
    
    private static final int MAX_TRANSACTIONS_PER_ITERATION = 1000;
    private void performFullRecovery()
    {
        int txnCount = nodeDaoService.getTransactionCount();
        // starting
        String msgStart = I18NUtil.getMessage(MSG_RECOVERY_STARTING, txnCount);
        logger.info(msgStart);
        
        // count the transactions
        int processedCount = 0;
        Transaction lastTxn = null;
        while(true)
        {
            List<Transaction> nextTxns = nodeDaoService.getNextTxns(
                    lastTxn,
                    MAX_TRANSACTIONS_PER_ITERATION);

            // reindex each transaction
            for (Transaction txn : nextTxns)
            {
                Long txnId = txn.getId();
                // check if we have to terminate
                if (isShuttingDown())
                {
                    String msgTerminated = I18NUtil.getMessage(MSG_RECOVERY_TERMINATED);
                    logger.warn(msgTerminated);
                    return;
                }
                
                reindexTransaction(txnId);
                
                // dump a progress report every 10% of the way
                double before = (double) processedCount / (double) txnCount * 10.0;     // 0 - 10 
                processedCount++;
                double after = (double) processedCount / (double) txnCount * 10.0;      // 0 - 10
                if (Math.floor(before) < Math.floor(after))                             // crossed a 0 - 10 integer boundary
                {
                    int complete = ((int)Math.floor(after))*10;
                    String msgProgress = I18NUtil.getMessage(MSG_RECOVERY_PROGRESS, complete);
                    logger.info(msgProgress);
                }
            }
            
            // have we finished?
            if (nextTxns.size() == 0)
            {
                // there are no more
                break;
            }
            lastTxn = nextTxns.get(nextTxns.size() - 1);
        }
        // done
        String msgDone = I18NUtil.getMessage(MSG_RECOVERY_COMPLETE);
        logger.info(msgDone);
    }
    
    /**
     * Perform a full reindexing of the given transaction in the context of a completely
     * new transaction.
     * 
     * @param txnId the transaction identifier
     */
    public void reindexTransaction(final long txnId)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Reindexing transaction: " + txnId);
        }
        
        TransactionWork<Object> reindexWork = new TransactionWork<Object>()
        {
            public Object doWork() throws Exception
            {
                // get the node references pertinent to the transaction
                List<NodeRef> nodeRefs = nodeDaoService.getTxnChanges(txnId);
                // reindex each node
                for (NodeRef nodeRef : nodeRefs)
                {
                    Status nodeStatus = nodeService.getNodeStatus(nodeRef);
                    if (nodeStatus == null)
                    {
                        // it's not there any more
                        continue;
                    }
                    if (nodeStatus.isDeleted())                                 // node deleted
                    {
                        // only the child node ref is relevant
                        ChildAssociationRef assocRef = new ChildAssociationRef(
                                ContentModel.ASSOC_CHILDREN,
                                null,
                                null,
                                nodeRef);
                      indexer.deleteNode(assocRef);
                    }
                    else                                                        // node created
                    {
                        // get the primary assoc for the node
                        ChildAssociationRef primaryAssocRef = nodeService.getPrimaryParent(nodeRef);
                        // reindex
                        indexer.createNode(primaryAssocRef);
                    }
                }
                // done
                return null;
            }
        };
        TransactionUtil.executeInNonPropagatingUserTransaction(transactionService, reindexWork, true);
        // done
    }
    
    private boolean isTxnIdPresentInIndex(StoreRef storeRef, Transaction txn)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Checking for transaction in index: \n" +
                    "   store: " + storeRef + "\n" +
                    "   txn: " + txn);
        }
        
        String changeTxnId = txn.getChangeTxnId();
        // count the changes in the transaction
        int updateCount = nodeDaoService.getTxnUpdateCountForStore(storeRef, txn.getId());
        int deleteCount = nodeDaoService.getTxnDeleteCountForStore(storeRef, txn.getId());
        if (logger.isDebugEnabled())
        {
            logger.debug("Transaction has " + updateCount + " updates and " + deleteCount + " deletes: " + txn);
        }
        
        // do the most update check, which is most common
        if (deleteCount == 0 && updateCount == 0)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("No changes in transaction: " + txn);
            }
            // there's nothing to check for
            return true;
        }
        else if (updateCount > 0)
        {
            ResultSet results = null;
            try
            {
                SearchParameters sp = new SearchParameters();
                sp.addStore(storeRef);
                // search for it in the index, sorting with youngest first, fetching only 1
                sp.setLanguage(SearchService.LANGUAGE_LUCENE);
                sp.setQuery("TX:" + LuceneQueryParser.escape(changeTxnId));
                sp.setLimit(1);
                
                results = searcher.query(sp);
                
                if (results.length() > 0)
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Index has results for txn (OK): " + txn);
                    }
                    return true;        // there were updates/creates and results for the txn were found
                }
                else
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Index has no results for txn (Index out of date): " + txn);
                    }
                    return false;
                }
            }
            finally
            {
                if (results != null) { results.close(); }
            }
        }
        // there have been deletes, so we have to ensure that none of the nodes deleted are present in the index
        // get all node refs for the transaction
        Long txnId = txn.getId();
        List<NodeRef> nodeRefs = nodeDaoService.getTxnChangesForStore(storeRef, txnId);
        for (NodeRef nodeRef : nodeRefs)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Searching for node in index: \n" +
                        "   node: " + nodeRef + "\n" +
                        "   txn: " + txn);
            }
            // we know that these are all deletions
            ResultSet results = null;
            try
            {
                SearchParameters sp = new SearchParameters();
                sp.addStore(storeRef);
                // search for it in the index, sorting with youngest first, fetching only 1
                sp.setLanguage(SearchService.LANGUAGE_LUCENE);
                sp.setQuery("ID:" + LuceneQueryParser.escape(nodeRef.toString()));
                sp.setLimit(1);
                
                results = searcher.query(sp);
                
                if (results.length() == 0)
                {
                    // no results, as expected
                    if (logger.isDebugEnabled())
                    {
                        logger.debug(" --> Node not found (OK)");
                    }
                    continue;
                }
                else
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug(" --> Node found (Index out of date)");
                    }
                    return false;
                }
            }
            finally
            {
                if (results != null) { results.close(); }
            }
        }
        
        // all tests passed
        if (logger.isDebugEnabled())
        {
            logger.debug("Index is in synch with transaction: " + txn);
        }
        return true;
    }
}