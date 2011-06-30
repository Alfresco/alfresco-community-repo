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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.alfresco.query.CannedQuery;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.query.CannedQuerySortDetails.SortOrder;
import org.alfresco.repo.blog.BlogService;
import org.alfresco.repo.blog.BlogService.BlogPostInfo;
import org.alfresco.repo.blog.cannedqueries.AbstractBlogPostsCannedQueryFactory.PropertyBasedComparator;
import org.alfresco.repo.domain.query.CannedQueryDAO;
import org.alfresco.repo.security.permissions.impl.acegi.AbstractCannedQueryPermissions;
import org.alfresco.repo.security.permissions.impl.acegi.MethodSecurityBean;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is a {@link CannedQuery} for the rather particular 'get my drafts and all published' blog-post query.
 * 
 * @author Neil Mc Erlean
 * @since 4.0
 * 
 * @see BlogService#getMyDraftsAndAllPublished(NodeRef, Date, Date, String, org.alfresco.query.PagingRequest)
 */
public class DraftsAndPublishedBlogPostsCannedQuery extends AbstractCannedQueryPermissions<BlogPostInfo>
{
    private Log logger = LogFactory.getLog(getClass());
    
    private static final String QUERY_NAMESPACE = "alfresco.blog";
    private static final String QUERY_SELECT_GET_BLOGS = "select_GetBlogsCannedQuery";
    
    private final CannedQueryDAO cannedQueryDAO;
    private final TaggingService taggingService;
    
    public DraftsAndPublishedBlogPostsCannedQuery(
            CannedQueryDAO cannedQueryDAO,
            TaggingService taggingService,
            MethodSecurityBean<BlogPostInfo> methodSecurity,
            CannedQueryParameters params)
    {
        super(params, methodSecurity);
        this.cannedQueryDAO = cannedQueryDAO;
        this.taggingService = taggingService;
    }
    
    @Override
    protected List<BlogPostInfo> queryAndFilter(CannedQueryParameters parameters)
    {
        Long start = (logger.isDebugEnabled() ? System.currentTimeMillis() : null);
        
        Object paramBeanObj = parameters.getParameterBean();
        if (paramBeanObj == null)
            throw new NullPointerException("Null GetBlogPosts query params");
        
        DraftsAndPublishedBlogPostsCannedQueryParams paramBean = (DraftsAndPublishedBlogPostsCannedQueryParams) paramBeanObj;
        String requestedCreator = paramBean.getCmCreator();
        String requestedTag = paramBean.getTag();
        Date createdFromDate = paramBean.getCreatedFromDate();
        Date createdToDate = paramBean.getCreatedToDate();
        
        // note: refer to SQL for specific DB filtering (eg.parent node and optionally blog integration aspect, etc)
        List<BlogEntity> results = cannedQueryDAO.executeQuery(QUERY_NAMESPACE, QUERY_SELECT_GET_BLOGS, paramBean, 0, Integer.MAX_VALUE);
        
        List<BlogEntity> filtered = new ArrayList<BlogEntity>(results.size());
        for (BlogEntity result : results)
        {
            // Is this next node in the list to be included in the results?
            boolean nextNodeIsAcceptable = true;
            
            Date actualPublishedDate = DefaultTypeConverter.INSTANCE.convert(Date.class, result.getPublishedDate());
            
            String actualCreator = result.getCreator();
            Date actualCreatedDate = DefaultTypeConverter.INSTANCE.convert(Date.class, result.getCreatedDate());
            
            // Return all published Blog Posts
            if (actualPublishedDate != null)
            {
                // Intentionally empty
            }
            else
            {
                // We're relying on cm:published being null below i.e. we are dealing with draft blog posts.
                if (requestedCreator != null)
                {
                    if (! requestedCreator.equals(actualCreator))
                    {
                        nextNodeIsAcceptable = false;
                    }
                }
                else
                {
                    nextNodeIsAcceptable = false;
                }
            }
            
            // Only return blogs created within the specified dates
            if ((createdFromDate != null) || (createdToDate != null))
            {
                if (actualCreatedDate != null)
                {
                    if (createdFromDate != null && actualCreatedDate.before(createdFromDate))
                    {
                        nextNodeIsAcceptable = false;
                    }
                    if (createdToDate != null && actualCreatedDate.after(createdToDate))
                    {
                        nextNodeIsAcceptable = false;
                    }
                }
            }
            
            // TODO review use-case and either remove or push-down
            // Only return blog posts tagged with the specified tag string.
            if (requestedTag != null && !taggingService.getTags(result.getNode().getNodeRef()).contains(requestedTag))
            {
                nextNodeIsAcceptable = false;
            }
            
            if (nextNodeIsAcceptable)
            {
                filtered.add(result);
            }
        }
        
        List<Pair<? extends Object, SortOrder>> sortPairs = parameters.getSortDetails().getSortPairs();
        // For now, the BlogService only sorts by a single property.
        if (sortPairs != null && !sortPairs.isEmpty())
        {
            Pair<? extends Object, SortOrder> sortPair = sortPairs.get(0);
            
            QName sortProperty = (QName) sortPair.getFirst();
            final PropertyBasedComparator comparator = new PropertyBasedComparator(sortProperty);
            
            if (sortPair.getSecond() == SortOrder.DESCENDING)
            {
                Collections.sort(filtered, Collections.reverseOrder(comparator));
            }
        }
        
        List<BlogPostInfo> blogPostInfos = new ArrayList<BlogPostInfo>(filtered.size());
        for (BlogEntity result : filtered)
        {
            blogPostInfos.add(new BlogPostInfo(result.getNodeRef(), result.getName()));
        }
        
        if (start != null)
        {
            logger.debug("Base query: "+blogPostInfos.size()+" in "+(System.currentTimeMillis()-start)+" msecs");
        }
        
        return blogPostInfos;
    }
    
    @Override
    protected boolean isApplyPostQuerySorting()
    {
        // No post-query sorting. It's done within the queryAndFilter() method above.
        return false;
    }
    
    @Override
    protected boolean isApplyPostQueryPermissions()
    {
        return true;
    }
}