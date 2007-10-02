/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.node.index;

import java.util.List;

import org.alfresco.repo.domain.Transaction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Component to check and recover the indexes.
 * 
 * @author Derek Hulley
 */
public class IndexRemoteTransactionTracker extends AbstractReindexComponent
{
    private static Log logger = LogFactory.getLog(IndexRemoteTransactionTracker.class);
    
    private boolean remoteOnly;
    private boolean started;
    private long currentTxnId;
    
    public IndexRemoteTransactionTracker()
    {
        remoteOnly = true;
        currentTxnId = -1L;
    }

    /**
     * Set whether or not this component should only track remote transactions.
     * By default, it is <tt>true</tt>, but under certain test conditions, it may
     * be desirable to track local transactions too; e.g. during testing of clustering
     * when running multiple instances on the same machine.
     * 
     * @param remoteOnly <tt>true</tt> to reindex only those transactions that were
     *      committed to the database by a remote server.
     */
    public void setRemoteOnly(boolean remoteOnly)
    {
        this.remoteOnly = remoteOnly;
    }



    @Override
    protected void reindexImpl()
    {
        if (!started)
        {
            // Initialize the starting poing
            currentTxnId = getLastIndexedTxn();
            started = true;
        }
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Performing index tracking from txn " + currentTxnId);
        }
        
        while (true)
        {
            // get next transactions to index
            List<Transaction> txns = getNextTransactions(currentTxnId);
            if (txns.size() == 0)
            {
                // we've caught up
                break;
            }
            // break out if the VM is shutting down
            if (isShuttingDown())
            {
                break;
            }
            // reindex all "foreign" or "local" transactions, one at a time
            for (Transaction txn : txns)
            {
                long txnId = txn.getId();
                reindexTransaction(txnId);
                currentTxnId = txnId;
                // break out if the VM is shutting down
                if (isShuttingDown())
                {
                    break;
                }
            }
        }
    }
    
    private static final long DECREMENT_COUNT = 10L;
    /**
     * Finds the last indexed transaction.  It works backwards from the
     * last index in increments, respecting the {@link #setRemoteOnly(boolean) remoteOnly}
     * flag.
     * 
     * @return Returns the last index transaction or -1 if there is none
     */
    protected long getLastIndexedTxn()
    {
        // get the last transaction
        Transaction txn = null;
        if (remoteOnly)
        {
            txn = nodeDaoService.getLastRemoteTxn();
        }
        else
        {
            txn = nodeDaoService.getLastTxn();
        }
        if (txn == null)
        {
            // There is no last transaction to use
            return -1L;
        }
        long currentTxnId = txn.getId();
        while (currentTxnId >= 0L)
        {
            // Check if the current txn is in the index
            InIndex txnInIndex = isTxnIdPresentInIndex(currentTxnId);
            if (txnInIndex == InIndex.YES)
            {
                // We found somewhere to start
                break;
            }
            
            // Get back in time
            long lastCheckTxnId = currentTxnId;
            currentTxnId -= DECREMENT_COUNT;
            if (currentTxnId < 0L)
            {
                currentTxnId = -1L;
            }
            // We don't know if this number we have is a local or remote txn, so get the very next one
            Transaction nextTxn = null;
            if (remoteOnly)
            {
                List<Transaction> nextTxns = nodeDaoService.getNextRemoteTxns(currentTxnId, 1);
                if (nextTxns.size() > 0)
                {
                    nextTxn = nextTxns.get(0);
                }
            }
            else
            {
                List<Transaction> nextTxns = nodeDaoService.getNextTxns(currentTxnId, 1);
                if (nextTxns.size() > 0)
                {
                    nextTxn = nextTxns.get(0);
                }
            }
            if (nextTxn == null)
            {
                // There was nothing relevant after this, so keep going back in time
                continue;
            }
            else if (nextTxn.getId() >= lastCheckTxnId)
            {
                // Decrementing by DECREMENT_COUNT was not enough
                continue;
            }
            // Adjust the last one we looked at to reflect the correct txn id
            currentTxnId = nextTxn.getId();
        }
        // We are close enough to the beginning, so just go for the first transaction
        if (currentTxnId < 0L)
        {
            currentTxnId = -1L;
        }
        return currentTxnId;
    }
    
    private static final int MAX_TXN_COUNT = 1000;
    private List<Transaction> getNextTransactions(long currentTxnId)
    {
        List<Transaction> txns = null;
        if (remoteOnly)
        {
            txns = nodeDaoService.getNextRemoteTxns(currentTxnId, MAX_TXN_COUNT);
        }
        else
        {
            txns = nodeDaoService.getNextTxns(currentTxnId, MAX_TXN_COUNT);
        }
        // done
        return txns;
    }
}