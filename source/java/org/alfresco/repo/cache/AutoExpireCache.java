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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A cache implementation that maintains items up to a threshold size. If the threshold size is reached
 * it begins removing old items during get() calls as they time-out after a specified timeout value.
 * <p>
 * If the threshold value is not reached, the items are not removed unless specifically requested with
 * a call to remove() or clear().
 * <p>
 * If the max size value is reached then no more items are added to the cache until some are removed
 * either explicitly or automically via timed-out values. 
 * 
 * @author Kevin Roast
 */
public class AutoExpireCache<K extends Serializable, V extends Serializable> implements SimpleCache<K, V>
{
    // TODO: configure these values via Spring
    private final long  TIMEDIFF  = 1000000L * 1000L * 60L * 5L;    // 5 mins in nano-seconds
    private final int   MAXSIZE   = 4096;                           // maximum size of the cache
    private final float THRESHOLD = 0.75f;                          // before we start removing items
    private int maxsize = MAXSIZE;
    private float threshold = THRESHOLD;
    private Map<Object, CacheItem<K, V>> cache = new HashMap<Object, CacheItem<K, V>>(maxsize, 1.0f);

    /**
     * Default constructor
     */
    public AutoExpireCache()
    {
    }
    
    /**
     * Constructor
     */
    public AutoExpireCache(int maxsize, float threshold)
    {
        maxsize = maxsize;
        threshold = threshold;
    }

    /**
     * @see org.alfresco.repo.cache.SimpleCache#get(K)
     */
    public synchronized V get(K key)
    {
        CacheItem<K, V> wrapper = cache.get(key);
        if (wrapper != null)
        {
            // we cache values till a specific timeout then remove them
            // this also gives other values a chance to get added if we are nearing the max size
            if (cache.size() > (maxsize * threshold) &&
                System.nanoTime() > (wrapper.Timestamp + TIMEDIFF))
            {
                //if (log.isDebugEnabled())
                //   log.debug("*****Removing timedout key: " + key);
                cache.remove(key);
                wrapper = null;
            }
        }
        return wrapper != null ? wrapper.Value : null;
    }

    /**
     * @see org.alfresco.repo.cache.SimpleCache#put(K, V)
     */
    public synchronized void put(K key, V value)
    {
        if (cache.size() < maxsize)
        {
            //if (log.isDebugEnabled())
            //   log.debug("***Adding new key: " + key + "   size: " + cache.size());
            cache.put(key, new CacheItem(key, value));
        }
    }

    /**
     * @see org.alfresco.repo.cache.SimpleCache#remove(K)
     */
    public synchronized void remove(K key)
    {
        cache.remove(key);
    }

    /**
     * @see org.alfresco.repo.cache.SimpleCache#clear()
     */
    public void clear()
    {
        // better to let the GC deal with removing the old map in one shot
        // rather than try to clear each slot slowly using clear()
        cache = new HashMap<Object, CacheItem<K, V>>(maxsize, 1.0f);
    }

    /**
     * @see org.alfresco.repo.cache.SimpleCache#contains(K)
     */
    public synchronized boolean contains(K key)
    {
        return false;
    }
    
    
    /**
     * Simple wrapper class for a cache item.
     * Stores a timestamp of when the item was added so it can be purged from the cache later.
     */
    private static class CacheItem<K, V>
    {
        CacheItem(K key, V value)
        {
            this.key = key;
            Value = value;
            Timestamp = System.nanoTime();
        }
        
        public int hashCode()
        {
            return key.hashCode();
        }
        
        public boolean equals(Object o)
        {
            if (o instanceof CacheItem == false) return false;
            return key.equals( ((CacheItem)o).key );
        }
        
        private K key;
        long Timestamp;
        V Value;
    }
}
