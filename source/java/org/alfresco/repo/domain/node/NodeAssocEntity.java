/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.domain.node;

import java.util.List;

import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.namespace.QName;

/**
 * Bean for <b>alf_node_assoc</b> table.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class NodeAssocEntity
{
    private Long id;
    private Long version;
    private NodeEntity sourceNode;
    private NodeEntity targetNode;
    private Long typeQNameId;
    private int assocIndex;
    private List<Long> typeQNameIds;
    
    /**
     * Required default constructor
     */
    public NodeAssocEntity()
    {
    }
        
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("NodeAssocEntity")
          .append("[ ID=").append(id)
          .append(", sourceNode=").append(sourceNode)
          .append(", targetNode=").append(targetNode)
          .append(", typeQNameId=").append(typeQNameId)
          .append(", assocIndex=").append(assocIndex)
          .append(", typeQNameIds=").append(typeQNameIds)
          .append("]");
        return sb.toString();
    }
    
    /**
     * Helper method to fetch the association reference
     */
    public AssociationRef getAssociationRef(QNameDAO qnameDAO)
    {
        QName assocTypeQName = qnameDAO.getQName(typeQNameId).getSecond();
        AssociationRef assocRef = new AssociationRef(
                id,
                sourceNode.getNodeRef(),
                assocTypeQName,
                targetNode.getNodeRef());
        return assocRef;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getVersion()
    {
        return version;
    }

    public void setVersion(Long version)
    {
        this.version = version;
    }

    public NodeEntity getSourceNode()
    {
        return sourceNode;
    }

    public void setSourceNode(NodeEntity sourceNode)
    {
        this.sourceNode = sourceNode;
    }

    public NodeEntity getTargetNode()
    {
        return targetNode;
    }

    public void setTargetNode(NodeEntity targetNode)
    {
        this.targetNode = targetNode;
    }

    public Long getTypeQNameId()
    {
        return typeQNameId;
    }

    public void setTypeQNameId(Long typeQNameId)
    {
        this.typeQNameId = typeQNameId;
    }

    public int getAssocIndex()
    {
        return assocIndex;
    }

    public void setAssocIndex(int assocIndex)
    {
        this.assocIndex = assocIndex;
    }

    public List<Long> getTypeQNameIds()
    {
        return typeQNameIds;
    }

    public void setTypeQNameIds(List<Long> typeQNameIds)
    {
        this.typeQNameIds = typeQNameIds;
    }
}
