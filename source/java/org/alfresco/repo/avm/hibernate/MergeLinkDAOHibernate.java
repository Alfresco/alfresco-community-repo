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
