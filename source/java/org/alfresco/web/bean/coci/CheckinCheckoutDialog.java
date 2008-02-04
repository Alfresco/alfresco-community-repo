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
package org.alfresco.web.bean.coci;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.alfresco.web.bean.BrowseBean;
import org.alfresco.web.bean.FileUploadBean;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Kevin Roast
 */
public class CheckinCheckoutDialog extends BaseDialogBean
{
   // ------------------------------------------------------------------------------
   // Private data

   private static Log logger = LogFactory.getLog(CheckinCheckoutDialog.class);

   /* /** I18N messages */
   public static final String MSG_ERROR_CHECKIN = "error_checkin";
   public static final String MSG_ERROR_CANCELCHECKOUT = "error_cancel_checkout";
   public static final String MSG_ERROR_UPDATE = "error_update";
   public static final String MSG_ERROR_CHECKOUT = "error_checkout";

   public static final String FILE = "file";

   protected CCProperties property;

   // ------------------------------------------------------------------------------
   // Bean property getters and setters

   /**
    * @param property the property to set
    */
   public void setProperty(CCProperties property)
   {
      this.property = property;
   }

   /**
    * @param navigator The NavigationBean to set.
    */
   public void setNavigator(NavigationBean navigator)
   {
      this.navigator = navigator;
   }

   /**
    * @return Returns the BrowseBean.
    */
   public BrowseBean getBrowseBean()
   {
      return this.browseBean;
   }

   /**
    * @param browseBean The BrowseBean to set.
    */
   public void setBrowseBean(BrowseBean browseBean)
   {
      this.browseBean = browseBean;
   }

   /**
    * @return Returns the NodeService.
    */
   public NodeService getNodeService()
   {
      return this.nodeService;
   }

   /**
    * @param nodeService The NodeService to set.
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }

   public boolean getFinishButtonDisabled()
   {
      return false;
   }

   public String getFinishButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "check_in");
   }

   public String getContainerTitle()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "check_in") + " '" + this.property.getDocument().getName() + "'";
   }

   /**
    * Determines whether the document being checked in has
    * the versionable aspect applied
    *
    * @return true if the versionable aspect is applied
    */
   public boolean isVersionable()
   {
      return property.getDocument().hasAspect(ContentModel.ASPECT_VERSIONABLE);
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
         property.setFile(fileBean.getFile());
         property.setFileName(fileBean.getFileName());
      }

      return property.getFileName();
   }

   /**
    * @param fileName The name of the file
    */
   public void setFileName(String fileName)
   {
      property.setFileName(fileName);

      // we also need to keep the file upload bean in sync
      FacesContext ctx = FacesContext.getCurrentInstance();
      FileUploadBean fileBean = (FileUploadBean)ctx.getExternalContext().getSessionMap().
         get(FileUploadBean.FILE_UPLOAD_BEAN_NAME);
      if (fileBean != null)
      {
         fileBean.setFileName(property.getFileName());
      }
   }

   /**
    * Clear the uploaded form, clearing the specific Upload component by Id
    */
   protected void clearUpload(final String id)
   {
      // remove the file upload bean from the session
      final FacesContext ctx = FacesContext.getCurrentInstance();
      FileUploadBean fileBean = (FileUploadBean)
         ctx.getExternalContext().getSessionMap().get(FileUploadBean.FILE_UPLOAD_BEAN_NAME);
      if (fileBean != null)
      {
         fileBean.setFile(null);
         fileBean.setFileName(null);
      }
   }

   /**
    * Action handler called when the user wishes to remove an uploaded file
    */
   public String removeUploadedFile()
   {
      this.clearUpload(CheckinCheckoutDialog.FILE);
      property.setFileName(null) ;
      property.setFile(null);
      return null;
   }

   // ------------------------------------------------------------------------------
   // Navigation action event handlers

   /**
    * Action event called by all actions that need to setup a Content Document context on the
    * CheckinCheckoutDialog before an action page/wizard is called. The context will be a Node in
    * setDocument() which can be retrieved on action pages via getDocument().
    *
    * @param event   ActionEvent
    */
   public void setupContentAction(ActionEvent event)
   {
      UIActionLink link = (UIActionLink) event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String id = params.get("id");
      if (id != null && id.length() != 0)
      {
         setupContentDocument(id);
      }
      else
      {
         property.setDocument(null);
      }

      resetState();
   }

   public void setupWorkflowContentAction(ActionEvent event)
   {
      // do the common processing
      setupContentAction(event);

      // retrieve the id of the task
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      property.setWorkflowTaskId(params.get("taskId"));

      property.setWorkflowAction(true);

      if (logger.isDebugEnabled())
         logger.debug("Setup for workflow package action for task id: " + property.getWorkflowTaskId());
   }

   /**
    * Setup a content document node context
    *
    * @param id GUID of the node to setup as the content document context
    * @return The Node
    */
   private Node setupContentDocument(String id)
   {
      if (logger.isDebugEnabled())
         logger.debug("Setup for action, setting current document to: " + id);

      Node node = null;

      try
      {
         // create the node ref, then our node representation
         NodeRef ref = new NodeRef(Repository.getStoreRef(), id);
         node = new Node(ref);

         // create content URL to the content download servlet with ID and expected filename
         // the myfile part will be ignored by the servlet but gives the browser a hint
         String url = DownloadContentServlet.generateDownloadURL(ref, node.getName());
         node.getProperties().put("url", url);
         node.getProperties().put("workingCopy", node.hasAspect(ContentModel.ASPECT_WORKING_COPY));
         node.getProperties().put("fileType32", Utils.getFileTypeImage(node.getName(), false));

         // remember the document
         property.setDocument(node);

         // refresh the UI, calling this method now is fine as it basically makes sure certain
         // beans clear the state - so when we finish here other beans will have been reset
         UIContextService.getInstance(FacesContext.getCurrentInstance()).notifyBeans();
      }
      catch (InvalidNodeRefException refErr)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_NODEREF), new Object[] {id}) );
      }

      return node;
   }

   /**
    * Action handler called to calculate which editing screen to display based on the mimetype
    * of a document. If appropriate, the in-line editing screen will be shown.
    */
   public void editFile(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String id = params.get("id");
      if (id != null && id.length() != 0)
      {
         boolean editingInline = false;
         Node node = setupContentDocument(id);

         if (node.hasAspect(WCMAppModel.ASPECT_FORM_INSTANCE_DATA))
         {
            editingInline = true;

            // editable form document
            FacesContext fc = FacesContext.getCurrentInstance();
            this.navigator.setupDispatchContext(node);

            // TODO - rename editContent Wizard since it only deals with editing form content
            fc.getApplication().getNavigationHandler().handleNavigation(fc, null, "wizard:editContent");
         }

         // detect the inline editing aspect to see which edit mode to use
         else if (node.hasAspect(ApplicationModel.ASPECT_INLINEEDITABLE) &&
             node.getProperties().get(ApplicationModel.PROP_EDITINLINE) != null &&
             ((Boolean)node.getProperties().get(ApplicationModel.PROP_EDITINLINE)).booleanValue() == true)
         {
            // retrieve the content reader for this node
            ContentReader reader = property.getContentService().getReader(node.getNodeRef(), ContentModel.PROP_CONTENT);
            if (reader != null)
            {
               editingInline = true;
               String mimetype = reader.getMimetype();

               // calculate which editor screen to display
               if (MimetypeMap.MIMETYPE_TEXT_PLAIN.equals(mimetype) ||
                   MimetypeMap.MIMETYPE_XML.equals(mimetype) ||
                   MimetypeMap.MIMETYPE_TEXT_CSS.equals(mimetype) ||
                   MimetypeMap.MIMETYPE_JAVASCRIPT.equals(mimetype))
               {
                  // make content available to the text editing screen
                  property.setEditorOutput(reader.getContentString());

                  // navigate to appropriate screen
                  FacesContext fc = FacesContext.getCurrentInstance();
                  this.navigator.setupDispatchContext(node);
                  fc.getApplication().getNavigationHandler().handleNavigation(fc, null, "dialog:editTextInline");
               }
               else
               {
                  // make content available to the html editing screen
                  property.setDocumentContent(reader.getContentString());
                  property.setEditorOutput(null);

                  // navigate to appropriate screen
                  FacesContext fc = FacesContext.getCurrentInstance();
                  this.navigator.setupDispatchContext(node);
                  fc.getApplication().getNavigationHandler().handleNavigation(fc, null, "dialog:editHtmlInline");
               }
            }
         }

         if (editingInline == false)
         {
            // normal downloadable document
            FacesContext fc = FacesContext.getCurrentInstance();
            this.navigator.setupDispatchContext(node);
            fc.getApplication().getNavigationHandler().handleNavigation(fc, null, "dialog:editFile");
         }
      }
   }

   /**
    * Action handler called to set the content of a node from an inline editing page.
    */
   public String editInline(FacesContext context, String outcome)
   {
      final Node node = property.getDocument();
      if (node != null)
      {
         try
         {
            if (logger.isDebugEnabled())
               logger.debug("Trying to update content node Id: " + node.getId());

            // get an updating writer that we can use to modify the content on the current node
            ContentWriter writer = property.getContentService().getWriter(node.getNodeRef(), ContentModel.PROP_CONTENT, true);
            writer.putContent(property.getEditorOutput());

            // clean up and clear action context
            resetState();
            property.setDocument(null);
            property.setDocumentContent(null);
            property.setEditorOutput(null);
         }
         catch (Throwable err)
         {
            Utils.addErrorMessage(Application.getMessage(
                  FacesContext.getCurrentInstance(), MSG_ERROR_UPDATE) + err.getMessage());
            outcome = null;
         }
      }
      else
      {
         logger.warn("WARNING: editInlineOK called without a current Document!");
      }
      return outcome;
   }

   /**
    * Action to undo the checkout of a document just checked out from the checkout screen.
    */
   public String undoCheckout()
   {
      String outcome = null;

      Node node = property.getWorkingDocument();
      if (node != null)
      {
         try
         {
            // try to cancel checkout of the working copy
            this.property.getVersionOperationsService().cancelCheckout(node.getNodeRef());

            resetState();

            outcome = AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME;
         }
         catch (Throwable err)
         {
            Utils.addErrorMessage(Application.getMessage(
                  FacesContext.getCurrentInstance(), MSG_ERROR_CANCELCHECKOUT) + err.getMessage(), err);
         }
      }
      else
      {
         logger.warn("WARNING: undoCheckout called without a current WorkingDocument!");
      }

      return outcome;
   }

   /**
    * Action called upon completion of the Check In file page
    */
   public String checkinFileOK(final FacesContext context, String outcome)
   {

      // NOTE: for checkin the document node _is_ the working document!
      final Node node = property.getDocument();
      if (node != null && (property.getCopyLocation().equals(CCProperties.COPYLOCATION_CURRENT) || (this.getFileName() != null && !this.getFileName().equals(""))))
      {
         try
         {
            RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(FacesContext.getCurrentInstance());
            RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
            {
               public Object execute() throws Throwable
               {
                  if (logger.isDebugEnabled())
                     logger.debug("Trying to checkin content node Id: " + node.getId());

                  // we can either checkin the content from the current working copy node
                  // which would have been previously updated by the user
                  String contentUrl;
                  if (property.getCopyLocation().equals(CCProperties.COPYLOCATION_CURRENT))
                  {
                     ContentData contentData = (ContentData) node.getProperties().get(ContentModel.PROP_CONTENT);
                     contentUrl = (contentData == null ? null : contentData.getContentUrl());
                  }
                  // or specify a specific file as the content instead
                  else
                  {
                     // add the content to an anonymous but permanent writer location
                     // we can then retrieve the URL to the content to to be set on the node during checkin
                     ContentWriter writer = property.getContentService().getWriter(node.getNodeRef(), ContentModel.PROP_CONTENT, true);
                     // also update the mime type in case a different type of file is uploaded
                     String mimeType = Repository.getMimeTypeForFileName(context, property.getFileName());
                     writer.setMimetype(mimeType);
                     writer.putContent(property.getFile());
                     contentUrl = writer.getContentUrl();
                  }

                  if (contentUrl == null || contentUrl.length() == 0)
                  {
                     throw new IllegalStateException("Content URL is empty for specified working copy content node!");
                  }

                  // add version history text to props
                  Map<String, Serializable> props = new HashMap<String, Serializable>(1, 1.0f);
                  props.put(Version.PROP_DESCRIPTION, property.getVersionNotes());
                  // set the flag for minor or major change
                  if (property.getMinorChange())
                  {
                     props.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);
                  }
                  else
                  {
                     props.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
                  }

                  // perform the checkin
                  property.getVersionOperationsService().checkin(node.getNodeRef(),
                           props, contentUrl, property.getKeepCheckedOut());
                  return null;
               }
            };
            txnHelper.doInTransaction(callback);

            outcome = AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME;

            if (property.isWorkflowAction() == false)
            {
               outcome = outcome + AlfrescoNavigationHandler.OUTCOME_SEPARATOR + "browse";
            }

            // clear action context
            property.setDocument(null);
            resetState();
         }
         catch (Throwable err)
         {
            Utils.addErrorMessage(Application.getMessage(
                  FacesContext.getCurrentInstance(), MSG_ERROR_CHECKIN) + err.getMessage(), err);
         }
      }
      else
      {
         logger.warn("WARNING: checkinFileOK called without a current Document!");
      }

      return outcome;
   }

   /**
    * Action called upon completion of the Update File page
    */
   public String updateFileOK(final FacesContext context, String outcome)
   {

      // NOTE: for update the document node _is_ the working document!
      final Node node = property.getDocument();
      if (node != null && this.getFileName() != null)
      {
         try
         {
            RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(FacesContext.getCurrentInstance());
            RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
            {
               public Object execute() throws Throwable
               {
                  if (logger.isDebugEnabled())
                     logger.debug("Trying to update content node Id: " + node.getId());

                  // get an updating writer that we can use to modify the content on the current node
                  ContentWriter writer = property.getContentService().getWriter(node.getNodeRef(), ContentModel.PROP_CONTENT, true);

                  // also update the mime type in case a different type of file is uploaded
                  String mimeType = Repository.getMimeTypeForFileName(context, property.getFileName());
                  writer.setMimetype(mimeType);

                  writer.putContent(property.getFile());
                  return null;
               }
            };
            txnHelper.doInTransaction(callback);

            // clear action context
            property.setDocument(null);
            resetState();

            outcome = AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME;
         }
         catch (Throwable err)
         {
            Utils.addErrorMessage(Application.getMessage(
                  FacesContext.getCurrentInstance(), MSG_ERROR_UPDATE) + err.getMessage(), err);
         }
      }
      else
      {
         logger.warn("WARNING: updateFileOK called without a current Document!");
      }

      return outcome;
   }

   /**
    * Deals with the cancel button being pressed on the check in file page
    */
   public String cancel()
   {
      String outcome = getDefaultCancelOutcome();

      FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(AlfrescoNavigationHandler.EXTERNAL_CONTAINER_SESSION);

      // reset the state
      resetState();
      return outcome;
   }

   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {

      return null;
   }

   /**
    * Clear form state and upload file bean
    */
   public  void resetState()
   {
      // delete the temporary file we uploaded earlier
      if (property.getFile() != null)
      {
         property.getFile().delete();
      }

      property.setFile(null);
      property.setFileName(null);
      property.setKeepCheckedOut(false);
      property.setMinorChange(true);
      property.setCopyLocation(CCProperties.COPYLOCATION_CURRENT);
      property.setVersionNotes("");
      property.setSelectedSpaceId(null);
      property.setWorkflowAction(false);
      property.setWorkflowTaskId(null);

      // remove the file upload bean from the session
      FacesContext ctx = FacesContext.getCurrentInstance();
      ctx.getExternalContext().getSessionMap().remove(FileUploadBean.FILE_UPLOAD_BEAN_NAME);
   }
}