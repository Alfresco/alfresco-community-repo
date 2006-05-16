package org.alfresco.web.bean.spaces;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;

/**
 * Dialog bean to create a space. 
 * Uses the CreateSpaceWizard and just overrides the finish button label
 * and the default outcomes.
 * 
 * @author gavinc
 */
public class CreateSpaceDialog extends CreateSpaceWizard
{
   // ------------------------------------------------------------------------------
   // Wizard implementation
   
   @Override
   public String getFinishButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "create_space");
   }
   
   @Override
   protected String getDefaultCancelOutcome()
   {
      return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME;
   }

   @Override
   protected String getDefaultFinishOutcome()
   {
      return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME;
   }
}
