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
package org.alfresco.repo.template;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.namespace.QName;

public class XSLTemplateModel implements Map<QName, Object>
{
    private Map<QName, Object> wrappedMap = new HashMap<QName, Object>();

    /**
     * 
     * @see java.util.Map#clear()
     */
    public void clear()
    {
        wrappedMap.clear();
    }

    /**
     * @param key
     * @return
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key)
    {
        return wrappedMap.containsKey(key);
    }

    /**
     * @param value
     * @return
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object value)
    {
        return wrappedMap.containsValue(value);
    }

    /**
     * @return
     * @see java.util.Map#entrySet()
     */
    public Set<Entry<QName, Object>> entrySet()
    {
        return wrappedMap.entrySet();
    }

    /**
     * @param o
     * @return
     * @see java.util.Map#equals(java.lang.Object)
     */
    public boolean equals(Object o)
    {
        return wrappedMap.equals(o);
    }

    /**
     * @param key
     * @return
     * @see java.util.Map#get(java.lang.Object)
     */
    public Object get(Object key)
    {
        return wrappedMap.get(key);
    }

    /**
     * @return
     * @see java.util.Map#hashCode()
     */
    public int hashCode()
    {
        return wrappedMap.hashCode();
    }

    /**
     * @return
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty()
    {
        return wrappedMap.isEmpty();
    }

    /**
     * @return
     * @see java.util.Map#keySet()
     */
    public Set<QName> keySet()
    {
        return wrappedMap.keySet();
    }

    /**
     * @param key
     * @param value
     * @return
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public Object put(QName key, Object value)
    {
        return wrappedMap.put(key, value);
    }

    /**
     * @param m
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map<? extends QName, ? extends Object> m)
    {
        wrappedMap.putAll(m);
    }

    /**
     * @param key
     * @return
     * @see java.util.Map#remove(java.lang.Object)
     */
    public Object remove(Object key)
    {
        return wrappedMap.remove(key);
    }

    /**
     * @return
     * @see java.util.Map#size()
     */
    public int size()
    {
        return wrappedMap.size();
    }

    /**
     * @return
     * @see java.util.Map#values()
     */
    public Collection<Object> values()
    {
        return wrappedMap.values();
    }
    
    
}
