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

import java.util.HashMap;

import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QNameMap;
import org.mozilla.javascript.Scriptable;

/**
 * @author Kevin Roast
 */
public class ScriptableHashMap<K,V> extends HashMap implements Scriptable
{
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
        return null;
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
        return false;
    }

    /**
     * @see org.mozilla.javascript.Scriptable#put(java.lang.String, org.mozilla.javascript.Scriptable, java.lang.Object)
     */
    public void put(String name, Scriptable start, Object value)
    {
        // add the property to the underlying QName map
        put(name, value);
    }

    /**
     * @see org.mozilla.javascript.Scriptable#put(int, org.mozilla.javascript.Scriptable, java.lang.Object)
     */
    public void put(int index, Scriptable start, Object value)
    {
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
    }

    /**
     * @see org.mozilla.javascript.Scriptable#getPrototype()
     */
    public Scriptable getPrototype()
    {
        return null;
    }

    /**
     * @see org.mozilla.javascript.Scriptable#setPrototype(org.mozilla.javascript.Scriptable)
     */
    public void setPrototype(Scriptable prototype)
    {
    }

    /**
     * @see org.mozilla.javascript.Scriptable#getParentScope()
     */
    public Scriptable getParentScope()
    {
        return null;
    }

    /**
     * @see org.mozilla.javascript.Scriptable#setParentScope(org.mozilla.javascript.Scriptable)
     */
    public void setParentScope(Scriptable parent)
    {
    }

    /**
     * @see org.mozilla.javascript.Scriptable#getIds()
     */
    public Object[] getIds()
    {
        return null;
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
