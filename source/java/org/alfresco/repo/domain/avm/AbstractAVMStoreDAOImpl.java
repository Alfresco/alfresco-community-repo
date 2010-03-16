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
package org.alfresco.repo.domain.avm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.cache.lookup.EntityLookupCache;
import org.alfresco.repo.cache.lookup.EntityLookupCache.EntityLookupCallbackDAO;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.service.namespace.QName;
import org.springframework.dao.ConcurrencyFailureException;
import org.alfresco.util.Pair;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * Abstract implementation for AVMStore DAO.
 * <p>
 * This provides basic services such as caching but defers to the underlying implementation
 * for CRUD operations.
 * 
 * @author janv
 * @since 3.2
 */
public abstract class AbstractAVMStoreDAOImpl implements AVMStoreDAO
{
    private static final String CACHE_REGION_AVM_STORE = "AVMStore";
    private static final String CACHE_REGION_AVM_STORE_PROP = "AVMStoreProp";
    
    private final AVMStoreEntityCallbackDAO avmStoreEntityDaoCallback;
    private final AVMStorePropertyEntityCallbackDAO avmStorePropEntityDaoCallback;
    
    private QNameDAO qnameDAO;
    
    /**
     * Cache for the AVM store entity:<br/>
     * KEY: ID<br/>
     * VALUE: AVMStoreEntity<br/>
     * VALUE KEY: Name<br/>
     */
    private EntityLookupCache<Long, AVMStoreEntity, String> avmStoreCache;
    
    /**
     * Cache for the AVM store property entity:<br/>
     * KEY: Pair of IDs (store, qname)<br/>
     * VALUE: AVMStorePropertyEntity<br/>
     * VALUE KEY: None<br/>
     */
    private EntityLookupCache<Pair<Long, Long>, AVMStorePropertyEntity, Serializable> avmStorePropCache;
    
    /**
     * Set the cache to use for <b>avm_stores</b> lookups (optional).
     * 
     * @param avmStoreCache            the cache of IDs to AVMStoreEntities
     */
    public void setAvmStoreCache(SimpleCache<Serializable, Object> avmStoreCache)
    {
        this.avmStoreCache = new EntityLookupCache<Long, AVMStoreEntity, String>(
                avmStoreCache,
                CACHE_REGION_AVM_STORE,
                avmStoreEntityDaoCallback);
    }
    
    /**
     * Set the cache to use for <b>avm_store_properties</b> lookups (optional).
     * 
     * @param avmStorePropCache            the cache of IDs to AVMStorePropertyEntities
     */
    public void setAvmStorePropertyCache(SimpleCache<Serializable, Object> avmStorePropCache)
    {
        this.avmStorePropCache = new EntityLookupCache<Pair<Long, Long>, AVMStorePropertyEntity, Serializable>(
                avmStorePropCache,
                CACHE_REGION_AVM_STORE_PROP,
                avmStorePropEntityDaoCallback);
    }
    
    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }
    
    
    /**
     * Default constructor.
     * <p>
     * This sets up the DAO accessors to bypass any caching to handle the case where the caches are not
     * supplied in the setters.
     */
    public AbstractAVMStoreDAOImpl()
    {
        this.avmStoreEntityDaoCallback = new AVMStoreEntityCallbackDAO();
        this.avmStoreCache = new EntityLookupCache<Long, AVMStoreEntity, String>(avmStoreEntityDaoCallback);
        
        this.avmStorePropEntityDaoCallback = new AVMStorePropertyEntityCallbackDAO();
        this.avmStorePropCache = new EntityLookupCache<Pair<Long, Long>, AVMStorePropertyEntity, Serializable>(avmStorePropEntityDaoCallback);
    }
    
    /**
     * {@inheritDoc}
     */
    public AVMStoreEntity createStore(String name)
    {
        ParameterCheck.mandatory("name", name);
        
        AVMStoreEntity storeEntity = new AVMStoreEntity();
        
        storeEntity.setVersion(0L);
        storeEntity.setName(name);
        storeEntity.setVers(0L);
        
        Pair<Long, AVMStoreEntity> entityPair = avmStoreCache.getOrCreateByValue(storeEntity);
        return entityPair.getSecond();
    }
    
    /**
     * {@inheritDoc}
     */
    public AVMStoreEntity getStore(long storeId)
    {
        Pair<Long, AVMStoreEntity> entityPair = avmStoreCache.getByKey(storeId);
        if (entityPair == null)
        {
            return null;
        }
        return entityPair.getSecond();
    }
    
    /**
     * {@inheritDoc}
     */
    public AVMStoreEntity getStoreByRoot(long rootNodeId)
    {
        // TODO review - not via cache
        return getStoreEntityByRoot(rootNodeId);
    }
    
    /**
     * {@inheritDoc}
     */
    public AVMStoreEntity getStore(String name)
    {
        ParameterCheck.mandatory("name", name);
        
        AVMStoreEntity storeEntity = new AVMStoreEntity();
        storeEntity.setName(name);
        
        Pair<Long, AVMStoreEntity> entityPair = avmStoreCache.getByValue(storeEntity);
        if (entityPair == null)
        {
            return null;
        }
        return entityPair.getSecond();
    }
    
    /**
     * {@inheritDoc}
     */
    public List<AVMStoreEntity> getAllStores()
    {
        // not via cache
        return getAllStoreEntities();
    }
    
    /**
     * {@inheritDoc}
     */
    public void updateStore(AVMStoreEntity storeEntity)
    {
        ParameterCheck.mandatory("storeEntity", storeEntity);
        ParameterCheck.mandatory("storeEntity.getId()", storeEntity.getId());
        ParameterCheck.mandatory("storeEntity.getVers()", storeEntity.getVers());
        
        int updated = avmStoreCache.updateValue(storeEntity.getId(), storeEntity);
        if (updated < 1)
        {
            throw new ConcurrencyFailureException("AVMStore with ID (" + storeEntity.getId() + ") no longer exists or has been updated concurrently");
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void deleteStore(long storeId)
    {
        Pair<Long, AVMStoreEntity> entityPair = avmStoreCache.getByKey(storeId);
        if (entityPair == null)
        {
            return;
        }
        
        int deleted = avmStoreCache.deleteByKey(storeId);
        if (deleted < 1)
        {
            throw new ConcurrencyFailureException("AVMStore with ID " + storeId + " no longer exists");
        }
    }
    
    /**
     * Callback for <b>avm_stores</b> DAO
     */
    private class AVMStoreEntityCallbackDAO implements EntityLookupCallbackDAO<Long, AVMStoreEntity, String>
    {
        private final Pair<Long, AVMStoreEntity> convertEntityToPair(AVMStoreEntity storeEntity)
        {
            if (storeEntity == null)
            {
                return null;
            }
            else
            {
                return new Pair<Long, AVMStoreEntity>(storeEntity.getId(), storeEntity);
            }
        }
        
        public String getValueKey(AVMStoreEntity value)
        {
            return value.getName();
        }
        
        public Pair<Long, AVMStoreEntity> createValue(AVMStoreEntity value)
        {
            AVMStoreEntity entity = createStoreEntity(value);
            return convertEntityToPair(entity);
        }
        
        public Pair<Long, AVMStoreEntity> findByKey(Long key)
        {
            AVMStoreEntity entity = getStoreEntity(key);
            return convertEntityToPair(entity);
        }
        
        public Pair<Long, AVMStoreEntity> findByValue(AVMStoreEntity value)
        {
            if ((value == null) || (value.getName() == null))
            {
                throw new AlfrescoRuntimeException("Unexpected: AVMStoreEntity / name must not be null");
            }
            return convertEntityToPair(getStoreEntity(value.getName()));
        }
        
        public int updateValue(Long key, AVMStoreEntity value)
        {
            return updateStoreEntity(value);
        }
        
        public int deleteByKey(Long key)
        {
            return deleteStoreEntity(key);
        }
        
        public int deleteByValue(AVMStoreEntity value)
        {
            // TODO
            throw new UnsupportedOperationException("deleteByValue(AVMStoreEntity)");
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void clearStoreEntityCache()
    {
        avmStoreCache.clear();
    }
    
    protected abstract AVMStoreEntity getStoreEntity(long id);
    protected abstract AVMStoreEntity getStoreEntity(String name);
    protected abstract AVMStoreEntity getStoreEntityByRoot(long rootNodeId);
    protected abstract List<AVMStoreEntity> getAllStoreEntities();
    protected abstract AVMStoreEntity createStoreEntity(AVMStoreEntity storeEntity);
    protected abstract int deleteStoreEntity(long id);
    protected abstract int updateStoreEntity(AVMStoreEntity storeEntity);
    
    /**
     * {@inheritDoc}
     */
    public void createOrUpdateStoreProperty(long storeId, QName qname, PropertyValue value)
    {
        ParameterCheck.mandatory("qname", qname);
        ParameterCheck.mandatory("value", value);
        
        // Get the persistent ID for the QName
        Pair<Long, QName> qnamePair = qnameDAO.getOrCreateQName(qname);
        Long qnameId = qnamePair.getFirst();
        
        AVMStorePropertyEntity propEntity = new AVMStorePropertyEntity(storeId, qnameId, value);
        
        Pair<Long, Long> key = new Pair<Long, Long>(storeId, propEntity.getQnameId());
        Pair<Pair<Long, Long>, AVMStorePropertyEntity> entityPair = avmStorePropCache.getByKey(key);
        
        if (entityPair != null)
        {
            int updated = avmStorePropCache.updateValue(key, propEntity);
            if (updated < 1)
            {
                throw new ConcurrencyFailureException("AVMStorePropertyEntity with IDs (" + propEntity.getAvmStoreId() + ", " + propEntity.getQnameId() + ") no longer exists");
            }
        }
        else
        {
            avmStorePropCache.getOrCreateByValue(propEntity);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public PropertyValue getStoreProperty(long storeId, QName qname)
    {
        // Get the persistent ID for the QName
        Pair<Long, QName> qnamePair = qnameDAO.getQName(qname);
        if (qnamePair != null)
        {
            Long qnameId = qnamePair.getFirst();
            
            Pair<Long, Long> key = new Pair<Long, Long>(storeId, qnameId);
            Pair<Pair<Long, Long>, AVMStorePropertyEntity> entityPair = avmStorePropCache.getByKey(key);
            if (entityPair == null)
            {
                return null;
            }
            return entityPair.getSecond();
        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    public Map<QName, PropertyValue> getStoreProperties(long storeId)
    {
        // not via cache
        List<AVMStorePropertyEntity> propEntities = getStorePropertyEntities(storeId);
        
        Map<QName, PropertyValue> props = new HashMap<QName, PropertyValue>(propEntities.size());
        for (AVMStorePropertyEntity propEntity : propEntities)
        {
            Pair<Long, QName> qnamePair = qnameDAO.getQName(propEntity.getQnameId());
            if (qnamePair != null)
            {
                props.put(qnamePair.getSecond(), propEntity);
            }
        }
        return props;
    }
    
    /**
     * {@inheritDoc}
     */
    public Map<String, Map<QName, PropertyValue>> getStorePropertiesByKeyPattern(String uriPattern, String localNamePattern)
    {
        ParameterCheck.mandatoryString("uriPattern", uriPattern);
        ParameterCheck.mandatoryString("localNamePattern", localNamePattern);
        
        // not via cache
        List<AVMStorePropertyEntity> spEntities = getStorePropertyEntitiesByKeyPattern(uriPattern, localNamePattern);
        
        Map<String, Map<QName, PropertyValue>> results = new HashMap<String, Map<QName, PropertyValue>>(10);
        
        for (AVMStorePropertyEntity spEntity : spEntities)
        {
            String storeName = getStore(spEntity.getAvmStoreId()).getName();
            
            Pair<Long, QName> qnamePair = qnameDAO.getQName(spEntity.getQnameId());
            if (qnamePair != null)
            {
                QName propQName = qnamePair.getSecond();
                
                Map<QName, PropertyValue> pairs = null;
                if ((pairs = results.get(storeName)) == null)
                {
                    pairs = new HashMap<QName, PropertyValue>();
                    results.put(storeName, pairs);
                }
                pairs.put(propQName, spEntity);
            }
            
        }
        return results;
    }
    
    /**
     * {@inheritDoc}
     */
    public Map<QName, PropertyValue> getStorePropertiesByStoreAndKeyPattern(long storeId, String uriPattern, String localNamePattern)
    {
        ParameterCheck.mandatoryString("uriPattern", uriPattern);
        ParameterCheck.mandatoryString("localNamePattern", localNamePattern);
        
        // not via cache
        List<AVMStorePropertyEntity> propEntities = getStorePropertyEntitiesByStoreAndKeyPattern(storeId, uriPattern, localNamePattern);
        
        Map<QName, PropertyValue> props = new HashMap<QName, PropertyValue>(propEntities.size());
        for (AVMStorePropertyEntity propEntity : propEntities)
        {
            Pair<Long, QName> qnamePair = qnameDAO.getQName(propEntity.getQnameId());
            if (qnamePair != null)
            {
                props.put(qnamePair.getSecond(), propEntity);
            }
        }
        return props;
    }
    
    /**
     * {@inheritDoc}
     */
    public void deleteStoreProperty(long storeId, QName qname)
    {
        // Get the persistent ID for the QName
        Pair<Long, QName> qnamePair = qnameDAO.getQName(qname);
        if (qnamePair != null)
        {
            Long qnameId = qnamePair.getFirst();
            
            Pair<Long, Long> key = new Pair<Long, Long>(storeId, qnameId);
            Pair<Pair<Long, Long>, AVMStorePropertyEntity> entityPair = avmStorePropCache.getByKey(key);
            if (entityPair == null)
            {
                return;
            }
            
            int deleted = avmStorePropCache.deleteByKey(key);
            if (deleted < 1)
            {
                throw new ConcurrencyFailureException("AVMStoreProperty with key (" + storeId + ", " + qnameId + ") no longer exists");
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void deleteStoreProperties(long storeId)
    {
        Map<QName, PropertyValue> props = getStoreProperties(storeId);
        if (props.size() == 0)
        {
            return;
        }
        
        for (QName qname : props.keySet())
        {
            deleteStoreProperty(storeId, qname);
        }
        
        // TODO single delete + cache(s)
        
        /*
        int deleted = deleteStorePropertyEntities(storeId);
        if (deleted < 1)
        {
            throw new ConcurrencyFailureException("AVMStoreProperties for store ID " + storeId + " no longer exist");
        }
        
        // TODO clear store property cache for this store id
        */
    }
    
    /**
     * Callback for <b>avm_store_properties</b> DAO
     */
    private class AVMStorePropertyEntityCallbackDAO implements EntityLookupCallbackDAO<Pair<Long, Long>, AVMStorePropertyEntity, Serializable>
    {
        private final Pair<Pair<Long, Long>, AVMStorePropertyEntity> convertEntityToPair(AVMStorePropertyEntity storePropEntity)
        {
            if (storePropEntity == null)
            {
                return null;
            }
            else
            {
                Pair<Long, Long> key = new Pair<Long, Long>(storePropEntity.getAvmStoreId(), storePropEntity.getQnameId());
                return new Pair<Pair<Long, Long>, AVMStorePropertyEntity>(key, storePropEntity);
            }
        }
        
        public Serializable getValueKey(AVMStorePropertyEntity value)
        {
            return null;
        }
        
        public Pair<Pair<Long, Long>, AVMStorePropertyEntity> createValue(AVMStorePropertyEntity value)
        {
            insertStorePropertyEntity(value);
            return convertEntityToPair(value);
        }
        
        public Pair<Pair<Long, Long>, AVMStorePropertyEntity> findByKey(Pair<Long, Long> key)
        {
            AVMStorePropertyEntity entity = getStorePropertyEntity(key.getFirst(), key.getSecond());
            return convertEntityToPair(entity);
        }
        
        public Pair<Pair<Long, Long>, AVMStorePropertyEntity> findByValue(AVMStorePropertyEntity value)
        {
            if ((value.getAvmStoreId() != null) && (value.getQnameId() != null))
            {
                return findByKey(new Pair<Long, Long>(value.getAvmStoreId(), value.getQnameId()));
            }
            return null;
        }
        
        public int updateValue(Pair<Long, Long> key, AVMStorePropertyEntity value)
        {
            return updateStorePropertyEntity(value);
        }
        
        public int deleteByKey(Pair<Long, Long> key)
        {
            return deleteStorePropertyEntity(key.getFirst(), key.getSecond());
        }
        
        public int deleteByValue(AVMStorePropertyEntity value)
        {
            throw new UnsupportedOperationException("deleteByValue(AVMStorePropertyEntity)");
        }
    }
    
    protected abstract void insertStorePropertyEntity(AVMStorePropertyEntity propEntity);
    protected abstract int updateStorePropertyEntity(AVMStorePropertyEntity propEntity);
    protected abstract AVMStorePropertyEntity getStorePropertyEntity(long storeId, long qnameId);
    protected abstract List<AVMStorePropertyEntity> getStorePropertyEntities(long storeId);
    protected abstract List<AVMStorePropertyEntity> getStorePropertyEntitiesByKeyPattern(String uriPattern, String localNamePattern);
    protected abstract List<AVMStorePropertyEntity> getStorePropertyEntitiesByStoreAndKeyPattern(long storeId, String uriPattern, String localNamePattern);
    protected abstract int deleteStorePropertyEntity(long storeId, long qnameId);
    protected abstract int deleteStorePropertyEntities(long storeId);
}
