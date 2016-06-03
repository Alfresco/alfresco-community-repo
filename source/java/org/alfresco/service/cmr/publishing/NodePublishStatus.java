
package org.alfresco.service.cmr.publishing;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Brian
 * @author Nick Smith
 * @since 4.0
 */
public interface NodePublishStatus
{
    enum Status {NOT_PUBLISHED, ON_QUEUE, PUBLISHED, PUBLISHED_AND_ON_QUEUE}
    
    <T> T visit(NodePublishStatusVisitor<T> visitor);
    
    NodeRef getNodeRef();
    
    Status getStatus();
    
    String getChannelId();
}
