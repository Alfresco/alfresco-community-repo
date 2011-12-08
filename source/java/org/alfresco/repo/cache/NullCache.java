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
package org.alfresco.repo.cache;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

/**
 * A cache that does nothing - always.
 * <P/>
 * There are conditions under which code that expects to be caching, should not be.  Using this
 * cache, it becomes possible to configure a valid cache in whilst still ensuring that the
 * actual caching is not performed.
 * 
 * @author Derek Hulley
 */
public class NullCache<K extends Serializable, V extends Object> implements SimpleCache<K, V>
{
    /** Singleton for retrieval via {@link #getInstance() } */
    private static final NullCache<Serializable, Object> INSTANCE = new NullCache<Serializable, Object>();
    
    /**
     * @return          Returns a singleton that can be used in any way - all operations are stateless
     */
    @SuppressWarnings("unchecked")
    public static final <K extends Serializable, V extends Object> NullCache<K, V> getInstance()
    {
        return (NullCache<K, V>) INSTANCE;
    }
    
    public NullCache()
    {
    }

    /** NO-OP */
    public boolean contains(K key)
    {
        return false;
    }

    public Collection<K> getKeys()
    {
        return Collections.<K>emptyList();
    }

    /** NO-OP */
    public V get(K key)
    {
        return null;
    }

    /** NO-OP */
    public void put(K key, V value)
    {
        return;
    }

    /** NO-OP */
    public void remove(K key)
    {
        return;
    }

    /** NO-OP */
    public void clear()
    {
        return;
    }
}
