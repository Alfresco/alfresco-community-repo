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
package org.alfresco.web.app.portlet;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.MimeResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.ResourceURL;
import javax.portlet.UnavailableException;
import javax.servlet.ServletRequest;

import org.alfresco.repo.SessionUser;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.util.TempFileProvider;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.AuthenticationHelper;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.ErrorBean;
import org.alfresco.web.bean.FileUploadBean;
import org.alfresco.web.bean.LoginBean;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.config.ClientConfigElement;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.portlet.PortletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.portlet.MyFacesGenericPortlet;
import org.apache.myfaces.portlet.PortletUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.config.ConfigService;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.web.context.WebApplicationContext;

/**
 * Class to extend the MyFacesGenericPortlet to provide behaviour specific to Alfresco web client.
 * Handles upload of multi-part forms through a JSR-168 Portlet, generic error handling and session
 * login authentication.
 * 
 * @author Gavin Cornwell, Kevin Roast
 */
public class AlfrescoFacesPortlet extends MyFacesGenericPortlet
{
   private static final String ATTRIBUTE_LOCALE = "locale";
   private static final String PREF_ALF_USERNAME = "_alfUserName";
   private static final String SESSION_LAST_VIEW_ID = "_alfLastViewId";
   
   private static final String ERROR_OCCURRED = "error-occurred";
   
   private static Log logger = LogFactory.getLog(AlfrescoFacesPortlet.class);

   private String loginPage = null;
   private String errorPage = null;
   
   
   /**
    * Called by the portlet container to allow the portlet to process an action request.
    */
   public void processAction(ActionRequest request, ActionResponse response) 
      throws PortletException, IOException 
   {
      Application.setInPortalServer(true);      
      try
      {
         // Set the current locale
         I18NUtil.setLocale(getLanguage(request.getPortletSession()));
         
         boolean isMultipart = PortletFileUpload.isMultipartContent(request);

         // NOTE: Due to filters not being called within portlets we can not make use
         //       of the MyFaces file upload support, therefore we are using a pure
         //       portlet request/action to handle file uploads until there is a 
         //       solution.
         
         if (isMultipart)
         {
            if (logger.isDebugEnabled())
               logger.debug("Handling multipart request...");
            
            PortletSession session = request.getPortletSession();
            
            // get the file from the request and put it in the session
            DiskFileItemFactory factory = new DiskFileItemFactory();
            PortletFileUpload upload = new PortletFileUpload(factory);
            List<FileItem> fileItems = upload.parseRequest(request);
            Iterator<FileItem> iter = fileItems.iterator();
            FileUploadBean bean = new FileUploadBean();
            while(iter.hasNext())
            {
               FileItem item = iter.next();
               String filename = item.getName();
               if(item.isFormField() == false)
               {
                  if (logger.isDebugEnabled())
                     logger.debug("Processing uploaded file: " + filename);
                  
                  // workaround a bug in IE where the full path is returned
                  // IE is only available for Windows so only check for the Windows path separator
                  int idx = filename.lastIndexOf('\\');
                  
                  if (idx == -1)
                  {
                     // if there is no windows path separator check for *nix
                     idx = filename.lastIndexOf('/');
                  }
                  
                  if (idx != -1)
                  {
                     filename = filename.substring(idx + File.separator.length());
                  }
                  
                  File tempFile = TempFileProvider.createTempFile("alfresco", ".upload");
                  item.write(tempFile);
                  bean.setFile(tempFile);
                  bean.setFileName(filename);
                  bean.setFilePath(tempFile.getAbsolutePath());
                  session.setAttribute(FileUploadBean.FILE_UPLOAD_BEAN_NAME, bean, 
                                       PortletSession.PORTLET_SCOPE);
               }
            }
            
            // Set the VIEW_ID parameter to tell the faces portlet bridge to treat the request
            // as a JSF request, this will send us back to the previous page we came from.
            String lastViewId = (String)request.getPortletSession().getAttribute(SESSION_LAST_VIEW_ID);
            if (lastViewId != null)
            {
               response.setRenderParameter(VIEW_ID, lastViewId);
            }
         }
         else
         {
            SessionUser sessionUser = (SessionUser) request.getPortletSession().getAttribute(
                  AuthenticationHelper.AUTHENTICATION_USER, PortletSession.APPLICATION_SCOPE);
            User user = sessionUser instanceof User ? (User) sessionUser : null;
            if (user != null)
            {
               // setup the authentication context
               try
               {
                  WebApplicationContext ctx = (WebApplicationContext)getPortletContext().getAttribute(
                        WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
                  AuthenticationService auth = (AuthenticationService)ctx.getBean("AuthenticationService");
                  auth.validate(user.getTicket());
                  
                  // save last username into portlet preferences, get from LoginBean state
                  LoginBean loginBean = (LoginBean)request.getPortletSession().getAttribute(AuthenticationHelper.LOGIN_BEAN);
                  if (loginBean != null)
                  {
                     // TODO: Need to login to the Portal to get a user here to store prefs against
                     //       so not really a suitable solution as they get thrown away at present!
                     //       Also would need to store prefs PER user - so auto login for each...?
                     String oldValue = request.getPreferences().getValue(PREF_ALF_USERNAME, null);
                     if (oldValue == null || oldValue.equals(loginBean.getUsernameInternal()) == false)
                     {
                        if (request.getPreferences().isReadOnly(PREF_ALF_USERNAME) == false)
                        {
                           request.getPreferences().setValue(PREF_ALF_USERNAME, loginBean.getUsernameInternal());
                           request.getPreferences().store();
                        }
                     }
                  }
                  
                  // do the normal JSF processing
                  super.processAction(request, response);
               }
               catch (AuthenticationException authErr)
               {
                  // remove User object as it's now useless
                  request.getPortletSession().removeAttribute(AuthenticationHelper.AUTHENTICATION_USER, PortletSession.APPLICATION_SCOPE);
               }
            }
            else
            {
               // do the normal JSF processing as we may be on the login page
               super.processAction(request, response);
            }
         }
      }
      catch (Throwable e)
      {
         if (getErrorPage() != null)
         {
            handleError(request, response, e);
         }
         else
         {
            logger.warn("No error page configured, re-throwing exception");
            
            if (e instanceof PortletException)
            {
               throw (PortletException)e;
            }
            else if (e instanceof IOException)
            {
               throw (IOException)e;
            }
            else
            {
               throw new PortletException(e);
            }
         }
      }
      finally
      {
         Application.setInPortalServer(false);
      }
   }

   
   /* (non-Javadoc)
    * @see javax.portlet.GenericPortlet#serveResource(javax.portlet.ResourceRequest, javax.portlet.ResourceResponse)
    */
   @Override
   public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException
   {
      Application.setInPortalServer(true);
      try
      {
         super.serveResource(request, response);
      }
      finally
      {
         Application.setInPortalServer(false);
      }
   }


   /**
    * @see org.apache.myfaces.portlet.MyFacesGenericPortlet#facesRender(javax.portlet.RenderRequest, javax.portlet.RenderResponse)
    */
   protected void facesRender(RenderRequest request, RenderResponse response) 
      throws PortletException, IOException
   {
      Application.setInPortalServer(true);
      
      try
      {      
         // Set the current locale
         I18NUtil.setLocale(getLanguage(request.getPortletSession()));
         
         if (request.getParameter(ERROR_OCCURRED) != null)
         {
            String errorPage = getErrorPage();
            
            if (logger.isDebugEnabled())
               logger.debug("An error has occurred, redirecting to error page: " + errorPage);
            
            response.setContentType("text/html");
            PortletRequestDispatcher dispatcher = getPortletContext().getRequestDispatcher(errorPage);
            dispatcher.include(request, response);
         }
         else
         {
            WebApplicationContext ctx = (WebApplicationContext)getPortletContext().getAttribute(
                  WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
            AuthenticationService auth = (AuthenticationService)ctx.getBean("AuthenticationService");
            
            // if we have no User object in the session then an HTTP Session timeout must have occured
            // use the viewId to check that we are not already on the login page
            PortletSession session = request.getPortletSession();
            String viewId = request.getParameter(VIEW_ID);
            // keep track of last view id so we can use it as return page from multi-part requests
            request.getPortletSession().setAttribute(SESSION_LAST_VIEW_ID, viewId);
            SessionUser sessionUser = (SessionUser) request.getPortletSession().getAttribute(
                  AuthenticationHelper.AUTHENTICATION_USER, PortletSession.APPLICATION_SCOPE);
            User user = sessionUser instanceof User ? (User)sessionUser : null;
            if (user == null && (viewId == null || viewId.equals(getLoginPage()) == false))
            {
               if (portalGuestAuthenticate(ctx, session, auth) != null)
               {
                  if (logger.isDebugEnabled())
                     logger.debug("Guest access successful.");
                  
                  // perform the forward to the page processed by the Faces servlet
                  response.setContentType("text/html");
                  request.getPortletSession().setAttribute(PortletUtil.PORTLET_REQUEST_FLAG, "true");
                  
                  // get the start location as configured by the web-client config
                  ConfigService configService = (ConfigService)ctx.getBean("webClientConfigService");
                  ClientConfigElement configElement = (ClientConfigElement)configService.getGlobalConfig().getConfigElement("client");
                  if (NavigationBean.LOCATION_MYALFRESCO.equals(configElement.getInitialLocation()))
                  {
                     nonFacesRequest(request, response, "/jsp/dashboards/container.jsp");
                  }
                  else
                  {
                     nonFacesRequest(request, response, FacesHelper.BROWSE_VIEW_ID);
                  }
               }
               else
               {
                  if (logger.isDebugEnabled())
                     logger.debug("No valid User login, requesting login page. ViewId: " + viewId);
                  
                  // set last used username as special session value used by the LoginBean
                  session.setAttribute(AuthenticationHelper.SESSION_USERNAME,
                        request.getPreferences().getValue(PREF_ALF_USERNAME, null));
                  
                  // login page is the default portal page
                  response.setContentType("text/html");
                  request.getPortletSession().setAttribute(PortletUtil.PORTLET_REQUEST_FLAG, "true");
                  nonFacesRequest(request, response);
               }
            }
            else
            {
               if (session.getAttribute(AuthenticationHelper.SESSION_INVALIDATED) != null)
               {
                  // remove the username preference value as explicit logout was requested by the user
                  if (request.getPreferences().isReadOnly(PREF_ALF_USERNAME) == false)
                  {
                     request.getPreferences().reset(PREF_ALF_USERNAME);
                  }
                  session.removeAttribute(AuthenticationHelper.SESSION_INVALIDATED);
               }
               
               try
               {
                  if (user != null)
                  {
                     if (logger.isDebugEnabled())
                        logger.debug("Validating ticket: " + user.getTicket());
                     
                     // setup the authentication context
                     auth.validate(user.getTicket());
                  }
                  
                  // do the normal JSF processing
                  super.facesRender(request, response);
               }
               catch (AuthenticationException authErr)
               {
                  // ticket is no longer valid!
                  if (logger.isDebugEnabled())
                     logger.debug("Invalid ticket, requesting login page.");
                  
                  // remove User object as it's now useless
                  session.removeAttribute(AuthenticationHelper.AUTHENTICATION_USER, PortletSession.APPLICATION_SCOPE);
                  
                  // login page is the default portal page
                  response.setContentType("text/html");
                  request.getPortletSession().setAttribute(PortletUtil.PORTLET_REQUEST_FLAG, "true");
                  nonFacesRequest(request, response);
               }
               catch (Throwable e)
               {
                  if (getErrorPage() != null)
                  {
                     handleError(request, response, e);
                  }
                  else
                  {
                     logger.warn("No error page configured, re-throwing exception");
                     
                     if (e instanceof PortletException)
                     {
                        throw (PortletException)e;
                     }
                     else if (e instanceof IOException)
                     {
                        throw (IOException)e;
                     }
                     else
                     {
                        throw new PortletException(e);
                     }
                  }
               }
            }
         }
      }
      finally
      {
         Application.setInPortalServer(false);
      }
   }
   
   /**
    * Handles errors that occur during a process action request
    */
   private void handleError(ActionRequest request, ActionResponse response, Throwable error)
      throws PortletException, IOException
   {
      // get the error bean from the session and set the error that occurred.
      PortletSession session = request.getPortletSession();
      ErrorBean errorBean = (ErrorBean)session.getAttribute(ErrorBean.ERROR_BEAN_NAME, 
                             PortletSession.PORTLET_SCOPE);
      if (errorBean == null)
      {
         errorBean = new ErrorBean();
         session.setAttribute(ErrorBean.ERROR_BEAN_NAME, errorBean, PortletSession.PORTLET_SCOPE);
      }
      errorBean.setLastError(error);
      
      response.setRenderParameter(ERROR_OCCURRED, "true");
   }
   
   /**
    * Gets the error bean from a request
    * 
    * @param request
    *           the request
    * @return the error bean
    */
   public static ErrorBean getErrorBean(ServletRequest request)
   {
      PortletRequest portletReq = (PortletRequest) request.getAttribute("javax.portlet.request");
      if (portletReq != null)
      {
         PortletSession session = portletReq.getPortletSession(false);
         if (session != null)
         {
            return (ErrorBean)session.getAttribute(ErrorBean.ERROR_BEAN_NAME);
         }
      }
      return null;
   }

   /**
    * Creates a render URL from the given request and parameters
    * 
    * @param request
    *           the request
    * @param parameters
    *           the parameters
    * @return the render url
    */
   public static String getRenderURL(ServletRequest request, Map<String, String[]> parameters)
   {
      RenderResponse renderResp = (RenderResponse) request.getAttribute("javax.portlet.response");
      if (renderResp == null)
      {
         throw new IllegalStateException("RenderResponse object is null");
      }

      PortletURL url = renderResp.createRenderURL();
      url.setParameters(parameters);
      return url.toString();
   }
   
   /**
    * Creates an action url from the given request.
    * 
    * @param request
    *           the request
    * @return the action url
    */
   public static String getActionURL(ServletRequest request)
   {
      RenderResponse renderResp = (RenderResponse) request.getAttribute("javax.portlet.response");
      if (renderResp == null)
      {
         throw new IllegalStateException(
               "RenderResponse object is null. The web application is not executing within a portal server!");
      }
      return renderResp.createActionURL().toString();

   }
   
    /**
     * Creates a resource URL from the given faces context.
     * 
     * @param context
     *            the faces context
     * @return the resource URL
     */
    public static String getResourceURL(FacesContext context, String path)
    {
        MimeResponse portletResponse = (MimeResponse) context.getExternalContext().getResponse();
        ResourceURL resourceURL = portletResponse.createResourceURL();
        resourceURL.setResourceID(path);
        return resourceURL.toString();
    }

   /**
     * Gets a session attribute.
     * 
     * @param context
     *            the faces context
     * @param attributeName
     *            the attribute name
     * @param shared
     *            get the attribute from shared (application) scope?
     * @return the portlet session attribute
     */
   public static Object getPortletSessionAttribute(FacesContext context, String attributeName, boolean shared)
   {
      Object portletReq = context.getExternalContext().getRequest();
      if (portletReq != null && portletReq instanceof PortletRequest)
      {
         PortletSession session = ((PortletRequest) portletReq).getPortletSession(false);
         if (session != null)
         {
            return session.getAttribute(attributeName, shared ? PortletSession.APPLICATION_SCOPE
                  : PortletSession.PORTLET_SCOPE);
         }
      }
      return null;
   }
   
   /**
     * Sets a session attribute.
     * 
     * @param context
     *            the faces context
     * @param attributeName
     *            the attribute name
     * @param value
     *            the value
     * @param shared
     *            set the attribute with shared (application) scope?
     */
   public static void setPortletSessionAttribute(FacesContext context, String attributeName, Object value,
         boolean shared)
   {
      Object portletReq = context.getExternalContext().getRequest();
      if (portletReq != null && portletReq instanceof PortletRequest)
      {
         PortletSession session = ((PortletRequest) portletReq).getPortletSession();
         session.setAttribute(attributeName, value, shared ? PortletSession.APPLICATION_SCOPE
               : PortletSession.PORTLET_SCOPE);
      }
      else
      {
         context.getExternalContext().getSessionMap().put(attributeName, value);
      }
   }

   /**
     * Initializes a new faces context using the portlet objects from a 'wrapped' servlet request.
     * 
     * @param request
     *            the servlet request
     * @return the faces context
     */
   public static FacesContext getFacesContext(ServletRequest request)
   {
      PortletRequest portletReq  = (PortletRequest) request.getAttribute("javax.portlet.request");
      PortletResponse portletRes = (PortletResponse) request.getAttribute("javax.portlet.response");
      PortletConfig portletConfig = (PortletConfig) request.getAttribute("javax.portlet.config");
      return FacesHelper.getFacesContext(portletReq, portletRes, portletConfig.getPortletContext());      
   }
   
   public static String onLogOut(Object req)
   {
      PortletRequest portletReq = null;
      if (req instanceof ServletRequest)
      {
         portletReq = (PortletRequest) ((ServletRequest) req).getAttribute("javax.portlet.request");
      }
      else if (req instanceof PortletRequest)
      {
         portletReq = (PortletRequest) req;
      }

      if (portletReq == null)
      {
         return null;
      }

      // remove all objects from our session by hand
      // we do this as invalidating the Portal session would invalidate all other portlets!
      PortletSession session = portletReq.getPortletSession();
      SessionUser user = (SessionUser) session.getAttribute(AuthenticationHelper.AUTHENTICATION_USER,
            PortletSession.APPLICATION_SCOPE);
      Enumeration<String> i = session.getAttributeNames();
      while (i.hasMoreElements())
      {
         session.removeAttribute(i.nextElement());
      }
      session.setAttribute(AuthenticationHelper.SESSION_INVALIDATED, true);

      return user == null ? null : user.getTicket();
   }   
   
   /**
    * Handles errors that occur during a render request
    */
   private void handleError(RenderRequest request, RenderResponse response, Throwable error)
      throws PortletException, IOException
   {
      // get the error bean from the session and set the error that occurred.
      PortletSession session = request.getPortletSession();
      ErrorBean errorBean = (ErrorBean)session.getAttribute(ErrorBean.ERROR_BEAN_NAME, 
                             PortletSession.PORTLET_SCOPE);
      if (errorBean == null)
      {
         errorBean = new ErrorBean();
         session.setAttribute(ErrorBean.ERROR_BEAN_NAME, errorBean, PortletSession.PORTLET_SCOPE);
      }
      errorBean.setLastError(error);

      // if the faces context is available set the current view to the browse page
      // so that the error page goes back to the application (rather than going back
      // to the same page which just throws the error again meaning we can never leave
      // the error page)
      FacesContext context = FacesContext.getCurrentInstance();
      if (context != null)
      {
         ViewHandler viewHandler = context.getApplication().getViewHandler();
         // TODO: configure the portlet error return page
         UIViewRoot view = viewHandler.createView(context, FacesHelper.BROWSE_VIEW_ID);
         context.setViewRoot(view);
      }

      // get the error page and include that instead
      String errorPage = getErrorPage();
      
      if (logger.isDebugEnabled())
         logger.debug("An error has occurred, redirecting to error page: " + errorPage);
      
      response.setContentType("text/html");
      PortletRequestDispatcher dispatcher = getPortletContext().getRequestDispatcher(errorPage);
      dispatcher.include(request, response);
   }
   
   /**
    * @see org.apache.myfaces.portlet.MyFacesGenericPortlet#setDefaultViewSelector()
    */
   protected void setDefaultViewSelector() throws UnavailableException
   {
      super.setDefaultViewSelector();
      if (this.defaultViewSelector == null)
      {
         this.defaultViewSelector = new AlfrescoDefaultViewSelector();
      }
   }

   /**
    * For no previous authentication or forced Guest - attempt Guest access
    * 
    * @param ctx        WebApplicationContext
    * @param auth       AuthenticationService
    */
   private static User portalGuestAuthenticate(WebApplicationContext ctx, PortletSession session, AuthenticationService auth)
   {
      User user = AuthenticationHelper.portalGuestAuthenticate(ctx, auth);
      
      if (user != null)
      {
         // store the User object in the Session - the authentication servlet will then proceed
         session.setAttribute(AuthenticationHelper.AUTHENTICATION_USER, user, PortletSession.APPLICATION_SCOPE);
      
         // Set the current locale
         I18NUtil.setLocale(getLanguage(session));
         
         // remove the session invalidated flag
         session.removeAttribute(AuthenticationHelper.SESSION_INVALIDATED);
      }
      return user;
   }
   
   /**
    * Return the language Locale for the current user Session.
    * 
    * @param session        PortletSession for the current user
    * 
    * @return Current language Locale set or the VM default if none set - never null
    */
   private static Locale getLanguage(PortletSession session)
   {
      Locale locale = (Locale)session.getAttribute(ATTRIBUTE_LOCALE);
      if (locale == null)
      {
         locale = Application.getLanguage((ApplicationContext)session.getPortletContext().getAttribute(
               WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE));

         // save in user session
         session.setAttribute(ATTRIBUTE_LOCALE, locale);
      }
      return locale;
   }

   /**
    * @return Retrieves the configured login page
    */
   private String getLoginPage()
   {
      if (this.loginPage == null)
      {
         this.loginPage = Application.getLoginPage((ApplicationContext)getPortletContext().getAttribute(
               WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE));
      }
      
      return this.loginPage;
   }
   
   /**
    * @return Retrieves the configured error page
    */
   private String getErrorPage()
   {
      if (this.errorPage == null)
      {
         this.errorPage = Application.getErrorPage((ApplicationContext)getPortletContext().getAttribute(
               WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE));
      }
      
      return this.errorPage;
   }
}
