package org.alfresco.web.bean.admin;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;

public class AdminConsoleDialog extends BaseDialogBean
{
   private static final long serialVersionUID = -2520599525975495006L;
    
   private static final String BUTTON_CLOSE = "close";

    @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      return outcome;
   }
    
    @Override
   public String getCancelButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), BUTTON_CLOSE);
   }
}
