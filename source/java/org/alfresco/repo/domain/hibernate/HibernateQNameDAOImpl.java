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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.domain.hibernate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.domain.NamespaceEntity;
import org.alfresco.repo.domain.QNameDAO;
import org.alfresco.repo.domain.QNameEntity;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Hibernate-specific implementation of the QName and Namespace DAO interface.
 * 
 * @author Derek Hulley
 * @since 2.1
 */
public class HibernateQNameDAOImpl extends HibernateDaoSupport implements QNameDAO
{
    private static Log logger = LogFactory.getLog(HibernateQNameDAOImpl.class);
    
    private static final String QUERY_GET_NS_BY_URI = "qname.GetNamespaceByUri";
    private static final String QUERY_GET_QNAME_BY_URI_AND_LOCALNAME = "qname.GetQNameByUriAndLocalName";

    private SimpleCache<QName, Long> qnameEntityCache;
    
    public void setQnameEntityCache(SimpleCache<QName, Long> qnameEntityCache)
    {
        this.qnameEntityCache = qnameEntityCache;
    }

    public NamespaceEntity getNamespaceEntity(Long id)
    {
        NamespaceEntity namespaceEntity = (NamespaceEntity) getSession().get(NamespaceEntityImpl.class, id);
        if (namespaceEntity == null)
        {
            throw new AlfrescoRuntimeException("The NamespaceEntity ID " + id + " doesn't exist.");
        }
        return namespaceEntity;
    }

    public NamespaceEntity getNamespaceEntity(final String namespaceUri)
    {
        // TODO: Use a cache if external use becomes common
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                String oracleSafeUri = (namespaceUri.length() == 0) ? NamespaceEntityImpl.EMPTY_URI_SUBSTITUTE : namespaceUri;
                
                Query query = session
                    .getNamedQuery(HibernateQNameDAOImpl.QUERY_GET_NS_BY_URI)
                    .setString("namespaceUri", oracleSafeUri);
                return query.uniqueResult();
            }
        };
        NamespaceEntity result = (NamespaceEntity) getHibernateTemplate().execute(callback);
        // Done
        return result;
    }

    public NamespaceEntity getOrCreateNamespaceEntity(String namespaceUri)
    {
        NamespaceEntity result = getNamespaceEntity(namespaceUri);
        if (result == null)
        {
            result = newNamespaceEntity(namespaceUri);
        }
        return result;
    }

    public NamespaceEntity newNamespaceEntity(String namespaceUri)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Persisting Namespace: " + namespaceUri);
        }
        NamespaceEntity namespace = new NamespaceEntityImpl();
        namespace.setUri(namespaceUri);
        // Persist
        getSession().save(namespace);
        // Done
        return namespace;
    }

    public QNameEntity getQNameEntity(Long id)
    {
        QNameEntity qnameEntity = (QNameEntity) getSession().get(QNameEntityImpl.class, id);
        if (qnameEntity == null)
        {
            throw new AlfrescoRuntimeException("The QNameEntity ID " + id + " doesn't exist.");
        }
        return qnameEntity;
    }

    public QName getQName(Long id)
    {
        // TODO: Explore caching options here
        QNameEntity qnameEntity = getQNameEntity(id);
        if (qnameEntity == null)
        {
            return null;
        }
        else
        {
            return qnameEntity.getQName();
        }
    }

    public QNameEntity getQNameEntity(final QName qname)
    {
        QNameEntity result;
        // First check the cache
        Long id = qnameEntityCache.get(qname);
        if (id == null)
        {
            // It's not in the cache, so query
            HibernateCallback callback = new HibernateCallback()
            {
                public Object doInHibernate(Session session)
                {
                    String namespaceUri = qname.getNamespaceURI();
                    String oracleSafeUri = (namespaceUri.length() == 0) ? NamespaceEntityImpl.EMPTY_URI_SUBSTITUTE : namespaceUri;
                    
                    Query query = session
                        .getNamedQuery(HibernateQNameDAOImpl.QUERY_GET_QNAME_BY_URI_AND_LOCALNAME)
                        .setString("namespaceUri", oracleSafeUri)
                        .setString("localName", qname.getLocalName());
                    return query.uniqueResult();
                }
            };
            result = (QNameEntity) getHibernateTemplate().execute(callback);
            if (result != null)
            {
                id = result.getId();
                // We found something, so we can add it to the cache
                qnameEntityCache.put(qname, id);
            }
            else
            {
                qnameEntityCache.put(qname, -1L);
            }
        }
        else if(id == -1L)
        {
            return null;
        }
        else
        {
            // Found in the cache.  Load using the ID.
            result = getQNameEntity(id);
            if (result == null)
            {
                // It is not available, so we need to go the query route.
                // But first remove the cache entry
                qnameEntityCache.remove(qname);
                // Recurse, but this time there is no cache entry
                return getQNameEntity(qname);
            }
        }
        // Done
        return result;
    }

    public QNameEntity getOrCreateQNameEntity(QName qname)
    {
        QNameEntity result = getQNameEntity(qname);
        if (result == null)
        {
            result = newQNameEntity(qname);
        }
        return result;
    }

    public QNameEntity newQNameEntity(QName qname)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Persisting QName: " + qname);
        }
        final String namespaceUri = qname.getNamespaceURI();
        final String localName = qname.getLocalName();
        NamespaceEntity namespace = getNamespaceEntity(namespaceUri);
        if (namespace == null)
        {
            namespace = newNamespaceEntity(namespaceUri);
        }
        QNameEntity qnameEntity = new QNameEntityImpl();
        qnameEntity.setNamespace(namespace);
        qnameEntity.setLocalName(localName);
        // Persist
        Long id = (Long) getSession().save(qnameEntity);
        // Update the cache
        qnameEntityCache.put(qname, id);
        // Done
        return qnameEntity;
    }

    public Set<QName> convertIdsToQNames(Set<Long> ids)
    {
        Set<QName> qnames = new HashSet<QName>(ids.size() * 2 + 1);
        for (Long id : ids)
        {
            QName qname = getQName(id);             // Never null
            qnames.add(qname);
        }
        return qnames;
    }
    
    public Map<QName, ? extends Object> convertIdMapToQNameMap(Map<Long, ? extends Object> idMap)
    {
        Map<QName, Object> qnameMap = new HashMap<QName, Object>(idMap.size() + 3);
        for (Map.Entry<Long, ? extends Object> entry : idMap.entrySet())
        {
            QName qname = getQName(entry.getKey());
            qnameMap.put(qname, entry.getValue());
        }
        return qnameMap;
    }
}
