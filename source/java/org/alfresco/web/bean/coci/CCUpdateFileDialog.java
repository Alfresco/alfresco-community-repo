package org.alfresco.web.bean.coci;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.Application;

public class CCUpdateFileDialog extends CheckinCheckoutDialog
{
   private final static String MSG_UPDATE = "update";
   
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      return updateFileOK(context, outcome);
   }

   @Override
   public String getContainerTitle()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_UPDATE) + " '" + property.getDocument().getName() + "'";
   }
   
   @Override
   public boolean getFinishButtonDisabled()
   {
      return getFileName() == null;
   }
   
   @Override
   public String getFinishButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_UPDATE); 
   }
}
