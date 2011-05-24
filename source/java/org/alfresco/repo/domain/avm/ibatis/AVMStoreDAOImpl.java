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
package org.alfresco.repo.domain.avm.ibatis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.domain.avm.AVMStoreEntity;
import org.alfresco.repo.domain.avm.AVMStorePropertyEntity;
import org.alfresco.repo.domain.avm.AbstractAVMStoreDAOImpl;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * iBatis-specific implementation of the AVMStore DAO.
 * 
 * @author janv
 * @since 3.2
 */
public class AVMStoreDAOImpl extends AbstractAVMStoreDAOImpl
{
    private static final String SELECT_AVM_STORE_BY_ID ="alfresco.avm.select_AVMStoreById";
    
    private static final String SELECT_AVM_STORE_BY_KEY ="alfresco.avm.select_AVMStoreByKey"; // name
    private static final String SELECT_AVM_STORE_BY_KEY_L ="alfresco.avm.select_AVMStoreByKeyL"; // lower(name)
    
    private static final String SELECT_AVM_STORE_BY_ROOT_NODE_ID ="alfresco.avm.select_AVMStoreByRootNodeId";
    private static final String SELECT_AVM_STORE_ALL ="alfresco.avm.select_AVMStoreAll";
    
    private static final String INSERT_AVM_STORE ="alfresco.avm.insert.insert_AVMStore";
    private static final String DELETE_AVM_STORE ="alfresco.avm.delete_AVMStore";
    private static final String UPDATE_AVM_STORE ="alfresco.avm.update_AVMStore";
    
    private static final String INSERT_AVM_STORE_PROP ="alfresco.avm.insert.insert_AVMStoreProperty";
    private static final String UPDATE_AVM_STORE_PROP ="alfresco.avm.update_AVMStoreProperty";
    private static final String SELECT_AVM_STORE_PROP ="alfresco.avm.select_AVMStoreProperty";
    private static final String SELECT_AVM_STORE_PROPS ="alfresco.avm.select_AVMStoreProperties";
    
    private static final String SELECT_AVM_STORE_PROPS_BY_KEY_PATTERN ="alfresco.avm.select_AVMStorePropertiesByKeyPattern"; // uri + local_name
    private static final String SELECT_AVM_STORE_PROPS_BY_KEY_PATTERN_L ="alfresco.avm.select_AVMStorePropertiesByKeyPatternL"; // uri + lower(local_name)
    
    private static final String SELECT_AVM_STORE_PROPS_BY_STORE_AND_KEY_PATTERN ="alfresco.avm.select_AVMStorePropertiesByStoreAndKeyPattern"; // store id + uri + local_name
    private static final String SELECT_AVM_STORE_PROPS_BY_STORE_AND_KEY_PATTERN_L ="alfresco.avm.select_AVMStorePropertiesByStoreAndKeyPatternL"; // store id + uri + lower(local_name)
    
    private static final String DELETE_AVM_STORE_PROP ="alfresco.avm.delete_AVMStoreProperty";
    private static final String DELETE_AVM_STORE_PROPS ="alfresco.avm.delete_AVMStoreProperties";
    
    
    private SqlSessionTemplate template;
    
    public final void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) 
    {
        this.template = sqlSessionTemplate;
    }
    
    
    // Initial generic fixes for ALF-2278 (pending SAIL-365) & ALF-498 (pending SAIL-359)
    // Note: in order to override to false, DB must be setup to be case-insensitive (at least on column avm_stores.name)
    private boolean toLower = true;
    
    public void setToLower(boolean toLower)
    {
        this.toLower = toLower;
    }
    
    @Override
    protected AVMStoreEntity getStoreEntity(long id)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", id);
        return (AVMStoreEntity) template.selectOne(SELECT_AVM_STORE_BY_ID, params);
    }
    
    @Override
    protected AVMStoreEntity getStoreEntity(String name)
    {
        AVMStoreEntity storeEntity = new AVMStoreEntity();
        storeEntity.setName(name);
        
        if (toLower)
        {
            return (AVMStoreEntity) template.selectOne(SELECT_AVM_STORE_BY_KEY_L, storeEntity);
        }
        return (AVMStoreEntity) template.selectOne(SELECT_AVM_STORE_BY_KEY, storeEntity);
    }
    
    @Override
    protected AVMStoreEntity getStoreEntityByRoot(long rootNodeId)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", rootNodeId);
        return (AVMStoreEntity) template.selectOne(SELECT_AVM_STORE_BY_ROOT_NODE_ID, params);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected List<AVMStoreEntity> getAllStoreEntities()
    {
        return (List<AVMStoreEntity>) template.selectList(SELECT_AVM_STORE_ALL);
    }
    
    @Override
    protected AVMStoreEntity createStoreEntity(AVMStoreEntity storeEntity)
    {
        template.insert(INSERT_AVM_STORE, storeEntity);
        return storeEntity;
    }
    
    @Override
    protected int updateStoreEntity(AVMStoreEntity updateStoreEntity)
    {
        updateStoreEntity.incrementVers();
        
        return template.update(UPDATE_AVM_STORE, updateStoreEntity);
    }
    
    @Override
    protected int deleteStoreEntity(long id)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", id);
        return template.delete(DELETE_AVM_STORE, params);
    }
    
    @Override
    protected void insertStorePropertyEntity(AVMStorePropertyEntity propEntity)
    {
        template.insert(INSERT_AVM_STORE_PROP, propEntity);
    }
    
    @Override
    protected int updateStorePropertyEntity(AVMStorePropertyEntity updatePropEntity)
    {
        return template.update(UPDATE_AVM_STORE_PROP, updatePropEntity);
    }
    
    @Override
    protected AVMStorePropertyEntity getStorePropertyEntity(long storeId, long qnameId)
    {
        AVMStorePropertyEntity propEntity = new AVMStorePropertyEntity();
        propEntity.setAvmStoreId(storeId);
        propEntity.setQnameId(qnameId);
        
        return (AVMStorePropertyEntity) template.selectOne(SELECT_AVM_STORE_PROP, propEntity);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected List<AVMStorePropertyEntity> getStorePropertyEntities(long storeId)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", storeId);
        
        try
        {
            return (List<AVMStorePropertyEntity>) template.selectList(SELECT_AVM_STORE_PROPS, params);
        }
        catch (Throwable e)
        {
            throw new RuntimeException("Unable to query for store properties: " + params, e);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected List<AVMStorePropertyEntity> getStorePropertyEntitiesByKeyPattern(String uriPattern, String localNamePattern)
    {
        Map<String, Object> params = new HashMap<String, Object>(2);
        params.put("uri", uriPattern);
        params.put("localname", localNamePattern);
        
        if (toLower)
        {
            return (List<AVMStorePropertyEntity>) template.selectList(SELECT_AVM_STORE_PROPS_BY_KEY_PATTERN_L, params);
        }
        return (List<AVMStorePropertyEntity>) template.selectList(SELECT_AVM_STORE_PROPS_BY_KEY_PATTERN, params);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected List<AVMStorePropertyEntity> getStorePropertyEntitiesByStoreAndKeyPattern(long storeId, String uriPattern, String localNamePattern)
    {
        Map<String, Object> params = new HashMap<String, Object>(3);
        params.put("id", storeId);
        params.put("uri", uriPattern);
        params.put("localname", localNamePattern);
        
        if (toLower)
        {
            return (List<AVMStorePropertyEntity>) template.selectList(SELECT_AVM_STORE_PROPS_BY_STORE_AND_KEY_PATTERN_L, params);
        }
        return (List<AVMStorePropertyEntity>) template.selectList(SELECT_AVM_STORE_PROPS_BY_STORE_AND_KEY_PATTERN, params);
    }
    
    @Override
    protected int deleteStorePropertyEntity(long storeId, long qnameId)
    {
        AVMStorePropertyEntity propEntity = new AVMStorePropertyEntity();
        propEntity.setAvmStoreId(storeId);
        propEntity.setQnameId(qnameId);
        
        return template.delete(DELETE_AVM_STORE_PROP, propEntity);
    }
    
    @Override
    protected int deleteStorePropertyEntities(long storeId)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", storeId);
        
        return template.delete(DELETE_AVM_STORE_PROPS, params);
    }
}
