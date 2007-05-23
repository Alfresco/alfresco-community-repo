/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.bean;

import java.io.File;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ImporterActionExecuter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Backing bean implementation for the Import dialog.
 * 
 * @author gavinc
 */
public class ImportBean
{
   private static final Log logger = LogFactory.getLog(ImportBean.class);

   private static final String DEFAULT_OUTCOME = "dialog:close";
   
   private static final String MSG_ERROR = "error_import";
   private static final String MSG_ERROR_NO_FILE = "error_import_no_file";
   private static final String MSG_ERROR_EMPTY_FILE = "error_import_empty_file";
   
   protected BrowseBean browseBean;
   protected NodeService nodeService;
   protected ActionService actionService;
   protected ContentService contentService;
   protected MimetypeService mimetypeService;
   
   private File file;
   private String fileName;
   private String encoding = "UTF-8";
   private boolean runInBackground = true;
   
   /**
    * Performs the import operation using the current state of the bean
    * 
    * @return The outcome
    */
   public String performImport()
   {
      String outcome = DEFAULT_OUTCOME;
      
      if (logger.isDebugEnabled())
         logger.debug("Called import for file: " + this.file);
      
      if (this.file != null && this.file.exists())
      {
         // check the file actually has contents
         if (this.file.length() > 0)
         {
            UserTransaction tx = null;
            
            try
            {
               FacesContext context = FacesContext.getCurrentInstance();
               tx = Repository.getUserTransaction(context);
               tx.begin();
               
               // first of all we need to add the uploaded ACP/ZIP file to the repository
               NodeRef acpNodeRef = addFileToRepository(context);
               
               // build the action params map based on the bean's current state
               Map<String, Serializable> params = new HashMap<String, Serializable>(2, 1.0f);
               params.put(ImporterActionExecuter.PARAM_DESTINATION_FOLDER, this.browseBean.getActionSpace().getNodeRef());
               params.put(ImporterActionExecuter.PARAM_ENCODING, this.encoding);
               
               // build the action to execute
               Action action = this.actionService.createAction(ImporterActionExecuter.NAME, params);
               action.setExecuteAsynchronously(this.runInBackground);
               
               // execute the action on the ACP file
               this.actionService.executeAction(action, acpNodeRef);
               
               if (logger.isDebugEnabled())
               {
                  logger.debug("Executed import action with action params of " + params);
               }
               
               // commit the transaction
               tx.commit();
               
               // reset the bean
               reset();
            }
            catch (Throwable e)
            {
               // rollback the transaction
               try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
               Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
                     FacesContext.getCurrentInstance(), MSG_ERROR), e.toString()), e);
               outcome = null;
            }
         }
         else
         {
            Utils.addErrorMessage(Application.getMessage(FacesContext.getCurrentInstance(), MSG_ERROR_EMPTY_FILE));
            outcome = null;
         }
      }
      else
      {
         Utils.addErrorMessage(Application.getMessage(FacesContext.getCurrentInstance(), MSG_ERROR_NO_FILE));
         outcome = null;
      }
      
      return outcome;
   }
   
   /**
    * Action called when the dialog is cancelled, just resets the bean's state
    * 
    * @return The outcome
    */
   public String cancel()
   {
      reset();
      
      return DEFAULT_OUTCOME;
   }
   
   /**
    * Resets the dialog state back to the default
    */
   public void reset()
   {
      this.file = null;
      this.fileName = null;
      this.runInBackground = true;
      
      // delete the temporary file we uploaded earlier
      if (this.file != null)
      {
         this.file.delete();
      }
      
      // remove the file upload bean from the session
      FacesContext ctx = FacesContext.getCurrentInstance();
      ctx.getExternalContext().getSessionMap().remove(FileUploadBean.FILE_UPLOAD_BEAN_NAME);
   }
   
   /**
    * @return Returns the message to display when a file has been uploaded
    */
   public String getFileUploadSuccessMsg()
   {
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
         this.fileName = fileBean.getFileName();
         this.file = fileBean.getFile();
      }
      
      return this.fileName;
   }
   
   /**
    * Returns the encoding to use for the export
    *  
    * @return The encoding
    */
   public String getEncoding()
   {
      return this.encoding;
   }

   /**
    * Sets the encoding to use for the export package
    * 
    * @param encoding The encoding
    */
   public void setEncoding(String encoding)
   {
      this.encoding = encoding;
   }

   /**
    * Determines whether the import should run in the background
    * 
    * @return true means the import will run in the background 
    */
   public boolean getRunInBackground()
   {
      return this.runInBackground;
   }

   /**
    * Determines whether the import will run in the background
    * 
    * @param runInBackground true to run the import in the background
    */
   public void setRunInBackground(boolean runInBackground)
   {
      this.runInBackground = runInBackground;
   }

   /**
    * Sets the BrowseBean instance to use to retrieve the current document
    * 
    * @param browseBean BrowseBean instance
    */
   public void setBrowseBean(BrowseBean browseBean)
   {
      this.browseBean = browseBean;
   }
   
   /**
    * Sets the action service
    * 
    * @param actionService  the action service
    */
   public void setActionService(ActionService actionService)
   {
      this.actionService = actionService;
   }
   
   /**
    * Sets the node service
    * 
    * @param nodeService  the node service
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }
   
   /**
    * Sets the content service
    * 
    * @param contentService the content service
    */
   public void setContentService(ContentService contentService)
   {
      this.contentService = contentService;
   }
   
   /**
    * Sets the mimetype sevice
    * 
    * @param mimetypeService the mimetype service
    */
   public void setMimetypeService(MimetypeService mimetypeService)
   {
      this.mimetypeService = mimetypeService;
   }
   
   /**
    * Adds the uploaded ACP/ZIP file to the repository
    *  
    * @param context Faces context
    * @return NodeRef representing the ACP/ZIP file in the repository
    */
   private NodeRef addFileToRepository(FacesContext context)
   {
      // set the name for the new node
      Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>(1);
      contentProps.put(ContentModel.PROP_NAME, this.fileName);
      
      // create the node to represent the zip file
      String assocName = QName.createValidLocalName(this.fileName);
      ChildAssociationRef assocRef = this.nodeService.createNode(
           this.browseBean.getActionSpace().getNodeRef(), ContentModel.ASSOC_CONTAINS,
           QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, assocName),
           ContentModel.TYPE_CONTENT, contentProps);
      
      NodeRef acpNodeRef = assocRef.getChildRef();
      
      // apply the titled aspect to behave in the web client
      String mimetype = this.mimetypeService.guessMimetype(this.fileName);
      Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>(2, 1.0f);
      titledProps.put(ContentModel.PROP_TITLE, this.fileName);
      titledProps.put(ContentModel.PROP_DESCRIPTION,
            MimetypeMap.MIMETYPE_ACP.equals(mimetype) ?
               Application.getMessage(context, "import_acp_description") :
               Application.getMessage(context, "import_zip_description"));
      this.nodeService.addAspect(acpNodeRef, ContentModel.ASPECT_TITLED, titledProps);
      
      // add the content to the node
      ContentWriter writer = this.contentService.getWriter(acpNodeRef, ContentModel.PROP_CONTENT, true);
      writer.setEncoding(this.encoding);
      writer.setMimetype(mimetype);
      writer.putContent(this.file);
      
      return acpNodeRef;
   }
}
