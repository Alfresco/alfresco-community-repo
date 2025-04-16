/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.security.authority;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
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
import org.alfresco.repo.security.permissions.impl.acegi.MethodSecurityBean;
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
    private MethodSecurityBean<AuthorityInfo> methodSecurity;

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

    public void setMethodSecurity(MethodSecurityBean<AuthorityInfo> methodSecurity)
    {
        this.methodSecurity = methodSecurity;
    }

    @Override
    public CannedQuery<AuthorityInfo> getCannedQuery(CannedQueryParameters parameters)
    {
        return (CannedQuery<AuthorityInfo>) new GetAuthoritiesCannedQuery(cannedQueryDAO, tenantService, methodSecurity, parameters);
    }

    public CannedQuery<AuthorityInfo> getCannedQuery(AuthorityType type, NodeRef containerRef, String displayNameFilter, String sortBy, boolean sortAscending, PagingRequest pagingRequest)
    {
        ParameterCheck.mandatory("containerRef", containerRef);
        ParameterCheck.mandatory("pagingRequest", pagingRequest);

        int requestTotalCountMax = pagingRequest.getRequestTotalCountMax();

        Pair<Long, NodeRef> nodePair = nodeDAO.getNodePair(tenantService.getName(containerRef));
        if (nodePair == null)
        {
            throw new InvalidNodeRefException("Container ref does not exist: " + containerRef, containerRef);
        }

        Long containerNodeId = nodePair.getFirst();

        Long qnameAuthDisplayNameId = Long.MIN_VALUE; // We query but using a value that won't return results
        Pair<Long, QName> qnameAuthDisplayNamePair = qnameDAO.getQName(ContentModel.PROP_AUTHORITY_DISPLAY_NAME);
        if (qnameAuthDisplayNamePair != null)
        {
            qnameAuthDisplayNameId = qnameAuthDisplayNamePair.getFirst();
        }

        // this can be null, in which case, there is no filtering on type, done at the database level
        Long typeQNameId = getQNameIdForType(type);
        // specific query params
        GetAuthoritiesCannedQueryParams paramBean = new GetAuthoritiesCannedQueryParams(type,
                typeQNameId,
                containerNodeId,
                qnameAuthDisplayNameId,
                displayNameFilter);

        // page details
        CannedQueryPageDetails cqpd = new CannedQueryPageDetails(pagingRequest.getSkipCount(), pagingRequest.getMaxItems(), CannedQueryPageDetails.DEFAULT_PAGE_NUMBER, CannedQueryPageDetails.DEFAULT_PAGE_COUNT);

        // sort details
        CannedQuerySortDetails cqsd = null;
        if (sortBy != null)
        {
            List<Pair<? extends Object, SortOrder>> sortPairs = new ArrayList<Pair<? extends Object, SortOrder>>(1);
            sortPairs.add(new Pair<String, SortOrder>(sortBy, (sortAscending ? SortOrder.ASCENDING : SortOrder.DESCENDING)));
            cqsd = new CannedQuerySortDetails(sortPairs);
        }

        // create query params holder
        CannedQueryParameters params = new CannedQueryParameters(paramBean, cqpd, cqsd, requestTotalCountMax, pagingRequest.getQueryExecutionId());

        // return canned query instance
        return getCannedQuery(params);
    }

    private Long getQNameIdForType(AuthorityType type)
    {
        if (type == null)
        {
            return null;
        }
        Pair<Long, QName> typeQNamePair = null;
        switch (type)
        {
        case GROUP:
        case ROLE:
            typeQNamePair = qnameDAO.getQName(ContentModel.TYPE_AUTHORITY_CONTAINER);
            break;
        case USER:
            typeQNamePair = qnameDAO.getQName(ContentModel.TYPE_PERSON);
            break;
        default:
            break;
        }
        return typeQNamePair != null ? typeQNamePair.getFirst() : null;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        super.afterPropertiesSet();

        PropertyCheck.mandatory(this, "tenantService", tenantService);
        PropertyCheck.mandatory(this, "nodeDAO", nodeDAO);
        PropertyCheck.mandatory(this, "qnameDAO", qnameDAO);
        PropertyCheck.mandatory(this, "cannedQueryDAO", cannedQueryDAO);
        PropertyCheck.mandatory(this, "methodSecurity", methodSecurity);
    }
}
