package org.alfresco.repo.cache;

/**
 * Basic cache API
 * 
 * @author Andy
 *
 */
public interface RefreshableCache <T> 
{
    /**
     * Get the cache.
     * If there is no cache value this call will block.
     * If the underlying cache is being refreshed, the old cache value will be returned until the refresh is complete.
     * 
     * @return T
     */
    public T get();
    
    /**
     * Refresh the cache asynchronously.
     */
    public void refresh();

//    /**
//     * Register to be informed when the cache is updated in the background.
//     * 
//     * Note: it is up to the implementation to provide any transactional wrapping.
//     * Transactional wrapping is not required to invalidate a shared cache entry directly via a transactional cache 
//     * @param listener
//     */
//    void register(RefreshableCacheListener listener);
}
