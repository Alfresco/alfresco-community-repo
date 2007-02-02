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
package org.alfresco.web.bean;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.servlet.http.HttpServletRequest;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.AuthenticationHelper;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JSF Managed Bean. Backs the "login.jsp" view to provide the form fields used
 * to enter user data for login. Also contains bean methods to validate form
 * fields and action event fired in response to the Login button being pressed.
 * 
 * @author Kevin Roast
 */
public class LoginBean
{
   // ------------------------------------------------------------------------------
   // Managed bean properties

   /**
    * @param authenticationService      The AuthenticationService to set.
    */
   public void setAuthenticationService(AuthenticationService authenticationService)
   {
      this.authenticationService = authenticationService;
   }

   /**
    * @param personService             The personService to set.
    */
   public void setPersonService(PersonService personService)
   {
      this.personService = personService;
   }

   /**
    * @param nodeService                The nodeService to set.
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }

   /**
    * @param browseBean                 The BrowseBean to set.
    */
   public void setBrowseBean(BrowseBean browseBean)
   {
      this.browseBean = browseBean;
   }
   
   /**
    * @param navigator     The NavigationBean to set.
    */
   public void setNavigator(NavigationBean navigator)
   {
      this.navigator = navigator;
   }
   
   /**
    * @param preferences   The UserPreferencesBean to set
    */
   public void setUserPreferencesBean(UserPreferencesBean preferences)
   {
      this.preferences = preferences;
   }
   
   public UserPreferencesBean getUserPreferencesBean()
   {
      return preferences;
   }
   
   /**
    * @return true if the default Alfresco authentication process is being used, else false
    *         if an external authorisation mechanism is present.
    */
   public boolean isAlfrescoAuth()
   {
      Map session = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
      return (session.get(LOGIN_EXTERNAL_AUTH) == null);
   }

   /**
    * @param val        Username from login dialog
    */
   public void setUsername(String val)
   {
      if ( val != null ) { val = val.trim(); }
      this.username = val;
   }

   /**
    * @return The username string from login dialog
    */
   public String getUsername()
   {
      // this value may have been set by a servlet filter via a cookie
      // check for this by detecting a special value in the session
      FacesContext context = FacesContext.getCurrentInstance();
      Map session = context.getExternalContext().getSessionMap();
      
      String username = (String)session.get(AuthenticationHelper.SESSION_USERNAME);
      if (username != null)
      {
         session.remove(AuthenticationHelper.SESSION_USERNAME);
         this.username = username;
      }
      
      return this.username;
   }
   
   public String getUsernameInternal()
   {
      return this.username;
   }

   /**
    * @param val         Password from login dialog
    */
   public void setPassword(String val)
   {
      this.password = val;
   }

   /**
    * @return The password string from login dialog
    */
   public String getPassword()
   {
      return this.password;
   }


   // ------------------------------------------------------------------------------
   // Validator methods

   /**
    * Validate password field data is acceptable
    */
   public void validatePassword(FacesContext context, UIComponent component, Object value)
         throws ValidatorException
   {
      String pass = (String) value;
      if (pass.length() < 3 || pass.length() > 32)
      {
         String err = MessageFormat.format(Application.getMessage(context, MSG_PASSWORD_LENGTH),
               new Object[]{3, 32});
         throw new ValidatorException(new FacesMessage(err));
      }
   }

   /**
    * Validate Username field data is acceptable
    */
   public void validateUsername(FacesContext context, UIComponent component, Object value)
         throws ValidatorException
   {
      String name = (String) value;
      name = name.trim();

      if (name.length() < 2 || name.length() > 32)
      {
         String err = MessageFormat.format(Application.getMessage(context, MSG_USERNAME_LENGTH),
               new Object[]{2, 32});
         throw new ValidatorException(new FacesMessage(err));
      }
      if (name.indexOf('\'') != -1 || name.indexOf('"') != -1 || name.indexOf('\\') != -1)
      {
         String err = MessageFormat.format(Application.getMessage(context, MSG_USER_ERR),
               new Object[]{"', \", \\"});
         throw new ValidatorException(new FacesMessage(err));
      }
   }

   
   // ------------------------------------------------------------------------------
   // Action event methods

   /**
    * Login action handler
    * 
    * @return outcome view name
    */
   public String login()
   {
      String outcome = null;
      
      FacesContext fc = FacesContext.getCurrentInstance();
      
      if (this.username != null && this.username.length() != 0 &&
          this.password != null && this.password.length() != 0)
      {
         try
         {
            Map session = fc.getExternalContext().getSessionMap();
            
            // Authenticate via the authentication service, then save the details of user in an object
            // in the session - this is used by the servlet filter etc. on each page to check for login
            this.authenticationService.authenticate(this.username, this.password.toCharArray());
            
            // Set the user name as stored by the back end 
            this.username = this.authenticationService.getCurrentUserName();
            
            // remove the session invalidated flag (used to remove last username cookie by AuthenticationFilter)
            session.remove(AuthenticationHelper.SESSION_INVALIDATED);
            
            // setup User object and Home space ID
            User user = new User(
                    this.username,
                  this.authenticationService.getCurrentTicket(),
                  personService.getPerson(this.username));
            
            NodeRef homeSpaceRef = (NodeRef) this.nodeService.getProperty(personService.getPerson(this.username), ContentModel.PROP_HOMEFOLDER);
            
            // check that the home space node exists - else user cannot login
            if (this.nodeService.exists(homeSpaceRef) == false)
            {
               throw new InvalidNodeRefException(homeSpaceRef);
            }
            user.setHomeSpaceId(homeSpaceRef.getId());
            
            // put the User object in the Session - the authentication servlet will then allow
            // the app to continue without redirecting to the login page
            session.put(AuthenticationHelper.AUTHENTICATION_USER, user);
            
            // if a redirect URL has been provided then use that
            // this allows servlets etc. to provide a URL to return too after a successful login
            String redirectURL = (String)session.get(LOGIN_REDIRECT_KEY);
            if (redirectURL != null)
            {
               if (logger.isDebugEnabled())
                  logger.debug("Redirect URL found: " + redirectURL);
               
               // remove redirect URL from session
               session.remove(LOGIN_REDIRECT_KEY);
               
               try
               {
                  fc.getExternalContext().redirect(redirectURL);
                  fc.responseComplete();
                  return null;
               }
               catch (IOException ioErr)
               {
                  logger.warn("Unable to redirect to url: " + redirectURL);
               }
            }
            else
            {
               // special case to handle jump to My Alfresco page initially
               if (NavigationBean.LOCATION_MYALFRESCO.equals(this.preferences.getStartLocation()))
               {
                  return "myalfresco";
               }
               else
               {
                  // generally this will navigate to the generic browse screen
                  return "success";
               }
            }
         }
         catch (AuthenticationException aerr)
         {
            Utils.addErrorMessage(Application.getMessage(fc, MSG_ERROR_UNKNOWN_USER));
         }
         catch (InvalidNodeRefException refErr)
         {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(fc,
                  Repository.ERROR_NOHOME), refErr.getNodeRef().getId()));
         }
      }
      else
      {
         Utils.addErrorMessage(Application.getMessage(fc, MSG_ERROR_MISSING));
      }

      return outcome;
   }

   /**
    * Invalidate ticket and logout user
    */
   public String logout()
   {
      FacesContext context = FacesContext.getCurrentInstance();
      
      // need to capture this value before invalidating the session
      boolean externalAuth = isAlfrescoAuth();
      
      // Invalidate Session for this user.
      if (Application.inPortalServer() == false)
      {
         // This causes the sessionDestroyed() event to be processed by ContextListener
         // which is responsible for invalidating the ticket and clearing the security context
         HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
         request.getSession().invalidate();
      }
      else
      {
         Map session = context.getExternalContext().getSessionMap();
         User user = (User)session.get(AuthenticationHelper.AUTHENTICATION_USER);
         if (user != null)
         {
            // invalidate ticket and clear the Security context for this thread
            authenticationService.invalidateTicket(user.getTicket());
            authenticationService.clearCurrentSecurityContext();
         }
         // remove all objects from our session by hand
         // we do this as invalidating the Portal session would invalidate all other portlets!
         for (Object key : session.keySet())
         {
            session.remove(key);
         }
      }
      
      // Request that the username cookie state is removed - this is not
      // possible from JSF - so instead we setup a session variable
      // which will be detected by the login.jsp/Portlet as appropriate.
      Map session = context.getExternalContext().getSessionMap();
      session.put(AuthenticationHelper.SESSION_INVALIDATED, true);
      
      // set language to last used
      String language = preferences.getLanguage();
      if (language != null && language.length() != 0)
      {
         Application.setLanguage(context, language);
      }
      
      return externalAuth ? "logout" : "relogin";
   }
   
   
   // ------------------------------------------------------------------------------
   // Private data

   private static final Log logger = LogFactory.getLog(LoginBean.class);
   
   /** I18N messages */
   private static final String MSG_ERROR_MISSING = "error_login_missing";
   private static final String MSG_ERROR_UNKNOWN_USER = "error_login_user";
   private static final String MSG_USERNAME_CHARS = "login_err_username_chars";
   private static final String MSG_USERNAME_LENGTH = "login_err_username_length";
   private static final String MSG_PASSWORD_CHARS = "login_err_password_chars";
   private static final String MSG_PASSWORD_LENGTH = "login_err_password_length";
   private static final String MSG_USER_ERR = "user_err_user_name";

   public static final String LOGIN_REDIRECT_KEY = "_alfRedirect";
   public static final String LOGIN_EXTERNAL_AUTH= "_alfExternalAuth";

   /** user name */
   private String username = null;

   /** password */
   private String password = null;

   /** PersonService bean reference */
   protected PersonService personService;
   
   /** AuthenticationService bean reference */
   protected AuthenticationService authenticationService;

   /** NodeService bean reference */
   protected NodeService nodeService;

   /** The BrowseBean reference */
   protected BrowseBean browseBean;
   
   /** The NavigationBean bean reference */
   protected NavigationBean navigator;
   
   /** The user preferences bean reference */
   protected UserPreferencesBean preferences;
}
