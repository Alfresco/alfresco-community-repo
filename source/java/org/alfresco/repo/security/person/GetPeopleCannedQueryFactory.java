/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.security.person;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.query.AbstractCannedQueryFactory;
import org.alfresco.query.CannedQuery;
import org.alfresco.query.CannedQueryPageDetails;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.query.CannedQuerySortDetails;
import org.alfresco.query.CannedQuerySortDetails.SortOrder;
import org.alfresco.query.PagingRequest;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.domain.query.CannedQueryDAO;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyCheck;

/**
 * GetPeople canned query factory - to get paged list of people
 * 
 * @author janv
 * @since 4.1.2
 */
public class GetPeopleCannedQueryFactory extends AbstractCannedQueryFactory<NodeRef>
{
    protected NodeDAO nodeDAO;
    protected QNameDAO qnameDAO;
    protected CannedQueryDAO cannedQueryDAO;
    protected TenantService tenantService;
    
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
    
    @Override
    public CannedQuery<NodeRef> getCannedQuery(CannedQueryParameters parameters)
    {
        return (CannedQuery<NodeRef>) new GetPeopleCannedQuery(nodeDAO, qnameDAO, cannedQueryDAO, tenantService, parameters);
    }
    
    public CannedQuery<NodeRef> getCannedQuery(NodeRef parentRef, String pattern, List<QName> filterProps, List<Pair<QName, Boolean>> sortProps, PagingRequest pagingRequest)
    {
        ParameterCheck.mandatory("parentRef", parentRef);
        ParameterCheck.mandatory("pagingRequest", pagingRequest);
        
        int requestTotalCountMax = pagingRequest.getRequestTotalCountMax();
        
        // specific query params - context (parent) and inclusive filters (property values)
        GetPeopleCannedQueryParams paramBean = new GetPeopleCannedQueryParams(tenantService.getName(parentRef), filterProps, pattern);
        
        // page details
        CannedQueryPageDetails cqpd = new CannedQueryPageDetails(pagingRequest.getSkipCount(), pagingRequest.getMaxItems(), CannedQueryPageDetails.DEFAULT_PAGE_NUMBER, CannedQueryPageDetails.DEFAULT_PAGE_COUNT);
        
        // sort details
        CannedQuerySortDetails cqsd = null;
        if (sortProps != null)
        {
            List<Pair<? extends Object, SortOrder>> sortPairs = new ArrayList<Pair<? extends Object, SortOrder>>(sortProps.size());
            for (Pair<QName, Boolean> sortProp : sortProps)
            {
                boolean sortAsc = ((sortProp.getSecond() == null) || sortProp.getSecond());
                sortPairs.add(new Pair<QName, SortOrder>(sortProp.getFirst(), (sortAsc ? SortOrder.ASCENDING : SortOrder.DESCENDING)));
            }
            
            cqsd = new CannedQuerySortDetails(sortPairs);
        }
        
        // create query params holder
        CannedQueryParameters params = new CannedQueryParameters(paramBean, cqpd, cqsd, requestTotalCountMax, pagingRequest.getQueryExecutionId());
        
        // return canned query instance
        return getCannedQuery(params);
    }
    
    @Override
    public void afterPropertiesSet() throws Exception
    {
        super.afterPropertiesSet();
        
        PropertyCheck.mandatory(this, "tenantService", tenantService);
        PropertyCheck.mandatory(this, "nodeDAO", nodeDAO);
        PropertyCheck.mandatory(this, "qnameDAO", qnameDAO);
        PropertyCheck.mandatory(this, "cannedQueryDAO", cannedQueryDAO);
    }
}
