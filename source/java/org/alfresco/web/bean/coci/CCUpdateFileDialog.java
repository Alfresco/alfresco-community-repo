package org.alfresco.web.bean.coci;

import javax.faces.context.FacesContext;

import org.alfresco.web.bean.repository.Node;

import org.alfresco.web.app.Application;

public class CCUpdateFileDialog extends CheckinCheckoutDialog
{
   private static final long serialVersionUID = 8230565659041530809L;
   
   private final static String MSG_UPDATE = "update";
   
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      return updateFileOK(context, outcome);
   }

   @Override
   public String getContainerTitle()
   {
	  Node document = property.getDocument();
	  if(document != null)
	  {
		  return Application.getMessage(FacesContext.getCurrentInstance(), MSG_UPDATE) + " '" + document.getName() + "'";
	  }
	  return null;
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
