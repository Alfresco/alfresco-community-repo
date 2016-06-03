
package org.alfresco.repo.publishing;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.publishing.PublishingDetails;
import org.alfresco.service.cmr.publishing.PublishingEvent;
import org.alfresco.service.cmr.publishing.PublishingEventFilter;
import org.alfresco.service.cmr.publishing.PublishingQueue;
import org.alfresco.service.cmr.publishing.StatusUpdate;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Brian
 * @author Nick Smith
 * @since 4.0
 */
public class PublishingQueueImpl implements PublishingQueue
{
    private final static String MSG_FAILED_TO_CREATE_PUBLISHING_EVENT = "publishing-create-event-failed";
    private final NodeRef nodeRef;
    private final PublishingEventHelper publishingEventHelper;
    
    public PublishingQueueImpl(NodeRef nodeRef, PublishingEventHelper publishingEventHelper)
    {
        this.nodeRef = nodeRef;
        this.publishingEventHelper = publishingEventHelper;
    }

    /**
     * {@inheritDoc}
    */
    public PublishingDetails createPublishingDetails()
    {
        return publishingEventHelper.createPublishingDetails();
    }

    public StatusUpdate createStatusUpdate(String message, NodeRef nodeToLinkTo, String... channelNames)
    {
        return createStatusUpdate(message, nodeToLinkTo, Arrays.asList(channelNames));
    }

    public StatusUpdate createStatusUpdate(String message, NodeRef nodeToLinkTo, Collection<String> channelNames)
    {
        return new StatusUpdateImpl(message, nodeToLinkTo, channelNames);
    }

    public List<PublishingEvent> getPublishingEvents(PublishingEventFilter filter)
    {
        return publishingEventHelper.findPublishingEvents(nodeRef, filter);
    }

    public PublishingEventFilter createPublishingEventFilter()
    {
        return new PublishingEventFilterImpl();
    }

    public String scheduleNewEvent(PublishingDetails publishingDetails)
    {
        try
        {
            NodeRef eventNode = publishingEventHelper.createNode(nodeRef, publishingDetails);
            publishingEventHelper.startPublishingWorkflow(eventNode, publishingDetails.getSchedule());
            return eventNode.toString();
        }
        catch (Exception ex)
        {
            throw new AlfrescoRuntimeException(MSG_FAILED_TO_CREATE_PUBLISHING_EVENT, ex);
        }
    }

    /**
     * @return the nodeRef
     */
    public NodeRef getNodeRef()
    {
        return nodeRef;
    }
}
