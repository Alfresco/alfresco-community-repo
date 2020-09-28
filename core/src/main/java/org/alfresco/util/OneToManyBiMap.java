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

/**
 * An extension of <code>org.alfresco.util.OneToManyMap</code> that stores the
 * inverse mapping from a value to its key.
 * 
 * @author Nick Smith
 */
public interface OneToManyBiMap<K, V> extends OneToManyMap<K, V>
{

    /**
     * Returns the key, if any, for the specified <code>value</code>. If the
     * specified value does not exist within the map then this method returns
     * <code>null</code>.
     * 
     * @param value
     * @return The key to the specified <code>value</code> or <code>null</code>.
     */
    public abstract K getKey(V value);

    /**
     * Removes the specified <code>value</code> from the <code>OneToManyBiMap</code>. If this was the only value associated with the key to this value, then the key is also removed.
     * 
     * @param value The value to be removed.
     * @return The key that is associated with the value to be removed.
     */
    public abstract K removeValue(V value);

}