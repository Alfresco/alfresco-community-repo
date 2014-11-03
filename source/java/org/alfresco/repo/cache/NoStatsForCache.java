package org.alfresco.repo.cache;

/**
 * Read operations on {@link CacheStatistics} throw this
 * RuntimeException when no statistics exist for the
 * specified cache.
 * 
 * @since 5.0
 * @author Matt Ward
 */
public class NoStatsForCache extends RuntimeException
{
    private static final long serialVersionUID = 1L;
    private final String cacheName;
    
    public NoStatsForCache(String cacheName)
    {
        super("No statistics have been calculated for cache ["+cacheName+"]");
        this.cacheName = cacheName;
    }

    public String getCacheName()
    {
        return this.cacheName;
    }
}
