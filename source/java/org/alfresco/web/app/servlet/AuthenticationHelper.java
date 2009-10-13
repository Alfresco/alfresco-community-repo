/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.app.servlet;

import java.io.IOException;
import java.util.Enumeration;

import javax.faces.context.FacesContext;
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
import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
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
import org.alfresco.web.bean.users.UserPreferencesBean;
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
   private static final String AUTHENTICATION_COMPONENT = "AuthenticationComponent";
   private static final String REMOTE_USER_MAPPER = "RemoteUserMapper";
   private static final String UNPROTECTED_AUTH_SERVICE = "authenticationService";
   private static final String PERSON_SERVICE = "personService";
   
   /** cookie names */
   private static final String COOKIE_ALFUSER = "alfUser";
   
   private static Log logger = LogFactory.getLog(AuthenticationHelper.class);
   
   
   /**
    * Does all the stuff you need to do after successfully authenticating/validating a user ticket to set up the request
    * thread. A useful utility method for an authentication filter.
    * 
    * @param sc
    *           the servlet context
    * @param req
    *           the request
    * @param res
    *           the response
    */
   public static void setupThread(ServletContext sc, HttpServletRequest req, HttpServletResponse res)
   {
      // setup faces context
      FacesContext fc = FacesHelper.getFacesContext(req, res, sc);
   
      // Set the current locale and language
      if (Application.getClientConfig(fc).isLanguageSelect())
      {
         I18NUtil.setLocale(Application.getLanguage(req.getSession()));
      }
      else
      {
         // Set the current thread locale (also for JSF context)
         fc.getViewRoot().setLocale(BaseServlet.setLanguageFromRequestHeader(req, sc));
      }
   
      // Programatically retrieve the UserPreferencesBean from JSF
      UserPreferencesBean userPreferencesBean = (UserPreferencesBean) fc.getApplication().createValueBinding(
            "#{UserPreferencesBean}").getValue(fc);
      if (userPreferencesBean != null)
      {
         String contentFilterLanguageStr = userPreferencesBean.getContentFilterLanguage();
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
   }

   /**
    * Helper to authenticate the current user using session based Ticket information.
    * <p>
    * User information is looked up in the Session. If found the ticket is retrieved and validated.
    * If no User info is found or the ticket is invalid then a redirect is performed to the login page. 
    * 
    * @param forceGuest       True to force a Guest login attempt
    * 
    * @return AuthenticationStatus result.
    */
   public static AuthenticationStatus authenticate(
         ServletContext sc, HttpServletRequest req, HttpServletResponse res, boolean forceGuest)
         throws IOException
   {
      return authenticate(sc, req, res, forceGuest, true);
   }
   
   /**
    * Helper to authenticate the current user using session based Ticket information.
    * <p>
    * User information is looked up in the Session. If found the ticket is retrieved and validated.
    * If no User info is found or the ticket is invalid then a redirect is performed to the login page. 
    * 
    * @param forceGuest       True to force a Guest login attempt
    * @param allowGuest       True to allow the Guest user if no user object represent
    * 
    * @return AuthenticationStatus result.
    */
   public static AuthenticationStatus authenticate(
         ServletContext sc, HttpServletRequest req, HttpServletResponse res, boolean forceGuest, boolean allowGuest)
         throws IOException
   {
      HttpSession session = req.getSession();
      
      // retrieve the User object
      User user = getUser(sc, req, res);
      
      // get the login bean if we're not in the portal
      LoginBean loginBean = null;
      if (Application.inPortalServer() == false)
      {
         loginBean = (LoginBean)session.getAttribute(LOGIN_BEAN);
      }
      
      // setup the authentication context
      WebApplicationContext wc = WebApplicationContextUtils.getRequiredWebApplicationContext(sc);
      AuthenticationService auth = (AuthenticationService)wc.getBean(AUTHENTICATION_SERVICE);
      
      if (user == null || forceGuest)
      {
         // Check for the session invalidated flag - this is set by the Logout action in the LoginBean
         // it signals a forced Logout and means we should not immediately attempt a relogin as Guest.
         // The attribute is removed from the session by the login.jsp page after the Cookie containing
         // the last stored username string is cleared.
         if (session.getAttribute(AuthenticationHelper.SESSION_INVALIDATED) == null)
         {
            Cookie authCookie = getAuthCookie(req);
            if (allowGuest == true && (authCookie == null || forceGuest))
            {
               // no previous authentication or forced Guest - attempt Guest access
               try
               {
                  auth.authenticateAsGuest();
                  
                  // if we get here then Guest access was allowed and successful
                  setUser(sc, req, PermissionService.GUEST_AUTHORITY, false);
                  
                  // Set up the thread context
                  setupThread(sc, req, res);
                  
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
            session.removeAttribute(AUTHENTICATION_USER);
            return AuthenticationStatus.Failure;
         }
         
         // set last authentication username cookie value
         if (loginBean != null)
         {
            setUsernameCookie(req, res, loginBean.getUsernameInternal());
         }

         // Set up the thread context
         setupThread(sc, req, res);

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
      HttpSession session = httpRequest.getSession();
      try
      {
         auth.validate(ticket);
         
         User user = (User)session.getAttribute(AuthenticationHelper.AUTHENTICATION_USER);
         if (user == null)
         {
            // need to create the User instance if not already available
            String currentUsername = auth.getCurrentUserName();
            
            setUser(context, httpRequest, currentUsername, false);
         }
      }
      catch (AuthenticationException authErr)
      {
         session.removeAttribute(AUTHENTICATION_USER);
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
      
      // Set up the thread context
      setupThread(context, httpRequest, httpResponse);
      
      return AuthenticationStatus.Success;
   }

    /**
     * Creates an object for an authenticated user and stores it in the session.
     * 
     * @param context
     *            the servlet context
     * @param req
     *            the request
     * @param currentUsername
     *            the current user name
     * @param externalAuth
     *            was this user authenticated externally?
     * @return the user object
     */
    public static User setUser(ServletContext context, HttpServletRequest req, String currentUsername,
            boolean externalAuth)
    {
        WebApplicationContext wc = WebApplicationContextUtils.getRequiredWebApplicationContext(context);
        AuthenticationService auth = (AuthenticationService) wc.getBean(AUTHENTICATION_SERVICE);

        User user = createUser(wc, auth, currentUsername, externalAuth);
        // store the User object in the Session - the authentication servlet will then proceed
        HttpSession session = req.getSession(true);
        session.setAttribute(AuthenticationHelper.AUTHENTICATION_USER, user);
        if (externalAuth)
        {
            session.setAttribute(LoginBean.LOGIN_EXTERNAL_AUTH, Boolean.TRUE);
        }
        return user;
    }

    /**
     * Creates an object for an authentication user.
     * 
     * @param wc
     *            the web application context
     * @param auth
     *            the authentication service
     * @param currentUsername
     *            the current user name
     * @param externalAuth
     *            was this user authenticated externally?
     * @return the user object
     */
    private static User createUser(WebApplicationContext wc, AuthenticationService auth, String currentUsername,
            boolean externalAuth)
    {
        UserTransaction tx = null;
        ServiceRegistry services = (ServiceRegistry) wc.getBean(ServiceRegistry.SERVICE_REGISTRY);
        try
        {
            tx = services.getTransactionService().getUserTransaction();
            tx.begin();

            NodeService nodeService = services.getNodeService();
            PersonService personService = (PersonService) wc.getBean(PERSON_SERVICE);
            NodeRef personRef = personService.getPerson(currentUsername);
            User user = new User(currentUsername, auth.getCurrentTicket(), personRef);
            NodeRef homeRef = (NodeRef) nodeService.getProperty(personRef, ContentModel.PROP_HOMEFOLDER);

            // check that the home space node exists - else Login cannot proceed
            if (nodeService.exists(homeRef) == false)
            {
                throw new InvalidNodeRefException(homeRef);
            }
            user.setHomeSpaceId(homeRef.getId());

            tx.commit();

            return user;
        }
        catch (Exception ex)
        {
            logger.error(ex);

            try
            {
                tx.rollback();
            }
            catch (Exception ex2)
            {
                logger.error("Failed to rollback transaction", ex2);
            }

            if (ex instanceof RuntimeException)
            {
                throw (RuntimeException) ex;
            }
            else
            {
                throw new RuntimeException("Failed to set authenticated user", ex);
            }
        }
    }
    
   /**
    * For no previous authentication or forced Guest - attempt Guest access
    * 
    * @param ctx        WebApplicationContext
    * @param auth       AuthenticationService
    */
   public static AuthenticationStatus portalGuestAuthenticate(WebApplicationContext ctx, PortletSession session, AuthenticationService auth)
   {
      try
      {
         auth.authenticateAsGuest();
         
         User user = createUser(ctx, auth, PermissionService.GUEST_AUTHORITY, false);
         
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
      
      return AuthenticationStatus.Failure;
   }
   
   /**
    * Attempts to retrieve the User object stored in the current session.
    * 
    * @param httpRequest The HTTP request
    * @param httpResponse The HTTP response
    * @return The User object representing the current user or null if it could not be found
    */
   @SuppressWarnings("unchecked")
   public static User getUser(ServletContext sc, HttpServletRequest httpRequest, HttpServletResponse httpResponse)
   {
      String userId = null;

      // If the remote user mapper is configured, we may be able to map in an externally authenticated user
      WebApplicationContext wc = WebApplicationContextUtils.getRequiredWebApplicationContext(sc);
      RemoteUserMapper remoteUserMapper = (RemoteUserMapper) wc.getBean(REMOTE_USER_MAPPER);
      if (!(remoteUserMapper instanceof ActivateableBean) || ((ActivateableBean) remoteUserMapper).isActive())
      {
         userId = remoteUserMapper.getRemoteUser(httpRequest);
      }

      HttpSession session = httpRequest.getSession();
      User user = null;

      // examine the appropriate session to try and find the User object
      if (Application.inPortalServer() == false)
      {
         user = (User) session.getAttribute(AUTHENTICATION_USER);
      }
      else
      {
         // naff solution as we need to enumerate all session keys until we find the one that
         // should match our User objects - this is weak but we don't know how the underlying
         // Portal vendor has decided to encode the objects in the session
         Enumeration<String> enumNames = (Enumeration<String>) session.getAttributeNames();
         while (enumNames.hasMoreElements())
         {
            String name = enumNames.nextElement();
            if (name.endsWith(AUTHENTICATION_USER))
            {
               user = (User) session.getAttribute(name);
               break;
            }
         }
      }

      // If the remote user mapper is configured, we may be able to map in an externally authenticated user
      if (userId != null)
      {
         // We have a previously-cached user with the wrong identity - replace them
         if (user != null && !user.getUserName().equals(userId))
         {
            user = null;
         }

         if (user == null)
         {
            // If we have been authenticated by other means, just propagate through the user identity
            if (userId != null)
            {
               AuthenticationComponent authenticationComponent = (AuthenticationComponent) wc
                     .getBean(AUTHENTICATION_COMPONENT);
               authenticationComponent.setCurrentUser(userId);
               user = setUser(sc, httpRequest, userId, true);
            }
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
