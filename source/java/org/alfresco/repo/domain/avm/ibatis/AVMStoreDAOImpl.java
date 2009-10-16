/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
package org.alfresco.repo.domain.avm.ibatis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.domain.avm.AVMStoreEntity;
import org.alfresco.repo.domain.avm.AVMStorePropertyEntity;
import org.alfresco.repo.domain.avm.AbstractAVMStoreDAOImpl;
import org.springframework.orm.ibatis.SqlMapClientTemplate;

/**
 * iBatis-specific implementation of the AVMStore DAO.
 * 
 * @author janv
 * @since 3.2
 */
public class AVMStoreDAOImpl extends AbstractAVMStoreDAOImpl
{
    private static final String SELECT_AVM_STORE_BY_ID ="alfresco.avm.select_AVMStoreById";
    private static final String SELECT_AVM_STORE_BY_KEY ="alfresco.avm.select_AVMStoreByKey";
    private static final String SELECT_AVM_STORE_BY_ROOT_NODE_ID ="alfresco.avm.select_AVMStoreByRootNodeId";
    private static final String SELECT_AVM_STORE_ALL ="alfresco.avm.select_AVMStoreAll";
    
    private static final String INSERT_AVM_STORE ="alfresco.avm.insert_AVMStore";
    private static final String DELETE_AVM_STORE ="alfresco.avm.delete_AVMStore";
    private static final String UPDATE_AVM_STORE ="alfresco.avm.update_AVMStore";
    
    private static final String INSERT_AVM_STORE_PROP ="alfresco.avm.insert_AVMStoreProperty";
    private static final String UPDATE_AVM_STORE_PROP ="alfresco.avm.update_AVMStoreProperty";
    private static final String SELECT_AVM_STORE_PROP ="alfresco.avm.select_AVMStoreProperty";
    private static final String SELECT_AVM_STORE_PROPS ="alfresco.avm.select_AVMStoreProperties";
    private static final String SELECT_AVM_STORE_PROPS_BY_KEY_PATTERN ="alfresco.avm.select_AVMStorePropertiesByKeyPattern";
    private static final String SELECT_AVM_STORE_PROPS_BY_STORE_AND_KEY_PATTERN ="alfresco.avm.select_AVMStorePropertiesByStoreAndKeyPattern";
    private static final String DELETE_AVM_STORE_PROP ="alfresco.avm.delete_AVMStoreProperty";
    private static final String DELETE_AVM_STORE_PROPS ="alfresco.avm.delete_AVMStoreProperties";
    
    
    private SqlMapClientTemplate template;
    
    public void setSqlMapClientTemplate(SqlMapClientTemplate sqlMapClientTemplate)
    {
        this.template = sqlMapClientTemplate;
    }
    
    @Override
    protected AVMStoreEntity getStoreEntity(long id)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", id);
        return (AVMStoreEntity) template.queryForObject(SELECT_AVM_STORE_BY_ID, params);
    }
    
    @Override
    protected AVMStoreEntity getStoreEntity(String name)
    {
        AVMStoreEntity storeEntity = new AVMStoreEntity();
        storeEntity.setName(name);
        return (AVMStoreEntity) template.queryForObject(SELECT_AVM_STORE_BY_KEY, storeEntity);
    }
    
    @Override
    protected AVMStoreEntity getStoreEntityByRoot(long rootNodeId)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", rootNodeId);
        return (AVMStoreEntity) template.queryForObject(SELECT_AVM_STORE_BY_ROOT_NODE_ID, params);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected List<AVMStoreEntity> getAllStoreEntities()
    {
        return (List<AVMStoreEntity>) template.queryForList(SELECT_AVM_STORE_ALL);
    }
    
    @Override
    protected AVMStoreEntity createStoreEntity(AVMStoreEntity storeEntity)
    {
        Long id = (Long) template.insert(INSERT_AVM_STORE, storeEntity);
        storeEntity.setId(id);
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
        
        return (AVMStorePropertyEntity) template.queryForObject(SELECT_AVM_STORE_PROP, propEntity);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected List<AVMStorePropertyEntity> getStorePropertyEntities(long storeId)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", storeId);
        
        return (List<AVMStorePropertyEntity>) template.queryForList(SELECT_AVM_STORE_PROPS, params);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected List<AVMStorePropertyEntity> getStorePropertyEntitiesByKeyPattern(String uriPattern, String localNamePattern)
    {
        Map<String, Object> params = new HashMap<String, Object>(2);
        params.put("uri", uriPattern);
        params.put("localname", localNamePattern);
        
        return (List<AVMStorePropertyEntity>) template.queryForList(SELECT_AVM_STORE_PROPS_BY_KEY_PATTERN, params);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected List<AVMStorePropertyEntity> getStorePropertyEntitiesByStoreAndKeyPattern(long storeId, String uriPattern, String localNamePattern)
    {
        Map<String, Object> params = new HashMap<String, Object>(3);
        params.put("id", storeId);
        params.put("uri", uriPattern);
        params.put("localname", localNamePattern);
        
        return (List<AVMStorePropertyEntity>) template.queryForList(SELECT_AVM_STORE_PROPS_BY_STORE_AND_KEY_PATTERN, params);
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
