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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.springframework.extensions.config.Config;
import org.springframework.extensions.config.ConfigElement;
import org.springframework.extensions.config.ConfigService;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.FileUploadBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;

/**
 * Bean implementation for the "Add Content" dialog
 * 
 * @author gavinc
 */
public class AddContentDialog extends BaseContentWizard
{
   private final static String MSG_OK = "ok";
   private static final long serialVersionUID = 3593557546118692687L;

   protected List<String> inlineEditableMimeTypes;
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
      cr.setEncoding(this.encoding);
      // create properties for content type
      Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>(5, 1.0f);
      
      if (Repository.extractMetadata(FacesContext.getCurrentInstance(), cr, contentProps))
      {
         this.author = (String)(contentProps.get(ContentModel.PROP_AUTHOR));
         this.title = DefaultTypeConverter.INSTANCE.convert(String.class, contentProps.get(ContentModel.PROP_TITLE));
         this.description = DefaultTypeConverter.INSTANCE.convert(String.class, contentProps.get(ContentModel.PROP_DESCRIPTION));
      }
      
      // default the title to the file name if not set
      if (this.title == null)
      {
         this.title = this.fileName;
      }
      
      // determine whether inline editing should be enabled by default.
      // if the mime type of the added file is in the list of mime types
      // configured in "Content Wizards" then enable inline editing
      List<String> mimeTypes = getInlineEditableMimeTypes();
      if (mimeTypes.contains(this.mimeType))
      {
         this.inlineEdit = true;
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
      clearUpload();
      
      // as we were successful, go to the set properties dialog if asked
      // to otherwise just return
      if (this.showOtherProperties)
      {
         // check whether the created node is checked out, if a 'check out'
         // rule is present in the space the new node will be and an
         // attempt to modify the properties will cause an error (ALF-438)
         if (getNodeService().hasAspect(this.createdNode, ContentModel.ASPECT_LOCKABLE))
         {
             Utils.addErrorMessage(Application.getMessage(FacesContext.getCurrentInstance(), MSG_NODE_LOCKED));
             return outcome;
         }
         else
         {
            // we are going to immediately edit the properties so we need
            // to setup the BrowseBean context appropriately
            this.browseBean.setDocument(new Node(this.createdNode));
      
            return "dialog:setContentProperties";
         }
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
      
      return "dialog:close:browse";
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
      FacesContext fc = FacesContext.getCurrentInstance();
      this.mimeType = Repository.getMimeTypeForFileName(fc, this.fileName);
      this.encoding = "UTF-8";
      InputStream is = null;
      try
      {
         if (this.file != null)
         {
            is = new BufferedInputStream(new FileInputStream(this.file));
            this.encoding = Repository.guessEncoding(fc, is, this.mimeType);
         }
      }
      catch (Throwable e)
      {
         // Not terminal
         logger.error("Failed to get encoding from file: " + this.fileName, e);
      }
      finally
      {
         try { is.close(); } catch (Throwable e) {}         // Includes NPE
      }
      
      this.inlineEdit = (this.mimeType.equals(MimetypeMap.MIMETYPE_HTML));
      
      // get the file upload message
      String msg = Application.getMessage(FacesContext.getCurrentInstance(), "file_upload_success");
      return MessageFormat.format(msg, new Object[] {Utils.encode(getFileName())});
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
      return "dialog:close";
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
   
   protected List<String> getInlineEditableMimeTypes()
   {
      if ((this.inlineEditableMimeTypes == null) || (Application.isDynamicConfig(FacesContext.getCurrentInstance())))	  
      {
         this.inlineEditableMimeTypes = new ArrayList<String>(8);
         
         // get the create mime types list from the config
         ConfigService svc = Application.getConfigService(FacesContext.getCurrentInstance());
         Config wizardCfg = svc.getConfig("Content Wizards");
         if (wizardCfg != null)
         {
            ConfigElement typesCfg = wizardCfg.getConfigElement("create-mime-types");
            if (typesCfg != null)
            {
               for (ConfigElement child : typesCfg.getChildren())
               {
                  String currentMimeType = child.getAttribute("name");
                  this.inlineEditableMimeTypes.add(currentMimeType);
               }
            }
         }
      }
      
      return this.inlineEditableMimeTypes;
   }
   
   @Override
   public String getFinishButtonLabel()
   {
    
      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_OK);
   }
   
   @Override
   protected String formatErrorMessage(Throwable exception)
   {
      if (exception instanceof FileExistsException)
      {
         return MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_EXISTS),
               ((FileExistsException)exception).getName());
      }
      else
      {
         return MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), "error_content"),
               exception.getMessage());
      }
   }

}
