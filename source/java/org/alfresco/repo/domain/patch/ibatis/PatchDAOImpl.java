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
package org.alfresco.repo.domain.patch.ibatis;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.ibatis.IdsEntity;
import org.alfresco.repo.domain.avm.AVMNodeEntity;
import org.alfresco.repo.domain.patch.AbstractPatchDAOImpl;
import org.springframework.orm.ibatis.SqlMapClientTemplate;

/**
 * iBatis-specific implementation of the AVMPatch DAO.
 * 
 * @author janv
 * @since 3.2
 */
public class PatchDAOImpl extends AbstractPatchDAOImpl
{
    private static final String SELECT_AVM_NODE_ENTITIES_COUNT_WHERE_NEW_IN_STORE = "alfresco.avm.select_AVMNodeEntitiesCountWhereNewInStore";
    private static final String SELECT_AVM_NODE_ENTITIES_WITH_EMPTY_GUID = "alfresco.avm.select_AVMNodesWithEmptyGUID";
    private static final String SELECT_AVM_LD_NODE_ENTITIES_NULL_VERSION = "alfresco.avm.select_AVMNodes_nullVersionLayeredDirectories";
    private static final String SELECT_AVM_LF_NODE_ENTITIES_NULL_VERSION = "alfresco.avm.select_AVMNodes_nullVersionLayeredFiles";
    private static final String SELECT_AVM_MAX_NODE_ID = "alfresco.patch.select_avmMaxNodeId";
    private static final String SELECT_ADM_MAX_NODE_ID = "alfresco.patch.select_admMaxNodeId";
    private static final String SELECT_AVM_NODES_WITH_OLD_CONTENT_PROPERTIES = "alfresco.patch.select_avmNodesWithOldContentProperties";
    private static final String SELECT_ADM_OLD_CONTENT_PROPERTIES = "alfresco.patch.select_admOldContentProperties";
    private static final String UPDATE_ADM_OLD_CONTENT_PROPERTY = "alfresco.patch.update_admOldContentProperty";
    private static final String UPDATE_CONTENT_MIMETYPE_ID = "alfresco.patch.update_contentMimetypeId";
    
    private SqlMapClientTemplate template;
    
    public void setSqlMapClientTemplate(SqlMapClientTemplate sqlMapClientTemplate)
    {
        this.template = sqlMapClientTemplate;
    }

    public void startBatch()
    {
        try
        {
            template.getSqlMapClient().startBatch();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Failed to start batch", e);
        }
    }

    public void executeBatch()
    {
        try
        {
            template.getSqlMapClient().executeBatch();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Failed to start batch", e);
        }
    }

    @Override
    protected Long getAVMNodeEntitiesCountWhereNewInStore()
    {
        return (Long) template.queryForObject(SELECT_AVM_NODE_ENTITIES_COUNT_WHERE_NEW_IN_STORE);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected List<AVMNodeEntity> getAVMNodeEntitiesWithEmptyGUID()
    {
        return (List<AVMNodeEntity>) template.queryForList(SELECT_AVM_NODE_ENTITIES_WITH_EMPTY_GUID);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected List<AVMNodeEntity> getNullVersionLayeredDirectoryNodeEntities()
    {
        return (List<AVMNodeEntity>) template.queryForList(SELECT_AVM_LD_NODE_ENTITIES_NULL_VERSION);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected List<AVMNodeEntity> getNullVersionLayeredFileNodeEntities()
    {
        return (List<AVMNodeEntity>) template.queryForList(SELECT_AVM_LF_NODE_ENTITIES_NULL_VERSION);
    }

    public Long getMaxAvmNodeID()
    {
        return (Long) template.queryForObject(SELECT_AVM_MAX_NODE_ID);
    }

    @SuppressWarnings("unchecked")
    public List<Long> getAvmNodesWithOldContentProperties(Long minNodeId, Long maxNodeId)
    {
        IdsEntity ids = new IdsEntity();
        ids.setIdOne(minNodeId);
        ids.setIdTwo(maxNodeId);
        return (List<Long>) template.queryForList(SELECT_AVM_NODES_WITH_OLD_CONTENT_PROPERTIES, ids);
    }

    public Long getMaxAdmNodeID()
    {
        return (Long) template.queryForObject(SELECT_ADM_MAX_NODE_ID);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<Map<String, Object>> getAdmOldContentProperties(Long minNodeId, Long maxNodeId)
    {
        IdsEntity ids = new IdsEntity();
        ids.setIdOne(minNodeId);
        ids.setIdTwo(maxNodeId);
        return (List<Map<String, Object>>) template.queryForList(SELECT_ADM_OLD_CONTENT_PROPERTIES, ids);
    }
    
    @Override
    protected void updateAdmOldContentProperty(Long nodeId, Long qnameId, Integer listIndex, Long localeId, Long longValue)
    {
        Map<String, Object> params = new HashMap<String, Object>(11);
        params.put("nodeId", nodeId);
        params.put("qnameId", qnameId);
        params.put("listIndex", listIndex);
        params.put("localeId", localeId);
        params.put("longValue", longValue);
        template.update(UPDATE_ADM_OLD_CONTENT_PROPERTY, params);
    }

    public int updateContentMimetypeIds(Long oldMimetypeId, Long newMimetypeId)
    {
        Map<String, Object> params = new HashMap<String, Object>(11);
        params.put("newMimetypeId", newMimetypeId);
        params.put("oldMimetypeId", oldMimetypeId);
        return template.update(UPDATE_CONTENT_MIMETYPE_ID, params);
    }
}
