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

package org.alfresco.repo.avm.hibernate;

import java.util.List;

import org.alfresco.repo.avm.AVMNode;
import org.alfresco.repo.avm.AVMNodeDAO;
import org.alfresco.repo.avm.AVMNodeImpl;
import org.alfresco.repo.avm.AVMNodeUnwrapper;
import org.alfresco.repo.avm.AVMStore;
import org.alfresco.repo.avm.DirectoryNode;
import org.hibernate.Query;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * @author britt
 *
 */
class AVMNodeDAOHibernate extends HibernateDaoSupport implements
        AVMNodeDAO
{
    /**
     * Do nothing constructor.
     */
    public AVMNodeDAOHibernate()
    {
        super();
    }
    
    /**
     * Save the given node, having never been saved before.
     */
    public void save(AVMNode node)
    {
        getSession().save(node);
    }

    /**
     * Delete a single node.
     * @param node The node to delete.
     */
    public void delete(AVMNode node)
    {
        getSession().delete(node);
    }

    /**
     * Get by ID.
     * @param id The id to get.
     */
    public AVMNode getByID(long id)
    {
        return AVMNodeUnwrapper.Unwrap((AVMNode)getSession().get(AVMNodeImpl.class, id));
    }

    /**
     * Update a node that has been dirtied.
     * @param node The node.
     */
    public void update(AVMNode node)
    {
        getSession().flush();
    }
    
    /**
     * Get the root of a particular version.
     * @param store The store we're querying.
     * @param version The version.
     * @return The VersionRoot or null.
     */
    public DirectoryNode getAVMStoreRoot(AVMStore store, int version)
    {
        Query query = 
            getSession().getNamedQuery("VersionRoot.GetVersionRoot");
        query.setEntity("store", store);
        query.setInteger("version", version);
        AVMNode root = (AVMNode)query.uniqueResult();
        if (root == null)
        {
            return null;
        }
        return (DirectoryNode)AVMNodeUnwrapper.Unwrap(root);
    }

    /**
     * Get the ancestor of a node.
     * @param node The node whose ancestor is desired.
     * @return The ancestor or null.
     */
    public AVMNode getAncestor(AVMNode node)
    {
        Query query = getSession().createQuery("select hl.ancestor from HistoryLinkImpl hl where hl.descendent = :desc");
        query.setEntity("desc", node);
        return (AVMNode)query.uniqueResult();
    }

    /**
     * Get the node the given node was merged from.
     * @param node The node whose merged from is desired.
     * @return The merged from node or null.
     */
    public AVMNode getMergedFrom(AVMNode node)
    {
        Query query = getSession().createQuery("select ml.mfrom from MergeLinkImpl ml where ml.mto = :to");
        query.setEntity("to", node);
        return (AVMNode)query.uniqueResult();
    }

    /**
     * Get up to batchSize orphans. 
     * @param batchSize Get no more than this number.
     * @return A List of orphaned AVMNodes.
     */
    @SuppressWarnings("unchecked")
    public List<AVMNode> getOrphans(int batchSize)
    {
        Query query = getSession().getNamedQuery("FindOrphans");
        query.setMaxResults(batchSize);
        return (List<AVMNode>)query.list();
    }

    /**
     * Get all content urls in he AVM Repository.
     * @return A List of URL Strings.
     */
    @SuppressWarnings("unchecked")
    public List<String> getContentUrls()
    {
        Query query = getSession().getNamedQuery("PlainFileNode.GetContentUrls");
        return (List<String>)query.list();
    }

    /**
     * Get all AVMNodes that are new in the given store.
     * @param store The given store.
     * @return A List of AVMNodes.
     */
    @SuppressWarnings("unchecked")
    public List<AVMNode> getNewInStore(AVMStore store)
    {
        Query query = getSession().getNamedQuery("AVMNode.GetNewInStore");
        query.setEntity("store", store);
        return (List<AVMNode>)query.list();
    }
    
    /**
     * Inappropriate hack to get Hibernate to play nice.
     */
    public void flush()
    {
        getSession().flush();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMNodeDAO#getEmptyGUIDS(int)
     */
    @SuppressWarnings("unchecked")
    public List<AVMNode> getEmptyGUIDS(int count)
    {
        Query query = getSession().createQuery("from AVMNodeImpl an where an.guid is null");
        query.setMaxResults(count);
        return (List<AVMNode>)query.list();
    }
}
