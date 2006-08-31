package org.alfresco.web.bean.workflow;

import java.util.List;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.Application;
import org.alfresco.web.config.DialogsConfigElement.DialogButtonConfig;

/**
 * Bean implementation for the "View Completed Task" dialog.
 * 
 * @author gavinc
 */
public class ViewCompletedTaskDialog extends ManageTaskDialog
{
   // ------------------------------------------------------------------------------
   // Dialog implementation

   @Override
   protected String finishImpl(FacesContext context, String outcome)
         throws Exception
   {
      // nothing to do as the finish button is not shown and the dialog is read only
      
      return outcome;
   }

   @Override
   public String getCancelButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "close");
   }
   
   @Override
   public List<DialogButtonConfig> getAdditionalButtons()
   {
      return null;
   }
}
