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
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the DefaultSimpleCache class.
 * 
 * @author Matt Ward
 */
public class DefaultSimpleCacheTest
{
    private DefaultSimpleCache<Integer, String> cache;
    
    @Before
    public void setUp()
    {
        cache = new DefaultSimpleCache<Integer, String>();
    }
    
    @Test
    public void unboundedSizeCache()
    {
        cache.put(1, "1");
        cache.put(2, "2");
        cache.put(3, "3");
        cache.put(4, "4");
        cache.put(5, "5");
     
        assertEquals("1", cache.get(1));
        assertEquals("2", cache.get(2));
        assertEquals("3", cache.get(3));
        assertEquals("4", cache.get(4));
        assertEquals("5", cache.get(5));
    }
    
    @Test
    public void boundedSizeCache()
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
    public void canStoreNullValues()
    {
        cache.put(2, null);
        assertEquals(null, cache.get(2));
        // Check that the key has an entry against it.
        assertTrue(cache.contains(2));
        
        // Ensure that a key that has not been assigned is discernable
        // from a key that has been assigned a null value.
        assertEquals(null, cache.get(4));
        assertFalse(cache.contains(4));
    }
    
    @Test
    public void canRemoveItems()
    {
        cache.put(1, "hello");
        cache.put(2, "world");
        assertEquals("hello", cache.get(1));
        assertEquals("world", cache.get(2));
        
        cache.remove(2);
        assertEquals("hello", cache.get(1));
        assertEquals(null, cache.get(2));        
        assertEquals(false, cache.contains(2));        
    }
    
    @Test
    public void canClearItems()
    {
        cache.put(1, "hello");
        cache.put(2, "world");
        assertEquals("hello", cache.get(1));
        assertEquals("world", cache.get(2));
        
        cache.clear();

        assertEquals(null, cache.get(1));        
        assertEquals(false, cache.contains(1));
        assertEquals(null, cache.get(2));        
        assertEquals(false, cache.contains(2));
    }
    
    @Test
    public void canGetKeys()
    {
        cache.put(3, "blue");
        cache.put(12, "red");
        cache.put(43, "olive");
        
        Iterator<Integer> it = cache.getKeys().iterator();
        assertEquals(3, it.next().intValue());
        assertEquals(12, it.next().intValue());
        assertEquals(43, it.next().intValue());
        assertFalse("There should be no more keys.", it.hasNext());
    }
    
    @Test
    public void clearUponSetMaxItems()
    {
        cache.put(1, "1");
        assertTrue(cache.contains(1));
        
        cache.setMaxItems(10);
        
        // The item should have gone.
        assertFalse(cache.contains(1));
    }
}
