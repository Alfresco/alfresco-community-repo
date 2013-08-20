/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.web.bean.content;

import java.util.Collections;
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
import org.alfresco.web.ui.common.NodeListUtils;
import org.alfresco.web.ui.common.NodePropertyComparator;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.Utils.URLMode;
import org.alfresco.web.ui.common.component.UIActionLink;

public class DocumentLinkDetailsDialog extends BaseDetailsBean implements NavigationSupport
{
   private static final long serialVersionUID = 4716260640608281667L;
   
   private static final String MSG_DETAILS_OF = "details_of";
   private static final String MSG_LOCATION = "location";
   private final static String MSG_CLOSE = "close";
   private final static String MSG_LEFT_QUOTE = "left_qoute";
   private final static String MSG_RIGHT_QUOTE = "right_quote";

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
            String currentSortColumn = this.browseBean.getContentRichList().getCurrentSortColumn();
            boolean currentSortDescending = this.browseBean.getContentRichList().isCurrentSortDescending();
            Collections.sort(nodes, new NodePropertyComparator(currentSortColumn, !currentSortDescending));
            Node next = NodeListUtils.nextItem(nodes, id);
            this.browseBean.setupContentAction(next.getId(), false);
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
            String currentSortColumn = this.browseBean.getContentRichList().getCurrentSortColumn();
            boolean currentSortDescending = this.browseBean.getContentRichList().isCurrentSortDescending();
            Collections.sort(nodes, new NodePropertyComparator(currentSortColumn, !currentSortDescending));
            Node previous = NodeListUtils.previousItem(nodes, id);
            this.browseBean.setupContentAction(previous.getId(), false);
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
       FacesContext fc = FacesContext.getCurrentInstance();
       return Application.getMessage(FacesContext.getCurrentInstance(), MSG_DETAILS_OF) + " " + Application.getMessage(fc, MSG_LEFT_QUOTE) + getName() + Application.getMessage(fc, MSG_RIGHT_QUOTE);
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
