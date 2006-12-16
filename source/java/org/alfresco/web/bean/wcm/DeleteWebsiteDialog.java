package org.alfresco.web.bean.wcm;

import java.text.MessageFormat;
import java.util.List;

import javax.faces.context.FacesContext;

import org.alfresco.model.WCMAppModel;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.spaces.DeleteSpaceDialog;

/**
 * Bean implementation for the "Delete Website" dialog.
 * Removes all user stores and the main staging and preview stores.
 * 
 * @author kevinr
 */
public class DeleteWebsiteDialog extends DeleteSpaceDialog
{
   protected AVMService avmService;
   
   // ------------------------------------------------------------------------------
   // Bean property getters and setters 
   
   /**
    * @param avmService       The AVMService to set.
    */
   public void setAvmService(AVMService avmService)
   {
      this.avmService = avmService;
   }
   
   
   // ------------------------------------------------------------------------------
   // Dialog implementation
   
   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#finishImpl(javax.faces.context.FacesContext, java.lang.String)
    */
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      Node websiteNode = this.browseBean.getActionSpace();
      
      // delete all attached website sandboxes in reverse order to the layering
      String storeRoot = (String)websiteNode.getProperties().get(WCMAppModel.PROP_AVMSTORE);
      
      // get the list of users who have a sandbox in the website
      List<ChildAssociationRef> userInfoRefs = nodeService.getChildAssocs(
            websiteNode.getNodeRef(), WCMAppModel.ASSOC_WEBUSER, RegexQNamePattern.MATCH_ALL);
      for (ChildAssociationRef ref : userInfoRefs)
      {
         String username = (String)nodeService.getProperty(ref.getChildRef(), WCMAppModel.PROP_WEBUSERNAME);
         
         // delete the preview store for this user
         deleteStore(AVMConstants.buildAVMUserPreviewStoreName(storeRoot, username));
         
         // delete the main store for this user
         deleteStore(AVMConstants.buildAVMUserMainStoreName(storeRoot, username));
      }
      
      // remove the main staging and preview stores
      deleteStore(AVMConstants.buildAVMStagingPreviewStoreName(storeRoot));
      deleteStore(AVMConstants.buildAVMStagingStoreName(storeRoot));
      
      // use the super implementation to delete the node itself
      return super.finishImpl(context, outcome);
   }
   
   /**
    * Delete a store, checking for its existance first.
    * 
    * @param store
    */
   private void deleteStore(String store)
   {
      // check it exists before we try to remove it
      if (this.avmService.getStore(store) != null)
      {
         this.avmService.purgeStore(store);
      }
   }
   
   
   // ------------------------------------------------------------------------------
   // Bean Getters and Setters
   
   /**
    * Returns the confirmation to display to the user before deleting the content.
    * 
    * @return The formatted message to display
    */
   public String getConfirmMessage()
   {
      String fileConfirmMsg = Application.getMessage(FacesContext.getCurrentInstance(), 
               "delete_website_confirm");
      
      return MessageFormat.format(fileConfirmMsg, 
            new Object[] {this.browseBean.getActionSpace().getName()});
   }
}
