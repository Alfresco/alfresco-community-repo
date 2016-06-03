package org.alfresco.repo.cache;

/**
 * A registry of all AsynchronouslyRefreshedCaches to be used for notification.
 * 
 * @author Andy
 *
 */
public interface AsynchronouslyRefreshedCacheRegistry
{
    /**
     * Register a listener
     * @param listener RefreshableCacheListener
     */
    public void register(RefreshableCacheListener listener);
    
    /**
     * Fire an even 
     * @param event RefreshableCacheEvent
     * @param toAll - true goes to all listeners, false only to listeners that have a matching cacheId 
     */
    public void broadcastEvent(RefreshableCacheEvent event, boolean toAll);
}
