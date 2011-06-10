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

package org.alfresco.repo.publishing;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.publishing.Environment;
import org.alfresco.service.cmr.publishing.NodePublishStatus;
import org.alfresco.service.cmr.publishing.PublishingEvent;
import org.alfresco.service.cmr.publishing.PublishingEventFilter;
import org.alfresco.service.cmr.publishing.PublishingQueue;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Brian
 * @author Nick Smith
 *
 */
public class EnvironmentImpl implements Environment
{
    private PublishingQueueFactory publishingQueueFactory;
    private NodeRef nodeRef;
    private String id;
    private EnvironmentHelper environmentHelper;
    private PublishingEventHelper publishingEventHelper;
    
    /**
     * {@inheritDoc}
     */
    public Map<NodeRef, NodePublishStatus> checkPublishStatus(String channelName, Collection<NodeRef> nodes)
    {
        
        Map<NodeRef, NodePublishStatus> results = new HashMap<NodeRef, NodePublishStatus>();
        for (NodeRef node : nodes)
        {
            if(node!=null && results.containsKey(node)==false)
            {
                results.put(node, environmentHelper.checkNodeStatus(node, this, channelName));
            }
        }
        return results;
    }
    
    /**
     * {@inheritDoc}
     */
    public Map<NodeRef, NodePublishStatus> checkPublishStatus(String channelName, NodeRef... nodes)
    {
        return checkPublishStatus(channelName, Arrays.asList(nodes));
    }

    /**
     * {@inheritDoc}
     */
    public String getId()
    {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    public List<PublishingEvent> getPublishingEvents(PublishingEventFilter filter)
    {
        NodeRef queue = environmentHelper.getPublishingQueue(nodeRef);
        return publishingEventHelper.findPublishingEvents(queue, filter);
    }

    /**
     * {@inheritDoc}
     */
    public PublishingQueue getPublishingQueue()
    {
        return publishingQueueFactory.createPublishingQueueObject(nodeRef);
    }

    /**
     * {@inheritDoc}
     */
     public PublishingEventFilter createPublishingEventFilter()
     {
         return new PublishingEventFilterImpl();
     }

     public NodeRef getNodeRef()
     {
         return nodeRef;
     }
     
    /**
     * @param node
     */
    public void setNodeRef(NodeRef node)
    {
        this.nodeRef = node;
        this.id = node.toString();
    }

    /**
     * @param publishingObjectFactory
     */
    public void setPublishingQueueFactory(PublishingQueueFactory publishingQueueFactory)
    {
        this.publishingQueueFactory = publishingQueueFactory;
    }

    /**
     * @param environmentHelper
     */
    public void setEnvironmentHelper(EnvironmentHelper environmentHelper)
    {
        this.environmentHelper = environmentHelper;
    }
    
    /**
     * @param publishingEventHelper the publishingEventHelper to set
     */
    public void setPublishingEventHelper(PublishingEventHelper publishingEventHelper)
    {
        this.publishingEventHelper = publishingEventHelper;
    }

}
