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

import org.alfresco.repo.cluster.ClusterChecker.NodeStatus;

/**
 * 
 * @Odin
 *
 */
public class ClusterNodePairStatusEvent extends ClusterEvent
{
    private static final long serialVersionUID = -4045195741687097066L;
    public static final String NOTIFICATION_TYPE = "Cluster Node Pair Status";

    private String sourceNodeId;
    private String targetNodeId;
    private NodeStatus status;

    public ClusterNodePairStatusEvent(ClusterChecker clusterChecker, String sourceNodeId, String targetNodeId, NodeStatus status)
    {
        super(clusterChecker);
        this.sourceNodeId = sourceNodeId;
        this.targetNodeId = targetNodeId;
        this.status = status;

    }

    public String getSourceNodeId()
    {
        return sourceNodeId;
    }
    
    public String getTargetNodeId()
    {
        return targetNodeId;
    }

    public NodeStatus getStatus()
    {
        return status;
    }
    
}