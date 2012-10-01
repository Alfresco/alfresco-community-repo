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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.BeanNameAware;

/**
 * {@link SimpleCache} implementation backed by a {@link LinkedHashMap}.
 * 
 * @author Matt Ward
 */
public final class DefaultSimpleCache<K extends Serializable, V extends Object>
    implements SimpleCache<K, V>, BeanNameAware
{
    private final Map<K, V> map;
    private int maxItems = 0;
    private String cacheName;
    
    public DefaultSimpleCache()
    {
        // Create a LinkedHashMap with accessOrder true, i.e. iteration order
        // will be least recently accessed first. Eviction policy will therefore be LRU.
        // The map will have a bounded size determined by the maxItems member variable.
        map = (Map<K, V>) Collections.synchronizedMap(new LinkedHashMap<K, V>(16, 0.75f, true) {
            private static final long serialVersionUID = 1L;
            @Override
            protected boolean removeEldestEntry(Entry<K, V> eldest)
            {
                return maxItems > 0 && size() > maxItems;
            }
        });
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
        return map.get(key);
    }

    @Override
    public void put(K key, V value)
    {
        map.put(key, value);
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
     * Sets the maximum number of items that the cache will hold. Setting
     * this value will cause the cache to be emptied. A value of zero
     * will allow the cache to grow unbounded.
     * 
     * @param maxItems
     */
    public void setMaxItems(int maxItems)
    {
        synchronized(map)
        {
            map.clear();
            this.maxItems = maxItems;
        }
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
