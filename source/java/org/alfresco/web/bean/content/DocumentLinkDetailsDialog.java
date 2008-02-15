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
package org.alfresco.web.bean.content;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.BaseDetailsBean;
import org.alfresco.web.bean.dialog.NavigationSupport;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.Utils.URLMode;
import org.alfresco.web.ui.common.component.UIActionLink;

public class DocumentLinkDetailsDialog extends BaseDetailsBean implements NavigationSupport
{
   private static final String MSG_DETAILS_OF = "details_of";
   private static final String MSG_LOCATION = "location";
   private final static String MSG_CLOSE = "close";

   /**
    * Returns the document this bean is currently representing
    *
    * @return The document Node
    */
   public Node getDocument()
   {
      return this.getNode();
   }
   
   @Override
   protected Node getLinkResolvedNode()
   {
      Node document = getDocument();
      if (ApplicationModel.TYPE_FILELINK.equals(document.getType()))
      {
         NodeRef destRef = (NodeRef)document.getProperties().get(ContentModel.PROP_LINK_DESTINATION);
         if (getNodeService().exists(destRef))
         {
            document = new Node(destRef);
         }
      }
      return document;
   }

   @Override
   public Node getNode()
   {
      return this.browseBean.getDocument();
   }

   @Override
   protected String getPropertiesPanelId()
   {
      return "document-props";
   }

   @Override
   public Map getTemplateModel()
   {
      Map<String, Object> model = new HashMap<String, Object>(2, 1.0f);

      model.put("document", getDocument().getNodeRef());
      model.put("space", this.navigator.getCurrentNode().getNodeRef());
      model.put(TemplateService.KEY_IMAGE_RESOLVER, imageResolver);

      return model;
   }

   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      return null;
   }

   public String getCurrentItemId()
   {
      return getId();
   }

   public String getOutcome()
   {
      return "dialog:close:dialog:showDocDetails";
   }

   public void nextItem(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String id = params.get("id");
      if (id != null && id.length() != 0)
      {
         List<Node> nodes = this.browseBean.getContent();
         if (nodes.size() > 1)
         {
            // perform a linear search - this is slow but stateless
            // otherwise we would have to manage state of last selected node
            // this gets very tricky as this bean is instantiated once and never
            // reset - it does not know when the document has changed etc.
            for (int i=0; i<nodes.size(); i++)
            {
               if (id.equals(nodes.get(i).getId()) == true)
               {
                  Node next;
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
                  this.browseBean.setupContentAction(next.getId(), false);
                  break;
               }
            }
         }
      }
      
   }

   public void previousItem(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String id = params.get("id");
      if (id != null && id.length() != 0)
      {
         List<Node> nodes = this.browseBean.getContent();
         if (nodes.size() > 1)
         {
            // see above
            for (int i=0; i<nodes.size(); i++)
            {
               if (id.equals(nodes.get(i).getId()) == true)
               {
                  Node previous;
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
                  this.browseBean.setupContentAction(previous.getId(), false);
                  break;
               }
            }
         }
      }
      
   }
   
   public String getCancelButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_CLOSE);
   }

   public String getContainerSubTitle()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_LOCATION) + ": " + 
             getDocument().getNodePath().toDisplayPath(getNodeService(), getPermissionService());
   }

   public String getContainerTitle()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_DETAILS_OF) + " '" + getName() + "'";
   }

   /**
    * Returns the URL to the content for the current document
    *
    * @return Content url to the current document
    */
   public String getBrowserUrl()
   {
      Node doc = getLinkResolvedNode();
      return Utils.generateURL(FacesContext.getCurrentInstance(), doc, URLMode.HTTP_INLINE);
   }
   
   /**
    * Returns the download URL to the content for the current document
    *
    * @return Download url to the current document
    */
   public String getDownloadUrl()
   {
      Node doc = getLinkResolvedNode();
      return Utils.generateURL(FacesContext.getCurrentInstance(), doc, URLMode.HTTP_DOWNLOAD);
   }
   
   /**
    * Returns whether the current document is locked
    *
    * @return true if the document is checked out
    */
   public boolean isLocked()
   {
      return getDocument().isLocked();
   }
   
   /**
    * Returns the URL to download content for the current document
    *
    * @return Content url to download the current document
    */
   public String getUrl()
   {
      return (String)getDocument().getProperties().get("url");
   }
   
}
