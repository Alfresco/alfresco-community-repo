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

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Element;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * A thin adapter for <b>Ehcache</b> support.
 * <p>
 * Thread-safety is taken care of by the underlying <b>Ehcache</b>
 * instance.
 *
 * @see org.springframework.cache.ehcache.EhCacheFactoryBean
 * @see org.springframework.cache.ehcache.EhCacheManagerFactoryBean
 * 
 * @author Derek Hulley
 */
public class EhCacheAdapter<K extends Serializable, V extends Object>
        implements SimpleCache<K, V>
{
    private net.sf.ehcache.Cache cache;
    
    public EhCacheAdapter()
    {
    }

    /**
     * @param cache the backing Ehcache instance
     */
    public void setCache(Cache cache)
    {
        this.cache = cache;
    }

    public boolean contains(K key)
    {
        try
        {
            return (cache.getQuiet(key) != null);
        }
        catch (CacheException e)
        {
            throw new AlfrescoRuntimeException("contains failed", e);
        }
    }

    @SuppressWarnings("unchecked")
    public Collection<K> getKeys()
    {
        return cache.getKeys();
    }

    @SuppressWarnings("unchecked")
    public V get(K key)
    {
        try
        {
            Element element = cache.get(key);
            if (element != null)
            {
                return (V) element.getObjectValue();
            }
            else
            {
                return null;
            }
        }
        catch (IllegalStateException ie)
        {
           throw new AlfrescoRuntimeException("Failed to get from EhCache as state invalid: \n" +
                 "  state: " + cache.getStatus() + "\n" +
                 "   key: " + key,
                 ie);
        }
        catch (CacheException e)
        {
            throw new AlfrescoRuntimeException("Failed to get from EhCache: \n" +
                    "   key: " + key,
                    e);
        }
    }

    public void put(K key, V value)
    {
        Element element = new Element(key, value);
        cache.put(element);
    }

    public void remove(K key)
    {
        cache.remove(key);
    }

    public void clear()
    {
        cache.removeAll();
    }
}
