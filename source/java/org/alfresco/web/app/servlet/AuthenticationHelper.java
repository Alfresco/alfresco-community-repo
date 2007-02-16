/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.app.servlet;

import java.io.IOException;
import java.util.Enumeration;

import javax.portlet.PortletSession;
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
   /** session variables */
   public static final String AUTHENTICATION_USER = "_alfAuthTicket";
   public static final String SESSION_USERNAME = "_alfLastUser";
   public static final String SESSION_INVALIDATED = "_alfSessionInvalid";
   
   /** JSF bean IDs */
   public static final String LOGIN_BEAN = "LoginBean";
   
   /** public service bean IDs **/
   private static final String AUTHENTICATION_SERVICE = "AuthenticationService";
   private static final String UNPROTECTED_AUTH_SERVICE = "authenticationServiceImpl";
   private static final String PERSON_SERVICE = "personService";
   
   /** cookie names */
   private static final String COOKIE_ALFUSER = "alfUser";
   
   /** portal mode key name */
   private static ThreadLocal<String> portalUserKeyName = new ThreadLocal<String>();
   
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
      
      // retrieve the User object
      User user = getUser(httpRequest, httpResponse);
      
      // get the login bean if we're not in the portal
      LoginBean loginBean = null;
      if (Application.inPortalServer() == false)
      {
         loginBean = (LoginBean)session.getAttribute(LOGIN_BEAN);
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
                  ServiceRegistry services = BaseServlet.getServiceRegistry(context);
                  tx = services.getTransactionService().getUserTransaction();
                  tx.begin();
                  
                  NodeService nodeService = services.getNodeService();
                  PersonService personService = (PersonService)wc.getBean(PERSON_SERVICE);
                  NodeRef guestRef = personService.getPerson(PermissionService.GUEST_AUTHORITY);
                  user = new User(PermissionService.GUEST_AUTHORITY, auth.getCurrentTicket(), guestRef);
                  NodeRef guestHomeRef = (NodeRef)nodeService.getProperty(guestRef, ContentModel.PROP_HOMEFOLDER);
                  
                  // check that the home space node exists - else Guest cannot proceed
                  if (nodeService.exists(guestHomeRef) == false)
                  {
                     // cannot login as Guest as Home is missing - return to login screen
                     logger.warn("Unable to locate Guest Home space - may have been deleted?");
                     throw new AuthenticationException("");
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
                  AuthenticationService unprotAuthService = (AuthenticationService)wc.getBean(UNPROTECTED_AUTH_SERVICE);
                  unprotAuthService.invalidateTicket(unprotAuthService.getCurrentTicket());
                  unprotAuthService.clearCurrentSecurityContext();
                  logger.warn("Unable to login as Guest: " + accessError.getMessage());
               }
               catch (Throwable e)
               {
                  // Some other kind of serious failure to report
                  AuthenticationService unprotAuthService = (AuthenticationService)wc.getBean(UNPROTECTED_AUTH_SERVICE);
                  unprotAuthService.invalidateTicket(unprotAuthService.getCurrentTicket());
                  unprotAuthService.clearCurrentSecurityContext();
                  throw new AlfrescoRuntimeException("Failed to authenticate as Guest user.", e);
               }
               finally
               {
                  try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
               }
            }
         }
         
         // session invalidated - return to login screen
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
            // expired ticket
            return AuthenticationStatus.Failure;
         }
         
         // set last authentication username cookie value
         if (loginBean != null)
         {
            setUsernameCookie(httpRequest, httpResponse, loginBean.getUsernameInternal());
         }
         
         // Set the current locale
         I18NUtil.setLocale(Application.getLanguage(httpRequest.getSession()));
         
         // setup faces context
         FacesHelper.getFacesContext(httpRequest, httpResponse, context);
         
         if (loginBean != null && (loginBean.getUserPreferencesBean() != null))
         {
            String contentFilterLanguageStr = loginBean.getUserPreferencesBean().getContentFilterLanguage();
            if (contentFilterLanguageStr != null)
            {
               // Set the locale for the method interceptor for MLText properties
               I18NUtil.setContentLocale(I18NUtil.parseLocale(contentFilterLanguageStr));
            }
            else
            {
               // Nothing has been selected, so remove the content filter
               I18NUtil.setContentLocale(null);
            }
         }

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
      UserTransaction tx = null;
      try
      {
         auth.validate(ticket);
         
         HttpSession session = httpRequest.getSession();
         User user = (User)session.getAttribute(AuthenticationHelper.AUTHENTICATION_USER);
         if (user == null)
         {
            // need to create the User instance if not already available
            String currentUsername = auth.getCurrentUserName();
            
            ServiceRegistry services = BaseServlet.getServiceRegistry(context);
            tx = services.getTransactionService().getUserTransaction();
            tx.begin();
            
            NodeService nodeService = services.getNodeService();
            PersonService personService = (PersonService)wc.getBean(PERSON_SERVICE);
            NodeRef personRef = personService.getPerson(currentUsername);
            user = new User(currentUsername, auth.getCurrentTicket(), personRef);
            NodeRef homeRef = (NodeRef)nodeService.getProperty(personRef, ContentModel.PROP_HOMEFOLDER);
            
            // check that the home space node exists - else Login cannot proceed
            if (nodeService.exists(homeRef) == false)
            {
               throw new InvalidNodeRefException(homeRef);
            }
            user.setHomeSpaceId(homeRef.getId());
            
            tx.commit();
            tx = null;     // clear this so we know not to rollback 
            
            // store the User object in the Session - the authentication servlet will then proceed
            session.setAttribute(AuthenticationHelper.AUTHENTICATION_USER, user);
         }
      }
      catch (AuthenticationException authErr)
      {
         return AuthenticationStatus.Failure;
      }
      catch (Throwable e)
      {
         // Some other kind of serious failure
         AuthenticationService unprotAuthService = (AuthenticationService)wc.getBean(UNPROTECTED_AUTH_SERVICE);
         unprotAuthService.invalidateTicket(unprotAuthService.getCurrentTicket());
         unprotAuthService.clearCurrentSecurityContext();
         return AuthenticationStatus.Failure;
      }
      finally
      {
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
      }
      
      // Set the current locale
      I18NUtil.setLocale(Application.getLanguage(httpRequest.getSession()));
      
      return AuthenticationStatus.Success;
   }
   
   /**
    * For no previous authentication or forced Guest - attempt Guest access
    * 
    * @param ctx        WebApplicationContext
    * @param auth       AuthenticationService
    */
   public static AuthenticationStatus portalGuestAuthenticate(WebApplicationContext ctx, PortletSession session, AuthenticationService auth)
   {
      UserTransaction tx = null;
      try
      {
         auth.authenticateAsGuest();
         
         // if we get here then Guest access was allowed and successful
         ServiceRegistry services = (ServiceRegistry)ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
         tx = services.getTransactionService().getUserTransaction();
         tx.begin();
         
         NodeService nodeService = services.getNodeService();
         PersonService personService = (PersonService)ctx.getBean(PERSON_SERVICE);
         NodeRef guestRef = personService.getPerson(PermissionService.GUEST_AUTHORITY);
         User user = new User(PermissionService.GUEST_AUTHORITY, auth.getCurrentTicket(), guestRef);
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
         I18NUtil.setLocale(Application.getLanguage(session));
         
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
         AuthenticationService unprotAuthService = (AuthenticationService)ctx.getBean(UNPROTECTED_AUTH_SERVICE);
         unprotAuthService.invalidateTicket(unprotAuthService.getCurrentTicket());
         unprotAuthService.clearCurrentSecurityContext();
         logger.warn("Unable to login as Guest: " + accessError.getMessage());
      }
      catch (Throwable e)
      {
         // Some other kind of serious failure to report
         AuthenticationService unprotAuthService = (AuthenticationService)ctx.getBean(UNPROTECTED_AUTH_SERVICE);
         unprotAuthService.invalidateTicket(unprotAuthService.getCurrentTicket());
         unprotAuthService.clearCurrentSecurityContext();
         throw new AlfrescoRuntimeException("Failed to authenticate as Guest user.", e);
      }
      finally
      {
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
      }
      
      return AuthenticationStatus.Failure;
   }
   
   /**
    * Attempts to retrieve the User object stored in the current session.
    * 
    * @param httpRequest The HTTP request
    * @param httpResponse The HTTP response
    * @return The User object representing the current user or null if it could not be found
    */
   public static User getUser(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
   {
      HttpSession session = httpRequest.getSession();
      User user = null;
      
      // examine the appropriate session to try and find the User object
      if (Application.inPortalServer() == false)
      {
         user = (User)session.getAttribute(AUTHENTICATION_USER);
      }
      else
      {
         // naff solution as we need to enumerate all session keys until we find the one that
         // should match our User objects - this is weak but we don't know how the underlying
         // Portal vendor has decided to encode the objects in the session
         if (portalUserKeyName.get() == null)
         {
            String userKeyPostfix = "?" + AUTHENTICATION_USER; 
            Enumeration enumNames = session.getAttributeNames();
            while (enumNames.hasMoreElements())
            {
               String name = (String)enumNames.nextElement();
               if (name.endsWith(userKeyPostfix))
               {
                  // cache the key value once found!
                  portalUserKeyName.set(name);
                  break;
               }
            }
         }
         if (portalUserKeyName.get() != null)
         {
            user = (User)session.getAttribute(portalUserKeyName.get());
         }
      }
      
      return user;
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
