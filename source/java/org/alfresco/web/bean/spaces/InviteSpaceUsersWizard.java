package org.alfresco.web.bean.spaces;

import java.util.Set;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.wizard.BaseInviteUsersWizard;

/**
 * Concrete implementation providing the ability to invite users to a space.
 * 
 * @author gavinc
 */
public class InviteSpaceUsersWizard extends BaseInviteUsersWizard
{
   private static final long serialVersionUID = -1584891656721183347L;
   
   @Override
   protected Set<String> getPermissionsForType()
   {
      // Let the permission service do the caching to allow for dynamic model updates, etc.
      return this.permissionService.getSettablePermissions(getNode().getType());
   }

   @Override
   protected Node getNode()
   {
      return this.browseBean.getActionSpace();
   }

    @Override
    protected String getEmailTemplateXPath()
    {
        FacesContext fc = FacesContext.getCurrentInstance();
        String xpath = Application.getRootPath(fc) + "/" + 
              Application.getGlossaryFolderName(fc) + "/" +
              Application.getEmailTemplatesFolderName(fc) + "/" + 
              Application.getInviteEmailTemplatesFolderName(fc) + "//*";
        return xpath;
    }
}
