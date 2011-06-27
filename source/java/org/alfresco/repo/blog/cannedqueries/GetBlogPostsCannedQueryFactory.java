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
package org.alfresco.repo.blog.cannedqueries;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.BlogIntegrationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.query.AbstractCannedQueryFactory;
import org.alfresco.query.CannedQuery;
import org.alfresco.query.CannedQueryFactory;
import org.alfresco.query.CannedQueryPageDetails;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.query.CannedQuerySortDetails;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.CannedQuerySortDetails.SortOrder;
import org.alfresco.repo.blog.BlogPostInfo;
import org.alfresco.repo.blog.BlogService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.impl.acegi.MethodSecurityInterceptor;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyCheck;

/**
 * A {@link CannedQueryFactory} for various queries relating to {@link BlogPostInfo blog-posts}.
 * Currently, this is implemented using calls to lower-level services, notably the {@link NodeService} rather
 * than database queries. This may change in the future.
 * 
 * @author Neil Mc Erlean.
 * @since 4.0
 * 
 * @see BlogService#getDrafts(NodeRef, String, PagingRequest)
 * @see BlogService#getPublished(NodeRef, Date, Date, String, PagingRequest)
 */
public class GetBlogPostsCannedQueryFactory extends AbstractCannedQueryFactory<BlogPostInfo>
{
    private MethodSecurityInterceptor methodSecurityInterceptor;
    private String methodName;
    private Object methodService;
    private NodeService rawNodeService;
    
    public void setRawNodeService(NodeService nodeService)
    {
        this.rawNodeService = nodeService;
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
    public CannedQuery<BlogPostInfo> getCannedQuery(CannedQueryParameters parameters)
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
        
        String queryExecutionId = (parameters.getQueryExecutionId() == null ? super.getQueryExecutionId(parameters) : parameters.getQueryExecutionId());
        
        final GetBlogPostsCannedQuery cq = new GetBlogPostsCannedQuery(rawNodeService, methodSecurityInterceptor, method, parameters, queryExecutionId);
        return (CannedQuery<BlogPostInfo>) cq;
    }
    
    public CannedQuery<BlogPostInfo> getGetDraftsCannedQuery(NodeRef blogContainerNode, String username, PagingRequest pagingReq)
    {
        ParameterCheck.mandatory("blogContainerNode", blogContainerNode);
        ParameterCheck.mandatory("pagingReq", pagingReq);
        
        int requestTotalCountMax = pagingReq.getRequestTotalCountMax();
        
        //FIXME Need tenant service like for GetChildren?
        boolean isPublished = false;
        List<QName> requiredAspects = null;
        GetBlogPostsCannedQueryParams paramBean = new GetBlogPostsCannedQueryParams(blogContainerNode,
                                                                                    username,
                                                                                    isPublished,
                                                                                    null, null,
                                                                                    requiredAspects);
        
        CannedQueryPageDetails cqpd = createCQPageDetails(pagingReq);
        CannedQuerySortDetails cqsd = createCQSortDetails(ContentModel.PROP_CREATED, SortOrder.DESCENDING);
        
        // create query params holder
        CannedQueryParameters params = new CannedQueryParameters(paramBean, cqpd, cqsd, AuthenticationUtil.getRunAsUser(), requestTotalCountMax, pagingReq.getQueryExecutionId());
        
        // return canned query instance
        return getCannedQuery(params);
    }
    
    public CannedQuery<BlogPostInfo> getGetPublishedExternallyCannedQuery(NodeRef blogContainerNode, PagingRequest pagingReq)
    {
        ParameterCheck.mandatory("blogContainerNode", blogContainerNode);
        ParameterCheck.mandatory("pagingReq", pagingReq);
        
        int requestTotalCountMax = pagingReq.getRequestTotalCountMax();
        
        boolean isPublished = true;
        List<QName> requiredAspects = Arrays.asList(new QName[]{BlogIntegrationModel.ASPECT_BLOG_POST});
        GetBlogPostsCannedQueryParams paramBean = new GetBlogPostsCannedQueryParams(blogContainerNode,
                                                                                    null,
                                                                                    isPublished,
                                                                                    null, null,
                                                                                    requiredAspects);
        
        CannedQueryPageDetails cqpd = createCQPageDetails(pagingReq);
        CannedQuerySortDetails cqsd = createCQSortDetails(BlogIntegrationModel.PROP_POSTED, SortOrder.DESCENDING);
        
        // create query params holder
        CannedQueryParameters params = new CannedQueryParameters(paramBean, cqpd, cqsd, AuthenticationUtil.getRunAsUser(), requestTotalCountMax, pagingReq.getQueryExecutionId());
        
        // return canned query instance
        return getCannedQuery(params);
    }
    
    public CannedQuery<BlogPostInfo> getGetPublishedCannedQuery(NodeRef blogContainerNode, Date fromDate, Date toDate, String byUser, PagingRequest pagingReq)
    {
        ParameterCheck.mandatory("blogContainerNode", blogContainerNode);
        ParameterCheck.mandatory("pagingReq", pagingReq);
        
        int requestTotalCountMax = pagingReq.getRequestTotalCountMax();
        
        boolean isPublished = true;
        List<QName> requiredAspects = null;
        GetBlogPostsCannedQueryParams paramBean = new GetBlogPostsCannedQueryParams(blogContainerNode,
                                                                                    byUser,
                                                                                    isPublished,
                                                                                    fromDate, toDate,
                                                                                    requiredAspects);
        
        CannedQueryPageDetails cqpd = createCQPageDetails(pagingReq);
        CannedQuerySortDetails cqsd = createCQSortDetails(ContentModel.PROP_PUBLISHED, SortOrder.DESCENDING);
        
        // create query params holder
        CannedQueryParameters params = new CannedQueryParameters(paramBean, cqpd, cqsd, AuthenticationUtil.getRunAsUser(), requestTotalCountMax, pagingReq.getQueryExecutionId());
        
        // return canned query instance
        return getCannedQuery(params);
    }
    
    private CannedQuerySortDetails createCQSortDetails(QName sortProp, SortOrder sortOrder)
    {
        CannedQuerySortDetails cqsd = null;
        List<Pair<? extends Object, SortOrder>> sortPairs = new ArrayList<Pair<? extends Object, SortOrder>>();
        sortPairs.add(new Pair<QName, SortOrder>(sortProp, sortOrder));
        cqsd = new CannedQuerySortDetails(sortPairs);
        return cqsd;
    }
    
    private CannedQueryPageDetails createCQPageDetails(PagingRequest pagingReq)
    {
        int skipCount = pagingReq.getSkipCount();
        if (skipCount == -1)
        {
            skipCount = CannedQueryPageDetails.DEFAULT_SKIP_RESULTS;
        }
        
        int maxItems = pagingReq.getMaxItems();
        if (maxItems == -1)
        {
            maxItems  = CannedQueryPageDetails.DEFAULT_PAGE_SIZE;
        }
        
        // page details
        CannedQueryPageDetails cqpd = new CannedQueryPageDetails(skipCount, maxItems, CannedQueryPageDetails.DEFAULT_PAGE_NUMBER, CannedQueryPageDetails.DEFAULT_PAGE_COUNT);
        return cqpd;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception
    {
        super.afterPropertiesSet();
        
        PropertyCheck.mandatory(this, "methodSecurityInterceptor", methodSecurityInterceptor);
        PropertyCheck.mandatory(this, "methodService", methodService);
        PropertyCheck.mandatory(this, "methodName", methodName);
    }
}
