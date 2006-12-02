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
import org.alfresco.repo.avm.AVMStore;
import org.alfresco.repo.avm.AVMStoreDAO;
import org.alfresco.repo.avm.AVMStoreImpl;
import org.hibernate.Query;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * The Hibernate version for AVMStoreDAO
 * @author britt
 */
class AVMStoreDAOHibernate extends HibernateDaoSupport implements
        AVMStoreDAO
{
    /**
     * Do nothing constructor.
     */
    public AVMStoreDAOHibernate()
    {
        super();
    }
    
    /**
     * Save a store, never before saved.
     * @param store The store
     */
    public void save(AVMStore store)
    {
        getSession().save(store);
    }

    /**
     * Delete the given AVMStore.
     * @param store The AVMStore.
     */
    public void delete(AVMStore store)
    {
        getSession().delete(store);
    }

    /**
     * Get all stores.
     * @return A List of all the AVMStores.
     */
    @SuppressWarnings("unchecked")
    public List<AVMStore> getAll()
    {
        Query query = getSession().createQuery("from AVMStoreImpl r");
        return (List<AVMStore>)query.list();
    }
    
    /**
     * Get a store by name.
     * @param name The name of the store.
     * @return The store or null if not found.
     */
    public AVMStore getByName(String name)
    {
        Query query = getSession().createQuery("from AVMStoreImpl st " +
                                               "where st.name = :name");
        query.setParameter("name", name);
        return (AVMStore)query.uniqueResult();
    }

    /**
     * Get the AVM Store that has the given root as HEAD.
     * @param root The root to query.
     * @return The matching store or null.
     */
    public AVMStore getByRoot(AVMNode root)
    {
        Query query = getSession().createQuery("from AVMStoreImpl st " +
                                               "where st.root = :root");
        query.setEntity("root", root);
        return (AVMStore)query.uniqueResult();
    }

    /**
     * Update the given AVMStore record.
     * @param store The dirty AVMStore.
     */
    public void update(AVMStore store)
    {
        // No op in Hibernate.
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMStoreDAO#getByID(long)
     */
    public AVMStore getByID(long id)
    {
        return (AVMStore)getSession().get(AVMStoreImpl.class, id);
    }
}
