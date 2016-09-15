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

/**
 * Interface for caches that support value locking
 * 
 * @author Derek Hulley
 * @since 4.1.6
 */
public interface LockingCache<K extends Serializable, V extends Object> extends SimpleCache<K, V>
{
    /**
     * Determine if a value (addition, removal or update) has been locked for the remainer of the transaction
     * 
     * @param key           the cache key to check up on
     * @return              <tt>true</tt> if the value will not change for the remaineder of the transaction
     */
    boolean isValueLocked(K key);
    
    /**
     * Prevent a key's value from being changed for the duriation of the transaction.
     * By default, further attempts to modify the associated value will be ignored;
     * this includes add a value back after removal.
     * 
     * @param key           the cache key that will be locked against change
     */
    void lockValue(K key);
    
    /**
     * Cancel any previous lock applied to a key's value.
     * 
     * @param key           the cache key that will be unlocked, allowing changes
     */
    void unlockValue(K key);
}
