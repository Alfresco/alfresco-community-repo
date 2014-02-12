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

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the {@link DefaultCacheFactory} class.
 * 
 * @author Matt Ward
 */
public class DefaultCacheFactoryTest
{
    private DefaultCacheFactory<String, String> cacheFactory;
    private Properties properties;
    private DefaultSimpleCache<String, String> cache;
    
    @Before
    public void setUp() throws Exception
    {
        cacheFactory = new DefaultCacheFactory<String, String>();
        properties = new Properties();
        // cache.someCache
        properties.setProperty("cache.someCache.maxItems", "4");
        properties.setProperty("cache.someCache.eviction-policy", "EVICT"); // Anything but NONE
        // cache.noSizeLimit
        properties.setProperty("cache.noSizeLimit.maxItems", "2"); // No effect
        properties.setProperty("cache.noSizeLimit.eviction-policy", "NONE");
        // cache.withTTL
        properties.setProperty("cache.withTTL.maxItems", "0");
        properties.setProperty("cache.withTTL.eviction-policy", "NONE");
        properties.setProperty("cache.withTTL.timeToLiveSeconds", "6");
        // cache.withMaxIdle
        properties.setProperty("cache.withMaxIdle.maxItems", "0");
        properties.setProperty("cache.withMaxIdle.eviction-policy", "NONE");
        properties.setProperty("cache.withMaxIdle.maxIdleSeconds", "7");
        
        cacheFactory.setProperties(properties);
    }

    @Test
    public void canCreateCache()
    {
        cache = (DefaultSimpleCache<String, String>) cacheFactory.createCache("cache.someCache");
        assertEquals(4, cache.getMaxItems());
        assertEquals("cache.someCache", cache.getCacheName());
        assertTrue(cache.isUseMaxItems());
    }
    
    @Test
    public void canCreateUnboundedCache()
    {
        cache = (DefaultSimpleCache<String, String>) cacheFactory.createCache("cache.noSizeLimit");
        assertEquals(2, cache.getMaxItems());
        assertEquals("cache.noSizeLimit", cache.getCacheName());
        assertFalse(cache.isUseMaxItems());        
    }
    
    @Test
    public void canCreateCacheWithTTL()
    {
        cache = (DefaultSimpleCache<String, String>) cacheFactory.createCache("cache.withTTL");
        assertEquals("cache.withTTL", cache.getCacheName());
        assertEquals(6, cache.getTTLSecs());        
    }
    
    @Test
    public void canCreateCacheWithMaxIdle()
    {
        cache = (DefaultSimpleCache<String, String>) cacheFactory.createCache("cache.withMaxIdle");
        assertEquals("cache.withMaxIdle", cache.getCacheName());
        assertEquals(0, cache.getTTLSecs());        
        assertEquals(7, cache.getMaxIdleSecs());        
    }
}
