/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.cache;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A cache backed by a simple <code>HashMap</code>.
 * <p>
 * <b>Note:</b> This cache is not transaction- or thread-safe.  Use it for single-threaded tests only.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class MemoryCache<K extends Serializable, V extends Object> implements SimpleCache<K, V>
{
    private Map<K, V> map;
    
    public MemoryCache()
    {
        map = new HashMap<K, V>(15);
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
