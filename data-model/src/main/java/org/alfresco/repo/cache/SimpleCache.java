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

import org.alfresco.api.AlfrescoPublicApi;

/**
 * Basic caching interface.
 * <p>
 * All implementations <b>must</b> be thread-safe.  Additionally, the use of the
 * <tt>Serializable</tt> for both keys and values ensures that the underlying
 * cache implementations can support both clustered caches as well as persistent
 * caches.
 * <p>
 * All implementations must support <tt>null</tt> values.  It therefore follows
 * that
 * <pre>
 *    (simpleCache.contains(key) == true) does not imply (simpleCache.get(key) != null)
 * </pre>
 * but
 * <pre>
 *    (simpleCache.contains(key) == false) implies (simpleCache.get(key) == null)
 * </pre>
 * 
 * @author Derek Hulley
 */
@AlfrescoPublicApi
public interface SimpleCache<K extends Serializable, V extends Object>
{
    /**
     * @param key       the cache key to check up on
     * @return          Returns <tt>true</tt> if there is a cache entry,
     *                  regardless of whether the value itself is <tt>null</tt>
     */
    public boolean contains(K key);
    
    public Collection<K> getKeys();
    
    /**
     * @param key K
     * @return          Returns the value associated with the key.  It will be <tt>null</tt>
     *                  if the value is <tt>null</tt> or if the cache doesn't have an entry.
     */
    public V get(K key);
    
    /**
     * Set the value to store for a given key.
     * <p/>
     * Be sure to use {@link #remove(Serializable) remove} if cache entries need to be removed
     * as the cache implementations must treat a <tt>null</tt> value as a first class object
     * in exactly the same way as a {@link Map} will allow storage and retrieval of <tt>null</tt> values.
     * 
     * @param key       the key against which to store the value
     * @param value     the value to store.  <tt>null</tt> is allowed.
     */
    public void put(K key, V value);
    
    /**
     * Removes the cache entry whether or not the value stored against it is <tt>null</tt>.
     * 
     * @param key       the key value to remove
     */
    public void remove(K key);
    
    public void clear();
}
