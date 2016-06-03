
package org.alfresco.repo.publishing;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.service.cmr.publishing.PublishingEventFilter;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Nick Smith
 * @since 4.0
 */
public class PublishingEventFilterImpl implements PublishingEventFilter
{
    private Set<String> ids = Collections.emptySet();
    private Set<NodeRef> publishedNodes = Collections.emptySet();
    private Set<NodeRef> unpublishedNodes = Collections.emptySet();
    
    /**
    * {@inheritDoc}
    */
    public PublishingEventFilter setIds(String... ids)
    {
        if (ids != null && ids.length > 0)
        {
            this.ids = new HashSet<String>(Arrays.asList(ids));
        }
        return this;
    }
    
    /**
    * {@inheritDoc}
    */
    @Override
    public PublishingEventFilter setIds(Collection<String> ids)
    {
        if (ids != null && ids.isEmpty() == false)
        {
            this.ids = new HashSet<String>(ids);
        }
        return this;
    }
    /**
    * {@inheritDoc}
    */
    public Set<String> getIds()
    {
        return Collections.unmodifiableSet(ids);
    }

    /**
    * {@inheritDoc}
    */
    public PublishingEventFilter setPublishedNodes(NodeRef... publishedNodes)
    {
        if (publishedNodes != null && publishedNodes.length > 0)
        {
            this.publishedNodes = new HashSet<NodeRef>(Arrays.asList(publishedNodes));
        }
        return this;
    }
    
    /**
    * {@inheritDoc}
    */
    public PublishingEventFilter setPublishedNodes(Collection<NodeRef> publishedNodes)
    {
        if (publishedNodes != null && publishedNodes.isEmpty() == false)
        {
            this.publishedNodes = new HashSet<NodeRef>(publishedNodes);
        }
        return this;
    }
    
    /**
    * {@inheritDoc}
    */
    public Set<NodeRef> getPublishedNodes()
    {
        return Collections.unmodifiableSet(publishedNodes);
    }
    
    /**
     * {@inheritDoc}
     */
    public PublishingEventFilter setUnpublishedNodes(NodeRef... unpublishedNodes)
    {
        if (unpublishedNodes != null && unpublishedNodes.length > 0)
        {
            this.unpublishedNodes = new HashSet<NodeRef>(Arrays.asList(unpublishedNodes));
        }
        return this;
    }
    
    /**
     * {@inheritDoc}
     */
    public PublishingEventFilter setUnpublishedNodes(Collection<NodeRef> unpublishedNodes)
    {
        if (unpublishedNodes != null && unpublishedNodes.isEmpty() == false)
        {
            this.unpublishedNodes = new HashSet<NodeRef>(unpublishedNodes);
        }
        return this;
    }
    
    /**
     * {@inheritDoc}
     */
    public Set<NodeRef> getUnpublishedNodes()
    {
        return Collections.unmodifiableSet(unpublishedNodes);
    }
}
