package org.alfresco.web.bean.coci;

import javax.faces.context.FacesContext;

import org.alfresco.web.bean.repository.Node;

import org.alfresco.web.app.Application;

public class CCUpdateFileDialog extends CheckinCheckoutDialog
{
   private static final long serialVersionUID = 8230565659041530809L;
   
   private final static String MSG_UPDATE = "update";
   private final static String MSG_LEFT_QUOTE = "left_qoute";
   private final static String MSG_RIGHT_QUOTE = "right_quote";
   
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
          FacesContext fc = FacesContext.getCurrentInstance();
          return Application.getMessage(fc, MSG_UPDATE) + " " + Application.getMessage(fc, MSG_LEFT_QUOTE)
              + document.getName() + Application.getMessage(fc, MSG_RIGHT_QUOTE);
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
