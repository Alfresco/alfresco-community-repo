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