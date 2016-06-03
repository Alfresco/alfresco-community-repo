
package org.alfresco.service.cmr.publishing;

import java.util.Collection;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Brian
 * @since 4.0
 */
public interface PublishingEventFilter
{
    PublishingEventFilter setIds(String... ids);

    PublishingEventFilter setIds(Collection<String> ids);

    Set<String> getIds();

    PublishingEventFilter setPublishedNodes(NodeRef... publishedNodes);
    
    PublishingEventFilter setPublishedNodes(Collection<NodeRef> publishedNodes);
    
    Set<NodeRef> getPublishedNodes();

    PublishingEventFilter setUnpublishedNodes(NodeRef... unpublishedNodes);
    
    PublishingEventFilter setUnpublishedNodes(Collection<NodeRef> unpublishedNodes);

    Set<NodeRef> getUnpublishedNodes();
}
