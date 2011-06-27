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

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Parameter objects for {@link GetBlogPostsCannedQuery}.
 * 
 * @author Neil Mc Erlean
 * @since 4.0
 */
public class GetBlogPostsCannedQueryParams
{
    private final NodeRef blogContainerNode;
    private final String cmCreator;
    /**
     * <tt>true</tt> means the blog-posts should be cm:published, <tt>false</tt> means they should not.
     */
    private final boolean isPublished;
    private final Date publishedFromDate;
    private final Date publishedToDate;
    private final List<QName> requiredAspects;
    
    public GetBlogPostsCannedQueryParams(NodeRef blogContainerNodeRef,
                                         String cmCreator,
                                         boolean isPublished,
                                         Date publishedFromDate,
                                         Date publishedToDate,
                                         List<QName> requiredAspects)
    {
        this.blogContainerNode = blogContainerNodeRef;
        this.cmCreator = cmCreator;
        this.isPublished = isPublished;
        this.publishedFromDate = publishedFromDate;
        this.publishedToDate = publishedToDate;
        if (requiredAspects == null)
        {
            requiredAspects = Collections.emptyList();
        }
        this.requiredAspects = requiredAspects;
    }
    
    public NodeRef getBlogContainerNode()
    {
        return blogContainerNode;
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
    
    public List<QName> getRequiredAspects()
    {
        return Collections.unmodifiableList(this.requiredAspects);
    }
}
