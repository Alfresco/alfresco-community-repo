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
import org.alfresco.repo.avm.DeletedChild;
import org.alfresco.repo.avm.DeletedChildDAO;
import org.alfresco.repo.avm.LayeredDirectoryNode;
import org.hibernate.Query;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Hibernate implementation of DAO for DeletedChildren.
 * @author britt
 */
class DeletedChildDAOHibernate extends HibernateDaoSupport implements
        DeletedChildDAO
{
    /**
     * Do nothing constructor.
     */
    public DeletedChildDAOHibernate()
    {
        super();
    }
    
    /**
     * Save an unsaved DeletedChild.
     * @param child The DeletedChild to be saved.
     */
    public void save(DeletedChild child)
    {
        getSession().save(child);
    }

    /**
     * Delete one.
     * @param child The one to delete.
     */
    public void delete(DeletedChild child)
    {
        getSession().delete(child);
    }

    /**
     * Delete all belonging to the given parent.
     * @param parent The parent.
     */
    public void deleteByParent(AVMNode parent)
    {
        Query delete = getSession().getNamedQuery("DeletedChild.DeleteByParent");
        delete.setEntity("parent", parent);
        delete.executeUpdate();
    }

    /**
     * Get by name and parent.
     * @param name The name of the deleted entry.
     * @param parent The parent.
     * @return A DeletedChild or null if not found.
     */
    public DeletedChild getByNameParent(String name, LayeredDirectoryNode parent)
    {
        Query query = getSession().getNamedQuery("DeletedChild.ByNameParent");
        query.setString("name", name);
        query.setEntity("parent", parent);
        query.setCacheable(true);
        query.setCacheRegion("DeletedChild.ByNameParent");
        return (DeletedChild)query.uniqueResult();
    }
    
    /**
     * Get all the deleted children of a given parent.
     * @param parent The parent.
     * @return A List of DeletedChildren.
     */
    @SuppressWarnings("unchecked")
    public List<DeletedChild> getByParent(LayeredDirectoryNode parent)
    {
        Query query = getSession().getNamedQuery("DeletedChild.ByParent");
        query.setEntity("parent", parent);
        query.setCacheable(true);
        query.setCacheRegion("DeletedChild.ByParent");
        return (List<DeletedChild>)query.list();
    }
}
