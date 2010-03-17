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
import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.cache.lookup.EntityLookupCache;
import org.alfresco.repo.cache.lookup.EntityLookupCache.EntityLookupCallbackDAO;
import org.alfresco.util.Pair;
import org.springframework.extensions.surf.util.ParameterCheck;
import org.alfresco.util.SearchLanguageConversion;
import org.springframework.dao.ConcurrencyFailureException;

/**
 * Abstract implementation for AVMNodeLinks DAO.
 * <p>
 * This provides basic services such as caching but defers to the underlying implementation
 * for CRUD operations.
 * 
 * @author janv
 * @since 3.2
 */
public abstract class AbstractAVMNodeLinksDAOImpl implements AVMNodeLinksDAO
{
    private static final String CACHE_REGION_AVM_CHILD_ENTRY = "AVMChildEntry";
    private static final String CACHE_REGION_AVM_HISTORY_LINK = "AVMHistoryLink";
    
    private final AVMChildEntryEntityCallbackDAO avmChildEntryEntityDaoCallback;
    private final AVMHistoryLinkEntityCallbackDAO avmHistoryLinkEntityDaoCallback;
    
    /**
     * Cache for the AVM child entry entity:<br/>
     * KEY: ChildKey<br/>
     * VALUE: AVMChildEntryEntity<br/>
     * VALUE KEY: Pair of node IDs (parent, child)<br/>
     */
    private EntityLookupCache<ChildKey, AVMChildEntryEntity, Pair<Long, Long>> avmChildEntryCache;
    
    /**
     * Cache for the AVM history link entity:<br/>
     * KEY: Descendent ID<br/>
     * VALUE: AVMHistoryLinkEntity<br/>
     * VALUE KEY: AVMHistoryLinkEntity<br/>
     */
    private EntityLookupCache<Long, AVMHistoryLinkEntity, AVMHistoryLinkEntity> avmHistoryLinkCache;
    
    /**
     * Set the cache to use for <b>avm_child_entry</b> lookups (optional).
     * 
     * @param avmChildEntryCache            the cache of IDs to AVMChildEntryEntities
     */
    public void setAvmChildEntryCache(SimpleCache<Serializable, Object> avmChildEntryCache)
    {
        this.avmChildEntryCache = new EntityLookupCache<ChildKey, AVMChildEntryEntity, Pair<Long, Long>>(
                avmChildEntryCache,
                CACHE_REGION_AVM_CHILD_ENTRY,
                avmChildEntryEntityDaoCallback);
    }
    
    /**
     * Set the cache to use for <b>avm_history_link</b> lookups (optional).
     * 
     * @param avmHistoryLinkCache            the cache of ID to ID (from descendent to ancestor)
     */
    public void setAvmHistoryLinkCache(SimpleCache<Serializable, Object> avmHistoryLinkCache)
    {
        this.avmHistoryLinkCache = new EntityLookupCache<Long, AVMHistoryLinkEntity, AVMHistoryLinkEntity>(
                avmHistoryLinkCache,
                CACHE_REGION_AVM_HISTORY_LINK,
                avmHistoryLinkEntityDaoCallback);
    }
    
    /**
     * Default constructor.
     * <p>
     * This sets up the DAO accessors to bypass any caching to handle the case where the caches are not
     * supplied in the setters.
     */
    public AbstractAVMNodeLinksDAOImpl()
    {
        this.avmChildEntryEntityDaoCallback = new AVMChildEntryEntityCallbackDAO();
        this.avmChildEntryCache = new EntityLookupCache<ChildKey, AVMChildEntryEntity, Pair<Long, Long>>(avmChildEntryEntityDaoCallback);
        
        this.avmHistoryLinkEntityDaoCallback = new AVMHistoryLinkEntityCallbackDAO();
        this.avmHistoryLinkCache = new EntityLookupCache<Long, AVMHistoryLinkEntity, AVMHistoryLinkEntity>(avmHistoryLinkEntityDaoCallback);
    }
    
    /**
     * {@inheritDoc}
     */
    public void createChildEntry(long parentNodeId, String name, long childNodeId)
    {
        ParameterCheck.mandatory("name", name);
        
        AVMChildEntryEntity ceEntity = new AVMChildEntryEntity();
        ceEntity.setParentNodeId(parentNodeId);
        ceEntity.setName(name);
        ceEntity.setChildNodeId(childNodeId);
        
        Pair<ChildKey, AVMChildEntryEntity> entityPair = avmChildEntryCache.getOrCreateByValue(ceEntity);
        entityPair.getSecond();
    }
    
    /**
     * {@inheritDoc}
     */
    public AVMChildEntryEntity getChildEntry(long parentNodeId, String name)
    {
        ParameterCheck.mandatory("name", name);
        
        Pair<ChildKey, AVMChildEntryEntity> entityPair = avmChildEntryCache.getByKey(new ChildKey(parentNodeId, name));
        if (entityPair == null)
        {
            return null;
        }
        return entityPair.getSecond();
    }
    
    /**
     * {@inheritDoc}
     */
    public List<AVMChildEntryEntity> getChildEntriesByParent(long parentNodeId, String childNamePattern)
    {
        List<AVMChildEntryEntity> result = null;
        
        if ((childNamePattern == null) || (childNamePattern.length() == 0))
        {
            result = getChildEntryEntitiesByParent(parentNodeId);
        }
        else
        {
            String pattern = SearchLanguageConversion.convert(SearchLanguageConversion.DEF_LUCENE, SearchLanguageConversion.DEF_SQL_LIKE, childNamePattern);
            result = getChildEntryEntitiesByParent(parentNodeId, pattern);
        }
        
        if (result == null)
        {
            result = new ArrayList<AVMChildEntryEntity>(0);
        }
        
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    public AVMChildEntryEntity getChildEntry(long parentNodeId, long childNodeId)
    {
        AVMChildEntryEntity ceEntity = new AVMChildEntryEntity();
        ceEntity.setParentNodeId(parentNodeId);
        ceEntity.setChildNodeId(childNodeId);
        
        Pair<ChildKey, AVMChildEntryEntity> entityPair = avmChildEntryCache.getByValue(ceEntity);
        if (entityPair == null)
        {
            return null;
        }
        return entityPair.getSecond();
    }
    
    /**
     * {@inheritDoc}
     */
    public List<AVMChildEntryEntity> getChildEntriesByChild(long childNodeId)
    {
        List<AVMChildEntryEntity> result = getChildEntryEntitiesByChild(childNodeId);
        if (result == null)
        {
            result = new ArrayList<AVMChildEntryEntity>(0);
        }
        
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    public void updateChildEntry(AVMChildEntryEntity childEntryEntity)
    {
        ParameterCheck.mandatory("childEntryEntity", childEntryEntity);
        ParameterCheck.mandatory("childEntryEntity.getParentNodeId()", childEntryEntity.getParentNodeId());
        ParameterCheck.mandatory("childEntryEntity.getChildId()", childEntryEntity.getChildId());
        ParameterCheck.mandatory("childEntryEntity.getName()", childEntryEntity.getName());
        
        ChildKey key = new ChildKey(childEntryEntity.getParentNodeId(), childEntryEntity.getName());
        int updated = avmChildEntryCache.updateValue(key, childEntryEntity);
        if (updated < 1)
        {
            throw new ConcurrencyFailureException("AVMChildEntry for parent/name (" + key.getParentNodeId() + ", " + key.getName() + ") no longer exists");
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void deleteChildEntry(AVMChildEntryEntity childEntryEntity)
    {
        ParameterCheck.mandatory("childEntryEntity", childEntryEntity);
        
        ParameterCheck.mandatory("childEntryEntity.getParentNodeId()", childEntryEntity.getParentNodeId());
        ParameterCheck.mandatory("childEntryEntity.getName()", childEntryEntity.getName());
        
        ChildKey key = new ChildKey(childEntryEntity.getParentNodeId(), childEntryEntity.getName());
        Pair<ChildKey, AVMChildEntryEntity> entityPair = avmChildEntryCache.getByKey(key);
        if (entityPair == null)
        {
            return;
        }
        
        int deleted = avmChildEntryCache.deleteByKey(key);
        if (deleted < 1)
        {
            throw new ConcurrencyFailureException("AVMChildEntry for parent/name (" + key.getParentNodeId() + ", " + key.getName() + ") no longer exists");
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void deleteChildEntriesByParent(long parentNodeId)
    {
        List<AVMChildEntryEntity> ceEntities = getChildEntriesByParent(parentNodeId, null);
        if (ceEntities.size() == 0)
        {
            return;
        }
        
        for (AVMChildEntryEntity ceEntity : ceEntities)
        {
            deleteChildEntry(ceEntity);
        }
        
        // TODO single delete + cache(s)
        
        /*
        int deleted = deleteChildEntryEntities(parentNodeId);
        if (deleted < 1)
        {
            throw new ConcurrencyFailureException("AVMChildEntries for parent node ID " + parentNodeId + " no longer exist");
        }
        
        // TODO clear child entry cache for this parent node id
        */
    }
    
    private static class ChildKey implements Serializable
    {
        private static final long serialVersionUID = 848161072437569305L;
        
        /**
         * The Parent Node Id
         */
        private Long parentNodeId;
        
        /**
         * The child's name.
         */
        private String name;
        
        public ChildKey(Long parentNodeId, String name)
        {
            this.parentNodeId = parentNodeId;
            this.name = name;
        }
        
        public Long getParentNodeId()
        {
            return parentNodeId;
        }
        
        public String getName()
        {
            return name;
        }
        
        /**
         * Override of equals.
         */
        @Override
        public boolean equals(Object other)
        {
            if (this == other)
            {
                return true;
            }
            if (!(other instanceof ChildKey))
            {
                return false;
            }
            ChildKey o = (ChildKey)other;
            return parentNodeId.equals(o.getParentNodeId()) &&
                   name.equalsIgnoreCase(o.getName());
        }
        
        /**
         * Override of hashCode.
         */
        public int hashCode()
        {
            return parentNodeId.hashCode() + name.toLowerCase().hashCode();
        }
    }
    
    /**
     * Callback for <b>avm_child_entry</b> DAO
     */
    private class AVMChildEntryEntityCallbackDAO implements EntityLookupCallbackDAO<ChildKey, AVMChildEntryEntity, Pair<Long, Long>>
    {
        private final Pair<ChildKey, AVMChildEntryEntity> convertEntityToPair(AVMChildEntryEntity ceEntity)
        {
            if (ceEntity == null)
            {
                return null;
            }
            else
            {
                return new Pair<ChildKey, AVMChildEntryEntity>(new ChildKey(ceEntity.getParentNodeId(), ceEntity.getName()), ceEntity);
            }
        }
        
        public Pair<Long, Long> getValueKey(AVMChildEntryEntity value)
        {
            return new Pair<Long,Long>(value.getParentNodeId(), value.getChildId());
        }
        
        public Pair<ChildKey, AVMChildEntryEntity> createValue(AVMChildEntryEntity value)
        {
            createChildEntryEntity(value);
            return convertEntityToPair(value);
        }
        
        public Pair<ChildKey, AVMChildEntryEntity> findByKey(ChildKey key)
        {
            AVMChildEntryEntity entity = getChildEntryEntity(key.getParentNodeId(), key.getName());
            return convertEntityToPair(entity);
        }
        
        public Pair<ChildKey, AVMChildEntryEntity> findByValue(AVMChildEntryEntity value)
        {
            AVMChildEntryEntity entity = getChildEntryEntity(value.getParentNodeId(), value.getChildId());
            return convertEntityToPair(entity);
        }
        
        public int updateValue(ChildKey key, AVMChildEntryEntity value)
        {
            return updateChildEntryEntity(value);
        }
        
        public int deleteByKey(ChildKey key)
        {
            return deleteChildEntryEntity(key.getParentNodeId(), key.getName());
        }
        
        public int deleteByValue(AVMChildEntryEntity value)
        {
            return deleteChildEntryEntity(value.getParentNodeId(), value.getChildId());
        }
    }
    
    protected abstract List<AVMChildEntryEntity> getChildEntryEntitiesByParent(long parentNodeId);
    protected abstract List<AVMChildEntryEntity> getChildEntryEntitiesByParent(long parentNodeId, String childNamePattern);
    protected abstract List<AVMChildEntryEntity> getChildEntryEntitiesByChild(long childNodeId);
    
    protected abstract AVMChildEntryEntity getChildEntryEntity(long parentNodeId, String name);
    protected abstract AVMChildEntryEntity getChildEntryEntity(long parentNodeId, long childNodeId);
    protected abstract AVMChildEntryEntity getChildEntryEntity(AVMChildEntryEntity childEntryEntity);
    
    protected abstract void createChildEntryEntity(AVMChildEntryEntity childEntryEntity);
    
    protected abstract int updateChildEntryEntity(AVMChildEntryEntity childEntryEntity); // specific rename 'case' only
    
    protected abstract int deleteChildEntryEntity(long parentNodeId, String name);
    protected abstract int deleteChildEntryEntity(long parentNodeId, long childNodeId);
    protected abstract int deleteChildEntryEntities(long parentNodeId);
    
    /**
     * {@inheritDoc}
     */
    public void createMergeLink(long mergeFromNodeId, long mergeToNodeId)
    {
        createMergeLinkEntity(mergeFromNodeId, mergeToNodeId);
    }
    
    /**
     * {@inheritDoc}
     */
    public void deleteMergeLink(long mergeFromNodeId, long mergeToNodeId)
    {
        AVMMergeLinkEntity mlEntity = getMergeLinkByTo(mergeToNodeId);
        if (mlEntity == null)
        {
            return;
        }
        
        int deleted = deleteMergeLinkEntity(mergeFromNodeId, mergeToNodeId);
        if (deleted < 1)
        {
            throw new ConcurrencyFailureException("AVMMergeLink (" + mergeFromNodeId + ", " + mergeToNodeId + ") no longer exists");
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public AVMMergeLinkEntity getMergeLinkByTo(long mergeToNodeId)
    {
        return getMergeLinkEntityByTo(mergeToNodeId);
    }
    
    /**
     * {@inheritDoc}
     */
    public List<AVMMergeLinkEntity> getMergeLinksByFrom(long mergeFromNodeId)
    {
        return getMergeLinkEntitiesByFrom(mergeFromNodeId);
    }
    
    protected abstract void createMergeLinkEntity(long mergeFromNodeId, long mergeToNodeId);
    protected abstract int deleteMergeLinkEntity(long mergeFromNodeId, long mergeToNodeId);
    protected abstract AVMMergeLinkEntity getMergeLinkEntityByTo(long mergeToNodeId);
    protected abstract List<AVMMergeLinkEntity> getMergeLinkEntitiesByFrom(long mergeFromNodeId);
    
    /**
     * {@inheritDoc}
     */
    public void createHistoryLink(long ancestorNodeId, long descendentNodeId)
    {
        AVMHistoryLinkEntity hlEntity = new AVMHistoryLinkEntity(ancestorNodeId, descendentNodeId);
        
        avmHistoryLinkCache.getOrCreateByValue(hlEntity); // ignore return value
    }
    
    /**
     * {@inheritDoc}
     */
    public void deleteHistoryLink(long ancestorNodeId, long descendentNodeId)
    {
        AVMHistoryLinkEntity hlEntity = new AVMHistoryLinkEntity(ancestorNodeId, descendentNodeId);
        Pair<Long, AVMHistoryLinkEntity> entityPair = avmHistoryLinkCache.getByValue(hlEntity);
        if (entityPair == null)
        {
            return;
        }
        
        int deleted = avmHistoryLinkCache.deleteByValue(hlEntity);
        if (deleted < 1)
        {
            throw new ConcurrencyFailureException("AVMHistoryLinkEntity (" + ancestorNodeId + ", " + descendentNodeId + ") no longer exists");
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public AVMHistoryLinkEntity getHistoryLinkByDescendent(long descendentNodeId)
    {
        Pair<Long, AVMHistoryLinkEntity> entityPair = avmHistoryLinkCache.getByKey(descendentNodeId);
        if (entityPair == null)
        {
            return null;
        }
        return entityPair.getSecond();
    }
    
    /**
     * {@inheritDoc}
     */
    public List<AVMHistoryLinkEntity> getHistoryLinksByAncestor(long ancestorNodeId)
    {
        // not via cache
        return getHistoryLinkEntitiesByAncestor(ancestorNodeId);
    }
    
    /**
     * Callback for <b>avm_history_link</b> DAO
     */
    private class AVMHistoryLinkEntityCallbackDAO implements EntityLookupCallbackDAO<Long, AVMHistoryLinkEntity, AVMHistoryLinkEntity>
    {
        private final Pair<Long, AVMHistoryLinkEntity> convertEntityToPair(AVMHistoryLinkEntity hlEntity)
        {
            if (hlEntity == null)
            {
                return null;
            }
            else
            {
                return new Pair<Long, AVMHistoryLinkEntity>(hlEntity.getDescendentNodeId(), hlEntity);
            }
        }
        
        public AVMHistoryLinkEntity getValueKey(AVMHistoryLinkEntity value)
        {
            return value;
        }
        
        public Pair<Long, AVMHistoryLinkEntity> createValue(AVMHistoryLinkEntity value)
        {
            createHistoryLinkEntity(value.getAncestorNodeId(), value.getDescendentNodeId());
            return convertEntityToPair(value);
        }
        
        public Pair<Long, AVMHistoryLinkEntity> findByKey(Long key)
        {
            AVMHistoryLinkEntity entity = getHistoryLinkEntityByDescendent(key);
            return convertEntityToPair(entity);
        }
        
        public Pair<Long, AVMHistoryLinkEntity> findByValue(AVMHistoryLinkEntity value)
        {
            AVMHistoryLinkEntity entity = getHistoryLinkEntity(value.getAncestorNodeId(), value.getDescendentNodeId());
            return convertEntityToPair(entity);
        }
        
        public int updateValue(Long key, AVMHistoryLinkEntity value)
        {
            throw new UnsupportedOperationException("updateValue(Long, AVMHistoryLinkEntity");
        }
        
        public int deleteByKey(Long key)
        {
            AVMHistoryLinkEntity entity = getHistoryLinkEntityByDescendent(key);
            return deleteHistoryLinkEntity(entity.getAncestorNodeId(), entity.getDescendentNodeId());
        }
        
        public int deleteByValue(AVMHistoryLinkEntity value)
        {
            return deleteHistoryLinkEntity(value.getAncestorNodeId(), value.getDescendentNodeId());
        }
    }
    
    protected abstract void createHistoryLinkEntity(long ancestorNodeId, long descendentNodeId);
    protected abstract int deleteHistoryLinkEntity(long ancestorNodeId, long descendentNodeId);
    protected abstract AVMHistoryLinkEntity getHistoryLinkEntity(long ancestorNodeId, long descendentNodeId);
    protected abstract AVMHistoryLinkEntity getHistoryLinkEntityByDescendent(long descendentNodeId);
    protected abstract List<AVMHistoryLinkEntity> getHistoryLinkEntitiesByAncestor(long ancestorNodeId);
}
