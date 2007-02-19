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
        if (currentTxnId < 0)
        {
            // initialize the starting point
            Transaction lastTxn = nodeDaoService.getLastTxn();
            if (lastTxn == null)
            {
                // there is nothing to do
                return;
            }
            long lastTxnId = lastTxn.getId();
            currentTxnId = getLastIndexedTxn(lastTxnId);
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
            }
        }
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