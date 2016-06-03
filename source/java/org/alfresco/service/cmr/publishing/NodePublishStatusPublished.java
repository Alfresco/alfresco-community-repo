
package org.alfresco.service.cmr.publishing;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Brian
 * @since 4.0
 */
public class NodePublishStatusPublished extends BaseNodePublishStatus
{

    private final PublishingEvent lastEvent;

    public NodePublishStatusPublished(NodeRef node, String channelId, PublishingEvent lastEvent)
    {
        super(node, channelId);
        this.lastEvent = lastEvent;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.publishing.NodePublishStatus#visit(org.alfresco.service.cmr.publishing.NodePublishStatusVisitor)
     */
    @Override
    public <T> T visit(NodePublishStatusVisitor<T> visitor)
    {
        return visitor.accept(this);
    }
    
    /**
     * Retrieve the most recent publishing event that affected (created or updated) the node relevant to this status.
     * @return PublishingEvent
     */
    public PublishingEvent getLatestPublishingEvent()
    {
        return lastEvent;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.publishing.NodePublishStatus#getStatus()
     */
    @Override
    public Status getStatus()
    {
        return Status.PUBLISHED;
    }
}
