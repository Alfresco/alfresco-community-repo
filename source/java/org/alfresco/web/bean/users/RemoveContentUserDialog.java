package org.alfresco.web.bean.users;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;

/**
 * Implementation of remove content user dialog.
 * 
 * @author gavinc
 */
public class RemoveContentUserDialog extends BaseDialogBean
{
   private static final long serialVersionUID = -3090054828215666084L;

   private static final String MSG_REMOVE_USER = "remove_user";
   private final static String MSG_LEFT_QUOTE = "left_qoute";
   private final static String MSG_RIGHT_QUOTE = "right_quote";
   
   private ContentUsersBean contentUsersBean;

   @Override
   public boolean getFinishButtonDisabled()
   {
      return false;
   }

   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      contentUsersBean.removeOK();
      
      return outcome;
   }

   @Override
   public String getContainerTitle()
   {
       FacesContext fc = FacesContext.getCurrentInstance();
       return Application.getMessage(fc, MSG_REMOVE_USER) + " " + Application.getMessage(fc, MSG_LEFT_QUOTE) 
             + this.getPersonName() + Application.getMessage(fc, MSG_RIGHT_QUOTE);
   }
   
   @Override
   public String getContainerSubTitle()
   {
      return this.browseBean.getDocument().getName();
   }

   public void setupUserAction(ActionEvent event)
   {
      this.contentUsersBean.setupUserAction(event);
   }

   public String getPersonName()
   {
      return this.contentUsersBean.getPersonName();
   }

   public void setPersonName(String personName)
   {
      this.contentUsersBean.setPersonName(personName);
   }

   public ContentUsersBean getContentUsersBean()
   {
      return contentUsersBean;
   }

   public void setContentUsersBean(ContentUsersBean contentUsersBean)
   {
      this.contentUsersBean = contentUsersBean;
   }
}
