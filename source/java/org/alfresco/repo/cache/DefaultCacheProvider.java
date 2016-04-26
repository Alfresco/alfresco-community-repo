package org.alfresco.repo.cache;

import java.io.Serializable;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.CacheProvider;
import org.hibernate.cache.Timestamper;

/**
 * A simple {@link CacheProvider} that allows Hibernate to create {@link DefaultSimpleCache}
 * based caches.
 *
 * @author Matt Ward
 */
public class DefaultCacheProvider implements CacheProvider
{
    private final static Log log = LogFactory.getLog(DefaultCacheProvider.class);
    // TODO: setup in spring (SystemPropertiesSetterBean)
    private int defaultMaxItems = 500;
    
    @Override
    public Cache buildCache(String regionName, Properties properties) throws CacheException
    {
        if (log.isDebugEnabled())
        {
            log.debug("building cache for regionName=" + regionName + ", with properties: " + properties);
        }
        DefaultSimpleCache<Serializable, Object> cache = new DefaultSimpleCache<Serializable, Object>(defaultMaxItems, null);
        Cache hibCache = new HibernateSimpleCacheAdapter(cache, regionName);
        return hibCache;
    }

    @Override
    public long nextTimestamp()
    {
        return Timestamper.next();
    }

    @Override
    public void start(Properties properties) throws CacheException
    {
        log.debug("Starting cache provider");
    }

    @Override
    public void stop()
    {
        log.debug("Stopping cache provider");
    }

    @Override
    public boolean isMinimalPutsEnabledByDefault()
    {
        return false;
    }

}
