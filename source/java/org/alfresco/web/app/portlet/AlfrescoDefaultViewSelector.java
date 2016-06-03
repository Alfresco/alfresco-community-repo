package org.alfresco.web.app.portlet;

import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.web.app.servlet.AuthenticationHelper;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.repository.User;
import org.apache.myfaces.portlet.DefaultViewSelector;

/**
 * @author Kevin Roast
 */
public class AlfrescoDefaultViewSelector implements DefaultViewSelector
{
   /**
    * Select the appropriate view ID
    */
   public String selectViewId(RenderRequest request, RenderResponse response) throws PortletException
   {
      User user = (User) request.getPortletSession().getAttribute(AuthenticationHelper.AUTHENTICATION_USER,
            PortletSession.APPLICATION_SCOPE);
      if (user != null && user.getUserName().equals(AuthenticationUtil.getGuestUserName()))
      {
         return FacesHelper.BROWSE_VIEW_ID;
      }
      else
      {
         return null;
      }
   }
   
   /**
    * @see org.apache.myfaces.portlet.DefaultViewSelector#setPortletContext(javax.portlet.PortletContext)
    */
   public void setPortletContext(PortletContext portletContext)
   {
   }
}
