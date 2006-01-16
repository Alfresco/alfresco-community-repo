package org.alfresco.web.bean.wizard;

import java.util.Set;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;

/**
 * Concrete implementation providing the ability to invite users to a space.
 * 
 * @author gavinc
 */
public class InviteSpaceUsersWizard extends InviteUsersWizard
{
   private static final String WIZARD_TITLE_ID = "invite_title";
   private static final String WIZARD_DESC_ID = "invite_desc";
   private static final String STEP1_DESCRIPTION_ID = "invite_step1_desc";
   
   /** Cache of available folder permissions */
   Set<String> folderPermissions = null;
   
   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#getWizardDescription()
    */
   public String getWizardDescription()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), WIZARD_DESC_ID);
   }

   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#getWizardTitle()
    */
   public String getWizardTitle()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), WIZARD_TITLE_ID);
   }

   @Override
   protected String getStep1DescriptionText()
   {
      return STEP1_DESCRIPTION_ID;
   }
   
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
