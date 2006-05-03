/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Alfresco Network License. You may obtain a
 * copy of the License at
 *
 *   http://www.alfrescosoftware.com/legal/
 *
 * Please view the license relevant to your network subscription.
 *
 * BY CLICKING THE "I UNDERSTAND AND ACCEPT" BOX, OR INSTALLING,  
 * READING OR USING ALFRESCO'S Network SOFTWARE (THE "SOFTWARE"),  
 * YOU ARE AGREEING ON BEHALF OF THE ENTITY LICENSING THE SOFTWARE    
 * ("COMPANY") THAT COMPANY WILL BE BOUND BY AND IS BECOMING A PARTY TO 
 * THIS ALFRESCO NETWORK AGREEMENT ("AGREEMENT") AND THAT YOU HAVE THE   
 * AUTHORITY TO BIND COMPANY. IF COMPANY DOES NOT AGREE TO ALL OF THE   
 * TERMS OF THIS AGREEMENT, DO NOT SELECT THE "I UNDERSTAND AND AGREE"   
 * BOX AND DO NOT INSTALL THE SOFTWARE OR VIEW THE SOURCE CODE. COMPANY   
 * HAS NOT BECOME A LICENSEE OF, AND IS NOT AUTHORIZED TO USE THE    
 * SOFTWARE UNLESS AND UNTIL IT HAS AGREED TO BE BOUND BY THESE LICENSE  
 * TERMS. THE "EFFECTIVE DATE" FOR THIS AGREEMENT SHALL BE THE DAY YOU  
 * CHECK THE "I UNDERSTAND AND ACCEPT" BOX.
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
