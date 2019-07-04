/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * Records management query DAO implementation
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class RecordsManagementQueryDAOImpl implements RecordsManagementQueryDAO, RecordsManagementModel
{
    private static final String COUNT_IDENTIFIER = "alfresco.query.rm.select_CountRMIndentifier";
    private static final String GET_CHILDREN_PROPERTY_VALUES = "select_GetStringPropertyValuesOfChildren";

    /** SQL session template */
    protected SqlSessionTemplate template;
    
    /** QName DAO */
    protected QNameDAO qnameDAO;
    protected NodeDAO nodeDAO;
    protected TenantService tenantService;
    
    /**
     * @param sqlSessionTemplate    SQL session template
     */
    public final void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) 
    {
        this.template = sqlSessionTemplate;
    }
    
    /**
     * @param qnameDAO  qname DAO
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
            Integer count = (Integer)template.selectOne(COUNT_IDENTIFIER, params);
            
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
}
