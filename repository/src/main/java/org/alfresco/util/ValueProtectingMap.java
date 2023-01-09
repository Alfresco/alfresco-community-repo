/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.util;

import org.alfresco.service.cmr.repository.MLText;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    private interface Protector
    {
        Serializable protect(Serializable value, Function<Class<? extends Serializable>, Protector> protectorProvider);
    }

    private static final Protector IDENTITY_PROTECTOR = (value, protectorProvider) -> value;

    private static final Map<Class<? extends Serializable>, Protector> DEFAULT_PROTECTORS;
    static
    {
        // need identity for Class<?> keys and linear time retrieval
        final Map<Class<? extends Serializable>, Protector> map = new IdentityHashMap<>();

        // protectors for common complex structures
        // should be better/faster than full clone, especially when elements are immutable
        map.put(MLText.class, ValueProtectingMap::protectMLText);
        map.put(ArrayList.class, ValueProtectingMap::protectArrayList);
        map.put(HashSet.class, ValueProtectingMap::protectHashSet);
        map.put(HashMap.class, ValueProtectingMap::protectHashMap);

        // default immutables
        map.put(String.class, IDENTITY_PROTECTOR);
        map.put(BigDecimal.class, IDENTITY_PROTECTOR);
        map.put(BigInteger.class, IDENTITY_PROTECTOR);
        map.put(Byte.class, IDENTITY_PROTECTOR);
        map.put(Double.class, IDENTITY_PROTECTOR);
        map.put(Float.class, IDENTITY_PROTECTOR);
        map.put(Integer.class, IDENTITY_PROTECTOR);
        map.put(Long.class, IDENTITY_PROTECTOR);
        map.put(Short.class, IDENTITY_PROTECTOR);
        map.put(Boolean.class, IDENTITY_PROTECTOR);
        map.put(Date.class, IDENTITY_PROTECTOR);
        map.put(Locale.class, IDENTITY_PROTECTOR);

        DEFAULT_PROTECTORS = Collections.unmodifiableMap(map);
    }
    
    /**
     * Protect a specific value if it is considered mutable
     * 
     * @param <S>                   the type of the value, which must be {@link Serializable}
     * @param value                 the value to protect if it is mutable (may be <tt>null</tt>)
     * @param immutableClasses      a set of classes that can be considered immutable
     *                              over and above the {@link #DEFAULT_PROTECTORS default set}
     * @return                      a cloned instance (via serialization) or the instance itself, if immutable
     */
    public static <S extends Serializable> S protectValue(S value, Set<Class<?>> immutableClasses)
    {
        Map<Class<?>, Protector> protectors = new IdentityHashMap<>();
        Function<Class<? extends Serializable>, Protector> protectorProvider = cls -> protectors
                .computeIfAbsent(cls, cls2 -> immutableClasses.contains(cls2) ? IDENTITY_PROTECTOR
                        : DEFAULT_PROTECTORS.getOrDefault(cls2, ValueProtectingMap::protectGeneric));
        return protectValue(value, protectorProvider);

    }

    @SuppressWarnings("unchecked")
    private static <S extends Serializable> S protectValue(final S value, final Function<Class<? extends Serializable>, Protector> protectorProvider)
    {
        S result = value;

        if (value != null)
        {
            final Class<? extends Serializable> valueClass = value.getClass();
            result = (S) protectorProvider.apply(valueClass).protect(value, protectorProvider);
        }
        return result;
    }

    private static Serializable protectMLText(final Serializable value, final Function<Class<? extends Serializable>, Protector> protectorProvider)
    {
        final MLText copy = new MLText();
        copy.putAll((MLText) value);
        return copy;
    }

    @SuppressWarnings("unchecked")
    private static Serializable protectArrayList(final Serializable value, final Function<Class<? extends Serializable>, Protector> protectorProvider)
    {
        final List<Serializable> copy = ((List<Serializable>) value).stream().map(e -> protectValue(e, protectorProvider))
                .collect(Collectors.toList());
        return (Serializable) copy;
    }

    @SuppressWarnings("unchecked")
    private static Serializable protectHashSet(final Serializable value, final Function<Class<? extends Serializable>, Protector> protectorProvider)
    {
        final Set<Serializable> copy = ((Set<Serializable>) value).stream().map(e -> protectValue(e, protectorProvider))
                .collect(Collectors.toSet());
        return (Serializable) copy;
    }

    @SuppressWarnings("unchecked")
    private static Serializable protectHashMap(final Serializable value, final Function<Class<? extends Serializable>, Protector> protectorProvider)
    {
        final Map<Serializable, Serializable> copy = ((Map<Serializable, Serializable>) value).entrySet().stream()
                .collect(Collectors.<Entry<Serializable, Serializable>, Serializable, Serializable> toMap(
                        entry -> protectValue(entry.getKey(), protectorProvider),
                        entry -> protectValue(entry.getValue(), protectorProvider)));
        return (Serializable) copy;
    }

    private static Serializable protectGeneric(final Serializable value, final Function<Class<? extends Serializable>, Protector> protectorProvider)
    {
        return (Serializable) SerializationUtils.deserialize(SerializationUtils.serialize(value));
    }
    
    private ReentrantReadWriteLock.ReadLock readLock;
    private ReentrantReadWriteLock.WriteLock writeLock;

    private Map<K, V> map;

    private boolean mapFullyProtected = false;

    private final Set<Class<? extends Serializable>> immutableClasses = new HashSet<>();

    private final transient Map<Class<? extends Serializable>, Protector> instanceProtectors = new IdentityHashMap<>(
            DEFAULT_PROTECTORS.size() * 2);

    private final transient Function<Class<? extends Serializable>, Protector> instanceProtectorProvider = cls -> this.instanceProtectors
            .computeIfAbsent(cls, cls2 -> this.immutableClasses.contains(cls2) ? IDENTITY_PROTECTOR
                    : DEFAULT_PROTECTORS.getOrDefault(cls2, ValueProtectingMap::protectGeneric));

    
    /**
     * Construct providing a protected map and using only the
     * {@link #DEFAULT_PROTECTORS default immutable classes}
     * 
     * @param protectedMap          the map to safeguard
     */
    public ValueProtectingMap(Map<K, V> protectedMap)
    {
        this (protectedMap, null);
    }
    
    /**
     * Construct providing a protected map, complementing the set of
     * {@link #DEFAULT_PROTECTORS default immutable classes}
     * 
     * @param protectedMap          the map to safeguard
     * @param immutableClasses      additional immutable classes
     *                              over and above the {@link #DEFAULT_PROTECTORS default set}
     *                              (may be <tt>null</tt>
     */
    public ValueProtectingMap(Map<K, V> protectedMap, Set<Class<?>> immutableClasses) {
        // Unwrap any internal maps if given a value protecting map
        if (protectedMap instanceof ValueProtectingMap<?, ?>) {
            ValueProtectingMap<K, V> mapTemp = (ValueProtectingMap<K, V>) protectedMap;
            this.map = mapTemp.map;
        } else {
            this.map = protectedMap;
        }

        this.mapFullyProtected = false;

        if (immutableClasses != null) {
            immutableClasses.stream().filter(Serializable.class::isAssignableFrom)
                    .map(c -> (Class<? extends Serializable>) c.asSubclass(Serializable.class)).forEach(this.immutableClasses::add);
        } else if (protectedMap instanceof ValueProtectingMap<?, ?>) {
            this.immutableClasses.addAll(((ValueProtectingMap<K, V>) protectedMap).immutableClasses);
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
    Map<K, V> getProtectedMap()
    {
        return map;
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
            return ValueProtectingMap.protectValue(value, instanceProtectorProvider);
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
        writeLock.lock();
        try
        {
            ensureDecoupledMap();
            return map.put(key, value);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public V remove(Object key)
    {
        writeLock.lock();
        try
        {
            ensureDecoupledMap();
            return map.remove(key);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m)
    {
        writeLock.lock();
        try
        {
            ensureDecoupledMap();
            map.putAll(m);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public void clear()
    {
        writeLock.lock();
        try
        {
            ensureDecoupledMap();
            map.clear();
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public Set<K> keySet()
    {
        return new KeySet();
    }

    @Override
    public Collection<V> values()
    {
        return new Values();
    }

    @Override
    public Set<Entry<K, V>> entrySet()
    {
        return new EntrySet();
    }

    private void ensureDecoupledMap()
    {
        writeLock.lock();
        try
        {
            if (!mapFullyProtected)
            {
                map = map.entrySet().stream().collect(Collectors.<Entry<K, V>, K, V> toMap(Entry::getKey,
                        e -> ValueProtectingMap.protectValue(e.getValue(), instanceProtectorProvider)));
                mapFullyProtected = true;
            }
        }
        finally
        {
            writeLock.unlock();
        }
    }

    private class KeySet extends AbstractCollection<K> implements Set<K>
    {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean contains(final Object o)
        {
            return ValueProtectingMap.this.containsKey(o);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean remove(final Object o)
        {
            writeLock.lock();
            try
            {
                boolean contained = contains(o);
                ValueProtectingMap.this.remove(o);
                return contained;
            }
            finally
            {
                writeLock.unlock();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean containsAll(final Collection<?> c)
        {
            readLock.lock();
            try
            {
                return c.stream().allMatch(ValueProtectingMap.this::containsKey);
            }
            finally
            {
                readLock.unlock();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean removeAll(final Collection<?> c)
        {
            writeLock.lock();
            try
            {
                long removed = c.stream().filter(this::remove).count();
                return removed != 0;
            }
            finally
            {
                writeLock.unlock();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void clear()
        {
            ValueProtectingMap.this.clear();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Iterator<K> iterator()
        {
            return new KeyIterator();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int size()
        {
            return ValueProtectingMap.this.size();
        }
    }

    private class KeyIterator implements Iterator<K>
    {

        private final Set<K> seenKeys = new HashSet<>();

        private boolean iteratorSwitched = false;

        private Iterator<K> activeIterator;

        private K nextKey;

        private K lastKey;

        private KeyIterator()
        {
            this.activeIterator = map.keySet().iterator();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNext()
        {
            readLock.lock();
            try
            {
                boolean hasNext;
                if (activeIterator == null)
                {
                    activeIterator = map.keySet().iterator();
                    iteratorSwitched = true;
                }

                if (iteratorSwitched)
                {
                    hasNext = activeIterator.hasNext();
                    nextKey = hasNext ? activeIterator.next() : null;
                    while (hasNext && seenKeys.contains(nextKey))
                    {
                        hasNext = activeIterator.hasNext();
                        nextKey = hasNext ? activeIterator.next() : null;
                    }
                }
                else
                {
                    hasNext = activeIterator.hasNext();
                }
                lastKey = null;
                return hasNext;
            }
            finally
            {
                readLock.unlock();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public K next()
        {
            readLock.lock();
            try
            {
                K next;
                if (nextKey != null)
                {
                    next = nextKey;
                }
                else
                {
                    if (activeIterator == null)
                    {
                        activeIterator = map.keySet().iterator();
                        iteratorSwitched = true;
                    }
                    next = activeIterator.next();
                }

                nextKey = null;
                lastKey = next;

                if (next != null)
                {
                    seenKeys.add(next);
                }

                return next;
            }
            finally
            {
                readLock.unlock();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void remove()
        {
            if (this.lastKey == null)
            {
                throw new IllegalStateException();
            }

            writeLock.lock();
            try
            {
                if (activeIterator != null)
                {
                    if (mapFullyProtected)
                    {
                        // safe to remove via iterator
                        activeIterator.remove();
                    }
                    else
                    {
                        ValueProtectingMap.this.remove(lastKey);
                        // iterator re-initialised on next hasNext/next
                        activeIterator = null;
                    }
                }
                else
                {
                    ValueProtectingMap.this.remove(lastKey);
                }
                lastKey = null;
            }
            finally
            {
                writeLock.unlock();
            }
        }

        /**
         * Notifies iterator that a change has been performed outside of its direct context resulting in the change of
         * {@link ValueProtectingMap#mapFullyProtected}, necessitating a reset of the backing iterator.
         */
        protected void mapIndirectlyProtected()
        {
            activeIterator = null;
            lastKey = null;
        }
    }

    private class EntrySet extends AbstractCollection<Entry<K, V>> implements Set<Entry<K, V>>
    {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean contains(final Object o)
        {
            boolean contains;
            if (o instanceof Entry<?, ?>)
            {
                readLock.lock();
                try
                {
                    final Object key = ((Entry<?, ?>) o).getKey();
                    if (containsKey(key))
                    {
                        final V val = map.get(key);
                        contains = val != null && val.equals(((Entry<?, ?>) o).getValue());
                    }
                    else
                    {
                        contains = false;
                    }
                }
                finally
                {
                    readLock.unlock();
                }
            }
            else
            {
                contains = false;
            }
            return contains;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean remove(final Object o)
        {
            boolean removed;
            if (o instanceof Entry<?, ?>)
            {
                final Object key = ((Entry<?, ?>) o).getKey();
                final Object value = ((Entry<?, ?>) o).getValue();
                removed = ValueProtectingMap.this.remove(key, value);
            }
            else
            {
                removed = false;
            }
            return removed;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void clear()
        {
            ValueProtectingMap.this.clear();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Iterator<Entry<K, V>> iterator()
        {
            return new EntryIterator();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int size()
        {
            return ValueProtectingMap.this.size();
        }
    }

    private class EntryIterator implements Iterator<Entry<K, V>>
    {

        private final KeyIterator effectiveIterator = new KeyIterator();

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNext()
        {
            return effectiveIterator.hasNext();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Entry<K, V> next()
        {
            readLock.lock();
            try
            {
                final K key = effectiveIterator.next();
                final V value = map.get(key);
                return new VirtualEntry(effectiveIterator, key, value);
            }
            finally
            {
                readLock.unlock();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void remove()
        {
            effectiveIterator.remove();
        }
    }

    private class VirtualEntry extends AbstractMap.SimpleEntry<K, V>
    {

        private static final long serialVersionUID = 786079730660828197L;

        private final KeyIterator keyIterator;

        private VirtualEntry(KeyIterator keyIterator, final K key, final V value)
        {
            super(key, value);
            this.keyIterator = keyIterator;
        }

        /**
         *
         * {@inheritDoc}
         */
        @Override
        public V getValue()
        {
            V value = super.getValue();
            return !mapFullyProtected ? protectValue(value, instanceProtectorProvider) : value;
        }

        @Override
        public V setValue(final V value)
        {
            writeLock.lock();
            boolean protectedBefore = mapFullyProtected;
            try
            {
                return put(getKey(), value);
            }
            finally
            {
                if (protectedBefore != mapFullyProtected)
                {
                    keyIterator.mapIndirectlyProtected();
                }
                writeLock.unlock();
            }
        }
    }

    private class Values extends AbstractCollection<V>
    {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean contains(final Object o)
        {
            return ValueProtectingMap.this.containsValue(o);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void clear()
        {
            ValueProtectingMap.this.clear();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Iterator<V> iterator()
        {
            return new ValueIterator();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int size()
        {
            return ValueProtectingMap.this.size();
        }
    }

    private class ValueIterator implements Iterator<V>
    {

        private Entry<K, V> lastEntry;

        private final Iterator<Entry<K, V>> effectiveIterator = new EntryIterator();

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNext()
        {
            return effectiveIterator.hasNext();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public V next()
        {
            lastEntry = effectiveIterator.next();
            return lastEntry.getValue();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void remove()
        {
            effectiveIterator.remove();
            lastEntry = null;
        }
    }
}
