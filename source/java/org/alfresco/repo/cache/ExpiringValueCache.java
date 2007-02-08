/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.cache;

/**
 * Simple cache of a single Object value.
 * <p>
 * The object placed in the cache will automatically be discarded after a timeout value.
 * 
 * @author Kevin Roast
 */
public class ExpiringValueCache<T>
{
    // default is to discard cached object after 1 minute
    private final static long TIMEOUT_DEFAULT = 1000L*60L;
    
    private long timeout = TIMEOUT_DEFAULT;
    private long snapshot = 0;
    private T value;
    
    /**
     * Default constructor.
     * 
     * Uses the default timeout of 1 minute.
     */
    public ExpiringValueCache()
    {
    }
    
    /**
     * Constructor
     * 
     * @param timeout   Timeout in milliseconds before cached value is discarded
     */
    public ExpiringValueCache(long timeout)
    {
        this.timeout = timeout; 
    }
    
    /**
     * Put a value into the cache. The item will be return from the associated get() method
     * until the timeout expires then null will be returned.
     * 
     * @param value     The object to store in the cache
     */
    public void put(T value)
    {
        this.value = value;
        this.snapshot = System.currentTimeMillis();
    }
    
    /**
     * Get the cached object. The set item will be returned until it expires, then null will be returned.
     *  
     * @return cached object or null if not set or expired.
     */
    public T get()
    {
        if (snapshot + timeout < System.currentTimeMillis())
        {
            this.value = null;
        }
        return this.value;
    }
    
    /**
     * Clear the cache value
     */
    public void clear()
    {
        this.value = null;
    }
}
