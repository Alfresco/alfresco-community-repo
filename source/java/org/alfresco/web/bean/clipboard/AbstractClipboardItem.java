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
package org.alfresco.web.bean.clipboard;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import javax.faces.context.FacesContext;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.QueryParameterDefImpl;
import org.alfresco.service.ServiceRegistry;
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
   protected ClipboardStatus mode;
   
   // cached values
   private String name;
   private QName type;
   private String icon;
   
   
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
   
}
