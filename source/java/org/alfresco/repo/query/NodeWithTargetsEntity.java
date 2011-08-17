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
    private List<Long> targetIds;
    private List<Long> targetAssocTypeIds;
    
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

    public List<Long> getTargetIds() 
    {
       return targetIds;
    }

    public void setTargetIds(List<Long> targetIds) 
    {
       this.targetIds = targetIds;
    }

    public List<Long> getTargetAssocTypeIds() 
    {
       return targetAssocTypeIds;
    }

    public void setTargetAssocTypeIds(List<Long> targetAssocTypeIds) 
    {
       this.targetAssocTypeIds = targetAssocTypeIds;
    }

    /**
     * If set, the ID of the assocation type to limit
     *  the target assocs to.
     */
    public Long getAssocTypeId() 
    {
       return assocTypeId;
    }
}
