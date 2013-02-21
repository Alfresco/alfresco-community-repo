/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.content.transform;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides simple get and put access to a Map like object with a double key that allows
 * either or both keys to be a wild card that matches any value. May not contain null
 * keys or values.<p>
 * 
 * Originally created for mapping source and target mimetypes to transformer configuration data.<p>
 * 
 * For example:
 * <pre>
 *       DoubleMap<String, String, String> foodLikes = new DoubleMap<String, String, String>("*", "*");
 *       
 *       foodLikes.put("cat",   "mouse", "likes");
 *       
 *       foodLikes.get("cat", "mouse"); // returns "likes"
 *       foodLikes.get("cat", "meat");  // returns null
 *       
 *       foodLikes.put("dog",   "meat",  "likes");
 *       foodLikes.put("dog",   "stick", "unsure");
 *       foodLikes.put("child", "olive", "dislikes");
 *       foodLikes.put("bird",  "*",     "worms only");
 *       foodLikes.put("*",     "meat",  "unknown");
 *       foodLikes.put("*",     "*",     "no idea at all");
 *       
 *       foodLikes.get("cat", "mouse"); // returns "likes"
 *       foodLikes.get("cat", "meat");  // returns "unknown"
 *       foodLikes.get("cat", "tea");   // returns "unknown"
 *       foodLikes.get("*",   "mouse"); // returns "no idea at all"
 *       foodLikes.get("dog", "*");     // returns "no idea at all"
 *       foodLikes.get("bird","*");     // returns "worms only"
 *       foodLikes.get("bird","tea");   // returns "worms only"
 * </pre>
 * 
 * @author Alan Davis
 */
public class DoubleMap<K1, K2, V>
{
    private final Map<K1, Map<K2, V>> mapMap = new ConcurrentHashMap<K1, Map<K2, V>>();
    private final K1 anyKey1;
    private final K2 anyKey2;
    
    public DoubleMap(K1 anyKey1, K2 anyKey2)
    {
        this.anyKey1 = anyKey1;
        this.anyKey2 = anyKey2;
    }
    
    /**
     * Returns a value for the given keys.
     */
    public V get(K1 key1, K2 key2)
    {
        V value = null;
        
        Map<K2, V> map = mapMap.get(key1);
        boolean anySource = false;
        if (map == null)
        {
            map = mapMap.get(anyKey1);
            anySource = true;
        }
        if (map != null)
        {
            value = map.get(key2);
            if (value == null)
            {
                value = map.get(anyKey2);
                
                // Handle the case were there is no match using an non wildcarded key1 and
                // key2 but is a match if key1 is wildcarded.
                if (value == null && !anySource)
                {
                    map = mapMap.get(anyKey1);
                    if (map != null)
                    {
                        value = map.get(key2);
                        if (value == null)
                        {
                            value = map.get(anyKey2);
                        }
                    }
                }
            }
        }
        
        return value;
    }
    
    /**
     * Returns a value for the given keys without using wildcards.
     */
    public V getNoWildcards(K1 key1, K2 key2)
    {
        V value = null;
        
        Map<K2, V> map = mapMap.get(key1);
        if (map != null)
        {
            value = map.get(key2);
        }
        
        return value;
    }

    /**
     * Adds a value for the given keys.
     */
    public void put(K1 key1, K2 key2, V t)
    {
        Map<K2, V> map = mapMap.get(key1);
        if (map == null)
        {
            map = new ConcurrentHashMap<K2, V>();
            mapMap.put(key1, map);
        }
        
        map.put(key2, t);
    }
}
