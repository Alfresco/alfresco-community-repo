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

import java.util.Date;

/**
 * Parameter objects for {@link GetBlogPostsCannedQuery}.
 * 
 * @author Neil Mc Erlean
 * @since 4.0
 */
public class GetBlogPostsCannedQueryParams extends BlogEntity
{
    private final String cmCreator;
    
    /**
     * <tt>true</tt> means the blog-posts should be cm:published, <tt>false</tt> means they should not.
     */
    private final boolean isPublished;
    
    private final Date publishedFromDate;
    private final Date publishedToDate;
    private final Long blogIntAspectQNameId;
    
    public GetBlogPostsCannedQueryParams(Long blogContainerNodeId,
                                         Long nameQNameId,
                                         Long publishedQNameId,
                                         Long contentTypeQNameId,
                                         String cmCreator,
                                         boolean isPublished,
                                         Date publishedFromDate,
                                         Date publishedToDate,
                                         Long blogIntAspectQNameId,
                                         Long blogIntPostedQNameId)
    {
        super(blogContainerNodeId, nameQNameId, publishedQNameId, contentTypeQNameId, blogIntAspectQNameId, blogIntPostedQNameId);
        
        this.cmCreator = cmCreator;
        this.isPublished = isPublished;
        this.publishedFromDate = publishedFromDate;
        this.publishedToDate = publishedToDate;
        this.blogIntAspectQNameId = blogIntAspectQNameId;
    }
    
    public String getCmCreator()
    {
        return cmCreator;
    }
    
    public boolean getIsPublished()
    {
        return this.isPublished;
    }
    
    public Date getPublishedFromDate()
    {
        return publishedFromDate;
    }

    public Date getPublishedToDate()
    {
        return publishedToDate;
    }
    
    public Long getBlogIntAspectQNameId()
    {
        return blogIntAspectQNameId;
    }
}
