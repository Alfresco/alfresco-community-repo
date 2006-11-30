package org.alfresco.web.bean.wizard;

import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.web.bean.repository.Node;

/**
 * Concrete implementation providing the ability to invite users to content.
 * 
 * @author gavinc
 */
public class InviteContentUsersWizard extends InviteUsersWizard
{
   /** Cache of available content permissions */
   Set<String> contentPermissions = null;
   
   @Override
   protected Set<String> getPermissionsForType()
   {
      if (this.contentPermissions == null)
      {
         this.contentPermissions = this.permissionService.getSettablePermissions(ContentModel.TYPE_CONTENT);
      }
       
      return this.contentPermissions;
   }

   @Override
   protected Node getNode()
   {
      return this.browseBean.getDocument();
   }
}
