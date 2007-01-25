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
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.BrowseBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.NodePropertyResolver;
import org.alfresco.web.config.ClientConfigElement;
import org.alfresco.web.ui.common.Utils;

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

   public final static NodePropertyResolver RESOLVER_PREVIEW_URL =
      new NodePropertyResolver()
      {
         public Object get(final Node node)
         {
            if (! (node instanceof AVMNode))
            {
               return null;
            }
            final ClientConfigElement config = 
               Application.getClientConfig(FacesContext.getCurrentInstance());
            final String dns = 
               AVMConstants.lookupStoreDNS(AVMConstants.getStoreName(node.getPath()));
            return AVMConstants.buildAssetUrl(AVMConstants.getSandboxRelativePath(node.getPath()),
                                              config.getWCMDomain(),
                                              config.getWCMPort(),
                                              dns);
         }
      };

   public final static NodePropertyResolver RESOLVER_SANDBOX_RELATIVE_PATH =
      new NodePropertyResolver()
      {
         public Object get(final Node node)
         {
            if (! (node instanceof AVMNode))
            {
               return null;
            }
            String s = node.getPath();
            s = AVMConstants.getSandboxRelativePath(s);
            final Path result = new Path();
            final String[] parts = s.split("/");
            for (int i = 1; i < parts.length; i++)
            {
               if (parts[i].length() != 0)
               {
                  final String s2 = parts[i];
                  result.append(new Path.Element() 
                  {
                     public String getElementString() { return s2; }
                  });
               }
            }
            return result;
         }
      };

   public final static NodePropertyResolver RESOLVER_FILE_TYPE_16 =
      new NodePropertyResolver()
      {
         public Object get(final Node node)
         {
            if (! (node instanceof AVMNode))
            {
               return null;
            }
            if (((AVMNode)node).isDirectory())
            {
               return "/images/icons/" + BrowseBean.SPACE_SMALL_DEFAULT + ".gif";
            }
            else
            {
               return Utils.getFileTypeImage(node.getName(), true);
            }
         }
      };


   private AVMNodeDescriptor avmRef;
   private int version;
   private boolean deleted = false;
   
   
   /**
    * Constructor
    * 
    * @param avmRef     The AVMNodeDescriptor that describes this node
    */
   public AVMNode(final AVMNodeDescriptor avmRef)
   {
      super(AVMNodeConverter.ToNodeRef(-1, avmRef.getPath()));
      this.avmRef = avmRef;
      this.version = -1;      // TODO: always -1 for now...
      this.id = avmRef.getPath();
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
   
   @Override
   public String getPath()
   {
      return this.avmRef.getPath();
   }
   
   public final AVMNodeDescriptor getDescriptor()
   {
      return avmRef;
   }
   
   public int getVersion()
   {
      return this.version;
   }
   
   @Override
   public String getName()
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
   public Map<String, Object> getProperties()
   {
      if (!this.propsRetrieved)
      {
         if (!this.deleted)
         {
            Map<QName, PropertyValue> props = getServiceRegistry().getAVMService().getNodeProperties(this.version, this.id);
            for (QName qname: props.keySet())
            {
               PropertyValue propValue = props.get(qname);
               this.properties.put(qname.toString(), propValue.getValue(DataTypeDefinition.ANY));
            }
         }
         
         this.properties.put("id", this.id);
         this.properties.put("nodeRef", this.nodeRef);
         this.properties.put("size", this.avmRef.getLength());
         this.properties.put("name", this.avmRef.getName());
         this.properties.put("created", this.avmRef.getCreateDate());
         this.properties.put("modified", this.avmRef.getModDate());
         this.properties.put("creator", this.avmRef.getCreator());
         this.properties.put("modifier", this.avmRef.getLastModifier());
         
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
