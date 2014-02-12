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
import java.util.AbstractMap;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.BeanNameAware;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * {@link SimpleCache} implementation backed by a Google {@link Cache} implementation.
 * 
 * @author Matt Ward
 */
public final class DefaultSimpleCache<K extends Serializable, V extends Object>
    implements SimpleCache<K, V>, BeanNameAware
{
    private static final int DEFAULT_CAPACITY = Integer.MAX_VALUE;
    private Cache<K, AbstractMap.SimpleImmutableEntry<K, V>> cache;
    private String cacheName;
    private final int maxItems;
    private final boolean useMaxItems;
    private final int ttlSecs;
    private final int maxIdleSecs;
    
    /**
     * Construct a cache using the specified capacity and name.
     * 
     * @param maxItems The cache capacity. 0 = use {@link #DEFAULT_CAPACITY}
     * @param useMaxItems Whether the maxItems value should be applied as a size-cap for the cache.
     * @param cacheName An arbitrary cache name.
     */
    @SuppressWarnings("unchecked")
    public DefaultSimpleCache(int maxItems, boolean useMaxItems, int ttlSecs, int maxIdleSecs, String cacheName)
    {
        if (maxItems == 0)
        {
            maxItems = DEFAULT_CAPACITY;
        }
        else if (maxItems < 0)
        {
            throw new IllegalArgumentException("maxItems may not be negative, but was " + maxItems);
        }
        this.maxItems = maxItems;
        this.useMaxItems = useMaxItems;
        this.ttlSecs = ttlSecs;
        this.maxIdleSecs = maxIdleSecs;
        setBeanName(cacheName);
        
        // The map will have a bounded size determined by the maxItems member variable.
        @SuppressWarnings("rawtypes")
        CacheBuilder builder = CacheBuilder.newBuilder();
        
        if (useMaxItems)
        {
            builder.maximumSize(maxItems);
        }
        if (ttlSecs > 0)
        {
            builder.expireAfterWrite(ttlSecs, TimeUnit.SECONDS);
        }
        if (maxIdleSecs > 0)
        {
            builder.expireAfterAccess(maxIdleSecs, TimeUnit.SECONDS);
        }
        builder.concurrencyLevel(32);
        
        cache = (Cache<K, AbstractMap.SimpleImmutableEntry<K, V>>) builder.build();
    }
    
    /**
     * Create a size limited, named cache with no other features enabled.
     * 
     * @param maxItems
     * @param cacheName
     */
    public DefaultSimpleCache(int maxItems, String cacheName)
    {
        this(maxItems, true, 0, 0, cacheName);
    }
    
    /**
     * Default constructor. Initialises the cache with no size limit and no name.
     */
    public DefaultSimpleCache()
    {
        this(0, false, 0, 0, null);
    }
    
    @Override
    public boolean contains(K key)
    {
        return cache.asMap().containsKey(key);
    }

    @Override
    public Collection<K> getKeys()
    {
        return cache.asMap().keySet();
    }

    @Override
    public V get(K key)
    {
        AbstractMap.SimpleImmutableEntry<K, V> kvp = cache.getIfPresent(key);
        if (kvp == null)
        {
            return null;
        }
        return kvp.getValue();
    }

    @Override
    public void put(K key, V value)
    {
        putAndCheckUpdate(key, value);
    }

    /**
     * <code>put</code> method that may be used to check for updates in a thread-safe manner.
     * 
     * @return <code>true</code> if the put resulted in a change in value, <code>false</code> otherwise.
     */
    public boolean putAndCheckUpdate(K key, V value)
    {
        AbstractMap.SimpleImmutableEntry<K, V> kvp = new AbstractMap.SimpleImmutableEntry<K, V>(key, value);
        AbstractMap.SimpleImmutableEntry<K, V> priorKVP = cache.asMap().put(key, kvp);
        return priorKVP != null && (! priorKVP.equals(kvp));
    }
    
    @Override
    public void remove(K key)
    {
        cache.invalidate(key);
    }

    @Override
    public void clear()
    {
        cache.invalidateAll();
    }

    @Override
    public String toString()
    {
        return "DefaultSimpleCache[maxItems=" + maxItems + ", useMaxItems=" + useMaxItems + ", cacheName=" + cacheName + "]";
    }
    
    /**
     * Gets the maximum number of items that the cache will hold.
     * 
     * @return maxItems
     */
    public int getMaxItems()
    {
        return maxItems;
    }
    
    /**
     * Is a size-cap in use?
     * 
     * @return useMaxItems
     */
    public boolean isUseMaxItems()
    {
        return this.useMaxItems;
    }
    
    /**
     * Get the time-to-live setting in seconds.
     * 
     * @return ttlSecs
     */
    public int getTTLSecs()
    {
        return this.ttlSecs;
    }

    /**
     * Get the time-to-idle setting in seconds.
     * 
     * @return maxIdleSecs
     */
    public int getMaxIdleSecs()
    {
        return this.maxIdleSecs;
    }

    /**
     * Retrieve the name of this cache.
     * 
     * @see #setCacheName(String)
     * @return the cacheName
     */
    public String getCacheName()
    {
        return this.cacheName;
    }

    /**
     * Since there are many cache instances, it is useful to be able to associate
     * a name with each one.
     * 
     * @see #setBeanName(String)
     * @param cacheName
     */
    public void setCacheName(String cacheName)
    {
        this.cacheName = cacheName;
    }

    /**
     * Since there are many cache instances, it is useful to be able to associate
     * a name with each one.
     * 
     * @param cacheName Set automatically by Spring, but can be set manually if required.
     */
    @Override
    public void setBeanName(String cacheName)
    {
        this.cacheName = cacheName;
    }
}
