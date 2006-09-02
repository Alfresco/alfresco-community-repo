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

package org.alfresco.repo.avm.hibernate;

import java.util.List;

import org.alfresco.repo.avm.AVMNode;
import org.alfresco.repo.avm.HistoryLink;
import org.alfresco.repo.avm.HistoryLinkDAO;
import org.hibernate.Query;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * The Hibernate implementation of the DAO for HistoryLinks.
 * @author britt
 */
class HistoryLinkDAOHibernate extends HibernateDaoSupport implements
        HistoryLinkDAO
{
    /**
     * Do nothing constructor.
     */
    public HistoryLinkDAOHibernate()
    {
        super();
    }
    
    /**
     * Save an unsaved HistoryLink.
     * @param link
     */
    public void save(HistoryLink link)
    {
        getSession().save(link);
    }

    /**
     * Get the history link with the given descendent.
     * @param descendent The descendent.
     * @return The HistoryLink or null if not found.
     */
    public HistoryLink getByDescendent(AVMNode descendent)
    {
        Query query = getSession().createQuery("from HistoryLinkImpl hl where hl.descendent = :desc");
        query.setEntity("desc", descendent);
        return (HistoryLink)query.uniqueResult();
    }

    /**
     * Get all the descendents of a node.
     * @param ancestor The ancestor node.
     * @return A List of AVMNode descendents.
     */
    @SuppressWarnings("unchecked")
    public List<HistoryLink> getByAncestor(AVMNode ancestor)
    {
        Query query = getSession().getNamedQuery("HistoryLink.ByAncestor");
        query.setEntity("node", ancestor);
        return (List<HistoryLink>)query.list();
    }
    
    /**
     * Delete a HistoryLink
     * @param link The link to delete.
     */
    public void delete(HistoryLink link)
    {
        getSession().delete(link);
    }
}
