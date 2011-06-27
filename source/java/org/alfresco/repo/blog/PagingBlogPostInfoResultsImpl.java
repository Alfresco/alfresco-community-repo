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
package org.alfresco.repo.blog;

import java.util.List;

import org.alfresco.query.PermissionedResults;
import org.alfresco.util.Pair;

/**
 * A simple results holder object for data relating to {@link BlogPostInfo blog-posts}, as
 * returned by the {@link BlogService}
 * 
 * @since 4.0
 * @author Neil Mc Erlean.
 */
class PagingBlogPostInfoResultsImpl implements PagingBlogPostInfoResults, PermissionedResults
{
    private List<BlogPostInfo> blogPosts;
    
    private boolean hasMoreItems;
    private Pair<Integer, Integer> totalResultCount;
    private String queryExecutionId;
    private boolean permissionsApplied;
    
    public PagingBlogPostInfoResultsImpl(List<BlogPostInfo> nodeInfos, boolean hasMoreItems, Pair<Integer, Integer> totalResultCount, String queryExecutionId, boolean permissionsApplied)
    {
        this.blogPosts = nodeInfos;
        this.hasMoreItems = hasMoreItems;
        this.totalResultCount = totalResultCount;
        this.queryExecutionId = queryExecutionId;
        this.permissionsApplied = permissionsApplied;
    }
    
    @Override
    public List<BlogPostInfo> getPage()
    {
        return blogPosts;
    }
    
    @Override
    public boolean hasMoreItems()
    {
        return hasMoreItems;
    }
    
    @Override
    public Pair<Integer, Integer> getTotalResultCount()
    {
        return totalResultCount;
    }
    
    @Override
    public String getQueryExecutionId()
    {
        return queryExecutionId;
    }
    
    @Override
    public boolean permissionsApplied()
    {
        return permissionsApplied;
    }
}
