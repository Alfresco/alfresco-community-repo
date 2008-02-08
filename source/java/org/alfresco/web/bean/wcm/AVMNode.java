/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.bean.wcm;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.faces.context.FacesContext;

import org.alfresco.model.WCMModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.avm.AVMNodeType;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.LayeringDescriptor;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
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
               AVMUtil.lookupStoreDNS(AVMUtil.getStoreName(node.getPath()));
            return AVMUtil.buildAssetUrl(AVMUtil.getSandboxRelativePath(node.getPath()),
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
   
   public final static NodePropertyResolver RESOLVER_DISPLAY_PATH =
      new NodePropertyResolver()
      {
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
   private WebProject webProject;
   private Boolean workflowInFlight;

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

   public final boolean isModified()
   {
      if (this.layeringDesc == null)
      {
         this.layeringDesc = getServiceRegistry().getAVMService().getLayeringInfo(this.version, this.id);
      }
      return !this.layeringDesc.isBackground();
   }

   public final boolean isWorkflowInFlight()
   {
      if (this.workflowInFlight == null)
      {
         if (!this.isModified())
         {
            this.workflowInFlight = false;
         }
         else
         {
            // optimization to avoid having to perform a workflow query and multiple lookups
            // per workflow sandbox.  only accurate for files, not new directories
            if (!this.isDirectory())
            {
               this.workflowInFlight = false;
               final List<Pair<Integer, String>> headPaths = this.getServiceRegistry().getAVMService().getHeadPaths(this.getDescriptor());
               for (final Pair<Integer, String> headPath : headPaths)
               {
                  if (AVMUtil.isWorkflowStore(AVMUtil.getStoreName(headPath.getSecond())))
                  {
                     this.workflowInFlight = true;
                     break;
                  }
               }
            }
            else
            {
               this.workflowInFlight = AVMWorkflowUtil.getAssociatedTasksForNode(this.getDescriptor()).size() != 0;
            }
         }
      }
      return this.workflowInFlight;
   }

   public WebProject getWebProject()
   {
      if (this.webProject == null)
      {
         this.webProject = new WebProject(this.id);
      }
      return this.webProject;
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
