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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
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
 * Base servlet class containing useful constant values and common methods for Alfresco servlets.
 * 
 * @author Kevin Roast
 */
public abstract class BaseServlet extends HttpServlet
{
   public static final String FACES_SERVLET = "/faces";
   
   /** an existing Ticket can be passed to most servlet for non-session based authentication */
   private static final String ARG_TICKET   = "ticket";
   
   /** forcing guess access is available on most servlets */
   private static final String ARG_GUEST    = "guest";
   
   /** list of valid JSPs for redirect after a clean login */
   // TODO: make this list configurable
   private static Set<String> validRedirectJSPs = new HashSet<String>();
   static
   {
      validRedirectJSPs.add("/jsp/browse/browse.jsp");
      validRedirectJSPs.add("/jsp/browse/dashboard.jsp");
      validRedirectJSPs.add("/jsp/admin/admin-console.jsp");
      validRedirectJSPs.add("/jsp/admin/node-browser.jsp");
      validRedirectJSPs.add("/jsp/admin/store-browser.jsp");
      validRedirectJSPs.add("/jsp/categories/categories.jsp");
      validRedirectJSPs.add("/jsp/dialog/about.jsp");
      validRedirectJSPs.add("/jsp/dialog/advanced-search.jsp");
      validRedirectJSPs.add("/jsp/dialog/system-info.jsp");
      validRedirectJSPs.add("/jsp/users/users.jsp");
      validRedirectJSPs.add("/jsp/trashcan/trash-list.jsp");
   }
   
   private static Log logger = LogFactory.getLog(BaseServlet.class);
   
   
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
    * Perform an authentication for the servlet request URI. Processing any "ticket" or
    * "guest" URL arguments.
    * 
    * @return AuthenticationStatus
    * 
    * @throws IOException
    */
   public AuthenticationStatus servletAuthenticate(HttpServletRequest req, HttpServletResponse res) 
      throws IOException
   {
      return servletAuthenticate(req, res, true);
   }
   
   /**
    * Perform an authentication for the servlet request URI. Processing any "ticket" or
    * "guest" URL arguments.
    * 
    * @return AuthenticationStatus
    * 
    * @throws IOException
    */
   public AuthenticationStatus servletAuthenticate(HttpServletRequest req, HttpServletResponse res,
         boolean redirectToLoginPage) throws IOException
   {
      AuthenticationStatus status;
      
      // see if a ticket or a force Guest parameter has been supplied
      String ticket = req.getParameter(ARG_TICKET);
      if (ticket != null && ticket.length() != 0)
      {
         status = AuthenticationHelper.authenticate(getServletContext(), req, res, ticket);
      }
      else
      {
         boolean forceGuest = false;
         String guest = req.getParameter(ARG_GUEST);
         if (guest != null)
         {
            forceGuest = Boolean.parseBoolean(guest);
         }
         status = AuthenticationHelper.authenticate(getServletContext(), req, res, forceGuest);
      }
      if (status == AuthenticationStatus.Failure && redirectToLoginPage)
      {
         // authentication failed - now need to display the login page to the user, if asked to
         redirectToLoginPage(req, res, getServletContext());
      }
      
      return status;
   }
   
   /**
    * Redirect to the Login page - saving the current URL which can be redirected back later
    * once the user has successfully completed the authentication process.
    */
   public static void redirectToLoginPage(HttpServletRequest req, HttpServletResponse res, ServletContext sc)
      throws IOException
   {
      // authentication failed - so end servlet execution and redirect to login page
      // also save the full requested URL so the login page knows where to redirect too later
      res.sendRedirect(req.getContextPath() + FACES_SERVLET + Application.getLoginPage(sc));
      String uri = req.getRequestURI();
      String url = uri + (req.getQueryString() != null ? ("?" + req.getQueryString()) : "");
      if (uri.indexOf(req.getContextPath() + FACES_SERVLET) != -1)
      {
         // if we find a JSF servlet reference in the URI then we need to check if the rest of the
         // JSP specified is valid for a redirect operation after Login has occured.
         int jspIndex = uri.indexOf(BaseServlet.FACES_SERVLET) + BaseServlet.FACES_SERVLET.length();
         if (uri.length() > jspIndex && BaseServlet.validRedirectJSP(uri.substring(jspIndex)))
         {
            req.getSession().setAttribute(LoginBean.LOGIN_REDIRECT_KEY, url);
         }
      }
      else
      {
         req.getSession().setAttribute(LoginBean.LOGIN_REDIRECT_KEY, url);
      }
   }
   
   /**
    * Returns true if the specified JSP file is valid for a redirect after login.
    * Only a specific sub-set of the available JSPs are valid to jump directly too after a
    * clean login attempt - e.g. those that do not require JSF bean context setup. This is
    * a limitation of the JSP architecture. The ExternalAccessServlet provides a mechanism to
    * setup the JSF bean context directly for some specific cases.
    * 
    * @param jsp     Filename of JSP to check, for example "/jsp/browse/browse.jsp"
    * 
    * @return true if the JSP is in the list of valid direct URLs, false otherwise
    */
   public static boolean validRedirectJSP(String jsp)
   {
      return validRedirectJSPs.contains(jsp);
   }
   
   /**
    * Resolves the given path elements to a NodeRef in the current repository
    * 
    * @param context Faces context
    * @param args    The elements of the path to lookup
    */
   public static NodeRef resolveWebDAVPath(FacesContext context, String[] args)
   {
      WebApplicationContext wc = FacesContextUtils.getRequiredWebApplicationContext(context);
      return resolveWebDAVPath(wc, args, true);
   }
   
   /**
    * Resolves the given path elements to a NodeRef in the current repository
    * 
    * @param context ServletContext context
    * @param args    The elements of the path to lookup
    */
   public static NodeRef resolveWebDAVPath(ServletContext context, String[] args)
   {
      WebApplicationContext wc = WebApplicationContextUtils.getRequiredWebApplicationContext(context);
      return resolveWebDAVPath(wc, args, true);
   }
   
   /**
    * Resolves the given path elements to a NodeRef in the current repository
    * 
    * @param context ServletContext context
    * @param args    The elements of the path to lookup
    * @param decode  True to decode the arg from UTF-8 format, false for no decoding
    */
   public static NodeRef resolveWebDAVPath(ServletContext context, String[] args, boolean decode)
   {
      WebApplicationContext wc = WebApplicationContextUtils.getRequiredWebApplicationContext(context);
      return resolveWebDAVPath(wc, args, decode);
   }
   
   /**
    * Resolves the given path elements to a NodeRef in the current repository
    * 
    * @param WebApplicationContext Context
    * @param args    The elements of the path to lookup
    * @param decode  True to decode the arg from UTF-8 format, false for no decoding
    */
   private static NodeRef resolveWebDAVPath(WebApplicationContext wc, String[] args, boolean decode)
   {
      NodeRef nodeRef = null;

      List<String> paths = new ArrayList<String>(args.length-1);
      
      FileInfo file = null;
      try
      {
         // create a list of path elements (decode the URL as we go)
         for (int x = 1; x < args.length; x++)
         {
            paths.add(decode ? URLDecoder.decode(args[x], "UTF-8") : args[x]);
         }
         
         if (logger.isDebugEnabled())
            logger.debug("Attempting to resolve webdav path: " + paths);
         
         // get the company home node to start the search from
         NodeRef companyHome = new NodeRef(Repository.getStoreRef(), 
               Application.getCompanyRootId());
         
         FileFolderService ffs = (FileFolderService)wc.getBean("FileFolderService");
         file = ffs.resolveNamePath(companyHome, paths);
         nodeRef = file.getNodeRef();
         
         if (logger.isDebugEnabled())
            logger.debug("Resolved webdav path to NodeRef: " + nodeRef);
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
            logger.warn("Failed to resolve webdav path", fne);
         
         nodeRef = null;
      }
      
      return nodeRef;
   }
   
   /**
    * Resolve a name based into a NodeRef and Filename string
    *  
    * @param sc      ServletContext
    * @param path    'cm:name' based path using the '/' character as a separator
    *  
    * @return PathRefInfo structure containing the resolved NodeRef and filename
    * 
    * @throws IllegalArgumentException
    */
   public final static PathRefInfo resolveNamePath(ServletContext sc, String path)
   {
      StringTokenizer t = new StringTokenizer(path, "/");
      int tokenCount = t.countTokens();
      String[] elements = new String[tokenCount];
      for (int i=0; i<tokenCount; i++)
      {
         elements[i] = t.nextToken();
      }
      
      // process name based path tokens using the webdav path resolving helper 
      NodeRef nodeRef = resolveWebDAVPath(sc, elements, false);
      if (nodeRef == null)
      {
         // unable to resolve path - output helpful error to the user
         throw new IllegalArgumentException("Unable to resolve item Path: " + path);
      }
      
      return new PathRefInfo(nodeRef, elements[tokenCount - 1]);
   }
   
   /**
    * Simple structure class for returning both a NodeRef and Filename String
    * @author Kevin Roast
    */
   public static class PathRefInfo
   {
      PathRefInfo(NodeRef ref, String filename)
      {
         this.NodeRef = ref;
         this.Filename = filename;
      }
      public NodeRef NodeRef;
      public String Filename;
   }
}
