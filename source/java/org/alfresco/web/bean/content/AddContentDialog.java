package org.alfresco.web.bean.content;

import java.io.File;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.FileUploadBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;

/**
 * Bean implementation for the "Add Content" dialog
 * 
 * @author gavinc
 */
public class AddContentDialog extends BaseContentWizard
{
   protected File file;
   
   // ------------------------------------------------------------------------------
   // Dialog implementation
   
   @Override
   protected String finishImpl(FacesContext context, String outcome)
         throws Exception
   {
      // Try and extract metadata from the file
      ContentReader cr = new FileContentReader(this.file);
      cr.setMimetype(this.mimeType);
      // create properties for content type
      Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>(5, 1.0f);
      
      if (Repository.extractMetadata(FacesContext.getCurrentInstance(), cr, contentProps))
      {
         this.author = (String)(contentProps.get(ContentModel.PROP_AUTHOR));
         this.title = (String)(contentProps.get(ContentModel.PROP_TITLE));
         this.description = (String)(contentProps.get(ContentModel.PROP_DESCRIPTION));
      }
      
      // default the title to the file name if not set
      if (this.title == null)
      {
         this.title = this.fileName;
      }
         
      saveContent(this.file, null);
      
      // return default outcome
      return outcome;
   }
   
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      clearUpload();
   }
   
   @Override
   protected String doPostCommitProcessing(FacesContext context, String outcome)
   {
      // as we were successful, go to the set properties dialog if asked
      // to otherwise just return
      if (this.showOtherProperties)
      {
         // we are going to immediately edit the properties so we need
         // to setup the BrowseBean context appropriately
         this.browseBean.setDocument(new Node(this.createdNode));
      
         return "dialog:setContentProperties";
      }
      else
      {
         return outcome;
      }
   }
   
   @Override
   protected String getDefaultFinishOutcome()
   {
      // as we are using this dialog outside the dialog framework 
      // just go back to the main page
      
      return "browse";
   }

   // ------------------------------------------------------------------------------
   // Bean getters and setters

   /**
    * @return Returns the message to display when a file has been uploaded
    */
   public String getFileUploadSuccessMsg()
   {
      // NOTE: This is a far from ideal solution but will do until we have 
      //       a pure JSF upload solution working. This method is only called
      //       after a file is uploaded, so we can calculate the mime type and
      //       determine whether to enable inline editing in here. 
      this.mimeType = Repository.getMimeTypeForFileName(
            FacesContext.getCurrentInstance(), this.fileName);
      this.inlineEdit = (this.mimeType.equals(MimetypeMap.MIMETYPE_HTML));
      
      // get the file upload message
      String msg = Application.getMessage(FacesContext.getCurrentInstance(), "file_upload_success");
      return MessageFormat.format(msg, new Object[] {getFileName()});
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
   
   // ------------------------------------------------------------------------------
   // Action event handlers
   
   /**
    * Action listener called when the add content dialog is called
    */
   public void start(ActionEvent event)
   {
      // NOTE: this is a temporary solution to allow us to use the new dialog
      //       framework beans outside of the dialog framework, we need to do
      //       this because the uploading requires a separate non-JSF form, this
      //       approach can not be used in the current dialog framework. Until
      //       we have a pure JSF upload solution we need this initialisation

      init(null);
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
      
      return "cancel";
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
