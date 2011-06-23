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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.cache.NullCache;
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
 * Abstract implementation for AVMNode DAO.
 * <p>
 * This provides basic services such as caching but defers to the underlying implementation
 * for CRUD operations.
 * 
 * @author janv
 * @since 3.2
 */
public abstract class AbstractAVMNodeDAOImpl implements AVMNodeDAO
{
    private static final String CACHE_REGION_AVM_NODE = "AVMNode";
    private static final String CACHE_REGION_AVM_NODE_PROP = "AVMNodeProp";
    
    private final AVMNodeEntityCallbackDAO avmNodeEntityDaoCallback;
    private final AVMNodePropertyEntityCallbackDAO avmNodePropEntityDaoCallback;
    
    private QNameDAO qnameDAO;
    
    /**
     * Cache for the AVM node entity:<br/>
     * KEY: ID (node)<br/>
     * VALUE: AVMNodeEntity<br/>
     * VALUE KEY: None<br/>
     */
    private EntityLookupCache<Long, AVMNodeEntity, Serializable> avmNodeCache;
    
    /**
     * Cache for the AVM node property entity:<br/>
     * KEY: Pair of IDs (node, qname)<br/>
     * VALUE: AVMNodePropertyEntity<br/>
     * VALUE KEY: None<br/>
     */
    private EntityLookupCache<Pair<Long, Long>, AVMNodePropertyEntity, Serializable> avmNodePropCache;
    
    /**
     * Set the cache to use for <b>avm_aspects</b> lookups (optional).
     * 
     * @param avmNodeAspectsCache
     */
    private SimpleCache<Serializable, Object> avmNodeAspectsCache;
    
    /**
     * Set the cache to use for <b>avm_nodes</b> lookups (optional).
     * 
     * @param avmNodeCache            the cache of IDs to AVMNodeEntities
     */
    public void setAvmNodeCache(SimpleCache<Serializable, Object> avmNodeCache)
    {
        this.avmNodeCache = new EntityLookupCache<Long, AVMNodeEntity, Serializable>(
                avmNodeCache,
                CACHE_REGION_AVM_NODE,
                avmNodeEntityDaoCallback);
    }
    
    /**
     * Set the cache to use for <b>avm_node_properties</b> lookups (optional).
     * 
     * @param avmNodePropCache            the cache of IDs to AVMNodePropertyEntities
     */
    public void setAvmNodePropertyCache(SimpleCache<Serializable, Object> avmNodePropCache)
    {
        this.avmNodePropCache = new EntityLookupCache<Pair<Long, Long>, AVMNodePropertyEntity, Serializable>(
                avmNodePropCache,
                CACHE_REGION_AVM_NODE_PROP,
                avmNodePropEntityDaoCallback);
    }
    
    /**
     * Set the cache to use for <b>avm_aspects</b> lookups (optional).
     * 
     * @param avmNodeAspectsCache
     */
    public void setAvmNodeAspectsCache(SimpleCache<Serializable, Object> avmNodeAspectsCache)
    {
        this.avmNodeAspectsCache = avmNodeAspectsCache;
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
    @SuppressWarnings("unchecked")
    public AbstractAVMNodeDAOImpl()
    {
        this.avmNodeEntityDaoCallback = new AVMNodeEntityCallbackDAO();
        this.avmNodeCache = new EntityLookupCache<Long, AVMNodeEntity, Serializable>(avmNodeEntityDaoCallback);
        
        this.avmNodePropEntityDaoCallback = new AVMNodePropertyEntityCallbackDAO();
        this.avmNodePropCache = new EntityLookupCache<Pair<Long, Long>, AVMNodePropertyEntity, Serializable>(avmNodePropEntityDaoCallback);
        
        this.avmNodeAspectsCache = (SimpleCache<Serializable, Object>)new NullCache();
    }
    
    public AVMNodeEntity createNode(AVMNodeEntity nodeEntity)
    {
        ParameterCheck.mandatory("nodeEntity", nodeEntity);
        
        nodeEntity.setVers(0L);
        
        Pair<Long, AVMNodeEntity> entityPair = avmNodeCache.getOrCreateByValue(nodeEntity);
        return entityPair.getSecond();
    }
    
    /**
     * {@inheritDoc}
     */
    public AVMNodeEntity getNode(long nodeId)
    {
        Pair<Long, AVMNodeEntity> entityPair = avmNodeCache.getByKey(nodeId);
        if (entityPair == null)
        {
            // cache-only operation: belts-and-braces
            avmNodeCache.removeByKey(nodeId);
            
            throw new ConcurrencyFailureException("getNode: "+nodeId);
        }
        return entityPair.getSecond();
    }
    
    /**
     * {@inheritDoc}
     */
    public void clearNodeEntityCache()
    {
        avmNodeCache.clear();
    }
    
    /**
     * {@inheritDoc}
     */
    public void updateNode(AVMNodeEntity nodeEntity)
    {
        ParameterCheck.mandatory("nodeEntity", nodeEntity);
        ParameterCheck.mandatory("nodeEntity.getId()", nodeEntity.getId());
        ParameterCheck.mandatory("nodeEntity.getVers()", nodeEntity.getVers());
        
        int updated = avmNodeCache.updateValue(nodeEntity.getId(), nodeEntity);
        if (updated < 1)
        {
            throw new ConcurrencyFailureException("AVMNode with ID (" + nodeEntity.getId() + ") no longer exists or has been updated concurrently");
        }
    }
    
    /**
     * {@inheritDoc}
     * @deprecated
     */
    public void updateNodeModTimeAndGuid(AVMNodeEntity nodeEntity)
    {
        ParameterCheck.mandatory("nodeEntity", nodeEntity);
        ParameterCheck.mandatory("nodeEntity.getId()", nodeEntity.getId());
        ParameterCheck.mandatory("nodeEntity.getGuid()", nodeEntity.getGuid());
        ParameterCheck.mandatory("nodeEntity.getModifiedDate()", nodeEntity.getModifiedDate());
        ParameterCheck.mandatory("nodeEntity.getVers()", nodeEntity.getVers());
        
        int updated = updateNodeEntityModTimeAndGuid(nodeEntity);
        if (updated < 1)
        {
            throw new ConcurrencyFailureException("AVMNode with ID (" + nodeEntity.getId() + ") no longer exists or has been updated concurrently");
        }
        
        // update cache
        avmNodeCache.removeByKey(nodeEntity.getId());
        avmNodeCache.getByKey(nodeEntity.getId());
    }
    
    /**
     * {@inheritDoc}
     * @deprecated
     */
    public void updateNodeModTimeAndContentData(AVMNodeEntity nodeEntity)
    {
        ParameterCheck.mandatory("nodeEntity", nodeEntity);
        ParameterCheck.mandatory("nodeEntity.getId()", nodeEntity.getId());
        ParameterCheck.mandatory("nodeEntity.getModifiedDate()", nodeEntity.getModifiedDate());
        ParameterCheck.mandatory("nodeEntity.getVers()", nodeEntity.getVers());
        
        int updated = updateNodeEntityModTimeAndContentData(nodeEntity);
        if (updated < 1)
        {
            throw new ConcurrencyFailureException("AVMNode with ID (" + nodeEntity.getId() + ") no longer exists or has been updated concurrently");
        }
        
        // update cache
        avmNodeCache.removeByKey(nodeEntity.getId());
        avmNodeCache.getByKey(nodeEntity.getId());
    }
    
    /**
     * {@inheritDoc}
     */
    public List<AVMNodeEntity> getNodesNewInStore(long storeId)
    {
        return getNodeEntitiesNewInStore(storeId);
    }
    
    /**
     * {@inheritDoc}
     */
    public List<AVMNodeEntity> getLayeredNodesNewInStore(long storeId)
    {
        return getLayeredNodeEntitiesNewInStore(storeId);
    }
    
    /**
     * {@inheritDoc}
     */
    public List<Long> getLayeredNodesNewInStoreIDs(long storeId)
    {
        return getLayeredNodeEntityIdsNewInStore(storeId);
    }
    
    /**
     * {@inheritDoc}
     */
    public List<AVMNodeEntity> getNodeOrphans(int maxSize)
    {
        return getNodeEntityOrphans(maxSize);
    }
    
    /**
     * {@inheritDoc}
     */
    public void updateNodesClearNewInStore(long storeId)
    {
        updateNodeEntitiesClearNewInStore(storeId);
    }
    
    /**
     * {@inheritDoc}
     */
    public void deleteNode(long nodeId)
    {
        Pair<Long, AVMNodeEntity> entityPair = avmNodeCache.getByKey(nodeId);
        if (entityPair == null)
        {
            return;
        }
        
        int deleted = avmNodeCache.deleteByKey(nodeId);
        if (deleted < 1)
        {
            throw new ConcurrencyFailureException("AVMNode with ID " + nodeId + " no longer exists");
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public List<AVMNodeEntity> getAllLayeredDirectories()
    {
        return getAllLayeredDirectoryNodeEntities();
    }
    
    /**
     * {@inheritDoc}
     */
    public List<AVMNodeEntity> getAllLayeredFiles()
    {
        return getAllLayeredFileNodeEntities();
    }
    
    /**
     * {@inheritDoc}
     */
    public void getContentUrls(ContentUrlHandler handler)
    {
        getPlainFileContentUrls(handler);
    }
    
    /**
     * Callback for <b>avm_nodes</b> DAO
     */
    private class AVMNodeEntityCallbackDAO implements EntityLookupCallbackDAO<Long, AVMNodeEntity, Serializable>
    {
        private final Pair<Long, AVMNodeEntity> convertEntityToPair(AVMNodeEntity nodeEntity)
        {
            if (nodeEntity == null)
            {
                return null;
            }
            else
            {
                return new Pair<Long, AVMNodeEntity>(nodeEntity.getId(), nodeEntity);
            }
        }
        
        public Serializable getValueKey(AVMNodeEntity value)
        {
            return null;
        }
        
        public Pair<Long, AVMNodeEntity> createValue(AVMNodeEntity value)
        {
            AVMNodeEntity entity = createNodeEntity(value);
            return convertEntityToPair(entity);
        }
        
        public Pair<Long, AVMNodeEntity> findByKey(Long key)
        {
            AVMNodeEntity entity = getNodeEntity(key);
            return convertEntityToPair(entity);
        }
        
        public Pair<Long, AVMNodeEntity> findByValue(AVMNodeEntity value)
        {
            if ((value != null) && (value.getId() != null))
            {
                return findByKey(value.getId());
            }
            return null;
        }
        
        public int updateValue(Long key, AVMNodeEntity value)
        {
            return updateNodeEntity(value);
        }
        
        public int deleteByKey(Long key)
        {
            return deleteNodeEntity(key);
        }
        
        public int deleteByValue(AVMNodeEntity value)
        {
            // TODO
            throw new UnsupportedOperationException("deleteByValue(AVMNodeEntity)");
        }
    }
    
    protected abstract AVMNodeEntity createNodeEntity(AVMNodeEntity nodeEntity);
    protected abstract AVMNodeEntity getNodeEntity(long nodeId);
    protected abstract int updateNodeEntity(AVMNodeEntity nodeEntity);
    protected abstract int updateNodeEntityModTimeAndGuid(AVMNodeEntity nodeEntity);
    protected abstract int updateNodeEntityModTimeAndContentData(AVMNodeEntity nodeEntity);
    protected abstract int deleteNodeEntity(long nodeId);
    protected abstract void updateNodeEntitiesClearNewInStore(long storeId);
    protected abstract List<AVMNodeEntity> getNodeEntitiesNewInStore(long storeId);
    protected abstract List<AVMNodeEntity> getLayeredNodeEntitiesNewInStore(long storeId);
    protected abstract List<Long> getLayeredNodeEntityIdsNewInStore(long storeId);
    protected abstract List<AVMNodeEntity> getNodeEntityOrphans(int maxSize);
    protected abstract List<AVMNodeEntity> getAllLayeredDirectoryNodeEntities();
    protected abstract List<AVMNodeEntity> getAllLayeredFileNodeEntities();
    protected abstract void getPlainFileContentUrls(ContentUrlHandler handler);
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public Set<QName> getAspects(long nodeId)
    {
        Set<QName> aspects = (Set<QName>)avmNodeAspectsCache.get(nodeId);
        if (aspects != null)
        {
            return aspects;
        }
        
        Set<QName> aspectQNames = null;
        
        // Get it from the DB
        List<Long> aspectIds = getAspectEntities(nodeId);
        if (aspectIds != null)
        {
            // Convert to QNames
            aspectQNames = qnameDAO.convertIdsToQNames(new HashSet(aspectIds));
        }
        else
        {
            aspectQNames = new HashSet<QName>(0);
        }
        
        // Cache it
        avmNodeAspectsCache.put(nodeId, aspectQNames);
        
        return aspectQNames;
    }
    
    /**
     * {@inheritDoc}
     */
    public void createAspect(long nodeId, QName qname)
    {
        Set<QName> aspects = getAspects(nodeId);
        if (aspects.contains(qname))
        {
            return;
        }
        
        // Get the persistent ID for the QName
        Pair<Long, QName> qnamePair = qnameDAO.getOrCreateQName(qname);
        if (qnamePair != null)
        {
            Long qnameId  = qnamePair.getFirst();
            createAspectEntity(nodeId, qnameId);
            
            // Cache it
            aspects.add(qname);
            avmNodeAspectsCache.put(new Long(nodeId), aspects);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void deleteAspect(long nodeId, QName qname)
    {
        Set<QName> aspects = getAspects(nodeId);
        if (! aspects.contains(qname))
        {
            return;
        }
        
        // Get the persistent ID for the QName
        Pair<Long, QName> qnamePair = qnameDAO.getQName(qname);
        if (qnamePair != null)
        {
            Long qnameId  = qnamePair.getFirst();
            
            int deleted = deleteAspectEntity(nodeId, qnameId);
            if (deleted < 1)
            {
                throw new ConcurrencyFailureException("AVMNodeAspect (" + nodeId + ", " + qnameId + ") no longer exists");
            }
            
            // Remove from cache
            aspects.remove(qname);
            avmNodeAspectsCache.put(new Long(nodeId), aspects);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void deleteAspects(long nodeId)
    {
        Set<QName> naEntities = getAspects(nodeId);
        if (naEntities.size() == 0)
        {
            return;
        }
        
        int deleted = deleteAspectEntities(nodeId);
        if (deleted < 1)
        {
            throw new ConcurrencyFailureException("AVMNodeAspects for node ID " + nodeId + " no longer exist");
        }
        
        // Remove from cache
        avmNodeAspectsCache.remove(nodeId);
    }
    
    protected abstract List<Long> getAspectEntities(long nodeId);
    protected abstract void createAspectEntity(long nodeId, long qnameId);
    protected abstract int deleteAspectEntity(long nodeId, long qnameId);
    protected abstract int deleteAspectEntities(long nodeId);
    
    /**
     * {@inheritDoc}
     */
    public void createOrUpdateNodeProperty(long nodeId, QName qname, PropertyValue value)
    {
        ParameterCheck.mandatory("qname", qname);
        
        // Get the persistent ID for the QName
        Pair<Long, QName> qnamePair = qnameDAO.getOrCreateQName(qname);
        Long qnameId = qnamePair.getFirst();
        
        AVMNodePropertyEntity propEntity = new AVMNodePropertyEntity(nodeId, qnameId, value);
        
        Pair<Long, Long> key = new Pair<Long, Long>(nodeId, propEntity.getQnameId());
        Pair<Pair<Long, Long>, AVMNodePropertyEntity> entityPair = avmNodePropCache.getByKey(key);
        
        if (entityPair != null)
        {
            int updated = avmNodePropCache.updateValue(key, propEntity);
            if (updated < 1)
            {
                throw new ConcurrencyFailureException("AVMNodePropertyEntity with key (" + propEntity.getNodeId() + ", " + propEntity.getQnameId() + ") no longer exists");
            }
        }
        else
        {
            avmNodePropCache.getOrCreateByValue(propEntity);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    /*
    public AVMNodePropertyEntity getNodeProperty(long nodeId, QName qname)
    {
        // Get the persistent ID for the QName
        Pair<Long, QName> qnamePair = qnameDAO.getQName(qname);
        if (qnamePair != null)
        {
            Long qnameId = qnamePair.getFirst();
            
            Pair<Long, Long> key = new Pair<Long, Long>(nodeId, qnameId);
            Pair<Pair<Long, Long>, AVMNodePropertyEntity> entityPair = avmNodePropCache.getByKey(key);
            if (entityPair == null)
            {
                return null;
            }
            return entityPair.getSecond();
        }
    }
    */
    
    /**
     * {@inheritDoc}
     */
    public Map<QName, PropertyValue> getNodeProperties(long nodeId)
    {
        // TODO not via cache
        List<AVMNodePropertyEntity> npEntities = getNodePropertyEntities(nodeId);
        Map<QName, PropertyValue> nProps = new HashMap<QName, PropertyValue>(npEntities.size());
        
        for (AVMNodePropertyEntity npEntity : npEntities)
        {
            Pair<Long, QName> qnamePair = qnameDAO.getQName(npEntity.getQnameId());
            if (qnamePair != null)
            {
                nProps.put(qnamePair.getSecond(), npEntity);
            }
        }
        
        return nProps;
    }
    
    /**
     * {@inheritDoc}
     */
    public void deleteNodeProperty(long nodeId, QName qname)
    {
        // Get the persistent ID for the QName
        Pair<Long, QName> qnamePair = qnameDAO.getQName(qname);
        if (qnamePair != null)
        {
            Long qnameId = qnamePair.getFirst();
            
            Pair<Long, Long> key = new Pair<Long, Long>(nodeId, qnameId);
            Pair<Pair<Long, Long>, AVMNodePropertyEntity> entityPair = avmNodePropCache.getByKey(key);
            if (entityPair == null)
            {
                return;
            }
            
            int deleted = avmNodePropCache.deleteByKey(key);
            if (deleted < 1)
            {
                throw new ConcurrencyFailureException("AVMNodeProperty (" + nodeId + ", " + qnameId + ") no longer exists");
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void deleteNodeProperties(long nodeId)
    {
        Map<QName, PropertyValue> nProps = getNodeProperties(nodeId);
        if (nProps.size() == 0)
        {
            return;
        }
        
        for (QName propQName : nProps.keySet())
        {
            deleteNodeProperty(nodeId, propQName);
        }
        
        // TODO single delete + cache(s)
        /*
        int deleted = deleteNodePropertyEntities(nodeId);
        if (deleted < 1)
        {
            throw new ConcurrencyFailureException("AVMNodeProperties for node ID " + nodeId + " no longer exist");
        }
        
        // TODO clear node property cache for this node id
        */
    }
    
    /**
     * Callback for <b>avm_node_properties</b> DAO
     */
    private class AVMNodePropertyEntityCallbackDAO implements EntityLookupCallbackDAO<Pair<Long, Long>, AVMNodePropertyEntity, Serializable>
    {
        private final Pair<Pair<Long, Long>, AVMNodePropertyEntity> convertEntityToPair(AVMNodePropertyEntity nodePropEntity)
        {
            if (nodePropEntity == null)
            {
                return null;
            }
            else
            {
                Pair<Long, Long> key = new Pair<Long, Long>(nodePropEntity.getNodeId(), nodePropEntity.getQnameId());
                return new Pair<Pair<Long, Long>, AVMNodePropertyEntity>(key, nodePropEntity);
            }
        }
        
        public Serializable getValueKey(AVMNodePropertyEntity value)
        {
            return null;
        }
        
        public Pair<Pair<Long, Long>, AVMNodePropertyEntity> createValue(AVMNodePropertyEntity value)
        {
            insertNodePropertyEntity(value);
            return convertEntityToPair(value);
        }
        
        public Pair<Pair<Long, Long>, AVMNodePropertyEntity> findByKey(Pair<Long, Long> key)
        {
            AVMNodePropertyEntity entity = getNodePropertyEntity(key.getFirst(), key.getSecond());
            return convertEntityToPair(entity);
        }
        
        public Pair<Pair<Long, Long>, AVMNodePropertyEntity> findByValue(AVMNodePropertyEntity value)
        {
            if ((value.getNodeId() != null) && (value.getQnameId() != null))
            {
                return findByKey(new Pair<Long, Long>(value.getNodeId(), value.getQnameId()));
            }
            return null;
        }
        
        public int updateValue(Pair<Long, Long> key, AVMNodePropertyEntity value)
        {
            return updateNodePropertyEntity(value);
        }
        
        public int deleteByKey(Pair<Long, Long> key)
        {
            return deleteNodePropertyEntity(key.getFirst(), key.getSecond());
        }
        
        public int deleteByValue(AVMNodePropertyEntity value)
        {
            throw new UnsupportedOperationException("deleteByValue(AVMNodePropertyEntity)");
        }
    }
    
    protected abstract void insertNodePropertyEntity(AVMNodePropertyEntity propEntity);
    protected abstract int updateNodePropertyEntity(AVMNodePropertyEntity propEntity);
    protected abstract AVMNodePropertyEntity getNodePropertyEntity(long nodeId, long qnameId);
    protected abstract List<AVMNodePropertyEntity> getNodePropertyEntities(long nodeId);
    protected abstract int deleteNodePropertyEntity(long nodeId, long qnameId);
    protected abstract int deleteNodePropertyEntities(long nodeId);
}
