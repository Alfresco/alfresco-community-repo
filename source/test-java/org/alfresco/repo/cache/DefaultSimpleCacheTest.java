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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Tests for the DefaultSimpleCache class.
 * 
 * @author Matt Ward
 */
public class DefaultSimpleCacheTest extends SimpleCacheTestBase<DefaultSimpleCache<Integer, String>>
{    
    @Override
    protected DefaultSimpleCache<Integer, String> createCache()
    {
        return new DefaultSimpleCache<Integer, String>(100, getClass().getName());
    }
    
    @Test
    public void boundedSizeCache() throws Exception
    {
        // We'll only keep the LAST 3 items
        cache.setMaxItems(3);
        
        cache.put(1, "1");
        cache.put(2, "2");
        cache.put(3, "3");
        cache.put(4, "4");
        cache.put(5, "5");
     
        // Lost the first item
        assertNull(cache.get(1));
        assertFalse(cache.contains(1));
        
        // Lost the second item
        assertNull(cache.get(2));        
        assertFalse(cache.contains(2));
        
        // Last three are still present
        assertEquals("3", cache.get(3));
        assertEquals("4", cache.get(4));
        assertEquals("5", cache.get(5));
    }
}
