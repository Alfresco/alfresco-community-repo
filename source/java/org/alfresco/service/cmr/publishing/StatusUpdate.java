
package org.alfresco.service.cmr.publishing;

import java.util.Set;

import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Nick Smith
 * @since 4.0
 */
public interface StatusUpdate
{
    /**
     * @return the status message to be published.
     */
    String getMessage();
    
    /**
     * @return a {@link Set} of String identifiers indicating which {@link Channel} the status update will be published to.
     */
    Set<String> getChannelIds();

    /**
     * Returns a {@link NodeRef}. The returned {@link NodeRef} is one of the {@link NodeRef}s to be published by the associated {@link PublishingEvent}. The status update message will have a URL appended to it which links to the published resource represented by this {@link NodeRef}.
     * @return the {@link NodeRef} to link to.
     */
    NodeRef getNodeToLinkTo();
}
