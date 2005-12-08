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
package org.alfresco.web.bean.repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QNameMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A extension of the repo QNameMap to provide custom property resolving support for Node wrappers.
 * 
 * @author Kevin Roast
 */
public final class QNameNodeMap<K,V> extends QNameMap implements Map, Cloneable
{
   private Node parent = null;
   private Map<String, NodePropertyResolver> resolvers = new HashMap<String, NodePropertyResolver>(11, 1.0f);
   
   /**
    * Constructor
    * 
    * @param parent     Parent Node of the QNameNodeMap
    */
   public QNameNodeMap(NamespacePrefixResolver resolver, Node parent)
   {
      super(resolver);
      if (parent == null)
      {
         throw new IllegalArgumentException("Parent Node cannot be null!");
      }
      this.parent = parent;
   }
   
   /**
    * Register a property resolver for the named property.
    * 
    * @param name       Name of the property this resolver is for
    * @param resolver   Property resolver to register
    */
   public void addPropertyResolver(String name, NodePropertyResolver resolver)
   {
      this.resolvers.put(name, resolver);
   }

   /**
    * @see java.util.Map#containsKey(java.lang.Object)
    */
   public boolean containsKey(Object key)
   {
      return (this.contents.containsKey(Repository.resolveToQNameString((String)key)) ||
              this.resolvers.containsKey(key));
   }

   /**
    * @see java.util.Map#get(java.lang.Object)
    */
   public Object get(Object key)
   {
      String qnameKey = Repository.resolveToQNameString(key.toString());
      Object obj = this.contents.get(qnameKey);
      if (obj == null)
      {
         // if a property resolver exists for this property name then invoke it
         NodePropertyResolver resolver = this.resolvers.get(key.toString());
         if (resolver != null)
         {
            obj = resolver.get(this.parent);
            // cache the result
            // obviously the cache is useless if the result is null, in most cases it shouldn't be
            this.contents.put(qnameKey, obj);
         }
      }
      
      return obj;
   }
   
   /**
    * Perform a get without using property resolvers
    * 
    * @param key    item key
    * @return object
    */
   public Object getRaw(Object key)
   {
      return this.contents.get(Repository.resolveToQNameString((String)key));
   }
   
   /**
    * Shallow copy the map by copying keys and values into a new QNameNodeMap
    */
   public Object clone()
   {
      QNameNodeMap map = new QNameNodeMap(this.resolver, this.parent);
      map.putAll(this);
      if (this.resolvers.size() != 0)
      {
         map.resolvers = (Map)((HashMap)this.resolvers).clone();
      }
      return map;
   }
}
