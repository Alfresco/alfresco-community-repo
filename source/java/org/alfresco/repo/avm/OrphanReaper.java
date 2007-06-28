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
 * http://www.alfresco.com/legal/licensing" */

package org.alfresco.repo.avm;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.repo.domain.DbAccessControlList;
import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.service.transaction.TransactionService;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * This is the background thread for reaping no longer referenced nodes
 * in the AVM repository.  These orphans arise from purge operations.
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
                return;
            }
            fRunning = true;
        }
        try
        {
            do
            {
                doBatch();
                if (fDone)
                {
                    return;
                }
                try
                {
                    Thread.sleep(fActiveBaseSleep);
                }
                catch (InterruptedException e)
                {
                    // Do nothing.
                }
            } while (fActive);
        }
        finally
        {
            synchronized (this)
            {
                fRunning = false;
            }
        }
    }

    private Logger fgLogger = Logger.getLogger(OrphanReaper.class);

    /**
     * The Transaction Service
     */
    private TransactionService fTransactionService;
    
    /**
     * The Session Factory
     */
    private SessionFactory fSessionFactory;
    
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
     * Set the transaction service.
     * @param transactionService The service.
     */
    public void setTransactionService(TransactionService transactionService)
    {
        fTransactionService = transactionService;
    }
    
    /**
     * Set the hibernate session factory. (For Spring.)
     * @param sessionFactory
     */
    public void setSessionFactory(SessionFactory sessionFactory)
    {
        fSessionFactory = sessionFactory;
    }
    
    /**
     * Set the maximum size of the queue of purgeable nodes.
     * @param queueLength The max length.
     */
    public void setMaxQueueLength(int queueLength)
    {
        fQueueLength = queueLength;
    }
    
    /**
     * Start things up after configuration is complete.
     */
//    public void init()
//    {
//        fThread = new Thread(this);
//        fThread.start();
//    }

    /**
     * Shutdown the reaper. This needs to be called when 
     * the application shuts down.
     */
    public void shutDown()
    {
        fDone = true;
    }
    
    /**
     * Sit in a loop, periodically querying for orphans.  When orphans
     * are found, unhook them in bite sized batches.
     */
//    public void run()
//    {
//        while (!fDone)
//        {
//            synchronized (this)
//            {
//                try
//                {
//                    wait(fActive? fActiveBaseSleep : fInactiveBaseSleep);
//                }
//                catch (InterruptedException ie)
//                {
//                    // Do nothing.
//                }
//                doBatch();
//            }
//        }
//    }
    
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
        class TxnWork implements TransactionUtil.TransactionWork<Object>
        {
            public Object doWork()
                throws Exception
            {
                if (fPurgeQueue == null)
                {
                    List<AVMNode> nodes = AVMDAOs.Instance().fAVMNodeDAO.getOrphans(fQueueLength);
                    if (nodes.size() == 0)
                    {
                        fActive = false;
                        return null;
                    }
                    fPurgeQueue = new LinkedList<Long>();
                    for (AVMNode node : nodes)
                    {
                        fPurgeQueue.add(node.getId());
                    }
                }
                fActive = true;
                for (int i = 0; i < fBatchSize; i++)
                {
                    if (fPurgeQueue.size() == 0)
                    {
                        fPurgeQueue = null;
                        return null;
                    }
                    AVMNode node = AVMDAOs.Instance().fAVMNodeDAO.getByID(fPurgeQueue.removeFirst());
                    // Save away the ancestor and merged from fields from this node.
                    HistoryLink hlink = AVMDAOs.Instance().fHistoryLinkDAO.getByDescendent(node);
                    AVMNode ancestor = null;
                    if (hlink != null)
                    {
                        ancestor = hlink.getAncestor();
                        AVMDAOs.Instance().fHistoryLinkDAO.delete(hlink);
                    }
                    MergeLink mlink = AVMDAOs.Instance().fMergeLinkDAO.getByTo(node);
                    AVMNode mergedFrom = null;
                    if (mlink != null)
                    {
                        mergedFrom = mlink.getMfrom();
                        AVMDAOs.Instance().fMergeLinkDAO.delete(mlink);
                    }
                    AVMDAOs.Instance().fAVMNodeDAO.flush();
                    // Get all the nodes that have this node as ancestor.
                    List<HistoryLink> links = AVMDAOs.Instance().fHistoryLinkDAO.getByAncestor(node);
                    for (HistoryLink link : links)
                    {
                        AVMNode desc = link.getDescendent();
                        desc.setAncestor(ancestor);
                        if (desc.getMergedFrom() == null)
                        {
                            desc.setMergedFrom(mergedFrom);
                        }
                        AVMDAOs.Instance().fHistoryLinkDAO.delete(link);
                    }
                    // Get all the nodes that have this node as mergedFrom
                    List<MergeLink> mlinks = AVMDAOs.Instance().fMergeLinkDAO.getByFrom(node);
                    for (MergeLink link : mlinks)
                    {
                        link.getMto().setMergedFrom(ancestor);
                        AVMDAOs.Instance().fMergeLinkDAO.delete(link);
                    }
                    // Get rid of all properties belonging to this node.
                    // AVMDAOs.Instance().fAVMNodePropertyDAO.deleteAll(node);
                    // Get rid of all aspects belonging to this node.
//                    AVMDAOs.Instance().fAVMAspectNameDAO.delete(node);
                    // Get rid of ACL.
                    DbAccessControlList acl = node.getAcl();
                    node.setAcl(null);
                    if (acl != null)
                    {
                        acl.deleteEntries();
                        (new HibernateTemplate(fSessionFactory)).delete(acl);
                    }
                    // Extra work for directories.
                    if (node.getType() == AVMNodeType.PLAIN_DIRECTORY ||
                        node.getType() == AVMNodeType.LAYERED_DIRECTORY)
                    {
                        // First get rid of all child entries for the node.
                        AVMDAOs.Instance().fChildEntryDAO.deleteByParent(node);
                    }
                    // This is not on, since content urls can be shared.
//                    else if (node.getType() == AVMNodeType.PLAIN_FILE)
//                    {
//                        PlainFileNode file = (PlainFileNode)node;
//                        String url = file.getContentData(null).getContentUrl();
//                        if (url != null)
//                        {
//                            RawServices.Instance().getContentStore().delete(url);
//                        }
//                    }
                    AVMDAOs.Instance().fAVMNodeDAO.delete(node);
                }
                return null;
            }
        }
        try
        {
            TransactionUtil.executeInUserTransaction(fTransactionService, 
                                                     new TxnWork());
        }
        catch (Exception e)
        {
            fgLogger.error("Garbage collector error", e);
        }
    }
}
