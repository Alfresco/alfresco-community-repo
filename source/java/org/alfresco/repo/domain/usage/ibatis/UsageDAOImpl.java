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
package org.alfresco.repo.domain.usage.ibatis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.domain.usage.AbstractUsageDAOImpl;
import org.alfresco.repo.domain.usage.UsageDeltaEntity;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.orm.ibatis.SqlMapClientTemplate;

import com.ibatis.sqlmap.client.event.RowHandler;

/**
 * iBatis-specific implementation of the Usage DAO.
 * 
 * @author janv
 * @since 3.4
 */
public class UsageDAOImpl extends AbstractUsageDAOImpl
{
    private static Log logger = LogFactory.getLog(UsageDAOImpl.class);
    
    private static final String INSERT_USAGE_DELTA = "alfresco.usage.insert_UsageDelta";
    private static final String SELECT_USAGE_DELTA_TOTAL_SIZE_BY_NODE = "alfresco.usage.select_GetTotalDeltaSizeByNodeId";
    private static final String SELECT_USAGE_DELTA_NODES = "alfresco.usage.select_GetUsageDeltaNodes";
    private static final String SELECT_USERS_WITH_USAGE = "alfresco.usage.select_GetUsersWithUsage";
    private static final String SELECT_USERS_WITHOUT_USAGE = "alfresco.usage.select_GetUsersWithoutUsage";
    private static final String SELECT_CONTENT_SIZES_NEW = "alfresco.usage.select_GetContentSizesForStoreNew";
    private static final String DELETE_USAGE_DELTAS_BY_NODE = "alfresco.usage.delete_UsageDeltasByNodeId";
    
    
    private SqlMapClientTemplate template;
    private QNameDAO qnameDAO;
    
    public void setSqlMapClientTemplate(SqlMapClientTemplate sqlMapClientTemplate)
    {
        this.template = sqlMapClientTemplate;
    }
    
    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }
    
    @Override
    protected UsageDeltaEntity insertUsageDeltaEntity(UsageDeltaEntity entity)
    {
        entity.setVersion(0L);
        Long id = (Long)template.insert(INSERT_USAGE_DELTA, entity);
        entity.setId(id);
        return entity;
    }
    
    @Override
    protected UsageDeltaEntity selectTotalUsageDeltaSize(long nodeEntityId)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", nodeEntityId);
        
        return (UsageDeltaEntity) template.queryForObject(SELECT_USAGE_DELTA_TOTAL_SIZE_BY_NODE, params);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected List<Long> selectUsageDeltaNodes()
    {
        return (List<Long>) template.queryForList(SELECT_USAGE_DELTA_NODES);
    }
    
    @Override
    protected int deleteUsageDeltaEntitiesByNodeId(long nodeEntityId)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", nodeEntityId);
        
        return template.delete(DELETE_USAGE_DELTAS_BY_NODE, params);
    }
    
    @Override
    protected void selectUsersWithoutUsage(StoreRef storeRef, MapHandler resultsCallback)
    {
        Pair<Long, ? extends Object> personTypeQNamePair = qnameDAO.getQName(ContentModel.TYPE_PERSON);
        Pair<Long, ? extends Object> usernamePropQNamePair = qnameDAO.getQName(ContentModel.PROP_USERNAME);
        Pair<Long, ? extends Object> sizeCurrentPropQNamePair = qnameDAO.getQName(ContentModel.PROP_SIZE_CURRENT);
        
        if (personTypeQNamePair == null || usernamePropQNamePair == null || sizeCurrentPropQNamePair == null)
        {
            return;             // The statics have not been used, so there can be no results
        }
        
        Long personTypeQNameEntityId = personTypeQNamePair.getFirst();
        Long usernamePropQNameEntityId = usernamePropQNamePair.getFirst();
        Long sizeCurrentPropQNameEntityId = sizeCurrentPropQNamePair.getFirst();
        
        Map<String, Object> params = new HashMap<String, Object>(5);
        params.put("personTypeQNameID", personTypeQNameEntityId); // cm:person (type)
        params.put("usernamePropQNameID", usernamePropQNameEntityId); // cm:username (prop)
        params.put("sizeCurrentPropQNameID", sizeCurrentPropQNameEntityId); // cm:sizeCurrent (prop)
        params.put("storeProtocol", storeRef.getProtocol());
        params.put("storeIdentifier", storeRef.getIdentifier());
        
        MapRowHandler rowHandler = new MapRowHandler(resultsCallback);
        
        template.queryWithRowHandler(SELECT_USERS_WITHOUT_USAGE, params, rowHandler);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("   Listed " + rowHandler.total + " users without usage");
        }
    }
    
    @Override
    protected void selectUsersWithUsage(StoreRef storeRef, MapHandler resultsCallback)
    {
        Pair<Long, ? extends Object> personTypeQNamePair = qnameDAO.getQName(ContentModel.TYPE_PERSON);
        Pair<Long, ? extends Object> usernamePropQNamePair = qnameDAO.getQName(ContentModel.PROP_USERNAME);
        Pair<Long, ? extends Object> sizeCurrentPropQNamePair = qnameDAO.getQName(ContentModel.PROP_SIZE_CURRENT);
        
        if (personTypeQNamePair == null || usernamePropQNamePair == null || sizeCurrentPropQNamePair == null)
        {
            return;             // The statics have not been used, so there can be no results
        }
        
        Long personTypeQNameEntityId = personTypeQNamePair.getFirst();
        Long usernamePropQNameEntityId = usernamePropQNamePair.getFirst();
        Long sizeCurrentPropQNameEntityId = sizeCurrentPropQNamePair.getFirst();
        
        Map<String, Object> params = new HashMap<String, Object>(5);
        params.put("personTypeQNameID", personTypeQNameEntityId); // cm:person (type)
        params.put("usernamePropQNameID", usernamePropQNameEntityId); // cm:username (prop)
        params.put("sizeCurrentPropQNameID", sizeCurrentPropQNameEntityId); // cm:sizeCurrent (prop)
        params.put("storeProtocol", storeRef.getProtocol());
        params.put("storeIdentifier", storeRef.getIdentifier());
        
        MapRowHandler rowHandler = new MapRowHandler(resultsCallback);
        
        template.queryWithRowHandler(SELECT_USERS_WITH_USAGE, params, rowHandler);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("   Listed " + rowHandler.total + " users with usage");
        }
    }
    
    @Override
    protected void selectUserContentSizesForStore(StoreRef storeRef, MapHandler resultsCallback)
    {
        Pair<Long, ? extends Object> contentTypeQNamePair = qnameDAO.getQName(ContentModel.TYPE_CONTENT);
        Pair<Long, ? extends Object> ownerPropQNamePair = qnameDAO.getQName(ContentModel.PROP_OWNER);
        Pair<Long, ? extends Object> contentPropQNamePair = qnameDAO.getQName(ContentModel.PROP_CONTENT);
        
        if (contentTypeQNamePair == null || ownerPropQNamePair == null || contentPropQNamePair == null)
        {
            return;             // The statics have not been used, so there can be no results
        }
        
        Long contentTypeQNameEntityId = contentTypeQNamePair.getFirst();
        Long ownerPropQNameEntityId = ownerPropQNamePair.getFirst();
        Long contentPropQNameEntityId = contentPropQNamePair.getFirst();
        
        MapRowHandler rowHandler = new MapRowHandler(resultsCallback);
        
        Map<String, Object> params = new HashMap<String, Object>(5);
        params.put("contentTypeQNameID", contentTypeQNameEntityId); // cm:content (type)
        params.put("ownerPropQNameID", ownerPropQNameEntityId); // cm:owner (prop)
        params.put("contentPropQNameID", contentPropQNameEntityId); // cm:content (prop)
        params.put("storeProtocol", storeRef.getProtocol());
        params.put("storeIdentifier", storeRef.getIdentifier());
        
        // Query for the 'new' (FK) style content data properties (stored in 'string_value')
        template.queryWithRowHandler(SELECT_CONTENT_SIZES_NEW, params, rowHandler);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("   Listed " + rowHandler.total + " old content sizes");
        }
    }
    
    /**
     * Row handler for getting map of strings
     */
    private static class MapRowHandler implements RowHandler
    {
        private final MapHandler handler;
        
        private int total = 0;
        
        private MapRowHandler(MapHandler handler)
        {
            this.handler = handler;
        }
        
        @SuppressWarnings("unchecked")
        public void handleRow(Object valueObject)
        {
            handler.handle((Map<String, Object>)valueObject);
            total++;
            if (logger.isDebugEnabled() && (total == 0 || (total % 1000 == 0) ))
            {
                logger.debug("   Listed " + total + " map entries");
            }
        }
    }
}
