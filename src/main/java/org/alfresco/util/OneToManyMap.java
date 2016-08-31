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

package org.alfresco.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * An extension of <code>java.util.Map</code> that represents a mapping
 * from a key to a set of values. In addition to the standard
 * <code>java.util.Map</code> methods this interface also provides several useful
 * methods for directly accessing the values rather than having to access values
 * via a <code>java.util.Set</code>
 * 
 * @author Nick Smith
 */
public interface OneToManyMap<K, V> extends Map<K, Set<V>>
{
    /**
     * This method returns <code>true</code> if any of the value sets in the
     * OneToManyMap contains an equivalent object to the <code>value</code>
     * parameter, where equivalence is determined using the
     * <code>equals(Object)</code> method.
     * 
     * @param value The value being searched for.
     * @return Returns <code>true</code> if any of the value sets contains a
     *         matching value, otherwise returns <code>false</code>
     */
    public abstract boolean containsSingleValue(V value);

    /**
     * This method is similar to the <code>java.util.Map.entrySet()</code>
     * method, however the entries returned map from a key to a value, rather
     * than from a key(<code>K</code>) to a value(<code>V</code>) rather than
     * froma key(<code>K</code>) to a set of values(<code>Set&ltV&gt</code>). <br/>
     * Note that the entries returned by this method do not support the method
     * <code>java.util.Map.Entry.setValue(V)</code>.
     * 
     * @return The
     *         <code>Set&ltEntry&ltK, V&gt&gt representing all the key-value pairs in the ManyToOneMap.
     */
    public abstract Set<Entry<K, V>> entries();

    /**
     * This method is similar to the method <code>java.util.Map.put(K, V)</code>
     * , however it allows the user to add a single value to the map rather than
     * adding a <code>java.util.Set</code> containing one or more values. If the
     * specified key already has a set of values associated with it then the new
     * value is added to this set. Otherwise a new set is created and the new
     * value is added to that.
     * 
     * @param key
     * @param value
     * @return returns the newly added value.
     */
    public abstract V putSingleValue(K key, V value);

    /**
     * This method is similar to <code>java.utilMap.putAll(Map m)</code>,
     * however the map specified is from keys to values instead of keys to sets
     * of values.
     * 
     * @param m A map containing the key-value mappings to be added to the
     *            ManyToOneMap.
     */
    public abstract void putAllSingleValues(Map<? extends K, ? extends V> m);

    /**
     * Returns a Collection of all the values in the map. Unlike
     * <code>values()</code> the values are in a single flattened
     * <code>Collection&ltV&gt</code> rather than a
     * <code>Collection&ltSet&ltV&gt&gt</code>.
     * 
     * @return All the values in the map as a flattened Collection.
     */
    public abstract Collection<V> flatValues();

}