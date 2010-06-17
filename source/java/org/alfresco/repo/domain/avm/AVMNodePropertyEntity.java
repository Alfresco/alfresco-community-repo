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
package org.alfresco.repo.domain.avm;

import org.alfresco.repo.domain.PropertyValue;

/**
 * Entity bean for <b>avm_node_properties</b> table.
 * <p>
 * 
 * @author janv
 * @since 3.2
 */
public class AVMNodePropertyEntity extends PropertyValue
{
    private static final long serialVersionUID = 1867663274306415601L;
    
    private Long nodeId;
    private Long qnameId;
    
    public AVMNodePropertyEntity()
    {
    }
    
    public AVMNodePropertyEntity(long nodeId, Long qnameId, PropertyValue value)
    {
        setNodeId(nodeId);
        setQnameId(qnameId);
        
        this.setActualType(value.getActualType());
        this.setBooleanValue(value.getBooleanValue());
        this.setDoubleValue(value.getDoubleValue());
        this.setFloatValue(value.getFloatValue());
        this.setLongValue(value.getLongValue());
        this.setMultiValued(value.isMultiValued());
        this.setPersistedType(value.getPersistedType());
        this.setSerializableValue(value.getSerializableValue());
        this.setStringValue(value.getStringValue());
    }
    
    public Long getNodeId()
    {
        return nodeId;
    }
    public void setNodeId(Long nodeId)
    {
        this.nodeId = nodeId;
    }
    public Long getQnameId()
    {
        return qnameId;
    }
    public void setQnameId(Long qnameId)
    {
        this.qnameId = qnameId;
    }
    
    @Override
    public int hashCode()
    {
        return super.hashCode();
    }
    
    @Override
    public boolean equals(Object obj)
    {
        return super.equals(obj);
    }
}
