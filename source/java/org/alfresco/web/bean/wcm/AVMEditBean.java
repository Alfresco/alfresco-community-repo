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

import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.ui.common.component.UIActionLink;

/**
 * Bean backing the edit pages for a AVM node content.
 * 
 * @author Kevin Roast
 */
public class AVMEditBean
{
   /** Current AVM Node context*/
   private AVMNodeDescriptor avmNode = null;
   
   private String documentContent = null;
   
   private String editorOutput = null;
   
   /** AVM service bean reference */
   protected AVMService avmService;
   
   /** The ContentService bean reference */
   protected ContentService contentService;
   
   
   // ------------------------------------------------------------------------------
   // Bean property getters and setters 
   
   /**
    * @param avmService The AVMService to set.
    */
   public void setAvmService(AVMService avmService)
   {
      this.avmService = avmService;
   }
   
   /**
    * @param contentService   The ContentService to set.
    */
   public void setContentService(ContentService contentService)
   {
      this.contentService = contentService;
   }
   
   /**
    * @return Returns the current AVM node context.
    */
   public AVMNodeDescriptor getAVMNode()
   {
      return this.avmNode;
   }

   /**
    * @param avmNode       The AVM node context to set.
    */
   public void setAVMNode(AVMNodeDescriptor avmNode)
   {
      this.avmNode = avmNode;
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
    * Action event called by all actions that need to setup a Content node context on the 
    * before an action page/wizard is called. The context will be an AVMNodeDescriptor in
    * setAVMNode() which can be retrieved on action pages via getAVMNode().
    * 
    * @param event   ActionEvent
    */
   public void setupContentAction(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String path = params.get("id");
      if (path != null && path.length() != 0)
      {
         setAVMNode(avmService.lookup(-1, path));
      }
      else
      {
         setAVMNode(null);
      }
   }
   
   /**
    * Action handler called to calculate which editing screen to display based on the mimetype
    * of a document. If appropriate, the in-line editing screen will be shown.
    */
   public void setupEditAction(ActionEvent event)
   {
      setupContentAction(event);
      
      // retrieve the content reader for this node
      NodeRef avmRef = AVMNodeConverter.ToNodeRef(-1, getAVMNode().getPath());
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
}
