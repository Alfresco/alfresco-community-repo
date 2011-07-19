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
package org.alfresco.web.bean.coci;

import java.util.StringTokenizer;

import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.ui.common.Utils;

/**
 * This bean class handle done-editing(commit) dialog.
 *
 */
public class DoneEditingDialog extends CheckinCheckoutDialog
{
   private final static String MSG_OK = "ok";
   private static final String MSG_DONE_EDITING = "done_editing";
   private final static String MSG_MISSING_ORIGINAL_NODE = "missing_original_node";
   private final static String MSG_LEFT_QUOTE = "left_qoute";
   private final static String MSG_RIGHT_QUOTE = "right_quote";

   private final static String DIALOG_NAME = AlfrescoNavigationHandler.DIALOG_PREFIX + "doneEditingFile";


   /**
    * this flag indicates occurrence when source node isn't versionable, but working copy yet is versionable
    */
   private boolean sourceVersionable;

   /**
    * this field contains reference to source node for working copy
    */
   private NodeRef sourceNodeRef;

   /**
    * @return Returns label for new version with major changes
    */
   public String getMajorNewVersionLabel()
   {
      String label = getCurrentVersionLabel();
      StringTokenizer st = new StringTokenizer(label, ".");
      return (Integer.valueOf(st.nextToken()) + 1) + ".0";
   }

   /**
    * @return Returns label for new version with minor changes
    */
   public String getMinorNewVersionLabel()
   {
      String label = getCurrentVersionLabel();
      StringTokenizer st = new StringTokenizer(label, ".");
      return st.nextToken() + "." + (Integer.valueOf(st.nextToken()) + 1);
   }

   /**
    * @return Returns flag, which indicates occurrence when source node is versionable
    */
   public boolean isSourceVersionable()
   {
      return sourceVersionable;
   }

   /**
    * @return Returns true if source node for selected working copy founded
    */
   public boolean isSourceFound()
   {
      return (sourceNodeRef != null);
   }

   /**
    * Method for handling done-editing action(e.g. "done_editing_doc")
    * @param event Action Event
    */
   public void handle(ActionEvent event)
   {
      setupContentAction(event);

      FacesContext fc = FacesContext.getCurrentInstance();
      NavigationHandler nh = fc.getApplication().getNavigationHandler();
      // if content is versionable then check-in else move to dialog for filling version info
      if (isVersionable())
      {
         nh.handleNavigation(fc, null, DIALOG_NAME);
      }
      else
      {
         checkinFileOK(fc, null);
         nh.handleNavigation(fc, null, AlfrescoNavigationHandler.DIALOG_PREFIX + "browse");
      }
   }

   @Override
   public void setupContentAction(ActionEvent event)
   {
      super.setupContentAction(event);

      Node node = property.getDocument();
      if (node != null)
      {
         sourceNodeRef = getSourceNodeRef(node.getNodeRef());
         if (sourceNodeRef != null)
         {
            sourceVersionable = getNodeService().hasAspect(sourceNodeRef, ContentModel.ASPECT_VERSIONABLE);
         }
      }
   }

   @Override
   public String getFinishButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_OK);
   }

   @Override
   public boolean getFinishButtonDisabled()
   {
      return !isSourceFound();
   }

   @Override
   public String getContainerTitle()
   {
      if (isSourceFound())
      {
          FacesContext fc = FacesContext.getCurrentInstance();
          return Application.getMessage(fc, MSG_DONE_EDITING) + " " + Application.getMessage(fc, MSG_LEFT_QUOTE)
              + getNodeService().getProperty(sourceNodeRef, ContentModel.PROP_NAME) + Application.getMessage(fc, MSG_RIGHT_QUOTE);
      }
      else
      {
         String message = Application.getMessage(FacesContext.getCurrentInstance(), MSG_MISSING_ORIGINAL_NODE);
         Utils.addErrorMessage(message);
         return message;
      }
   }

   @Override
   public void resetState()
   {
      super.resetState();

      sourceVersionable = false;
      sourceNodeRef = null;
   }

   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      return checkinFileOK(context, outcome);
   }

   /**
    * @return Returns version label for source node for working copy. Null indicates error
    */
   private String getCurrentVersionLabel()
   {
      if (isSourceFound())
      {
         Version curVersion = property.getVersionQueryService().getCurrentVersion(sourceNodeRef);
         return curVersion.getVersionLabel();
      }

      return null;
   }

   /**
    * @param workingCopyNodeRef node reference to working copy
    * @return Returns node reference to node, which is source for working copy node. Null indicates error
    */
   private NodeRef getSourceNodeRef(NodeRef workingCopyNodeRef)
   {
      return getCheckOutCheckInService().getCheckedOut(workingCopyNodeRef);
   }

}
