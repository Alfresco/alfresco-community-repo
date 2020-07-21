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
