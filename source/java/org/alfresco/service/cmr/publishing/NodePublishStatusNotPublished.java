
package org.alfresco.service.cmr.publishing;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Brian
 * @since 4.0
 */
public class NodePublishStatusNotPublished extends BaseNodePublishStatus
{
    /**
     * @param node NodeRef
     * @param channelName TODO
     */
    public NodePublishStatusNotPublished(NodeRef node, String channelName)
    {
        super(node, channelName);
    }

    /**
    * {@inheritDoc}
     */
    public <T> T visit(NodePublishStatusVisitor<T> visitor)
    {
        return visitor.accept(this);
    }

    /**
     * {@inheritDoc}
     */
    public Status getStatus()
    {
        return Status.NOT_PUBLISHED;
    }
}
