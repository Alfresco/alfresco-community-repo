package org.alfresco.repo.cache;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * {@link CacheFactory} implementation that creates {@link DefaultSimpleCache} instances.
 * The caches are created with a capacity specified by the property {name}.maxItems.
 * For example, a cache named <tt>cache.ticketsCache</tt> would have a capacity specified
 * by the property <tt>cache.ticketsCache.maxItems</tt>
 * 
 * @author Matt Ward
 */
public class DefaultCacheFactory<K extends Serializable, V> extends AbstractCacheFactory<K, V>
{
    private static final Log log = LogFactory.getLog(DefaultCacheFactory.class);
    private static final String EVICT_NONE = "NONE";
    
    
    @Override
    public SimpleCache<K, V> createCache(String cacheName)
    {
        return createLocalCache(cacheName);
    }
    
    private SimpleCache<K, V> createLocalCache(String cacheName)
    {
        int maxItems = maxItems(cacheName);
        boolean useMaxItems = useMaxItems(cacheName);
        int ttlSecs = ttlSeconds(cacheName);
        int maxIdleSeconds = maxIdleSeconds(cacheName);
        DefaultSimpleCache<K, V> cache = new DefaultSimpleCache<K, V>(maxItems, useMaxItems, ttlSecs, maxIdleSeconds, cacheName);
        if (log.isDebugEnabled())
        {
            log.debug("Creating cache: " + cache);
        }
        return cache;
    }

    private int maxItems(String cacheName)
    {
        String maxItemsStr = getProperty(cacheName, "maxItems", "0");
        Integer maxItems = Integer.parseInt(maxItemsStr); 
        return maxItems.intValue();
    }
    
    private boolean useMaxItems(String cacheName)
    {
        String evictionPolicy = getProperty(cacheName, "eviction-policy", EVICT_NONE);
        return !evictionPolicy.equals(EVICT_NONE);
    }
    
    private int ttlSeconds(String cacheName)
    {
        String ttlSecsStr = getProperty(cacheName, "timeToLiveSeconds", "0");
        Integer ttlSecs = Integer.parseInt(ttlSecsStr);
        return ttlSecs;
    }
    
    private int maxIdleSeconds(String cacheName)
    {
        String maxIdleSecsStr = getProperty(cacheName, "maxIdleSeconds", "0");
        Integer maxIdleSecs = Integer.parseInt(maxIdleSecsStr);
        return maxIdleSecs;
    }
}
