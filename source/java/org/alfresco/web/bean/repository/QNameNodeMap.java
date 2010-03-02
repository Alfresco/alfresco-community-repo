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
package org.alfresco.web.bean.repository;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.namespace.NamespacePrefixResolverProvider;
import org.alfresco.service.namespace.QNameMap;

/**
 * A extension of the repo QNameMap to provide custom property resolving support for Node wrappers.
 * 
 * @author Kevin Roast
 */
public final class QNameNodeMap<K, V> extends QNameMap implements Map, Cloneable, Serializable
{
   private static final long serialVersionUID = -1760755862411509263L;
   
   private Node parent = null;
   private Map<String, NodePropertyResolver> resolvers = new HashMap<String, NodePropertyResolver>(8, 1.0f);
   
   
   /**
    * Constructor
    * 
    * @param parent     Parent Node of the QNameNodeMap
    */
   public QNameNodeMap(NamespacePrefixResolverProvider provider, Node parent)
   {
      super(provider);
      if (parent == null)
      {
         throw new IllegalArgumentException("Parent Node cannot be null!");
      }
      this.parent = parent;
   }
   
   /**
    * Serialization constructor
    */
   protected QNameNodeMap()
   {
      super();
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
    * Returns if a property resolver with a specific name has been applied to the map
    *  
    * @param name of property resolver to look for
    * 
    * @return true if a resolver with the name is found, false otherwise
    */
   public boolean containsPropertyResolver(String name)
   {
      return this.resolvers.containsKey(name);
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
   @SuppressWarnings("unchecked")
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
   @SuppressWarnings("unchecked")
   public Object clone()
   {
      QNameNodeMap map = new QNameNodeMap(this.provider, this.parent);
      map.putAll(this);
      if (this.resolvers.size() != 0)
      {
         map.resolvers = (Map)((HashMap)this.resolvers).clone();
      }
      return map;
   }
}
