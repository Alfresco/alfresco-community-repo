/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
