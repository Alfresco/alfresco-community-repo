/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.web.bean.wcm;

import java.io.File;
import java.text.MessageFormat;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.transaction.UserTransaction;

import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.FileUploadBean;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;

/**
 * @author Kevin Roast
 */
public class ImportWebsiteDialog
{
   protected File file;
   protected String fileName;
   private boolean isFinished = false;
   
   
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
         
         UserTransaction tx = null;
         
         try
         {
            FacesContext context = FacesContext.getCurrentInstance();
            tx = Repository.getUserTransaction(context);
            tx.begin();
            
            //
            // TODO: import the content
            //
            
            tx.commit();
            
            outcome = AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME;
         }
         catch (Throwable e)
         {
            // rollback the transaction
            try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
            Utils.addErrorMessage(MessageFormat.format(
                  Application.getMessage(FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), 
                  e.getMessage(), e));
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
}
