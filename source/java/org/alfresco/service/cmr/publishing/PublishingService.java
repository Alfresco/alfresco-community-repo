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
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;


public interface PublishingService
{

    /**
     * Retrieve the publishing event that has the specified identifier
     * 
     * @param id The identifier of the required publishing event
     * @return The PublishingEvent object that corresponds to the requested
     *         identifier or <code>null</code> if no such publishing event can
     *         be located
     */
    PublishingEvent getPublishingEvent(String id);

    /**
     * Request that the specified publishing event be cancelled. This call will
     * cancel the identified publishing event immediately if it hasn't been
     * started. If it has been started but not yet completed then the request
     * for cancellation will be recorded, and acted upon when (and if) possible.
     * 
     * @param id The identifier of the publishing event that is to be cancelled.
     */
    void cancelPublishingEvent(String id);
    
    /**
     * Retrieve the publishing queue associated with this publishing environment
     * @return A PublishingQueue object corresponding tho this environment's publishing queue
     */
    PublishingQueue getPublishingQueue(String siteId);
    
    /**
     * Discover the publishing status of each of the specified nodes
     * @param channelId TODO
     * @param nodes The identifiers of the nodes whose publishing status is being sought
     * @return A map associating a NodePublishStatus object with each of the supplied NodeRef objects
     */
    Map<NodeRef,NodePublishStatus> checkPublishStatus(String channelId, Collection<NodeRef> nodes);

    Map<NodeRef,NodePublishStatus> checkPublishStatus(String channelId, NodeRef... nodes);
}
