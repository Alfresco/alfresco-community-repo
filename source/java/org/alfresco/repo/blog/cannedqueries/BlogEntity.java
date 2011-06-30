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

import org.alfresco.repo.domain.node.NodeEntity;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Blog Entity - used by GetBlogs CQ
 *
 * @author janv
 * @since 4.0
 */
public class BlogEntity
{
    private Long id; // node id
    
    private NodeEntity node;
    
    private String name;
    
    private String publishedDate;
    private String postedDate;
    
    // Supplemental query-related parameters
    private Long parentNodeId;
    private Long nameQNameId;
    private Long publishedQNameId;
    private Long contentTypeQNameId;
    
    private Long blogIntAspectQNameId;
    private Long blogIntPostedQNameId;
    
    /**
     * Default constructor
     */
    public BlogEntity()
    {
    }
    
    public BlogEntity(Long parentNodeId, Long nameQNameId, Long publishedQNameId, Long contentTypeQNameId, Long blogIntAspectQNameId, Long blogIntPostedQNameId)
    {
        this.parentNodeId = parentNodeId;
        this.nameQNameId = nameQNameId;
        this.publishedQNameId = publishedQNameId;
        this.contentTypeQNameId = contentTypeQNameId;
        
        this.blogIntAspectQNameId = blogIntAspectQNameId;
        this.blogIntPostedQNameId = blogIntPostedQNameId;
    }
    
    public Long getId()
    {
        return id;
    }
    
    public void setId(Long id)
    {
        this.id = id;
    }
    
    // helper
    public NodeRef getNodeRef()
    {
        return (node != null ? node.getNodeRef() : null);
    }
    
    // helper (ISO 8061)
    public String getCreatedDate()
    {
        return ((node != null && node.getAuditableProperties() != null) ? node.getAuditableProperties().getAuditCreated() : null);
    }
    
    // helper
    public String getCreator()
    {
        return ((node != null && node.getAuditableProperties() != null) ? node.getAuditableProperties().getAuditCreator() : null);
    }
    
    public NodeEntity getNode()
    {
        return node;
    }
    
    public void setNode(NodeEntity childNode)
    {
        this.node = childNode;
    }
    
    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
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
    
    public Long getParentNodeId()
    {
        return parentNodeId;
    }
    
    public Long getNameQNameId()
    {
        return nameQNameId;
    }
    
    public Long getPublishedQNameId()
    {
        return publishedQNameId;
    }
    
    public Long getContentTypeQNameId()
    {
        return contentTypeQNameId;
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