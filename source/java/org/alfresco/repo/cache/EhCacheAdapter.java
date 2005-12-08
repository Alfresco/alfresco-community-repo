/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.cache;

import java.io.IOException;
import java.io.Serializable;

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
public class EhCacheAdapter<K extends Serializable, V extends Serializable>
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
            return (cache.get(key) != null);
        }
        catch (CacheException e)
        {
            throw new AlfrescoRuntimeException("contains failed", e);
        }
    }

    @SuppressWarnings("unchecked")
    public V get(K key)
    {
        try
        {
            Element element = cache.get(key);
            if (element != null)
            {
                return (V) element.getValue();
            }
            else
            {
                return null;
            }
        }
        catch (CacheException e)
        {
            throw new AlfrescoRuntimeException("Failed to get from EhCache: \n" +
                    "   key: " + key);
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
        try
        {
            cache.removeAll();
        }
        catch (IOException e)
        {
            throw new AlfrescoRuntimeException("Failed to clear cache", e);
        }
    }
}
