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

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Brian
 * @author Nick Smith
 *
 */
public class Environment
{
    private final NodeRef nodeRef;
    private final PublishingQueueImpl queue;
    private final NodeRef channelsContainer;
    
    public Environment(NodeRef nodeRef, PublishingQueueImpl queue, NodeRef channelsContainer)
    {
        this.nodeRef = nodeRef;
        this.queue = queue;
        this.channelsContainer = channelsContainer;
    }

    /**
     * {@inheritDoc}
     */
    public PublishingQueueImpl getPublishingQueue()
    {
        return queue;
    }

    /**
     * @return the channelsContainer
     */
    public NodeRef getChannelsContainer()
    {
        return channelsContainer;
    }
    
     public NodeRef getNodeRef()
     {
         return nodeRef;
     }
     
}
