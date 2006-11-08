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
import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.repo.transaction.TransactionUtil.TransactionWork;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeRef.Status;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Component to check and recover the indexes.  By default, the server is
 * put into read-only mode during the reindex process in order to prevent metadata changes.
 * This is not critical and can be {@link #setLockServer(boolean) switched off} if the
 * server is required immediately. 
 * 
 * @author Derek Hulley
 */
public class FullIndexRecoveryComponent extends AbstractReindexComponent
{
    private static final String ERR_INDEX_OUT_OF_DATE = "index.recovery.out_of_date";
    private static final String MSG_RECOVERY_STARTING = "index.recovery.starting";
    private static final String MSG_RECOVERY_COMPLETE = "index.recovery.complete";
    private static final String MSG_RECOVERY_PROGRESS = "index.recovery.progress";
    private static final String MSG_RECOVERY_TERMINATED = "index.recovery.terminated";
    
    private static Log logger = LogFactory.getLog(FullIndexRecoveryComponent.class);
    
    public static enum RecoveryMode
    {
        /** Do nothing - not even a check. */
        NONE,
        /**
         * Perform a quick check on the state of the indexes only.
         */
        VALIDATE,
        /**
         * Performs a validation and starts a quick recovery, if necessary.
         */
        AUTO,
        /**
         * Performs a full pass-through of all recorded transactions to ensure that the indexes
         * are up to date.
         */
        FULL;
    }
    
    private RecoveryMode recoveryMode;
    private boolean lockServer;
    
    public FullIndexRecoveryComponent()
    {
        recoveryMode = RecoveryMode.VALIDATE;
    }

    /**
     * Set the type of recovery to perform.  Default is {@link RecoveryMode#VALIDATE to validate}
     * the indexes only.
     * 
     * @param recoveryMode one of the {@link RecoveryMode } values
     */
    public void setRecoveryMode(String recoveryMode)
    {
        this.recoveryMode = RecoveryMode.valueOf(recoveryMode);
    }

    /**
     * Set this on to put the server into READ-ONLY mode for the duration of the index recovery.
     * The default is <tt>true</tt>, i.e. the server will be locked against further updates.
     * 
     * @param lockServer true to force the server to be read-only
     */
    public void setLockServer(boolean lockServer)
    {
        this.lockServer = lockServer;
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
            Transaction txn = nodeDaoService.getLastTxn();
            if (txn == null)
            {
                // no transactions - just bug out
                return;
            }
            long txnId = txn.getId();
            boolean txnInIndex = isTxnIdPresentInIndex(txnId);
            if (!txnInIndex)
            {
                String msg = I18NUtil.getMessage(ERR_INDEX_OUT_OF_DATE);
                logger.warn(msg);
                // this store isn't up to date
                if (recoveryMode == RecoveryMode.VALIDATE)
                {
                    // the store is out of date - validation failed
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
            if (lockServer)
            {
                // set the server into read-only mode
                transactionService.setAllowWrite(false);
            }
            
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
            long lastTxnId = (lastTxn == null) ? -1L : lastTxn.getId().longValue();
            List<Transaction> nextTxns = nodeDaoService.getNextTxns(
                    lastTxnId,
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
}