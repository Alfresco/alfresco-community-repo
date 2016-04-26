package org.alfresco.repo.content.transform;

import java.util.Map;
import java.util.Map.Entry;
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
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (Entry<K1, Map<K2, V>> outerEntry: mapMap.entrySet())
        {
            for (Entry<K2, V> innerEntry: outerEntry.getValue().entrySet())
            {
                if (sb.length() > 0)
                {
                    sb.append("\n");
                }
                sb.append(outerEntry.getKey()).
                    append(", ").
                    append(innerEntry.getKey()).
                    append(" = ").
                    append(innerEntry.getValue());
            }
        }
        return sb.toString();
    }
}
