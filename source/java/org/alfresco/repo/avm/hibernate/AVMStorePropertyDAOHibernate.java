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

import org.alfresco.repo.avm.AVMStore;
import org.alfresco.repo.avm.AVMStoreProperty;
import org.alfresco.repo.avm.AVMStorePropertyDAO;
import org.alfresco.repo.domain.QNameDAO;
import org.alfresco.repo.domain.QNameEntity;
import org.alfresco.service.namespace.QName;
import org.hibernate.Query;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * The Hibernate implementation of the DAO for AVMNodeProperties.
 * @author britt
 */
class AVMStorePropertyDAOHibernate extends HibernateDaoSupport implements AVMStorePropertyDAO
{
    private QNameDAO qnameDAO;

    /**
     * Set the DAO for accessing QName entities
     */
    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }

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
        QNameEntity qnameEntity = qnameDAO.getQNameEntity(name);
        if (qnameEntity == null)
        {
            // No such QName
            return null;
        }
        else
        {
            Query query =
                getSession().createQuery(
                        "select asp " +
                        "from AVMStorePropertyImpl asp " +
                        "where " +
                        "asp.store = :store and " +
                        "asp.name = :name");
            query.setEntity("store", store);
            query.setParameter("name", qnameEntity);
            return (AVMStoreProperty)query.uniqueResult();
        }
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
        // Get the URI and LocalName parts
        String uri = keyPattern.getNamespaceURI();
        if (uri == null || uri.length() == 0)
        {
            uri = "%";
        }
        String localName = keyPattern.getLocalName();
        if (localName == null || localName.length() == 0)
        {
            localName = "%";
        }
        Query query =
            getSession().createQuery(
                "select asp " +
                "from AVMStorePropertyImpl asp " +
                "join asp.name name " +
                "join name.namespace namespace " +
                "where " +
                "asp.store = :store and " +
                "namespace.safeUri like :uri and " +
                "name.localName like :localName");
        query.setEntity("store", store);
        query.setParameter("uri", uri);
        query.setParameter("localName", localName);
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
        // Get the URI and LocalName parts
        String uri = keyPattern.getNamespaceURI();
        if (uri == null || uri.length() == 0)
        {
            uri = "%";
        }
        String localName = keyPattern.getLocalName();
        if (localName == null || localName.length() == 0)
        {
            localName = "%";
        }
        Query query =
            getSession().createQuery(
                    "select asp " +
                    "from AVMStorePropertyImpl asp " +
                    "join asp.name name " +
                    "join name.namespace namespace " +
                    "where " +
                    "namespace.safeUri like :uri and " +
                    "name.localName like :localName");
            query.setParameter("uri", uri);
            query.setParameter("localName", localName);
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
        QNameEntity qnameEntity = qnameDAO.getQNameEntity(name);
        if (qnameEntity != null)
        {
            Query delete = 
                getSession().createQuery("delete from AVMStorePropertyImpl asp " +
                                         "where asp.store = :store and asp.name = :name");
            delete.setEntity("store", store);
            delete.setParameter("name", qnameEntity);
            delete.executeUpdate();
        }
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
