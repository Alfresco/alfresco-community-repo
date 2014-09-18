/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;

import org.springframework.beans.factory.BeanNameAware;

/**
 * {@link SimpleCache} implementation backed by a {@link DefaultSimpleCache} but forcing
 * object serialization in and out.  This is only useful for tests.
 * 
 * @author Derek Hulley
 * @since 4.2.3
 */
public final class SerializingSimpleCache<K extends Serializable, V extends Serializable>
    implements SimpleCache<K, V>, BeanNameAware
{
    private SimpleCache<K, V> cache;
    
    public SerializingSimpleCache(int maxItems, String cacheName)
    {
        cache = new DefaultSimpleCache<>(maxItems, cacheName);
    }
    
    public SerializingSimpleCache()
    {
        cache = new DefaultSimpleCache<>();
    }
    
    @SuppressWarnings("unchecked")
    private V serialize(V value)
    {
        if (value == null)
        {
            return null;
        }
        try
        {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(value);
            byte[] bytes = bos.toByteArray();
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            V ret = (V) ois.readObject();
            // This is just test code!
            bos.close();
            oos.close();
            bis.close();
            ois.close();
            // Done
            return ret;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failure to serialize/deserialize object: " + value, e);
        }
    }
    
    @Override
    public boolean contains(K key)
    {
        return cache.contains(key);
    }

    @Override
    public Collection<K> getKeys()
    {
        return cache.getKeys();
    }

    @Override
    public V get(K key)
    {
        V ret = cache.get(key);
        return serialize(ret);
    }

    @Override
    public void put(K key, V value)
    {
        value = serialize(value);
        cache.put(key, value);
    }

    @Override
    public void remove(K key)
    {
        cache.remove(key);
    }

    @Override
    public void clear()
    {
        cache.clear();
    }

    @Override
    public void setBeanName(String cacheName)
    {
    }
}
