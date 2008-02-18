/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.usage;

import java.util.HashSet;
import java.util.Set;

import org.alfresco.repo.domain.Node;
import org.alfresco.repo.node.db.NodeDaoService;
import org.alfresco.repo.usage.hibernate.UsageDeltaImpl;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.usage.UsageService;
import org.alfresco.util.ParameterCheck;

/**
 * The implementation of the UsageService for tracking usages.
 * 
 */
public class UsageServiceImpl implements UsageService
{
    private UsageDeltaDAO usageDeltaDao;
    private NodeDaoService nodeDaoService;

    public void setUsageDeltaDao(UsageDeltaDAO usageDeltaDao)
    {
        this.usageDeltaDao = usageDeltaDao;
    }

    public void setNodeDaoService(NodeDaoService nodeDaoService)
    {
        this.nodeDaoService = nodeDaoService;
    }
    
    
    public void insertDelta(NodeRef usageNodeRef, long deltaSize)
    {
        UsageDelta delta = new UsageDeltaImpl();
        
        // delta properties
        delta.setNode(getNodeNotNull(usageNodeRef));
        delta.setDeltaSize(deltaSize);
        
        usageDeltaDao.insertDelta(delta);
    }

    public long getTotalDeltaSize(NodeRef usageNodeRef)
    {
        return usageDeltaDao.getTotalDeltaSize(getNodeNotNull(usageNodeRef));
    }
    
    public Set<NodeRef> getUsageDeltaNodes()
    {
        Set<Node> nodes = usageDeltaDao.getUsageDeltaNodes();
        
        // convert nodes to nodeRefs (tenant-specific)
        Set<NodeRef> results = new HashSet<NodeRef>(nodes.size());
        for (Node node : nodes)
        {
            results.add(node.getNodeRef());
        }
        return results;
    }
    
    public int deleteDeltas(NodeRef usageNodeRef)
    {
        return usageDeltaDao.deleteDeltas(getNodeNotNull(usageNodeRef));
    }
    
    private Node getNodeNotNull(NodeRef nodeRef) throws InvalidNodeRefException
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        
        Node unchecked = nodeDaoService.getNode(nodeRef);
        if (unchecked == null)
        {
            throw new InvalidNodeRefException("Node does not exist: " + nodeRef, nodeRef);
        }
        return unchecked;
    }
}
