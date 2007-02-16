/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.web.bean.wcm;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.Pair;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.alfresco.web.bean.CheckinCheckoutBean;
import org.alfresco.web.bean.FileUploadBean;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.forms.Form;
import org.alfresco.web.forms.FormInstanceData;
import org.alfresco.web.forms.FormInstanceDataImpl;
import org.alfresco.web.forms.FormProcessor;
import org.alfresco.web.forms.Rendition;
import org.alfresco.web.forms.RenditionImpl;
import org.alfresco.web.forms.XMLUtil;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

/**
 * Bean backing the edit pages for a AVM node content.
 * 
 * @author Ariel Backenroth
 * @author Kevin Roast
 */
public class AVMEditBean
{
   private static final Log LOGGER = LogFactory.getLog(AVMEditBean.class);
   
   private static final String MSG_ERROR_UPDATE = "error_update";
   private static final String MSG_UPLOAD_SUCCESS = "file_upload_success";
   
   private String documentContent = null;
   private Document instanceDataDocument = null;
   private String editorOutput = null;
   
   private File file = null;
   private String fileName = null;
   protected FormProcessor.Session formProcessorSession = null;
   private Form form = null;

   /** AVM service bean reference */
   protected AVMService avmService;

   /** AVM sync service bean reference */
   protected AVMSyncService avmSyncService;
   
   /** AVM Browse Bean reference */
   protected AVMBrowseBean avmBrowseBean;
   
   /** The ContentService bean reference */
   protected ContentService contentService;
   
   /** The NodeService bean reference */
   protected NodeService nodeService;
   
   
   // ------------------------------------------------------------------------------
   // Bean property getters and setters 
   
   /**
    * @param avmService       The AVMService to set.
    */
   public void setAvmService(AVMService avmService)
   {
      this.avmService = avmService;
   }

   /**
    * @param avmSyncService       The AVMSyncService to set.
    */
   public void setAvmSyncService(AVMSyncService avmSyncService)
   {
      this.avmSyncService = avmSyncService;
   }
   
   /**
    * @param avmBrowseBean    The AVMBrowseBean to set.
    */
   public void setAvmBrowseBean(AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
   }

   /**
    * @param contentService   The ContentService to set.
    */
   public void setContentService(ContentService contentService)
   {
      this.contentService = contentService;
   }
   
   /**
    * @param nodeService      The nodeService to set.
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }

   /**
    * @return Returns the current AVM node context.
    */
   public AVMNode getAvmNode()
   {
      return this.avmBrowseBean.getAvmActionNode();
   }
   
   /**
    * @return Large file icon for current AVM node
    */
   public String getFileType32()
   {
      return Utils.getFileTypeImage(getAvmNode().getName(), false);
   }
   
   /**
    * @return Small file icon for current AVM node
    */
   public String getFileType16()
   {
      return Utils.getFileTypeImage(getAvmNode().getName(), true);
   }
   
   /**
    * @return Content URL for current AVM node
    */
   public String getUrl()
   {
      return DownloadContentServlet.generateDownloadURL(AVMNodeConverter.ToNodeRef(-1, getAvmNode().getPath()), 
                                                        getAvmNode().getName());
   }
   
   /**
    * @return Returns the document content used for HTML in-line editing.
    */
   public String getDocumentContent()
   {
      return this.documentContent;
   }
   
   /**
    * @param documentContent     The document content for HTML in-line editing.
    */
   public void setDocumentContent(String documentContent)
   {
      this.documentContent = documentContent;
   }
   
   /**
    * @return Returns output from the in-line editor page.
    */
   public String getEditorOutput()
   {
      return this.editorOutput;
   }

   /**
    * @param editorOutput  The output from the in-line editor page
    */
   public void setEditorOutput(String editorOutput)
   {
      this.editorOutput = editorOutput;
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
    * @return Returns the message to display when a file has been uploaded
    */
   public String getFileUploadSuccessMsg()
   {
      String msg = Application.getMessage(FacesContext.getCurrentInstance(), MSG_UPLOAD_SUCCESS);
      return MessageFormat.format(msg, new Object[] {getFileName()});
   }
   
   /**
    * @return Returns the form when in the context of editing an xml asset.
    */
   public Form getForm()
   {
      if (this.form == null)
      {
         final PropertyValue pv = 
            this.avmService.getNodeProperty(-1, 
                                            this.getAvmNode().getPath(), 
                                            WCMAppModel.PROP_PARENT_FORM_NAME);
         
         final String formName = (String)pv.getValue(DataTypeDefinition.TEXT);
         final WebProject wp = new WebProject(this.getAvmNode().getPath());
         this.form = wp.getForm(formName);

         if (LOGGER.isDebugEnabled())
             LOGGER.debug("loaded form " + this.form + 
                          ", form name " + formName +
                          ", for " + this.getAvmNode().getPath());
      }
      return this.form;
   }

   /**
    * @return Returns the wrapper instance data for feeding the xml
    * content to the form processor.
    */
   public Document getInstanceDataDocument()
   {
      if (this.instanceDataDocument == null)
      {
         final String content = this.getEditorOutput();
         try
         {
            this.instanceDataDocument = (content != null 
                                         ? XMLUtil.parse(content) 
                                         : XMLUtil.newDocument());
         }
         catch (Exception e)
         {
            Utils.addErrorMessage("error parsing document", e);
            return XMLUtil.newDocument();
         }
      }
      return this.instanceDataDocument;
   }

   /**
    * Returns the form processor session.
    */
   public FormProcessor.Session getFormProcessorSession()
   {
      return this.formProcessorSession;
   }

   /**
    * Sets the form processor session.
    */
   public void setFormProcessorSession(final FormProcessor.Session formProcessorSession)
   {
      if (this.formProcessorSession != null &&
          this.formProcessorSession != formProcessorSession)
      {
         this.formProcessorSession.destroy();
      }
      this.formProcessorSession = formProcessorSession;
   }

   // ------------------------------------------------------------------------------
   // Action event handlers
   
   /**
    * Action handler called to calculate which editing screen to display based on the mimetype
    * of a document. If appropriate, the in-line editing screen will be shown.
    */
   public void setupEditAction(ActionEvent event)
   {
      this.avmBrowseBean.setupContentAction(event);
      
      // retrieve the content reader for this node
      String avmPath = getAvmNode().getPath();
      if (this.avmService.hasAspect(-1, avmPath, WCMAppModel.ASPECT_RENDITION))
      {
         if (LOGGER.isDebugEnabled())
            LOGGER.debug(avmPath + " is a rendition, editing primary rendition instead");

         try
         {
            final FormInstanceData fid = 
               new RenditionImpl(AVMNodeConverter.ToNodeRef(-1, avmPath)).getPrimaryFormInstanceData();
            avmPath = fid.getPath();

            if (LOGGER.isDebugEnabled())
               LOGGER.debug("Editing primary form instance data " + avmPath);

            this.avmBrowseBean.setAvmActionNode(new AVMNode(this.avmService.lookup(-1, avmPath)));
         }
         catch (FileNotFoundException fnfe)
         {
            this.avmService.removeAspect(avmPath, WCMAppModel.ASPECT_RENDITION);
            this.avmService.removeAspect(avmPath, WCMAppModel.ASPECT_FORM_INSTANCE_DATA);
            Utils.addErrorMessage(fnfe.getMessage(), fnfe);
         }
      }

      if (this.avmService.hasAspect(-1, avmPath, WCMAppModel.ASPECT_FORM_INSTANCE_DATA))
      {
         // reset the preview layer
         String storeName = AVMConstants.getStoreName(avmPath);
         storeName = AVMConstants.getCorrespondingPreviewStoreName(storeName);
         final String path = AVMConstants.buildStoreRootPath(storeName);

         if (LOGGER.isDebugEnabled())
             LOGGER.debug("reseting layer " + path);

         this.avmSyncService.resetLayer(path);
      }

      if (LOGGER.isDebugEnabled())
          LOGGER.debug("Editing AVM node: " + avmPath);

      ContentReader reader = this.avmService.getContentReader(-1, avmPath);
      if (reader != null)
      {
         String mimetype = reader.getMimetype();
         String outcome = null;
         // calculate which editor screen to display
         if (MimetypeMap.MIMETYPE_XML.equals(mimetype) && 
             this.avmService.hasAspect(-1, avmPath, WCMAppModel.ASPECT_FORM_INSTANCE_DATA))
         {
            // make content available to the editing screen
            this.setEditorOutput(reader.getContentString());
            this.setFormProcessorSession(null);
            this.instanceDataDocument = null;
            this.form = null;
            
            // navigate to appropriate screen
            outcome = "dialog:editAvmXmlInline";
         }
         else
         {
            // normal downloadable document
            outcome = "dialog:editAvmFile";
         }
         
         final FacesContext fc = FacesContext.getCurrentInstance();
         fc.getApplication().getNavigationHandler().handleNavigation(fc, null, outcome);
      }
   }
   
   /**
    * Action called upon completion of the Edit File download page
    */
   public String editFileOK()
   {
      String outcome = null;
      
      AVMNode node = getAvmNode();
      if (node != null)
      {
         // Possibly notify virt server
         AVMConstants.updateVServerWebapp(node.getPath(), false);

         resetState();
         outcome = AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME;
      }
      
      return outcome;
   }
   
   /**
    * Action handler called to set the content of a node from an inline editing page.
    */
   public String editInlineOK()
   {
      UserTransaction tx = null;
      final AVMNode avmNode = getAvmNode();
      if (avmNode == null)
      {
         return null;
      }
      
      final String avmPath = avmNode.getPath();

      if (LOGGER.isDebugEnabled())
          LOGGER.debug("saving " + avmPath);

      try
      {
         tx = Repository.getUserTransaction(FacesContext.getCurrentInstance());
         tx.begin();
         
         // get an updating writer that we can use to modify the content on the current node
         final ContentWriter writer = this.avmService.getContentWriter(avmPath);
         if (this.avmService.hasAspect(-1, avmPath, WCMAppModel.ASPECT_FORM_INSTANCE_DATA))
         {
            this.editorOutput = XMLUtil.toString(this.instanceDataDocument, false);
         }
         writer.putContent(this.editorOutput);
         
         // commit the transaction
         tx.commit();
         
         // regenerate form content
         if (this.avmService.hasAspect(-1, avmPath, WCMAppModel.ASPECT_FORM_INSTANCE_DATA))
         {
            final FormInstanceData fid = new FormInstanceDataImpl(AVMNodeConverter.ToNodeRef(-1, avmPath))
            {
               @Override
               public Form getForm() { return AVMEditBean.this.getForm(); }
            };

            if (LOGGER.isDebugEnabled())
                LOGGER.debug("regenerating renditions of " + fid);

            for (Rendition rendition : fid.getRenditions())
            {
               try
               {
                  rendition.regenerate(fid);
               }
               catch (Exception e)
               {

                  Utils.addErrorMessage("error regenerating " + rendition.getName() + 
                                        " using " + rendition.getRenderingEngineTemplate().getName() + 
                                        ": " + e.getMessage(),
                                        e);
               }
            }
            final NodeRef[] uploadedFiles = this.formProcessorSession.getUploadedFiles();

            if (LOGGER.isDebugEnabled())
                LOGGER.debug("updating " + uploadedFiles.length + " uploaded files");

            final List<AVMDifference> diffList = new ArrayList<AVMDifference>(uploadedFiles.length);
            for (NodeRef uploadedFile : uploadedFiles)
            {
               final String path = AVMNodeConverter.ToAVMVersionPath(uploadedFile).getSecond();
               diffList.add(new AVMDifference(-1, path,
                                              -1, AVMConstants.getCorrespondingPathInMainStore(path),
                                              AVMDifference.NEWER));
            }
            this.avmSyncService.update(diffList, null, true, true, true, true, null, null);
         }
            
         // Possibly notify virt server
         AVMConstants.updateVServerWebapp(avmNode.getPath(), false);
         
         resetState();
         
         return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME;
      }
      catch (Throwable err)
      {
         // rollback the transaction
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
         Utils.addErrorMessage(Application.getMessage(
                                  FacesContext.getCurrentInstance(), CheckinCheckoutBean.MSG_ERROR_UPDATE) + err.getMessage());
         return null;
      }
   }
   
   /**
    * Action called upon completion of the Update File page
    */
   public String updateFileOK()
   {
      String outcome = null;
      
      UserTransaction tx = null;
      
      AVMNode node = getAvmNode();
      if (node != null && this.getFileName() != null)
      {
         try
         {
            FacesContext context = FacesContext.getCurrentInstance();
            tx = Repository.getUserTransaction(context);
            tx.begin();
            
            // get an updating writer that we can use to modify the content on the current node
            ContentWriter writer = this.contentService.getWriter(node.getNodeRef(), ContentModel.PROP_CONTENT, true);
            
            // also update the mime type in case a different type of file is uploaded
            String mimeType = Repository.getMimeTypeForFileName(context, this.fileName);
            writer.setMimetype(mimeType);
            
            writer.putContent(this.file);            
            
            // commit the transaction
            tx.commit();

            // Possibly notify virt server
            AVMConstants.updateVServerWebapp(node.getPath(), false);
            
            // clear action context
            resetState();
            
            outcome = AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME;
         }
         catch (Throwable err)
         {
            // rollback the transaction
            try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
            Utils.addErrorMessage(Application.getMessage(
                  FacesContext.getCurrentInstance(), MSG_ERROR_UPDATE) + err.getMessage(), err);
         }
      }
      
      return outcome;
   }
   
   /**
    * Deals with the cancel button being pressed on the upload file page
    */
   public String cancel()
   {
      // reset the state
      resetState();
      
      return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME;
   }
   
   private void resetState()
   {
      // clean up and clear action context
      clearUpload();
      setDocumentContent(null);
      setEditorOutput(null);
      this.setFormProcessorSession(null);
      this.instanceDataDocument = null;
      this.form = null;
   }
   
   /**
    * Clear form state and upload file bean
    */
   private void clearUpload()
   {
      // delete the temporary file we uploaded earlier
      if (this.file != null)
      {
         this.file.delete();
      }
      
      this.file = null;
      this.fileName = null;
      
      // remove the file upload bean from the session
      FacesContext ctx = FacesContext.getCurrentInstance();
      ctx.getExternalContext().getSessionMap().remove(FileUploadBean.FILE_UPLOAD_BEAN_NAME);
   }
}
