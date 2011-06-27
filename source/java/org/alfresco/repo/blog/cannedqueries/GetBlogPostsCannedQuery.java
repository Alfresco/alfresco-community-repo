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
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * This class provides support for several {@link CannedQuery canned queries} used by the
 * {@link BlogService}.
 * 
 * @author Neil Mc Erlean
 * @since 4.0
 */
public class GetBlogPostsCannedQuery extends AbstractCannedQueryPermissions<BlogPostInfo>
{
    /*
     * This must be the small n nodeService, not the big N NodeService. See below.
     */
    private final NodeService rawNodeService;
    
    public GetBlogPostsCannedQuery(
            NodeService rawNodeService,
            MethodSecurityInterceptor methodSecurityInterceptor,
            Method method,
            CannedQueryParameters params,
            String queryExecutionId)
    {
        super(params, queryExecutionId, methodSecurityInterceptor, method);
        this.rawNodeService = rawNodeService;
    }
    
    @Override
    protected List<BlogPostInfo> queryAndFilter(CannedQueryParameters parameters)
    {
        Object paramBeanObj = parameters.getParameterBean();
        if (paramBeanObj == null)
            throw new NullPointerException("Null GetBlogPosts query params");
        
        GetBlogPostsCannedQueryParams paramBean = (GetBlogPostsCannedQueryParams) paramBeanObj;
        String requestedCreator = paramBean.getCmCreator();
        boolean isPublished = paramBean.getIsPublished();
        Date publishedFromDate = paramBean.getPublishedFromDate();
        Date publishedToDate = paramBean.getPublishedToDate();
        List<QName> requiredAspects = paramBean.getRequiredAspects();
        
        // Retrieve all blog-post nodes under the blogContainer root. This could potentially
        // be a long list of NodeRefs and it is possible that future optimisation towards DB queries
        // would avoid the retrieval of potentially long lists like this.
        // It is however important to retrieve the full list of relevant nodes before any sorting
        // is applied. Otherwise it would be possible to have nodes that were not retrieved, which after sorting
        // could be at the front of this list.
        // For that reason, we must use the small n nodeService, and not the large N NodeService, because the
        // latter truncates results.
        List<ChildAssociationRef> childAssocs = getAllBlogNodes(paramBean.getBlogContainerNode());
        
        List<BlogPostInfo> filteredNodeRefs = new ArrayList<BlogPostInfo>();
        for (ChildAssociationRef chAssRef : childAssocs)
        {
            // Is the nextBlogPostNode going to be included or not?
            boolean nextNodeIsAcceptable = true;
            
            NodeRef nextBlogNode = chAssRef.getChildRef();
            
            // Only return blog-posts whose cm:published status matches that requested.
            final boolean nextBlogNodeIsPublished = rawNodeService.getProperty(nextBlogNode, ContentModel.PROP_PUBLISHED) != null;
            if (nextBlogNodeIsPublished != isPublished)
            {
                nextNodeIsAcceptable = false;
            }
            
            // Only return blog posts whose creator matches the given username, if there is one.
            if (requestedCreator != null && !rawNodeService.getProperty(nextBlogNode, ContentModel.PROP_CREATOR).equals(requestedCreator))
            {
                nextNodeIsAcceptable = false;
            }
            
            // Only return blogs published within the specified dates
            Date actualPublishedDate = (Date) rawNodeService.getProperty(nextBlogNode, ContentModel.PROP_PUBLISHED);
            if (actualPublishedDate != null)
            {
                if (publishedFromDate != null && actualPublishedDate.before(publishedFromDate))
                {
                    nextNodeIsAcceptable = false;
                }
                if (publishedToDate != null && actualPublishedDate.after(publishedToDate))
                {
                    nextNodeIsAcceptable = false;
                }
            }
            
            // Only those with the required aspects.
            for (QName aspect : requiredAspects)
            {
                if (!rawNodeService.hasAspect(nextBlogNode, aspect))
                {
                    nextNodeIsAcceptable = false;
                }
            }
            
            // If all the above conditions are true...
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