package org.alfresco.web.bean.wizard;

import java.util.Set;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;

/**
 * Concrete implementation providing the ability to invite users to content.
 * 
 * @author gavinc
 */
public class InviteContentUsersWizard extends InviteUsersWizard
{
   private static final String WIZARD_TITLE_ID = "invite_content_title";
   private static final String WIZARD_DESC_ID = "invite_content_desc";
   private static final String STEP1_DESCRIPTION_ID = "invite_content_step1_desc";
   
   /** Cache of available content permissions */
   Set<String> contentPermissions = null;
   
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
