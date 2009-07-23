/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.cache.lookup;

import java.io.Serializable;

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.util.Pair;

/**
 * A cache for two-way lookups of database entities.  These are characterized by having a unique
 * key (perhaps a database ID) and a separate unique key that identifies the object.
 * <p>
 * The keys must have good <code>equals</code> and </code>hashCode</code> implementations and
 * must respect the case-sensitivity of the use-case.
 * 
 * @author Derek Hulley
 * @since 3.3
 */
public class EntityLookupCache<K extends Serializable, V extends Object, VK extends Serializable>
{
    private static final String NULL_VALUE = "@@NULL_VALUE@@";
    
    private final SimpleCache<Serializable, Object> cache;
    private final EntityLookup<K, V, VK> entityLookup;
    
    @SuppressWarnings("unchecked")
    public EntityLookupCache(SimpleCache cache, EntityLookup<K, V, VK> entityLookup)
    {
        this.cache = cache;
        this.entityLookup = entityLookup;
    }
    
    /**
     * Interface to support lookups of the entities using keys and values.
     */
    public static interface EntityLookup<K1 extends Serializable, V1 extends Object, VK1 extends Serializable>
    {
        /**
         * Resolve the given value into a unique value key that can be used to find the entity's ID.
         * <p>
         * Implementations will often return the value itself, provided that the value is both
         * serializable and has a good <code>equals</code> and <code>hashCode</code>.
         * 
         * @param value         the full value being keyed
         * @return              Returns the business key representing the entity
         */
        VK1 getValueKey(V1 value);
        
        /**
         * Find an entity for a given key.
         * 
         * @param key           the key (ID) used to identify the entity
         * @return              Return the entity or <tt>null</tt> if no entity is exists for the ID
         */
        Pair<K1, V1> findByKey(K1 key);
        
        /**
         * Find and entity using the given value key.  The <code>equals</code> and <code>hashCode</code>
         * methods of the value object should respect case-sensitivity in the same way that this
         * lookup treats case-sensitivity i.e. if the <code>equals</code> method is <b>case-sensitive</b>
         * then this method should look the entity up using a <b>case-sensitive</b> search.  Where the
         * behaviour is configurable, 
         * 
         * @param value         the value (business object) used to identify the entity
         * @return              Return the entity or <tt>null</tt> if no entity matches the given value
         */
        Pair<K1, V1> findByValue(V1 value);
        
        Pair<K1, V1> createValue(V1 value);
    }
    
    @SuppressWarnings("unchecked")
    Pair<K, V> getByKey(K key)
    {
        // Look in the cache
        V value = (V) cache.get(key);
        if (value != null && value.equals(NULL_VALUE))
        {
            // We checked before
            return null;
        }
        else if (value != null)
        {
            return new Pair<K, V>(key, value);
        }
        // Resolve it
        Pair<K, V> entityPair = entityLookup.findByKey(key);
        if (entityPair == null)
        {
            // Cache nulls
            cache.put(key, NULL_VALUE);
        }
        else
        {
            // Cache the value
            cache.put(key, entityPair.getSecond());
        }
        // Done
        return entityPair;
    }
    
    @SuppressWarnings("unchecked")
    Pair<K, V> getByValue(V value)
    {
        // Get the value key
        VK valueKey = entityLookup.getValueKey(value);
        // Look in the cache
        K key = (K) cache.get(valueKey);
        // Check if we have looked this up already
        if (key != null && key.equals(NULL_VALUE))
        {
            // We checked before
            return null;
        }
        else if (key != null)
        {
            return new Pair<K, V>(key, value);
        }
        // Resolve it
        Pair<K, V> entityPair = entityLookup.findByValue(value);
        if (entityPair == null)
        {
            // Cache a null
            cache.put(valueKey, NULL_VALUE);
        }
        else
        {
            // Cache the key
            cache.put(valueKey, key);
        }
        // Done
        return entityPair;
    }
    
    @SuppressWarnings("unchecked")
    Pair<K, V> getOrCreateByValue(V value)
    {
        // Get the value key
        VK valueKey = entityLookup.getValueKey(value);
        // Look in the cache
        K key = (K) cache.get(valueKey);
        // Check if the value is already mapped to a key
        if (key != null && !key.equals(NULL_VALUE))
        {
            return new Pair<K, V>(key, value);
        }
        // Resolve it
        Pair<K, V> entityPair = entityLookup.findByValue(value);
        if (entityPair == null)
        {
            // Create it
            entityPair = entityLookup.createValue(value);
        }
        key = entityPair.getFirst();
        // Cache the key and value
        cache.put(valueKey, key);
        cache.put(key, value);
        // Done
        return entityPair;
    }
}
