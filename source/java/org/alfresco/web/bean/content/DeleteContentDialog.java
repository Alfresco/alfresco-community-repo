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
package org.alfresco.web.bean.content;

import java.text.MessageFormat;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean implementation for the "Delete Content" dialog
 *
 * @author gavinc
 */
public class DeleteContentDialog extends BaseDialogBean
{
   private static final long serialVersionUID = 4199496011879649213L;

   transient private MultilingualContentService multilingualContentService;

   private static final Log logger = LogFactory.getLog(DeleteContentDialog.class);

   // ------------------------------------------------------------------------------
   // Dialog implementation

   @Override
   protected String finishImpl(FacesContext context, String outcome)
         throws Exception
   {
      // get the content to delete
      Node node = this.browseBean.getDocument();
      if (node != null)
      {
         if(ContentModel.TYPE_MULTILINGUAL_CONTAINER.equals(node.getType()))
         {
             if (logger.isDebugEnabled())
                 logger.debug("Trying to delete multilingual container: " + node.getId() + " and its translations" );

             // delete the mlContainer and its translations
             getMultilingualContentService().deleteTranslationContainer(node.getNodeRef());
         }
         else
         {
             if (logger.isDebugEnabled())
                 logger.debug("Trying to delete content node: " + node.getId());

             // delete the node
             this.getNodeService().deleteNode(node.getNodeRef());
         }

      }
      else
      {
         logger.warn("WARNING: delete called without a current Document!");
      }

      return outcome;
   }

   @Override
   protected String doPostCommitProcessing(FacesContext context, String outcome)
   {
      // clear action context
      this.browseBean.setDocument(null);

      // setting the outcome will show the browse view again
      return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME +
             AlfrescoNavigationHandler.OUTCOME_SEPARATOR + "browse";
   }

   @Override
   protected String getErrorMessageId()
   {
      return "error_delete_file";
   }

   @Override
   public boolean getFinishButtonDisabled()
   {
      return false;
   }

   // ------------------------------------------------------------------------------
   // Bean Getters and Setters

   /**
    * Returns the confirmation to display to the user before deleting the content.
    *
    * @return The formatted message to display
    */
   public String getConfirmMessage()
   {
      String fileConfirmMsg = null;

      Node document = this.browseBean.getDocument();

      if(document.getType().equals(ContentModel.TYPE_MULTILINGUAL_CONTAINER))
      {
          fileConfirmMsg = Application.getMessage(FacesContext.getCurrentInstance(),
              "delete_ml_container_confirm");
      }
      else if(document.hasAspect(ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION))
      {
          fileConfirmMsg = Application.getMessage(FacesContext.getCurrentInstance(),
              "delete_empty_translation_confirm");
      }
      else if(document.hasAspect(ContentModel.ASPECT_MULTILINGUAL_DOCUMENT))
      {
          fileConfirmMsg = Application.getMessage(FacesContext.getCurrentInstance(),
              "delete_translation_confirm");
      }
      else
      {
          String strHasMultipleParents = this.parameters.get("hasMultipleParents");
          if (strHasMultipleParents != null && "true".equals(strHasMultipleParents))
          {
             fileConfirmMsg = Application.getMessage(FacesContext.getCurrentInstance(),
                "delete_file_multiple_parents_confirm");
          }
          else
          {
             fileConfirmMsg = Application.getMessage(FacesContext.getCurrentInstance(),
                 "delete_file_confirm");
          }
      }

      return MessageFormat.format(fileConfirmMsg,
            new Object[] {document.getName()});
   }

   /**
   * @param multilingualContentService the Multilingual Content Service to set
   */
   public void setMultilingualContentService(MultilingualContentService multilingualContentService)
   {
       this.multilingualContentService = multilingualContentService;
   }
   
   protected MultilingualContentService getMultilingualContentService()
   {
      if (multilingualContentService == null)
      {
         multilingualContentService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getMultilingualContentService();
      }
      return multilingualContentService;
   }

}
