package org.alfresco.web.bean.coci;

import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.springframework.extensions.webscripts.ui.common.StringUtils;

/**
 * This base dialog class provides methods for online editing. It does
 * doesn't have entry in web-client-config-dialogs.xml as is not instantiated directly.
 */
public class EditOnlineDialog extends CCCheckoutFileDialog
{
   public final static String ONLINE_EDITING = "onlineEditing";

   
   /**
    * Action listener for handle webdav online editing action. E.g "edit_doc_online_webdav" action
    *
    * @param event ActionEvent
    */
   public void handleWebdavEditing(ActionEvent event)
   {
      handle(event);
      
      Node workingCopyNode = property.getDocument();
      if (workingCopyNode != null)
      {
         UIActionLink link = (UIActionLink) event.getComponent();
         Map<String, String> params = link.getParameterMap();
         String webdavUrl = params.get("webdavUrl");
         
         if (webdavUrl != null)
         {
            // modify webDav for editing working copy
            property.setWebdavUrl(webdavUrl.substring(0, webdavUrl.lastIndexOf('/') + 1) + workingCopyNode.getName());
         }
         
         FacesContext fc = FacesContext.getCurrentInstance();
         
         fc.getApplication().getNavigationHandler().handleNavigation(fc, null, "dialog:close:browse");
      }
   }

   /**
    * Action listener for handle cifs online editing action. E.g "edit_doc_online_cifs" action
    *
    * @param event ActionEvent
    */
   public void handleCifsEditing(ActionEvent event)
   {
      handle(event);
      
      Node workingCopyNode = property.getDocument();
      if (workingCopyNode != null)
      {
         UIActionLink link = (UIActionLink) event.getComponent();
         Map<String, String> params = link.getParameterMap();
         String cifsPath = params.get("cifsPath");
         
         if (cifsPath != null)
         {
            // modify cifsPath for editing working copy
            property.setCifsPath(cifsPath.substring(0, cifsPath.lastIndexOf('\\') + 1) + workingCopyNode.getName());
         }
         
         FacesContext fc = FacesContext.getCurrentInstance();
     	   
         fc.getApplication().getNavigationHandler().handleNavigation(fc, null, "dialog:close:browse");
      }
   }

   /**
    * Action listener for handle http online(inline) editing action. E.g "edit_doc_online_http" action
    *
    * @param event ActionEvent
    */
   public void handleHttpEditing(ActionEvent event)
   {
      handle(event);
      
      Node workingCopyNode = property.getDocument();
      if (workingCopyNode != null)
      {
         ContentReader reader = property.getContentService().getReader(workingCopyNode.getNodeRef(), ContentModel.PROP_CONTENT);
         if (reader != null)
         {
            String mimetype = reader.getMimetype();
            
            // calculate which editor screen to display
            if (MimetypeMap.MIMETYPE_TEXT_PLAIN.equals(mimetype) || MimetypeMap.MIMETYPE_XML.equals(mimetype) ||
                MimetypeMap.MIMETYPE_TEXT_CSS.equals(mimetype) || MimetypeMap.MIMETYPE_JAVASCRIPT.equals(mimetype))
            {
               // make content available to the text editing screen
               property.setEditorOutput(reader.getContentString());
               
               // navigate to appropriate screen
               FacesContext fc = FacesContext.getCurrentInstance();
               fc.getApplication().getNavigationHandler().handleNavigation(fc, null, "dialog:close:browse");
               this.navigator.setupDispatchContext(workingCopyNode);
               fc.getApplication().getNavigationHandler().handleNavigation(fc, null, "dialog:editTextInline");
            }
            else
            {
               // make content available to the html editing screen
               property.setDocumentContent(StringUtils.stripUnsafeHTMLTags(reader.getContentString(), false));
               property.setEditorOutput(null);
               
               // navigate to appropriate screen
               FacesContext fc = FacesContext.getCurrentInstance();
               fc.getApplication().getNavigationHandler().handleNavigation(fc, null, "dialog:close:browse");
               this.navigator.setupDispatchContext(workingCopyNode);
               fc.getApplication().getNavigationHandler().handleNavigation(fc, null, "dialog:editHtmlInline");
            }
         }
      }
   }

   /**
    * Base handling method.
    *
    * @param event ActionEvent
    */
   public void handle(ActionEvent event)
   {
      super.setupContentAction(event);
      
      Node node = property.getDocument();
      if (node != null)
      {
         UserTransaction tx = null;
         FacesContext context = FacesContext.getCurrentInstance();
         
         try
         {
            tx = Repository.getUserTransaction(context, false);
            tx.begin();
                        
            // if current content is already working copy then we don't checkout
            if (node.hasAspect(ContentModel.ASPECT_WORKING_COPY) == false)
            {
               // if checkout is successful, then checkoutFile sets property workingDocument
               checkoutFile(FacesContext.getCurrentInstance(), null);
               
               Node workingCopyNode = property.getWorkingDocument();
   
               if (workingCopyNode != null)
               {
                   getRuleService().disableRules();
                   try
                   {
                       // set working copy node as document for editing
                       property.setDocument(workingCopyNode);
                       getNodeService().setProperty(workingCopyNode.getNodeRef(), ContentModel.PROP_WORKING_COPY_MODE, ONLINE_EDITING);
                   }
                   finally
                   {
                       getRuleService().enableRules();
                   }
               }
            }
            
            // commit the transaction
            tx.commit();
         }
         catch (Throwable err)
         {
            try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
            property.setDocument(null);
         }
      }
   }
}