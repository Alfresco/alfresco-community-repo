/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/lgpl.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
   
   private BrowseBean browseBean;
   private NodeService nodeService;
   private ActionService actionService;
   private ContentService contentService;
   
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
               
               // first of all we need to add the uploaded ACP file to the repository
               NodeRef acpNodeRef = addACPToRepository(context);
               
               // build the action params map based on the bean's current state
               Map<String, Serializable> params = new HashMap<String, Serializable>(3);
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
    * Adds the uploaded ACP file to the repository
    *  
    * @param context Faces context
    * @return NodeRef representing the ACP file in the repository
    */
   private NodeRef addACPToRepository(FacesContext context)
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
      Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>(3, 1.0f);
      titledProps.put(ContentModel.PROP_TITLE, this.fileName);
      titledProps.put(ContentModel.PROP_DESCRIPTION, Application.getMessage(context, "import_package_description"));
      this.nodeService.addAspect(acpNodeRef, ContentModel.ASPECT_TITLED, titledProps);
     
      // add the content to the node
      ContentWriter writer = this.contentService.getWriter(acpNodeRef, ContentModel.PROP_CONTENT, true);
      writer.setEncoding(this.encoding);
      writer.setMimetype(MimetypeMap.MIMETYPE_ACP);
      writer.putContent(this.file);
            
      return acpNodeRef;
   }
}
