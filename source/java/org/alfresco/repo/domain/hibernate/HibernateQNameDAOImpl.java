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

import java.io.Serializable;
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
import org.springframework.extensions.surf.util.Pair;
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

    private static final Long CACHE_NULL_LONG = Long.MIN_VALUE;
    private SimpleCache<Serializable, Serializable> namespaceEntityCache;
    private SimpleCache<Serializable, Serializable> qnameEntityCache;

    /**
     * Set the cache that maintains the ID-Namespace mappings and vice-versa.
     * 
     * @param namespaceEntityCache          the cache
     */
    public void setNamespaceEntityCache(SimpleCache<Serializable, Serializable> namespaceEntityCache)
    {
        this.namespaceEntityCache = namespaceEntityCache;
    }

    /**
     * Set the cache that maintains the ID-QName mappings and vice-versa.
     * 
     * @param qnameEntityCache              the cache
     */
    public void setQnameEntityCache(SimpleCache<Serializable, Serializable> qnameEntityCache)
    {
        this.qnameEntityCache = qnameEntityCache;
    }

    public Pair<Long, String> getNamespace(Long id)
    {
        // Check the cache
        String uri = (String) namespaceEntityCache.get(id);
        if (uri != null)
        {
            return new Pair<Long, String>(id, uri);
        }
        // Get it from the DB
        NamespaceEntity namespaceEntity = (NamespaceEntity) getSession().get(NamespaceEntityImpl.class, id);
        if (namespaceEntity == null)
        {
            throw new AlfrescoRuntimeException("The NamespaceEntity ID " + id + " doesn't exist.");
        }
        uri = namespaceEntity.getUri();
        // Cache it
        namespaceEntityCache.put(uri, id);
        namespaceEntityCache.put(id, uri);
        // Done
        return new Pair<Long, String>(id, uri);
    }

    public Pair<Long, String> getNamespace(final String namespaceUri)
    {
        // Check the cache
        Long id = (Long) namespaceEntityCache.get(namespaceUri);
        if (id != null)
        {
            if (id.equals(CACHE_NULL_LONG))
            {
                return null;
            }
            else
            {
                return new Pair<Long, String>(id, namespaceUri);
            }
        }
        // Get it from the DB
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
        if (result == null)
        {
            // Cache it
            namespaceEntityCache.put(namespaceUri, CACHE_NULL_LONG);
            // Done
            return null;
        }
        else
        {
            id = result.getId();
            // Cache it
            namespaceEntityCache.put(id, namespaceUri);
            namespaceEntityCache.put(namespaceUri, id);
            // Done
            return new Pair<Long, String>(id, namespaceUri);
        }
    }

    public Pair<Long, String> getOrCreateNamespace(String namespaceUri)
    {
        Pair<Long, String> result = getNamespace(namespaceUri);
        if (result == null)
        {
            result = newNamespace(namespaceUri);
        }
        return result;
    }

    public Pair<Long, String> newNamespace(String namespaceUri)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Persisting Namespace: " + namespaceUri);
        }
        NamespaceEntity namespace = new NamespaceEntityImpl();
        namespace.setUri(namespaceUri);
        // Persist
        Session session = getSession();
        Long id = (Long) session.save(namespace);
        DirtySessionMethodInterceptor.flushSession(session, true);
        // Force a flush because Hibernate doesn't always get the flush order right
        // for DBs that use sequences for the PK: ETHREEOH-1962
        DirtySessionMethodInterceptor.flushSession(getSession(), true);
        // Cache it
        namespaceEntityCache.put(id, namespaceUri);
        namespaceEntityCache.put(namespaceUri, id);
        // Done
        return new Pair<Long, String>(id, namespaceUri);
    }

    public void updateNamespace(String oldNamespaceUri, String newNamespaceUri)
    {
        // First check for clashes
        if (getNamespace(newNamespaceUri) != null)
        {
            throw new AlfrescoRuntimeException("Namespace URI '" + newNamespaceUri + "' already exists.");
        }
        // Get the old one
        Pair<Long, String> oldNamespacePair = getNamespace(oldNamespaceUri);
        if (oldNamespacePair == null)
        {
            // Nothing to rename
            return;
        }
		NamespaceEntity oldNamespaceEntity = (NamespaceEntity) getSession().load(NamespaceEntityImpl.class, oldNamespacePair.getFirst());
        oldNamespaceEntity.setUri(newNamespaceUri);
        // Flush to force early failure
        getSession().flush();
        // Trash the cache
        qnameEntityCache.clear();
        // Done
    }

    public Pair<Long, QName> getQName(Long id)
    {
        // Check the cache
        QName qname = (QName) qnameEntityCache.get(id);
        if (qname != null)
        {
            return new Pair<Long, QName>(id, qname);
        }
        QNameEntity qnameEntity = (QNameEntity) getSession().get(QNameEntityImpl.class, id);
        if (qnameEntity == null)
        {
            throw new AlfrescoRuntimeException("The QNameEntity ID " + id + " doesn't exist.");
        }
        qname = qnameEntity.getQName();
        // Cache it
        qnameEntityCache.put(id, qname);
        qnameEntityCache.put(qname, id);
        // Done
        return new Pair<Long, QName>(id, qname);
    }

    public Pair<Long, QName> getQName(final QName qname)
    {
        // Check the cache
        Long id = (Long) qnameEntityCache.get(qname);
        if (id != null)
        {
            if (id.equals(CACHE_NULL_LONG))
            {
                return null;
            }
            else
            {
                return new Pair<Long, QName>(id, qname);
            }
        }
        QNameEntity result;
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
        if (result == null)
        {
            // Cache it
            qnameEntityCache.put(qname, CACHE_NULL_LONG);
            // Done
            return null;
        }
        else
        {
            id = result.getId();
            // Cache it
            qnameEntityCache.put(id, qname);
            qnameEntityCache.put(qname, id);
            // Done
            return new Pair<Long, QName>(id, qname);
        }
    }

    public Pair<Long, QName> getOrCreateQName(QName qname)
    {
        Pair<Long, QName> result = getQName(qname);
        if (result == null)
        {
            result = newQName(qname);
        }
        return result;
    }

    public Pair<Long, QName> newQName(QName qname)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Persisting QName: " + qname);
        }
        final String namespaceUri = qname.getNamespaceURI();
        final String localName = qname.getLocalName();
        // Get the namespace
        Pair<Long, String> namespacePair = getOrCreateNamespace(namespaceUri);
        NamespaceEntity namespace = (NamespaceEntity) getSession().load(NamespaceEntityImpl.class, namespacePair.getFirst());
        // Create the QNameEntity
        QNameEntity qnameEntity = new QNameEntityImpl();
        qnameEntity.setNamespace(namespace);
        qnameEntity.setLocalName(localName);
        // Persist
        Long id = (Long) getSession().save(qnameEntity);
        // Force a flush because Hibernate doesn't always get the flush order right
        // for DBs that use sequences for the PK: ETHREEOH-1962
        DirtySessionMethodInterceptor.flushSession(getSession(), true);
        // Update the cache
        qnameEntityCache.put(qname, id);
        qnameEntityCache.put(id, qname);
        // Done
        return new Pair<Long, QName>(id, qname);
    }

    public Set<QName> convertIdsToQNames(Set<Long> ids)
    {
        Set<QName> qnames = new HashSet<QName>(ids.size() * 2 + 1);
        for (Long id : ids)
        {
            QName qname = getQName(id).getSecond();                     // getQName(id) is never null
            qnames.add(qname);
        }
        return qnames;
    }
    
    public Map<QName, ? extends Object> convertIdMapToQNameMap(Map<Long, ? extends Object> idMap)
    {
        Map<QName, Object> qnameMap = new HashMap<QName, Object>(idMap.size() + 3);
        for (Map.Entry<Long, ? extends Object> entry : idMap.entrySet())
        {
            QName qname = getQName(entry.getKey()).getSecond();         // getQName(id) is never null
            qnameMap.put(qname, entry.getValue());
        }
        return qnameMap;
    }

    /**
     * @return      Returns a set of IDs mapping to the QNames provided.  If create is <tt>false</tt>
     *              then there will not be corresponding entries for the QNames that don't exist.
     *              So there is no guarantee that the returned set will be ordered the same or even
     *              contain the same number of elements as the original unless create is <tt>true</tt>.
     */
    public Set<Long> convertQNamesToIds(Set<QName> qnames, boolean create)
    {
        Set<Long> qnameIds = new HashSet<Long>(qnames.size(), 1.0F);
        for (QName qname : qnames)
        {
            Long qnameEntityId = null;
            if (create)
            {
                qnameEntityId = getOrCreateQName(qname).getFirst();     // getOrCreateQName(qname) is never null
            }
            else
            {
                Pair<Long, QName> qnamePair = getQName(qname);
                if (qnamePair == null)
                {
                    // No such qname and we are not creating one
                    continue;
                }
                else
                {
                    qnameEntityId = qnamePair.getFirst();
                }
            }
            if (qnameEntityId != null)
            {
                qnameIds.add(qnameEntityId);
            }
        }
        // Done
        return qnameIds;
    }
}
