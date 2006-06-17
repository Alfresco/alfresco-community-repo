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
import org.hibernate.proxy.HibernateProxy;

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
        long start = System.currentTimeMillis();
        class HTxnCallback implements HibernateTxnCallback
        {
            public void perform(Session session)
            {
                Query query = session.getNamedQuery("FindOrphans");
                query.setMaxResults(fBatchSize);
                List<AVMNode> nodes = (List<AVMNode>)query.list();
                if (nodes.size() == 0)
                {
                    fActive = false;
                    return;
                }
                for (AVMNode node : nodes)
                {
                    // Save away the ancestor and merged from fields from this node.
                    AVMNode ancestor = node.getAncestor();
                    AVMNode mergedFrom = node.getMergedFrom();
                    // Get all the nodes that have this node as ancestor.
                    query = session.createQuery("from AVMNodeImpl an where an.ancestor = :node");
                    query.setEntity("node", node);
                    List<AVMNode> descendents = (List<AVMNode>)query.list();
                    for (AVMNode desc : descendents)
                    {
                        desc.setAncestor(ancestor);
                        if (desc.getMergedFrom() == null)
                        {
                            desc.setMergedFrom(mergedFrom);
                        }
                    }
                    // Get all the nodes that have this node as mergedFrom
                    query = session.createQuery("from AVMNodeImpl an where an.mergedFrom = :merged");
                    query.setEntity("merged", node);
                    List<AVMNode> merged = (List<AVMNode>)query.list();
                    for (AVMNode merge : merged)
                    {
                        merge.setMergedFrom(ancestor);
                    }
                    // Work around Bitter Hibernate.
                    if (node instanceof HibernateProxy)
                    {
                        node = (AVMNode)((HibernateProxy)node).getHibernateLazyInitializer().getImplementation();
                    }
                    // Extra work for directories.
                    if (node instanceof DirectoryNode)
                    {
                        // First get rid of all child entries for the node.
                        Query delete = session.createQuery("delete ChildEntryImpl ce where ce.parent = :parent");
                        delete.setEntity("parent", node);
                        delete.executeUpdate();
                        // Now find all the nodes that point to this node as their
                        // canonical parent and null that reference out.
                        query = session.createQuery("from AVMNodeImpl an where an.parent = :parent");
                        query.setEntity("parent", node);
                        List<AVMNode> children = (List<AVMNode>)query.list();
                        for (AVMNode child : children)
                        {
                            child.setParent(null);
                        }
                        if (node instanceof LayeredDirectoryNode)
                        {
                            // More special work for layered directories.
                            delete = session.createQuery("delete DeletedChildImpl dc where dc.parent = :parent");
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
        System.err.println("Batch took: " + (System.currentTimeMillis() - start) + "ms");
    }
}
