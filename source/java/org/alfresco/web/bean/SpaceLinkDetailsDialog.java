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
package org.alfresco.web.bean;

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
import org.alfresco.web.bean.dialog.NavigationSupport;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.NodeListUtils;
import org.alfresco.web.ui.common.NodePropertyComparator;
import org.alfresco.web.ui.common.component.UIActionLink;

public class SpaceLinkDetailsDialog extends BaseDetailsBean implements NavigationSupport
{
   private static final long serialVersionUID = 8372741472120796169L;
   
   private static final String MSG_DETAILS_OF = "details_of";
   private static final String MSG_LOCATION = "location";
   private final static String MSG_CLOSE = "close";
   private final static String MSG_LEFT_QUOTE = "left_qoute";
   private final static String MSG_RIGHT_QUOTE = "right_quote";
   
   /**
    * Returns the Space this bean is currently representing
    * 
    * @return The Space Node
    */
   public Node getSpace()
   {
      return getNode();
   }

   @Override
   protected Node getLinkResolvedNode()
   {
      Node space = getSpace();
      if (ApplicationModel.TYPE_FOLDERLINK.equals(space.getType()))
      {
         NodeRef destRef = (NodeRef) space.getProperties().get(ContentModel.PROP_LINK_DESTINATION);
         if (getNodeService().exists(destRef))
         {
            space = new Node(destRef);
         }
      }
      return space;
   }

   @Override
   public Node getNode()
   {
      return this.browseBean.getActionSpace();
   }

   @Override
   protected String getPropertiesPanelId()
   {
      return "space-props";
   }

   @Override
   public Map getTemplateModel()
   {
      HashMap model = new HashMap(1, 1.0f);

      model.put("space", getSpace().getNodeRef());
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

   public void nextItem(ActionEvent event)
   {
      UIActionLink link = (UIActionLink) event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String id = params.get("id");
      if (id != null && id.length() != 0)
      {
         NodeRef currNodeRef = new NodeRef(Repository.getStoreRef(), id);
         List<Node> nodes = this.browseBean.getParentNodes(currNodeRef);
         Node next = null;
         if (nodes.size() > 1)
         {
            String currentSortColumn = this.browseBean.getSpacesRichList().getCurrentSortColumn();
            boolean currentSortDescending = this.browseBean.getSpacesRichList().isCurrentSortDescending();
            Collections.sort(nodes, new NodePropertyComparator(currentSortColumn, !currentSortDescending));
            next = NodeListUtils.nextItem(nodes, id);
            this.browseBean.setupSpaceAction(next.getId(), false);
         }
         if (next == null)
         {
            Node currNode = new Node(currNodeRef);
            this.navigator.setupDispatchContext(currNode);
         }
      }

   }

   public void previousItem(ActionEvent event)
   {
      UIActionLink link = (UIActionLink) event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String id = params.get("id");
      if (id != null && id.length() != 0)
      {
         NodeRef currNodeRef = new NodeRef(Repository.getStoreRef(), id);
         List<Node> nodes = this.browseBean.getParentNodes(currNodeRef);
         Node previous = null;
         if (nodes.size() > 1)
         {
            String currentSortColumn = this.browseBean.getSpacesRichList().getCurrentSortColumn();
            boolean currentSortDescending = this.browseBean.getSpacesRichList().isCurrentSortDescending();
            Collections.sort(nodes, new NodePropertyComparator(currentSortColumn, !currentSortDescending));
            previous = NodeListUtils.previousItem(nodes, id);
            this.browseBean.setupSpaceAction(previous.getId(), false);
         }
         if (previous == null)
         {
            Node currNode = new Node(currNodeRef);
            this.navigator.setupDispatchContext(currNode);
         }
      }
   }
   
   public String getCancelButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_CLOSE);
   }

   @Override
   public String getContainerSubTitle()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_LOCATION) + ": " + 
             getSpace().getNodePath().toDisplayPath(getNodeService(), getPermissionService());
   }

   public String getContainerTitle()
   {
       FacesContext fc = FacesContext.getCurrentInstance();
       return Application.getMessage(fc, MSG_DETAILS_OF) + " " + Application.getMessage(fc, MSG_LEFT_QUOTE) + getName() + Application.getMessage(fc, MSG_RIGHT_QUOTE);
   }
   
   public String getOutcome()
   {
      return "dialog:close:dialog:showSpaceDetails";
   }

   public String cancel()
   {
      this.navigator.resetCurrentNodeProperties();
      return super.cancel();
   }
}
