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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.LoginBean;
import org.alfresco.web.bean.repository.Repository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * Useful constant values and common methods for Alfresco servlets.
 * 
 * @author Kevin Roast
 */
public final class ServletHelper
{
   /** an existing Ticket can be passed to most servlet for non-session based authentication */
   private static final String ARG_TICKET   = "ticket";
   
   /** forcing guess access is available on most servlets */
   private static final String ARG_GUEST    = "guest";
   
   private static Log logger = LogFactory.getLog(ServletHelper.class);
   
   
   /**
    * Return the ServiceRegistry helper instance
    * 
    * @param sc      ServletContext
    * 
    * @return ServiceRegistry
    */
   public static ServiceRegistry getServiceRegistry(ServletContext sc)
   {
      WebApplicationContext wc = WebApplicationContextUtils.getRequiredWebApplicationContext(sc);
      return (ServiceRegistry)wc.getBean(ServiceRegistry.SERVICE_REGISTRY);
   }
   
   /**
    * Private constructor
    */
   private ServletHelper()
   {
   }
   
   /**
    * Perform an authentication for the servlet request URI. Processing any "ticket" or
    * "guest" URL arguments.
    * 
    * @return AuthenticationStatus
    * 
    * @throws IOException
    */
   public static AuthenticationStatus servletAuthenticate(HttpServletRequest req, HttpServletResponse res, ServletContext sc)
      throws IOException
   {
      AuthenticationStatus status;
      
      // see if a ticket or a force Guest parameter has been supplied
      String ticket = req.getParameter(ServletHelper.ARG_TICKET);
      if (ticket != null && ticket.length() != 0)
      {
         status = AuthenticationHelper.authenticate(sc, req, res, ticket);
      }
      else
      {
         boolean forceGuest = false;
         String guest = req.getParameter(ServletHelper.ARG_GUEST);
         if (guest != null)
         {
            forceGuest = Boolean.parseBoolean(guest);
         }
         status = AuthenticationHelper.authenticate(sc, req, res, forceGuest);
      }
      if (status == AuthenticationStatus.Failure)
      {
         // authentication failed - no point returning the content as we haven't logged in yet
         // so end servlet execution and save the URL so the login page knows what to do later
         req.getSession().setAttribute(LoginBean.LOGIN_REDIRECT_KEY, req.getRequestURI() + (req.getQueryString() != null ? ("?" + req.getQueryString()) : ""));
      }
      
      return status;
   }
   
   /**
    * Return a JSF managed bean reference.
    * 
    * @param fc      FacesContext
    * @param name    Name of the managed bean to return
    * 
    * @return the managed bean or null if not found
    */
   public static Object getManagedBean(FacesContext fc, String name)
   {
      ValueBinding vb = fc.getApplication().createValueBinding("#{" + name + "}");
      return vb.getValue(fc);
   }
   
   /**
    * Resolves the given path elements to a NodeRef in the current repository
    * 
    * @param context Faces context
    * @param args The elements of the path to lookup
    */
   public static NodeRef resolveWebDAVPath(FacesContext context, String[] args)
   {
      NodeRef nodeRef = null;

      List<String> paths = new ArrayList<String>(args.length-1);
      
      FileInfo file = null;
      try
      {
         // create a list of path elements (decode the URL as we go)
         for (int x = 1; x < args.length; x++)
         {
            paths.add(URLDecoder.decode(args[x], "UTF-8"));
         }
         
         if (logger.isDebugEnabled())
            logger.debug("Attempting to resolve webdav path to NodeRef: " + paths);
         
         // get the company home node to start the search from
         NodeRef companyHome = new NodeRef(Repository.getStoreRef(), 
               Application.getCompanyRootId());
         
         WebApplicationContext wc = FacesContextUtils.getRequiredWebApplicationContext(context);
         FileFolderService ffs = (FileFolderService)wc.getBean("FileFolderService");
         file = ffs.resolveNamePath(companyHome, paths);
         nodeRef = file.getNodeRef();
      }
      catch (UnsupportedEncodingException uee)
      {
         if (logger.isWarnEnabled())
            logger.warn("Failed to resolve webdav path", uee);
         
         nodeRef = null;
      }
      catch (FileNotFoundException fne)
      {
         if (logger.isWarnEnabled())
            logger.debug("Failed to resolve webdav path", fne);
         
         nodeRef = null;
      }
      
      return nodeRef;
   }
}
