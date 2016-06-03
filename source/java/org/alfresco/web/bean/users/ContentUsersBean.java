package org.alfresco.web.bean.users;

import java.text.MessageFormat;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;

/**
 * Concrete implementation providing access to users of the current content/document.
 * 
 * @author gavinc
 */
public class ContentUsersBean extends UserMembersBean
{
   private static final long serialVersionUID = 5206400236997654181L;

   private final static String MSG_MANAGE_CONTENT_USERS = "manage_content_users";
   private final static String MSG_CONTENT_OWNER = "content_owner";
   private final static String MSG_CLOSE= "close";
   private final static String MSG_LEFT_QUOTE = "left_qoute";
   private final static String MSG_RIGHT_QUOTE = "right_quote";
   
   /**
    * @return The space to work against
    */
   public Node getNode()
   {
      return this.browseBean.getDocument();
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
       return Application.getMessage(fc, MSG_MANAGE_CONTENT_USERS) + " " + Application.getMessage(fc, MSG_LEFT_QUOTE)
               + browseBean.getDocument().getName() + Application.getMessage(fc, MSG_RIGHT_QUOTE);
   }
   
   @Override
   public String getContainerSubTitle()
   {
      String pattern = Application.getMessage(FacesContext.getCurrentInstance(), MSG_CONTENT_OWNER);
      return MessageFormat.format(pattern, getOwner());
   }
   
   @Override
   public String getCancelButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_CLOSE);
   }
}
