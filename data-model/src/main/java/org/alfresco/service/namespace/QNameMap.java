/*
 * #%L
 * Alfresco Data model classes
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
package org.alfresco.service.namespace;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A Map that holds as it's key a QName stored in it's internal String representation.
 * Calls to get and put automatically map the key to and from the QName representation.
 * 
 * @author gavinc
 */
public class QNameMap<K,V> implements Map, Cloneable, Serializable
{
    private static final long serialVersionUID = -6578946123712939602L;
    
    protected static Log logger = LogFactory.getLog(QNameMap.class);
    protected Map<String, Object> contents = new HashMap<String, Object>(16, 1.0f);
    protected NamespacePrefixResolverProvider provider = null;
    
    
    /**
     * Constructor
     * 
     * @param provider      Mandatory NamespacePrefixResolverProvider helper
     */
    public QNameMap(NamespacePrefixResolverProvider provider)
    {
        if (provider == null)
        {
            throw new IllegalArgumentException("NamespacePrefixResolverProvider is mandatory.");
        }
        this.provider = provider;
    }
    
    /**
     * Constructor for Serialization mechanism
     */
    protected QNameMap()
    {
        super();
    }
    
    
    /**
     * Helper to return a NamespacePrefixResolver instance - should -always- be used
     * rather than holding onto a reference on the heap.
     * 
     * @return NamespacePrefixResolver
     */
    protected final NamespacePrefixResolver getResolver()
    {
        return this.provider.getNamespacePrefixResolver();
    }
    
    /**
     * @see java.util.Map#size()
     */
    public final int size()
    {
        return this.contents.size();
    }
    
    /**
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty()
    {
        return this.contents.isEmpty();
    }
    
    /**
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key)
    {
        return (this.contents.containsKey(QName.resolveToQNameString(getResolver(), key.toString())));
    }
    
    /**
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object value)
    {
        return this.contents.containsValue(value);
    }
    
    /**
     * @see java.util.Map#get(java.lang.Object)
     */
    public Object get(Object key)
    {
        String qnameKey = QName.resolveToQNameString(getResolver(), key.toString());
        Object obj = this.contents.get(qnameKey);
        
        return obj;
    }
    
    /**
     * @see java.util.Map#put(Object, Object)
     */
    public Object put(Object key, Object value)
    {
        return this.contents.put(QName.resolveToQNameString(getResolver(), key.toString()), value);
    }
    
    /**
     * @see java.util.Map#remove(java.lang.Object)
     */
    public Object remove(Object key)
    {
        return this.contents.remove(QName.resolveToQNameString(getResolver(), key.toString()));
    }
    
    /**
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map t)
    {
        for (Object key : t.keySet())
        {
            this.put(key, t.get(key));
        }
    }
    
    /**
     * @see java.util.Map#clear()
     */
    public void clear()
    {
        this.contents.clear();
    }
    
    /**
     * @see java.util.Map#keySet()
     */
    public Set<String> keySet()
    {
        return this.contents.keySet();
    }
    
    /**
     * @see java.util.Map#values()
     */
    public Collection values()
    {
        return this.contents.values();
    }
    
    /**
     * @see java.util.Map#entrySet()
     */
    public Set entrySet()
    {
        return this.contents.entrySet();
    }
    
    /**
     * Override Object.toString() to provide useful debug output
     */
    public String toString()
    {
        return this.contents.toString();
    }
    
    /**
     * Shallow copy the map by copying keys and values into a new QNameMap
     */
    public Object clone()
    {
        QNameMap<K, V> map = new QNameMap<K, V>(provider);
        map.putAll(this);
        
        return map;
    }
    
    @SuppressWarnings("unchecked")
    public Map<QName, V> getMapOfQNames()
    {
        HashMap<QName, V> map = new HashMap<QName, V>(contents.size());
        for (Entry<String, Object> entry : contents.entrySet())
        {
            QName key = QName.createQName(entry.getKey());
            map.put(key, (V)entry.getValue());
        }
        return map;
    }

}
