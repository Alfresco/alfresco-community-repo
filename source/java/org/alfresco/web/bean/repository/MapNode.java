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
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Lighweight client side representation of a node held in the repository, which
 * is modelled as a map for use in the data tables. 
 * 
 * @author gavinc
 */
public class MapNode extends Node implements Map<String, Object>
{
   private static final long serialVersionUID = 4051322327734433079L;
   
   private boolean propsInitialised = false;
   
   
   /**
    * Constructor
    * 
    * @param nodeRef        The NodeRef this Node wrapper represents
    */
   public MapNode(NodeRef nodeRef)
   {
      super(nodeRef);
   }
   
   /**
    * Constructor
    * 
    * @param nodeRef        The NodeRef this Node wrapper represents
    * @param nodeService    The node service to use to retrieve data for this node
    * @param initProps      True to immediately init the properties of the node, false to do nothing
    */
   public MapNode(NodeRef nodeRef, NodeService nodeService, boolean initProps)
   {
      super(nodeRef);
      if (initProps == true)
      {
         getProperties();
      }
   }
   
   /**
    * Constructor
    * 
    * @param nodeRef        The NodeRef this Node wrapper represents
    * @param nodeService    The node service to use to retrieve data for this node
    * @param props          The properties of the node, already retrieved from NodeService
    */
   public MapNode(NodeRef nodeRef, NodeService nodeService, Map<QName, Serializable> props)
   {
      super(nodeRef);
      
      for (QName qname: props.keySet())
      {
         Serializable propValue = props.get(qname);
         this.properties.put(qname.toString(), propValue);
      }
      this.propsRetrieved = true;
   }
   
   
   // ------------------------------------------------------------------------------
   // Map implementation - allows the Node bean to be accessed using JSF expression syntax 
   
   /**
    * @see java.util.Map#clear()
    */
   public void clear()
   {
      getProperties().clear();
   }

   /**
    * @see java.util.Map#containsKey(java.lang.Object)
    */
   public boolean containsKey(Object key)
   {
      return getProperties().containsKey(key);
   }

   /**
    * @see java.util.Map#containsValue(java.lang.Object)
    */
   public boolean containsValue(Object value)
   {
      return getProperties().containsKey(value);
   }

   /**
    * @see java.util.Map#entrySet()
    */
   public Set entrySet()
   {
      return getProperties().entrySet();
   }

   /**
    * @see java.util.Map#get(java.lang.Object)
    */
   public Object get(Object key)
   {
      Object obj = null;
      
      // there are some things that aren't available as properties
      // but from method calls, so for these handle them individually
      Map<String, Object> props = getProperties();
      if (propsInitialised == false)
      {
         // well known properties required as publically accessable map attributes
         props.put("id", this.getId());
         props.put("name", this.getName());     // TODO: perf test pulling back single prop here instead of all!
         props.put("nodeRef", this.getNodeRef());
         props.put("nodeRefAsString", this.getNodeRefAsString());
         
         propsInitialised = true;
      }
      
      if (key.equals("properties"))
      {
         return props;
      }
      else
      {
         return props.get(key);
      }
   }

   /**
    * @see java.util.Map#isEmpty()
    */
   public boolean isEmpty()
   {
      return getProperties().isEmpty();
   }

   /**
    * @see java.util.Map#keySet()
    */
   public Set keySet()
   {
      return getProperties().keySet();
   }

   /**
    * @see java.util.Map#put(java.lang.Object, java.lang.Object)
    */
   public Object put(String key, Object value)
   {
      return getProperties().put(key, value);
   }

   /**
    * @see java.util.Map#putAll(java.util.Map)
    */
   public void putAll(Map t)
   {
      getProperties().putAll(t);
   }

   /**
    * @see java.util.Map#remove(java.lang.Object)
    */
   public Object remove(Object key)
   {
      return getProperties().remove(key);
   }

   /**
    * @see java.util.Map#size()
    */
   public int size()
   {
      return getProperties().size();
   }
   
   @Override
   public void reset() 
   {
	  super.reset();
	  propsInitialised = false;
   }

   /**
    * @see java.util.Map#values()
    */
   public Collection values()
   {
      return getProperties().values();
   }
}
