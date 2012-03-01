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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.alfresco.repo.domain.avm.AVMHistoryLinkEntity;
import org.alfresco.repo.domain.avm.AVMMergeLinkEntity;
import org.alfresco.repo.domain.permissions.Acl;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.JobLockService.JobLockRefreshCallback;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
            if (fRunning.get())
            {
                if (fgLogger.isDebugEnabled())
                {
                    fgLogger.debug("OrphanReaper is already running - just return");
                }
                
                return;
            }
            
            fRunning.set(true);
            
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
                if (fDone.get())
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
            while (fActive.get());
        }
        finally
        {
            synchronized (this)
            {
                fRunning.set(false);
                
                if (fgLogger.isTraceEnabled())
                {
                    fgLogger.trace("... finish running OrphanReaper");
                }
            }
        }
    }

    private Log fgLogger = LogFactory.getLog(OrphanReaper.class);

    private static final QName LOCK = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "OrphanReaper");
    private JobLockService jobLockService;

    /**
     * The Transaction Service
     */
    private TransactionService fTransactionService;
    
    /**
     * How many ms before refreshing the lock?
     */
    private long lockRefreshTime = 60000;
    
    /**
     * How long in ms to keep the lock in total before giving up, just in case there is a dead lock.
     */
    private long lockTimeOut = 3600000;

    /**
     * Active base sleep interval.
     */
    private long fActiveBaseSleep = 1000;

    /**
     * Batch size.
     */
    private int fBatchSize = 50;

    /**
     * Whether we are currently active, ie have work queued up.
     * Using Atomics so that the memory model is synchronized between threads.
     */
    private AtomicBoolean fActive = new AtomicBoolean(false);
    private AtomicBoolean fDone = new AtomicBoolean(false);
    private AtomicBoolean fRunning = new AtomicBoolean(false);
    
    /**
     * Create one with default parameters.
     */
    public OrphanReaper()
    {
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
     * @param jobLockService service used to ensure that reaper runs are not duplicated
     */
    public void setJobLockService(JobLockService jobLockService)
    {
        this.jobLockService = jobLockService;
    }

    public void setLockRefreshTime(long lockRefreshTime)
    {
        this.lockRefreshTime = lockRefreshTime;
    }

    public long getLockRefreshTime()
    {
        return lockRefreshTime;
    }
    
    public long getTimeToLive()
    {
        return getLockRefreshTime() * 2;
    }

    public void setLockTimeOut(long lockTimeOut)
    {
        this.lockTimeOut = lockTimeOut;
    }

    public long getLockTimeOut()
    {
        return lockTimeOut;
    }

    /**
     * Shutdown the reaper. This needs to be called when the application shuts down.
     */
    public void shutDown()
    {
        fDone.set(true);
    }

    /**
     * Attempts to get the lock. If the lock couldn't be taken, then <tt>null</tt> is returned.
     * 
     * @return Returns the lock token or <tt>null</tt>
     */
    private String getLock()
    {
        try
        {
            return jobLockService.getLock(LOCK, getTimeToLive());
        }
        catch (LockAcquisitionException e)
        {
            return null;
        }
    }

    /**
     * Creates a callback to refresh the lock if we are still doing work.
     * @param lockToken to refresh
     * @param lockHeld flag to indicate if the lock is needed/held.
     * @param start when processing started.
     */
    private void createLockRefreshCallback(final String lockToken, final AtomicBoolean lockHeld, final long start)
    {
        if (lockToken == null)
        {
            throw new IllegalArgumentException("Must provide existing lockToken");
        }
        
        JobLockRefreshCallback callback = new JobLockRefreshCallback()
        {
            @Override
            public boolean isActive()
            {
                boolean active = lockHeld.get();
                if (active)
                {
                    // Check for deadlock
                    if (System.currentTimeMillis() >= start + getLockTimeOut())
                    {
                        active = false;
                        lockHeld.set(false); // if not deadlocked this stop processing in the main thread
                        fgLogger.error("Lock held too long. Do we have a deadlock? Restart process.");
                    } 
                }
                return active;
            }

            @Override
            public void lockReleased()
            {
                lockHeld.set(false);
            }
        };

        jobLockService.refreshLock(lockToken, LOCK, getTimeToLive(), callback);
    }

    /**
     * This is really for debugging and testing. Allows another thread to mark the orphan reaper busy so that it can
     * monitor for it's being done.
     */
    public void activate()
    {
        fActive.set(true);
    }

    /**
     * See if the reaper is actively reaping.
     * 
     * @return Whether this is actively reaping.
     */
    public boolean isActive()
    {
        return fActive.get();
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
                final long start = System.currentTimeMillis();
                int reapCnt = 0;

                String lockToken = getLock();
                if (lockToken == null)
                {
                    fgLogger.info("Can't get lock. Assume multiple reapers ...");
                    fActive.set(false);
                    return null;
                }

                AtomicBoolean lockHeld = new AtomicBoolean(true);
                try
                {
                    // Creates a callback that refreshes the lock as long the code in this try block is
                    // still running. If the JVM crashes, the lock will time out. Just in case the lock
                    // still times out, we check at several points in processing and have an overall
                    // timeout in case of deadlock.
                    createLockRefreshCallback(lockToken, lockHeld, start);
                    if (fgLogger.isTraceEnabled())
                    {
                        fgLogger.trace("Orphan reaper doBatch: batchSize="+fBatchSize+", fActiveBaseSleep="+fActiveBaseSleep);
                    }
                
                    List<AVMNode> nodes = AVMDAOs.Instance().fAVMNodeDAO.getOrphans(fBatchSize);
                    if (nodes.size() == 0)
                    {
                        if (fgLogger.isTraceEnabled())
                        {
                            fgLogger.trace("Nothing to purge (set fActive = false)");
                        }
                        
                        fActive.set(false);
                        return null;
                    }
                    
                    if (!lockHeld.get())
                    {
                        throw new LockAcquisitionException("Lock lost. Finding orphans to reap.");
                    }
                    LinkedList<Long> fPurgeQueue = new LinkedList<Long>();
                    for (AVMNode node : nodes)
                    {
                        fPurgeQueue.add(node.getId());
                    }
                    
                    if (fgLogger.isDebugEnabled())
                    {
                        fgLogger.debug("Queue was empty so got more orphans from DB. Orphan queue size = "+fPurgeQueue.size());
                    }
                
                    fActive.set(true);
                
                    for (int i = 0; i < fBatchSize; i++)
                    {
                        if (fPurgeQueue.size() == 0)
                        {
                            if (fgLogger.isTraceEnabled())
                            {
                                fgLogger.trace("Purge queue is empty (fpurgeQueue size = "
                                        + fPurgeQueue.size() + ")");
                            }

                            fPurgeQueue = null;
                            break;
                        }

                        if (!lockHeld.get())
                    	{
                    	    throw new LockAcquisitionException("Lock lost. Orphan reap loop: "+i);
                    	}
                        Long nodeId = fPurgeQueue.removeFirst();
                        AVMNode node = AVMDAOs.Instance().fAVMNodeDAO.getByID(nodeId);

                        // Save away the ancestor and merged from fields from
                        // this node.

                        AVMNode ancestor = null;
                        AVMHistoryLinkEntity hlEntity = AVMDAOs.Instance().newAVMNodeLinksDAO
                                .getHistoryLinkByDescendent(node.getId());
                        if (hlEntity != null)
                        {
                            ancestor = AVMDAOs.Instance().fAVMNodeDAO.getByID(hlEntity
                                    .getAncestorNodeId());
                            AVMDAOs.Instance().newAVMNodeLinksDAO.deleteHistoryLink(
                                    hlEntity.getAncestorNodeId(), hlEntity.getDescendentNodeId());
                        }

                        AVMNode mergedFrom = null;
                        AVMMergeLinkEntity mlEntity = AVMDAOs.Instance().newAVMNodeLinksDAO
                                .getMergeLinkByTo(node.getId());
                        if (mlEntity != null)
                        {
                            mergedFrom = AVMDAOs.Instance().fAVMNodeDAO.getByID(mlEntity
                                    .getMergeFromNodeId());
                            AVMDAOs.Instance().newAVMNodeLinksDAO.deleteMergeLink(
                                    mlEntity.getMergeFromNodeId(), mlEntity.getMergeToNodeId());
                        }

                        // Get all the nodes that have this node as ancestor.
                        List<AVMHistoryLinkEntity> hlEntities = AVMDAOs.Instance().newAVMNodeLinksDAO
                                .getHistoryLinksByAncestor(node.getId());
                        for (AVMHistoryLinkEntity link : hlEntities)
                        {
                            AVMNode desc = AVMDAOs.Instance().fAVMNodeDAO.getByID(link
                                    .getDescendentNodeId());
                            if (desc != null)
                            {
                                desc.setAncestor(ancestor);
                                if (desc.getMergedFrom() == null)
                                {
                                    desc.setMergedFrom(mergedFrom);
                                }
                            }
                            AVMDAOs.Instance().newAVMNodeLinksDAO.deleteHistoryLink(
                                    link.getAncestorNodeId(), link.getDescendentNodeId());
                        }
                        // Get all the nodes that have this node as mergedFrom
                        List<AVMMergeLinkEntity> mlEntities = AVMDAOs.Instance().newAVMNodeLinksDAO
                                .getMergeLinksByFrom(node.getId());
                        for (AVMMergeLinkEntity link : mlEntities)
                        {
                            AVMNode mto = AVMDAOs.Instance().fAVMNodeDAO.getByID(link
                                    .getMergeToNodeId());
                            if (mto != null)
                            {
                                mto.setMergedFrom(ancestor);
                            }
                            AVMDAOs.Instance().newAVMNodeLinksDAO.deleteMergeLink(
                                    link.getMergeFromNodeId(), link.getMergeToNodeId());
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
                        if (node.getType() == AVMNodeType.PLAIN_DIRECTORY
                                || node.getType() == AVMNodeType.LAYERED_DIRECTORY)
                        {
                            // First get rid of all child entries for the node.
                            AVMDAOs.Instance().fChildEntryDAO.deleteByParent(node);
                        }
                        else if (node.getType() == AVMNodeType.PLAIN_FILE)
                        {
                            PlainFileNode file = (PlainFileNode) node;
                            if (file.isLegacyContentData())
                            {
                                // We quickly convert the old ContentData to the
                                // new storage
                                ContentData contentData = file.getContentData();
                                file.setContentData(contentData);
                            }
                            Long contentDataId = file.getContentDataId();
                            if (contentDataId != null)
                            {
                                // The ContentDataDAO will take care of
                                // dereferencing and cleanup
                                AVMDAOs.Instance().contentDataDAO.deleteContentData(contentDataId);
                            }
                        }

                        // Finally, delete it
                        AVMDAOs.Instance().fAVMNodeDAO.delete(node);

                        if (fgLogger.isTraceEnabled())
                        {
                            fgLogger.trace("Deleted Node [" + node.getId() + "]");
                        }

                        reapCnt++;
                    }
                    // Check we still have the lock at the end
                    if (!lockHeld.get())
                    {
                        throw new LockAcquisitionException("Lock lost at the end of processing");
                    }
                }
                finally
                {
                    lockHeld.set(false);
                    jobLockService.releaseLock(lockToken, LOCK);
                    
                    if (fgLogger.isDebugEnabled())
                    {
                        fgLogger.debug("Reaped "+reapCnt+" nodes in "+(System.currentTimeMillis()-start)+" ms");
                    }
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
            fgLogger.warn("Garbage collector error. Restarting process", e);
        }
    }
}
