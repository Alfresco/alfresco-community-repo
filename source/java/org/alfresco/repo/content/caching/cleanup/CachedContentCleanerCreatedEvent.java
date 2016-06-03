package org.alfresco.repo.content.caching.cleanup;

import org.alfresco.repo.content.caching.CachingContentStoreEvent;

/**
 * Event fired when CachedContentCleaner instances are created.
 * 
 * @author Matt Ward
 */
public class CachedContentCleanerCreatedEvent extends CachingContentStoreEvent
{
    private static final long serialVersionUID = 1L;
    
    /**
     * @param cleaner - cleaner
     */
    public CachedContentCleanerCreatedEvent(CachedContentCleaner cleaner)
    {
        super(cleaner);
    }

    public CachedContentCleaner getCleaner()
    {
        return (CachedContentCleaner) source;
    }
}
