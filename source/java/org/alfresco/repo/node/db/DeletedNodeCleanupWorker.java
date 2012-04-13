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

import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.node.cleanup.AbstractNodeCleanupWorker;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;

/**
 * Cleans up deleted nodes and dangling transactions that are old enough.
 * 
 * @author Derek Hulley
 * @since 2.2 SP2
 */
public class DeletedNodeCleanupWorker extends AbstractNodeCleanupWorker
{
    private long minPurgeAgeMs;
    
    // Unused transactions will be purged in chunks determined by commit time boundaries. 'index.tracking.purgeSize' specifies the size
    // of the chunk (in ms). Default is a couple of hours.
    private int purgeSize = 7200000; // ms

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
        if (minPurgeAgeMs < 0)
        {
            return Collections.singletonList("Minimum purge age is negative; purge disabled");
        }
        
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

    /**
     * Set the purge transaction block size. This determines how many unused transactions are purged in one go.
     * 
     * @param txnBlockSize
     */
    public void setPurgeSize(int purgeSize)
    {
		this.purgeSize = purgeSize;
	}

	/**
     * Cleans up deleted nodes that are older than the given minimum age.
     * 
     * @param minAge        the minimum age of a transaction or deleted node
     * @return              Returns log message results
     */
    private List<String> purgeOldDeletedNodes(long minAge)
    {
        final List<String> results = new ArrayList<String>(100);

        final long maxCommitTimeMs = System.currentTimeMillis() - minAge;
        
        RetryingTransactionCallback<Integer> purgeNodesCallback = new RetryingTransactionCallback<Integer>()
        {
            public Integer execute() throws Throwable
            {
                return nodeDAO.purgeNodes(maxCommitTimeMs);
            }
        };
        // TODO: Add error catching and decrement the maxCommitTimeMs to reduce DB resource usage
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
                    "   Max commit time: " + maxCommitTimeMs + "\n" +
                    "   Purge count:     " + purgeCount;
                results.add(msg);
            }
        }
        catch (Throwable e)
        {
            String msg = 
                "Failed to purge nodes." +
                "  If the purgable set is too large for the available DB resources \n" +
                "  then the nodes can be purged manually as well. \n" +
                "  Set log level to WARN for this class to get exception log: \n" +
                "   Max commit time: " + maxCommitTimeMs + "\n" +
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
        }
        // Done
        return results;
    }

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

        final long maxCommitTime = System.currentTimeMillis() - minAge;
    	long fromCommitTime = nodeDAO.getMinUnusedTxnCommitTime().longValue();

    	// delete unused transactions in batches of size 'purgeTxnBlockSize'
        while (true)
        {
            // Ensure we keep the lock
            refreshLock();

            RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
            txnHelper.setMaxRetries(5);                             // Limit number of retries
            txnHelper.setRetryWaitIncrementMs(1000);                // 1 second to allow other cleanups time to get through

            long toCommitTime = fromCommitTime + purgeSize;
            if(toCommitTime >= maxCommitTime)
            {
            	toCommitTime = maxCommitTime;
            }

            // Purge transactions
            try
            {
                DeleteTransactionsCallback purgeTxnsCallback = new DeleteTransactionsCallback(nodeDAO, fromCommitTime, toCommitTime);
                long purgeCount = txnHelper.doInTransaction(purgeTxnsCallback, false, true);
                if (purgeCount > 0)
                {
                    String msg =
                        "Purged old txns: \n" +
                        "   From commit time (ms):      " + fromCommitTime + "\n" +
                        "   To commit time (ms):      " + toCommitTime + "\n" +
                        "   Purge count:     " + purgeCount;
                    results.add(msg);
                }
            }
            catch (Throwable e)
            {
                String msg = 
                    "Failed to purge txns." +
                    "  Set log level to WARN for this class to get exception log: \n" +
                    "   From commit time:      " + fromCommitTime + "\n" +
                    "   To commit time (ms):   " + toCommitTime + "\n" +
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

        	fromCommitTime += purgeSize;
            if(fromCommitTime >= maxCommitTime)
            {
            	break;
            }
        }
        // Done
        return results;
    }
    
    /*
     * Delete a block of unused transactions
     */
    private static class DeleteTransactionsCallback implements RetryingTransactionCallback<Long>
    {
    	private NodeDAO nodeDAO;
    	private long fromCommitTime;
    	private long toCommitTime;

    	DeleteTransactionsCallback(NodeDAO nodeDAO, long fromCommitTime, long toCommitTime)
    	{
    		this.nodeDAO = nodeDAO;
    		this.fromCommitTime = fromCommitTime;
    		this.toCommitTime = toCommitTime;
    	}

        public Long execute() throws Throwable
        {
        	long count = nodeDAO.deleteTxnsUnused(fromCommitTime, toCommitTime);
        	return count;
        }    	
    }
}