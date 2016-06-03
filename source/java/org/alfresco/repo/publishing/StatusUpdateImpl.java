
package org.alfresco.repo.publishing;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.service.cmr.publishing.StatusUpdate;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Nick Smith
 * @since 4.0
 */
public class StatusUpdateImpl implements StatusUpdate
{
    private final String message;
    private final NodeRef nodeToLinkTo;
    private final Set<String> channelNames;
    
    public StatusUpdateImpl(String message, NodeRef nodeToLinkTo, Collection<String> channelNames)
    {
        this.message = message;
        this.nodeToLinkTo = nodeToLinkTo;
        this.channelNames = Collections.unmodifiableSet(new HashSet<String>(channelNames));
    }

    /**
    * {@inheritDoc}
    */
    public String getMessage()
    {
        return message;
    }

    /**
    * {@inheritDoc}
    */
    public Set<String> getChannelIds()
    {
        return channelNames;
    }

    /**
    * {@inheritDoc}
    */
    public NodeRef getNodeToLinkTo()
    {
        return nodeToLinkTo;
    }
}
