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

import org.alfresco.repo.avm.hibernate.HibernateHelper;
import org.alfresco.repo.avm.hibernate.HibernateTxn;
import org.alfresco.repo.avm.hibernate.HibernateTxnCallback;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * This is the background thread for reaping no longer referenced nodes
 * in the AVM repository.  These orphans arise from purge operations.
 * @author britt
 */
class OrphanReaper implements Runnable
{
    /**
     * The HibernateTxn instance.
     */
    private HibernateTxn fTransaction;
    
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
        fTransaction = new HibernateTxn(HibernateHelper.GetSessionFactory());
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
    @SuppressWarnings("unchecked")
    public void doBatch()
    {
        class HTxnCallback implements HibernateTxnCallback
        {
            public void perform(Session session)
            {
                SuperRepository.GetInstance().setSession(session);
                Query query = session.getNamedQuery("FindOrphans");
                query.setMaxResults(fBatchSize);
                List<AVMNode> nodes = (List<AVMNode>)query.list();
                if (nodes.size() == 0)
                {
                    fActive = false;
                    return;
                }
                fActive = true;
                for (AVMNode node : nodes)
                {
                    // Save away the ancestor and merged from fields from this node.
                    query = session.createQuery("from HistoryLinkImpl hl where hl.descendent = :desc");
                    query.setEntity("desc", node);
                    HistoryLink hlink = (HistoryLink)query.uniqueResult();
                    AVMNode ancestor = null;
                    if (hlink != null)
                    {
                        ancestor = hlink.getAncestor();
                        session.delete(hlink);
                    }
                    query = session.createQuery("from MergeLinkImpl ml where ml.mto = :to");
                    query.setEntity("to", node);
                    MergeLink mlink = (MergeLink)query.uniqueResult();
                    AVMNode mergedFrom = null;
                    if (mlink != null)
                    {
                        mergedFrom = mlink.getMfrom();
                        session.delete(mlink);
                    }
                    // Get all the nodes that have this node as ancestor.
                    query = session.getNamedQuery("HistoryLink.ByAncestor");
                    query.setEntity("node", node);
                    List<HistoryLink> links = (List<HistoryLink>)query.list();
                    for (HistoryLink link : links)
                    {
                        AVMNode desc = link.getDescendent();
                        desc.setAncestor(ancestor);
                        if (desc.getMergedFrom() == null)
                        {
                            desc.setMergedFrom(mergedFrom);
                        }
                        session.delete(link);
                    }
                    // Get all the nodes that have this node as mergedFrom
                    query = session.getNamedQuery("MergeLink.ByFrom");
                    query.setEntity("merged", node);
                    List<MergeLink> mlinks = (List<MergeLink>)query.list();
                    for (MergeLink link : mlinks)
                    {
                        link.getMto().setMergedFrom(ancestor);
                        session.delete(link);
                    }
                    session.flush();
                    node = AVMNodeUnwrapper.Unwrap(node);
                    // Extra work for directories.
                    if (node instanceof DirectoryNode)
                    {
                        // First get rid of all child entries for the node.
                        Query delete = session.getNamedQuery("ChildEntry.DeleteByParent");
                        delete.setEntity("parent", node);
                        delete.executeUpdate();
                        if (node instanceof LayeredDirectoryNode)
                        {
                            // More special work for layered directories.
                            delete = session.getNamedQuery("DeletedChild.DeleteByParent");
                            delete.setEntity("parent", node);
                            delete.executeUpdate();
                        } 
                    }
                    else if (node instanceof PlainFileNode)
                    {
                        // FileContent should be purged if nobody else references it.
                        FileContent content = ((PlainFileNode)node).getContent();
                        if (content.getRefCount() == 1)
                        {
                            content.delete();
                            session.delete(content);
                        }
                    }
                    session.delete(node);
                }
            }
        }
        try
        {
            HTxnCallback doit = new HTxnCallback();
            fTransaction.perform(doit, true);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            // TODO Log this properly.
        }
    }
}
