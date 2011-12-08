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

/**
 * Key for caches that need to be bound implicitly to the current version of a node.
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public class NodeVersionKey implements Serializable
{
    private static final long serialVersionUID = 2241045540959490539L;
    
    private final Long nodeId;
    private final Long version;

    public NodeVersionKey(Long nodeId, Long version)
    {
        this.nodeId = nodeId;
        this.version = version;
    }

    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (!(other instanceof NodeVersionKey))
        {
            return false;
        }
        NodeVersionKey o = (NodeVersionKey)other;
        return nodeId.equals(o.nodeId) && version.equals(o.version);
    }
    
    @Override
    public int hashCode()
    {
        return nodeId.hashCode() + version.hashCode()*37;
    }
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("NodeVersionKey ")
               .append("[nodeId=").append(nodeId)
               .append(", version=").append(version)
               .append("]");
        return builder.toString();
    }

    public Long getNodeId()
    {
        return nodeId;
    }

    public Long getVersion()
    {
        return version;
    }
}
