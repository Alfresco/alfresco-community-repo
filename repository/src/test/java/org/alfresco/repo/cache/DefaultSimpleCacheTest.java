/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.cache;

import static org.junit.Assert.*;

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
        return new DefaultSimpleCache<Integer, String>(100, true, 0, 0, getClass().getName());
    }
    
    @Test
    public void boundedSizeCache() throws Exception
    {
        // We'll only keep the LAST 3 items
        cache = new DefaultSimpleCache<Integer, String>(3, true, 0, 0, getClass().getName());
        
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
        
        assertTrue(cache.isUseMaxItems());
    }
    
    @Test
    public void defaultMaxItems()
    {
        // maxItems of 0 results in a capacity of Integer.MAX_VALUE - this is to match Hazelcast cache behaviour.
        cache = new DefaultSimpleCache<Integer, String>(0, true, 0, 0, getClass().getName());
        assertEquals(Integer.MAX_VALUE, cache.getMaxItems());
        assertTrue(cache.isUseMaxItems());
    }
    
    @Test
    public void sizeLimitConstructor()
    {
        cache = new DefaultSimpleCache<Integer, String>(123, getClass().getName());
        assertEquals(123, cache.getMaxItems());
        assertTrue(cache.isUseMaxItems());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void noNegativeMaxItems()
    {
        cache = new DefaultSimpleCache<Integer, String>(-1, true, 0, 0, getClass().getName());
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

    @Test
    public void putAndCheckUpdateIncludeNewCheck()
    {
        // Put an initial value
        cache.put(101, "101");
        // Update it
        assertEquals(true, cache.putAndCheckUpdate(101, "99101", true));
        // Check the value really was updated
        assertEquals("99101", cache.get(101));

        // Precondition: no value for key 102
        assertFalse(cache.contains(102));
        // Put a value - and test the return
        assertEquals(true, cache.putAndCheckUpdate(102, "102", true));
        assertEquals("102", cache.get(102));

        cache.put(103, null);
        assertEquals(true, cache.putAndCheckUpdate(103, "103", true));
        // Repeat the put, this should not be an update
        assertEquals(false, cache.putAndCheckUpdate(103, "103", true));

        assertFalse(cache.contains(104));
        assertEquals(true, cache.putAndCheckUpdate(104, null, true));
        // Repeat putting null - still not an update, as we had that value a moment ago.
        assertEquals(false, cache.putAndCheckUpdate(104, null, true));
        // Now an update
        assertEquals(true, cache.putAndCheckUpdate(104, "104", true));
        // Another update
        assertEquals(true, cache.putAndCheckUpdate(104, "99104", true));
        // Another update, back to null
        assertEquals(true, cache.putAndCheckUpdate(104, null, true));
        // Not an update - still null
        assertEquals(false, cache.putAndCheckUpdate(104, null, true));

        cache.remove(104);
        assertEquals(true, cache.putAndCheckUpdate(104, "104", true));
        assertEquals(true, cache.putAndCheckUpdate(104, null, true));
    }
    
    // TODO: Timer-based tests are not ideal. An alternative approach is to factor out the CacheBuilder.newBuilder()
    // call to a protected method, override that in this test class to return a mock and use the mock to check
    // that the Cache is being configured correctly, e.g. assert that expireAfterWrite(int, TimeUnit) is called.
    @Test
    public void cachesCanHaveTTL()
    {
        // TTL of 2 seconds
        cache = new DefaultSimpleCache<Integer, String>(0, false, 2, 0, getClass().getName());
        assertFalse(cache.isUseMaxItems());

        cache.put(1, "1");
        assertTrue(cache.contains(1));
        assertFalse(cache.contains(2));
        assertFalse(cache.contains(3));

        sleep(1500L);
        cache.put(2, "2");
        assertTrue(cache.contains(1));
        assertTrue(cache.contains(2));
        assertFalse(cache.contains(3));

        sleep(1500L);
        cache.put(3, "3");
        assertFalse(cache.contains(1));
        assertTrue(cache.contains(2));
        assertTrue(cache.contains(3));
    }
     
    @Test
    public void cachesCanHaveTTI()
    {
        cache = new DefaultSimpleCache<Integer, String>(0, false, 0, 2, getClass().getName());
        assertFalse(cache.isUseMaxItems());
        assertEquals(0, cache.getTTLSecs());
        assertEquals(2, cache.getMaxIdleSecs());

        cache.put(1, "1");
        assertEquals("1", cache.get(1));

        sleep(1000L);
        // cause zeroing of idle time
        assertEquals("1", cache.get(1));

        sleep(1000L);
        // cause zeroing of idle time
        assertEquals("1", cache.get(1));

        sleep(1000L);
        // At least 3 seconds have passed, but the item should still be present. 
        assertEquals("1", cache.get(1));

        sleep(2500L);
        // time-to-idle now exceeded without access
        assertNotEquals("1", cache.get(1));
    }

    private void sleep(long miliseconds)
    {
        try
        {
            Thread.sleep(miliseconds);
        }
        catch (InterruptedException error)
        {
            throw new RuntimeException(error);
        }
    }

}
