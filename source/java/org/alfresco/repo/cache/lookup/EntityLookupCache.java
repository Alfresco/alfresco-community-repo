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
import org.alfresco.util.ParameterCheck;

/**
 * A cache for two-way lookups of database entities.  These are characterized by having a unique
 * key (perhaps a database ID) and a separate unique key that identifies the object.  If no cache
 * is given, then all calls are passed through to the backing DAO.
 * <p>
 * The keys must have good <code>equals</code> and </code>hashCode</code> implementations and
 * must respect the case-sensitivity of the use-case.
 * <p>
 * All keys will be unique to the given cache region, allowing the cache to be shared
 * between instances of this class.
 * <p>
 * Generics:
 * <ul>
 *   <li>K:  The database unique identifier.</li>
 *   <li>V:  The value stored against K.</li>
 *   <li>VK: The a value-derived key that will be used as a cache key when caching K for lookups by V.
 *           This can be the value itself if it is itself a good key.</li>
 * </ul>
 * 
 * @author Derek Hulley
 * @since 3.3
 */
public class EntityLookupCache<K extends Serializable, V extends Object, VK extends Serializable>
{
    /**
     * Interface to support lookups of the entities using keys and values.
     */
    public static interface EntityLookupCallbackDAO<K1 extends Serializable, V1 extends Object, VK1 extends Serializable>
    {
        /**
         * Resolve the given value into a unique value key that can be used to find the entity's ID.
         * A return value should be small and efficient; don't return a value if this is not possible.
         * <p/>
         * Implementations will often return the value itself, provided that the value is both
         * serializable and has a good <code>equals</code> and <code>hashCode</code>.
         * <p/>
         * Were no adequate key can be generated for the value, then <tt>null</tt> can be returned.
         * In this case, the {@link #findByValue(Object) findByValue} method might not even do a search
         * and just return <tt>null</tt> itself  i.e. if it is difficult to look the value up in storage
         * then it is probably difficult to generate a cache key from it, too..  In this scenario, the
         * cache will be purely for key-based lookups 
         * 
         * @param value         the full value being keyed (never <tt>null</tt>)
         * @return              Returns the business key representing the entity, or <tt>null</tt>
         *                      if an economical key cannot be generated.
         */
        VK1 getValueKey(V1 value);
        
        /**
         * Find an entity for a given key.
         * 
         * @param key           the key (ID) used to identify the entity (never <tt>null</tt>)
         * @return              Return the entity or <tt>null</tt> if no entity is exists for the ID
         */
        Pair<K1, V1> findByKey(K1 key);
        
        /**
         * Find and entity using the given value key.  The <code>equals</code> and <code>hashCode</code>
         * methods of the value object should respect case-sensitivity in the same way that this
         * lookup treats case-sensitivity i.e. if the <code>equals</code> method is <b>case-sensitive</b>
         * then this method should look the entity up using a <b>case-sensitive</b> search.
         * <p/>
         * Since this is a cache backed by some sort of database, <tt>null</tt> values are allowed by the
         * cache.  The implementation of this method can throw an exception if <tt>null</tt> is not
         * appropriate for the use-case.
         * <p/>
         * If the search is impossible or expensive, this method should just return <tt>null</tt>.  This
         * would usually be the case if the {@link #getValueKey(Object) getValueKey} method also returned
         * <tt>null</tt> i.e. if it is difficult to look the value up in storage then it is probably
         * difficult to generate a cache key from it, too.
         * 
         * @param value         the value (business object) used to identify the entity (<tt>null</tt> allowed).
         * @return              Return the entity or <tt>null</tt> if no entity matches the given value
         */
        Pair<K1, V1> findByValue(V1 value);
        
        /**
         * Create an entity using the given values.  It is valid to assume that the entity does not exist
         * within the current transaction at least.
         * <p/>
         * Since persistence mechanisms often allow <tt>null</tt> values, these can be expected here.  The
         * implementation  must throw an exception if <tt>null</tt> is not allowed for the specific use-case.
         * 
         * @param value         the value (business object) used to identify the entity (<tt>null</tt> allowed).
         * @return              Return the newly-created entity ID-value pair
         */
        Pair<K1, V1> createValue(V1 value);
    }
    
    /**
     * A valid <code>null</code> value i.e. a value that has been <u>persisted</u> as null.
     */
    private static final Serializable VALUE_NULL = "@@VALUE_NULL@@";
    /**
     * A value that was not found or persisted.
     */
    private static final Serializable VALUE_NOT_FOUND = "@@VALUE_NOT_FOUND@@";
    /**
     * The cache region that will be used (see {@link CacheRegionKey}) in all the cache keys
     */
    private static final String CACHE_REGION_DEFAULT = "DEFAULT";
    
    private final SimpleCache<Serializable, Object> cache;
    private final EntityLookupCallbackDAO<K, V, VK> entityLookup;
    private final String cacheRegion;

    /**
     * Construct the lookup cache <b>without any cache</b>.  All calls are passed directly to the
     * underlying DAO entity lookup.
     * 
     * @param entityLookup          the instance that is able to find and persist entities
     */
    public EntityLookupCache(EntityLookupCallbackDAO<K, V, VK> entityLookup)
    {
        this(null, CACHE_REGION_DEFAULT, entityLookup);
    }
    
    /**
     * Construct the lookup cache, using the {@link #CACHE_REGION_DEFAULT default cache region}.
     * 
     * @param cache                 the cache that will back the two-way lookups
     * @param entityLookup          the instance that is able to find and persist entities
     */
    @SuppressWarnings("unchecked")
    public EntityLookupCache(SimpleCache cache, EntityLookupCallbackDAO<K, V, VK> entityLookup)
    {
        this(cache, CACHE_REGION_DEFAULT, entityLookup);
    }
    
    /**
     * Construct the lookup cache, using the given cache region.
     * <p>
     * All keys will be unique to the given cache region, allowing the cache to be shared
     * between instances of this class.
     * 
     * @param cache                 the cache that will back the two-way lookups; <tt>null</tt> to have no backing
     *                              in a cache.
     * @param cacheRegion           the region within the cache to use.
     * @param entityLookup          the instance that is able to find and persist entities
     */
    @SuppressWarnings("unchecked")
    public EntityLookupCache(SimpleCache cache, String cacheRegion, EntityLookupCallbackDAO<K, V, VK> entityLookup)
    {
        ParameterCheck.mandatory("cacheRegion", cacheRegion);
        ParameterCheck.mandatory("entityLookup", entityLookup);
        this.cache = cache;
        this.cacheRegion = cacheRegion;
        this.entityLookup = entityLookup;
    }
    
    @SuppressWarnings("unchecked")
    public Pair<K, V> getByKey(K key)
    {
        if (key == null)
        {
            throw new IllegalArgumentException("An entity lookup key may not be null");
        }
        // Handle missing cache
        if (cache == null)
        {
            return entityLookup.findByKey(key);
        }
        
        CacheRegionKey cacheKey = new CacheRegionKey(cacheRegion, key);
        // Look in the cache
        V value = (V) cache.get(cacheKey);
        if (value != null)
        {
            if (value.equals(VALUE_NOT_FOUND))
            {
                // We checked before
                return null;
            }
            else if (value.equals(VALUE_NULL))
            {
                return new Pair<K, V>(key, null);
            }
            else
            {
                return new Pair<K, V>(key, value);
            }
        }
        // Resolve it
        Pair<K, V> entityPair = entityLookup.findByKey(key);
        if (entityPair == null)
        {
            // Cache "not found"
            cache.put(cacheKey, VALUE_NOT_FOUND);
        }
        else
        {
            value = entityPair.getSecond();
            // Cache the value
            cache.put(
                    cacheKey,
                    (value == null ? VALUE_NULL : value));
        }
        // Done
        return entityPair;
    }
    
    @SuppressWarnings("unchecked")
    public Pair<K, V> getByValue(V value)
    {
        // Handle missing cache
        if (cache == null)
        {
            return entityLookup.findByValue(value);
        }
        
        // Get the value key.
        // The cast to (VK) is counter-intuitive, but works because they're all just Serializable
        // It's nasty, but hidden from the cache client code.
        VK valueKey = (value == null) ? (VK)VALUE_NULL : entityLookup.getValueKey(value);
        // Check if the value has a good key
        if (valueKey == null)
        {
            return entityLookup.findByValue(value);
        }
        
        // Look in the cache
        CacheRegionKey valueCacheKey = new CacheRegionKey(cacheRegion, valueKey);
        K key = (K) cache.get(valueCacheKey);
        // Check if we have looked this up already
        if (key != null)
        {
            // We checked before and ...
            if (key.equals(VALUE_NOT_FOUND))
            {
                // ... it didn't exist
                return null;
            }
            else
            {
                // ... it did exist
                return new Pair<K, V>(key, value);
            }
        }
        // Resolve it
        Pair<K, V> entityPair = entityLookup.findByValue(value);
        if (entityPair == null)
        {
            // Cache "not found"
            cache.put(valueCacheKey, VALUE_NOT_FOUND);
        }
        else
        {
            key = entityPair.getFirst();
            // Cache the key
            cache.put(valueCacheKey, key);
            cache.put(
                    new CacheRegionKey(cacheRegion, key),
                    (value == null ? VALUE_NULL : value));
        }
        // Done
        return entityPair;
    }
    
    @SuppressWarnings("unchecked")
    public Pair<K, V> getOrCreateByValue(V value)
    {
        // Handle missing cache
        if (cache == null)
        {
            Pair<K, V> entityPair = entityLookup.findByValue(value);
            if (entityPair == null)
            {
                entityPair = entityLookup.createValue(value);
            }
            return entityPair;
        }
        
        // Get the value key
        // The cast to (VK) is counter-intuitive, but works because they're all just Serializable.
        // It's nasty, but hidden from the cache client code.
        VK valueKey = (value == null) ? (VK)VALUE_NULL : entityLookup.getValueKey(value);
        // Check if the value has a good key
        if (valueKey == null)
        {
            Pair<K, V> entityPair = entityLookup.findByValue(value);
            if (entityPair == null)
            {
                entityPair = entityLookup.createValue(value);
            }
            return entityPair;
        }
        
        // Look in the cache
        CacheRegionKey valueCacheKey = new CacheRegionKey(cacheRegion, valueKey);
        K key = (K) cache.get(valueCacheKey);
        // Check if the value is already mapped to a key
        if (key != null && !key.equals(VALUE_NOT_FOUND))
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
        cache.put(valueCacheKey, key);
        cache.put(
                new CacheRegionKey(cacheRegion, key),
                (value == null ? VALUE_NULL : value));
        // Done
        return entityPair;
    }
    
    @SuppressWarnings("unchecked")
    public void remove(K key)
    {
        CacheRegionKey keyCacheKey = new CacheRegionKey(cacheRegion, key);
        V value = (V) cache.get(keyCacheKey);
        if (value != null && !value.equals(VALUE_NOT_FOUND))
        {
            // Get the value key and remove it
            VK valueKey = entityLookup.getValueKey(value);
            CacheRegionKey valueCacheKey = new CacheRegionKey(cacheRegion, valueKey);
            cache.remove(valueCacheKey);
        }
        cache.remove(keyCacheKey);
    }
    
    /**
     * Key-wrapper used to separate cache regions, allowing a single cache to be used for different
     * purposes.
     */
    private static class CacheRegionKey implements Serializable
    {
        private static final long serialVersionUID = -213050301938804468L;

        private final String cacheRegion;
        private final Serializable cacheKey;
        private final int hashCode;
        private CacheRegionKey(String cacheRegion, Serializable cacheKey)
        {
            this.cacheRegion = cacheRegion;
            this.cacheKey = cacheKey;
            this.hashCode = cacheRegion.hashCode() + cacheKey.hashCode();
        }
        @Override
        public String toString()
        {
            return cacheRegion + "." + cacheKey.toString();
        }
        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            else if (!(obj instanceof CacheRegionKey))
            {
                return false;
            }
            CacheRegionKey that = (CacheRegionKey) obj;
            return this.cacheRegion.equals(that.cacheRegion) && this.cacheKey.equals(that.cacheKey);
        }
        @Override
        public int hashCode()
        {
            return hashCode;
        }
    }
}
