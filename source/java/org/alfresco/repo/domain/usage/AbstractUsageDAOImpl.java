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
package org.alfresco.repo.domain.usage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.Pair;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.extensions.surf.util.ParameterCheck;


/**
 * Abstract implementation for Usage DAO.
 * <p>
 * This provides basic services such as caching, but defers to the underlying implementation
 * for CRUD operations for:
 * 
 *     <b>alf_usage_delta</b>
 * 
 * @author janv
 * @since 3.4
 */
public abstract class AbstractUsageDAOImpl implements UsageDAO
{
    private NodeDAO nodeDAO;
    
    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }
    
    private long getNodeIdNotNull(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        
        Pair<Long, NodeRef> nodePair = nodeDAO.getNodePair(nodeRef);
        if (nodePair == null)
        {
            throw new InvalidNodeRefException("Node does not exist: " + nodeRef, nodeRef);
        }
        return nodePair.getFirst();
    }
    
    private NodeRef getNodeRefNotNull(long nodeId)
    {
        Pair<Long, NodeRef> nodePair = nodeDAO.getNodePair(nodeId);
        if (nodePair == null)
        {
            throw new AlfrescoRuntimeException("Node does not exist: " + nodeId);
        }
        return nodePair.getSecond();
    }
    
    public int deleteDeltas(NodeRef nodeRef)
    {
        long nodeId = getNodeIdNotNull(nodeRef);
        return deleteDeltas(nodeId);
    }
    
    public int deleteDeltas(long nodeId)
    {
        return deleteUsageDeltaEntitiesByNodeId(nodeId);
    }
    
    public long getTotalDeltaSize(NodeRef nodeRef, boolean removeDeltas)
    {
        long nodeId = getNodeIdNotNull(nodeRef);
        UsageDeltaEntity entity = selectTotalUsageDeltaSize(nodeId);
        Long totalSize = entity.getDeltaSize();
        // Remove the deltas, making sure that the correct number are removed
        if (removeDeltas)
        {
            int deleted = deleteUsageDeltaEntitiesByNodeId(nodeId);
            if (entity.getDeltaCount() != null && entity.getDeltaCount().intValue() != deleted)
            {
                throw new ConcurrencyFailureException(
                        "The number of usage deltas was " + entity.getDeltaCount() + " but only " + deleted + " were deleted.");
            }
        }
        return (totalSize != null ? totalSize : 0L);
    }
    
    public void insertDelta(NodeRef usageNodeRef, long deltaSize)
    {
        long nodeId = getNodeIdNotNull(usageNodeRef);
        UsageDeltaEntity entity = new UsageDeltaEntity(nodeId, deltaSize);
        
        insertUsageDeltaEntity(entity);
    }
    
    public Set<NodeRef> getUsageDeltaNodes()
    {
        // TODO move into nodeDAO to directly return set of nodeRefs
        List<Long> nodeIds = selectUsageDeltaNodes();
        Set<NodeRef> nodeRefs = new HashSet<NodeRef>(nodeIds.size());
        for (Long nodeId : nodeIds)
        {
            nodeRefs.add(getNodeRefNotNull(nodeId));
        }
        return nodeRefs;
    }
    
    public void getUserContentSizesForStore(StoreRef storeRef, MapHandler resultsCallback)
    {
        selectUserContentSizesForStore(storeRef, resultsCallback);
    }
    
    public void getUsersWithoutUsage(StoreRef storeRef, MapHandler handler)
    {
        selectUsersWithoutUsage(storeRef, handler);
    }
    
    public void getUsersWithUsage(StoreRef storeRef, MapHandler handler)
    {
        selectUsersWithUsage(storeRef, handler);
    }
    
    protected abstract UsageDeltaEntity insertUsageDeltaEntity(UsageDeltaEntity entity);
    protected abstract UsageDeltaEntity selectTotalUsageDeltaSize(long nodeEntityId);
    protected abstract List<Long> selectUsageDeltaNodes();
    protected abstract void selectUsersWithoutUsage(StoreRef storeRef, MapHandler handler);
    protected abstract void selectUsersWithUsage(StoreRef storeRef, MapHandler handler);
    protected abstract void selectUserContentSizesForStore(StoreRef storeRef, MapHandler resultsCallback);
    protected abstract int deleteUsageDeltaEntitiesByNodeId(long nodeEntityId);
}
