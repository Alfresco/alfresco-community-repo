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
package org.alfresco.repo.node;

import java.util.Set;

import org.alfresco.query.AbstractCannedQueryFactory;
import org.alfresco.query.CannedQuery;
import org.alfresco.query.CannedQueryPageDetails;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.query.PagingRequest;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.security.permissions.impl.acegi.MethodSecurityBean;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyCheck;

/**
 * GetNodesWithAspectCannedQuery canned query factory - to get paged list of
 *  Nodes with a given Aspect
 * 
 * @author Nick Burch
 * @since 4.1
 */
public class GetNodesWithAspectCannedQueryFactory extends AbstractCannedQueryFactory<NodeRef>
{
    private NodeDAO nodeDAO;
    private TenantService tenantService;
    
    private MethodSecurityBean<NodeRef> methodSecurity;
    
    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }    
    
    public void setMethodSecurity(MethodSecurityBean<NodeRef> methodSecurity)
    {
        this.methodSecurity = methodSecurity;
    }

    @Override
    public CannedQuery<NodeRef> getCannedQuery(CannedQueryParameters parameters)
    {
        return (CannedQuery<NodeRef>) new GetNodesWithAspectCannedQuery(nodeDAO, tenantService, methodSecurity, parameters);
    }
    
    /**
     * Retrieve an unsorted instance of a {@link CannedQuery} based on parameters including 
     * request for a total count (up to a given max)
     *
     * @param storeRef           the store to search in, if requested
     * @param aspectQNames       qnames of aspects to search for
     * @param pagingRequest      skipCount, maxItems - optionally queryExecutionId and requestTotalCountMax
     * 
     * @return                   an implementation that will execute the query
     */
    public CannedQuery<NodeRef> getCannedQuery(StoreRef storeRef, Set<QName> aspectQNames, PagingRequest pagingRequest)
    {
        ParameterCheck.mandatory("aspectQNames",  aspectQNames);
        ParameterCheck.mandatory("pagingRequest", pagingRequest);
        
        int requestTotalCountMax = pagingRequest.getRequestTotalCountMax();
        
        // specific query params - context (parent) and inclusive filters (child types, property values)
        GetNodesWithAspectCannedQueryParams paramBean = new GetNodesWithAspectCannedQueryParams(storeRef, aspectQNames);

        // page details
        CannedQueryPageDetails cqpd = new CannedQueryPageDetails(pagingRequest.getSkipCount(), pagingRequest.getMaxItems(), CannedQueryPageDetails.DEFAULT_PAGE_NUMBER, CannedQueryPageDetails.DEFAULT_PAGE_COUNT);
        
        // no sort details - no sorting done
        
        // create query params holder
        CannedQueryParameters params = new CannedQueryParameters(paramBean, cqpd, null, requestTotalCountMax, pagingRequest.getQueryExecutionId());
        
        // return canned query instance
        return getCannedQuery(params);
    }
    
    @Override
    public void afterPropertiesSet() throws Exception
    {
        super.afterPropertiesSet();
        
        PropertyCheck.mandatory(this, "tenantService", tenantService);
        PropertyCheck.mandatory(this, "nodeDAO", nodeDAO);
        PropertyCheck.mandatory(this, "methodSecurityInterceptor", methodSecurity);
    }
}
