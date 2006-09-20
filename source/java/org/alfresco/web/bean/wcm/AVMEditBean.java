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

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.alfresco.web.bean.CheckinCheckoutBean;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.templating.OutputUtil;
import org.alfresco.web.templating.TemplatingService;
import org.alfresco.web.ui.common.Utils;

/**
 * Bean backing the edit pages for a AVM node content.
 * 
 * @author Kevin Roast
 */
public class AVMEditBean
{   
   private String documentContent = null;
   
   private String editorOutput = null;
   
   /** AVM service bean reference */
   protected AVMService avmService;
   
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
      return this.avmBrowseBean.getAvmNode();
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
      return DownloadContentServlet.generateDownloadURL(AVMNodeConverter.ToNodeRef(-1, getAvmNode().getPath()), getAvmNode().getName());
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
      NodeRef avmRef = AVMNodeConverter.ToNodeRef(-1, getAvmNode().getPath());
      ContentReader reader = contentService.getReader(avmRef, ContentModel.PROP_CONTENT);
      if (reader != null)
      {
         String mimetype = reader.getMimetype();
         
         // calculate which editor screen to display
         if (MimetypeMap.MIMETYPE_TEXT_PLAIN.equals(mimetype) ||
             MimetypeMap.MIMETYPE_XML.equals(mimetype) ||
             MimetypeMap.MIMETYPE_TEXT_CSS.equals(mimetype) ||
             MimetypeMap.MIMETYPE_JAVASCRIPT.equals(mimetype))
         {
            // make content available to the editing screen
            setEditorOutput(reader.getContentString());
            
            // navigate to appropriate screen
            FacesContext fc = FacesContext.getCurrentInstance();
            String outcome;
            if (MimetypeMap.MIMETYPE_XML.equals(mimetype))
            {
               outcome = "dialog:editAvmXmlInline";
            }
            else
            {
               outcome = "dialog:editAvmTextInline";
            }
            fc.getApplication().getNavigationHandler().handleNavigation(fc, null, outcome);
         }
         else if (MimetypeMap.MIMETYPE_HTML.equals(mimetype))
         {
            // make content available to the editing screen
            setDocumentContent(reader.getContentString());
            setEditorOutput(null);
            
            // navigate to appropriate screen
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.getApplication().getNavigationHandler().handleNavigation(fc, null, "dialog:editAvmHtmlInline");
         }
         else
         {
            // normal downloadable document
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.getApplication().getNavigationHandler().handleNavigation(fc, null, "dialog:editAvmFile");
         }
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
      String outcome = null;
      
      UserTransaction tx = null;
      
      AVMNode avmNode = getAvmNode();
      if (avmNode != null)
      {
         NodeRef avmRef = AVMNodeConverter.ToNodeRef(-1, getAvmNode().getPath());
         try
         {
            tx = Repository.getUserTransaction(FacesContext.getCurrentInstance());
            tx.begin();
            
            // get an updating writer that we can use to modify the content on the current node
            ContentWriter writer = this.contentService.getWriter(avmRef, ContentModel.PROP_CONTENT, true);
            writer.putContent(this.editorOutput);
            
            // commit the transaction
            tx.commit();
            
            // TODO: regenerate template content
            if (nodeService.getProperty(avmRef, TemplatingService.TT_QNAME) != null)
            {
               OutputUtil.regenerate(avmRef,
                     this.contentService,
                     this.nodeService);
            }
            
            resetState();
            
            outcome = AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME;
         }
         catch (Throwable err)
         {
            // rollback the transaction
            try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
            Utils.addErrorMessage(Application.getMessage(
                  FacesContext.getCurrentInstance(), CheckinCheckoutBean.MSG_ERROR_UPDATE) + err.getMessage());
         }
      }
      
      return outcome;
   }
   
   private void resetState()
   {
      // clean up and clear action context
      //clearUpload();
      this.avmBrowseBean.setAvmNode(null);
      setDocumentContent(null);
      setEditorOutput(null);
   }
}
