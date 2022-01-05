/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.query;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.domain.contentdata.ContentUrlEntity;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.propval.PropertyStringValueEntity;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * Records management query DAO implementation
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class RecordsManagementQueryDAOImpl implements RecordsManagementQueryDAO, RecordsManagementModel
{
    /**
     * logger
     */
    @SuppressWarnings("unused")
    private static final Log logger = LogFactory.getLog(RecordsManagementQueryDAOImpl.class);

    /**
     * query names
     */
    private static final String COUNT_IDENTIFIER = "alfresco.query.rm.select_CountRMIndentifier";
    private static final String GET_CHILDREN_PROPERTY_VALUES = "select_GetStringPropertyValuesOfChildren";
    private static final String SELECT_NODE_IDS_WHICH_REFERENCE_CONTENT_URL = "select_NodeIdsWhichReferenceContentUrl";
    private static final String SCHEDULED_FOLDERS = "alfresco.query.rm.select_RecordFoldersWithSchedules";
    private static final String SCHEDULED_FOLDERS_COUNT = "alfresco.query.rm.select_RecordFoldersWithSchedulesCount";
    private static final String GET_PROP_STRING_VALUE = "alfresco.query.rm.select_PropertyStringValue";
    private static final String UPDATE_PROP_STRING_VALUE = "alfresco.query.rm.update_PropertyStringValue";

    /**
     * SQL session template
     */
    protected SqlSessionTemplate template;

    /**
     * QName DAO
     */
    protected QNameDAO qnameDAO;
    protected NodeDAO nodeDAO;
    protected TenantService tenantService;

    /**
     * @param sqlSessionTemplate SQL session template
     */
    public final void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate)
    {
        this.template = sqlSessionTemplate;
    }

    /**
     * @param qnameDAO qname DAO
     */
    public final void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }

    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }

    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.query.RecordsManagementQueryDAO#getCountRmaIdentifier(java.lang.String)
     */
    @Override
    public int getCountRmaIdentifier(String identifierValue)
    {
        int result = 0;

        // lookup the id of the identifier property qname
        Pair<Long, QName> pair = qnameDAO.getQName(PROP_IDENTIFIER);
        if (pair != null)
        {
            // create query params
            Map<String, Object> params = new HashMap<>(2);
            params.put("qnameId", pair.getFirst());
            params.put("idValue", identifierValue);

            // return the number of rma identifiers found that match the passed value
            Integer count = (Integer) template.selectOne(COUNT_IDENTIFIER, params);

            if (count != null)
            {
                result = count;
            }
        }

        return result;
    }

    @Override
    public Set<String> getChildrenStringPropertyValues(NodeRef parent, QName property)
    {
        PropertyValuesOfChildrenQueryParams queryParams = new PropertyValuesOfChildrenQueryParams();

        // Set the parent node id
        Pair<Long, NodeRef> nodePair = nodeDAO.getNodePair(tenantService.getName(parent));
        if (nodePair == null)
        {
            throw new InvalidNodeRefException("The parent node does not exist.", parent);
        }
        Long parentNodeId = nodePair.getFirst();
        queryParams.setParentId(parentNodeId);

        // Set the property qname id
        Pair<Long, QName> pair = qnameDAO.getQName(property);
        if (pair == null)
        {
            return Collections.emptySet();
        }
        queryParams.setPropertyQnameId(pair.getFirst());

        // Perform the query
        return new HashSet<>(template.selectList(GET_CHILDREN_PROPERTY_VALUES, queryParams));

    }

    /**
     * Get a set of node reference which reference the provided content URL
     *
     * @param String contentUrl	content URL
     * @return Set<NodeRef>	set of nodes that reference the provided content URL
     */
    @Override
    public Set<NodeRef> getNodeRefsWhichReferenceContentUrl(String contentUrl)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Getting nodes that reference content URL = " + contentUrl);
        }

        // create the content URL entity used to query for nodes
        ContentUrlEntity contentUrlEntity = new ContentUrlEntity();
        contentUrlEntity.setContentUrl(contentUrl.toLowerCase());

        if (logger.isDebugEnabled())
        {
            logger.debug("Executing query " + SELECT_NODE_IDS_WHICH_REFERENCE_CONTENT_URL);
        }

        // Get all the node ids which reference the given content url
        List<Long> nodeIds = template.selectList(SELECT_NODE_IDS_WHICH_REFERENCE_CONTENT_URL, contentUrlEntity);

        if (logger.isDebugEnabled())
        {
            logger.debug("Query " + SELECT_NODE_IDS_WHICH_REFERENCE_CONTENT_URL + " returned " + nodeIds.size() + " results");
        }

        // create a set of uuids which reference the content url
        Set<NodeRef> nodesReferencingContentUrl = new HashSet<NodeRef>(nodeIds.size());
        for (Long nodeId : nodeIds)
        {
            StringBuilder logMessage = null;
            NodeRef nodeRefToAdd;

            if (nodeId != null && nodeDAO.exists(nodeId))
            {
                if (logger.isDebugEnabled())
                {
                    logMessage = new StringBuilder("Adding noderef ");
                }

                // if the referencing node is a version2Store reference to the content url, add the version 2 frozen node ref
                NodeRef version2FrozenNodeRef = (NodeRef) nodeDAO.getNodeProperty(nodeId, Version2Model.PROP_QNAME_FROZEN_NODE_REF);
                if (version2FrozenNodeRef != null && nodeDAO.exists(version2FrozenNodeRef))
                {
                    nodeRefToAdd = version2FrozenNodeRef;

                    if (logger.isDebugEnabled())
                    {
                        logMessage.append(nodeRefToAdd)
                            .append(" (from version)");
                    }
                }

                // add the node ref of the referencing node
                else
                {
                    nodeRefToAdd = nodeDAO.getNodeIdStatus(nodeId)
                        .getNodeRef();
                    if (logger.isDebugEnabled())
                    {
                        logMessage.append(nodeRefToAdd);
                    }
                }

                nodesReferencingContentUrl.add(nodeRefToAdd);

                if (logger.isDebugEnabled())
                {
                    logger.debug(logMessage.toString());
                }
            }
            else
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Not adding " + nodeId + " (exist==false)");
                }
            }
        }

        return nodesReferencingContentUrl;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.query.RecordsManagementQueryDAO#getRecordFoldersWithSchedules(Long, Long)
     */
    @Override
    public List<NodeRef> getRecordFoldersWithSchedules(Long start, Long end)
    {
        Map<String, Object> params = new HashMap<>(2);
        params.put("processed", qnameDAO.getQName(ASPECT_DISPOSITION_PROCESSED)
            .getFirst());
        params.put("folderQnameId", qnameDAO.getQName(TYPE_RECORD_FOLDER)
            .getFirst());
        params.put("start", start);
        params.put("end", end);

        List<NodeRefEntity> entities = template.selectList(SCHEDULED_FOLDERS, params);

        List<NodeRef> results = new ArrayList<>();

        // convert the entities to NodeRefs
        for (NodeRefEntity nodeRefEntity : entities)
        {
            results.add(
                new NodeRef(nodeRefEntity.getProtocol(), nodeRefEntity.getIdentifier(), nodeRefEntity.getUuid()));
        }

        return results;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.query.RecordsManagementQueryDAO#getPropertyStringValueEntity(String stringValue)
     */
    public PropertyStringValueEntity getPropertyStringValueEntity(String stringValue){

        PropertyStringValueEntity propertyStringValueEntity = new PropertyStringValueEntity();
        propertyStringValueEntity.setValue(stringValue);

        return template.selectOne(GET_PROP_STRING_VALUE, propertyStringValueEntity);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.query.RecordsManagementQueryDAO#updatePropertyStringValueEntity(PropertyStringValueEntity propertyStringValueEntity)
     */
    public int updatePropertyStringValueEntity(PropertyStringValueEntity propertyStringValueEntity)
    {
        return template.update(UPDATE_PROP_STRING_VALUE, propertyStringValueEntity);
    }
}
