/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.domain.node;

import java.io.Serializable;

import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;

/**
 * @author janv
 * @since 4.0
 */
/* package */ class ChildByNameKey implements Serializable
{
    private static final long serialVersionUID = -2167221525380802365L;
    
    private final Long parentNodeId;
    private QName assocTypeQName;
    private String childNodeName;
    
    ChildByNameKey(Long parentNodeId, QName assocTypeQName, String childNodeName)
    {
    	ParameterCheck.mandatory("childNodeName", childNodeName);
    	ParameterCheck.mandatory("assocTypeQName", assocTypeQName);
    	ParameterCheck.mandatory("parentNodeId", parentNodeId);
    		
        this.parentNodeId = parentNodeId;
        this.assocTypeQName = assocTypeQName;
        this.childNodeName = childNodeName;
    }
    
    public Long getParentNodeId()
    {
        return parentNodeId;
    }
    
    public QName getAssocTypeQName()
    {
        return assocTypeQName;
    }
    
    public String getChildNodeName()
    {
        return childNodeName;
    }
    
    
    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (!(other instanceof ChildByNameKey))
        {
            return false;
        }
        ChildByNameKey o = (ChildByNameKey)other;
        return parentNodeId.equals(o.getParentNodeId()) &&
               assocTypeQName.equals(o.getAssocTypeQName()) &&
               childNodeName.equalsIgnoreCase(o.getChildNodeName());
    }
    
    @Override
    public int hashCode()
    {
        return parentNodeId.hashCode() + assocTypeQName.hashCode() + childNodeName.toLowerCase().hashCode();
    }
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("ChildByNameInfo ")
               .append("[parentNodeId=").append(parentNodeId)
               .append(", assocTypeQName=").append(assocTypeQName)
               .append(", childNodeName=").append(childNodeName)
               .append("]");
        return builder.toString();
    }
}
