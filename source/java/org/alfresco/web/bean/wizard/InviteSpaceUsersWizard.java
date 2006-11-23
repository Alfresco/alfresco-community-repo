package org.alfresco.web.bean.wizard;

import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.web.bean.repository.Node;

/**
 * Concrete implementation providing the ability to invite users to a space.
 * 
 * @author gavinc
 */
public class InviteSpaceUsersWizard extends InviteUsersWizard
{
   /** Cache of available folder permissions */
   Set<String> folderPermissions = null;
   
   @Override
   protected Set<String> getPermissionsForType()
   {
      if (this.folderPermissions == null)
      {
         this.folderPermissions = this.permissionService.getSettablePermissions(ContentModel.TYPE_FOLDER);
      }
       
      return this.folderPermissions;
   }

   @Override
   protected Node getNode()
   {
      return this.browseBean.getActionSpace();
   }
}
