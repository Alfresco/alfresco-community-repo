/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.web.app.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.SessionUser;
import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.webdav.auth.RemoteUserMapper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.portlet.AlfrescoFacesPortlet;
import org.alfresco.web.bean.LoginBean;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.bean.users.UserPreferencesBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.Base64;
import org.springframework.extensions.surf.util.I18NUtil;
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
   private static final String COOKIE_ALFUSER = "alfUser0";
   
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
   public static void setupThread(ServletContext sc, HttpServletRequest req, HttpServletResponse res, boolean useInterfaceLanguage)
   {
      // setup faces context
      FacesContext fc = Application.inPortalServer() ? AlfrescoFacesPortlet.getFacesContext(req) : FacesHelper
            .getFacesContext(req, res, sc);
   
      // Set the current locale and language (overriding the one already decoded from the Accept-Language header
      I18NUtil.setLocale(Application.getLanguage(req.getSession(), Application.getClientConfig(fc).isLanguageSelect() && useInterfaceLanguage));
   
      // Programatically retrieve the UserPreferencesBean from JSF
      UserPreferencesBean userPreferencesBean = (UserPreferencesBean) FacesHelper.getManagedBean(fc, "UserPreferencesBean");
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
      // retrieve the User object
      User user = getUser(sc, req, res);
      
      HttpSession session = req.getSession();
      
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
                  setUser(sc, req, AuthenticationUtil.getGuestUserName(), auth.getCurrentTicket(), false);
                  
                  // Set up the thread context
                  setupThread(sc, req, res, true);
                  
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
         // set last authentication username cookie value
         String loginName;
         if (loginBean != null && (loginName = loginBean.getUsernameInternal()) != null)
         {
            setUsernameCookie(req, res, loginName);
         }

         // Set up the thread context
         setupThread(sc, req, res, true);

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
          // If we already have a cached user, make sure it is for the right ticket
         SessionUser user = (SessionUser)session.getAttribute(AuthenticationHelper.AUTHENTICATION_USER);
         if (user != null && !user.getTicket().equals(ticket))
         {
             session.removeAttribute(AUTHENTICATION_USER);
             if (!Application.inPortalServer())
             {
                session.invalidate();
                session = httpRequest.getSession();
             }
             user = null;
         }
         
         // Validate the ticket and associate it with the session
         auth.validate(ticket);

         // Cache a new user in the session if required
         if (user == null)
         {
            setUser(context, httpRequest, auth.getCurrentUserName(), ticket, false);
         }
      }
      catch (AuthenticationException authErr)
      {
         session.removeAttribute(AUTHENTICATION_USER);
         if (!Application.inPortalServer())
         {
            session.invalidate();
         }
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
      
      // As we are authenticating via a ticket, establish the session locale using request headers rather than web client preferences
      setupThread(context, httpRequest, httpResponse, false);
      
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
     * @param ticket
     *            a validated ticket
     * @param externalAuth
     *            was this user authenticated externally?
     * @return the user object
     */
    public static User setUser(ServletContext context, HttpServletRequest req, String currentUsername,
            String ticket, boolean externalAuth)
    {
        WebApplicationContext wc = WebApplicationContextUtils.getRequiredWebApplicationContext(context);

        User user = createUser(wc, currentUsername, ticket);
        // store the User object in the Session - the authentication servlet will then proceed
        HttpSession session = req.getSession(true);
        session.setAttribute(AuthenticationHelper.AUTHENTICATION_USER, user);
        setExternalAuth(session, externalAuth);
        return user;
    }

    /**
     * Sets or clears the external authentication flag on the session
     * 
     * @param session
     *            the session
     * @param externalAuth
     *            was the user authenticated externally?
     */
    private static void setExternalAuth(HttpSession session, boolean externalAuth)
    {
        if (externalAuth)
        {
            session.setAttribute(LoginBean.LOGIN_EXTERNAL_AUTH, Boolean.TRUE);
        }
        else
        {
            session.removeAttribute(LoginBean.LOGIN_EXTERNAL_AUTH);
        }
    }

   /**
    * Creates an object for an authentication user.
    * 
    * @param wc
    *           the web application context
    * @param currentUsername
    *           the current user name
    * @param ticket
    *           a validated ticket
    * @return the user object
    */
   private static User createUser(final WebApplicationContext wc, final String currentUsername, final String ticket)
   {
      final ServiceRegistry services = (ServiceRegistry) wc.getBean(ServiceRegistry.SERVICE_REGISTRY);
      return services.getTransactionService().getRetryingTransactionHelper().doInTransaction(
            new RetryingTransactionHelper.RetryingTransactionCallback<User>()
            {

               public User execute() throws Throwable
               {
                  NodeService nodeService = services.getNodeService();
                  PersonService personService = (PersonService) wc.getBean(PERSON_SERVICE);
                  NodeRef personRef = personService.getPerson(currentUsername);
                  User user = new User(currentUsername, ticket, personRef);
                  NodeRef homeRef = (NodeRef) nodeService.getProperty(personRef, ContentModel.PROP_HOMEFOLDER);

                  // check that the home space node exists - else Login cannot proceed
                  if (nodeService.exists(homeRef) == false)
                  {
                     throw new InvalidNodeRefException(homeRef);
                  }
                  user.setHomeSpaceId(homeRef.getId());
                  return user;
               }
            });
   }
    
   /**
    * For no previous authentication or forced Guest - attempt Guest access
    * 
    * @param ctx
    *           WebApplicationContext
    * @param auth
    *           AuthenticationService
    */
   public static User portalGuestAuthenticate(WebApplicationContext ctx, AuthenticationService auth)
   {
      try
      {
         auth.authenticateAsGuest();

         return createUser(ctx, AuthenticationUtil.getGuestUserName(), auth.getCurrentTicket());
      }
      catch (AuthenticationException guestError)
      {
         // Expected if Guest access not allowed - continue to login page as usual
      }
      catch (AccessDeniedException accessError)
      {
         // Guest is unable to access either properties on Person
         AuthenticationService unprotAuthService = (AuthenticationService) ctx.getBean(UNPROTECTED_AUTH_SERVICE);
         unprotAuthService.invalidateTicket(unprotAuthService.getCurrentTicket());
         unprotAuthService.clearCurrentSecurityContext();
         logger.warn("Unable to login as Guest: " + accessError.getMessage());
      }
      catch (Throwable e)
      {
         // Some other kind of serious failure to report
         AuthenticationService unprotAuthService = (AuthenticationService) ctx.getBean(UNPROTECTED_AUTH_SERVICE);
         unprotAuthService.invalidateTicket(unprotAuthService.getCurrentTicket());
         unprotAuthService.clearCurrentSecurityContext();
         throw new AlfrescoRuntimeException("Failed to authenticate as Guest user.", e);
      }
      return null;
   }

   /**
    * Uses the remote user mapper, if one is configured, to extract a user ID from the request
    * 
    * @param sc
    *           the servlet context
    * @param httpRequest
    *           The HTTP request
    * @return the user ID if a user has been externally authenticated or <code>null</code> otherwise.
    */
   public static String getRemoteUser(final ServletContext sc, final HttpServletRequest httpRequest)
   {
      String userId = null;

      // If the remote user mapper is configured, we may be able to map in an externally authenticated user
      final WebApplicationContext wc = WebApplicationContextUtils.getRequiredWebApplicationContext(sc);
      RemoteUserMapper remoteUserMapper = (RemoteUserMapper) wc.getBean(REMOTE_USER_MAPPER);
      if (!(remoteUserMapper instanceof ActivateableBean) || ((ActivateableBean) remoteUserMapper).isActive())
      {
         userId = remoteUserMapper.getRemoteUser(httpRequest);
      }
      return userId;
   }

   /**
     * Attempts to retrieve the User object stored in the current session.
     * 
     * @param sc
     *            the servlet context
     * @param httpRequest
     *            The HTTP request
     * @param httpResponse
     *            The HTTP response
     * @return The User object representing the current user or null if it could not be found
     */
   public static User getUser(final ServletContext sc, final HttpServletRequest httpRequest, HttpServletResponse httpResponse)
   {
      // If the remote user mapper is configured, we may be able to map in an externally authenticated user
      String userId = getRemoteUser(sc, httpRequest);

      final WebApplicationContext wc = WebApplicationContextUtils.getRequiredWebApplicationContext(sc);
      HttpSession session = httpRequest.getSession();
      User user = null;

      // examine the appropriate session to try and find the User object
      SessionUser sessionUser = Application.getCurrentUser(session);

      // Make sure the ticket is valid, the person exists, and the cached user is of the right type (WebDAV users have
      // been known to leak in but shouldn't now)
      if (sessionUser != null)
      {
         AuthenticationService auth = (AuthenticationService) wc.getBean(AUTHENTICATION_SERVICE);
         try
         {
            auth.validate(sessionUser.getTicket());
            if (sessionUser instanceof User)
            {
               user = (User)sessionUser;
               setExternalAuth(session, userId != null);                  
            }
            else
            {
               user = setUser(sc, httpRequest, sessionUser.getUserName(), sessionUser.getTicket(), userId != null);
            }
         }
         catch (AuthenticationException authErr)
         {
            session.removeAttribute(AUTHENTICATION_USER);
            if (!Application.inPortalServer())
            {
               session.invalidate();
            }
         }
      }
      
      // If the remote user mapper is configured, we may be able to map in an externally authenticated user
      if (userId != null)
      {
         // We have a previously-cached user with the wrong identity - replace them
         if (user != null && !user.getUserName().equals(userId))
         {
             session.removeAttribute(AUTHENTICATION_USER);
             if (!Application.inPortalServer())
             {
                session.invalidate();
             }
             user = null;
         }

         if (user == null)
         {
            // If we have been authenticated by other means, just propagate through the user identity
            AuthenticationComponent authenticationComponent = (AuthenticationComponent) wc
                  .getBean(AUTHENTICATION_COMPONENT);
            try
            {
               authenticationComponent.setCurrentUser(userId);
               AuthenticationService authenticationService = (AuthenticationService) wc.getBean(AUTHENTICATION_SERVICE);
               user = setUser(sc, httpRequest, userId, authenticationService.getCurrentTicket(), true);
            }
            catch (AuthenticationException authErr)
            {
               // Allow for an invalid external user ID to be indicated
               session.removeAttribute(AUTHENTICATION_USER);
               if (!Application.inPortalServer())
               {
                  session.invalidate();
               }
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
      // Let's Base 64 encode the username so it is a legal cookie value
      String encodedUsername;
      try
      {
         encodedUsername = Base64.encodeBytes(username.getBytes("UTF-8"));
      }
      catch (UnsupportedEncodingException e)
      {
         throw new RuntimeException(e);
      }
      if (authCookie == null)
      {
         authCookie = new Cookie(COOKIE_ALFUSER, encodedUsername);
      }
      else
      {
         authCookie.setValue(encodedUsername);
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
   
   /**
    * Gets the decoded auth cookie value.
    * 
    * @param authCookie
    *           the auth cookie
    * @return the auth cookie value
    */
   public static String getAuthCookieValue(Cookie authCookie)
   {
      String authCookieValue = authCookie.getValue();
      if (authCookieValue == null)
      {
         return null;
      }
      try
      {
         return new String(Base64.decode(authCookieValue), "UTF-8");
      }
      catch (UnsupportedEncodingException e)
      {
         throw new RuntimeException(e);
      }
   }
}
