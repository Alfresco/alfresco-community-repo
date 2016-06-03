package org.alfresco.repo.content.caching;

/**
 * Event fired when a CachingContentStore instance is created.
 * 
 * @author Matt Ward
 */
public class CachingContentStoreCreatedEvent extends CachingContentStoreEvent
{
    private static final long serialVersionUID = 1L;

    public CachingContentStoreCreatedEvent(CachingContentStore source)
    {
        super(source);
    }
    
    public CachingContentStore getCachingContentStore()
    {
        return (CachingContentStore) source;
    }
}
