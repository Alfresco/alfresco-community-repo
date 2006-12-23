package org.alfresco.web.bean.wcm;

import java.text.MessageFormat;
import java.util.List;

import javax.faces.context.FacesContext;

import org.alfresco.model.WCMAppModel;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean implementation for the AVM "Delete Sandbox" dialog
 * 
 * @author kevinr
 */
public class DeleteSandboxDialog extends BaseDialogBean
{
   private static final Log logger = LogFactory.getLog(DeleteSandboxDialog.class);
   
   protected AVMService avmService;
   protected AVMBrowseBean avmBrowseBean;
   
   
   /**
    * @param avmBrowseBean The avmBrowseBean to set.
    */
   public void setAvmBrowseBean(AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
   }

   /**
    * @param avmService    The avmService to set.
    */
   public void setAvmService(AVMService avmService)
   {
      this.avmService = avmService;
   }
   
   
   // ------------------------------------------------------------------------------
   // Dialog implementation
   
   @Override
   protected String finishImpl(FacesContext context, String outcome)
         throws Exception
   {
      // the username for the sandbox to delete
      String username = this.avmBrowseBean.getUsername();
      if (username != null)
      {
         Node website = this.avmBrowseBean.getWebsite();
         
         // remove the store reference from the website folder meta-data
         List<ChildAssociationRef> userInfoRefs = this.nodeService.getChildAssocs(
                  website.getNodeRef(),
                  WCMAppModel.ASSOC_WEBUSER, RegexQNamePattern.MATCH_ALL);
         for (ChildAssociationRef ref : userInfoRefs)
         {
            NodeRef userInfoRef = ref.getChildRef();
            String user = (String)nodeService.getProperty(userInfoRef, WCMAppModel.PROP_WEBUSERNAME);
            String role = (String)nodeService.getProperty(userInfoRef, WCMAppModel.PROP_WEBUSERROLE);
            
            if (username.equals(user))
            {
               // found the sandbox to remove
               String storeRoot = (String)website.getProperties().get(WCMAppModel.PROP_AVMSTORE);
               
               // TODO: would it be better to use the .sandbox-id. property to delete all sandboxes?
               
               // purge the user main sandbox store from the system
               String sandbox = AVMConstants.buildUserMainStoreName(storeRoot, username);
               this.avmService.purgeStore(sandbox);
               
               // purge the user preview sandbox store from the system
               sandbox = AVMConstants.buildUserPreviewStoreName(storeRoot, username);
               this.avmService.purgeStore(sandbox);
               
               // remove the association to this web project user meta-data
               this.nodeService.removeChild(website.getNodeRef(), ref.getChildRef());
               
               // update virtualisation server for the sandbox removal
               String path = AVMConstants.buildStoreWebappPath(sandbox, this.avmBrowseBean.getWebapp());
               AVMConstants.removeVServerWebapp(path, true);
               
               break;
            }
         }
      }
      
      return outcome;
   }
      
   @Override
   protected String doPostCommitProcessing(FacesContext context, String outcome)
   {
      return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME;
   }
   
   @Override
   protected String getErrorMessageId()
   {
      return "error_delete_sandbox";
   }

   @Override
   public boolean getFinishButtonDisabled()
   {
      return false;
   }
   
   
   // ------------------------------------------------------------------------------
   // Bean Getters and Setters

   /**
    * Returns the confirmation to display to the user before deleting the user sandbox.
    * 
    * @return The formatted message to display
    */
   public String getConfirmMessage()
   {
      String fileConfirmMsg = Application.getMessage(FacesContext.getCurrentInstance(), 
               "delete_sandbox_confirm");
      
      return MessageFormat.format(fileConfirmMsg, 
            new Object[] {this.avmBrowseBean.getUsername()});
   }
}
