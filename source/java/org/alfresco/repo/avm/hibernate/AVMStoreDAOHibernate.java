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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.avm.AVMNode;
import org.alfresco.repo.avm.AVMStore;
import org.alfresco.repo.avm.AVMStoreDAO;
import org.alfresco.repo.avm.AVMStoreImpl;
import org.hibernate.Query;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * The Hibernate version for AVMStoreDAO
 * @author britt
 */
class AVMStoreDAOHibernate extends HibernateDaoSupport implements
        AVMStoreDAO
{
    /**
     * An in memory cache of name to primary key mappings.
     */
    private Map<String, Long> fNameCache;
    
    /**
     * Do nothing constructor.
     */
    public AVMStoreDAOHibernate()
    {
        super();
        fNameCache = new HashMap<String, Long>();
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
        List<AVMStore> result = (List<AVMStore>)query.list();
        List<AVMStore> ret = new ArrayList<AVMStore>(result.size());
        for (AVMStore store : result)
        {
            ret.add(forceNonLazy(store));
        }
        return ret;
    }
    
    /**
     * Get a store by name.
     * @param name The name of the store.
     * @return The store or null if not found.
     */
    public AVMStore getByName(String name)
    {
        Long id = null;
        synchronized (this)
        {
            id = fNameCache.get(name);
        }
        if (id != null)
        {
            return forceNonLazy((AVMStore)getSession().get(AVMStoreImpl.class, id));      
        }
        Query query = getSession().createQuery("from AVMStoreImpl st " +
                                               "where st.name = :name");
        query.setParameter("name", name);
        AVMStore result = (AVMStore)query.uniqueResult();
        synchronized (this)
        {
            if (result != null)
            {
                fNameCache.put(name, result.getId());
            }
        }
        return forceNonLazy(result);
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
        return forceNonLazy((AVMStore)query.uniqueResult());
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
        AVMStore result = (AVMStore)getSession().get(AVMStoreImpl.class, id);
        return forceNonLazy(result);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMStoreDAO#invalidateCache()
     */
    public synchronized void invalidateCache() 
    {
        fNameCache.clear();
    }
    
    private AVMStore forceNonLazy(AVMStore store)
    {
        if (store instanceof HibernateProxy)
        {
            return (AVMStore)((HibernateProxy)store).getHibernateLazyInitializer().getImplementation();
        }
        return store;
    }
}
