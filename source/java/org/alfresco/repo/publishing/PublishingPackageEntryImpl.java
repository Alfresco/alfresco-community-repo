
package org.alfresco.repo.publishing;

import org.alfresco.service.cmr.publishing.NodeSnapshot;
import org.alfresco.service.cmr.publishing.PublishingPackageEntry;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Brian
 * @author Nick Smith
 * @since 4.0
 */
// Package protected
class PublishingPackageEntryImpl implements PublishingPackageEntry
{
    private final boolean publish; 
    private final NodeRef nodeRef;
    private final NodeSnapshot snapshot;
    
    public PublishingPackageEntryImpl(boolean publish, NodeRef nodeRef, NodeSnapshot snapshot)
    {
        this.publish = publish;
        this.nodeRef = nodeRef;
        this.snapshot= snapshot;
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
    public boolean isPublish()
    {
        return publish;
    }

    /**
     * {@inheritDoc}
      */
    public NodeSnapshot getSnapshot()
    {
        return snapshot;
    }
}
