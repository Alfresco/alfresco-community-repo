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
package org.alfresco.service.cmr.transfer;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Definition of a transfer.
 * 
 * Specifies which node to transfer
 *
 */
public class TransferDefinition implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = -8497919749300106861L;
    
    // Which nodes to deploy
    private Set<NodeRef> nodes;

    /**
     * Set which nodes to transfer
     * @param nodes
     */
    public void setNodes(Collection<NodeRef> nodes)
    {
        this.nodes = new HashSet<NodeRef>(nodes);
    }
    
    public void setNodes(NodeRef...nodes)
    {
        this.setNodes(new HashSet<NodeRef>(Arrays.asList(nodes)));
    }

    /**
     * Get which nodes to transfer
     * @return
     */
    public Set<NodeRef> getNodes()
    {
        return nodes;
    }
}
