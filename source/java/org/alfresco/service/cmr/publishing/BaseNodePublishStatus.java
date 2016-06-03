
package org.alfresco.service.cmr.publishing;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Brian
 * @author Nick Smith
 * @since 4.0
 */
public abstract class BaseNodePublishStatus implements NodePublishStatus
{
    private final NodeRef nodeRef;
    private final String channelId;
    
    public BaseNodePublishStatus(NodeRef nodeRef, String channelName)
    {
        this.nodeRef = nodeRef;
        this.channelId = channelName;
    }
    
    /**
    * {@inheritDoc}
     */
    public NodeRef getNodeRef()
    {
        return nodeRef;
    }

    /**
    * {@inheritDoc}
    */
    public String getChannelId()
    {
        return channelId;
    }
}
