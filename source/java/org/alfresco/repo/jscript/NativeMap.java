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
package org.alfresco.repo.jscript;

import java.util.Iterator;
import java.util.Map;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Wrapper;


/**
 * Wrapper for exposing maps in Rhino scripts. 
 * 
 * @author davidc
 */
public class NativeMap implements Scriptable, Wrapper
{
    private static final long serialVersionUID = 3664761893203964569L;
    
    private Map<Object, Object> map;
    private Scriptable parentScope;
    private Scriptable prototype;

    
    /**
     * Construct
     * 
     * @param scope
     * @param map
     * @return  native map
     */
    public static NativeMap wrap(Scriptable scope, Map<Object, Object> map)
    {
        return new NativeMap(scope, map);
    }

    /**
     * Construct
     * 
     * @param scope
     * @param map
     */
    public NativeMap(Scriptable scope, Map<Object, Object> map)
    {
        this.parentScope = scope;
        this.map = map;
    }

    /* (non-Javadoc)
     * @see org.mozilla.javascript.Wrapper#unwrap()
     */
    public Object unwrap()
    {
        return map;
    }

    /* (non-Javadoc)
     * @see org.mozilla.javascript.Scriptable#getClassName()
     */
    public String getClassName()
    {
        return "NativeMap";
    }

    /* (non-Javadoc)
     * @see org.mozilla.javascript.Scriptable#get(java.lang.String, org.mozilla.javascript.Scriptable)
     */
    public Object get(String name, Scriptable start)
    {
        // get the property from the underlying QName map
        if ("length".equals(name))
        {
            return map.size();
        }
        else
        {
            return map.get(name);
        }
    }

    /* (non-Javadoc)
     * @see org.mozilla.javascript.Scriptable#get(int, org.mozilla.javascript.Scriptable)
     */
    public Object get(int index, Scriptable start)
    {
        Object value =  null;
        int i=0;
        Iterator itrValues = map.values().iterator();
        while (i++ <= index && itrValues.hasNext())
        {
            value = itrValues.next();
        }
        return value;
    }

    /* (non-Javadoc)
     * @see org.mozilla.javascript.Scriptable#has(java.lang.String, org.mozilla.javascript.Scriptable)
     */
    public boolean has(String name, Scriptable start)
    {
        // locate the property in the underlying map
        return map.containsKey(name);
    }

    /* (non-Javadoc)
     * @see org.mozilla.javascript.Scriptable#has(int, org.mozilla.javascript.Scriptable)
     */
    public boolean has(int index, Scriptable start)
    {
        return (index >= 0 && map.values().size() > index);
    }

    /* (non-Javadoc)
     * @see org.mozilla.javascript.Scriptable#put(java.lang.String, org.mozilla.javascript.Scriptable, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public void put(String name, Scriptable start, Object value)
    {
        map.put(name, value);
    }

    /* (non-Javadoc)
     * @see org.mozilla.javascript.Scriptable#put(int, org.mozilla.javascript.Scriptable, java.lang.Object)
     */
    public void put(int index, Scriptable start, Object value)
    {
        // TODO: implement?
    }

    /* (non-Javadoc)
     * @see org.mozilla.javascript.Scriptable#delete(java.lang.String)
     */
    public void delete(String name)
    {
        map.remove(name);
    }

    /* (non-Javadoc)
     * @see org.mozilla.javascript.Scriptable#delete(int)
     */
    public void delete(int index)
    {
        int i=0;
        Iterator itrKeys = map.keySet().iterator();
        while (i <= index && itrKeys.hasNext())
        {
            Object key = itrKeys.next();
            if (i == index)
            {
                map.remove(key);
                break;
            }
        }
    }

    /* (non-Javadoc)
     * @see org.mozilla.javascript.Scriptable#getPrototype()
     */
    public Scriptable getPrototype()
    {
        return this.prototype;
    }

    /* (non-Javadoc)
     * @see org.mozilla.javascript.Scriptable#setPrototype(org.mozilla.javascript.Scriptable)
     */
    public void setPrototype(Scriptable prototype)
    {
        this.prototype = prototype;
    }

    /* (non-Javadoc)
     * @see org.mozilla.javascript.Scriptable#getParentScope()
     */
    public Scriptable getParentScope()
    {
        return this.parentScope;
    }

    /* (non-Javadoc)
     * @see org.mozilla.javascript.Scriptable#setParentScope(org.mozilla.javascript.Scriptable)
     */
    public void setParentScope(Scriptable parent)
    {
        this.parentScope = parent;
    }

    /* (non-Javadoc)
     * @see org.mozilla.javascript.Scriptable#getIds()
     */
    public Object[] getIds()
    {
        return map.keySet().toArray();
    }

    /* (non-Javadoc)
     * @see org.mozilla.javascript.Scriptable#getDefaultValue(java.lang.Class)
     */
    public Object getDefaultValue(@SuppressWarnings("rawtypes") Class hint)
    {
        return String.valueOf(map);
    }

    /* (non-Javadoc)
     * @see org.mozilla.javascript.Scriptable#hasInstance(org.mozilla.javascript.Scriptable)
     */
    public boolean hasInstance(Scriptable value)
    {
        if (!(value instanceof Wrapper))
            return false;
        Object instance = ((Wrapper)value).unwrap();
        return Map.class.isInstance(instance);
    }

}
