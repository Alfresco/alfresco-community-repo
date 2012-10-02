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
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.Weighers;

/**
 * {@link SimpleCache} implementation backed by a {@link ConcurrentLinkedHashMap}.
 * 
 * @author Matt Ward
 */
public final class DefaultSimpleCache<K extends Serializable, V extends Object>
    implements SimpleCache<K, V>, BeanNameAware, InitializingBean
{
    private Map<K, AbstractMap.SimpleImmutableEntry<K, V>> map;
    private int maxItems = 1000000;
    private String cacheName;
    
    /**
     * Default constructor. {@link #afterPropertiesSet()} MUST be called before the cache
     * may be used when the cache is constructed using the default constructor.
     */
    public DefaultSimpleCache()
    {
    }
    
    /**
     * Constructor for programmatic use.
     * @param maxItems
     * @param cacheName
     */
    public DefaultSimpleCache(int maxItems, String cacheName)
    {
        setMaxItems(maxItems);
        setBeanName(cacheName);
        afterPropertiesSet();
    }



    @Override
    public boolean contains(K key)
    {
        return map.containsKey(key);
    }

    @Override
    public Collection<K> getKeys()
    {
        return map.keySet();
    }

    @Override
    public V get(K key)
    {
        AbstractMap.SimpleImmutableEntry<K, V> kvp = map.get(key);
        if (kvp == null)
        {
            return null;
        }
        return kvp.getValue();
    }

    @Override
    public void put(K key, V value)
    {
        AbstractMap.SimpleImmutableEntry<K, V> kvp = new AbstractMap.SimpleImmutableEntry<K, V>(key, value);
        map.put(key, kvp);
    }

    @Override
    public void remove(K key)
    {
        map.remove(key);
    }

    @Override
    public void clear()
    {
        map.clear();
    }

    @Override
    public String toString()
    {
        return "DefaultSimpleCache[maxItems=" + maxItems + ", cacheName=" + cacheName + "]";
    }

    /**
     * Sets the maximum number of items that the cache will hold. The cache
     * must be re-initialised if already in existence using {@link #afterPropertiesSet()}.
     * 
     * @param maxItems
     */
    public synchronized void setMaxItems(int maxItems)
    {
        this.maxItems = maxItems;
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

    
    /**
     * Initialise the cache.
     * 
     * @throws Exception
     */
    @Override
    public synchronized void afterPropertiesSet()
    {
        if (maxItems < 1)
        {
            throw new IllegalArgumentException("maxItems property must be a positive integer.");
        }
        
        // The map will have a bounded size determined by the maxItems member variable.
        map = new ConcurrentLinkedHashMap.Builder<K, AbstractMap.SimpleImmutableEntry<K, V>>()
                    .maximumWeightedCapacity(maxItems)
                    .concurrencyLevel(32)
                    .weigher(Weighers.singleton())
                    .build();
    }
}
