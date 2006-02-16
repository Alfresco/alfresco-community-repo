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
package org.alfresco.web.app;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.faces.context.FacesContext;
import javax.portlet.PortletContext;
import javax.portlet.PortletSession;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.alfresco.config.ConfigService;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.web.app.servlet.AuthenticationHelper;
import org.alfresco.web.bean.ErrorBean;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.config.ClientConfigElement;
import org.apache.commons.logging.Log;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * Utilities class
 * 
 * @author gavinc
 */
public class Application
{
   private static final String LOCALE = "locale";
   
   public static final String BEAN_CONFIG_SERVICE = "webClientConfigService";
   public static final String BEAN_DATA_DICTIONARY = "dataDictionary";
   public static final String BEAN_IMPORTER_BOOTSTRAP = "importerBootstrap";
   
   public static final String MESSAGE_BUNDLE = "alfresco.messages.webclient";
   
   private static boolean inPortalServer = false;
   private static StoreRef repoStoreRef;
   private static String rootPath;
   private static String companyRootId;
   private static String glossaryFolderName;
   private static String spaceTemplatesFolderName;
   private static String contentTemplatesFolderName;
   private static String savedSearchesFolderName;
   
   /**
    * Private constructor to prevent instantiation of this class 
    */
   private Application()
   {
   }
   
   /**
    * Sets whether this application is running inside a portal server
    * 
    * @param inPortal true to indicate the application is running as a portlet 
    */
   public static void setInPortalServer(boolean inPortal)
   {
      inPortalServer = inPortal;
   }
   
   /**
    * Determines whether the server is running in a portal
    * 
    * @return true if we are running inside a portal server
    */
   public static boolean inPortalServer()
   {
      return inPortalServer;
   }
   
   /**
    * Handles errors thrown from servlets
    * 
    * @param servletContext The servlet context
    * @param request The HTTP request
    * @param response The HTTP response
    * @param error The exception
    * @param logger The logger
    */
   public static void handleServletError(ServletContext servletContext, HttpServletRequest request,
         HttpServletResponse response, Throwable error, Log logger, String returnPage)
      throws IOException, ServletException
   {
      // get the error bean from the session and set the error that occurred.
      HttpSession session = request.getSession();
      ErrorBean errorBean = (ErrorBean)session.getAttribute(ErrorBean.ERROR_BEAN_NAME);
      if (errorBean == null)
      {
         errorBean = new ErrorBean();
         session.setAttribute(ErrorBean.ERROR_BEAN_NAME, errorBean);
      }
      errorBean.setLastError(error);
      errorBean.setReturnPage(returnPage);
      
      // try and find the configured error page
      boolean errorShown = false;
      String errorPage = getErrorPage(servletContext);
      
      if (errorPage != null)
      {
         if (logger.isDebugEnabled())
            logger.debug("An error has occurred, redirecting to error page: " + errorPage);
         
         if (response.isCommitted() == false)
         {
            errorShown = true;
            response.sendRedirect(request.getContextPath() + errorPage);
         }
         else
         {
            if (logger.isDebugEnabled())
               logger.debug("Response is already committed, re-throwing error");
         }
      }
      else
      {
         if (logger.isDebugEnabled())
            logger.debug("No error page defined, re-throwing error");
      }
      
      // if we could not show the error page for whatever reason, re-throw the error
      if (!errorShown)
      {
         if (error instanceof IOException)
         {
            throw (IOException)error;
         }
         else if (error instanceof ServletException)
         {
            throw (ServletException)error;
         }
         else
         {
            throw new ServletException(error);
         }
      }
   }
   
   /**
    * Retrieves the configured error page for the application
    * 
    * @param servletContext The servlet context
    * @return The configured error page or null if the configuration is missing
    */
   public static String getErrorPage(ServletContext servletContext)
   {
      return getErrorPage(WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext));
   }
   
   /**
    * Retrieves the configured error page for the application
    * 
    * @param portletContext The portlet context
    * @return
    */
   public static String getErrorPage(PortletContext portletContext)
   {
      return getErrorPage((WebApplicationContext)portletContext.getAttribute(
            WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE));
   }
   
   /**
    * Retrieves the configured login page for the application
    * 
    * @param servletContext The servlet context
    * @return The configured login page or null if the configuration is missing
    */
   public static String getLoginPage(ServletContext servletContext)
   {
      return getLoginPage(WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext));
   }
   
   /**
    * Retrieves the configured login page for the application
    * 
    * @param portletContext The portlet context
    * @return
    */
   public static String getLoginPage(PortletContext portletContext)
   {
      return getLoginPage((WebApplicationContext)portletContext.getAttribute(
            WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE));
   }
   
   /**
    * @return Returns the User object representing the currently logged in user
    */
   public static User getCurrentUser(HttpSession session)
   {
      return (User)session.getAttribute(AuthenticationHelper.AUTHENTICATION_USER);
   }
   
   /**
    * @return Returns the User object representing the currently logged in user
    */
   public static User getCurrentUser(FacesContext context)
   {
      return (User)context.getExternalContext().getSessionMap().get(AuthenticationHelper.AUTHENTICATION_USER);
   }
   
   /**
    * @return Returns the repository store URL (retrieved from config service)
    */
   public static StoreRef getRepositoryStoreRef(ServletContext context)
   {
      return getRepositoryStoreRef(WebApplicationContextUtils.getRequiredWebApplicationContext(context));
   }
   
   /**
    * @return Returns the repository store URL (retrieved from config service)
    */
   public static StoreRef getRepositoryStoreRef(FacesContext context)
   {
      return getRepositoryStoreRef(FacesContextUtils.getRequiredWebApplicationContext(context));
   }
   
   /**
    * @return Returns id of the company root 
    */
   public static String getCompanyRootId()
   {
      return companyRootId;
   }
   
   /**
    * Sets the company root id. This is setup by the ContextListener.
    * 
    * @param id The company root id
    */
   public static void setCompanyRootId(String id)
   {
      companyRootId = id;
   }
   
   /**
    * @return Returns the root path for the application
    */
   public static String getRootPath(ServletContext context)
   {
      return getRootPath(WebApplicationContextUtils.getRequiredWebApplicationContext(context));
   }
   
   /**
    * @return Returns the root path for the application
    */
   public static String getRootPath(FacesContext context)
   {
      return getRootPath(FacesContextUtils.getRequiredWebApplicationContext(context));
   }
   
   /**
    * @return Returns the glossary folder name
    */
   public static String getGlossaryFolderName(ServletContext context)
   {
      return getGlossaryFolderName(WebApplicationContextUtils.getRequiredWebApplicationContext(context));
   }
   
   /**
    * @return Returns the glossary folder name
    */
   public static String getGlossaryFolderName(FacesContext context)
   {
      return getGlossaryFolderName(FacesContextUtils.getRequiredWebApplicationContext(context));
   }
   
   /**
    * @return Returns the Space templates folder name
    */
   public static String getSpaceTemplatesFolderName(ServletContext context)
   {
      return getSpaceTemplatesFolderName(WebApplicationContextUtils.getRequiredWebApplicationContext(context));
   }
   
   /**
    * @return Returns the Space templates folder name
    */
   public static String getSpaceTemplatesFolderName(FacesContext context)
   {
      return getSpaceTemplatesFolderName(FacesContextUtils.getRequiredWebApplicationContext(context));
   }
   
   /**
    * @return Returns the Content templates folder name
    */
   public static String getContentTemplatesFolderName(ServletContext context)
   {
      return getContentTemplatesFolderName(WebApplicationContextUtils.getRequiredWebApplicationContext(context));
   }
   
   /**
    * @return Returns the Content templates folder name
    */
   public static String getContentTemplatesFolderName(FacesContext context)
   {
      return getContentTemplatesFolderName(FacesContextUtils.getRequiredWebApplicationContext(context));
   }
   
   /**
    * @return Return the Saved Searches folder name
    */
   public static String getSavedSearchesFolderName(ServletContext context)
   {
      return getSavedSearchesFolderName(WebApplicationContextUtils.getRequiredWebApplicationContext(context));
   }
   
   /**
    * @return Return the Saved Searches folder name
    */
   public static String getSavedSearchesFolderName(FacesContext context)
   {
      return getSavedSearchesFolderName(FacesContextUtils.getRequiredWebApplicationContext(context));
   }
   
   /**
    * Set the language locale for the current user context
    * 
    * @param context        FacesContext for current user
    * @param code           The ISO locale code to set
    */
   public static void setLanguage(FacesContext context, String code)
   {
      Locale locale = parseLocale(code);
      
      // set locale for JSF framework usage
      context.getViewRoot().setLocale(locale);
      
      // set locale for our framework usage
      context.getExternalContext().getSessionMap().put(LOCALE, locale);
      
      // clear the current message bundle - so it's reloaded with new locale
      context.getExternalContext().getSessionMap().remove(MESSAGE_BUNDLE);
   }
   
   /**
    * Set the language locale for the current user session
    * 
    * @param session        HttpSession for current user
    * @param code           The ISO locale code to set
    */
   public static void setLanguage(HttpSession session, String code)
   {
      Locale locale = parseLocale(code);
      
      session.putValue(LOCALE, locale);
      session.removeAttribute(MESSAGE_BUNDLE);
   }
   
   /**
    * @param code    Locale code (java format with underscores) to parse
    * @return Locale object or default if unable to parse
    */
   private static Locale parseLocale(String code)
   {
      Locale locale = Locale.getDefault();
      
      StringTokenizer t = new StringTokenizer(code, "_");
      int tokens = t.countTokens();
      if (tokens == 1)
      {
         locale = new Locale(code);
      }
      else if (tokens == 2)
      {
         locale = new Locale(t.nextToken(), t.nextToken());
      }
      else if (tokens == 3)
      {
         locale = new Locale(t.nextToken(), t.nextToken(), t.nextToken());
      }
      
      return locale;
   }
   
   /**
    * Return the language Locale for the current user context
    * 
    * @param context        FacesContext for the current user
    * 
    * @return Current language Locale set or the VM default if none set
    */
   public static Locale getLanguage(FacesContext context)
   {
      Locale locale = (Locale)context.getExternalContext().getSessionMap().get(LOCALE);
      return locale != null ? locale : Locale.getDefault();
   }
   
   /**
    * Return the language Locale for the current user Session.
    * 
    * @param session        HttpSession for the current user
    * 
    * @return Current language Locale set or the VM default if none set
    */
   public static Locale getLanguage(HttpSession session)
   {
      Locale locale = (Locale)session.getAttribute(LOCALE);
      return locale != null ? locale : Locale.getDefault();
   }
   
   /**
    * Return the language Locale for the current user PortletSession.
    * 
    * @param session        PortletSession for the current user
    * 
    * @return Current language Locale set or the VM default if none set
    */
   public static Locale getLanguage(PortletSession session)
   {
      Locale locale = (Locale)session.getAttribute(LOCALE);
      return locale != null ? locale : Locale.getDefault();
   }
   
   /**
    * Get the specified I18N message string from the default message bundle for this user
    * 
    * @param context        FacesContext
    * @param msg            Message ID
    * 
    * @return String from message bundle or $$msg$$ if not found
    */
   public static String getMessage(FacesContext context, String msg)
   {
      return getBundle(context).getString(msg);
   }
   
   /**
    * Get the specified I18N message string from the default message bundle for this user
    * 
    * @param session        HttpSession
    * @param msg            Message ID
    * 
    * @return String from message bundle or $$msg$$ if not found
    */
   public static String getMessage(HttpSession session, String msg)
   {
      return getBundle(session).getString(msg);
   }
   
   /**
    * Get the specified the default message bundle for this user
    * 
    * @param session        HttpSession
    * 
    * @return ResourceBundle for this user
    */
   public static ResourceBundle getBundle(HttpSession session)
   {
      ResourceBundle bundle = (ResourceBundle)session.getAttribute(MESSAGE_BUNDLE);
      if (bundle == null)
      {
         // get Locale from language selected by each user on login
         Locale locale = (Locale)session.getAttribute(LOCALE);
         if (locale == null)
         {
            locale = Locale.getDefault();
         }
         bundle = ResourceBundleWrapper.findSharedResourceBundle(MESSAGE_BUNDLE, locale);
         
         session.setAttribute(MESSAGE_BUNDLE, bundle);
      }
      
      return bundle;
   }
   
   /**
    * Get the specified the default message bundle for this user
    * 
    * @param context        FacesContext
    * 
    * @return ResourceBundle for this user
    */
   public static ResourceBundle getBundle(FacesContext context)
   {
      // get the resource bundle for the current locale
      // we store the bundle in the users session
      // this makes it easy to add a locale per user support later
      Map session = context.getExternalContext().getSessionMap();
      ResourceBundle bundle = (ResourceBundle)session.get(MESSAGE_BUNDLE);
      if (bundle == null)
      {
         // get Locale from language selected by each user on login
         Locale locale = (Locale)session.get(LOCALE);
         if (locale == null)
         {
            locale = Locale.getDefault();
         }
         bundle = ResourceBundleWrapper.findSharedResourceBundle(MESSAGE_BUNDLE, locale);
         
         session.put(MESSAGE_BUNDLE, bundle);
      }
      
      return bundle;
   }
   
   /**
    * Helper to get the ConfigService instance
    * 
    * @param context        FacesContext
    * 
    * @return ConfigService
    */
   public static ConfigService getConfigService(FacesContext context)
   {
      return (ConfigService)FacesContextUtils.getRequiredWebApplicationContext(context).getBean(
            Application.BEAN_CONFIG_SERVICE);
   }
   
   /**
    * Helper to get the client config element from the config service
    * 
    * @param context FacesContext
    * @return The ClientConfigElement
    */
   public static ClientConfigElement getClientConfig(FacesContext context)
   {
      return (ClientConfigElement)getConfigService(context).getGlobalConfig().
         getConfigElement(ClientConfigElement.CONFIG_ELEMENT_ID);
   }
   
   /**
    * Returns the repository store URL
    * 
    * @param context The spring context
    * @return The repository store URL to use
    */
   private static StoreRef getRepositoryStoreRef(WebApplicationContext context)
   {
      if (repoStoreRef == null)
      {
         ImporterBootstrap bootstrap = (ImporterBootstrap)context.getBean(BEAN_IMPORTER_BOOTSTRAP);
         repoStoreRef = bootstrap.getStoreRef();
      }
      
      return repoStoreRef;
   }
   
   /**
    * Returns the root path for the application
    * 
    * @param context The spring context
    * @return The application root path
    */
   private static String getRootPath(WebApplicationContext context)
   {
      if (rootPath == null)
      {
         ImporterBootstrap bootstrap = (ImporterBootstrap)context.getBean(BEAN_IMPORTER_BOOTSTRAP);
         Properties configuration = bootstrap.getConfiguration();
         rootPath = configuration.getProperty("spaces.company_home.childname");
      }
      
      return rootPath;
   }
   
   /**
    * Returns the glossary folder name
    * 
    * @param context The spring context
    * @return The glossary folder name
    */
   private static String getGlossaryFolderName(WebApplicationContext context)
   {
      if (glossaryFolderName == null)
      {
         ImporterBootstrap bootstrap = (ImporterBootstrap)context.getBean(BEAN_IMPORTER_BOOTSTRAP);
         Properties configuration = bootstrap.getConfiguration();
         glossaryFolderName = configuration.getProperty("spaces.dictionary.childname");
      }
      
      return glossaryFolderName;
   }
   
   /**
    * Returns the Space Templates folder name
    * 
    * @param context The spring context
    * @return The templates folder name
    */
   private static String getSpaceTemplatesFolderName(WebApplicationContext context)
   {
      if (spaceTemplatesFolderName == null)
      {
         ImporterBootstrap bootstrap = (ImporterBootstrap)context.getBean(BEAN_IMPORTER_BOOTSTRAP);
         Properties configuration = bootstrap.getConfiguration();
         spaceTemplatesFolderName = configuration.getProperty("spaces.templates.childname");
      }
      
      return spaceTemplatesFolderName;
   }
   
   /**
    * Returns the Content Templates folder name
    * 
    * @param context The spring context
    * @return The templates folder name
    */
   private static String getContentTemplatesFolderName(WebApplicationContext context)
   {
      if (contentTemplatesFolderName == null)
      {
         ImporterBootstrap bootstrap = (ImporterBootstrap)context.getBean(BEAN_IMPORTER_BOOTSTRAP);
         Properties configuration = bootstrap.getConfiguration();
         contentTemplatesFolderName = configuration.getProperty("spaces.templates.content.childname");
      }
      
      return contentTemplatesFolderName;
   }
   
   /**
    * Returns the Saved Searches folder name
    * 
    * @param context The spring context
    * @return The saved searches folder name
    */
   private static String getSavedSearchesFolderName(WebApplicationContext context)
   {
      /*
       * This lookup is duplicated in a patch.  If this logic changes, investigate the changes
       * required for the patch(es).
       */
       
      if (savedSearchesFolderName == null)
      {
         ImporterBootstrap bootstrap = (ImporterBootstrap)context.getBean(BEAN_IMPORTER_BOOTSTRAP);
         Properties configuration = bootstrap.getConfiguration();
         savedSearchesFolderName = configuration.getProperty("spaces.savedsearches.childname");
      }
      
      return savedSearchesFolderName;
   }
   
   /**
    * Retrieves the configured error page for the application
    * 
    * @param context The Spring contexr
    * @return The configured error page or null if the configuration is missing
    */
   private static String getErrorPage(WebApplicationContext context)
   {
      String errorPage = null;
      
      ConfigService svc = (ConfigService)context.getBean(BEAN_CONFIG_SERVICE);
      ClientConfigElement clientConfig = (ClientConfigElement)svc.getGlobalConfig().
            getConfigElement(ClientConfigElement.CONFIG_ELEMENT_ID);
      
      if (clientConfig != null)
      {
         errorPage = clientConfig.getErrorPage();
      }
      
      return errorPage;
   }
   
   /**
    * Retrieves the configured login page for the application
    * 
    * @param context The Spring contexr
    * @return The configured login page or null if the configuration is missing
    */
   private static String getLoginPage(WebApplicationContext context)
   {
      String loginPage = null;
      
      ConfigService svc = (ConfigService)context.getBean(BEAN_CONFIG_SERVICE);
      ClientConfigElement clientConfig = (ClientConfigElement)svc.getGlobalConfig().
            getConfigElement(ClientConfigElement.CONFIG_ELEMENT_ID);
      
      if (clientConfig != null)
      {
         loginPage = clientConfig.getLoginPage();
      }
      
      return loginPage;
   }
}
