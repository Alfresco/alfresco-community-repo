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
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQuery;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.query.CannedQuerySortDetails.SortOrder;
import org.alfresco.repo.blog.BlogPostInfo;
import org.alfresco.repo.blog.BlogService;
import org.alfresco.repo.security.permissions.impl.acegi.AbstractCannedQueryPermissions;
import org.alfresco.repo.security.permissions.impl.acegi.MethodSecurityInterceptor;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

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
    private final NodeService rawNodeService;
    private final TaggingService taggingService;
    
    public DraftsAndPublishedBlogPostsCannedQuery(
            NodeService rawNodeService,
            TaggingService taggingService,
            MethodSecurityInterceptor methodSecurityInterceptor,
            Method method,
            CannedQueryParameters params,
            String queryExecutionId)
    {
        super(params, queryExecutionId, methodSecurityInterceptor, method);
        this.rawNodeService = rawNodeService;
        this.taggingService = taggingService;
    }
    
    @Override
    protected List<BlogPostInfo> queryAndFilter(CannedQueryParameters parameters)
    {
        Object paramBeanObj = parameters.getParameterBean();
        if (paramBeanObj == null)
            throw new NullPointerException("Null GetBlogPosts query params");
        
        DraftsAndPublishedBlogPostsCannedQueryParams paramBean = (DraftsAndPublishedBlogPostsCannedQueryParams) paramBeanObj;
        
        String requestedTag = paramBean.getTag();
        Date createdFromDate = paramBean.getCreatedFromDate();
        Date createdToDate = paramBean.getCreatedToDate();
        
        List<ChildAssociationRef> childAssocs = getAllBlogNodes(paramBean.getBlogContainerNode());
        
        List<BlogPostInfo> filteredNodeRefs = new ArrayList<BlogPostInfo>();
        for (ChildAssociationRef chAssRef : childAssocs)
        {
            NodeRef nextBlogNode = chAssRef.getChildRef();
            
            // Is this next node in the list to be included in the results?
            boolean nextNodeIsAcceptable = true;
            
            // Return all published Blog Posts
            if (rawNodeService.getProperty(nextBlogNode, ContentModel.PROP_PUBLISHED) != null)
            {
                // Intentionally empty
            }
            else
            {
                // We're relying on cm:published being null below i.e. we are dealing with draft blog posts.
                if (!rawNodeService.getProperty(nextBlogNode, ContentModel.PROP_CREATOR).equals(paramBean.getCmCreator()))
                {
                    nextNodeIsAcceptable = false;
                }
            }
            
            // Only return blogs created within the specified dates
            Date actualCreatedDate = (Date) rawNodeService.getProperty(nextBlogNode, ContentModel.PROP_CREATED);
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
            
            // Only return blog posts tagged with the specified tag string.
            if (requestedTag != null && !taggingService.getTags(nextBlogNode).contains(requestedTag))
            {
                nextNodeIsAcceptable = false;
            }
            
            
            if (nextNodeIsAcceptable)
            {
                filteredNodeRefs.add(new BlogPostInfoImpl(nextBlogNode, rawNodeService.getProperties(nextBlogNode)));
            }
        }
        
        List<Pair<? extends Object, SortOrder>> sortPairs = parameters.getSortDetails().getSortPairs();
        // For now, the BlogService only sorts by a single property.
        if (sortPairs != null && !sortPairs.isEmpty())
        {
            Pair<? extends Object, SortOrder> sortPair = sortPairs.get(0);
            
            QName sortProperty = (QName) sortPair.getFirst();
            final PropertyBasedComparator createdDateComparator = new PropertyBasedComparator(sortProperty, rawNodeService);
                
            if (sortPair.getSecond() == SortOrder.DESCENDING)
            {
                Collections.sort(filteredNodeRefs, Collections.reverseOrder(createdDateComparator));
            }
        }
        
        
        return filteredNodeRefs;
    }
    
    private List<ChildAssociationRef> getAllBlogNodes(NodeRef containerNode)
    {
        final Set<QName> childNodeTypes = new HashSet<QName>();
        childNodeTypes.add(ContentModel.TYPE_CONTENT);
        
        // This will, of course, retrieve all the blog posts which may be a very long list.
        List<ChildAssociationRef> childAssocs = rawNodeService.getChildAssocs(containerNode, childNodeTypes);
        return childAssocs;
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