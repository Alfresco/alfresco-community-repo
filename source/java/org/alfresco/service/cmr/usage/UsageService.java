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
package org.alfresco.service.cmr.usage;

import java.util.Set;

import org.alfresco.service.NotAuditable;
import org.alfresco.service.PublicService;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * The public API by which applications can create usage delta entries.
 * 
 * @author Jan Vonka
 * @since 2.9
 */
@PublicService
public interface UsageService
{
    /**
     * Add a usage delta entry.
     */
    @NotAuditable
    public void insertDelta(NodeRef usageNodeRef, long deltaSize);
    
    /**
     * Get sum of usage delta sizes.
     */
    @NotAuditable
    public long getTotalDeltaSize(NodeRef usageNodeRef);
    
    /**
     * Get distinct set of usage delta nodes
     */
    @NotAuditable
    public Set<NodeRef> getUsageDeltaNodes();
    
    /**
     * Delete the usage delta nodes
     */
    @NotAuditable
    public int deleteDeltas(NodeRef usageNodeRef);
}
