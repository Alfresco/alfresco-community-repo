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

import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.Node;

/**
 * Node class representing an AVM specific Node.
 * 
 * Handles AVM related notions such as Path and Version. Provides the usual properties and
 * property resolving functions, and appropriate method overrides for the AVM world.
 * 
 * @author Kevin Roast
 */
public class AVMNode extends Node implements Map<String, Object>
{
   private AVMNodeDescriptor avmRef;
   private String path;
   private int version;
   private boolean deleted = false;
   
   
   /**
    * Constructor
    * 
    * @param avmRef     The AVMNodeDescriptor that describes this node
    */
   public AVMNode(AVMNodeDescriptor avmRef)
   {
      super(AVMNodeConverter.ToNodeRef(-1, avmRef.getPath()));
      this.avmRef = avmRef;
      this.version = -1;      // TODO: always -1 for now...
      this.path = avmRef.getPath();
      this.id = this.path;
   }
   
   /**
    * Constructor
    * 
    * @param avmRef     The AVMNodeDescriptor that describes this node
    * @param deleted    True if the node represents a ghosted deleted node
    */
   public AVMNode(AVMNodeDescriptor avmRef, boolean deleted)
   {
      this(avmRef);
      this.deleted = deleted;
   }
   
   public final String getPath()
   {
      return this.path;
   }
   
   public final int getVersion()
   {
      return this.version;
   }
   
   public final String getName()
   {
      return this.avmRef.getName();
   }
   
   public final boolean isDirectory()
   {
      return this.avmRef.isDirectory();
   }
   
   public final boolean isFile()
   {
      return this.avmRef.isFile();
   }

   /**
    * @return All the properties known about this node.
    */
   public final Map<String, Object> getProperties()
   {
      if (this.propsRetrieved == false)
      {
         if (this.deleted == false)
         {
            Map<QName, PropertyValue> props = getServiceRegistry().getAVMService().getNodeProperties(this.version, this.path);
            for (QName qname: props.keySet())
            {
               PropertyValue propValue = props.get(qname);
               this.properties.put(qname.toString(), propValue.getValue(DataTypeDefinition.ANY));
            }
         }
         
         this.properties.put("id", this.path);
         this.properties.put("path", this.path);
         this.properties.put("size", this.avmRef.getLength());
         this.properties.put("name", this.avmRef.getName());
         this.properties.put("created", this.avmRef.getCreateDate());
         this.properties.put("modified", this.avmRef.getModDate());
         this.properties.put("creator", this.avmRef.getCreator());
         
         this.propsRetrieved = true;
      }
      
      return this.properties;
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
