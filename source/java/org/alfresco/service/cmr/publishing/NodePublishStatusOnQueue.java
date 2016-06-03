
package org.alfresco.service.cmr.publishing;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Brian
 * @author Nick Smith
 * @since 4.0
 */
public class NodePublishStatusOnQueue extends BaseNodePublishStatus
{
    private final PublishingEvent queuedEvent;

    public NodePublishStatusOnQueue(NodeRef nodeRef, String channelName, PublishingEvent queuedEvent)
    {
        super(nodeRef, channelName);
        this.queuedEvent =queuedEvent;
    }

    /**
    * {@inheritDoc}
     */
    public <T> T visit(NodePublishStatusVisitor<T> visitor)
    {
        return visitor.accept(this);
    }

    public PublishingEvent getQueuedPublishingEvent()
    {
        return queuedEvent;
    }

    /**
     * {@inheritDoc}
      */
    public Status getStatus()
    {
        return Status.ON_QUEUE;
    }
}
