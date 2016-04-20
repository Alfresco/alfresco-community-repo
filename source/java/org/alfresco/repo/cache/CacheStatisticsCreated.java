package org.alfresco.repo.cache;

import org.springframework.context.ApplicationEvent;

/**
 * Signal that cache statistics have been registered for the given cache.
 * 
 * @since 5.0
 * @author Matt Ward
 */
public class CacheStatisticsCreated extends ApplicationEvent
{
    private static final long serialVersionUID = 1L;
    private final CacheStatistics cacheStats;
    private final String cacheName;
    
    public CacheStatisticsCreated(CacheStatistics cacheStats, String cacheName)
    {
        super(cacheStats);
        this.cacheStats = cacheStats;
        this.cacheName = cacheName;
    }

    public CacheStatistics getCacheStats()
    {
        return this.cacheStats;
    }

    public String getCacheName()
    {
        return this.cacheName;
    }
}
