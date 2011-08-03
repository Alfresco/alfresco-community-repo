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

import org.alfresco.repo.query.NodeBackedEntity;

/**
 * Blog Entity - used by GetBlogs CQ
 *
 * @author janv
 * @since 4.0
 */
public class BlogEntity extends NodeBackedEntity
{
    private String publishedDate;
    private String postedDate;
    
    // Supplemental query-related parameters
    private Long publishedQNameId;
    
    private Long blogIntAspectQNameId;
    private Long blogIntPostedQNameId;
    
    /**
     * Default constructor
     */
    public BlogEntity()
    {
        super();
    }
    
    public BlogEntity(Long parentNodeId, Long nameQNameId, Long publishedQNameId, Long contentTypeQNameId, Long blogIntAspectQNameId, Long blogIntPostedQNameId)
    {
        super(parentNodeId, nameQNameId, contentTypeQNameId);
        this.publishedQNameId = publishedQNameId;
        
        this.blogIntAspectQNameId = blogIntAspectQNameId;
        this.blogIntPostedQNameId = blogIntPostedQNameId;
    }
    
    // (ISO-8061)
    public String getPublishedDate()
    {
        return publishedDate;
    }
    
    public void setPublishedDate(String published)
    {
        this.publishedDate = published;
    }
    
    // (ISO-8061)
    public String getPostedDate()
    {
        return postedDate;
    }
    
    public void setPostedDate(String postedDateISO8061)
    {
        this.postedDate = postedDateISO8061;
    }
    
    // Supplemental query-related parameters
    
    public Long getPublishedQNameId()
    {
        return publishedQNameId;
    }
    
    public Long getBlogIntAspectQNameId()
    {
        return blogIntAspectQNameId;
    }
    
    public Long getBlogIntPostedQNameId()
    {
        return blogIntPostedQNameId;
    }
}