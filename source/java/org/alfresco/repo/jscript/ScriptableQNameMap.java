package org.alfresco.repo.jscript;

import org.alfresco.service.namespace.NamespacePrefixResolverProvider;
import org.alfresco.service.namespace.QNameMap;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 * @author Kevin Roast
 */
public class ScriptableQNameMap<K,V> extends QNameMap<K,V> implements Scriptable
{
    /**
     * @param resolver NamespacePrefixResolverProvider
     */
    public ScriptableQNameMap(NamespacePrefixResolverProvider resolver)
    {
        super(resolver);
    }

    /**
     * @see org.mozilla.javascript.Scriptable#getClassName()
     */
    public String getClassName()
    {
        return "ScriptableQNameMap";
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
        else if ("hasOwnProperty".equals(name))
        {
            return new Callable()
            {
                @Override
                public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args)
                {
                    return (args.length > 0 ? hasOwnProperty(args[0]) : null);
                }
            };
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
     * ECMAScript 5 hasOwnProperty method support.
     * 
     * @param key   Object key to test for
     * @return true if found, false otherwise
     */
    public boolean hasOwnProperty(Object key)
    {
        return containsKey(key);
    }

    /**
     * @see org.mozilla.javascript.Scriptable#has(java.lang.String, org.mozilla.javascript.Scriptable)
     */
    public boolean has(String name, Scriptable start)
    {
        // locate the property in the underlying QName map
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
        return keySet().toArray();
    }

    /**
     * @see org.mozilla.javascript.Scriptable#getDefaultValue(java.lang.Class)
     */
    public Object getDefaultValue(@SuppressWarnings("rawtypes") Class hint)
    {
        return toString();
    }

    /**
     * @see org.mozilla.javascript.Scriptable#hasInstance(org.mozilla.javascript.Scriptable)
     */
    public boolean hasInstance(Scriptable instance)
    {
        return instance instanceof ScriptableQNameMap;
    }
}
