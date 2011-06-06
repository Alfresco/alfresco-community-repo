/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.domain.node;

import java.io.Serializable;

import org.alfresco.service.namespace.QName;

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
