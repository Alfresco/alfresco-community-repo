/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.domain;

import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * The interface to persist usage delta information.
 * 
 */
public interface UsageDeltaDAO
{
    /**
     * Create a usage delta entry.
     * 
     * @param deltaSize     the size change
     */
    public void insertDelta(NodeRef usageNodeRef, long deltaSize);
    
    /**
     * Get the total delta size for a node.
     * 
     * @param nodeRef       the node reference
     * @return              sum of delta sizes (in bytes) - can be +ve or -ve
     */
    public long getTotalDeltaSize(NodeRef usageNodeRef);
    
    public Set<NodeRef> getUsageDeltaNodes();
    
    public int deleteDeltas(NodeRef nodeRef);
    
    public int deleteDeltas(Long nodeId);
}
