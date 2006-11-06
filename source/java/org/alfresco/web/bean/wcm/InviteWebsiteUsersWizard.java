package org.alfresco.web.bean.wcm;

import java.util.Set;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.wizard.InviteUsersWizard;

/**
 * Concrete implementation providing the ability to invite users to a space.
 * 
 * @author gavinc
 */
public class InviteWebsiteUsersWizard extends InviteUsersWizard
{
   private static final String WIZARD_TITLE_ID = "invite_title";
   private static final String WIZARD_DESC_ID = "invite_desc";
   private static final String STEP1_DESCRIPTION_ID = "invite_step1_desc";
   
   /** Cache of available folder permissions */
   Set<String> folderPermissions = null;
   
   private Node website;
   
   
   /**
    * @see org.alfresco.web.bean.wizard.InviteUsersWizard#init()
    */
   @Override
   public void init()
   {
      super.init();
      // only allow one selection per authority
      allowDuplicateAuthorities = false;
   }

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
         // get permissions and roles for a website folder type
         this.folderPermissions = this.permissionService.getSettablePermissions(ContentModel.TYPE_AVMWEBFOLDER);
      }
       
      return this.folderPermissions;
   }

   protected void setNode(Node node)
   {
      this.website = node;
   }
   
   @Override
   protected Node getNode()
   {
      return this.website;
   }
}
