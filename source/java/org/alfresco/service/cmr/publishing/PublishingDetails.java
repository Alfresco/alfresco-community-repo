package org.alfresco.service.cmr.publishing;

import java.util.Calendar;
import java.util.Collection;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * A simple DTO used to gather parameters for scheduling a Publishing Event.
 * 
 * @author Brian
 * @author Nick Smith
 *
 * @since 4.0
 */
public interface PublishingDetails
{
    PublishingDetails addNodesToUnpublish(NodeRef... nodesToRemove);

    PublishingDetails addNodesToUnpublish(Collection<NodeRef> nodesToRemove);

    PublishingDetails addNodesToPublish(NodeRef... nodesToPublish);
    
    PublishingDetails addNodesToPublish(Collection<NodeRef> nodesToPublish);
    
    PublishingDetails setPublishChannelId(String channelId);
    
    PublishingDetails setComment(String comment);
    
    PublishingDetails setSchedule(Calendar schedule);
    
    PublishingDetails setStatusMessage(String message);
    
    PublishingDetails setStatusNodeToLinkTo(NodeRef nodeToLinkTo);
    
    PublishingDetails addStatusUpdateChannels(Collection<String> channelIds);
    PublishingDetails addStatusUpdateChannels(String... channelIds);
    
    /**
     * @return the comment
     */
     String getComment();
    
    /**
     * @return the message
     */
     String getStatusMessage();
    
    /**
     * @return the nodeToLinkTo
     */
    NodeRef getNodeToLinkTo();
    
    /**
     * @return the publishChannelId
     */
    String getPublishChannelId();
    
    /**
     * @return the schedule
     */
    Calendar getSchedule();

    Set<String> getStatusUpdateChannels();

    /**
     * @return a {@link Set} of all the {@link NodeRef}s to be published.
     */
    Set<NodeRef> getNodesToPublish();

    /**
     * @return a {@link Set} of all the {@link NodeRef}s to be unpublished.
     */
    Set<NodeRef> getNodesToUnpublish();
}
