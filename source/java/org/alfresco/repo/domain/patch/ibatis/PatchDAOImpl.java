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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.ibatis.IdsEntity;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.CrcHelper;
import org.alfresco.repo.domain.avm.AVMNodeEntity;
import org.alfresco.repo.domain.patch.AbstractPatchDAOImpl;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.orm.ibatis.SqlMapClientTemplate;

import com.ibatis.sqlmap.client.SqlMapSession;
import com.ibatis.sqlmap.client.event.RowHandler;
import com.ibatis.sqlmap.engine.execution.SqlExecutor;

/**
 * iBatis-specific implementation of the AVMPatch DAO.
 * 
 * @author janv
 * @since 3.2
 */
public class PatchDAOImpl extends AbstractPatchDAOImpl
{
    private static Log logger = LogFactory.getLog(PatchDAOImpl.class);
    
    private static final String SELECT_AVM_NODE_ENTITIES_COUNT_WHERE_NEW_IN_STORE = "alfresco.avm.select_AVMNodeEntitiesCountWhereNewInStore";
    private static final String SELECT_AVM_NODE_ENTITIES_WITH_EMPTY_GUID = "alfresco.avm.select_AVMNodesWithEmptyGUID";
    private static final String SELECT_AVM_LD_NODE_ENTITIES_NULL_VERSION = "alfresco.avm.select_AVMNodes_nullVersionLayeredDirectories";
    private static final String SELECT_AVM_LF_NODE_ENTITIES_NULL_VERSION = "alfresco.avm.select_AVMNodes_nullVersionLayeredFiles";
    private static final String SELECT_AVM_MAX_NODE_ID = "alfresco.patch.select_avmMaxNodeId";
    private static final String SELECT_ADM_MAX_NODE_ID = "alfresco.patch.select_admMaxNodeId";
    private static final String SELECT_AVM_NODES_WITH_OLD_CONTENT_PROPERTIES = "alfresco.patch.select_avmNodesWithOldContentProperties";
    private static final String SELECT_ADM_OLD_CONTENT_PROPERTIES = "alfresco.patch.select_admOldContentProperties";
    private static final String SELECT_AUTHORITIES_AND_CRC = "alfresco.patch.select_authoritiesAndCrc";
    private static final String UPDATE_ADM_OLD_CONTENT_PROPERTY = "alfresco.patch.update_admOldContentProperty";
    private static final String UPDATE_CONTENT_MIMETYPE_ID = "alfresco.patch.update_contentMimetypeId";
    private static final String UPDATE_AVM_NODE_LIST_NULLIFY_ACL = "alfresco.avm.update_AVMNodeList_nullifyAcl";
    private static final String UPDATE_AVM_NODE_LIST_SET_ACL = "alfresco.avm.update_AVMNodeList_setAcl";
    
    private static final String SELECT_USERS_WITHOUT_USAGE_PROP = "alfresco.usage.select_GetUsersWithoutUsageProp";
    
    private static final String SELECT_PERMISSIONS_MAX_ACL_ID = "alfresco.permissions.select_MaxAclId";
    private static final String SELECT_PERMISSIONS_DM_NODE_COUNT = "alfresco.permissions.select_DmNodeCount";
    private static final String SELECT_PERMISSIONS_DM_NODE_COUNT_WITH_NEW_ACLS = "alfresco.permissions.select_DmNodeCountWherePermissionsHaveChanged";
    
    private static final String SELECT_PERMISSIONS_ALL_ACL_IDS = "alfresco.permissions.select_AllAclIds";
    private static final String SELECT_PERMISSIONS_USED_ACL_IDS = "alfresco.permissions.select_UsedAclIds";
    private static final String DELETE_PERMISSIONS_UNUSED_ACES = "alfresco.permissions.delete_UnusedAces";
    private static final String DELETE_PERMISSIONS_ACL_LIST = "alfresco.permissions.delete_AclList";
    private static final String DELETE_PERMISSIONS_ACL_MEMBERS_FOR_ACL_LIST = "alfresco.permissions.delete_AclMembersForAclList";
    
    
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
    protected boolean supportsProgressTrackingImpl()
    {
        try
        {
            return template.getSqlMapClient().getCurrentConnection().getMetaData().supportsTransactionIsolationLevel(1);
        }
        catch (SQLException e)
        {
            return false;
        }
    }
    
    @Override
    protected Long getAVMNodeEntitiesCountWhereNewInStore()
    {
        return (Long) template.queryForObject(SELECT_AVM_NODE_ENTITIES_COUNT_WHERE_NEW_IN_STORE);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected List<AVMNodeEntity> getAVMNodeEntitiesWithEmptyGUID(int maxResults)
    {
        if (maxResults < 0)
        {
            maxResults = SqlExecutor.NO_MAXIMUM_RESULTS;
        }
        
        return (List<AVMNodeEntity>) template.queryForList(SELECT_AVM_NODE_ENTITIES_WITH_EMPTY_GUID, 0, maxResults);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected List<AVMNodeEntity> getNullVersionLayeredDirectoryNodeEntities(int maxResults)
    {
        if (maxResults < 0)
        {
            maxResults = SqlExecutor.NO_MAXIMUM_RESULTS;
        }
        
        return (List<AVMNodeEntity>) template.queryForList(SELECT_AVM_LD_NODE_ENTITIES_NULL_VERSION, 0, maxResults);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected List<AVMNodeEntity> getNullVersionLayeredFileNodeEntities(int maxResults)
    {
        if (maxResults < 0)
        {
            maxResults = SqlExecutor.NO_MAXIMUM_RESULTS;
        }
        
        return (List<AVMNodeEntity>) template.queryForList(SELECT_AVM_LF_NODE_ENTITIES_NULL_VERSION, 0, maxResults);
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
    
    @Override
    protected void selectUsersWithoutUsageProp(StoreRef storeRef, StringHandler handler)
    {
        long personTypeQNameEntityId = qnameDAO.getOrCreateQName(ContentModel.TYPE_PERSON).getFirst();
        long sizeCurrentPropQNameEntityId = qnameDAO.getOrCreateQName(ContentModel.PROP_SIZE_CURRENT).getFirst();
        
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("personTypeQNameID", personTypeQNameEntityId); // cm:person
        params.put("sizeCurrentPropQNameID",sizeCurrentPropQNameEntityId); // cm:sizeCurrent
        params.put("storeProtocol", storeRef.getProtocol());
        params.put("storeIdentifier", storeRef.getIdentifier());
        params.put("isDeleted", false);
        
        StringRowHandler rowHandler = new StringRowHandler(handler);
        
        template.queryWithRowHandler(SELECT_USERS_WITHOUT_USAGE_PROP, params, rowHandler);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("   Listed " + rowHandler.total + " users without usage");
        }
    }
    
    /**
     * Row handler for getting strings
     */
    private static class StringRowHandler implements RowHandler
    {
        private final StringHandler handler;
        
        private int total = 0;
        
        private StringRowHandler(StringHandler handler)
        {
            this.handler = handler;
        }
        public void handleRow(Object valueObject)
        {
            handler.handle((String)valueObject);
            total++;
            if (logger.isDebugEnabled() && (total == 0 || (total % 1000 == 0) ))
            {
                logger.debug("   Listed " + total + " strings");
            }
        }
    }
    
    @Override
    protected int updateAVMNodeEntitiesNullifyAcl(List<Long> nodeIds)
    {
        return template.update(UPDATE_AVM_NODE_LIST_NULLIFY_ACL, nodeIds);
    }
    
    @Override
    protected int updateAVMNodeEntitiesSetAcl(long aclId, List<Long> nodeIds)
    {
        IdListOfIdsParam params = new IdListOfIdsParam();
        params.setId(aclId);
        params.setListOfIds(nodeIds);
        
        return template.update(UPDATE_AVM_NODE_LIST_SET_ACL, params);
    }
    
    @Override
    protected Long getMaxAclEntityId()
    {
        SqlMapSession session = null;
        try
        {
            session = template.getSqlMapClient().openSession();
            Connection conn = template.getSqlMapClient().getCurrentConnection();
            int isolationLevel = conn.getTransactionIsolation();
            try
            {
                conn.setTransactionIsolation(1);
                return (Long)template.queryForObject(SELECT_PERMISSIONS_MAX_ACL_ID, null);
            }
            finally
            {
                conn.setTransactionIsolation(isolationLevel);
            }
        }
        catch (SQLException e)
        {
            throw new AlfrescoRuntimeException("Failed to set TX isolation level", e);
        }
        finally
        {
            if (session != null)
            {
                session.close();
            }
        }
    }
    
    @Override
    protected long getDmNodeEntitiesCount()
    {
        SqlMapSession session = null;
        try
        {
            session = template.getSqlMapClient().openSession();
            Connection conn = template.getSqlMapClient().getCurrentConnection();
            int isolationLevel = conn.getTransactionIsolation();
            try
            {
                conn.setTransactionIsolation(1);
                
                return (Long)template.queryForObject(SELECT_PERMISSIONS_DM_NODE_COUNT, null);
            }
            finally
            {
                conn.setTransactionIsolation(isolationLevel);
            }
        }
        catch (SQLException e)
        {
            throw new AlfrescoRuntimeException("Failed to set TX isolation level", e);
        }
        finally
        {
            if (session != null)
            {
                session.close();
            }
        }
    }
    
    @Override
    protected long getDmNodeEntitiesCountWithNewACLs(Long above)
    {
        SqlMapSession session = null;
        try
        {
            session = template.getSqlMapClient().openSession();
            Connection conn = template.getSqlMapClient().getCurrentConnection();
            int isolationLevel = conn.getTransactionIsolation();
            try
            {
                conn.setTransactionIsolation(1);
                
                Map<String, Object> params = new HashMap<String, Object>(1);
                params.put("id", above);
                
                return (Long)template.queryForObject(SELECT_PERMISSIONS_DM_NODE_COUNT_WITH_NEW_ACLS, params);
            }
            finally
            {
                conn.setTransactionIsolation(isolationLevel);
            }
        }
        catch (SQLException e)
        {
            throw new AlfrescoRuntimeException("Failed to set TX isolation level", e);
        }
        finally
        {
            if (session != null)
            {
                session.close();
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected List<Long> selectAllAclEntityIds()
    {
        return (List<Long>) template.queryForList(SELECT_PERMISSIONS_ALL_ACL_IDS);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected List<Long> selectNonDanglingAclEntityIds()
    {
        return (List<Long>) template.queryForList(SELECT_PERMISSIONS_USED_ACL_IDS);
    }
    
    @Override
    protected int deleteDanglingAceEntities()
    {
        return template.delete(DELETE_PERMISSIONS_UNUSED_ACES);
    }
    
    @Override
    protected int deleteAclEntities(List<Long> aclIds)
    {
        return template.delete(DELETE_PERMISSIONS_ACL_LIST, aclIds);
    }
    
    @Override
    protected int deleteAclMemberEntitiesForAcls(List<Long> aclIds)
    {
        return template.delete(DELETE_PERMISSIONS_ACL_MEMBERS_FOR_ACL_LIST, aclIds);
    }

    public List<String> getAuthoritiesWithNonUtf8Crcs()
    {
        final List<String> results = new ArrayList<String>(1000);
        RowHandler rowHandler = new RowHandler()
        {
            @SuppressWarnings("unchecked")
            public void handleRow(Object valueObject)
            {
                Map<String, Object> result = (Map<String, Object>) valueObject;
                String authority = (String) result.get("authority");
                Long crc = (Long) result.get("crc");
                Long crcShouldBe = CrcHelper.getStringCrcPair(authority, 32, true, true).getSecond();
                if (!crcShouldBe.equals(crc))
                {
                    // One to fix
                    results.add(authority);
                }
            }
        };
        template.queryWithRowHandler(SELECT_AUTHORITIES_AND_CRC, rowHandler);
        // Done
        return results;
    }
}
