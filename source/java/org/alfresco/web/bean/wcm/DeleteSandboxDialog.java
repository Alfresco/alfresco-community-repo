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
               String sandbox   = AVMConstants.buildUserMainStoreName(storeRoot, username);
               String path      = AVMConstants.buildStoreWebappPath(sandbox, this.avmBrowseBean.getWebapp());

               // Notifiy virtualisation server about removing this sandbox.
               //
               // Implementation note:
               //
               //     Because the removal of virtual webapps in the 
               //     virtualization server is recursive,  it only
               //     needs to be given the name of the main store.  
               //
               //     This notification must occur *prior* to purging content
               //     within the AVM because the virtualization server must list
               //     the avm_webapps dir in each store to discover which 
               //     virtual webapps must be unloaded.  The virtualization 
               //     server traverses the sandbox's stores in most-to-least 
               //     dependent order, so clients don't have to worry about
               //     accessing a preview layer whose main layer has been torn
               //     out from under it.

               AVMConstants.removeVServerWebapp(path, true);

               
               // TODO: Use the .sandbox-id.  property to delete all sandboxes,
               //       rather than assume a sandbox always had a single preview
               //       layer attached.

               // purge the user main sandbox store from the system
               this.avmService.purgeStore(sandbox);

               
               // purge the user preview sandbox store from the system
               sandbox = AVMConstants.buildUserPreviewStoreName(storeRoot, username);
               this.avmService.purgeStore(sandbox);
               
               // remove the association to this web project user meta-data
               this.nodeService.removeChild(website.getNodeRef(), ref.getChildRef());
               
               
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
