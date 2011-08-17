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

import java.util.List;

import org.alfresco.repo.domain.node.NodeEntity;

/**
 * Parent class of Canned Query Entities which are a
 *  {@link NodeEntity} with additional properties
 *
 * @author Nick Burch
 * @since 4.0
 */
public class NodeWithTargetsEntity extends NodeBackedEntity
{
    private List<TargetAndTypeId> targets;
    
    // Supplemental query-related parameters
    private Long assocTypeId;
    
    /**
     * Default constructor
     */
    public NodeWithTargetsEntity()
    {
    }
    
    /**
     * Query constructor
     */
    public NodeWithTargetsEntity(Long parentNodeId, Long nameQNameId, Long contentTypeQNameId, Long assocTypeId)
    {
       super(parentNodeId, nameQNameId, contentTypeQNameId);
       this.assocTypeId = assocTypeId;
    }

    /**
     * @return Pairs of (Target Node, Assoc Type)
     */
    public List<TargetAndTypeId> getTargetIds() 
    {
       return targets;
    }

    public void setTargets(List<TargetAndTypeId> targets) 
    {
       this.targets = targets;
    }

    /**
     * If set, the ID of the assocation type to limit
     *  the target assocs to.
     */
    public Long getAssocTypeId() 
    {
       return assocTypeId;
    }
    
    public static class TargetAndTypeId
    {
       private final Long targetId;
       private final Long assocTypeId;
       
       public TargetAndTypeId(Long targetId, Long assocTypeId)
       {
          this.targetId = targetId;
          this.assocTypeId = assocTypeId;
       }

       public Long getTargetId() 
       {
          return targetId;
       }

       public Long getAssocTypeId() 
       {
          return assocTypeId;
       }
    }
}
