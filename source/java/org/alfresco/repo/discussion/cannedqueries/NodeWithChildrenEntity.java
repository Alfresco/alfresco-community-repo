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
package org.alfresco.repo.discussion.cannedqueries;

import java.util.List;

import org.alfresco.repo.domain.node.NodeEntity;
import org.alfresco.repo.query.NodeBackedEntity;

/**
 * An extension of a {@link NodeEntity} which has the name
 *  of all children of it, used with the discussions 
 *  canned queries.
 * As well as the name comes some auditable information, but
 *  not full nodes as we don't do permissions checking on
 *  the children.
 *
 * @author Nick Burch
 * @since 4.0
 */
public class NodeWithChildrenEntity extends NodeBackedEntity
{
    private List<NameAndCreatedAt> children;
    
    // Supplemental query-related parameters
    private Long childrenTypeQNameId;
    
    /**
     * Default constructor
     */
    public NodeWithChildrenEntity()
    {
    }
    
    /**
     * Query constructor
     */
    public NodeWithChildrenEntity(Long parentNodeId, Long nameQNameId, Long contentTypeQNameId, Long childrenTypeQNameId)
    {
       super(parentNodeId, nameQNameId, contentTypeQNameId);
       this.childrenTypeQNameId = childrenTypeQNameId;
    }

    /**
     * @return Child Node name+created at 
     */
    public List<NameAndCreatedAt> getChildren() 
    {
       return children;
    }

    public void setChildren(List<NameAndCreatedAt> children) 
    {
       this.children = children;
    }

    /**
     * If set, the ID of the children's content type to limit
     *  the children too.
     */
    public Long getChildrenTypeQNameId() 
    {
       return childrenTypeQNameId;
    }
    
    public static class NameAndCreatedAt
    {
       private final Long nodeId;
       private final String name;
       private final String createdAt;
       
       public NameAndCreatedAt(Long nodeId, String name, String createdAt)
       {
          this.nodeId = nodeId;
          this.name = name;
          this.createdAt = createdAt;
       }

       public Long getNodeId() 
       {
          return nodeId;
       }
       
       public String getName()
       {
          return name;
       }
       
       public String getCreatedAt()
       {
          return createdAt;
       }
    }
}
