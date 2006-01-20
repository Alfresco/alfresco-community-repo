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
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.portlet.AlfrescoFacesPortlet;
import org.alfresco.web.bean.LoginBean;
import org.alfresco.web.bean.repository.User;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Helper to authenticate the current user using available Ticket information.
 * 
 * @author Kevin Roast
 */
public final class AuthenticationHelper
{
   public static final String AUTHENTICATION_USER = "_alfAuthTicket";
   public static final String SESSION_USERNAME = "_alfLastUser";
   public static final String SESSION_INVALIDATED = "_alfSessionInvalid";
   public static final String LOGIN_BEAN = "LoginBean";
   
   private static final String AUTHENTICATION_SERVICE = "authenticationService";
   private static final String COOKIE_ALFUSER = "alfUser";
   
   /**
    * Helper to authenticate the current user using session based Ticket information.
    * <p>
    * User information is looked up in the Session. If found the ticket is retrieved and validated.
    * If no User info is found or the ticket is invalid then a redirect is performed to the login page. 
    * 
    * @return AuthenticationStatus result.
    */
   public static AuthenticationStatus authenticate(
         ServletContext context, HttpServletRequest httpRequest, HttpServletResponse httpResponse)
         throws IOException
   {
      HttpSession session = httpRequest.getSession();
      
      // examine the appropriate session for our User object
      User user;
      LoginBean loginBean = null;
      if (Application.inPortalServer() == false)
      {
         user = (User)session.getAttribute(AUTHENTICATION_USER);
         loginBean = (LoginBean)session.getAttribute(LOGIN_BEAN);
      }
      else
      {
         user = (User)session.getAttribute(AlfrescoFacesPortlet.MANAGED_BEAN_PREFIX + AUTHENTICATION_USER);
      }
      
      // setup the authentication context
      WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(context);
      AuthenticationService auth = (AuthenticationService)ctx.getBean(AUTHENTICATION_SERVICE);
      
      if (user == null)
      {
         if (session.getAttribute(AuthenticationHelper.SESSION_INVALIDATED) == null)
         {
            Cookie authCookie = getAuthCookie(httpRequest);
            if (authCookie == null)
            {
               // TODO: "forced" guest access on URLs!
               // no previous authentication - attempt Guest access first
               UserTransaction tx = null;
               try
               {
                  auth.authenticateAsGuest();
                  
                  // if we get here then Guest access was allowed and successful
                  tx = ((TransactionService)ctx.getBean("TransactionService")).getUserTransaction();
                  tx.begin();
                  
                  PersonService personService = (PersonService)ctx.getBean("personService");
                  NodeService nodeService = (NodeService)ctx.getBean("nodeService");
                  NodeRef guestRef = personService.getPerson(PermissionService.GUEST);
                  user = new User(PermissionService.GUEST, auth.getCurrentTicket(), guestRef);
                  NodeRef guestHomeRef = (NodeRef)nodeService.getProperty(guestRef, ContentModel.PROP_HOMEFOLDER);
                  
                  // check that the home space node exists - else Guest cannot proceed
                  if (nodeService.exists(guestHomeRef) == false)
                  {
                     throw new InvalidNodeRefException(guestHomeRef);
                  }
                  user.setHomeSpaceId(guestHomeRef.getId());
                  
                  tx.commit();
                  
                  // store the User object in the Session - the authentication servlet will then proceed
                  session.setAttribute(AuthenticationHelper.AUTHENTICATION_USER, user);
               
                  // Set the current locale
                  I18NUtil.setLocale(Application.getLanguage(httpRequest.getSession()));
                  
                  return AuthenticationStatus.Guest;
                  
                  // TODO: What now? Any redirects can be performed directly from the appropriate 
                  //       servlet entry points, as we are now authenticated and don't
                  //       need to go through the Login screen to gain authentication.
               }
               catch (AuthenticationException guestError)
               {
                  // Guest access not allowed - continue to login page as usual
               }
               catch (Throwable e)
               {
                  // Guest access not allowed - some other kind of serious failure to report
                  try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
                  throw new AlfrescoRuntimeException("Failed to authenticate as Guest user.", e);
               }
            }
         }
         
         // no user/ticket found or session invalidated by user (logout) - redirect to login page
         httpResponse.sendRedirect(httpRequest.getContextPath() + "/faces" + Application.getLoginPage(context));
         
         return AuthenticationStatus.Failure;
      }
      else
      {
         try
         {
            auth.validate(user.getTicket());
         }
         catch (AuthenticationException authErr)
         {
            // expired ticket - redirect to login page
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/faces" + Application.getLoginPage(context));
            return AuthenticationStatus.Failure;
         }
         
         // set last authentication username cookie value
         if (loginBean != null)
         {
            setUsernameCookie(httpRequest, httpResponse, loginBean.getUsernameInternal());
         }
         
         // Set the current locale
         I18NUtil.setLocale(Application.getLanguage(httpRequest.getSession()));
         
         return AuthenticationStatus.Success;
      }
   }
   
   /**
    * Helper to authenticate the current user using the supplied Ticket value.
    * 
    * @return true if authentication successful, false otherwise.
    */
   public static AuthenticationStatus authenticate(
         ServletContext context, HttpServletRequest httpRequest, HttpServletResponse httpResponse, String ticket)
         throws IOException
   {
      // setup the authentication context
      WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(context);
      AuthenticationService auth = (AuthenticationService)ctx.getBean(AUTHENTICATION_SERVICE);
      try
      {
         auth.validate(ticket);
      }
      catch (AuthenticationException authErr)
      {
         return AuthenticationStatus.Failure;
      }
      
      // Set the current locale
      I18NUtil.setLocale(Application.getLanguage(httpRequest.getSession()));
      
      return AuthenticationStatus.Success;
   }
   
   /**
    * Setup the Alfresco auth cookie value.
    * 
    * @param httpRequest
    * @param httpResponse
    * @param username
    */
   public static void setUsernameCookie(HttpServletRequest httpRequest, HttpServletResponse httpResponse, String username)
   {
      Cookie authCookie = getAuthCookie(httpRequest);
      if (authCookie == null)
      {
         authCookie = new Cookie(COOKIE_ALFUSER, username);
      }
      else
      {
         authCookie.setValue(username);
      }
      authCookie.setPath(httpRequest.getContextPath());
      // TODO: make this configurable - currently 7 days (value in seconds)
      authCookie.setMaxAge(60*60*24*7);
      httpResponse.addCookie(authCookie);
   }
   
   /**
    * Helper to return the Alfresco auth cookie. The cookie saves the last used username value.
    * 
    * @param httpRequest
    * 
    * @return Cookie if found or null if not present
    */
   public static Cookie getAuthCookie(HttpServletRequest httpRequest)
   {
      Cookie authCookie = null;
      Cookie[] cookies = httpRequest.getCookies();
      if (cookies != null)
      {
         for (int i=0; i<cookies.length; i++)
         {
            if (COOKIE_ALFUSER.equals(cookies[i].getName()))
            {
               // found cookie
               authCookie = cookies[i];
               break;
            }
         }
      }
      return authCookie;
   }
}
