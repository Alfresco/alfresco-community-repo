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
package org.alfresco.web.bean.wcm;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.faces.context.FacesContext;

import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNameMap;
import org.alfresco.web.bean.repository.NodePropertyResolver;
import org.alfresco.web.bean.repository.QNameNodeMap;
import org.alfresco.web.bean.repository.Repository;

/**
 * @author Kevin Roast
 */
public class AVMNode implements Map<String, Object>
{
   private QNameMap<String, Object> properties = null;
   private ServiceRegistry services = null;
   private AVMNodeDescriptor avmRef;
   private String path;
   private int version;
   
   
   /**
    * Constructor
    */
   public AVMNode(AVMNodeDescriptor avmRef)
   {
      this.avmRef = avmRef;
      this.version = -1;      // TODO: why does avmNode.getVersionID() return 1?
      this.path = avmRef.getPath();
      
      getProperties();
   }

   /**
    * @return All the properties known about this node.
    */
   public final Map<String, Object> getProperties()
   {
      if (this.properties == null)
      {
         Map<QName, PropertyValue> props = getServiceRegistry().getAVMService().getNodeProperties(this.version, this.path);
         
         this.properties = new QNameMap<String, Object>(getServiceRegistry().getNamespaceService());
         for (QName qname: props.keySet())
         {
            PropertyValue propValue = props.get(qname);
            this.properties.put(qname.toString(), propValue.getSerializableValue());
         }
         
         this.properties.put("id", this.path);
         this.properties.put("path", this.path);
         this.properties.put("size", this.avmRef.getLength());
         this.properties.put("name", this.avmRef.getName());
         this.properties.put("created", this.avmRef.getCreateDate());
         this.properties.put("modified", this.avmRef.getModDate());
      }
      
      return this.properties;
   }
   
   /**
    * Determines whether the given property name is held by this node 
    * 
    * @param propertyName Property to test existence of
    * @return true if property exists, false otherwise
    */
   public final boolean hasProperty(String propertyName)
   {
      return getProperties().containsKey(propertyName);
   }
   
   private ServiceRegistry getServiceRegistry()
   {
      if (this.services == null)
      {
          this.services = Repository.getServiceRegistry(FacesContext.getCurrentInstance());
      }
      return this.services;
   }
   
   
   // ------------------------------------------------------------------------------------
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
      /*if (propsInitialised == false)
      {
         // well known properties required as publically accessable map attributes
         props.put("id", this.getId());
         props.put("name", this.getName());     // TODO: perf test pulling back single prop here instead of all!
         props.put("nodeRef", this.getNodeRef());
         
         propsInitialised = true;
      }*/
      
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
