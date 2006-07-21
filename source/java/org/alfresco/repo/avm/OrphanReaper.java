/*
 * Copyright (C) 2006 Alfresco, Inc.
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

package org.alfresco.repo.avm;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is the background thread for reaping no longer referenced nodes
 * in the AVM repository.  These orphans arise from purge operations.
 * @author britt
 */
public class OrphanReaper implements Runnable
{
    private Log fgLogger = LogFactory.getLog(OrphanReaper.class);
    /**
     * The HibernateTxn instance.
     */
    private RetryingTransaction fTransaction;
    
    /**
     * Inactive base sleep interval.
     */
    private long fInactiveBaseSleep;
    
    /**
     * Active base sleep interval.
     */
    private long fActiveBaseSleep;
    
    /**
     * Batch size.
     */
    private int fBatchSize;
    
    /**
     * Whether we are currently active, ie have 
     * work queued up.
     */
    private boolean fActive;
    
    /**
     * Flag for shutting down this.
     */
    private boolean fDone;
    
    /**
     * The thread for this.
     */
    private Thread fThread;
    
    /**
     * Create one with default parameters.
     */
    public OrphanReaper()
    {
        fInactiveBaseSleep = 30000;
        fActiveBaseSleep = 1000;
        fBatchSize = 50;
        fActive = false;
        fDone = false;
    }
    
    // Setters for configuration.
    
    /**
     * Set the Inactive Base Sleep interval.
     * @param interval The interval to set in ms.
     */
    public void setInactiveBaseSleep(long interval)
    {
        fInactiveBaseSleep = interval;
    }
    
    /**
     * Set the active base sleep interval.
     * @param interval The interval to set in ms.
     */
    public void setActiveBaseSleep(long interval)
    {
        fActiveBaseSleep = interval;
    }
    
    /**
     * Set the batch size.
     * @param size The batch size to set.
     */
    public void setBatchSize(int size)
    {
        fBatchSize = size;
    }

    /**
     * Set the Hibernate Transaction Wrapper.
     * @param transaction
     */
    public void setRetryingTransaction(RetryingTransaction transaction)
    {
        fTransaction = transaction;
    }
    
    /**
     * Start things up after configuration is complete.
     */
    public void init()
    {
        fThread = new Thread(this);
        fThread.start();
    }

    /**
     * Shutdown the reaper. This needs to be called when 
     * the application shuts down.
     */
    public void shutDown()
    {
        fDone = true;
        try
        {
            fThread.join();
        }
        catch (InterruptedException ie)
        {
            // Do nothing.
        }
    }
    
    /**
     * Sit in a loop, periodically querying for orphans.  When orphans
     * are found, unhook them in bite sized batches.
     */
    public void run()
    {
        while (!fDone)
        {
            try
            {
                Thread.sleep(fActive ? fActiveBaseSleep : fInactiveBaseSleep);
            }
            catch (InterruptedException ie)
            {
                // Do nothing.
            }
            doBatch();
        }
    }
    
    /**
     * This is really for debugging and testing. Allows another thread to 
     * mark the orphan reaper busy so that it can monitor for it's being done.
     */
    public void activate()
    {
        fActive = true;
    }
    
    /**
     * See if the reaper is actively reaping.
     * @return Whether this is actively reaping.
     */
    public boolean isActive()
    {
        return fActive;
    }
        
    /**
     * Do a batch of cleanup work.
     */
    public void doBatch()
    {
        class TxnCallback implements RetryingTransactionCallback
        {
            public void perform()
            {
                List<AVMNode> nodes = AVMContext.fgInstance.fAVMNodeDAO.getOrphans(fBatchSize);
                if (nodes.size() == 0)
                {
                    fActive = false;
                    return;
                }
                fActive = true;
                for (AVMNode node : nodes)
                {
                    // Save away the ancestor and merged from fields from this node.
                    HistoryLink hlink = AVMContext.fgInstance.fHistoryLinkDAO.getByDescendent(node);
                    AVMNode ancestor = null;
                    if (hlink != null)
                    {
                        ancestor = hlink.getAncestor();
                        AVMContext.fgInstance.fHistoryLinkDAO.delete(hlink);
                    }
                    MergeLink mlink = AVMContext.fgInstance.fMergeLinkDAO.getByTo(node);
                    AVMNode mergedFrom = null;
                    if (mlink != null)
                    {
                        mergedFrom = mlink.getMfrom();
                        AVMContext.fgInstance.fMergeLinkDAO.delete(mlink);
                    }
                    AVMContext.fgInstance.fAVMNodeDAO.flush();
                    // Get all the nodes that have this node as ancestor.
                    List<HistoryLink> links = AVMContext.fgInstance.fHistoryLinkDAO.getByAncestor(node);
                    for (HistoryLink link : links)
                    {
                        AVMNode desc = link.getDescendent();
                        desc.setAncestor(ancestor);
                        if (desc.getMergedFrom() == null)
                        {
                            desc.setMergedFrom(mergedFrom);
                        }
                        AVMContext.fgInstance.fHistoryLinkDAO.delete(link);
                    }
                    // Get all the nodes that have this node as mergedFrom
                    List<MergeLink> mlinks = AVMContext.fgInstance.fMergeLinkDAO.getByFrom(node);
                    for (MergeLink link : mlinks)
                    {
                        link.getMto().setMergedFrom(ancestor);
                        AVMContext.fgInstance.fMergeLinkDAO.delete(link);
                    }
                    NewInAVMStore newInRep = AVMContext.fgInstance.fNewInAVMStoreDAO.getByNode(node);
                    if (newInRep != null)
                    {
                        AVMContext.fgInstance.fNewInAVMStoreDAO.delete(newInRep);
                    }
                    // Get rid of all properties belonging to this node.
                    AVMContext.fgInstance.fAVMNodePropertyDAO.deleteAll(node);
                    // Extra work for directories.
                    if (node.getType() == AVMNodeType.PLAIN_DIRECTORY ||
                        node.getType() == AVMNodeType.LAYERED_DIRECTORY)
                    {
                        // First get rid of all child entries for the node.
                        AVMContext.fgInstance.fChildEntryDAO.deleteByParent(node);
                        if (node.getType() == AVMNodeType.LAYERED_DIRECTORY)
                        {
                            // More special work for layered directories.
                            AVMContext.fgInstance.fDeletedChildDAO.deleteByParent(node);
                        } 
                        AVMContext.fgInstance.fAVMNodeDAO.delete(node);
                    }
                    else if (node.getType() == AVMNodeType.PLAIN_FILE)
                    {
                        AVMContext.fgInstance.fAVMNodeDAO.delete(node);
                        // FileContent should be purged if nobody else references it.
                        FileContent content = ((PlainFileNode)node).getContent();
                        if (content.getRefCount() == 1)
                        {
                            content.delete();
                            AVMContext.fgInstance.fFileContentDAO.delete(content);
                        }
                    }
                    else
                    {
                        AVMContext.fgInstance.fAVMNodeDAO.delete(node);
                    }
                }
            }
        }
        try
        {
            TxnCallback doit = new TxnCallback();
            fTransaction.perform(doit, true);
        }
        catch (Exception e)
        {
            fgLogger.error("Garbage collector error", e);
        }
    }
}
