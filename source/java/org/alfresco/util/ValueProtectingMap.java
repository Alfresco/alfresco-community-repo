/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A map that protects keys and values from accidental modification.
 * <p/>
 * Use this map when keys or values need to be protected against client modification.
 * For example, when a component pulls a map from a common resource it can wrap
 * the map with this class to prevent any accidental modification of the shared
 * resource.
 * <p/>
 * Upon first write to this map , the underlying map will be copied (selectively cloned),
 * the original map handle will be discarded and the copied map will be used.  Note that
 * the map copy process will also occur if any mutable value is in danger of being
 * exposed to client modification.  Therefore, methods that iterate and retrieve values
 * will also trigger the copy if any values are mutable.
 * 
 * @param <K>               the map key type (must extend {@link Serializable})
 * @param <V>               the map value type (must extend {@link Serializable})
 * 
 * @author Derek Hulley
 * @since 3.4.9
 * @since 4.0.1
 */
public class ValueProtectingMap<K extends Serializable, V extends Serializable> implements Map<K, V>, Serializable
{
    private static final long serialVersionUID = -9073485393875357605L;
    
    /**
     * Default immutable classes:
     * <li>String</li>
     * <li>BigDecimal</li>
     * <li>BigInteger</li>
     * <li>Byte</li>
     * <li>Double</li>
     * <li>Float</li>
     * <li>Integer</li>
     * <li>Long</li>
     * <li>Short</li>
     * <li>Boolean</li>
     * <li>Date</li>
     * <li>Locale</li>
     */
    public static final Set<Class<?>> DEFAULT_IMMUTABLE_CLASSES;
    static
    {
        DEFAULT_IMMUTABLE_CLASSES = new HashSet<Class<?>>(13);
        DEFAULT_IMMUTABLE_CLASSES.add(String.class);
        DEFAULT_IMMUTABLE_CLASSES.add(BigDecimal.class);
        DEFAULT_IMMUTABLE_CLASSES.add(BigInteger.class);
        DEFAULT_IMMUTABLE_CLASSES.add(Byte.class);
        DEFAULT_IMMUTABLE_CLASSES.add(Double.class);
        DEFAULT_IMMUTABLE_CLASSES.add(Float.class);
        DEFAULT_IMMUTABLE_CLASSES.add(Integer.class);
        DEFAULT_IMMUTABLE_CLASSES.add(Long.class);
        DEFAULT_IMMUTABLE_CLASSES.add(Short.class);
        DEFAULT_IMMUTABLE_CLASSES.add(Boolean.class);
        DEFAULT_IMMUTABLE_CLASSES.add(Date.class);
        DEFAULT_IMMUTABLE_CLASSES.add(Locale.class);
    }
    
    /**
     * Protect a specific value if it is considered mutable
     * 
     * @param <S>                   the type of the value, which must be {@link Serializable}
     * @param value                 the value to protect if it is mutable (may be <tt>null</tt>)
     * @param immutableClasses      a set of classes that can be considered immutable
     *                              over and above the {@link #DEFAULT_IMMUTABLE_CLASSES default set}
     * @return                      a cloned instance (via serialization) or the instance itself, if immutable
     */
    @SuppressWarnings("unchecked")
    public static <S extends Serializable> S protectValue(S value, Set<Class<?>> immutableClasses)
    {
        if (!mustProtectValue(value, immutableClasses))
        {
            return value;
        }
        // We have to clone it
        // No worries about the return type; it has to be the same as we put into the serializer
        return (S) SerializationUtils.deserialize(SerializationUtils.serialize(value));
    }
    
    /**
     * Utility method to check if values need to be cloned or not
     * 
     * @param <S>                   the type of the value, which must be {@link Serializable}
     * @param value                 the value to check
     * @param immutableClasses      a set of classes that can be considered immutable
     *                              over and above the {@link #DEFAULT_IMMUTABLE_CLASSES default set}
     * @return                      <tt>true</tt> if the value must <b>NOT</b> be given
     *                              to the calling clients
     */
    public static <S extends Serializable> boolean mustProtectValue(S value, Set<Class<?>> immutableClasses)
    {
        if (value == null)
        {
            return false;
        }
        Class<?> clazz = value.getClass();
        return (
                DEFAULT_IMMUTABLE_CLASSES.contains(clazz) == false &&
                immutableClasses.contains(clazz) == false);
    }
    
    /**
     * Utility method to clone a map, preserving immutable instances
     * 
     * @param <K>                   the map key type, which must be {@link Serializable}
     * @param <V>                   the map value type, which must be {@link Serializable}
     * @param map                   the map to copy
     * @param immutableClasses      a set of classes that can be considered immutable
     *                              over and above the {@link #DEFAULT_IMMUTABLE_CLASSES default set}
     */
    public static <K extends Serializable, V extends Serializable> Map<K, V> cloneMap(Map<K, V> map, Set<Class<?>> immutableClasses)
    {
        Map<K, V> copy = new HashMap<K, V>((int)(map.size() * 1.3));
        for (Map.Entry<K, V> element : map.entrySet())
        {
            K key = element.getKey();
            V value = element.getValue();
            // Clone as necessary
            key = ValueProtectingMap.protectValue(key, immutableClasses);
            value = ValueProtectingMap.protectValue(value, immutableClasses);
            copy.put(key, value);
        }
        return copy;
    }
    
    private ReentrantReadWriteLock.ReadLock readLock;
    private ReentrantReadWriteLock.WriteLock writeLock;
    
    private boolean cloned = false;
    private Map<K, V> map;
    private Set<Class<?>> immutableClasses;
    
    /**
     * Construct providing a protected map and using only the
     * {@link #DEFAULT_IMMUTABLE_CLASSES default immutable classes}
     * 
     * @param protectedMap          the map to safeguard
     */
    public ValueProtectingMap(Map<K, V> protectedMap)
    {
        this (protectedMap, null);
    }
    
    /**
     * Construct providing a protected map, complementing the set of
     * {@link #DEFAULT_IMMUTABLE_CLASSES default immutable classes}
     * 
     * @param protectedMap          the map to safeguard
     * @param immutableClasses      additional immutable classes
     *                              over and above the {@link #DEFAULT_IMMUTABLE_CLASSES default set}
     *                              (may be <tt>null</tt>
     */
    public ValueProtectingMap(Map<K, V> protectedMap, Set<Class<?>> immutableClasses)
    {
        // Unwrap any internal maps if given a value protecting map
        if (protectedMap instanceof ValueProtectingMap)
        {
            ValueProtectingMap<K, V> mapTemp = (ValueProtectingMap<K, V>) protectedMap;
            this.map = mapTemp.map;
        }
        else
        {
            this.map = protectedMap;
        }
        
        this.cloned = false;
        if (immutableClasses == null)
        {
            this.immutableClasses = Collections.emptySet();
        }
        else
        {
            this.immutableClasses = new HashSet<Class<?>>(immutableClasses);
        }
        // Construct locks
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        this.readLock = lock.readLock();
        this.writeLock = lock.writeLock();
    }
    
    /**
     * An unsafe method to use for anything except tests.
     * 
     * @return              the map that this instance is protecting
     */
    /* protected */ Map<K, V> getProtectedMap()
    {
        return map;
    }
    
    /**
     * Called by methods that need to force the map into a safe state.
     * <p/>
     * This method can be called without any locks being active.
     */
    private void cloneMap()
    {
        readLock.lock();
        try
        {
            // Check that it hasn't been copied already
            if (cloned)
            {
                return;
            }
        }
        finally
        {
            readLock.unlock();
        }
        /*
         * Note: This space here is a window during which some code could have made
         *       a copy.  Therefore we will do a cautious double-check.
         */
        // Put in a write lock before cloning the map
        writeLock.lock();
        try
        {
            // Check that it hasn't been copied already
            if (cloned)
            {
                return;
            }
            
            Map<K, V> copy = ValueProtectingMap.cloneMap(map, immutableClasses);
            // Discard the original
            this.map = copy;
            this.cloned = true;
        }
        finally
        {
            writeLock.unlock();
        }
    }

    /*
     * READ-ONLY METHODS
     */
    
    @Override
    public int size()
    {
        readLock.lock();
        try
        {
            return map.size();
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public boolean isEmpty()
    {
        readLock.lock();
        try
        {
            return map.isEmpty();
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public boolean containsKey(Object key)
    {
        readLock.lock();
        try
        {
            return map.containsKey(key);
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public boolean containsValue(Object value)
    {
        readLock.lock();
        try
        {
            return map.containsValue(value);
        }
        finally
        {
            readLock.unlock();
        }

    }

    @Override
    public int hashCode()
    {
        readLock.lock();
        try
        {
            return map.hashCode();
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        readLock.lock();
        try
        {
            return map.equals(obj);
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public String toString()
    {
        readLock.lock();
        try
        {
            return map.toString();
        }
        finally
        {
            readLock.unlock();
        }
    }

    /*
     * METHODS THAT *MIGHT* REQUIRE COPY
     */

    @Override
    public V get(Object key)
    {
        readLock.lock();
        try
        {
            V value = map.get(key);
            return ValueProtectingMap.protectValue(value, immutableClasses);
        }
        finally
        {
            readLock.unlock();
        }
    }

    /*
     * METHODS THAT REQUIRE COPY
     */

    @Override
    public V put(K key, V value)
    {
        cloneMap();
        return map.put(key, value);
    }

    @Override
    public V remove(Object key)
    {
        cloneMap();
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m)
    {
        cloneMap();
        map.putAll(m);
    }

    @Override
    public void clear()
    {
        cloneMap();
        map.clear();
    }

    @Override
    public Set<K> keySet()
    {
        cloneMap();
        return map.keySet();
    }

    @Override
    public Collection<V> values()
    {
        cloneMap();
        return map.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet()
    {
        cloneMap();
        return map.entrySet();
    }
}
