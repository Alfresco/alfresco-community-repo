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
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

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
         
         propsInitialised = true;
      }
      
      return props.get(key);
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
    * @see java.util.Map#put(K, V)
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

   /**
    * @see java.util.Map#values()
    */
   public Collection values()
   {
      return getProperties().values();
   }
}
