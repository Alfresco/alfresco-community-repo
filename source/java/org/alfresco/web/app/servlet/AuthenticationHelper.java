/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.web.app.servlet;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.portlet.AlfrescoFacesPortlet;
import org.alfresco.web.bean.repository.User;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author Kevin Roast
 */
public final class AuthenticationHelper
{
   public final static String AUTHENTICATION_USER = "_alfAuthTicket";
   
   public static boolean authenticate(ServletContext context, HttpServletRequest httpRequest, HttpServletResponse httpResponse)
      throws IOException
   {
      // examine the appropriate session for our User object
      User user;
      if (Application.inPortalServer() == false)
      {
         user = (User)httpRequest.getSession().getAttribute(AUTHENTICATION_USER);
      }
      else
      {
         user = (User)httpRequest.getSession().getAttribute(AlfrescoFacesPortlet.MANAGED_BEAN_PREFIX + AUTHENTICATION_USER);
      }
      
      if (user == null)
      {
         // no user/ticket - redirect to login page
         httpResponse.sendRedirect(httpRequest.getContextPath() + "/faces" + Application.getLoginPage(context));
         
         return false;
      }
      else
      {
         // setup the authentication context
         WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(context);
         AuthenticationService auth = (AuthenticationService)ctx.getBean("authenticationService");
         try
         {
            auth.validate(user.getTicket());
         }
         catch (AuthenticationException authErr)
         {
            return false;
         }
         
         // Set the current locale
         I18NUtil.setLocale(Application.getLanguage(httpRequest.getSession()));
         
         return true;
      }
   }
   
   public static boolean authenticate(ServletContext context, HttpServletRequest httpRequest, HttpServletResponse httpResponse, String ticket)
      throws IOException
   {
      // setup the authentication context
      WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(context);
      AuthenticationService auth = (AuthenticationService)ctx.getBean("authenticationService");
      try
      {
         auth.validate(ticket);
      }
      catch (AuthenticationException authErr)
      {
         return false;
      }
      
      // Set the current locale
      I18NUtil.setLocale(Application.getLanguage(httpRequest.getSession()));
      
      return true;
   }
}
