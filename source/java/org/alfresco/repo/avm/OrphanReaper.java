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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

package org.alfresco.repo.avm;

import java.sql.Savepoint;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.repo.domain.avm.AVMHistoryLinkEntity;
import org.alfresco.repo.domain.avm.AVMMergeLinkEntity;
import org.alfresco.repo.domain.control.ControlDAO;
import org.alfresco.repo.domain.permissions.Acl;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.ConcurrencyFailureException;

/**
 * This is the background thread for reaping no longer referenced nodes in the AVM repository. These orphans arise from
 * purge operations.
 * 
 * @author britt
 */
public class OrphanReaper
{
    public void execute()
    {
        synchronized (this)
        {
            if (fRunning)
            {
                if (fgLogger.isDebugEnabled())
                {
                    fgLogger.debug("OrphanReaper is already running - just return");
                }
                
                return;
            }
            
            fRunning = true;
            
            if (fgLogger.isTraceEnabled())
            {
                fgLogger.trace("Start running OrphanReaper ...");
            }
        }
        try
        {
            do
            {
                doBatch();
                if (fDone)
                {
                    if (fgLogger.isTraceEnabled())
                    {
                        fgLogger.trace("OrphanReaper is done - just return");
                    }
                    return;
                }
                try
                {
                    if (fgLogger.isTraceEnabled())
                    {
                        fgLogger.trace("OrphanReaper is not done - sleep for "+fActiveBaseSleep+" ms");
                    }
                    Thread.sleep(fActiveBaseSleep);
                }
                catch (InterruptedException e)
                {
                    fgLogger.warn("OrphanReaper was interrupted - do nothing: "+e);
                    // Do nothing.
                }
            }
            while (fActive);
        }
        finally
        {
            synchronized (this)
            {
                fRunning = false;
                
                if (fgLogger.isTraceEnabled())
                {
                    fgLogger.trace("... finish running OrphanReaper");
                }
            }
        }
    }

    private Log fgLogger = LogFactory.getLog(OrphanReaper.class);

    /**
     * The Transaction Service
     */
    private TransactionService fTransactionService;
    
    private ControlDAO controlDAO;

    /**
     * Active base sleep interval.
     */
    private long fActiveBaseSleep;

    /**
     * Batch size.
     */
    private int fBatchSize;

    /**
     * Whether we are currently active, ie have work queued up.
     */
    private boolean fActive;

    /**
     * The maximum length of the queue.
     */
    private int fQueueLength;

    /**
     * The linked list containing ids of nodes that are purgable.
     */
    private LinkedList<Long> fPurgeQueue;

    private boolean fDone = false;

    private boolean fRunning = false;

    /**
     * Create one with default parameters.
     */
    public OrphanReaper()
    {
        fActiveBaseSleep = 1000;
        fBatchSize = 50;
        fQueueLength = 1000;
        fActive = false;
    }

    // Setters for configuration.

    /**
     * Set the active base sleep interval.
     * 
     * @param interval
     *            The interval to set in ms.
     */
    public void setActiveBaseSleep(long interval)
    {
        fActiveBaseSleep = interval;
    }

    /**
     * Set the batch size.
     * 
     * @param size
     *            The batch size to set.
     */
    public void setBatchSize(int size)
    {
        fBatchSize = size;
    }

    /**
     * Set the transaction service.
     * 
     * @param transactionService
     *            The service.
     */
    public void setTransactionService(TransactionService transactionService)
    {
        fTransactionService = transactionService;
    }

    /**
     * Set the maximum size of the queue of purgeable nodes.
     * 
     * @param queueLength
     *            The max length.
     */
    public void setMaxQueueLength(int queueLength)
    {
        fQueueLength = queueLength;
    }
    
    public void setControlDAO(ControlDAO controlDAO)
    {
        this.controlDAO = controlDAO;
    }

    /**
     * Start things up after configuration is complete.
     */
    // public void init()
    // {
    // fThread = new Thread(this);
    // fThread.start();
    // }
    /**
     * Shutdown the reaper. This needs to be called when the application shuts down.
     */
    public void shutDown()
    {
        fDone = true;
    }

    /**
     * Sit in a loop, periodically querying for orphans. When orphans are found, unhook them in bite sized batches.
     */
    // public void run()
    // {
    // while (!fDone)
    // {
    // synchronized (this)
    // {
    // try
    // {
    // wait(fActive? fActiveBaseSleep : fInactiveBaseSleep);
    // }
    // catch (InterruptedException ie)
    // {
    // // Do nothing.
    // }
    // doBatch();
    // }
    // }
    // }
    /**
     * This is really for debugging and testing. Allows another thread to mark the orphan reaper busy so that it can
     * monitor for it's being done.
     */
    public void activate()
    {
        fActive = true;
    }

    /**
     * See if the reaper is actively reaping.
     * 
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
        class TxnWork implements RetryingTransactionCallback<Object>
        {
            public Object execute() throws Exception
            {
                if (fgLogger.isTraceEnabled())
                {
                    fgLogger.trace("Orphan reaper doBatch: batchSize="+fBatchSize+", maxQueueLength="+fQueueLength+", fActiveBaseSleep="+fActiveBaseSleep);
                }
                
                if (fPurgeQueue == null)
                {
                    List<AVMNode> nodes = AVMDAOs.Instance().fAVMNodeDAO.getOrphans(fQueueLength);
                    if (nodes.size() == 0)
                    {
                        if (fgLogger.isTraceEnabled())
                        {
                            fgLogger.trace("Nothing to purge (set fActive = false)");
                        }
                        
                        fActive = false;
                        return null;
                    }
                    
                    fPurgeQueue = new LinkedList<Long>();
                    for (AVMNode node : nodes)
                    {
                        fPurgeQueue.add(node.getId());
                    }
                    
                    if (fgLogger.isDebugEnabled())
                    {
                        fgLogger.debug("Queue was empty so got more orphans from DB. Orphan queue size = "+fPurgeQueue.size());
                    }
                }
                else
                {
                    if (fgLogger.isDebugEnabled())
                    {
                        fgLogger.debug("Queue was not empty. Orphan queue size = "+fPurgeQueue.size());
                    }
                }
                
                fActive = true;
                
                int reapCnt = 0;
                
                long start = System.currentTimeMillis();
                
                for (int i = 0; i < fBatchSize; i++)
                {
                    if (fPurgeQueue.size() == 0)
                    {
                        if (fgLogger.isTraceEnabled())
                        {
                            fgLogger.trace("Purge queue is empty (fpurgeQueue size = "+fPurgeQueue.size()+")");
                        }
                        
                        fPurgeQueue = null;
                        break;
                    }
                    
                    Long nodeId = fPurgeQueue.removeFirst();
                    AVMNode node = AVMDAOs.Instance().fAVMNodeDAO.getByID(nodeId);
                    if (node == null)
                    {
                        // eg. cluster, multiple reapers
                        
                        fgLogger.warn("Node ["+nodeId+"] not found - assume multiple reapers ...");
                        
                        continue;
                    }
                    
                    // Save away the ancestor and merged from fields from this node.
                    
                    AVMNode ancestor = null;
                    AVMHistoryLinkEntity hlEntity = AVMDAOs.Instance().newAVMNodeLinksDAO.getHistoryLinkByDescendent(node.getId());
                    if (hlEntity != null)
                    {
                        ancestor = AVMDAOs.Instance().fAVMNodeDAO.getByID(hlEntity.getAncestorNodeId());
                        AVMDAOs.Instance().newAVMNodeLinksDAO.deleteHistoryLink(hlEntity.getAncestorNodeId(), hlEntity.getDescendentNodeId());
                    }
                    
                    AVMNode mergedFrom = null;
                    AVMMergeLinkEntity mlEntity = AVMDAOs.Instance().newAVMNodeLinksDAO.getMergeLinkByTo(node.getId());
                    if (mlEntity != null)
                    {
                        mergedFrom = AVMDAOs.Instance().fAVMNodeDAO.getByID(mlEntity.getMergeFromNodeId());
                        AVMDAOs.Instance().newAVMNodeLinksDAO.deleteMergeLink(mlEntity.getMergeFromNodeId(), mlEntity.getMergeToNodeId());
                    }
                    
                    // Get all the nodes that have this node as ancestor.
                    List<AVMHistoryLinkEntity> hlEntities = AVMDAOs.Instance().newAVMNodeLinksDAO.getHistoryLinksByAncestor(node.getId());
                    for (AVMHistoryLinkEntity link : hlEntities)
                    {
                        AVMNode desc = AVMDAOs.Instance().fAVMNodeDAO.getByID(link.getDescendentNodeId());
                        if (desc != null)
                        {
                            desc.setAncestor(ancestor);
                            if (desc.getMergedFrom() == null)
                            {
                                desc.setMergedFrom(mergedFrom);
                            }
                        }
                        AVMDAOs.Instance().newAVMNodeLinksDAO.deleteHistoryLink(link.getAncestorNodeId(), link.getDescendentNodeId());
                    }
                    // Get all the nodes that have this node as mergedFrom
                    List<AVMMergeLinkEntity> mlEntities = AVMDAOs.Instance().newAVMNodeLinksDAO.getMergeLinksByFrom(node.getId());
                    for (AVMMergeLinkEntity link : mlEntities)
                    {
                        AVMNode mto = AVMDAOs.Instance().fAVMNodeDAO.getByID(link.getMergeToNodeId());
                        if (mto != null)
                        {
                            mto.setMergedFrom(ancestor);
                        }
                        AVMDAOs.Instance().newAVMNodeLinksDAO.deleteMergeLink(link.getMergeFromNodeId(), link.getMergeToNodeId());
                    }
                    
                    // Get rid of all properties belonging to this node.
                    AVMDAOs.Instance().fAVMNodeDAO.deleteProperties(node.getId());
                    
                    // Get rid of all aspects belonging to this node.
                    AVMDAOs.Instance().fAVMNodeDAO.deleteAspects(node.getId());
                    
                    // Get rid of ACL.
                    @SuppressWarnings("unused")
                    Acl acl = node.getAcl();
                    node.setAcl(null);
                    // Unused acls will be garbage collected
                    // Many acls will be shared
                    // Extra work for directories.
                    if (node.getType() == AVMNodeType.PLAIN_DIRECTORY || node.getType() == AVMNodeType.LAYERED_DIRECTORY)
                    {
                        // First get rid of all child entries for the node.
                        AVMDAOs.Instance().fChildEntryDAO.deleteByParent(node);
                    }
                    else if (node.getType() == AVMNodeType.PLAIN_FILE)
                    {
                        PlainFileNode file = (PlainFileNode)node;
                        if (file.isLegacyContentData())
                        {
                            // We quickly convert the old ContentData to the new storage
                            ContentData contentData = file.getContentData();
                            file.setContentData(contentData);
                        }
                        Long contentDataId = file.getContentDataId();
                        if (contentDataId != null)
                        {
                            // The ContentDataDAO will take care of dereferencing and cleanup
                            AVMDAOs.Instance().contentDataDAO.deleteContentData(contentDataId);
                        }
                    }
                    
                    Savepoint savepoint = controlDAO.createSavepoint("OrphanReaper");
                    
                    try
                    {
                        // Finally, delete it
                        AVMDAOs.Instance().fAVMNodeDAO.delete(node);
                        controlDAO.releaseSavepoint(savepoint);
                        
                        if (fgLogger.isTraceEnabled())
                        {
                            fgLogger.trace("Deleted Node ["+node.getId()+"]");
                        }
                    }
                    catch (ConcurrencyFailureException e)
                    {
                        // Since we are deleting the row, it doesn't matter
                        // if it is deleted here or not.
                        controlDAO.rollbackToSavepoint(savepoint);
                        
                        if (fgLogger.isTraceEnabled())
                        {
                            fgLogger.trace("Node already deleted ["+node.getId()+"]");
                        }
                    }
                    
                    reapCnt++;
                }
                
                if (fgLogger.isDebugEnabled())
                {
                    fgLogger.debug("Reaped "+reapCnt+" nodes in "+(System.currentTimeMillis()-start)+" msecs");
                }
                
                return null;
            }
        }
        try
        {
            if (!fTransactionService.isReadOnly())
            {
                fTransactionService.getRetryingTransactionHelper().doInTransaction(new TxnWork());
            }
        }
        catch (Exception e)
        {
            fgLogger.error("Garbage collector error", e);
        }
    }
}
