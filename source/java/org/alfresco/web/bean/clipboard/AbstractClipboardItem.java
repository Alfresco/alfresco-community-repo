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
package org.alfresco.web.bean.clipboard;

import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.search.QueryParameterDefImpl;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.Repository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class representing a single item added to the clipboard. 
 * 
 * @author Kevin Roast
 */
abstract class AbstractClipboardItem implements ClipboardItem
{
   protected static Log logger = LogFactory.getLog(ClipboardBean.class);
   
   protected static final String MSG_COPY_OF = "copy_of";
   
   /** Shallow search for nodes with a name pattern */
   private static final String XPATH_QUERY_NODE_MATCH = "./*[like(@cm:name, $cm:name, false)]";
   
   transient private ServiceRegistry services = null;
   
   protected NodeRef ref;
   protected NodeRef parent;
   protected ClipboardStatus mode;
   
   // cached values
   private String name;
   private QName type;
   private String icon;
   
   transient protected AVMService avmService;
   
   public void setAvmService(AVMService avmService)
   {
      this.avmService = avmService;
   }
   
   protected AVMService getAvmService()
   {
      if (avmService == null)
      {
         avmService = getServiceRegistry().getAVMLockingAwareService();
      }
      return avmService;
   }
   
   
   /**
    * Constructor
    * 
    * @param ref        The ref of the item on the clipboard
    * @param mode       The ClipboardStatus enum value
    */
   public AbstractClipboardItem(NodeRef ref, ClipboardStatus mode)
   {
      this.ref = ref;
      this.mode = mode;
   }
   
   /**
     * Constructor
     * 
     * @param ref The ref of the item on the clipboard
     * @param parent The parent of the item on the clipboard
     * @param mode The ClipboardStatus enum value
     */
   public AbstractClipboardItem(NodeRef ref, NodeRef parent, ClipboardStatus mode)
   {
       this.ref = ref;
       this.mode = mode;
       this.parent = parent;
   }
   
   public ClipboardStatus getMode()
   {
      return this.mode;
   }
   
   public String getName()
   {
      if (this.name == null)
      {
         this.name = (String)getServiceRegistry().getNodeService().getProperty(
               this.ref, ContentModel.PROP_NAME);
      }
      return this.name;
   }
   
   public QName getType()
   {
      if (this.type == null)
      {
         this.type = getServiceRegistry().getNodeService().getType(this.ref);
      }
      return this.type;
   }
   
   public String getIcon()
   {
      if (this.icon == null)
      {
         this.icon = (String)getServiceRegistry().getNodeService().getProperty(
               this.ref, ApplicationModel.PROP_ICON);
      }
      return this.icon;
   }
   
   public NodeRef getNodeRef()
   {
      return this.ref;
   }
   
   public NodeRef getParent()
   {
      return this.parent;
   }
   
   /**
    * Override equals() to compare NodeRefs
    */
   public boolean equals(Object obj)
   {
      if (obj == this)
      {
         return true;
      }
      if (obj instanceof ClipboardItem)
      {
         return ((ClipboardItem)obj).getNodeRef().equals(this.ref);
      }
      else
      {
         return false;
      }
   }
   
   /**
    * Override hashCode() to use the internal NodeRef hashcode instead
    */
   public int hashCode()
   {
      return ref.hashCode();
   }
   
   protected ServiceRegistry getServiceRegistry()
   {
      if (services == null)
      {
         services = Repository.getServiceRegistry(FacesContext.getCurrentInstance());
      }
      return services;
   }
   
   protected boolean checkExists(String name, NodeRef parent)
   {
      QueryParameterDefinition[] params = new QueryParameterDefinition[1];
      params[0] = new QueryParameterDefImpl(
            ContentModel.PROP_NAME,
            getServiceRegistry().getDictionaryService().getDataType(
                  DataTypeDefinition.TEXT),
                  true,
                  name);
      
      // execute the query
      List<NodeRef> nodeRefs = getServiceRegistry().getSearchService().selectNodes(
            parent,
            XPATH_QUERY_NODE_MATCH,
            params,
            getServiceRegistry().getNamespaceService(),
            false);
      
      return (nodeRefs.size() != 0);
   }
   
   protected void recursiveFormCheck(AVMNodeDescriptor desc)
   {
       if (desc.isFile())
       {
           String filePath = desc.getPath();
           if (avmService.hasAspect(-1, filePath, WCMAppModel.ASPECT_FORM_INSTANCE_DATA))
           {
               avmService.removeAspect(filePath, WCMAppModel.ASPECT_FORM_INSTANCE_DATA);
           }
           if (avmService.hasAspect(-1, filePath, WCMAppModel.ASPECT_RENDITION))
           {
               avmService.removeAspect(filePath, WCMAppModel.ASPECT_RENDITION);
           }
       }
       else
       {
           Map<String, AVMNodeDescriptor> listing = getAvmService().getDirectoryListing(desc);
           for (Map.Entry<String, AVMNodeDescriptor> entry : listing.entrySet())
           {
               recursiveFormCheck(entry.getValue());
           }
       }
   }
}
