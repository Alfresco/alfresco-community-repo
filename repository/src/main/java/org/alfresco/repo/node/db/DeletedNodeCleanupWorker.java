/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.node.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    // used for tests, to consider only transactions after a certain commit time
    private long fromCustomCommitTime = -1;
    
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
        long fromCommitTime = fromCustomCommitTime;
        long startTime = System.currentTimeMillis();
        if (fromCommitTime <= 0L)
        {
            fromCommitTime = nodeDAO.getMinTxnCommitTimeForDeletedNodes().longValue();
        }

        logger.debug("DeletedNodeCleanupWorker: nodeDAO.getMinTxnCommitTimeForDeletedNodes - execution time:"+ getFormattedExecutionTime(startTime));

        logger.debug("DeletedNodeCleanupWorker: About to execute the clean up nodes ");
        startTime = System.currentTimeMillis();
        List<String> purgedNodes = purgeOldDeletedNodes(minPurgeAgeMs, fromCommitTime);
        logger.debug("DeletedNodeCleanupWorker: purgeOldDeletedNodes - total Time:"+ getFormattedExecutionTime(startTime));

        logger.debug("DeletedNodeCleanupWorker: About to execute the clean up txns ");
        startTime = System.currentTimeMillis();
        List<String> purgedTxns = purgeOldEmptyTransactions(minPurgeAgeMs, fromCommitTime);
        logger.debug("DeletedNodeCleanupWorker: purgeOldEmptyTransactions  -total Time:"+ getFormattedExecutionTime(startTime));

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
     * Set a custom "from commit time" that will consider only the transactions after this specified time
     * Setting a negative value or 0 will trigger the default behaviour to get the oldest "from time" for any deleted node
     *
     * @param fromCustomCommitTime the custom from commit time value
     */
    public void setFromCustomCommitTime(long fromCustomCommitTime)
    {
        this.fromCustomCommitTime = fromCustomCommitTime;
    }
    /**
     * Set the purge transaction block size. This determines how many unused transactions are purged in one go.
     * 
     * @param purgeSize int
     */
    public void setPurgeSize(int purgeSize)
    {
        this.purgeSize = purgeSize;
    }

	/**
     * Cleans up deleted nodes that are older than the given minimum age.
     * 
     * @param minAge        the minimum age of a transaction or deleted node
     * @param fromCommitTime  the minimum commit time for deleted nodes
     * @return              Returns log message results
     */
    private List<String> purgeOldDeletedNodes(long minAge, long fromCommitTime)
    {
        final List<String> results = new ArrayList<String>(100);

        final long maxCommitTime = System.currentTimeMillis() - minAge;

        if ( fromCommitTime == 0L )
        {
              String msg = "There are no old nodes to purge.";
              results.add(msg);
              return results;
        }
        
        long loopPurgeSize = purgeSize;
        Long purgeCount = 0l;
        logger.debug("DeletedNodeCleanupWorker: purgeOldDeletedNodes started ");
        while (true)
        {
            // Ensure we keep the lock
            refreshLock();
            
            RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
            txnHelper.setMaxRetries(5);                             // Limit number of retries
            txnHelper.setRetryWaitIncrementMs(1000);                // 1 second to allow other cleanups time to get through

            long toCommitTime = fromCommitTime + loopPurgeSize;
            if(toCommitTime > maxCommitTime)
            {
                toCommitTime = maxCommitTime;
            }
            
            try
            {
                DeleteNodesByTransactionsCallback purgeNodesCallback = new DeleteNodesByTransactionsCallback(nodeDAO, fromCommitTime, toCommitTime);
                purgeCount = txnHelper.doInTransaction(purgeNodesCallback, false, true);

                if (purgeCount.longValue() > 0)
                {
                    String msg =
                        "Purged old nodes: \n" +
                        "   From commit time (ms):    " + fromCommitTime + "\n" +
                        "   To commit time (ms):      " + toCommitTime + "\n" +
                        "   Purge count:     " + purgeCount;
                    results.add(msg);
                }

                fromCommitTime += loopPurgeSize;
                
                // If the delete succeeded, double the loopPurgeSize
                loopPurgeSize *= 2L;
                if (loopPurgeSize > purgeSize)
                {
                    loopPurgeSize = purgeSize;
                }
            }
            catch (Throwable e)
            {
                String msg = 
                    "Failed to purge nodes. \n" +
                    "  If the purgable set is too large for the available DB resources \n" +
                    "  then the nodes can be purged manually as well. \n" +
                    "  Set log level to WARN for this class to get exception log: \n" +
                    "   From commit time (ms):    " + fromCommitTime + "\n" +
                    "   To commit time (ms):      " + toCommitTime + "\n" +
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
                
                // If delete failed, halve the loopPurgeSize and try again
                loopPurgeSize /= 2L;
                // If the purge size drops below 10% of the original size, the entire process must stop
                if (loopPurgeSize < 0.1 * purgeSize)
                {
                    msg ="Failed to purge nodes. \n" +
                         " The purge time interval dropped below 10% of the original size (" + purgeSize + "), so the purging process was stopped.";
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
            }
                      
            if(fromCommitTime >= maxCommitTime)
            {
                break;
            }
        }

        logger.debug("DeletedNodeCleanupWorker: purgeOldDeletedNodes finished ");
        // Done
        return results;
    }

    /**
     * Cleans up unused transactions that are older than the given minimum age.
     * 
     * @param minAge        the minimum age of a transaction or deleted node
     * @param fromCommitTime the commit time of the oldest unused transaction of deleted node
     * @return              Returns log message results
     */
    private List<String> purgeOldEmptyTransactions(long minAge, long fromCommitTime)
    {
        if (minAge < 0)
        {
            return Collections.emptyList();
        }
        final List<String> results = new ArrayList<String>(100);

        final long maxCommitTime = System.currentTimeMillis() - minAge;
        logger.debug("DeletedNodeCleanupWorker: purgeOldEmptyTransactions started ");
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
                        "   From commit time (ms):    " + fromCommitTime + "\n" +
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
        logger.debug("DeletedNodeCleanupWorker: purgeOldEmptyTransactions finished ");
        // Done
        return results;
    }
    
    private static abstract class DeleteByTransactionsCallback implements RetryingTransactionCallback<Long>
    {
        protected NodeDAO nodeDAO;
        protected long fromCommitTime;
        protected long toCommitTime;

        DeleteByTransactionsCallback(NodeDAO nodeDAO, long fromCommitTime, long toCommitTime)
        {
            this.nodeDAO = nodeDAO;
            this.fromCommitTime = fromCommitTime;
            this.toCommitTime = toCommitTime;
        }

        public abstract Long execute() throws Throwable;
    }
    
    /*
     * Delete a block of unused transactions
     */
    private static class DeleteTransactionsCallback extends DeleteByTransactionsCallback
    {
        DeleteTransactionsCallback(NodeDAO nodeDAO, long fromCommitTime, long toCommitTime)
        {
            super(nodeDAO, fromCommitTime, toCommitTime);
        }

        public Long execute() throws Throwable
        {
            long count = nodeDAO.deleteTxnsUnused(fromCommitTime, toCommitTime);
            return count;
        }       
    }
    
    /*
     * Purge a block of deleted nodes and their properties
     */
    private static class DeleteNodesByTransactionsCallback extends DeleteByTransactionsCallback
    {
        DeleteNodesByTransactionsCallback(NodeDAO nodeDAO, long fromCommitTime, long toCommitTime)
        {
            super(nodeDAO, fromCommitTime, toCommitTime);
        }

        public Long execute() throws Throwable
        {
            long count = nodeDAO.purgeNodes(fromCommitTime, toCommitTime);
            return count;
        }       
    }

    private String getFormattedExecutionTime(long startTimeInMillis)
    {
        long endTime = System.currentTimeMillis();
        long totalTimeInMillis = endTime - startTimeInMillis;
        final long hr = TimeUnit.MILLISECONDS.toHours(totalTimeInMillis)
                    - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(totalTimeInMillis));
        final long min = TimeUnit.MILLISECONDS.toMinutes(totalTimeInMillis)
                    - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(totalTimeInMillis));
        final long sec = TimeUnit.MILLISECONDS.toSeconds(totalTimeInMillis)
                    - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(totalTimeInMillis));
        final long ms = TimeUnit.MILLISECONDS.toMillis(totalTimeInMillis)
                    - TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(totalTimeInMillis));

        return String.format("%d Hours %d Minutes %d Seconds %d Milliseconds", hr, min, sec, ms);

    }
}