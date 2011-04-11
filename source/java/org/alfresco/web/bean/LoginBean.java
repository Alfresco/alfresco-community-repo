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
package org.alfresco.web.bean;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationDisallowedException;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationMaxUsersException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.AuthenticationHelper;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.bean.users.UserPreferencesBean;
import org.alfresco.web.ui.common.PanelGenerator;
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
public class LoginBean implements Serializable
{
   /**
    * The default outcome of the logout action.
    */
   private static final String OUTCOME_LOGOUT = "logout";

   /**
    * The outcome of the logout action when the user has been signed on by SSO.
    */
   private static final String OUTCOME_RELOGIN = "relogin";

   /**
    * The name of the form parameter carrying the outcome to the logout action.
    */
   private static final String PARAM_OUTCOME = "outcome";

   private static final long serialVersionUID = 7417882503323795282L;

   /**
    * @param authenticationService      The AuthenticationService to set.
    */
   public void setAuthenticationService(AuthenticationService authenticationService)
   {
      this.authenticationService = authenticationService;
   }
   
   protected AuthenticationService getAuthenticationService()
   {
      if (authenticationService == null)
         authenticationService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getAuthenticationService();
      return authenticationService;
   }

   /**
    * @param personService             The personService to set.
    */
   public void setPersonService(PersonService personService)
   {
      this.personService = personService;
   }
   
   protected PersonService getPersonService()
   {
      if (personService == null)
         personService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getPersonService();
      return personService;
   }

   /**
    * @param nodeService                The nodeService to set.
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }
   
   protected NodeService getNodeService()
   {
      if (nodeService == null)
         nodeService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getNodeService();
      return nodeService;
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
    * @return "logout" if the default Alfresco authentication process is being used, else "relogin"
    *         if an external authorisation mechanism is present.
    */
   public String getLogoutOutcome()
   {
       Map<?, ?> session = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
       return session.get(LOGIN_EXTERNAL_AUTH) == null ? OUTCOME_LOGOUT : OUTCOME_RELOGIN;
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
   
   /**
    * @return true to display language selection, false to 
    */
   public boolean isLanguageSelect()
   {
      return Application.getClientConfig(FacesContext.getCurrentInstance()).isLanguageSelect();
   }

   /**
    * Returns the HTML to display if the system is in TEAM mode
    * 
    * @return The HTML to display
    */
   public String getTeamLoginWarningHTML()
   {
      FacesContext context = FacesContext.getCurrentInstance();
      String contextPath = context.getExternalContext().getRequestContextPath();
      StringBuilder html = new StringBuilder();
      
      try
      {
         html.append("<tr><td colspan='2'>");
         
         StringWriter writer = new StringWriter();
         PanelGenerator.generatePanelStart(writer, contextPath, "yellowInner", "#ffffcc");
         html.append(writer.toString());
         
         html.append("<table cellpadding='0' cellspacing='0' border='0' width='100%'>");
         html.append("<tr><td valign='top' style='padding-top: 2px' width='20'>");
         html.append("<img src='");
         html.append(contextPath);
         html.append("/images/icons/warning.gif' width='16' height='16' /></td>");
         html.append("<td width='180' class='statusErrorText'>");
         html.append(Application.getMessage(context, "team_login_warning"));
         html.append("</td></tr></table>");
         
         writer = new StringWriter();
         PanelGenerator.generatePanelEnd(writer, contextPath, "yellowInner");
         html.append(writer.toString());
            
         html.append("</td></tr>");
      }
      catch (IOException ioe)
      {
         logger.error(ioe);
      }
      
      return html.toString();
   }

   // ------------------------------------------------------------------------------
   // Validator methods

   /**
    * Validate password field data is acceptable
    */
   public void validatePassword(FacesContext context, UIComponent component, Object value)
         throws ValidatorException
   {
      int minPasswordLength = Application.getClientConfig(context).getMinPasswordLength();
      
      String pass = (String)value;
      if (pass.length() < minPasswordLength || pass.length() > 256)
      {
         String err = MessageFormat.format(Application.getMessage(context, MSG_PASSWORD_LENGTH),
               new Object[]{minPasswordLength, 256});
         throw new ValidatorException(new FacesMessage(err));
      }
   }

   /**
    * Validate Username field data is acceptable
    */
   public void validateUsername(FacesContext context, UIComponent component, Object value)
         throws ValidatorException
   {
      int minUsernameLength = Application.getClientConfig(context).getMinUsernameLength();
      
      String name = ((String)value).trim();
      
      if (name.length() < minUsernameLength || name.length() > 256)
      {
         String err = MessageFormat.format(Application.getMessage(context, MSG_USERNAME_LENGTH),
               new Object[]{minUsernameLength, 256});
         throw new ValidatorException(new FacesMessage(err));
      }
      if (name.indexOf('"') != -1)
      {
         String err = MessageFormat.format(Application.getMessage(context, MSG_USER_ERR),
               new Object[]{"\""});
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
            this.getAuthenticationService().authenticate(this.username, this.password.toCharArray());
            
            // Set the user name as stored by the back end 
            this.username = this.getAuthenticationService().getCurrentUserName();
            
            // remove the session invalidated flag (used to remove last username cookie by AuthenticationFilter)
            session.remove(AuthenticationHelper.SESSION_INVALIDATED);
            
            // setup User object and Home space ID
            User user = new User(
                    this.username,
                  this.getAuthenticationService().getCurrentTicket(),
                  getPersonService().getPerson(this.username));
            
            NodeRef homeSpaceRef = (NodeRef) this.getNodeService().getProperty(getPersonService().getPerson(this.username), ContentModel.PROP_HOMEFOLDER);
            
            // check that the home space node exists - else user cannot login
            if (homeSpaceRef == null || this.getNodeService().exists(homeSpaceRef) == false)
            {
               throw new InvalidNodeRefException(homeSpaceRef);
            }
            user.setHomeSpaceId(homeSpaceRef.getId());
            
            // put the User object in the Session - the authentication servlet will then allow
            // the app to continue without redirecting to the login page
            Application.setCurrentUser(fc, user);
            
            // Programatically retrieve the LoginOutcomeBean from JSF
            LoginOutcomeBean loginOutcomeBean = (LoginOutcomeBean) fc.getApplication().createValueBinding(
                  "#{LoginOutcomeBean}").getValue(fc);
            
            // if a redirect URL has been provided then use that
            // this allows servlets etc. to provide a URL to return too after a successful login
            String redirectURL = loginOutcomeBean.getRedirectURL();
            if (redirectURL != null && redirectURL.length() > 0)
            {
               if (logger.isDebugEnabled())
                  logger.debug("Redirect URL found: " + redirectURL);
                              
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
            	
               // note: to enable MT runtime client config customization, need to re-init NavigationBean
               // in context of tenant login page
               this.navigator.initFromClientConfig();
                 
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
         catch (AuthenticationDisallowedException aerr)
         {
        	 Utils.addErrorMessage(Application.getMessage(fc, MSG_ERROR_LOGIN_DISALLOWED));
         }  
         catch (AuthenticationMaxUsersException aerr)
         {
        	 Utils.addErrorMessage(Application.getMessage(fc, MSG_ERROR_LOGIN_MAXUSERS));
         }           
         catch (AuthenticationException aerr)
         {
            Utils.addErrorMessage(Application.getMessage(fc, MSG_ERROR_UNKNOWN_USER));
         }
         catch (InvalidNodeRefException refErr)
         {
            String msg;
            if (refErr.getNodeRef() != null)
            {
                msg = refErr.getNodeRef().toString();
            }
            else
            {
                msg = Application.getMessage(fc, MSG_NONE);
            }
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(fc,
                  Repository.ERROR_NOHOME), msg));
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

      // The outcome is decided in advance (before session expiry) and included as a parameter
      Map<?, ?> params = context.getExternalContext().getRequestParameterMap();
      String outcome = (String)params.get(PARAM_OUTCOME);
      if (outcome == null)
      {
          outcome = OUTCOME_LOGOUT;
      }

      Locale language = Application.getLanguage(context);
      
      // Perform log out actions
      Application.logOut(context);
      
      // set language to last used on the login page
      Application.setLanguage(context, language.toString());
      
      return outcome;
   }
   
   
   // ------------------------------------------------------------------------------
   // Private data

   private static final Log logger = LogFactory.getLog(LoginBean.class);
   
   /** I18N messages */
   private static final String MSG_ERROR_MISSING = "error_login_missing";
   private static final String MSG_ERROR_UNKNOWN_USER = "error_login_user";
   private static final String MSG_ERROR_LOGIN_DISALLOWED = "error_login_disallowed";
   private static final String MSG_ERROR_LOGIN_MAXUSERS = "error_login_maxusers";
   private static final String MSG_NONE = "none";
   
   public static final String MSG_ERROR_LOGIN_NOPERMISSIONS = "login_err_permissions";
   public static final String MSG_USERNAME_LENGTH = "login_err_username_length";
   public static final String MSG_PASSWORD_LENGTH = "login_err_password_length";
   public static final String MSG_USER_ERR = "user_err_user_name";
   
   public static final String LOGIN_EXTERNAL_AUTH = "_alfExternalAuth";
   public static final String LOGIN_NOPERMISSIONS = "_alfNoPermissions";

   /** user name */
   private String username = null;

   /** password */
   private String password = null;

   /** PersonService bean reference */
   private transient PersonService personService;
   
   /** AuthenticationService bean reference */
   private transient AuthenticationService authenticationService;

   /** NodeService bean reference */
   private transient NodeService nodeService;

   /** The BrowseBean reference */
   protected BrowseBean browseBean;
   
   /** The NavigationBean bean reference */
   protected NavigationBean navigator;
   
   /** The user preferences bean reference */
   protected UserPreferencesBean preferences;
}