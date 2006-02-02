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
package org.alfresco.web.app.portlet;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.UnavailableException;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.util.TempFileProvider;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.AuthenticationHelper;
import org.alfresco.web.app.servlet.AuthenticationStatus;
import org.alfresco.web.bean.ErrorBean;
import org.alfresco.web.bean.FileUploadBean;
import org.alfresco.web.bean.LoginBean;
import org.alfresco.web.bean.repository.User;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.portlet.PortletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.portlet.MyFacesGenericPortlet;
import org.apache.myfaces.portlet.PortletUtil;
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
   private static final String PREF_ALF_USERNAME = "_alfUserName";
   public static final String INSTANCE_NAME = "AlfrescoClientInstance";
   public static final String MANAGED_BEAN_PREFIX = "javax.portlet.p." + INSTANCE_NAME + "?";
   
   private static final String ERROR_PAGE_PARAM = "error-page";
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
      
      // Set the current locale
      I18NUtil.setLocale(Application.getLanguage(request.getPortletSession()));
      
      boolean isMultipart = PortletFileUpload.isMultipartContent(request);
      
      try
      {
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
            
            // it doesn't matter what the value is we just need the VIEW_ID parameter
            // to tell the faces portlet bridge to treat the request as a JSF request,
            // this will send us back to the same page we came from, which is fine for
            // most scenarios.
            response.setRenderParameter(VIEW_ID, "a-jsf-page");
         }
         else
         {
            User user = (User)request.getPortletSession().getAttribute(AuthenticationHelper.AUTHENTICATION_USER);
            if (user != null)
            {
               // setup the authentication context
               try
               {
                  WebApplicationContext ctx = (WebApplicationContext)getPortletContext().getAttribute(
                        WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
                  AuthenticationService auth = (AuthenticationService)ctx.getBean("authenticationService");
                  auth.validate(user.getTicket());
                  
                  // save last username into portlet preferences, get from LoginBean state
                  LoginBean loginBean = (LoginBean)request.getPortletSession().getAttribute(AuthenticationHelper.LOGIN_BEAN);
                  if (loginBean != null)
                  {
                     //
                     // TODO: Need to login to JBoss Portal to get a user here to store prefs against
                     //       so not really a suitable solution as they get thrown away at present!
                     //       Also would need to store prefs PER user - so auto login for each...?
                     //
                     String oldValue = request.getPreferences().getValue(PREF_ALF_USERNAME, null);
                     if (oldValue == null || oldValue.equals(loginBean.getUsernameInternal()) == false)
                     {
                        request.getPreferences().setValue(PREF_ALF_USERNAME, loginBean.getUsernameInternal());
                        request.getPreferences().store();
                     }
                  }
                  
                  // do the normal JSF processing
                  super.processAction(request, response);
               }
               catch (AuthenticationException authErr)
               {
                  // remove User object as it's now useless
                  request.getPortletSession().removeAttribute(AuthenticationHelper.AUTHENTICATION_USER);
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
   }

   /**
    * @see org.apache.myfaces.portlet.MyFacesGenericPortlet#facesRender(javax.portlet.RenderRequest, javax.portlet.RenderResponse)
    */
   protected void facesRender(RenderRequest request, RenderResponse response) 
      throws PortletException, IOException
   {
      Application.setInPortalServer(true);
      
      // Set the current locale
      I18NUtil.setLocale(Application.getLanguage(request.getPortletSession()));
      
      if (request.getParameter(ERROR_OCCURRED) != null)
      {
         String errorPage = Application.getErrorPage(getPortletContext());
         
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
         User user = (User)request.getPortletSession().getAttribute(AuthenticationHelper.AUTHENTICATION_USER);
         if (user == null && (viewId == null || viewId.equals(getLoginPage()) == false))
         {
            if (AuthenticationHelper.portalGuestAuthenticate(ctx, session, auth) == AuthenticationStatus.Guest)
            {
               if (logger.isDebugEnabled())
                  logger.debug("Guest access successful.");
               
               // perform the forward to the page processed by the Faces servlet
               response.setContentType("text/html");
               request.getPortletSession().setAttribute(PortletUtil.PORTLET_REQUEST_FLAG, "true");
               nonFacesRequest(request, response, "/jsp/browse/browse.jsp");
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
               request.getPreferences().reset(PREF_ALF_USERNAME);
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
               request.getPortletSession().removeAttribute(AuthenticationHelper.AUTHENTICATION_USER);
               
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
         UIViewRoot view = viewHandler.createView(context, "/jsp/browse/browse.jsp");
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
    * @return Retrieves the configured login page
    */
   private String getLoginPage()
   {
      if (this.loginPage == null)
      {
         this.loginPage = Application.getLoginPage(getPortletContext());
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
         this.errorPage = Application.getErrorPage(getPortletContext());
      }
      
      return this.errorPage;
   }
}
