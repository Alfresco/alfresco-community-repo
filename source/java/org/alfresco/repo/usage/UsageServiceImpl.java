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
package org.alfresco.repo.usage;

import java.util.Set;

import org.alfresco.repo.domain.usage.UsageDAO;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.usage.UsageService;

/**
 * The implementation of the UsageService for tracking usages.
 * 
 * @author janv
 * @since 2.9, 3.0
 */
public class UsageServiceImpl implements UsageService
{
    private UsageDAO usageDAO;
    
    public void setUsageDAO(UsageDAO usageDAO)
    {
        this.usageDAO = usageDAO;
    }
    
    public void insertDelta(NodeRef usageNodeRef, long deltaSize)
    {
        usageDAO.insertDelta(usageNodeRef, deltaSize);
    }
    
    public long getTotalDeltaSize(NodeRef usageNodeRef)
    {
        return usageDAO.getTotalDeltaSize(usageNodeRef, false);
    }
    
    public long getAndRemoveTotalDeltaSize(NodeRef usageNodeRef)
    {
        return usageDAO.getTotalDeltaSize(usageNodeRef, true);
    }

    public Set<NodeRef> getUsageDeltaNodes()
    {
        return usageDAO.getUsageDeltaNodes();
    }
    
    public int deleteDeltas(NodeRef usageNodeRef)
    {
        return usageDAO.deleteDeltas(usageNodeRef);
    }
}
