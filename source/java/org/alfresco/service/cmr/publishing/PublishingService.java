/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

import java.util.List;

import org.alfresco.service.Auditable;
import org.alfresco.service.NotAuditable;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Brian
 * @since 4.0
 */
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
    @NotAuditable
    PublishingEvent getPublishingEvent(String id);

    /**
     * Retrieve a list of publishing events for which the specified <code>node</code> was published.
     * @param publishedNode The node that was published.
     * @return A list of {@link PublishingEvent}s.
     */
    @NotAuditable
    List<PublishingEvent> getPublishEventsForNode(NodeRef publishedNode);
    
    /**
     * Retrieve a list of publishing events for which the specified <code>node</code> was unpublished.
     * @param unpublishedNode The node that was unpublished.
     * @return A list of {@link PublishingEvent}s.
     */
    @NotAuditable
    List<PublishingEvent> getUnpublishEventsForNode(NodeRef unpublishedNode);

    /**
     * Request that the specified publishing event be cancelled. This call will
     * cancel the identified publishing event immediately if it hasn't been
     * started. If it has been started but not yet completed then the request
     * for cancellation will be recorded, and acted upon when (and if) possible.
     * 
     * @param id The identifier of the publishing event that is to be cancelled.
     */
    @Auditable(parameters={"id"})
    void cancelPublishingEvent(String id);
    
    /**
     * A factory method to create an empty publishing package that can be populated before being passed into
     * a call to the {@link PublishingQueue#scheduleNewEvent(PublishingDetails)} operation.
     * @return A publishing package that can be populated before being placed on the publishing queue.
     */
    @NotAuditable
    PublishingDetails createPublishingDetails();
    
    /**
     * Adds the supplied publishing package onto the queue.
     * @param publishingDetails The publishing package that is to be enqueued
     * @return The identifier of the newly scheduled event
     */
    @Auditable
    String scheduleNewEvent(PublishingDetails publishingDetails);
}
