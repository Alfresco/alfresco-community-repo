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
    
    @Test
    public void putAndCheckUpdate()
    {
        // Put an initial value
        cache.put(101, "101");
        // Update it
        assertEquals(true, cache.putAndCheckUpdate(101, "99101"));
        // Check the value really was updated
        assertEquals("99101", cache.get(101));
        
        // Precondition: no value for key 102
        assertFalse(cache.contains(102));
        // Put a value - and test the return
        assertEquals(false, cache.putAndCheckUpdate(102, "102"));
        assertEquals("102", cache.get(102));
        
        cache.put(103, null);
        assertEquals(true, cache.putAndCheckUpdate(103, "103"));
        // Repeat the put, this should not be an update
        assertEquals(false, cache.putAndCheckUpdate(103, "103"));
        
        assertFalse(cache.contains(104));
        assertEquals(false, cache.putAndCheckUpdate(104, null));
        // Repeat putting null - still not an update, as we had that value a moment ago.
        assertEquals(false, cache.putAndCheckUpdate(104, null));
        // Now an update
        assertEquals(true, cache.putAndCheckUpdate(104, "104"));
        // Another update
        assertEquals(true, cache.putAndCheckUpdate(104, "99104"));
        // Another update, back to null
        assertEquals(true, cache.putAndCheckUpdate(104, null));
        // Not an update - still null
        assertEquals(false, cache.putAndCheckUpdate(104, null));
        
        cache.remove(104);
        assertEquals(false, cache.putAndCheckUpdate(104, "104"));
        assertEquals(true, cache.putAndCheckUpdate(104, null));
    }
}
