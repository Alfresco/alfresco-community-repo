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
public class DefaultCacheFactory<K extends Serializable, V> extends AbstractCacheFactory<K, V>
{
    private static final Log log = LogFactory.getLog(DefaultCacheFactory.class);
    
    
    @Override
    public SimpleCache<K, V> createCache(String cacheName)
    {
        return createLocalCache(cacheName);
    }
    
    private SimpleCache<K, V> createLocalCache(String cacheName)
    {
        int maxItems = maxItems(cacheName);
        DefaultSimpleCache<K, V> cache = new DefaultSimpleCache<K, V>(maxItems, cacheName);
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
}
