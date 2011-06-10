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
package org.alfresco.service.cmr.publishing;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * An interface that describes a publishing environment.
 * @author Brian
 *
 */
public interface Environment
{
    /**
     * Retrieve the identifier of this publishing environment
     * @return The identifier of this publishing environment
     */
    String getId();
    
    /**
     * Retrieve the publishing queue associated with this publishing environment
     * @return A PublishingQueue object corresponding tho this environment's publishing queue
     */
    PublishingQueue getPublishingQueue();
    
    /**
     * Discover the publishing status of each of the specified nodes
     * @param channelName TODO
     * @param nodes The identifiers of the nodes whose publishing status is being sought
     * @return A map associating a NodePublishStatus object with each of the supplied NodeRef objects
     */
    Map<NodeRef,NodePublishStatus> checkPublishStatus(String channelName, Collection<NodeRef> nodes);

    Map<NodeRef,NodePublishStatus> checkPublishStatus(String channelName, NodeRef... nodes);
    
    /**
     * Retrieve a list of publishing events associated with this publishing environment, filtering them using the
     * supplied PublishingEventFilter object (optional - may be <code>null</code>)
     * @param filter The filter that is to be applied to the publishing events on this environment
     * @return A list of PublishingEvent objects representing the publishing events that matched the supplied filter on this publishing environment
     */
    List<PublishingEvent> getPublishingEvents(PublishingEventFilter filter);
    
    /**
     * A factory method that creates a {@link PublishingEventFilter} object.
     * @return a new {@link PublishingEventFilter}.
     */
    PublishingEventFilter createPublishingEventFilter();
}
