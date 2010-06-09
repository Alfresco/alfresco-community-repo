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
package org.alfresco.web.bean.wcm;

import java.io.FileNotFoundException;
import java.util.List;
import java.text.MessageFormat;

import javax.faces.context.FacesContext;

import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.forms.*;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean implementation for the AVM "Delete File" dialog
 * 
 * @author kevinr
 */
public class DeleteFileDialog extends BaseDialogBean
{
   private static final long serialVersionUID = -3962232696127851920L;

   private static final Log logger = LogFactory.getLog(DeleteFileDialog.class);
   
   transient private AVMService avmService;
   protected AVMBrowseBean avmBrowseBean;
   transient private FormsService formsService;
 
   /**
    * @param avmBrowseBean The avmBrowseBean to set.
    */
   public void setAvmBrowseBean(AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
   }

   /**
    * @param avmService    The avmService to set.
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
    * @param formsService    The FormsService to set.
    */
   public void setFormsService(final FormsService formsService)
   {
      this.formsService = formsService;
   }
   
   protected FormsService getFormsService()
   {
      if (formsService == null)
      {
         formsService = (FormsService) FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), "FormsService");
      }
      return formsService;
   }

   
   // ------------------------------------------------------------------------------
   // Dialog implementation
   
   @Override
   protected String finishImpl(final FacesContext context, final String outcome)
      throws Exception
   {
      // get the content to delete
      final AVMNode node = this.avmBrowseBean.getAvmActionNode();
      if (node == null)
      {
         logger.warn("WARNING: delete called without a current AVM Node!");
      }
      else
      {
         if (logger.isDebugEnabled())
            logger.debug("Trying to delete AVM node: " + node.getPath());
         FormInstanceData fid = null;
         if (node.hasAspect(WCMAppModel.ASPECT_RENDITION))
         {
            try
            {
               fid = this.getFormsService().getRendition(node.getNodeRef()).getPrimaryFormInstanceData();
            }
            catch (FileNotFoundException fnfe)
            {
               //ignore
            }
         }
         else if (node.hasAspect(WCMAppModel.ASPECT_FORM_INSTANCE_DATA))
         {
            fid = this.getFormsService().getFormInstanceData(node.getNodeRef());
         }
         if (fid != null)
         {
            final List<Rendition> renditions = fid.getRenditions();
            for (final Rendition r : renditions)
            {
               this.getAvmService().removeNode(AVMNodeConverter.SplitBase(r.getPath())[0],
                                          AVMNodeConverter.SplitBase(r.getPath())[1]);
            }
            this.getAvmService().removeNode(AVMNodeConverter.SplitBase(fid.getPath())[0],
                                       AVMNodeConverter.SplitBase(fid.getPath())[1]);
         }
         else
         {
            // delete the node
            this.getAvmService().removeNode(AVMNodeConverter.SplitBase(node.getPath())[0],
                                       AVMNodeConverter.SplitBase(node.getPath())[1]);
         }
      }
      return outcome;
   }
      
   @Override
   protected String doPostCommitProcessing(final FacesContext context, 
                                           final String outcome)
   {
      return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME;
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
      final AVMNode node = this.avmBrowseBean.getAvmActionNode();
      if (node.hasAspect(WCMAppModel.ASPECT_RENDITION))
      {
         try
         {
            final FormInstanceData fid = this.getFormsService().getRendition(node.getNodeRef()).getPrimaryFormInstanceData();
            return MessageFormat.format(Application.getMessage(FacesContext.getCurrentInstance(), 
                                                               "delete_rendition_confirm"), 
                                        node.getName(),
                                        fid.getName(),
                                        fid.getRenditions().size() - 1);
         }
         catch (FileNotFoundException fnfe)
         {
            //ignore
         }
      }
      else if (node.hasAspect(WCMAppModel.ASPECT_FORM_INSTANCE_DATA))
      {
          try
          {
              final FormInstanceData fid = this.getFormsService().getFormInstanceData(node.getNodeRef());
              return MessageFormat.format(Application.getMessage(FacesContext.getCurrentInstance(), 
                                                                 "delete_form_instance_data_confirm"), 
                                                                 fid.getName(),
                                                                 fid.getRenditions().size());
          }
          catch (FormNotFoundException fnfe)
          {
             // ignore
          }
      }
      return MessageFormat.format(Application.getMessage(FacesContext.getCurrentInstance(), 
                                                         "delete_avm_file_confirm"), 
                                  node.getName());
   }
}
