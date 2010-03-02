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

import java.io.File;
import java.text.MessageFormat;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.wcm.asset.AssetService;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.bean.FileUploadBean;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;

/**
 * Backing bean for the Import Website Content dialog.
 *
 * This dialog manages the upload of a ZIP archive file, which is then unpacked and loaded into
 * the AVM store with the complete folder and file structure.
 *
 * @author Kevin Roast
 */
public class ImportWebsiteDialog extends BaseDialogBean
{
   private static final long serialVersionUID = -432986732265292504L;

   //private static Log logger = LogFactory.getLog(ImportWebsiteDialog.class);

   protected File file;
   protected String fileName;
   protected boolean isFinished = false;
   protected boolean highByteZip = false;

   protected AVMBrowseBean avmBrowseBean;
   transient private AssetService assetService;


   /**
    * @param avmBrowseBean       The AVMBrowseBean to set.
    */
   public void setAvmBrowseBean(AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
   }

   /**
    * @param avmService          The AssetService to set.
    */
   public void setAssetService(AssetService assetService)
   {
      this.assetService = assetService;
   }

   protected AssetService getAssetService()
   {
      if (this.assetService == null)
      {
         this.assetService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getAssetService();
      }
      return this.assetService;
   }

   /**
    * @return Returns the name of the file
    */
   public String getFileName()
   {
      // try and retrieve the file and filename from the file upload bean
      // representing the file we previously uploaded.
      FacesContext ctx = FacesContext.getCurrentInstance();
      FileUploadBean fileBean = (FileUploadBean)ctx.getExternalContext().getSessionMap().
         get(FileUploadBean.FILE_UPLOAD_BEAN_NAME);
      if (fileBean != null)
      {
         this.file = fileBean.getFile();
         this.fileName = fileBean.getFileName();
      }

      return this.fileName;
   }

   /**
    * @param fileName The name of the file
    */
   public void setFileName(String fileName)
   {
      this.fileName = fileName;

      // we also need to keep the file upload bean in sync
      FacesContext ctx = FacesContext.getCurrentInstance();
      FileUploadBean fileBean = (FileUploadBean)ctx.getExternalContext().getSessionMap().
         get(FileUploadBean.FILE_UPLOAD_BEAN_NAME);
      if (fileBean != null)
      {
         fileBean.setFileName(this.fileName);
      }
   }

   /**
    * @return the highByteZip encoding switch
    */
   public boolean isHighByteZip()
   {
      return this.highByteZip;
   }

   /**
    * @param highByteZip the encoding switch for high-byte ZIP filenames to set
    */
   public void setHighByteZip(boolean highByteZip)
   {
      this.highByteZip = highByteZip;
   }

   public boolean getFinishButtonDisabled()
   {
      return (this.fileName == null || this.fileName.length() == 0);
   }


   // ------------------------------------------------------------------------------
   // Action event handlers

   /**
    * Action listener called when the add content dialog is called
    */
   public void start(ActionEvent event)
   {
      clearUpload();
      this.fileName = null;
   }

   /**
    * Action handler called when the Finish button is pressed
    */
   public String finish()
   {
      String outcome = null;

      // check the isFinished flag to stop the finish button
      // being pressed multiple times
      if (this.isFinished == false)
      {
         this.isFinished = true;

         try
         {
            FacesContext context = FacesContext.getCurrentInstance();
            RetryingTransactionHelper.RetryingTransactionCallback<Object> cb =
            new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
            {
               public Object execute()
               {
                  // get the AVM path that will contain the imported content
                  String rootPath = avmBrowseBean.getCurrentPath();
                  
                  String[] parts = rootPath.split(":");
                  String sbStoreId = parts[0];
                  String parentFolderPath = parts[1];
   
                  getAssetService().bulkImport(sbStoreId, parentFolderPath, file, isHighByteZip());

                  return null;
               }
            };
            
            Repository.getRetryingTransactionHelper(context).doInTransaction(cb);

            UIContextService.getInstance(context).notifyBeans();

            outcome = AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME;
         }
         catch (Throwable e)
         {
            Utils.addErrorMessage(MessageFormat.format(
                  Application.getMessage(FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC),
                  e.getMessage()), e);
         }
         finally
         {
            // reset the flag so we can re-attempt the operation
            this.isFinished = false;
         }
      }

      return outcome;
   }

   /**
    * Action handler called when the user wishes to remove an uploaded file
    */
   public String removeUploadedFile()
   {
      clearUpload();

      // also clear the file name
      this.fileName = null;

      // refresh the current page
      return null;
   }

   /**
    * Action handler called when the dialog is cancelled
    */
   public String cancel()
   {
      clearUpload();

      return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME;
   }


   // ------------------------------------------------------------------------------
   // Helper Methods

   /**
    * Deletes the uploaded file and removes the FileUploadBean from the session
    */
   protected void clearUpload()
   {
      // delete the temporary file we uploaded earlier
      if (this.file != null)
      {
         this.file.delete();
      }

      this.file = null;

      // remove the file upload bean from the session
      FacesContext ctx = FacesContext.getCurrentInstance();
      ctx.getExternalContext().getSessionMap().remove(FileUploadBean.FILE_UPLOAD_BEAN_NAME);
   }

   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      return null;
   }
}
