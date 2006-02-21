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
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;
import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import org.alfresco.config.Config;
import org.alfresco.config.ConfigService;
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
import org.alfresco.web.config.LanguagesConfigElement;
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
    * @param navigator The NavigationBean to set.
    */
   public void setNavigator(NavigationBean navigator)
   {
      this.navigator = navigator;
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

   /**
    * @return the available languages
    */
   public SelectItem[] getLanguages()
   {
      Config config = Application.getConfigService(FacesContext.getCurrentInstance()).getConfig("Languages");
      LanguagesConfigElement langConfig = (LanguagesConfigElement)config.getConfigElement(
            LanguagesConfigElement.CONFIG_ELEMENT_ID);
      
      List<String> languages = langConfig.getLanguages();
      SelectItem[] items = new SelectItem[languages.size()];
      int count = 0;
      for (String locale : languages)
      {
         // get label associated to the locale
         String label = langConfig.getLabelForLanguage(locale);

         // set default selection
         if (count == 0 && this.language == null)
         {
            // first try to get the language that the current user is using
            Locale lastLocale = Application.getLanguage(FacesContext.getCurrentInstance());
            if (lastLocale != null)
            {
               this.language = lastLocale.toString();
            }
            // else we default to the first item in the list
            else
            {
               this.language = locale;
            }
         }
         
         items[count++] = new SelectItem(locale, label);
      }
      
      return items;
   }

   /**
    * @return Returns the language selection.
    */
   public String getLanguage()
   {
      return this.language;
   }

   /**
    * @param language       The language selection to set.
    */
   public void setLanguage(String language)
   {
      this.language = language;
      Application.setLanguage(FacesContext.getCurrentInstance(), this.language);
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
      String pass = (String) value;
      if (pass.length() < 3 || pass.length() > 32)
      {
         String err = MessageFormat.format(Application.getMessage(context, MSG_USERNAME_LENGTH),
               new Object[]{3, 32});
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
            
            // remove the session invalidated flag (used to remove last username cookie by AuthenticationFilter)
            session.remove(AuthenticationHelper.SESSION_INVALIDATED);
            
            // setup User object and Home space ID
            User user = new User(
                  this.authenticationService.getCurrentUserName(),
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
            String redirectURL = (String)fc.getExternalContext().getSessionMap().get(LOGIN_REDIRECT_KEY);
            if (redirectURL != null)
            {
               if (logger.isDebugEnabled())
                  logger.debug("Redirect URL found: " + redirectURL);
               
               // remove redirect URL from session
               fc.getExternalContext().getSessionMap().remove(LOGIN_REDIRECT_KEY);
               
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
               return "success";
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
      
      Map session = context.getExternalContext().getSessionMap();
      User user = (User) session.get(AuthenticationHelper.AUTHENTICATION_USER);
      
      // need to capture this value before invalidating the session
      boolean externalAuth = isAlfrescoAuth();
      
      // Invalidate Session for this user.
      // This causes the sessionDestroyed() event to be processed by ContextListener
      // which is responsible for invalidating the ticket and clearing the security context
      if (Application.inPortalServer() == false)
      {
         HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
         request.getSession().invalidate();
      }
      else
      {
         PortletRequest request = (PortletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
         request.getPortletSession().invalidate();
      }
      
      // Request that the username cookie state is removed - this is not
      // possible from JSF - so instead we setup a session variable
      // which will be detected by the login.jsp/Portlet as appropriate.
      session = context.getExternalContext().getSessionMap();
      session.put(AuthenticationHelper.SESSION_INVALIDATED, true);
      
      // set language to last used
      if (this.language != null && this.language.length() != 0)
      {
         Application.setLanguage(context, this.language);
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

   public static final String LOGIN_REDIRECT_KEY = "_alfRedirect";
   public static final String LOGIN_EXTERNAL_AUTH= "_alfExternalAuth";

   /** user name */
   private String username = null;

   /** password */
   private String password = null;

   /** language locale selection */
   private String language = null;

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
}
