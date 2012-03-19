/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.cluster;

/**
 * 
 * @since Odin
 *
 */
public class ClusterMessageEvent extends ClusterEvent
{
    private static final long serialVersionUID = -8677530378696271077L;

    private String sourceId;
    private String targetId;
    
    public ClusterMessageEvent(ClusterChecker clusterChecker, String sourceId, String targetId)
    {
        super(clusterChecker);
        this.sourceId = sourceId;
        this.targetId = targetId;
    }

    public String getSourceId()
    {
        return sourceId;
    }

    public String getTargetId()
    {
        return targetId;
    }
    
}
