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
package org.alfresco.repo.node.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.repo.domain.node.NodeDAO.NodeRefQueryCallback;
import org.alfresco.repo.node.cleanup.AbstractNodeCleanupWorker;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;
import org.apache.commons.lang.mutable.MutableLong;

/**
 * Cleans up deleted nodes and dangling transactions that are old enough.
 * 
 * @author Derek Hulley
 * @since 2.2 SP2
 */
public class DeletedNodeCleanupWorker extends AbstractNodeCleanupWorker
{
    private long minPurgeAgeMs;
    
    /**
     * Default constructor
     */
    public DeletedNodeCleanupWorker()
    {
        minPurgeAgeMs = 7L * 24L * 3600L * 1000L;
    }

    /**
     * {@inheritDoc}
     */
    protected List<String> doCleanInternal() throws Throwable
    {
      List<String> purgedNodes = purgeOldDeletedNodes(minPurgeAgeMs);
      List<String> purgedTxns = purgeOldEmptyTransactions(minPurgeAgeMs);
      
      List<String> allResults = new ArrayList<String>(100);
      allResults.addAll(purgedNodes);
      allResults.addAll(purgedTxns);
      
      // Done
      return allResults;
    }

    /**
     * Set the minimum age (days) that nodes and transactions must be before they get purged.
     * The default is 7 days.
     * 
     * @param minPurgeAgeDays           the minimum age (in days) before nodes and transactions get purged
     */
    public void setMinPurgeAgeDays(int minPurgeAgeDays)
    {
        this.minPurgeAgeMs = ((long) minPurgeAgeDays) * 24L * 3600L * 1000L;
    }

    private static final int NODE_PURGE_BATCH_SIZE = 1000;
    /**
     * Cleans up deleted nodes that are older than the given minimum age.
     * 
     * @param minAge        the minimum age of a transaction or deleted node
     * @return              Returns log message results
     */
    private List<String> purgeOldDeletedNodes(long minAge)
    {
        if (minAge < 0)
        {
            return Collections.emptyList();
        }
        final List<String> results = new ArrayList<String>(100);
        final MutableLong minNodeId = new MutableLong(0L);

        final long maxCommitTime = System.currentTimeMillis() - minAge;
        RetryingTransactionCallback<Integer> purgeNodesCallback = new RetryingTransactionCallback<Integer>()
        {
            public Integer execute() throws Throwable
            {
                final List<Pair<Long, NodeRef>> nodePairs = new ArrayList<Pair<Long, NodeRef>>(NODE_PURGE_BATCH_SIZE);
                NodeRefQueryCallback callback = new NodeRefQueryCallback()
                {
                    public boolean handle(Pair<Long, NodeRef> nodePair)
                    {
                        nodePairs.add(nodePair);
                        return true;
                    }
                };
                nodeDAO.getNodesDeletedInOldTxns(minNodeId.longValue(), maxCommitTime, NODE_PURGE_BATCH_SIZE, callback);
                for (Pair<Long, NodeRef> nodePair : nodePairs)
                {
                    Long nodeId = nodePair.getFirst();
                    nodeDAO.purgeNode(nodeId);
                    // Update the min node ID for the next query
                    if (nodeId.longValue() > minNodeId.longValue())
                    {
                        minNodeId.setValue(nodeId.longValue());
                    }
                }
                return nodePairs.size();
            }
        };
        while (true)
        {
            RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
            txnHelper.setMaxRetries(5);                             // Limit number of retries
            txnHelper.setRetryWaitIncrementMs(1000);                // 1 second to allow other cleanups time to get through
            // Get nodes to delete
            Integer purgeCount = new Integer(0);
            // Purge nodes
            try
            {
                purgeCount = txnHelper.doInTransaction(purgeNodesCallback, false, true);
                if (purgeCount.intValue() > 0)
                {
                    String msg =
                        "Purged old nodes: \n" +
                        "   Min node ID:     " + minNodeId.longValue() + "\n" +
                        "   Batch size:      " + NODE_PURGE_BATCH_SIZE + "\n" +
                        "   Max commit time: " + maxCommitTime + "\n" +
                        "   Purge count:     " + purgeCount;
                    results.add(msg);
                }
            }
            catch (Throwable e)
            {
                String msg = 
                    "Failed to purge nodes." +
                    "  Set log level to WARN for this class to get exception log: \n" +
                    "   Min node ID:     " + minNodeId.longValue() + "\n" +
                    "   Batch size:      " + NODE_PURGE_BATCH_SIZE + "\n" +
                    "   Max commit time: " + maxCommitTime + "\n" +
                    "   Error:       " + e.getMessage();
                // It failed; do a full log in WARN mode
                if (logger.isWarnEnabled())
                {
                    logger.warn(msg, e);
                }
                else
                {
                    logger.error(msg);
                }
                results.add(msg);
                break;
            }
            if (purgeCount.intValue() == 0)
            {
                break;
            }
        }
        // Done
        return results;
    }

    private static final int TXN_PURGE_BATCH_SIZE = 50;
    /**
     * Cleans up unused transactions that are older than the given minimum age.
     * 
     * @param minAge        the minimum age of a transaction or deleted node
     * @return              Returns log message results
     */
    private List<String> purgeOldEmptyTransactions(long minAge)
    {
        if (minAge < 0)
        {
            return Collections.emptyList();
        }
        final List<String> results = new ArrayList<String>(100);
        final MutableLong minTxnId = new MutableLong(0L);

        final long maxCommitTime = System.currentTimeMillis() - minAge;
        RetryingTransactionCallback<Integer> purgeTxnsCallback = new RetryingTransactionCallback<Integer>()
        {
            public Integer execute() throws Throwable
            {
                 final List<Long> txnIds = nodeDAO.getTxnsUnused(
                         minTxnId.longValue(),
                         maxCommitTime,
                         TXN_PURGE_BATCH_SIZE);
                for (Long txnId : txnIds)
                {
                    nodeDAO.purgeTxn(txnId);
                    // Update the min node ID for the next query
                    if (txnId.longValue() > minTxnId.longValue())
                    {
                        minTxnId.setValue(txnId.longValue());
                    }
                }
                return txnIds.size();
            }
        };
        while (true)
        {
            RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
            txnHelper.setMaxRetries(5);                             // Limit number of retries
            txnHelper.setRetryWaitIncrementMs(1000);                // 1 second to allow other cleanups time to get through
            // Get nodes to delete
            Integer purgeCount = new Integer(0);
            // Purge nodes
            try
            {
                purgeCount = txnHelper.doInTransaction(purgeTxnsCallback, false, true);
                if (purgeCount.intValue() > 0)
                {
                    String msg =
                        "Purged old txns: \n" +
                        "   Min txn ID:      " + minTxnId.longValue() + "\n" +
                        "   Batch size:      " + TXN_PURGE_BATCH_SIZE + "\n" +
                        "   Max commit time: " + maxCommitTime + "\n" +
                        "   Purge count:     " + purgeCount;
                    results.add(msg);
                }
            }
            catch (Throwable e)
            {
                String msg = 
                    "Failed to purge txns." +
                    "  Set log level to WARN for this class to get exception log: \n" +
                    "   Min txn ID:      " + minTxnId.longValue() + "\n" +
                    "   Batch size:      " + TXN_PURGE_BATCH_SIZE + "\n" +
                    "   Max commit time: " + maxCommitTime + "\n" +
                    "   Error:       " + e.getMessage();
                // It failed; do a full log in WARN mode
                if (logger.isWarnEnabled())
                {
                    logger.warn(msg, e);
                }
                else
                {
                    logger.error(msg);
                }
                results.add(msg);
                break;
            }
            if (purgeCount.intValue() == 0)
            {
                break;
            }
        }
        // Done
        return results;
    }
}