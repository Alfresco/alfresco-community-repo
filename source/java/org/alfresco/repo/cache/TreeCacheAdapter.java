/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
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

import java.io.Serializable;

import org.alfresco.error.AlfrescoRuntimeException;
import org.jboss.cache.Fqn;
import org.jboss.cache.TreeCache;

/**
 * A thin adapter for <b>TreeCache</b> support.
 *
 * @author Derek Hulley
 */
public class TreeCacheAdapter<K extends Serializable, V extends Serializable>
        implements SimpleCache<K, V>
{
    private TreeCache cache;
    private Fqn regionFqn;
    
    public TreeCacheAdapter()
    {
    }

    /**
     * @param cache the backing Ehcache instance
     */
    public void setCache(TreeCache cache)
    {
        this.cache = cache;
    }

    /**
     * Set the uniquely named region of the cache within which all object must be cached
     * 
     * @param regionName the cache region
     */
    public void setRegionName(String regionName)
    {
        this.regionFqn = new Fqn(regionName);
    }

    public boolean contains(K key)
    {
        try
        {
            return cache.exists(regionFqn, key);
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException("contains failed", e);
        }
    }

    @SuppressWarnings("unchecked")
    public V get(K key)
    {
        try
        {
            Object element = cache.get(regionFqn, key);
            if (element != null)
            {
                return (V) element;
            }
            else
            {
                return null;
            }
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException("Failed to get from TreeCache: \n" +
                    "   key: " + key,
                    e);
        }
    }

    public void put(K key, V value)
    {
        try
        {
            cache.put(regionFqn, key, value);
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException("Failed to put into TreeCache: \n" +
                    "   key: " + key + "\n" +
                    "   value: " + value,
                    e);
        }
    }

    public void remove(K key)
    {
        try
        {
            cache.remove(regionFqn, key);
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException("Failed to remove from TreeCache: \n" +
                    "   key: " + key,
                    e);
        }
    }

    public void clear()
    {
        try
        {
            cache.remove(regionFqn);
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException("Failed to clear cache", e);
        }
    }
}
