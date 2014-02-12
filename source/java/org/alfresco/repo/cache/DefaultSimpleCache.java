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
    private static final int DEFAULT_CAPACITY = 200000;
    private Cache<K, AbstractMap.SimpleImmutableEntry<K, V>> map;
    private String cacheName;
    private final int maxItems;
    
    /**
     * Construct a cache using the specified capacity and name.
     * 
     * @param maxItems The cache capacity.
     */
    public DefaultSimpleCache(int maxItems, String cacheName)
    {
        if (maxItems < 1)
        {
            throw new IllegalArgumentException("maxItems must be a positive integer, but was " + maxItems);
        }
        else if (maxItems == 0)
        {
            maxItems = DEFAULT_CAPACITY;
        }
        this.maxItems = maxItems;
        setBeanName(cacheName);
        
        // The map will have a bounded size determined by the maxItems member variable.
        map = CacheBuilder.newBuilder()
                    .maximumSize(maxItems)
                    .concurrencyLevel(32)
                    .build();
    }
    
    /**
     * Default constructor. Initialises the cache with a default capacity {@link #DEFAULT_CAPACITY}
     * and no name.
     */
    public DefaultSimpleCache()
    {
        this(DEFAULT_CAPACITY, null);
    }
    
    @Override
    public boolean contains(K key)
    {
        return map.asMap().containsKey(key);
    }

    @Override
    public Collection<K> getKeys()
    {
        return map.asMap().keySet();
    }

    @Override
    public V get(K key)
    {
        AbstractMap.SimpleImmutableEntry<K, V> kvp = map.getIfPresent(key);
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
        AbstractMap.SimpleImmutableEntry<K, V> priorKVP = map.asMap().put(key, kvp);
        return priorKVP != null && (! priorKVP.equals(kvp));
    }
    
    @Override
    public void remove(K key)
    {
        map.invalidate(key);
    }

    @Override
    public void clear()
    {
        map.invalidateAll();
    }

    @Override
    public String toString()
    {
        return "DefaultSimpleCache[maxItems=" + maxItems + ", cacheName=" + cacheName + "]";
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
