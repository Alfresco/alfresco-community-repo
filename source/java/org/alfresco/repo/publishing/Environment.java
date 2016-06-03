
package org.alfresco.repo.publishing;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Brian
 * @author Nick Smith
 * @since 4.0
 */
public class Environment
{
    private final NodeRef nodeRef;
    private final PublishingQueueImpl queue;
    private final NodeRef channelsContainer;
    
    public Environment(NodeRef nodeRef, PublishingQueueImpl queue, NodeRef channelsContainer)
    {
        this.nodeRef = nodeRef;
        this.queue = queue;
        this.channelsContainer = channelsContainer;
    }

    public PublishingQueueImpl getPublishingQueue()
    {
        return queue;
    }

    /**
     * @return the channelsContainer
     */
    public NodeRef getChannelsContainer()
    {
        return channelsContainer;
    }
    
    public NodeRef getNodeRef()
    {
        return nodeRef;
    } 
}
