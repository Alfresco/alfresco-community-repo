/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.security.authority;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.query.AbstractCannedQueryFactory;
import org.alfresco.query.CannedQuery;
import org.alfresco.query.CannedQueryPageDetails;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.query.CannedQuerySortDetails;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.CannedQuerySortDetails.SortOrder;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.domain.query.CannedQueryDAO;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.impl.acegi.MethodSecurityInterceptor;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyCheck;

/**
 * GetAuthorities CQ Factory - to get paged list of authorities
 * 
 * @author janv
 * @since 4.0
 */
public class GetAuthoritiesCannedQueryFactory extends AbstractCannedQueryFactory<AuthorityInfo>
{
    private NodeDAO nodeDAO;
    private QNameDAO qnameDAO;
    private CannedQueryDAO cannedQueryDAO;
    private TenantService tenantService;
    
    private MethodSecurityInterceptor methodSecurityInterceptor;
    private String methodName;
    private Object methodService;
    
    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }
    
    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }
    
    public void setCannedQueryDAO(CannedQueryDAO cannedQueryDAO) 
    {
        this.cannedQueryDAO = cannedQueryDAO;
    }
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }    
    
    public void setMethodSecurityInterceptor(MethodSecurityInterceptor methodSecurityInterceptor)
    {
        this.methodSecurityInterceptor = methodSecurityInterceptor;
    }
    
    public void setMethodName(String methodName)
    {
        this.methodName = methodName;
    }
    
    public void setMethodService(Object methodService)
    {
        this.methodService = methodService;
    }
    
    @Override
    public CannedQuery<AuthorityInfo> getCannedQuery(CannedQueryParameters parameters)
    {
        Method method = null;
        for (Method m : methodService.getClass().getMethods())
        {
            // note: currently matches first found
            if (m.getName().equals(methodName))
            {
                method = m;
                break;
            }
        }
        
        if (method == null)
        {
            throw new AlfrescoRuntimeException("Method not found: "+methodName);
        }
        
        // if not passed in (TODO or not in future cache) then generate a new query execution id
        String queryExecutionId = (parameters.getQueryExecutionId() == null ? super.getQueryExecutionId(parameters) : parameters.getQueryExecutionId());
        
        return (CannedQuery<AuthorityInfo>) new GetAuthoritiesCannedQuery(cannedQueryDAO, tenantService, methodSecurityInterceptor, method, parameters, queryExecutionId);
    }
    
    public CannedQuery<AuthorityInfo> getCannedQuery(AuthorityType type, NodeRef containerRef, String displayNameFilter, boolean sortByDisplayName, boolean sortAscending, PagingRequest pagingRequest)
    {
        ParameterCheck.mandatory("pagingRequest", pagingRequest);
        
        if ((type == null) && (containerRef == null))
        {
            throw new IllegalArgumentException("Type and/or containerRef required - both cannot be null");
        }
        
        int requestTotalCountMax = pagingRequest.getRequestTotalCountMax();
        
        Long containerNodeId = null;
        if (containerRef != null)
        {
            Pair<Long, NodeRef> nodePair = nodeDAO.getNodePair(tenantService.getName(containerRef));
            if (nodePair == null)
            {
                throw new InvalidNodeRefException("Container ref does not exist: " + containerRef, containerRef);
            }
            containerNodeId = nodePair.getFirst();
        }
        
        // specific query params
        GetAuthoritiesCannedQueryParams paramBean = new GetAuthoritiesCannedQueryParams(type,
                                                                                        containerNodeId,
                                                                                        getQNameId(ContentModel.PROP_AUTHORITY_DISPLAY_NAME),
                                                                                        displayNameFilter);
        
        // page details
        CannedQueryPageDetails cqpd = new CannedQueryPageDetails(pagingRequest.getSkipCount(), pagingRequest.getMaxItems(), CannedQueryPageDetails.DEFAULT_PAGE_NUMBER, CannedQueryPageDetails.DEFAULT_PAGE_COUNT);
        
        // sort details
        CannedQuerySortDetails cqsd = null;
        if (sortByDisplayName)
        {
            List<Pair<? extends Object, SortOrder>> sortPairs = new ArrayList<Pair<? extends Object, SortOrder>>(1);
            sortPairs.add(new Pair<String, SortOrder>("sortByName", (sortAscending ? SortOrder.ASCENDING : SortOrder.DESCENDING))); // note: sortByName is implied
            cqsd = new CannedQuerySortDetails(sortPairs);
        }
        
        // create query params holder
        CannedQueryParameters params = new CannedQueryParameters(paramBean, cqpd, cqsd, AuthenticationUtil.getRunAsUser(), requestTotalCountMax, pagingRequest.getQueryExecutionId());
        
        // return canned query instance
        return getCannedQuery(params);
    }
    
    private Long getQNameId(QName qname)
    {
        Pair<Long, QName> qnamePair = qnameDAO.getQName(qname);
        if (qnamePair == null)
        {
            throw new AlfrescoRuntimeException("QName does not exist: " + qname);
        }
        return qnamePair.getFirst();
    }
    
    @Override
    public void afterPropertiesSet() throws Exception
    {
        super.afterPropertiesSet();
        
        PropertyCheck.mandatory(this, "tenantService", tenantService);
        PropertyCheck.mandatory(this, "nodeDAO", nodeDAO);
        PropertyCheck.mandatory(this, "qnameDAO", qnameDAO);
        PropertyCheck.mandatory(this, "cannedQueryDAO", cannedQueryDAO);
        PropertyCheck.mandatory(this, "methodSecurityInterceptor", methodSecurityInterceptor);
        PropertyCheck.mandatory(this, "methodService", methodService);
        PropertyCheck.mandatory(this, "methodName", methodName);
    }
}
