
package org.alfresco.service.cmr.publishing;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Brian
 * @since 4.0
 */
public class NodePublishStatusPublishedAndOnQueue extends BaseNodePublishStatus
{

    private final PublishingEvent queuedPublishingEvent;
    private final PublishingEvent latestPublishingEvent;

    /**
     * @param nodeRef NodeRef
     * @param channelName TODO
     * @param queuedPublishingEvent The next scheduled {@link PublishingEvent} on the {@link PublishingQueue}
     * @param latestPublishingEvent The last {@link PublishingEvent} to successfully publish the node.
     */
    public NodePublishStatusPublishedAndOnQueue(NodeRef nodeRef, String channelName,
            PublishingEvent queuedPublishingEvent, PublishingEvent latestPublishingEvent)
    {
        super(nodeRef, channelName);
        this.queuedPublishingEvent = queuedPublishingEvent;
        this.latestPublishingEvent = latestPublishingEvent;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.publishing.NodePublishStatus#visit(org.alfresco.service.cmr.publishing.NodePublishStatusVisitor)
     */
    @Override
    public <T> T visit(NodePublishStatusVisitor<T> visitor)
    {
        return visitor.accept(this);
    }

    public PublishingEvent getQueuedPublishingEvent()
    {
        return queuedPublishingEvent;
    }

    /**
     * Retrieve the most recent publishing event that affected (created or updated) the node relevant to this status.
     * @return PublishingEvent
     */
    public PublishingEvent getLatestPublishingEvent()
    {
        return latestPublishingEvent;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.publishing.NodePublishStatus#getStatus()
     */
    @Override
    public Status getStatus()
    {
        return Status.PUBLISHED_AND_ON_QUEUE;
    }
}
