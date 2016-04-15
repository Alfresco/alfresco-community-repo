package org.alfresco.repo.cache;

import java.io.Serializable;

/**
 * Cache factory interface. Implementing classes create {@link SimpleCache} objects
 * for a given cache name. It is the responsibility of the implementation to lookup
 * specific cache configuration details using the supplied name.
 *  
 * @author Matt Ward
 */
public interface CacheFactory<K extends Serializable, V>
{
    /**
     * Creates a cache. The type of cache (e.g. localised, clustered etc.) produced is
     * dependant on the factory implementation, and will vary at runtime.
     * 
     * @param cacheName String
     * @return SimpleCache
     */
    SimpleCache<K, V> createCache(String cacheName);
}
