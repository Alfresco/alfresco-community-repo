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
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.domain.Transaction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Component to check and recover the indexes.
 * 
 * @author Derek Hulley
 */
public class IndexTransactionTracker extends AbstractReindexComponent
{
    private static Log logger = LogFactory.getLog(IndexTransactionTracker.class);
    
    private long maxTxnDurationMs;
    private long reindexLagMs;
    private int maxRecordSetSize;
    
    private boolean started;
    private List<Long> previousTxnIds;
    private long lastMaxTxnId;
    private long fromTimeInclusive;
    private Map<Long, TxnRecord> voids;
    
    /**
     * Set the defaults.
     * <ul>
     *   <li><b>Maximum transaction duration:</b> 1 hour</li>
     *   <li><b>Reindex lag:</b> 1 second</li>
     *   <li><b>Maximum recordset size:</b> 1000</li>
     * </ul>
     */
    public IndexTransactionTracker()
    {
        maxTxnDurationMs = 3600L * 1000L;
        reindexLagMs = 1000L;
        maxRecordSetSize = 1000;
        previousTxnIds = Collections.<Long>emptyList();
        lastMaxTxnId = Long.MAX_VALUE;
        fromTimeInclusive = -1L;
        voids = new TreeMap<Long, TxnRecord>();
    }

    /**
     * Set the expected maximum duration of transaction supported.  This value is used to adjust the
     * look-back used to detect transactions that committed.  Values must be greater than zero.
     * 
     * @param maxTxnDurationMinutes     the maximum length of time a transaction will take in minutes
     * 
     * @since 1.4.5, 2.0.5, 2.1.1
     */
    public void setMaxTxnDurationMinutes(long maxTxnDurationMinutes)
    {
        if (maxTxnDurationMinutes < 1)
        {
            throw new AlfrescoRuntimeException("Maximum transaction duration must be at least one minute.");
        }
        this.maxTxnDurationMs = maxTxnDurationMinutes * 60L * 1000L;
    }

    /**
     * Transaction tracking should lag by the average commit time for a transaction.  This will minimize
     * the number of holes in the transaction sequence.  Values must be greater than zero.
     * 
     * @param reindexLagMs              the minimum age of a transaction to be considered by
     *                                  the index transaction tracking
     * 
     * @since 1.4.5, 2.0.5, 2.1.1
     */
    public void setReindexLagMs(long reindexLagMs)
    {
        if (reindexLagMs < 1)
        {
            throw new AlfrescoRuntimeException("Reindex lag must be at least 1 millisecond.");
        }
        this.reindexLagMs = reindexLagMs;
    }

    /**
     * Set the number of transactions to request per query.
     */
    public void setMaxRecordSetSize(int maxRecordSetSize)
    {
        this.maxRecordSetSize = maxRecordSetSize;
    }

    @Override
    protected void reindexImpl()
    {
        if (!started)
        {
            // Make sure that we start clean
            voids.clear();
            previousTxnIds = new ArrayList<Long>(maxRecordSetSize);
            lastMaxTxnId = Long.MAX_VALUE;                              // So that it is ignored at first
            fromTimeInclusive = getStartingTxnCommitTime();
            started = true;
        }
        
        while (true)
        {
            long toTimeExclusive = System.currentTimeMillis() - reindexLagMs;
            
            // Check that the voids haven't been filled
            fromTimeInclusive = checkVoids(fromTimeInclusive);
            
            // get next transactions to index
            List<Transaction> txns = getNextTransactions(fromTimeInclusive, toTimeExclusive, previousTxnIds);
            
            if (logger.isDebugEnabled())
            {
                String msg = String.format(
                        "Reindexing %d transactions from %s (%s)",
                        txns.size(),
                        (new Date(fromTimeInclusive)).toString(),
                        txns.isEmpty() ? "---" : txns.get(0).getId().toString());
                logger.debug(msg);
            }
            
            // Reindex the transactions.  Voids between the last set of transactions and this
            // set will be detected as well.  Additionally, the last max transaction will be
            // updated by this method.
            reindexTransactions(txns);
            
            // Move the time on.
            // Note the subtraction here.  Yes, it's odd.  But the results of the getNextTransactions
            // may be limited by recordset size and it is possible to have multiple transactions share
            // the same commit time.  If these txns get split up and we exclude the time period, then
            // they won't be requeried.  The list of previously used transaction IDs is passed back to
            // be exluded from the next query.
            fromTimeInclusive = toTimeExclusive - 1L;
            previousTxnIds.clear();
            for (Transaction txn : txns)
            {
                previousTxnIds.add(txn.getId());
            }
            
            // Break out if there were no transactions processed
            if (previousTxnIds.isEmpty())
            {
                break;
            }

            // break out if the VM is shutting down
            if (isShuttingDown())
            {
                break;
            }
        }
    }
    
    /**
     * Find a transaction time to start indexing from (inclusive).  The last recorded transaction by ID
     * is taken and the max transaction duration substracted from its commit time.  A transaction is
     * retrieved for this time and checked for indexing.  If it is present, then that value is chosen.
     * If not, a step back in time is taken again.  This goes on until there are no more transactions
     * or a transaction is found in the index.
     */
    protected long getStartingTxnCommitTime()
    {
        // Look back in time by the maximum transaction duration
        long toTimeExclusive = System.currentTimeMillis() - maxTxnDurationMs;
        long fromTimeInclusive = 0L;
        double stepFactor = 1.0D;
found:
        while (true)
        {
            // Get the most recent transaction before the given look-back
            List<Transaction> nextTransactions = nodeDaoService.getTxnsByCommitTimeDescending(
                    0L,
                    toTimeExclusive,
                    1,
                    null);
            // There are no transactions in that time range
            if (nextTransactions.size() == 0)
            {
                break found;
            }
            // We found a transaction
            Transaction txn = nextTransactions.get(0);
            Long txnId = txn.getId();
            long txnCommitTime = txn.getCommitTimeMs();
            // Check that it is in the index
            InIndex txnInIndex = isTxnIdPresentInIndex(txnId);
            switch (txnInIndex)
            {
                case YES:
                    fromTimeInclusive = txnCommitTime;
                    break found;
                default:
                    // Look further back in time.  Step back by the maximum transaction duration and
                    // increase this step back by a factor of 10% each iteration.
                    toTimeExclusive = txnCommitTime - (long)(maxTxnDurationMs * stepFactor);
                    stepFactor *= 1.1D;
                    continue;
            }
        }
        // We have a starting value
        return fromTimeInclusive;
    }
    
    /**
     * Voids - otherwise known as 'holes' - in the transaction sequence are timestamped when they are
     * discovered.  This method discards voids that were timestamped before the given date.  It checks
     * all remaining voids, passing back the transaction time for the newly-filled void.  Otherwise
     * the value passed in is passed back.
     * 
     * @param fromTimeInclusive     the oldest void to consider
     * @return                      Returns an adjused start position based on any voids being filled
     */
    private long checkVoids(long fromTimeInclusive)
    {
        long maxHistoricalTime = (fromTimeInclusive - maxTxnDurationMs);
        long fromTimeAdjusted = fromTimeInclusive;
        
        List<Long> toExpireTxnIds = new ArrayList<Long>(1);
        // The voids are stored in a sorted map, sorted by the txn ID
        for (Long voidTxnId : voids.keySet())
        {
            TxnRecord voidTxnRecord = voids.get(voidTxnId);
            // Is the transaction around, yet?
            Transaction voidTxn = nodeDaoService.getTxnById(voidTxnId);
            if (voidTxn == null)
            {
                // It's still just a void.  Shall we expire it?
                if (voidTxnRecord.txnCommitTime < maxHistoricalTime)
                {
                    // It's too late for this void
                    toExpireTxnIds.add(voidTxnId);
                }
                continue;
            }
            else
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Void has become live: " + voidTxn);
                }
                // We found one that has become a real transaction.
                // We don't throw the other voids away.
                fromTimeAdjusted = voidTxn.getCommitTimeMs();
                // Break out as sequential rebuilding is required
                break;
            }
        }
        // Throw away all the expired ones
        for (Long toExpireTxnId : toExpireTxnIds)
        {
            voids.remove(toExpireTxnId);
        }
        // Done
        return fromTimeAdjusted;
    }
    
    private List<Transaction> getNextTransactions(long fromTimeInclusive, long toTimeExclusive, List<Long> previousTxnIds)
    {
        List<Transaction> txns = nodeDaoService.getTxnsByCommitTimeAscending(
                fromTimeInclusive,
                toTimeExclusive,
                maxRecordSetSize,
                previousTxnIds);
        // done
        return txns;
    }
    
    /**
     * Checks that each of the transactions is present in the index.  As soon as one is found that
     * isn't, all the following transactions will be reindexed.  After the reindexing, the sequence
     * of transaction IDs will be examined for any voids.  These will be recorded.
     * 
     * @param txns      transactions ordered by time ascending
     * @return          returns the 
     */
    private void reindexTransactions(List<Transaction> txns)
    {
        if (txns.isEmpty())
        {
            return;
        }
        
        Set<Long> processedTxnIds = new HashSet<Long>(13);
            
        boolean forceReindex = false;
        long minNewTxnId = Long.MAX_VALUE;
        long maxNewTxnId = Long.MIN_VALUE;
        long maxNewTxnCommitTime = System.currentTimeMillis();
        for (Transaction txn : txns)
        {
            Long txnId = txn.getId();
            long txnIdLong = txnId.longValue();
            if (txnIdLong < minNewTxnId)
            {
                minNewTxnId = txnIdLong;
            }
            if (txnIdLong > maxNewTxnId)
            {
                maxNewTxnId = txnIdLong;
                maxNewTxnCommitTime = txn.getCommitTimeMs();
            }
            // Keep track of it for void checking
            processedTxnIds.add(txnId);
            // Remove this entry from the void list - it is not void
            voids.remove(txnId);
            
            // Reindex the transaction if we are forcing it or if it isn't in the index already
            if (forceReindex || isTxnIdPresentInIndex(txnId) == InIndex.NO)
            {
                // Any indexing means that all the next transactions have to be indexed
                forceReindex = true;
                try
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Reindexing transaction: " + txn);
                    }
                    // We try the reindex, but for the sake of continuity, have to let it run on
                    reindexTransaction(txnId);
                }
                catch (Throwable e)
                {
                    logger.warn("\n" +
                            "Reindex of transaction failed: \n" +
                            "   Transaction ID: " + txnId + "\n" +
                            "   Error: " + e.getMessage(),
                            e);
                }
            }
            else
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Reindex skipping transaction: " + txn);
                }
            }
        }
        // We have to search for voids now.  Don't start at the min transaction,
        // but start at the least of the lastMaxTxnId and minNewTxnId
        long voidCheckStartTxnId = (lastMaxTxnId < minNewTxnId ? lastMaxTxnId : minNewTxnId) + 1;
        long voidCheckEndTxnId = maxNewTxnId;
        // Check for voids in new transactions
        for (long i = voidCheckStartTxnId; i <= voidCheckEndTxnId; i++)
        {
            Long txnId = Long.valueOf(i);
            if (processedTxnIds.contains(txnId))
            {
                // It is there
                continue;
            }
            
            // First make sure that it is a real void.  Sometimes, transactions are in the table but don't
            // fall within the commit time window that we queried.  If they're in the DB AND in the index,
            // then they're not really voids and don't need further checks.  If they're missing from either,
            // then they're voids and must be processed.
            Transaction voidTxn = nodeDaoService.getTxnById(txnId);
            if (voidTxn != null && isTxnIdPresentInIndex(txnId) != InIndex.NO)
            {
                // It is a real transaction (not a void) and is already in the index, so just ignore it.
                continue;
            }
            
            // Calculate an age for the void.  We can't use the current time as that will mean we keep all
            // discovered voids, even if they are very old.  Rather, we use the commit time of the last transaction
            // in the set as it represents the query time for this iteration.
            TxnRecord voidRecord = new TxnRecord();
            voidRecord.txnCommitTime = maxNewTxnCommitTime;
            voids.put(txnId, voidRecord);
            if (logger.isDebugEnabled())
            {
                logger.debug("Void detected: " + txnId);
            }
        }
        // Having searched for the nodes, we've recorded all the voids.  So move the lastMaxTxnId up.
        lastMaxTxnId = voidCheckEndTxnId;
    }
    
    private class TxnRecord
    {
        private long txnCommitTime;
    }
}