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

import org.alfresco.repo.avm.AVMStore;
import org.alfresco.repo.avm.AVMStoreProperty;
import org.alfresco.repo.avm.AVMStorePropertyDAO;
import org.alfresco.service.namespace.QName;
import org.hibernate.Query;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * The Hibernate implementation of the DAO for AVMNodeProperties.
 * @author britt
 */
class AVMStorePropertyDAOHibernate extends HibernateDaoSupport implements AVMStorePropertyDAO
{
    /**
     * Persist a property.
     * @param prop The AVMStoreProperty to persist.
     */
    public void save(AVMStoreProperty prop)
    {
        getSession().save(prop);
    }
    
    /**
     * Get a property by store and name.
     * @param store The AVMStore.
     * @param name The QName of the property.
     * @return The given AVMStoreProperty or null if not found.
     */
    public AVMStoreProperty get(AVMStore store, QName name)
    {
        Query query =
            getSession().createQuery("from AVMStorePropertyImpl asp where asp.store = :store and asp.name = :name");
        query.setEntity("store", store);
        query.setParameter("name", name);
        return (AVMStoreProperty)query.uniqueResult();
    }
    
    /**
     * Get all the properties associated with a store.
     * @param store The AVMStore whose properties should be fetched.
     * @return A List of properties associated with the store.
     */
    @SuppressWarnings("unchecked")
    public List<AVMStoreProperty> get(AVMStore store)
    {
        Query query =
            getSession().createQuery("from AVMStorePropertyImpl asp where asp.store = :store");
        query.setEntity("store", store);
        return (List<AVMStoreProperty>)query.list();
    }

    /**
     * Query store properties by key pattern.
     * @param store The store.
     * @param keyPattern An sql 'like' pattern wrapped up in a QName
     * @return A List of matching AVMStoreProperties.
     */
    @SuppressWarnings("unchecked")
    public List<AVMStoreProperty> queryByKeyPattern(AVMStore store, QName keyPattern)
    {
        Query query =
            getSession().createQuery(
                "from AVMStorePropertyImpl asp " +
                "where asp.store = :store and asp.name like :name");
        query.setEntity("store", store);
        query.setParameter("name", keyPattern);
        return (List<AVMStoreProperty>)query.list();
    }

    /**
     * Query all stores' properties by key pattern.
     * @param keyPattern The sql 'like' pattern wrapped up in a QName
     * @return A List of match AVMStoreProperties.
     */
    @SuppressWarnings("unchecked")
    public List<AVMStoreProperty> queryByKeyPattern(QName keyPattern)
    {
        Query query =
            getSession().createQuery(
                "from AVMStorePropertyImpl asp " +
                "where asp.name like :name");
        query.setParameter("name", keyPattern);
        return (List<AVMStoreProperty>)query.list();
    }

    /**
     * Update a modified property.
     * @param prop The AVMStoreProperty to update.
     */
    public void update(AVMStoreProperty prop)
    {
        // This is a no op for hibernate.
    }
    
    /**
     * Delete a property from a store by name.
     * @param store The AVMStore to delete from.
     * @param name The name of the property.
     */
    public void delete(AVMStore store, QName name)
    {
        Query delete = 
            getSession().createQuery("delete from AVMStorePropertyImpl asp " +
                                     "where asp.store = :store and asp.name = :name");
        delete.setEntity("store", store);
        delete.setParameter("name", name);
        delete.executeUpdate();
    }
    
    /**
     * Delete all properties associated with a store.
     * @param store The AVMStore whose properties are to be deleted.
     */
    public void delete(AVMStore store)
    {
        Query delete =
            getSession().createQuery("delete from AVMStorePropertyImpl asp where asp.store = :store");
        delete.setEntity("store", store);
        delete.executeUpdate();
    }
}
