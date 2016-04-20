package org.alfresco.web.bean.users;

import java.text.MessageFormat;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;

/**
 * Concrete implementation providing access to users of the current space.
 * 
 * @author gavinc
 */
public class SpaceUsersBean extends UserMembersBean
{
   private static final long serialVersionUID = -4847219834289259559L;

   private final static String MSG_MANAGE_INVITED_USERS = "manage_invited_users";
   private final static String MSG_SPACE_OWNER = "space_owner";
   private final static String MSG_CLOSE= "close";
   private final static String MSG_LEFT_QUOTE = "left_qoute";
   private final static String MSG_RIGHT_QUOTE = "right_quote";
   
   /**
    * @return The space to work against
    */
   public Node getNode()
   {
      return this.browseBean.getActionSpace();
   }

   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      return null;
   }
   
   @Override
   public Object getActionsContext()
   {
      return getNode();
   }
   
   @Override
   public String getContainerTitle()
   {
       FacesContext fc = FacesContext.getCurrentInstance();
       return Application.getMessage(fc, MSG_MANAGE_INVITED_USERS) + " " + Application.getMessage(fc, MSG_LEFT_QUOTE)
               + browseBean.getActionSpace().getName() + Application.getMessage(fc, MSG_RIGHT_QUOTE);
   }
   
   @Override
   public String getContainerSubTitle()
   {
      String pattern = Application.getMessage(FacesContext.getCurrentInstance(), MSG_SPACE_OWNER);
      return MessageFormat.format(pattern, getOwner());
   }
   
   @Override
   public String getCancelButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_CLOSE);
   }
}
