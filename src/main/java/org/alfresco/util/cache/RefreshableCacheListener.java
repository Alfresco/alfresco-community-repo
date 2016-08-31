package org.alfresco.util.cache;


public interface RefreshableCacheListener
{
     /**
     * Callback made when a cache refresh occurs
     * 
     * @param  refreshableCacheEvent             the cache event
     */
    public void onRefreshableCacheEvent(RefreshableCacheEvent refreshableCacheEvent);

    /**
     * Cache id so broadcast can be constrained to matching caches
     *  
     * @return              the cache ID
     */
    public String getCacheId();

}
