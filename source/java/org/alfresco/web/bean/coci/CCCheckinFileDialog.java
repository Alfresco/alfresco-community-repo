package org.alfresco.web.bean.coci;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.Application;

public class CCCheckinFileDialog extends CheckinCheckoutDialog
{

   private static final String MSG_CHECK_IN = "check_in";
   
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      return checkinFileOK(context, outcome);
   }
   
   @Override
   public String getFinishButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_CHECK_IN);
   }
   
   @Override
   public String getContainerTitle()
   {
     return Application.getMessage(FacesContext.getCurrentInstance(), MSG_CHECK_IN) + " '" + property.getDocument().getName() + "'";
   }
   
   @Override
   public boolean getFinishButtonDisabled()
   {
      return false;
   }
   
}
