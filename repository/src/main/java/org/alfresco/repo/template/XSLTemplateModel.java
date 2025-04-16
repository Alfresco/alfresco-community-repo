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
     *            Object
     * @return boolean
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key)
    {
        return wrappedMap.containsKey(key);
    }

    /**
     * @param value
     *            Object
     * @return boolean
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object value)
    {
        return wrappedMap.containsValue(value);
    }

    /**
     * @return Set
     * @see java.util.Map#entrySet()
     */
    public Set<Entry<QName, Object>> entrySet()
    {
        return wrappedMap.entrySet();
    }

    /**
     * @param o
     *            Object
     * @return boolean
     * @see java.util.Map#equals(java.lang.Object)
     */
    public boolean equals(Object o)
    {
        return wrappedMap.equals(o);
    }

    /**
     * @param key
     *            Object
     * @return Object
     * @see java.util.Map#get(java.lang.Object)
     */
    public Object get(Object key)
    {
        return wrappedMap.get(key);
    }

    /**
     * @return int
     * @see java.util.Map#hashCode()
     */
    public int hashCode()
    {
        return wrappedMap.hashCode();
    }

    /**
     * @return boolean
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty()
    {
        return wrappedMap.isEmpty();
    }

    /**
     * @see java.util.Map#keySet()
     */
    public Set<QName> keySet()
    {
        return wrappedMap.keySet();
    }

    /**
     * @param key
     *            Object
     * @param value
     *            Object
     * @return Object
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public Object put(QName key, Object value)
    {
        return wrappedMap.put(key, value);
    }

    /**
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map<? extends QName, ? extends Object> m)
    {
        wrappedMap.putAll(m);
    }

    /**
     * @param key
     *            Object
     * @return Object
     * @see java.util.Map#remove(java.lang.Object)
     */
    public Object remove(Object key)
    {
        return wrappedMap.remove(key);
    }

    /**
     * @return int
     * @see java.util.Map#size()
     */
    public int size()
    {
        return wrappedMap.size();
    }

    /**
     * @see java.util.Map#values()
     */
    public Collection<Object> values()
    {
        return wrappedMap.values();
    }

}
