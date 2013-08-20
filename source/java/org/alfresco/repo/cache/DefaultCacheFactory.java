/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.cache;

import java.io.Serializable;
import java.util.Properties;

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
public class DefaultCacheFactory<K extends Serializable, V> implements CacheFactory<K, V>
{
    private static final Log log = LogFactory.getLog(DefaultCacheFactory.class);
    private Properties properties;
    
    @Override
    public SimpleCache<K, V> createCache(String cacheName)
    {
        return createLocalCache(cacheName);
    }
    
    @Override
    public SimpleCache<K, V> createLocalCache(String cacheName)
    {
        DefaultSimpleCache<K, V> cache = new DefaultSimpleCache<K, V>();
        cache.setCacheName(cacheName);
        int maxItems = maxItems(cacheName);
        // maxItems of zero has no effect, DefaultSimpleCache will use its default capacity.
        if (maxItems > 0)
        {
            cache.setMaxItems(maxItems);
        }
        if (log.isDebugEnabled())
        {
            log.debug("Creating cache: " + cache);
        }
        return cache;
    }

    @Override
    public SimpleCache<K, V> createInvalidatingCache(String cacheName)
    {
        return createLocalCache(cacheName);
    }

    @Override
    public SimpleCache<K, V> createInvalidateRemovalCache(String cacheName)
    {
        return createLocalCache(cacheName);
    }

    private int maxItems(String cacheName)
    {
        String maxItemsStr = properties.getProperty(cacheName + ".maxItems");
        Integer maxItems = maxItemsStr != null ? Integer.parseInt(maxItemsStr) : 0; 
        return maxItems.intValue();
    }

    /**
     * Provide properties to parameterize cache creation. Cache properties are prefixed
     * with the cacheName supplied when invoking {@link DefaultCacheFactory#createCache(String)}.
     * For example, for a cache named cache.ticketsCache the property cache.ticketsCache.maxItems
     * will determine the capacity of the cache.
     * 
     * @param properties
     */
    public void setProperties(Properties properties)
    {
        this.properties = properties;
    }
}
