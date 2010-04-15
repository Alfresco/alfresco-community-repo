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
package org.alfresco.repo.usage;

import java.util.Set;

import org.alfresco.repo.domain.UsageDeltaDAO;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.usage.UsageService;

/**
 * The implementation of the UsageService for tracking usages.
 * 
 * @author Jan Vonka
 * @since 2.9
 */
public class UsageServiceImpl implements UsageService
{
    private UsageDeltaDAO usageDeltaDAO;

    public void setUsageDeltaDAO(UsageDeltaDAO usageDeltaDAO)
    {
        this.usageDeltaDAO = usageDeltaDAO;
    }

    public void insertDelta(NodeRef usageNodeRef, long deltaSize)
    {
        usageDeltaDAO.insertDelta(usageNodeRef, deltaSize);
    }

    public long getTotalDeltaSize(NodeRef usageNodeRef)
    {
        return usageDeltaDAO.getTotalDeltaSize(usageNodeRef);
    }
    
    public long getAndRemoveTotalDeltaSize(NodeRef usageNodeRef)
    {
        return usageDeltaDAO.getAndRemoveTotalDeltaSize(usageNodeRef);
    }
    
    public Set<NodeRef> getUsageDeltaNodes()
    {
        return usageDeltaDAO.getUsageDeltaNodes();
    }
    
    public int deleteDeltas(NodeRef usageNodeRef)
    {
        return usageDeltaDAO.deleteDeltas(usageNodeRef);
    }
}
