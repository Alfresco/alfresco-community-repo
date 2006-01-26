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
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.portlet.AlfrescoFacesPortlet;
import org.alfresco.web.bean.LoginBean;
import org.alfresco.web.bean.repository.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Helper to authenticate the current user using available Ticket information.
 * <p>
 * User information is looked up in the Session. If found the ticket is retrieved and validated.
 * If the ticket is invalid then a redirect is performed to the login page.
 * <p>
 * If no User info is found then a search will be made for a previous username stored in a Cookie
 * value. If the username if found then a redirect to the Login page will occur. If no username
 * is found then Guest access login will be attempted by the system. Guest access can be forced
 * with the appropriate method call.  
 * 
 * @author Kevin Roast
 */
public final class AuthenticationHelper
{
   public static final String FACES_SERVLET = "/faces";
   
   /** session variables */
   public static final String AUTHENTICATION_USER = "_alfAuthTicket";
   public static final String SESSION_USERNAME = "_alfLastUser";
   public static final String SESSION_INVALIDATED = "_alfSessionInvalid";
   
   /** JSF bean IDs */
   public static final String LOGIN_BEAN = "LoginBean";
   
   /** public service bean IDs **/
   private static final String AUTHENTICATION_SERVICE = "AuthenticationService";
   private static final String UNPROTECTED_AUTH_SERVICE = "authenticationService";
   private static final String PERSON_SERVICE = "personService";
   
   /** cookie names */
   private static final String COOKIE_ALFUSER = "alfUser";
   
   private static Log logger = LogFactory.getLog(AuthenticationHelper.class);
   
   
   /**
    * Helper to authenticate the current user using session based Ticket information.
    * <p>
    * User information is looked up in the Session. If found the ticket is retrieved and validated.
    * If no User info is found or the ticket is invalid then a redirect is performed to the login page. 
    * 
    * @param guest      True to force a Guest login attempt
    * 
    * @return AuthenticationStatus result.
    */
   public static AuthenticationStatus authenticate(
         ServletContext context, HttpServletRequest httpRequest, HttpServletResponse httpResponse, boolean guest)
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
      WebApplicationContext wc = WebApplicationContextUtils.getRequiredWebApplicationContext(context);
      AuthenticationService auth = (AuthenticationService)wc.getBean(AUTHENTICATION_SERVICE);
      
      if (user == null || guest)
      {
         // Check for the session invalidated flag - this is set by the Logout action in the LoginBean
         // it signals a forced Logout and means we should not immediately attempt a relogin as Guest.
         // The attribute is removed from the session by the login.jsp page after the Cookie containing
         // the last stored username string is cleared.
         if (session.getAttribute(AuthenticationHelper.SESSION_INVALIDATED) == null)
         {
            Cookie authCookie = getAuthCookie(httpRequest);
            if (authCookie == null || guest)
            {
               // no previous authentication or forced Guest - attempt Guest access
               UserTransaction tx = null;
               try
               {
                  auth.authenticateAsGuest();
                  
                  // if we get here then Guest access was allowed and successful
                  ServiceRegistry services = ServletHelper.getServiceRegistry(context);
                  tx = services.getTransactionService().getUserTransaction();
                  tx.begin();
                  
                  NodeService nodeService = services.getNodeService();
                  PersonService personService = (PersonService)wc.getBean(PERSON_SERVICE);
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
                  tx = null;     // clear this so we know not to rollback 
                  
                  // store the User object in the Session - the authentication servlet will then proceed
                  session.setAttribute(AuthenticationHelper.AUTHENTICATION_USER, user);
               
                  // Set the current locale
                  I18NUtil.setLocale(Application.getLanguage(httpRequest.getSession()));
                  
                  // remove the session invalidated flag
                  session.removeAttribute(AuthenticationHelper.SESSION_INVALIDATED);
                  
                  // it is the responsibilty of the caller to handle the Guest return status
                  return AuthenticationStatus.Guest;
               }
               catch (AuthenticationException guestError)
               {
                  // Expected if Guest access not allowed - continue to login page as usual
               }
               catch (AccessDeniedException accessError)
               {
                  // Guest is unable to access either properties on Person
                  //AuthenticationService smallAuth = (AuthenticationService)wc.getBean(UNPROTECTED_AUTH_SERVICE);
                  //smallAuth.invalidateTicket(smallAuth.getCurrentTicket());
                  logger.warn("Unable to login as Guest: " + accessError.getMessage());
               }
               catch (Throwable e)
               {
                  // Some other kind of serious failure to report
                  //AuthenticationService smallAuth = (AuthenticationService)wc.getBean(UNPROTECTED_AUTH_SERVICE);
                  //smallAuth.invalidateTicket(smallAuth.getCurrentTicket());
                  throw new AlfrescoRuntimeException("Failed to authenticate as Guest user.", e);
               }
               finally
               {
                  try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
               }
            }
         }
         
         // no user/ticket found or session invalidated by user (logout) - redirect to login page
         httpResponse.sendRedirect(httpRequest.getContextPath() + FACES_SERVLET + Application.getLoginPage(context));
         
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
            httpResponse.sendRedirect(httpRequest.getContextPath() + FACES_SERVLET + Application.getLoginPage(context));
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
      WebApplicationContext wc = WebApplicationContextUtils.getRequiredWebApplicationContext(context);
      AuthenticationService auth = (AuthenticationService)wc.getBean(AUTHENTICATION_SERVICE);
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
