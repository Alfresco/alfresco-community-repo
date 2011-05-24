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
import java.util.Date;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.cache.NullCache;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.util.Pair;
import org.springframework.extensions.surf.util.ParameterCheck;
import org.springframework.dao.ConcurrencyFailureException;

/**
 * Abstract implementation for AVMVersionRoot DAO.
 * <p>
 * This provides basic services such as caching but defers to the underlying implementation
 * for CRUD operations.
 * 
 * @author janv
 * @since 3.2
 */
public abstract class AbstractAVMVersionRootDAOImpl implements AVMVersionRootDAO
{
    private SimpleCache<Serializable, Serializable> vrEntityCache;
    
    /**
     * Set the cache to use for <b>avm_version_roots</b> lookups (optional).
     * 
     * @param vrEntityCache
     */
    public void setVersionRootEntityCache(SimpleCache<Serializable, Serializable> vrEntityCache)
    {
        this.vrEntityCache = vrEntityCache;
    }
    
    @SuppressWarnings("unchecked")
    public AbstractAVMVersionRootDAOImpl()
    {
        this.vrEntityCache = (SimpleCache<Serializable, Serializable>)new NullCache();
    }
    
    /**
     * {@inheritDoc}
     */
    public AVMVersionRootEntity createVersionRoot(long storeId, long rootNodeId, int version, String creator, String tag, String description)
    {
        ParameterCheck.mandatory("creator", creator);
        
        AVMVersionRootEntity vrEntity = new AVMVersionRootEntity();
        vrEntity.setStoreId(storeId);
        vrEntity.setRootNodeId(rootNodeId);
        vrEntity.setVersion(version);
        vrEntity.setCreator(creator);
        vrEntity.setCreatedDate(new Date().getTime()); // app server date/time
        vrEntity.setTag(tag);
        vrEntity.setDescription(description);
        
        vrEntity = createVersionRootEntity(vrEntity);
        
        // Cache it
        vrEntityCache.put(new Pair<Long, Integer>(storeId, version), vrEntity.getId());
        vrEntityCache.put(vrEntity.getId(), vrEntity);
        
        return vrEntity;
    }
    
    /**
     * {@inheritDoc}
     */
    public void updateVersionRoot(AVMVersionRootEntity vrEntity)
    {
        ParameterCheck.mandatory("vrEntity", vrEntity);
        
        ParameterCheck.mandatory("vrEntity.storeId", vrEntity.getStoreId());
        ParameterCheck.mandatory("vrEntity.id", vrEntity.getId());
        ParameterCheck.mandatory("vrEntity.version", vrEntity.getVersion());
        
        int updated = updateVersionRootEntity(vrEntity);
        if (updated != 1)
        {
            // unexpected number of rows affected
            throw new ConcurrencyFailureException("Incorrect number of rows affected for updateVersionRoot: " + vrEntity + ": expected 1, actual " + updated);
        }
        
        // Cache it
        vrEntityCache.put(new Pair<Long, Integer>(vrEntity.getStoreId(), vrEntity.getVersion()), vrEntity.getId());
        vrEntityCache.put(vrEntity.getId(), vrEntity);
    }
    
    /**
     * {@inheritDoc}
     */
    public List<AVMVersionRootEntity> getAllInStore(long storeId)
    {
        return getAllVersionRootEntitiesByStoreId(storeId);
    }
    
    /**
     * {@inheritDoc}
     */
    protected AVMVersionRootEntity getByID(long vrEntityId)
    {
        AVMVersionRootEntity vrEntity = (AVMVersionRootEntity) vrEntityCache.get(vrEntityId);
        if (vrEntity != null)
        {
            return vrEntity;
        }
        
        // Get it from the DB
        vrEntity = getVersionRootEntityById(vrEntityId);
        if (vrEntity != null)
        {
            // Cache it
            vrEntityCache.put(new Pair<Long, Integer>(vrEntity.getStoreId(), vrEntity.getVersion()), vrEntity.getId());
            vrEntityCache.put(vrEntity.getId(), vrEntity);
        }
        
        return vrEntity;
    }
    
    /**
     * {@inheritDoc}
     */
    public AVMVersionRootEntity getByVersionID(long storeId, int version)
    {
        // Check the cache
        Long vrEntityId = (Long) vrEntityCache.get(new Pair<Long, Integer>(storeId, version));
        AVMVersionRootEntity vrEntity = null;
        if (vrEntityId != null)
        {
            vrEntity = (AVMVersionRootEntity) vrEntityCache.get(vrEntityId);
            if (vrEntity != null)
            {
                return vrEntity;
            }
        }
        
        // Get it from the DB
        vrEntity = getVersionRootEntityByStoreVersion(storeId, version);
        if (vrEntity != null)
        {
            // Cache it
            vrEntityCache.put(new Pair<Long, Integer>(vrEntity.getStoreId(), vrEntity.getVersion()), vrEntity.getId());
            vrEntityCache.put(vrEntity.getId(), vrEntity);
        }
        
        return vrEntity;
    }
    
    /**
     * {@inheritDoc}
     */
    public AVMVersionRootEntity getByRoot(long rootNodeId)
    {
        return getVersionRootEntityByRootNodeId(rootNodeId);
    }
    
    /**
     * {@inheritDoc}
     */
    public List<AVMVersionRootEntity> getByDates(long storeId, Date from, Date to)
    {
        if ((from != null) && (to != null))
        {
            return getVersionRootEntitiesByBetween(storeId, from.getTime(), to.getTime());
        }
        else if ((from == null) && (to != null))
        {
            return getVersionRootEntitiesByTo(storeId, to.getTime());
        }
        else if ((to == null) && (from != null))
        {
            return getVersionRootEntitiesByFrom(storeId, from.getTime());
        }
        else
        {
            throw new AlfrescoRuntimeException("getByDates: from and to are both null for store id: "+storeId);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public AVMVersionRootEntity getMaxVersion(long storeId)
    {
        return getVersionRootEntityMaxVersion(storeId);
    }
    
    /**
     * {@inheritDoc}
     */
    public Long getMaxVersionID(long storeId)
    {
        return getVersionRootEntityMaxVersionId(storeId);
    }
    
    /**
     * {@inheritDoc}
     */
    public void deleteVersionRoot(long vrEntityId)
    {
        AVMVersionRootEntity vrEntity = getByID(vrEntityId);
        if (vrEntity == null)
        {
            return;
        }
        
        int deleted = deleteVersionRootEntity(vrEntityId);
        if (deleted < 1)
        {
            throw new ConcurrencyFailureException("AVMVersionRoot with ID " + vrEntityId + " no longer exists");
        }
        
        // Remove from cache
        vrEntityCache.remove(new Pair<Long, Integer>(vrEntity.getStoreId(), vrEntity.getVersion()));
        vrEntityCache.remove(vrEntityId);
    }
    
    protected abstract AVMVersionRootEntity createVersionRootEntity(AVMVersionRootEntity vrEntity);
    protected abstract int updateVersionRootEntity(AVMVersionRootEntity updateVersionRootEntity);
    protected abstract int deleteVersionRootEntity(long vrEntityId);
    protected abstract AVMVersionRootEntity getVersionRootEntityMaxVersion(long storeId);
    protected abstract Long getVersionRootEntityMaxVersionId(long storeId);
    protected abstract AVMVersionRootEntity getVersionRootEntityById(long vrEntityId);
    protected abstract AVMVersionRootEntity getVersionRootEntityByStoreVersion(long storeId, int version);
    protected abstract AVMVersionRootEntity getVersionRootEntityByRootNodeId(long rootNodeId);
    protected abstract List<AVMVersionRootEntity> getAllVersionRootEntitiesByStoreId(long storeId);
    protected abstract List<AVMVersionRootEntity> getVersionRootEntitiesByTo(long storeId, long to);
    protected abstract List<AVMVersionRootEntity> getVersionRootEntitiesByFrom(long storeId, long from);
    protected abstract List<AVMVersionRootEntity> getVersionRootEntitiesByBetween(long storeId, long from, long to);
    
    /**
     * {@inheritDoc}
     */
    public AVMVersionLayeredNodeEntryEntity createVersionLayeredNodeEntry(long versionRootId, String md5sum, String path)
    {
        ParameterCheck.mandatory("md5sum", md5sum);
        ParameterCheck.mandatory("path", path);
        
        return createVersionLayeredNodeEntryEntity(versionRootId, md5sum, path);
    }
    
    /**
     * {@inheritDoc}
     */
    public void deleteVersionLayeredNodeEntries(long versionRootId)
    {
        List<AVMVersionLayeredNodeEntryEntity> vlneEntities = getVersionLayeredNodeEntries(versionRootId);
        if (vlneEntities.size() == 0)
        {
            return;
        }
        
        int deleted = deleteVersionLayeredNodeEntryEntities(versionRootId);
        if (deleted < 1)
        {
            throw new ConcurrencyFailureException("AVMVersionLayeredNodeEntries with version root id " + versionRootId + " no longer exist");
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public List<AVMVersionLayeredNodeEntryEntity> getVersionLayeredNodeEntries(long versionRootId)
    {
        return getVersionLayeredNodeEntryEntities(versionRootId);
    }
    
    protected abstract AVMVersionLayeredNodeEntryEntity createVersionLayeredNodeEntryEntity(long versionRootId, String md5sum, String path);
    protected abstract int deleteVersionLayeredNodeEntryEntities(long versionRootId);
    protected abstract List<AVMVersionLayeredNodeEntryEntity> getVersionLayeredNodeEntryEntities(long storeId);

    
    protected abstract List<AVMVersionRootEntity> getVersionRootEntitiesByVersionsTo(long storeId, long to);
    protected abstract List<AVMVersionRootEntity> getVersionRootEntitiesByVersionsFrom(long storeId, long from);
    protected abstract List<AVMVersionRootEntity> getVersionRootEntitiesByVersionsBetween(long storeId, long from, long to);
    
    public List<AVMVersionRootEntity> getByVersionsTo(long id, int version)
    {
        return getVersionRootEntitiesByVersionsTo(id, version);
    }

    public List<AVMVersionRootEntity> getByVersionsFrom(long id, int version)
    {
       return getVersionRootEntitiesByVersionsFrom(id, version);
    }

    public List<AVMVersionRootEntity> getByVersionsBetween(long id, int startVersion, int endVersion)
    {
        return getVersionRootEntitiesByVersionsBetween(id, startVersion, endVersion);
    }
    
    
    
}
