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
package org.alfresco.web.bean.wcm;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.WCMModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.web.scripts.FileTypeImageUtils;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.LayeringDescriptor;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.Path.Element;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;
import org.alfresco.wcm.asset.AssetInfo;
import org.alfresco.wcm.asset.AssetInfoImpl;
import org.alfresco.wcm.util.WCMUtil;
import org.alfresco.web.bean.BrowseBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.NodePropertyResolver;

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
   private static final long serialVersionUID = 2200295347489543757L;

   public final static NodePropertyResolver RESOLVER_PREVIEW_URL =
      new NodePropertyResolver()
      {
        private static final long serialVersionUID = -8437274476137672895L;

        public Object get(final Node node)
         {
            if (! (node instanceof AVMNode))
            {
               return null;
            }
            final String storeId   = AVMUtil.getStoreName(node.getPath());
            final String assetPath = AVMUtil.getStoreRelativePath(node.getPath());
            return AVMUtil.getPreviewURI(storeId, assetPath);
         }
      };

   public final static NodePropertyResolver RESOLVER_SANDBOX_RELATIVE_PATH =
      new NodePropertyResolver()
      {
        private static final long serialVersionUID = -2367701285830581225L;

        public Object get(final Node node)
         {
            if (! (node instanceof AVMNode))
            {
               return null;
            }
            String s = node.getPath();
            s = AVMUtil.getSandboxRelativePath(s);
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

                    @Override
                    public Element getBaseNameElement(TenantService tenantService)
                    {
                        return this;
                    }
                  });
               }
            }
            return result;
         }
      };

   public final static NodePropertyResolver RESOLVER_FILE_TYPE_16 =
      new NodePropertyResolver()
      {
        private static final long serialVersionUID = 4300079423348609858L;

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
               return FileTypeImageUtils.getFileTypeImage(node.getName(), true);
            }
         }
      };
   
   public final static NodePropertyResolver RESOLVER_DISPLAY_PATH =
      new NodePropertyResolver()
      {
         private static final long serialVersionUID = 368552730555134975L;

         public Object get(final Node node)
         {
            if (! (node instanceof AVMNode))
            {
               return null;
            }
            
            // the display path is the parent path to the node
            String parentPath = AVMNodeConverter.SplitBase(node.getPath())[0];
            return AVMUtil.getSandboxRelativePath(parentPath);
         }
      };

   public final static NodePropertyResolver RESOLVER_PARENT_PATH =
      new NodePropertyResolver()
      {
         private static final long serialVersionUID = -798036430912409497L;

         public Object get(final Node node)
         {
            if (! (node instanceof AVMNode))
            {
               return null;
            }
            
            return AVMNodeConverter.SplitBase(node.getPath())[0];
         }
      };
   
   private final AVMNodeDescriptor avmRef;
   private LayeringDescriptor layeringDesc;
   private final int version;
   private final boolean deleted;
   private Boolean workflowInFlight;

   public AVMNode(final AssetInfo asset)
   {
      super(AVMNodeConverter.ToNodeRef(-1, asset.getAvmPath()));
      
      // TODO - refactor !!
      this.avmRef = ((AssetInfoImpl)asset).getAVMNodeDescriptor();
      
      this.version = -1;      // TODO: always -1 for now...
      this.id = asset.getAvmPath();
      this.deleted = asset.isDeleted();
   }
   
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
      this.deleted = avmRef.isDeleted();
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
   
   @Override
   public QName getType()
   {
      if (this.type == null)
      {
         if (this.deleted == false)
         {
            this.type = getServiceRegistry().getNodeService().getType(this.nodeRef);
         }
         else
         {
            this.type = avmRef.isDeletedDirectory() ? WCMModel.TYPE_AVM_FOLDER : WCMModel.TYPE_AVM_CONTENT;
         }
      }
      
      return type;
   }
   
   public final boolean isDirectory()
   {
      return this.avmRef.isDirectory() || this.avmRef.isDeletedDirectory();
   }
   
   public final boolean isFile()
   {
      return this.avmRef.isFile() || this.avmRef.isDeletedFile();
   }

   public final boolean isDeleted()
   {
       return this.avmRef.isDeleted();
   }
   
   public final boolean isModified()
   {
      if (this.layeringDesc == null)
      {
         this.layeringDesc = getServiceRegistry().getAVMService().getLayeringInfo(this.version, this.id);
      }
      return !this.layeringDesc.isBackground();
   }
   
   public final boolean isInActiveWorkflow(String sandbox)
   {
      if (this.workflowInFlight == null)
      {
         if (!this.isModified())
         {
            this.workflowInFlight = false;
         }
         else
         {
            this.workflowInFlight = AVMWorkflowUtil.isInActiveWorkflow(sandbox, this.getDescriptor());
         }
      }
      return this.workflowInFlight;
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
         this.properties.put("deleted", this.deleted);
         
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

   /**
    * @see java.util.Map#values()
    */
   public Collection values()
   {
      return getProperties().values();
   }
}
