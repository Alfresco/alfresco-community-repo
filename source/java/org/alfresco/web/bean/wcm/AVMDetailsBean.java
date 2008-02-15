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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.dialog.NavigationSupport;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.common.component.UIPanel.ExpandedEvent;

/**
 * Base backing bean provided access to the details of an AVM item
 * 
 * @author Kevin Roast
 */
public abstract class AVMDetailsBean extends BaseDialogBean implements NavigationSupport
{
   private static final long serialVersionUID = -4895328117656471680L;

   /** NodeService bean reference */
   transient private NodeService nodeService;
   
   /** AVM service bean reference */
   transient private AVMService avmService;
   
   /** AVMBrowseBean bean reference */
   protected AVMBrowseBean avmBrowseBean;
   
   protected Map<String, Boolean> panels = new HashMap<String, Boolean>(4, 1.0f);
   
   
   // ------------------------------------------------------------------------------
   // Bean property getters and setters 
   
   /**
    * @param nodeService      The NodeService to set
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }
   
   protected NodeService getNodeService()
   {
      if (nodeService == null)
      {
         nodeService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getNodeService();
      }
      return nodeService;
   }
   
   /**
    * @param avmService       The AVMService to set.
    */
   public void setAvmService(AVMService avmService)
   {
      this.avmService = avmService;
   }
   
   protected AVMService getAvmService()
   {
      if (avmService == null)
      {
         avmService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getAVMService();
      }
      return avmService;
   }
   
   /**
    * @param avmBrowseBean    The AVMBrowseBean to set.
    */
   public void setAvmBrowseBean(AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
   }

   /**
    * @return Returns the panels expanded state map.
    */
   public Map<String, Boolean> getPanels()
   {
      return this.panels;
   }

   /**
    * @param panels The panels expanded state map.
    */
   public void setPanels(Map<String, Boolean> panels)
   {
      this.panels = panels;
   }
   
   /**
    * @return the AVM node to display the details for
    */
   public abstract AVMNode getAvmNode();
   
   /**
    * Returns the Path of the current node
    * 
    * @return The path
    */
   public String getPath()
   {
      return getAvmNode().getPath();
   }
   
   /**
    * Returns the name of the current node
    * 
    * @return Name of the current node
    */
   public String getName()
   {
      return getAvmNode().getName();
   }
   
   /**
    * Return the Alfresco NodeRef URL for the current node
    * 
    * @return the Alfresco NodeRef URL
    */
   public String getNodeRefUrl()
   {
      return getAvmNode().getNodeRef().toString();
   }
   
   /**
    * @return if we are currently within the context of a Browse list (otherwise we are
    *         probably looking at an item in the Modified File list)
    */
   public boolean getIsBrowseList()
   {
      return !this.avmBrowseBean.isCurrentPathNull();
   }
   
   /**
    * @return the sibling nodes for this item
    */
   protected abstract List<AVMNode> getNodes();
   
   
   // ------------------------------------------------------------------------------
   // Action event handlers
   
   /**
    * Save the state of the panel that was expanded/collapsed
    */
   public void expandPanel(ActionEvent event)
   {
      if (event instanceof ExpandedEvent)
      {
         String id = event.getComponent().getId();
         this.panels.put(id, ((ExpandedEvent)event).State);
      }
   }
   
   /**
    * Navigates to next item in the list of items for the current folder
    */
   public void nextItem(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String path = params.get("id");
      if (path != null && path.length() != 0)
      {
         List<AVMNode> nodes = getNodes();
         if (nodes.size() > 1)
         {
            // perform a linear search - this is slow but stateless
            // otherwise we would have to manage state of last selected node
            // this gets very tricky as this bean is instantiated once and never
            // reset - it does not know when the item has changed etc.
            for (int i=0; i<nodes.size(); i++)
            {
               if (path.equals(nodes.get(i).get("id")) == true)
               {
                  AVMNode next;
                  // found our item - navigate to next
                  if (i != nodes.size() - 1)
                  {
                     next = nodes.get(i + 1);
                  }
                  else
                  {
                     // handle wrapping case
                     next = nodes.get(0);
                  }
                  
                  // prepare for showing details for this node
                  this.avmBrowseBean.setupContentAction(next.getPath(), false);
                  break;
               }
            }
         }
      }
   }
   
   /**
    * Navigates to the previous item in the list of items for the current folder
    */
   public void previousItem(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String path = params.get("id");
      if (path != null && path.length() != 0)
      {
         List<AVMNode> nodes = getNodes();
         if (nodes.size() > 1)
         {
            // see above
            for (int i=0; i<nodes.size(); i++)
            {
               if (path.equals(nodes.get(i).get("id")) == true)
               {
                  AVMNode previous;
                  // found our item - navigate to previous
                  if (i != 0)
                  {
                     previous = nodes.get(i - 1);
                  }
                  else
                  {
                     // handle wrapping case
                     previous = nodes.get(nodes.size() - 1);
                  }
                  
                  // prepare for showing details for this node
                  this.avmBrowseBean.setupContentAction(previous.getPath(), false);
                  break;
               }
            }
         }
      }
   }
}
