
package org.alfresco.service.cmr.publishing;

/**
 * @author Brian
 * @since 4.0
 */
public interface PublishingQueue
{
    /**
     * A factory method to create an empty publishing package that can be populated before being passed into
     * a call to the {@link PublishingQueue#scheduleNewEvent(PublishingDetails)} operation.
     * @return A publishing package that can be populated before being placed on the publishing queue.
     */
    PublishingDetails createPublishingDetails();
    
    /**
     * Adds the supplied publishing package onto the queue.
     * @param publishingDetails The publishing package that is to be enqueued
     * @return The identifier of the newly scheduled event
     */
    String scheduleNewEvent(PublishingDetails publishingDetails);
}
