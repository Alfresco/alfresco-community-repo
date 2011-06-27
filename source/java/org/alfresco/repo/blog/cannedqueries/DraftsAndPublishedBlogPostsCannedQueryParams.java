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

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Parameters for {@link DraftsAndPublishedBlogPostsCannedQuery}.
 * 
 * @author Neil Mc Erlean
 * @since 4.0
 */
public class DraftsAndPublishedBlogPostsCannedQueryParams
{
    private final NodeRef blogContainerNode;
    private final String cmCreator;
    private final Date createdFromDate;
    private final Date createdToDate;
    private final String tag;
    
    public DraftsAndPublishedBlogPostsCannedQueryParams(NodeRef blogContainerNodeRef,
                                         String cmCreator,
                                         Date createdFromDate,
                                         Date createdToDate,
                                         String tag)
    {
        this.blogContainerNode = blogContainerNodeRef;
        this.cmCreator = cmCreator;
        this.createdFromDate = createdFromDate;
        this.createdToDate = createdToDate;
        this.tag = tag;
    }
    
    public NodeRef getBlogContainerNode()
    {
        return blogContainerNode;
    }
    
    public String getCmCreator()
    {
        return cmCreator;
    }
    
    public Date getCreatedFromDate()
    {
        return createdFromDate;
    }

    public Date getCreatedToDate()
    {
        return createdToDate;
    }

    public String getTag()
    {
        return tag;
    }
}
