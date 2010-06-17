/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.domain.qname;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.cache.lookup.EntityLookupCache;
import org.alfresco.repo.cache.lookup.EntityLookupCache.EntityLookupCallbackDAOAdaptor;
import org.alfresco.service.namespace.QName;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.alfresco.util.Pair;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * Abstract implementation of the QName and Namespace DAO interface.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public abstract class AbstractQNameDAOImpl implements QNameDAO
{
    private static final String CACHE_REGION_NAMESPACE = "Namespace";
    private static final String CACHE_REGION_QNAME = "QName";
    
    /**
     * Cache for the Namespace values:<br/>
     * KEY: ID<br/>
     * VALUE: Namespace<br/>
     * VALUE KEY: Namespace<br/>
     */
    private EntityLookupCache<Long, String, String> namespaceCache;
    /**
     * Cache for the QName values:<br/>
     * KEY: ID<br/>
     * VALUE: QName<br/>
     * VALUE KEY: QName<br/>
     */
    private EntityLookupCache<Long, QName, QName> qnameCache;
    
    /**
     * Default constructor.
     * <p>
     * This sets up the DAO accessors to bypass any caching to handle the case where the caches are not
     * supplied in the setters.
     */
    protected AbstractQNameDAOImpl()
    {
        this.namespaceCache = new EntityLookupCache<Long, String, String>(new NamespaceCallbackDAO());
        this.qnameCache = new EntityLookupCache<Long, QName, QName>(new QNameCallbackDAO());
    }
    
    /**
     * Set the cache that maintains the ID-Namespace mappings and vice-versa.
     * 
     * @param namespaceCache        the cache
     */
    public void setNamespaceCache(SimpleCache<Long, String> namespaceCache)
    {
        this.namespaceCache = new EntityLookupCache<Long, String, String>(
                namespaceCache,
                CACHE_REGION_NAMESPACE,
                new NamespaceCallbackDAO());
    }

    /**
     * Set the cache that maintains the ID-Namespace mappings and vice-versa.
     * 
     * @param qnameCache            the cache
     */
    public void setQnameCache(SimpleCache<Long, QName> qnameCache)
    {
        this.qnameCache = new EntityLookupCache<Long, QName, QName>(
                qnameCache,
                CACHE_REGION_QNAME,
                new QNameCallbackDAO());
    }

    //================================
    // 'alf_namespace' accessors
    //================================

    public Pair<Long, String> getNamespace(Long id)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("Cannot look up entity by null ID.");
        }
        Pair<Long, String> entityPair = namespaceCache.getByKey(id);
        if (entityPair == null)
        {
            throw new DataIntegrityViolationException("No namespace exists for ID " + id);
        }
        return entityPair;
    }
    
    public Pair<Long, String> getNamespace(String namespaceUri)
    {
        if (namespaceUri == null)
        {
            throw new IllegalArgumentException("Namespace URI cannot be null");
        }
        Pair<Long, String> entityPair = namespaceCache.getByValue(namespaceUri);
        return entityPair;
    }

    public Pair<Long, String> getOrCreateNamespace(String namespaceUri)
    {
        if (namespaceUri == null)
        {
            throw new IllegalArgumentException("Namespace URI cannot be null");
        }
        Pair<Long, String> entityPair = namespaceCache.getOrCreateByValue(namespaceUri);
        return entityPair;
    }

    public void updateNamespace(String oldNamespaceUri, String newNamespaceUri)
    {
        ParameterCheck.mandatory("newNamespaceUri", newNamespaceUri);

        Pair<Long, String> oldEntityPair = getNamespace(oldNamespaceUri);   // incl. null check
        if (oldEntityPair == null)
        {
            throw new DataIntegrityViolationException(
                    "Cannot update namespace as it doesn't exist: " + oldNamespaceUri);
        }
        // Find the value
        int updated = namespaceCache.updateValue(oldEntityPair.getFirst(), newNamespaceUri);
        if (updated != 1)
        {
            throw new ConcurrencyFailureException(
                    "Incorrect update count: \n" +
                    "   Namespace:    " + oldNamespaceUri + "\n" +
                    "   Rows Updated: " + updated);
        }
        // All the QNames need to be dumped
        qnameCache.clear();
        // Done
    }

    /**
     * Callback for <b>alf_namespace</b> DAO.
     */
    private class NamespaceCallbackDAO extends EntityLookupCallbackDAOAdaptor<Long, String, String>
    {
        @Override
        public String getValueKey(String value)
        {
            return value;
        }

        public Pair<Long, String> findByKey(Long id)
        {
            NamespaceEntity entity = findNamespaceEntityById(id);
            if (entity == null)
            {
                return null;
            }
            else
            {
                return new Pair<Long, String>(id, entity.getUriSafe());
            }
        }

        @Override
        public Pair<Long, String> findByValue(String uri)
        {
            NamespaceEntity entity = findNamespaceEntityByUri(uri);
            if (entity == null)
            {
                return null;
            }
            else
            {
                return new Pair<Long, String>(entity.getId(), uri);
            }
        }
        
        public Pair<Long, String> createValue(String uri)
        {
            NamespaceEntity entity = createNamespaceEntity(uri);
            return new Pair<Long, String>(entity.getId(), uri);
        }

        @Override
        public int updateValue(Long id, String uri)
        {
            NamespaceEntity entity = findNamespaceEntityById(id);
            if (entity == null)
            {
                // Client can decide if this is a problem
                return 0;
            }
            return updateNamespaceEntity(entity, uri);
        }
    }
    
    protected abstract NamespaceEntity findNamespaceEntityById(Long id);
    protected abstract NamespaceEntity findNamespaceEntityByUri(String uri);
    protected abstract NamespaceEntity createNamespaceEntity(String uri);
    protected abstract int updateNamespaceEntity(NamespaceEntity entity, String uri);
    
    //================================
    // 'alf_qname' accessors
    //================================

    public Pair<Long, QName> getQName(Long id)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("Cannot look up entity by null ID.");
        }
        Pair<Long, QName> entityPair = qnameCache.getByKey(id);
        if (entityPair == null)
        {
            throw new DataIntegrityViolationException("No qname exists for ID " + id);
        }
        return entityPair;
    }

    public Pair<Long, QName> getQName(final QName qname)
    {
        if (qname == null)
        {
            throw new IllegalArgumentException("QName cannot be null");
        }
        Pair<Long, QName> entityPair = qnameCache.getByValue(qname);
        return entityPair;
    }

    public Pair<Long, QName> getOrCreateQName(QName qname)
    {
        if (qname == null)
        {
            throw new IllegalArgumentException("QName cannot be null");
        }
        Pair<Long, QName> entityPair = qnameCache.getOrCreateByValue(qname);
        return entityPair;
    }

    public Pair<Long, QName> updateQName(QName qnameOld, QName qnameNew)
    {
        if (qnameOld == null|| qnameNew == null)
        {
            throw new IllegalArgumentException("QName cannot be null");
        }
        if (qnameOld.equals(qnameNew))
        {
            throw new IllegalArgumentException("Cannot update QNames: they are the same");
        }
        // See if the old QName exists
        Pair<Long, QName> qnameOldPair = qnameCache.getByValue(qnameOld);
        if (qnameOldPair == null)
        {
            throw new IllegalArgumentException("Cannot rename QName.  QName " + qnameOld + " does not exist");
        }
        // See if the new QName exists
        if (qnameCache.getByValue(qnameNew) != null)
        {
            throw new IllegalArgumentException("Cannot rename QName.  QName " + qnameNew + " already exists");
        }
        // Update
        Long qnameId = qnameOldPair.getFirst();
        int updated = qnameCache.updateValue(qnameId, qnameNew);
        if (updated != 1)
        {
            throw new ConcurrencyFailureException("Failed to update QName entity " + qnameId);
        }
        return new Pair<Long, QName>(qnameId, qnameNew);
    }

    /**
     * Callback for <b>alf_qname</b> DAO.
     */
    private class QNameCallbackDAO extends EntityLookupCallbackDAOAdaptor<Long, QName, QName>
    {
        @Override
        public QName getValueKey(QName value)
        {
            return value;
        }

        public Pair<Long, QName> findByKey(Long id)
        {
            QNameEntity entity = findQNameEntityById(id);
            if (entity == null)
            {
                return null;
            }
            else
            {
                Long namespaceId = entity.getNamespaceId();
                String uri = getNamespace(namespaceId).getSecond();
                String localName = entity.getLocalNameSafe();
                QName qname = QName.createQName(uri, localName);
                return new Pair<Long, QName>(id, qname);
            }
        }

        @Override
        public Pair<Long, QName> findByValue(QName qname)
        {
            String uri = qname.getNamespaceURI();
            String localName = qname.getLocalName();
            Pair<Long, String> namespaceEntity = getNamespace(uri);
            if (namespaceEntity == null)
            {
                // There is no match on NS, so there is no QName like this
                return null;
            }
            Long nsId = namespaceEntity.getFirst();
            QNameEntity entity = findQNameEntityByNamespaceAndLocalName(nsId, localName);
            if (entity == null)
            {
                return null;
            }
            else
            {
                return new Pair<Long, QName>(entity.getId(), qname);
            }
        }
        
        public Pair<Long, QName> createValue(QName qname)
        {
            String uri = qname.getNamespaceURI();
            String localName = qname.getLocalName();
            // Create namespace
            Pair<Long, String> namespaceEntity = getOrCreateNamespace(uri);
            Long nsId = namespaceEntity.getFirst();
            // Create QName
            QNameEntity entity = createQNameEntity(nsId, localName);
            return new Pair<Long, QName>(entity.getId(), qname);
        }

        @Override
        public int updateValue(Long id, QName qname)
        { 
            String uri = qname.getNamespaceURI();
            String localName = qname.getLocalName();

            QNameEntity entity = findQNameEntityById(id);
            if (entity == null)
            {
                // No chance of updating
                return 0;
            }

            // Create namespace
            Pair<Long, String> namespaceEntity = getOrCreateNamespace(uri);
            Long nsId = namespaceEntity.getFirst();
            // Create QName
            return updateQNameEntity(entity, nsId, localName);
        }
    }
    
    protected abstract QNameEntity findQNameEntityById(Long id);
    protected abstract QNameEntity findQNameEntityByNamespaceAndLocalName(Long nsId, String localName);
    protected abstract QNameEntity createQNameEntity(Long nsId, String localName);
    protected abstract int updateQNameEntity(QNameEntity entity, Long nsId, String localName);
    
    
    //================================
    // Utility method implementations
    //================================

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
