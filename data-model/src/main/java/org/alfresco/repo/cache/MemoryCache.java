/*
 * #%L
 * Alfresco Data model classes
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

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A cache backed by a simple <code>ConcurrentHashMap</code>.
 * <p>
 * <b>Note:</b> This cache is not transaction-safe.  Use it for tests or wrap it appropriately.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class MemoryCache<K extends Serializable, V extends Object> implements SimpleCache<K, V>
{
    private Map<K, V> map;
    
    public MemoryCache()
    {
        map = new ConcurrentHashMap<K, V>(15);
    }

    public boolean contains(K key)
    {
        return map.containsKey(key);
    }

    public Collection<K> getKeys()
    {
        return map.keySet();
    }

    public V get(K key)
    {
        return map.get(key);
    }

    public void put(K key, V value)
    {
        map.put(key, value);
    }

    public void remove(K key)
    {
        map.remove(key);
    }

    public void clear()
    {
        map.clear();
    }
}
