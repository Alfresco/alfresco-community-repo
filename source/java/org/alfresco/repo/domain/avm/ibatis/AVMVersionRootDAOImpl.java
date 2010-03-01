/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.domain.avm.ibatis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.domain.avm.AVMStoreEntity;
import org.alfresco.repo.domain.avm.AVMVersionLayeredNodeEntryEntity;
import org.alfresco.repo.domain.avm.AVMVersionRootEntity;
import org.alfresco.repo.domain.avm.AbstractAVMVersionRootDAOImpl;
import org.springframework.orm.ibatis.SqlMapClientTemplate;

/**
 * iBatis-specific implementation of the AVMVersionRoot DAO
 * 
 * @author janv
 * @since 3.2
 */
public class AVMVersionRootDAOImpl extends AbstractAVMVersionRootDAOImpl
{
    private static final String INSERT_AVM_VERSION_ROOT ="alfresco.avm.insert_AVMVersionRoot";
    private static final String DELETE_AVM_VERSION_ROOT ="alfresco.avm.delete_AVMVersionRoot";
    private static final String UPDATE_AVM_VERSION_ROOT ="alfresco.avm.update_AVMVersionRoot";
    
    private static final String SELECT_AVM_VERSION_ROOT_MAX_VERSION ="alfresco.avm.select_AVMVersionRootMaxVersion";
    private static final String SELECT_AVM_VERSION_ROOT_MAX_VERSION_ID ="alfresco.avm.select_AVMVersionRootMaxVersionID";
    private static final String SELECT_AVM_VERSION_ROOT_BY_ID ="alfresco.avm.select_AVMVersionRootById";
    private static final String SELECT_AVM_VERSION_ROOT_BY_STORE_VERSION ="alfresco.avm.select_AVMVersionRootByStoreVersion";
    private static final String SELECT_AVM_VERSION_ROOT_BY_ROOT_NODE_ID ="alfresco.avm.select_AVMVersionRootByRootNodeId";
    private static final String SELECT_AVM_VERSION_ROOTS_BY_STORE_ID ="alfresco.avm.select_AVMVersionRootsByStoreId";
    private static final String SELECT_AVM_VERSION_ROOTS_BY_TO ="alfresco.avm.select_AVMVersionRootsByTo";
    private static final String SELECT_AVM_VERSION_ROOTS_BY_FROM ="alfresco.avm.select_AVMVersionRootsByFrom";
    private static final String SELECT_AVM_VERSION_ROOTS_BETWEEN ="alfresco.avm.select_AVMVersionRootsBetween";
    
    private static final String INSERT_AVM_VERSION_LAYERED_NODE_ENTRY ="alfresco.avm.insert_AVMVersionLayeredNodeEntry";
    private static final String DELETE_AVM_VERSION_LAYERED_NODE_ENTRIES ="alfresco.avm.delete_AVMVersionLayeredNodeEntries";
    private static final String SELECT_AVM_VERSION_LAYERED_NODE_ENTRIES ="alfresco.avm.select_AVMVersionLayeredNodeEntries";
    
    
    private SqlMapClientTemplate template;
    
    public void setSqlMapClientTemplate(SqlMapClientTemplate sqlMapClientTemplate)
    {
        this.template = sqlMapClientTemplate;
    }
    
    @Override
    protected AVMVersionRootEntity createVersionRootEntity(AVMVersionRootEntity newVersionRootEntity)
    {
        Long id = (Long) template.insert(INSERT_AVM_VERSION_ROOT, newVersionRootEntity);
        newVersionRootEntity.setId(id);
        return newVersionRootEntity;
    }
    
    @Override
    protected void updateVersionRootEntity(AVMVersionRootEntity updateVersionRootEntity)
    {
        template.update(UPDATE_AVM_VERSION_ROOT, updateVersionRootEntity, 1);
    }
    
    @Override
    protected AVMVersionRootEntity getVersionRootEntityMaxVersion(long storeId)
    {
        AVMStoreEntity storeEntity = new AVMStoreEntity();
        storeEntity.setId(storeId);
        return (AVMVersionRootEntity) template.queryForObject(SELECT_AVM_VERSION_ROOT_MAX_VERSION, storeEntity);
    }
    
    @Override
    protected Long getVersionRootEntityMaxVersionId(long storeId)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", storeId);
        Integer maxVersionId = (Integer) template.queryForObject(SELECT_AVM_VERSION_ROOT_MAX_VERSION_ID, params);
        if (maxVersionId == null)
        {
            return null;
        }
        return new Long(maxVersionId);
    }
    
    @Override
    protected AVMVersionRootEntity getVersionRootEntityById(long vrEntityId)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", vrEntityId);
        return (AVMVersionRootEntity) template.queryForObject(SELECT_AVM_VERSION_ROOT_BY_ID, params);
    }
    
    @Override
    protected AVMVersionRootEntity getVersionRootEntityByStoreVersion(long storeId, int version)
    {
        AVMVersionRootEntity vrEntity = new AVMVersionRootEntity();
        vrEntity.setStoreId(storeId);
        vrEntity.setVersion(version);
        
        return (AVMVersionRootEntity) template.queryForObject(SELECT_AVM_VERSION_ROOT_BY_STORE_VERSION, vrEntity);
    }
    
    @Override
    protected AVMVersionRootEntity getVersionRootEntityByRootNodeId(long rootNodeId)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", rootNodeId);
        return (AVMVersionRootEntity) template.queryForObject(SELECT_AVM_VERSION_ROOT_BY_ROOT_NODE_ID, params);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected List<AVMVersionRootEntity> getAllVersionRootEntitiesByStoreId(long storeId)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", storeId);
        return (List<AVMVersionRootEntity>) template.queryForList(SELECT_AVM_VERSION_ROOTS_BY_STORE_ID, params);
    }
    
    @SuppressWarnings("unchecked")
    protected List<AVMVersionRootEntity> getVersionRootEntitiesByTo(long storeId, long to)
    {
        Map<String, Object> params = new HashMap<String, Object>(2);
        params.put("id", storeId);
        params.put("to", to);
        return (List<AVMVersionRootEntity>) template.queryForList(SELECT_AVM_VERSION_ROOTS_BY_TO, params);
    }
    
    @SuppressWarnings("unchecked")
    protected List<AVMVersionRootEntity> getVersionRootEntitiesByFrom(long storeId, long from)
    {
        Map<String, Object> params = new HashMap<String, Object>(2);
        params.put("id", storeId);
        params.put("from", from);
        return (List<AVMVersionRootEntity>) template.queryForList(SELECT_AVM_VERSION_ROOTS_BY_FROM, params);
    }
    
    @SuppressWarnings("unchecked")
    protected List<AVMVersionRootEntity> getVersionRootEntitiesByBetween(long storeId, long from, long to)
    {
        Map<String, Object> params = new HashMap<String, Object>(3);
        params.put("id", storeId);
        params.put("from", from);
        params.put("to", to);
        return (List<AVMVersionRootEntity>) template.queryForList(SELECT_AVM_VERSION_ROOTS_BETWEEN, params);
    }
    
    @Override
    protected int deleteVersionRootEntity(long vrEntityId)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", vrEntityId);
        return template.delete(DELETE_AVM_VERSION_ROOT, params);
    }
    
    @Override
    protected AVMVersionLayeredNodeEntryEntity createVersionLayeredNodeEntryEntity(long versionRootId, String md5sum, String path)
    {
        AVMVersionLayeredNodeEntryEntity vlneEntity = new AVMVersionLayeredNodeEntryEntity();
        vlneEntity.setVersionRootId(versionRootId);
        vlneEntity.setMd5sum(md5sum);
        vlneEntity.setPath(path);
        template.insert(INSERT_AVM_VERSION_LAYERED_NODE_ENTRY, vlneEntity);
        return vlneEntity;
    }
    
    @Override
    protected int deleteVersionLayeredNodeEntryEntities(long versionRootId)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", versionRootId);
        return template.delete(DELETE_AVM_VERSION_LAYERED_NODE_ENTRIES, params);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected List<AVMVersionLayeredNodeEntryEntity> getVersionLayeredNodeEntryEntities(long versionRootId)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", versionRootId);
        return (List<AVMVersionLayeredNodeEntryEntity>) template.queryForList(SELECT_AVM_VERSION_LAYERED_NODE_ENTRIES, params);
    }
}
