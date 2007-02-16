/*
 * Copyright (C) 2006 Alfresco, Inc.
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
import org.alfresco.repo.avm.MergeLink;
import org.alfresco.repo.avm.MergeLinkDAO;
import org.hibernate.Query;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * The Hibernate implementation of the DAO for a MergeLink
 * @author britt
 */
class MergeLinkDAOHibernate extends HibernateDaoSupport implements
        MergeLinkDAO
{
    /**
     * Do nothing constructor.
     */
    public MergeLinkDAOHibernate()
    {
        super();
    }
    
    /**
     * Save an unsaved MergeLink.
     * @param link The link to save.
     */
    public void save(MergeLink link)
    {
        getSession().save(link);
    }

    /**
     * Get a link from the merged to node.
     * @param to The node merged to.
     * @return An AVMNode or null if not found.
     */
    public MergeLink getByTo(AVMNode to)
    {
        Query query = getSession().createQuery("from MergeLinkImpl ml where ml.mto = :to");
        query.setEntity("to", to);
        return (MergeLink)query.uniqueResult();
    }

    /**
     * Get all the link that the given node was merged to.
     * @param from The node that was merged from
     * @return A List of MergeLinks.
     */
    @SuppressWarnings("unchecked")
    public List<MergeLink> getByFrom(AVMNode from)
    {
        Query query = getSession().getNamedQuery("MergeLink.ByFrom");
        query.setEntity("merged", from);
        return (List<MergeLink>)query.list();
    }

    /**
     * Delete a link.
     * @param link The link to delete.
     */
    public void delete(MergeLink link)
    {
        getSession().delete(link);
    }
}
