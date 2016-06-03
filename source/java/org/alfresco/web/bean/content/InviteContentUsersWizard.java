package org.alfresco.web.bean.content;

import java.util.Set;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.wizard.BaseInviteUsersWizard;

/**
 * Concrete implementation providing the ability to invite users to content.
 * 
 * @author gavinc
 */
public class InviteContentUsersWizard extends BaseInviteUsersWizard
{
   private static final long serialVersionUID = 9198783146031469545L;
   
   @Override
   protected Set<String> getPermissionsForType()
   {
      // Let the permission service do the caching to allow for dynamic model updates, etc.
      return this.permissionService.getSettablePermissions(getNode().getType());
   }

   @Override
   protected Node getNode()
   {
      return this.browseBean.getDocument();
   }

    @Override
    protected String getEmailTemplateXPath()
    {
        FacesContext fc = FacesContext.getCurrentInstance();
        String xpath = Application.getRootPath(fc) + "/" + 
              Application.getGlossaryFolderName(fc) + "/" +
              Application.getEmailTemplatesFolderName(fc) + "/" + 
              Application.getNotifyEmailTemplatesFolderName(fc) + "//*";
        return xpath;
    }
}
