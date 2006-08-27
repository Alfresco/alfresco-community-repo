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
package org.alfresco.repo.jscript;

import java.util.Iterator;
import java.util.LinkedHashMap;

import org.mozilla.javascript.Scriptable;

/**
 * @author Kevin Roast
 */
public class ScriptableHashMap<K,V> extends LinkedHashMap<K, V> implements Scriptable
{
    private static final long serialVersionUID = 3664761893203964569L;
    
    private Scriptable parentScope;
    private Scriptable prototype;
    
    /**
     * @see org.mozilla.javascript.Scriptable#getClassName()
     */
    public String getClassName()
    {
        return "ScriptableHashMap";
    }

    /**
     * @see org.mozilla.javascript.Scriptable#get(java.lang.String, org.mozilla.javascript.Scriptable)
     */
    public Object get(String name, Scriptable start)
    {
        // get the property from the underlying QName map
        if ("length".equals(name))
        {
            return this.size();
        }
        else
        {
            return get(name);
        }
    }

    /**
     * @see org.mozilla.javascript.Scriptable#get(int, org.mozilla.javascript.Scriptable)
     */
    public Object get(int index, Scriptable start)
    {
        Object value =  null;
        int i=0;
        Iterator itrValues = this.values().iterator();
        while (i++ <= index && itrValues.hasNext())
        {
            value = itrValues.next();
        }
        return value;
    }

    /**
     * @see org.mozilla.javascript.Scriptable#has(java.lang.String, org.mozilla.javascript.Scriptable)
     */
    public boolean has(String name, Scriptable start)
    {
        // locate the property in the underlying map
        return containsKey(name);
    }

    /**
     * @see org.mozilla.javascript.Scriptable#has(int, org.mozilla.javascript.Scriptable)
     */
    public boolean has(int index, Scriptable start)
    {
        return (index >= 0 && this.values().size() > index);
    }

    /**
     * @see org.mozilla.javascript.Scriptable#put(java.lang.String, org.mozilla.javascript.Scriptable, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public void put(String name, Scriptable start, Object value)
    {
        // add the property to the underlying QName map
        put((K)name, (V)value);
    }

    /**
     * @see org.mozilla.javascript.Scriptable#put(int, org.mozilla.javascript.Scriptable, java.lang.Object)
     */
    public void put(int index, Scriptable start, Object value)
    {
        // TODO: implement?
    }

    /**
     * @see org.mozilla.javascript.Scriptable#delete(java.lang.String)
     */
    public void delete(String name)
    {
        // remove the property from the underlying QName map
        remove(name);
    }

    /**
     * @see org.mozilla.javascript.Scriptable#delete(int)
     */
    public void delete(int index)
    {
        int i=0;
        Iterator itrKeys = this.keySet().iterator();
        while (i <= index && itrKeys.hasNext())
        {
            Object key = itrKeys.next();
            if (i == index)
            {
                remove(key);
                break;
            }
        }
    }

    /**
     * @see org.mozilla.javascript.Scriptable#getPrototype()
     */
    public Scriptable getPrototype()
    {
        return this.prototype;
    }

    /**
     * @see org.mozilla.javascript.Scriptable#setPrototype(org.mozilla.javascript.Scriptable)
     */
    public void setPrototype(Scriptable prototype)
    {
        this.prototype = prototype;
    }

    /**
     * @see org.mozilla.javascript.Scriptable#getParentScope()
     */
    public Scriptable getParentScope()
    {
        return this.parentScope;
    }

    /**
     * @see org.mozilla.javascript.Scriptable#setParentScope(org.mozilla.javascript.Scriptable)
     */
    public void setParentScope(Scriptable parent)
    {
        this.parentScope = parent;
    }

    /**
     * @see org.mozilla.javascript.Scriptable#getIds()
     */
    public Object[] getIds()
    {
        return keySet().toArray();
    }

    /**
     * @see org.mozilla.javascript.Scriptable#getDefaultValue(java.lang.Class)
     */
    public Object getDefaultValue(Class hint)
    {
        return null;
    }

    /**
     * @see org.mozilla.javascript.Scriptable#hasInstance(org.mozilla.javascript.Scriptable)
     */
    public boolean hasInstance(Scriptable instance)
    {
        return instance instanceof ScriptableHashMap;
    }
}
