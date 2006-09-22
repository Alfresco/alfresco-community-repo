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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;

import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNameMap;
import org.alfresco.web.bean.repository.Repository;
import sun.security.krb5.internal.av;

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
   private boolean deleted = false;
   
   
   /**
    * Constructor
    */
   public AVMNode(AVMNodeDescriptor avmRef)
   {
      this.avmRef = avmRef;
      this.version = -1;      // TODO: always -1 for now...
      this.path = avmRef.getPath();
   }
   
   public AVMNode(AVMNodeDescriptor avmRef, boolean deleted)
   {
      this(avmRef);
      this.deleted = deleted;
   }
   
   public String getPath()
   {
      return this.path;
   }
   
   public int getVersion()
   {
      return this.version;
   }
   
   public String getName()
   {
      return this.avmRef.getName();
   }
   
   public NodeRef getNodeRef()
   {
      return AVMNodeConverter.ToNodeRef(this.version, this.path);
   }

   /**
    * @return All the properties known about this node.
    */
   public final Map<String, Object> getProperties()
   {
      if (this.properties == null)
      {
         this.properties = new QNameMap<String, Object>(getServiceRegistry().getNamespaceService());
         
         if (this.deleted == false)
         {
            Map<QName, PropertyValue> props = getServiceRegistry().getAVMService().getNodeProperties(this.version, this.path);
            for (QName qname: props.keySet())
            {
               PropertyValue propValue = props.get(qname);
               this.properties.put(qname.toString(), propValue.getSerializableValue());
            }
         }
         
         this.properties.put("id", this.path);
         this.properties.put("path", this.path);
         this.properties.put("size", this.avmRef.getLength());
         this.properties.put("name", this.avmRef.getName());
         this.properties.put("created", this.avmRef.getCreateDate());
         this.properties.put("modified", this.avmRef.getModDate());
         this.properties.put("creator", this.avmRef.getCreator());
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
      return getProperties().get(key);
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
