
package org.alfresco.repo.publishing;

import org.alfresco.service.cmr.publishing.PublishingQueue;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Brian
 * @since 4.0
 */
public interface PublishingQueueFactory
{
    PublishingQueue createPublishingQueueObject(String siteId);

    PublishingQueue createPublishingQueueObject(NodeRef environmentNodeRef);
}
