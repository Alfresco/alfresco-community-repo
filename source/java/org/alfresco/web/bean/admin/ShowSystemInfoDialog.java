package org.alfresco.web.bean.admin;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.Application;
import org.alfresco.web.bean.AboutBean;
import org.alfresco.web.bean.dialog.BaseDialogBean;

/**
 * Implementation for the SystemInfo dialog.
 * 
 * @author gavinc
 */
public class ShowSystemInfoDialog extends BaseDialogBean
{
   private static final long serialVersionUID = 1328587489092603676L;
   
   private static final String MSG_CURRENT_USER = "current_user";
   private static final String MSG_CLOSE = "close";
   private static final String MSG_VERSION = "version";

   protected AboutBean aboutBean; 

   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      return null;
   }

   public void setAboutBean(AboutBean aboutBean)
   {
      this.aboutBean = aboutBean;
   }

   @Override
   public String getContainerSubTitle()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_CURRENT_USER) + ": " + 
               this.navigator.getCurrentUser().getUserName();
   }

   @Override
   public String getContainerDescription()
   {
      StringBuilder builder = new StringBuilder(Application.getMessage(
               FacesContext.getCurrentInstance(), MSG_VERSION));
      builder.append(": ");
      builder.append(this.aboutBean.getEdition());
      builder.append(" - v");
      builder.append(this.aboutBean.getVersion());
      
      return builder.toString();
   }

   @Override
   public String getCancelButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_CLOSE);
   }

}
