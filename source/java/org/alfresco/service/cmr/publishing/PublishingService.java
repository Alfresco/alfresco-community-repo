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
