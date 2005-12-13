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
package org.alfresco.service.namespace;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.util.ApplicationContextHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A Map that holds as it's key a QName stored in it's internal String representation.
 * Calls to get and put automatically map the key to and from the QName representation.
 * 
 * @author gavinc
 */
public class QNameMap<K,V> implements Map, Cloneable
{
    protected static Log logger = LogFactory.getLog(QNameMap.class);
    protected Map<String, Object> contents = new HashMap<String, Object>(11, 1.0f);
    protected NamespacePrefixResolver resolver = null;
    
    /**
     * Constructor
     * 
     * @param resolver      Mandatory NamespacePrefixResolver helper
     */
    public QNameMap(NamespacePrefixResolver resolver)
    {
        if (resolver == null)
        {
            throw new IllegalArgumentException("NamespacePrefixResolver is mandatory.");
        }
        this.resolver = resolver;
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
        return (this.contents.containsKey(QName.resolveToQNameString(resolver, key.toString())));
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
        String qnameKey = QName.resolveToQNameString(resolver, key.toString());
        Object obj = this.contents.get(qnameKey);
        
        return obj;
    }
    
    /**
     * @see java.util.Map#put(K, V)
     */
    public Object put(Object key, Object value)
    {
        return this.contents.put(QName.resolveToQNameString(resolver, key.toString()), value);
    }
    
    /**
     * @see java.util.Map#remove(java.lang.Object)
     */
    public Object remove(Object key)
    {
        return this.contents.remove(QName.resolveToQNameString(resolver, key.toString()));
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
    public Set keySet()
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
        QNameMap map = new QNameMap(resolver);
        map.putAll(this);
        
        return map;
    }
}
