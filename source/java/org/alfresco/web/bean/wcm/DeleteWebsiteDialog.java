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
package org.alfresco.web.bean.wcm;

import java.util.List;

import javax.faces.context.FacesContext;

import org.alfresco.model.WCMAppModel;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.namespace.RegexQNamePattern;
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
      
      if (websiteNode != null)
      {
         // delete all attached website sandboxes in reverse order to the layering
         String storeRoot = (String)websiteNode.getProperties().get(WCMAppModel.PROP_AVMSTORE);
         
         if (storeRoot != null)
         {
            // Notifiy virtualization server about removing this website
            //
            // Implementation note:
            //
            //     Because the removal of virtual webapps in the virtualization 
            //     server is recursive,  it only needs to be given the name of 
            //     the main staging store.  
            //
            //     This notification must occur *prior* to purging content
            //     within the AVM because the virtualization server must list
            //     the avm_webapps dir in each store to discover which 
            //     virtual webapps must be unloaded.  The virtualization 
            //     server traverses the sandbox's stores in most-to-least 
            //     dependent order, so clients don't have to worry about
            //     accessing a preview layer whose main layer has been torn
            //     out from under it.
            //
            //     It does not matter what webapp name we give here, so "/ROOT"
            //     is as sensible as anything else.  It's all going away.
            
            String sandbox = AVMUtil.buildStagingStoreName(storeRoot);
            String path    =  AVMUtil.buildStoreWebappPath(sandbox, "/ROOT");
            AVMUtil.removeVServerWebapp(path, true);
            
            // get the list of users who have a sandbox in the website
            List<ChildAssociationRef> userInfoRefs = nodeService.getChildAssocs(
                  websiteNode.getNodeRef(), WCMAppModel.ASSOC_WEBUSER, RegexQNamePattern.MATCH_ALL);
            for (ChildAssociationRef ref : userInfoRefs)
            {
               String username = (String)nodeService.getProperty(ref.getChildRef(), WCMAppModel.PROP_WEBUSERNAME);

               // delete the preview store for this user
               deleteStore(AVMUtil.buildUserPreviewStoreName(storeRoot, username));

               // delete the main store for this user
               deleteStore(AVMUtil.buildUserMainStoreName(storeRoot, username));
            }
            
            // remove the main staging and preview stores
            deleteStore(AVMUtil.buildStagingPreviewStoreName(storeRoot));
            deleteStore(AVMUtil.buildStagingStoreName(storeRoot));
         }
      }
      
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
   
   /**
    * Returns the message bundle id of the confirmation message to display to 
    * the user before deleting the website.
    * 
    * @return The message bundle id
    */
   @Override
   protected String getConfirmMessageId()
   {
      return "delete_website_confirm";
   }
}
