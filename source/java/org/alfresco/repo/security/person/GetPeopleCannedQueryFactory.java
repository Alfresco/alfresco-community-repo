/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
import java.util.Set;

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
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
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
    protected NodeService nodeService;
    protected AuthorityService authorityService;
    
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
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }
    
    @Override
    public CannedQuery<NodeRef> getCannedQuery(CannedQueryParameters parameters)
    {
        return (CannedQuery<NodeRef>) new GetPeopleCannedQuery(nodeDAO, qnameDAO, cannedQueryDAO, tenantService, nodeService, authorityService, parameters);
    }
    
    /**
     * Retrieve an optionally filtered/sorted instance of a {@link CannedQuery} based on parameters including request for a total count (up to a given max)
     * 
     * Note: if both filtering and sorting is required then the combined total of unique QName properties should be the 0 to 3.
     *
     * @param parentRef             parent node ref
     * @param pattern               the pattern to use to filter children (wildcard character is '*')
     * @param filterProps           filter props
     * @param inclusiveAspects      If not null, only child nodes with any aspect in this collection will be included in the results.
     * @param exclusiveAspects      If not null, any child nodes with any aspect in this collection will be excluded in the results.
     * @param filterProps           filter properties
     * @param includeAdministrators include administrators in the returned results
     * @param sortProps             sort property pairs (QName and Boolean - true if ascending)
     * @param pagingRequest         skipCount, maxItems - optionally queryExecutionId and requestTotalCountMax
     * 
     * @return                      an implementation that will execute the query
     */
    public CannedQuery<NodeRef> getCannedQuery(NodeRef parentRef, String pattern, List<QName> filterProps, Set<QName> inclusiveAspects, Set<QName> exclusiveAspects, boolean includeAdministrators, List<Pair<QName, Boolean>> sortProps, PagingRequest pagingRequest)
    {
        ParameterCheck.mandatory("parentRef", parentRef);
        ParameterCheck.mandatory("pagingRequest", pagingRequest);
        
        int requestTotalCountMax = pagingRequest.getRequestTotalCountMax();
        
        // specific query params - context (parent) and inclusive filters (property values)
        GetPeopleCannedQueryParams paramBean = new GetPeopleCannedQueryParams(tenantService.getName(parentRef), filterProps, pattern, inclusiveAspects, exclusiveAspects, includeAdministrators);

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
