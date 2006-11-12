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
import org.alfresco.repo.avm.ChildEntry;
import org.alfresco.repo.avm.ChildEntryImpl;
import org.alfresco.repo.avm.ChildEntryDAO;
import org.alfresco.repo.avm.ChildKey;
import org.alfresco.repo.avm.DirectoryNode;
import org.hibernate.Query;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * The Hibernate version of the ChildEntry DAO.
 * @author britt
 */
class ChildEntryDAOHibernate extends HibernateDaoSupport implements
        ChildEntryDAO
{
    /**
     * Do nothing constructor.
     */
    public ChildEntryDAOHibernate()
    {
        super();
    }
    
    /**
     * Save an unsaved ChildEntry.
     * @param entry The entry to save.
     */
    public void save(ChildEntry entry)
    {
        getSession().save(entry);
    }

    /**
     * Get an entry by name and parent.
     * @param name The name of the child to find.
     * @param parent The parent to look in.
     * @return The ChildEntry or null if not foun.
     */
    public ChildEntry get(ChildKey key)
    {
        return (ChildEntry)getSession().get(ChildEntryImpl.class, key);
    }
    
    /**
     * Get all the children of a given parent.
     * @param parent The parent.
     * @return A List of ChildEntries.
     */
    @SuppressWarnings("unchecked")
    public List<ChildEntry> getByParent(DirectoryNode parent)
    {
        Query query =
            getSession().createQuery("from ChildEntryImpl ce where ce.key.parent = :parent");
        query.setEntity("parent", parent);
        return (List<ChildEntry>)query.list();
    }

    /**
     * Get the entry for a given child in a given parent.
     * @param parent The parent.
     * @param child The child.
     * @return The ChildEntry or null.
     */
    public ChildEntry getByParentChild(DirectoryNode parent, AVMNode child)
    {
        Query query =
            getSession().createQuery("from ChildEntryImpl ce where ce.key.parent = :parent " +
                                     "and ce.child = :child");
        query.setEntity("parent", parent);
        query.setEntity("child", child);
        return (ChildEntry)query.uniqueResult();
    }

    /**
     * Get all the ChildEntries corresponding to the given child.
     * @param child The child for which to look up entries.
     * @return The matching entries.
     */
    @SuppressWarnings("unchecked")
    public List<ChildEntry> getByChild(AVMNode child)
    {
        Query query = getSession().createQuery("from ChildEntryImpl ce " +
                                               "where ce.child = :child");
        query.setEntity("child", child);
        return (List<ChildEntry>)query.list();
    }

    /**
     * Update a dirty ChildEntry.
     * @param child The dirty entry.
     */
    public void update(ChildEntry child)
    {
        // NO OP in Hibernate.
    }

    /**
     * Delete one.
     * @param child The one to delete.
     */
    public void delete(ChildEntry child)
    {
        getSession().delete(child);
    }

    // TODO Does this have dangerous interactions with the cache?
    /**
     * Delete all children of the given parent.
     * @param parent The parent.
     */
    public void deleteByParent(AVMNode parent)
    {
        Query delete = getSession().getNamedQuery("ChildEntry.DeleteByParent");
        delete.setEntity("parent", parent);
        delete.executeUpdate();
    }
}
