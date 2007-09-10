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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.web.bean.wcm;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.alfresco.web.bean.FileUploadBean;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.forms.Form;
import org.alfresco.web.forms.FormInstanceData;
import org.alfresco.web.forms.FormInstanceDataImpl;
import org.alfresco.web.forms.FormNotFoundException;
import org.alfresco.web.forms.FormProcessor;
import org.alfresco.web.forms.RenderingEngineTemplate;
import org.alfresco.web.forms.Rendition;
import org.alfresco.web.forms.RenditionImpl;
import org.alfresco.web.forms.XMLUtil;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIActionLink;
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
   public static final String BEAN_NAME = "AVMEditBean";
   
   private static final Log LOGGER = LogFactory.getLog(AVMEditBean.class);
   
   private static final String MSG_ERROR_UPDATE = "error_update";
   private static final String MSG_UPLOAD_SUCCESS = "file_upload_success";
   
   private String documentContent = null;
   private Document instanceDataDocument = null;
   private String editorOutput = null;
   
   private File file = null;
   private String fileName = null;

   /** AVM service bean reference */
   protected AVMService avmService;

   /** AVM sync service bean reference */
   protected AVMSyncService avmSyncService;
   
   /** AVM Browse Bean reference */
   protected AVMBrowseBean avmBrowseBean;
   
   // ------------------------------------------------------------------------------
   // Bean property getters and setters 
   
   /**
    * @param avmService       The AVMService to set.
    */
   public void setAvmService(final AVMService avmService)
   {
      this.avmService = avmService;
   }

   /**
    * @param avmSyncService       The AVMSyncService to set.
    */
   public void setAvmSyncService(final AVMSyncService avmSyncService)
   {
      this.avmSyncService = avmSyncService;
   }
   
   /**
    * @param avmBrowseBean    The AVMBrowseBean to set.
    */
   public void setAvmBrowseBean(final AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
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
      final FacesContext ctx = FacesContext.getCurrentInstance();
      final FileUploadBean fileBean = (FileUploadBean)
         ctx.getExternalContext().getSessionMap().get(FileUploadBean.FILE_UPLOAD_BEAN_NAME);
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
      final String msg = Application.getMessage(FacesContext.getCurrentInstance(), MSG_UPLOAD_SUCCESS);
      return MessageFormat.format(msg, new Object[] { this.getFileName() });
   }

   // ------------------------------------------------------------------------------
   // Action event handlers
   
   /**
    * Action handler called to calculate which editing screen to display based on the mimetype
    * of a document. If appropriate, the in-line editing screen will be shown.
    */
   public void setupEditAction(final ActionEvent event)
   {
      final UIActionLink link = (UIActionLink)event.getComponent();
      final Map<String, String> params = link.getParameterMap();
      this.setupEditAction(params.get("id"));
   }
   
   /**
    * Action handler called to calculate which editing screen to display based on the mimetype
    * of a document. If appropriate, the in-line editing screen will be shown.
    */
   public void setupEditAction(String path)
   {
      this.avmBrowseBean.setupContentAction(path, true);
      
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
         String storeName = AVMUtil.getStoreName(avmPath);
         storeName = AVMUtil.getCorrespondingPreviewStoreName(storeName);
         final String rootPath = AVMUtil.buildStoreRootPath(storeName);

         if (LOGGER.isDebugEnabled())
             LOGGER.debug("reseting layer " + rootPath);

         this.avmSyncService.resetLayer(rootPath);
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
            
            // navigate to appropriate screen
            outcome = "wizard:editWebContent";
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
         AVMUtil.updateVServerWebapp(node.getPath(), false);

         resetState();
         outcome = AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME;
      }
      
      return outcome;
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
            final ContentWriter writer = this.avmService.getContentWriter(node.getPath());
            
            // also update the mime type in case a different type of file is uploaded
            String mimeType = Repository.getMimeTypeForFileName(context, this.fileName);
            writer.setMimetype(mimeType);
            writer.putContent(this.file);            
            
            // commit the transaction
            tx.commit();
            if (this.avmService.hasAspect(-1, node.getPath(), WCMAppModel.ASPECT_FORM_INSTANCE_DATA))
            {
               this.regenerateRenditions();
            }
            // Possibly notify virt server
            AVMUtil.updateVServerWebapp(node.getPath(), false);
            
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
      this.resetState();
      
      return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME;
   }
   
   private void resetState()
   {
      // clean up and clear action context
      clearUpload();
      setDocumentContent(null);
      setEditorOutput(null);
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

   private void regenerateRenditions()
      throws FormNotFoundException
   {
      final String avmPath = this.getAvmNode().getPath();
      final FormInstanceData fid = new FormInstanceDataImpl(-1, avmPath)
      {
         @Override
         public Form getForm() 
            throws FormNotFoundException
         {
            final WebProject wp = new WebProject(this.getPath());
            Form f = super.getForm();
            try
            {
               return wp.getForm(f.getName());
            }
            catch (FormNotFoundException fnfe)
            {
               throw new FormNotFoundException(f, wp, this);
            }
         }
      };
      final List<FormInstanceData.RegenerateResult> result = fid.regenerateRenditions();
      for (FormInstanceData.RegenerateResult rr : result)
      {
         if (rr.getException() != null)
         {
            Utils.addErrorMessage("error regenerating rendition using " + rr.getRenderingEngineTemplate().getName() + 
                                  ": " + rr.getException().getMessage(),
                                  rr.getException());
         }
      }
   }
}
