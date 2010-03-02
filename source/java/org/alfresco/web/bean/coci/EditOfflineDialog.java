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

import java.text.MessageFormat;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.web.scripts.FileTypeImageUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.users.UserPreferencesBean;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class EditOfflineDialog extends CheckinCheckoutDialog
{
   private static final long serialVersionUID = -4848508258494238150L;
   
   public static final String OFFLINE_EDITING = "offlineEditing";
   public static final String CLOSE = "close";
   public static final String MSG_ERROR_CHECKOUT = "error_checkout";
   public static final String OFFLINE_TITLE = "offline_title";
   
   private static Log logger = LogFactory.getLog(EditOfflineDialog.class);
   
   private boolean continueCountdown;
   protected UserPreferencesBean userPreferencesBean;

   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      continueCountdown = true;
   }

   @Override
   public void restored()
   {
      super.restored();
      continueCountdown = false;
   }

   @Override
   public String getContainerTitle()
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      String pattern = Application.getMessage(fc, OFFLINE_TITLE);
      return MessageFormat.format(pattern, property.getDocument().getName());
   }

   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      return outcome;
   }

   @Override
   public String getCancelButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), CLOSE);
   }

   /**
    * Action listener for handle offline editing action. E.g "edit_doc_offline"
    * action
    * 
    * @param event ActionEvent
    */
   public void setupContentAction(ActionEvent event)
   {
      UIActionLink link = (UIActionLink) event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String id = params.get("id");
      if (id != null && id.length() != 0)
      {
         super.setupContentDocument(id);
         checkoutFile(property.getDocument());
         if (userPreferencesBean.isDownloadAutomatically())
         {
            FacesContext fc = FacesContext.getCurrentInstance();
            this.navigator.setupDispatchContext(property.getDocument());
            fc.getApplication().getNavigationHandler().handleNavigation(fc, null,
                     "dialog:editOfflineDialog");
         }
         else
         {
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.getApplication().getNavigationHandler().handleNavigation(fc, null,
                     "dialog:close:browse");
         }
      }
      else
      {
         property.setDocument(null);
      }
      super.resetState();
   }

   /**
    * Checkout document to the same space as original one and then add the
    * OFFLINE_EDITING property.
    */
   private void checkoutFile(Node node)
   {
      UserTransaction tx = null;
      FacesContext context = FacesContext.getCurrentInstance();
      
      if (node != null)
      {
         try
         {
            tx = Repository.getUserTransaction(context, false);
            tx.begin();
            
            if (logger.isDebugEnabled())
               logger.debug("Trying to checkout content node Id: " + node.getId());
            NodeRef workingCopyRef = null;
            
            // checkout the content to the current space
            workingCopyRef = property.getVersionOperationsService().checkout(node.getNodeRef());
            getNodeService().setProperty(workingCopyRef, ContentModel.PROP_WORKING_COPY_MODE,
                     OFFLINE_EDITING);
            
            // set the working copy Node instance
            Node workingCopy = new Node(workingCopyRef);
            property.setWorkingDocument(workingCopy);

            // create content URL to the content download servlet with ID and
            // expected filename
            String url = DownloadContentServlet.generateDownloadURL(workingCopyRef, workingCopy
                     .getName());

            workingCopy.getProperties().put("url", url);
            workingCopy.getProperties().put("fileType32",
                    FileTypeImageUtils.getFileTypeImage(workingCopy.getName(), false));
            
            // commit the transaction
            tx.commit();
         }
         catch (Throwable err)
         {
            try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
            
            Utils.addErrorMessage(Application.getMessage(FacesContext.getCurrentInstance(),
                     MSG_ERROR_CHECKOUT)
                     + err.getMessage(), err);
         }
      }
      else
      {
         logger.warn("WARNING: checkoutFile called without a current Document!");
      }
   }

   @Override
   public String cancel()
   {
      super.cancel();
      return "dialog:close:browse";
   }

   /**
    * @return userPreferencesBean bean with current user's preferences
    */
   public UserPreferencesBean getUserPreferencesBean()
   {
      return userPreferencesBean;
   }

   /**
    * @param userPreferencesBean bean with current user's preferences to set
    */
   public void setUserPreferencesBean(UserPreferencesBean userPreferencesBean)
   {
      this.userPreferencesBean = userPreferencesBean;
   }

   /**
    * @return continueCountdown
    */
   public boolean isContinueCountdown()
   {
      return continueCountdown;
   }

   /**
    * @param continueCountdown
    */
   public void setContinueCountdown(boolean continueCountdown)
   {
      this.continueCountdown = continueCountdown;
   }

}
