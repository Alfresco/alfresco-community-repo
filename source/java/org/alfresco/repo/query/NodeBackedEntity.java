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
package org.alfresco.repo.query;

import org.alfresco.repo.domain.node.NodeEntity;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Parent class of Canned Query Entities which are a
 *  {@link NodeEntity} with additional properties
 *
 * @author Nick Burch
 * @since 4.0
 */
public abstract class NodeBackedEntity
{
    private Long id; // node id
    
    private NodeEntity node;
    
    private String name;
    
    /**
     * Default constructor
     */
    public NodeBackedEntity()
    {
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
}
