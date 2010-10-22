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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.ibatis.IdsEntity;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.CrcHelper;
import org.alfresco.repo.domain.avm.AVMNodeEntity;
import org.alfresco.repo.domain.patch.AbstractPatchDAOImpl;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.orm.ibatis.SqlMapClientTemplate;

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
    private static final String SELECT_USERS_WITHOUT_USAGE_PROP = "alfresco.usage.select_GetUsersWithoutUsageProp";
    private static final String SELECT_AUTHORITIES_AND_CRC = "alfresco.patch.select_authoritiesAndCrc";
    private static final String SELECT_PERMISSIONS_ALL_ACL_IDS = "alfresco.patch.select_AllAclIds";
    private static final String SELECT_PERMISSIONS_USED_ACL_IDS = "alfresco.patch.select_UsedAclIds";
    private static final String SELECT_PERMISSIONS_MAX_ACL_ID = "alfresco.patch.select_MaxAclId";
    private static final String SELECT_PERMISSIONS_DM_NODE_COUNT = "alfresco.patch.select_DmNodeCount";
    private static final String SELECT_PERMISSIONS_DM_NODE_COUNT_WITH_NEW_ACLS = "alfresco.patch.select_DmNodeCountWherePermissionsHaveChanged";
    private static final String SELECT_CHILD_ASSOCS_COUNT = "alfresco.patch.select_allChildAssocsCount";
    private static final String SELECT_CHILD_ASSOCS_MAX_ID = "alfresco.patch.select_maxChildAssocId";
    private static final String SELECT_CHILD_ASSOCS_FOR_CRCS = "alfresco.patch.select_allChildAssocsForCrcs";
    private static final String SELECT_NODES_BY_TYPE_AND_NAME_PATTERN = "alfresco.patch.select_nodesByTypeAndNamePattern";
    
    private static final String UPDATE_ADM_OLD_CONTENT_PROPERTY = "alfresco.patch.update_admOldContentProperty";
    private static final String UPDATE_CONTENT_MIMETYPE_ID = "alfresco.patch.update_contentMimetypeId";
    private static final String UPDATE_AVM_NODE_LIST_NULLIFY_ACL = "alfresco.avm.update_AVMNodeList_nullifyAcl";
    private static final String UPDATE_AVM_NODE_LIST_SET_ACL = "alfresco.avm.update_AVMNodeList_setAcl";
    private static final String UPDATE_CHILD_ASSOC_CRC = "alfresco.patch.update_childAssocCrc";
    
    private static final String DELETE_PERMISSIONS_UNUSED_ACES = "alfresco.permissions.delete_UnusedAces";
    private static final String DELETE_PERMISSIONS_ACL_LIST = "alfresco.permissions.delete_AclList";
    private static final String DELETE_PERMISSIONS_ACL_MEMBERS_FOR_ACL_LIST = "alfresco.permissions.delete_AclMembersForAclList";
    
    private static final String SELECT_OLD_ATTR_TENANTS = "alfresco.patch.select_oldAttrTenants";
    private static final String SELECT_OLD_ATTR_AVM_LOCKS= "alfresco.patch.select_oldAttrAVMLocks";
    private static final String SELECT_OLD_ATTR_PBBS = "alfresco.patch.select_oldAttrPropertyBackedBeans";
    private static final String SELECT_OLD_ATTR_CHAINING_URS = "alfresco.patch.select_oldAttrChainingURS";
    private static final String SELECT_OLD_ATTR_CUSTOM_NAMES = "alfresco.patch.select_oldAttrCustomNames";
    
    private static final String  DELETE_OLD_ATTR_LIST = "alfresco.patch.delete_oldAttrAlfListAttributeEntries";
    private static final String  DELETE_OLD_ATTR_MAP = "alfresco.patch.delete_oldAttrAlfMapAttributeEntries";
    private static final String  DELETE_OLD_ATTR_GLOBAL = "alfresco.patch.delete_oldAttrAlfGlobalAttributes";
    private static final String  DELETE_OLD_ATTR = "alfresco.patch.delete_oldAttrAlfAttributes";
    
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
    protected long getAVMNodeEntitiesCountWhereNewInStore()
    {
        Long count = (Long) template.queryForObject(SELECT_AVM_NODE_ENTITIES_COUNT_WHERE_NEW_IN_STORE);
        return count == null ? 0L : count;
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

    public long getMaxAvmNodeID()
    {
        Long count = (Long) template.queryForObject(SELECT_AVM_MAX_NODE_ID);
        return count == null ? 0L : count;
    }

    @SuppressWarnings("unchecked")
    public List<Long> getAvmNodesWithOldContentProperties(Long minNodeId, Long maxNodeId)
    {
        IdsEntity ids = new IdsEntity();
        ids.setIdOne(minNodeId);
        ids.setIdTwo(maxNodeId);
        return (List<Long>) template.queryForList(SELECT_AVM_NODES_WITH_OLD_CONTENT_PROPERTIES, ids);
    }

    public long getMaxAdmNodeID()
    {
        Long count = (Long) template.queryForObject(SELECT_ADM_MAX_NODE_ID);
        return count == null ? 0L : count;
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
    protected long getMaxAclEntityId()
    {
        Long count = (Long) template.queryForObject(SELECT_PERMISSIONS_MAX_ACL_ID, null);
        return count == null ? 0L : count;
    }
    
    @Override
    protected long getDmNodeEntitiesCount()
    {
        Long count = (Long) template.queryForObject(SELECT_PERMISSIONS_DM_NODE_COUNT, null);
        return count == null ? 0L : count;
    }
    
    @Override
    protected long getDmNodeEntitiesCountWithNewACLs(Long above)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", above);
        Long count = (Long) template.queryForObject(SELECT_PERMISSIONS_DM_NODE_COUNT_WITH_NEW_ACLS, params);
        return count == null ? 0L : count;
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
    
    public int getChildAssocCount()
    {
        return (Integer) template.queryForObject(SELECT_CHILD_ASSOCS_COUNT);
    }
    
    @Override
    public Long getMaxChildAssocId()
    {
        Long maxAssocId = (Long) template.queryForObject(SELECT_CHILD_ASSOCS_MAX_ID);
        return maxAssocId == null ? 0L : maxAssocId;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getChildAssocsForCrcFix(
            Long minAssocId,
            Long stopAtAssocId,
            long rangeMultiplier,
            long maxIdRange,
            int maxResults)
    {
        ParameterCheck.mandatory("minAssocId", minAssocId);
        ParameterCheck.mandatory("stopAtAssocId", stopAtAssocId);
        /*
         * ALF-4529: Database connection problems when upgrading large sample 2.1.x data set
         *           We have to set an upper bound on the query that is driven by an index
         *           otherwise we get OOM on the resultset, even with a limit.
         *           Since there can be voids in the sequence, we have to check if we have hit the max ID, yet.
         */
        Long qnameId = qnameDAO.getOrCreateQName(ContentModel.PROP_NAME).getFirst();

        int queryMaxResults = maxResults;
        List<Map<String, Object>> results = new ArrayList<Map<String,Object>>(maxResults);
        while (results.size() < maxResults && minAssocId <= stopAtAssocId)
        {
            // Avoid getting too few results because of voids.
            // On the other hand, the distribution of child assoc types can result in swathes of
            // the table containing voids and rows of no interest.  So we ramp up the multiplier
            // to take larger and larger ID ranges in order to quickly walk through these zones.
            Long maxAssocId = minAssocId + Math.min(maxResults * rangeMultiplier, maxIdRange);

            IdsEntity entity = new IdsEntity();
            entity.setIdOne(qnameId);
            entity.setIdTwo(minAssocId);
            entity.setIdThree(maxAssocId);
            
            try
            {
                List<Map<String, Object>> rows = template.queryForList(SELECT_CHILD_ASSOCS_FOR_CRCS, entity, 0, queryMaxResults);
                if (results.size() == 0 && rows.size() >= maxResults)
                {
                    // We have all we need
                    results = rows;
                    break;
                }
                // Add these rows to the result
                results.addAll(rows);
                // Calculate new maxResults
                queryMaxResults = maxResults - results.size();
                // Move the minAssocId up to ensure we get new results
                // If we got fewer results than queryMaxResults, then there were too many voids and we
                // requery using the previous maxAssocId
                minAssocId = maxAssocId;
                // Double the range multiplier if we have a low hit-rate (<50% of desired size)
                if (rows.size() < queryMaxResults / 2)
                {
                    rangeMultiplier *= 2L;
                }
            }
            catch (Throwable e)
            {
                // Hit a DB problem.  Log all the details of the query so that parameters can be adjusted externally.
                String msg =
                        "Failed to query for batch of alf_child_assoc rows; use a lower 'maxIdRange': \n" +
                        "   minAssocId:      " + minAssocId + "\n" +
                        "   maxAssocId:      " + maxAssocId + "\n" +
                        "   maxIdRange:      " + maxIdRange + "\n" +
                        "   stopAtAssocId:   " + stopAtAssocId + "\n" +
                        "   rangeMultiplier: " + rangeMultiplier + "\n" +
                        "   queryMaxResults: " + queryMaxResults;
                logger.error(msg);
                throw new RuntimeException(msg, e);
            }
        }
        
        // Done
        return results;
    }
    
    public int updateChildAssocCrc(Long assocId, Long childNodeNameCrc, Long qnameCrc)
    {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", assocId);
        params.put("childNodeNameCrc", childNodeNameCrc);
        params.put("qnameCrc", qnameCrc);
        return template.update(UPDATE_CHILD_ASSOC_CRC, params);
    }
    
    public List<Pair<NodeRef, String>> getNodesOfTypeWithNamePattern(QName typeQName, String namePattern)
    {
        Pair<Long, QName> typeQNamePair = qnameDAO.getQName(typeQName);
        if (typeQNamePair == null)
        {
            // No point querying
            return Collections.emptyList();
        }
        Long typeQNameId = typeQNamePair.getFirst();
        
        Pair<Long, QName> propQNamePair = qnameDAO.getQName(ContentModel.PROP_NAME);
        if (propQNamePair == null)
        {
            return Collections.emptyList();
        }
        Long propQNameId = propQNamePair.getFirst();
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("typeQNameId", typeQNameId);
        params.put("propQNameId", propQNameId);
        params.put("namePattern", namePattern);
        
        final List<Pair<NodeRef, String>> results = new ArrayList<Pair<NodeRef, String>>(500);
        RowHandler rowHandler = new RowHandler()
        {
            @SuppressWarnings("unchecked")
            public void handleRow(Object rowObject)
            {
                Map<String, Object> row = (Map<String, Object>) rowObject;
                String protocol = (String) row.get("protocol");
                String identifier = (String) row.get("identifier");
                String uuid = (String) row.get("uuid");
                NodeRef nodeRef = new NodeRef(new StoreRef(protocol, identifier), uuid);
                String name = (String) row.get("name");
                Pair<NodeRef, String> pair = new Pair<NodeRef, String>(nodeRef, name);
                results.add(pair);
            }
        };
        template.queryWithRowHandler(SELECT_NODES_BY_TYPE_AND_NAME_PATTERN, params, rowHandler);
        return results;
    }
    
    @Override
    protected void getOldAttrTenantsImpl(RowHandler rowHandler)
    {
        template.queryWithRowHandler(SELECT_OLD_ATTR_TENANTS, rowHandler);
    }
    
    @Override
    protected void getOldAttrAVMLocksImpl(RowHandler rowHandler)
    {
        template.queryWithRowHandler(SELECT_OLD_ATTR_AVM_LOCKS, rowHandler);
    }
    
    @Override
    protected void getOldAttrPropertyBackedBeansImpl(RowHandler rowHandler)
    {
        template.queryWithRowHandler(SELECT_OLD_ATTR_PBBS, rowHandler);
    }
    
    @Override
    protected void getOldAttrChainingURSImpl(RowHandler rowHandler)
    {
        template.queryWithRowHandler(SELECT_OLD_ATTR_CHAINING_URS, rowHandler);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected List<String> getOldAttrCustomNamesImpl()
    {
        return (List<String>)template.queryForList(SELECT_OLD_ATTR_CUSTOM_NAMES);
    }
    
    @Override
    protected void deleteAllOldAttrsImpl()
    {
        int deleted = 0;
        
        deleted = template.delete(DELETE_OLD_ATTR_LIST);
        logger.info("Deleted "+deleted+" rows from alf_list_attribute_entries");
        
        deleted = template.delete(DELETE_OLD_ATTR_MAP);
        logger.info("Deleted "+deleted+" rows from alf_map_attribute_entries");
        
        deleted = template.delete(DELETE_OLD_ATTR_GLOBAL);
        logger.info("Deleted "+deleted+" rows from alf_global_attributes");
        
        deleted = template.delete(DELETE_OLD_ATTR);
        logger.info("Deleted "+deleted+" rows from alf_attributes");
    }
}
