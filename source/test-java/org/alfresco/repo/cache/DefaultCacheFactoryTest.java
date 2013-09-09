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

import static org.junit.Assert.assertEquals;

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
        properties.setProperty("cache.someCache.maxItems", "4");
        cacheFactory.setProperties(properties);
    }

    @Test
    public void canCreateCache()
    {
        cache = (DefaultSimpleCache<String, String>) cacheFactory.createCache("cache.someCache");
        assertEquals(4, cache.getMaxItems());
        assertEquals("cache.someCache", cache.getCacheName());
    }
}
