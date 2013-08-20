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
package org.alfresco.web.bean.preview;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.NavigationSupport;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.NodeListUtils;
import org.alfresco.web.ui.common.NodePropertyComparator;
import org.alfresco.web.ui.common.component.UIActionLink;

/**
 * Backing bean for the Preview Document in Template action page
 * 
 * @author Kevin Roast
 */
public class DocumentPreviewBean extends BasePreviewBean implements NavigationSupport
{
   private static final long serialVersionUID = 2476545866710371333L;

   private final static String MSG_PREVIEW_OF = "preview_of";
   private final static String MSG_CLOSE = "close";
   private final static String MSG_LEFT_QUOTE = "left_qoute";
   private final static String MSG_RIGHT_QUOTE = "right_quote";

   /**
    * Returns the document this bean is currently representing
    * 
    * @return The document Node
    */
   public Node getNode()
   {
      return this.browseBean.getDocument();
   }
   
   /**
    * Returns a model for use by a template on the Document Details page.
    * 
    * @return model containing current document and current space info.
    */
   @SuppressWarnings("unchecked")
   public Map getTemplateModel()
   {
      HashMap model = new HashMap(3, 1.0f);
      
      model.put("document", getNode().getNodeRef());
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
      return "dialog:close:dialog:previewContent";
   }

   public void nextItem(ActionEvent event)
   {
      UIActionLink link = (UIActionLink) event.getComponent();
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
      UIActionLink link = (UIActionLink) event.getComponent();
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

   public String getContainerTitle()
   {
       FacesContext fc = FacesContext.getCurrentInstance();
       return Application.getMessage(fc, MSG_PREVIEW_OF) + " " + Application.getMessage(fc, MSG_LEFT_QUOTE) + getName() + Application.getMessage(fc, MSG_RIGHT_QUOTE);
   }

   public String getCancelButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_CLOSE);
   }
}
