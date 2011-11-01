/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.mutable.MutableInt;

/**
 * Helper class that will look up or create transactional resources.
 * This shortcuts some of the "<i>if not existing, then create</i>" code.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public abstract class TransactionalResourceHelper
{
    /**
     * Get the current count value for a named key
     * 
     * @param resourceKey               the key to count against
     * @return                          the current value for the named key
     */
    public static final int getCount(Object resourceKey)
    {
        MutableInt counter = (MutableInt) AlfrescoTransactionSupport.getResource(resourceKey);
        return counter == null ? 0 : counter.intValue();
    }
    
    /**
     * Reset the current count value for a named key.  After this operation, the effective
     * value will be 0.
     * 
     * @param resourceKey               the key to count against
     */
    public static final void resetCount(Object resourceKey)
    {
        AlfrescoTransactionSupport.unbindResource(resourceKey);
    }
    
    /**
     * Increment a count value for named key
     * 
     * @param resourceKey               the key to count against
     * @return                          the newly-incremented value
     */
    public static final int incrementCount(Object resourceKey)
    {
        MutableInt counter = (MutableInt) AlfrescoTransactionSupport.getResource(resourceKey);
        if (counter == null)
        {
            counter = new MutableInt(0);
            AlfrescoTransactionSupport.bindResource(resourceKey, counter);
        }
        counter.increment();
        return counter.intValue();
    }
    
    /**
     * Decrement a count value for a named key
     * 
     * @param resourceKey               the key to count against
     * @param allowNegative             <tt>true</tt> to allow negative values otherwise zero will be the floor
     * @return                          the newly-decremented value (negative, if allowed)
     */
    public static final int decrementCount(Object resourceKey, boolean allowNegative)
    {
        MutableInt counter = (MutableInt) AlfrescoTransactionSupport.getResource(resourceKey);
        if (counter == null)
        {
            counter = new MutableInt(0);
            AlfrescoTransactionSupport.bindResource(resourceKey, counter);
        }
        if (counter.intValue() > 0 || allowNegative)
        {
            counter.decrement();
        }
        return counter.intValue();
    }
    
    /**
     * Support method to determine if there is already a resource associated with the
     * given key.  This method allows quick conditional checking of the key without
     * building a new collection.
     * 
     * @param resourceKey   the key of the resource to check
     * @return              <tt>true</tt> if a resource is already present at the key
     */
    public static final boolean isResourcePresent(Object resourceKey)
    {
        Object resource = AlfrescoTransactionSupport.getResource(resourceKey);
        return resource != null;
    }
    
    /**
     * Support method to retrieve or create and bind a <tt>HashMap</tt> to the current transaction.
     * 
     * @param <K>           the map key type
     * @param <V>           the map value type
     * @param resourceKey   the key under which the resource will be stored
     * @return              Returns an previously-bound <tt>Map</tt> or else a newly-bound <tt>HashMap</tt>
     */
    public static final <K,V> Map<K,V> getMap(Object resourceKey)
    {
        Map<K,V> map = AlfrescoTransactionSupport.<Map<K,V>>getResource(resourceKey);
        if (map == null)
        {
            map = new HashMap<K, V>(29);
            AlfrescoTransactionSupport.bindResource(resourceKey, map);
        }
        return map;
    }

    /**
     * Support method to retrieve or create and bind a <tt>HashSet</tt> to the current transaction.
     * 
     * @param <V>           the set value type
     * @param resourceKey   the key under which the resource will be stored
     * @return              Returns an previously-bound <tt>Set</tt> or else a newly-bound <tt>HashSet</tt>
     */
    public static final <V> Set<V> getSet(Object resourceKey)
    {
        Set<V> set = AlfrescoTransactionSupport.<Set<V>>getResource(resourceKey);
        if (set == null)
        {
            set = new HashSet<V>(29);
            AlfrescoTransactionSupport.bindResource(resourceKey, set);
        }
        return set;
    }

    /**
     * Support method to retrieve or create and bind a <tt>TreeSet</tt> to the current transaction.
     * 
     * @param <V>           the set value type
     * @param resourceKey   the key under which the resource will be stored
     * @return              Returns an previously-bound <tt>TreeSet</tt> or else a newly-bound <tt>TreeSet</tt>
     */
    public static final <V> TreeSet<V> getTreeSet(Object resourceKey)
    {
        TreeSet<V> set = AlfrescoTransactionSupport.<TreeSet<V>>getResource(resourceKey);
        if (set == null)
        {
            set = new TreeSet<V>();
            AlfrescoTransactionSupport.bindResource(resourceKey, set);
        }
        return set;
    }

    /**
     * Support method to retrieve or create and bind a <tt>ArrayList</tt> to the current transaction.
     * 
     * @param <V>           the list value type
     * @param resourceKey   the key under which the resource will be stored
     * @return              Returns an previously-bound <tt>List</tt> or else a newly-bound <tt>ArrayList</tt>
     */
    public static final <V> List<V> getList(Object resourceKey)
    {
        List<V> list = AlfrescoTransactionSupport.<List<V>>getResource(resourceKey);
        if (list == null)
        {
            list = new ArrayList<V>(29);
            AlfrescoTransactionSupport.bindResource(resourceKey, list);
        }
        return list;
    }
}
