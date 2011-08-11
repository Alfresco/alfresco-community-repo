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
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.LoginOutcomeBean;
import org.alfresco.web.bean.repository.Repository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.URLDecoder;
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
   private static final long serialVersionUID = -826295358696861789L;
   
   public static final String FACES_SERVLET = "/faces";
   public static final String KEY_STORE = "store";
   public static final String KEY_ROOT_PATH = "rootPath";
   
   /** an existing Ticket can be passed to most servlet for non-session based authentication */
   private static final String ARG_TICKET   = "ticket";
   
   /** forcing guess access is available on most servlets */
   private static final String ARG_GUEST    = "guest";
   
   private static final String MSG_ERROR_PERMISSIONS = "error_permissions";   
   
   /** list of valid JSPs for redirect after a clean login */
   // TODO: make this list configurable
   private static Set<String> validRedirectJSPs = new HashSet<String>();
   static
   {
      validRedirectJSPs.add("/jsp/browse/browse.jsp");
      validRedirectJSPs.add("/jsp/admin/admin-console.jsp");
      validRedirectJSPs.add("/jsp/admin/avm-console.jsp");
      validRedirectJSPs.add("/jsp/admin/node-browser.jsp");
      validRedirectJSPs.add("/jsp/admin/store-browser.jsp");
      validRedirectJSPs.add("/jsp/users/user-console.jsp");
      validRedirectJSPs.add("/jsp/categories/categories.jsp");
      validRedirectJSPs.add("/jsp/dialog/about.jsp");
      validRedirectJSPs.add("/jsp/search/advanced-search.jsp");
      validRedirectJSPs.add("/jsp/admin/system-info.jsp");
      validRedirectJSPs.add("/jsp/forums/forums.jsp");
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
    * Check the user has the given permission on the given node. If they do not either force a log on if this is a guest
    * user or forward to an error page.
    * 
    * @param req
    *           the request
    * @param res
    *           the response
    * @param nodeRef
    *           the node in question
    * @param allowLogIn
    *           Indicates whether guest users without access to the node should be redirected to the log in page. If
    *           <code>false</code>, a status 403 forbidden page is displayed instead.
    * @return <code>true</code>, if the user has access
    * @throws IOException
    *            Signals that an I/O exception has occurred.
    * @throws ServletException
    *            On other errors
    */
   public boolean checkAccess(HttpServletRequest req, HttpServletResponse res, NodeRef nodeRef, String permission,
         boolean allowLogIn) throws IOException, ServletException
   {
      ServletContext sc = getServletContext();
      ServiceRegistry serviceRegistry = getServiceRegistry(sc);
      PermissionService permissionService = serviceRegistry.getPermissionService();

      // check that the user has the permission
      if (permissionService.hasPermission(nodeRef, permission) == AccessStatus.DENIED)
      {
         if (logger.isDebugEnabled())
            logger.debug("User does not have " + permission + " permission for NodeRef: " + nodeRef.toString());

         if (allowLogIn && serviceRegistry.getAuthorityService().hasGuestAuthority())
         {
            if (logger.isDebugEnabled())
               logger.debug("Redirecting to login page...");
            redirectToLoginPage(req, res, sc);
         }
         else
         {
            if (logger.isDebugEnabled())
               logger.debug("Forwarding to error page...");
            Application
                  .handleSystemError(sc, req, res, MSG_ERROR_PERMISSIONS, HttpServletResponse.SC_FORBIDDEN, logger);
         }
         return false;
      }
      return true;
   }
   
   /**
    * Redirect to the Login page - saving the current URL which can be redirected back later
    * once the user has successfully completed the authentication process.
    */
   public static void redirectToLoginPage(HttpServletRequest req, HttpServletResponse res, ServletContext sc)
         throws IOException
   {
      // authentication failed - so end servlet execution and redirect to login page
      StringBuilder redirectURL = new StringBuilder(1024).append(req.getContextPath()).append(FACES_SERVLET).append(
            Application.getLoginPage(sc));

      // Pass the full requested URL as a parameter so the login page knows where to redirect to later
      String uri = req.getRequestURI();

      // if we find a JSF servlet reference in the URI then we need to check if the rest of the
      // JSP specified is valid for a redirect operation after Login has occured.
      int jspIndex;
      if (uri.indexOf(req.getContextPath() + FACES_SERVLET) == -1
            || uri.length() > (jspIndex = uri.indexOf(BaseServlet.FACES_SERVLET) + BaseServlet.FACES_SERVLET.length())
            && BaseServlet.validRedirectJSP(uri.substring(jspIndex)))
      {
         if (redirectURL.indexOf("?") == -1)
         {
            redirectURL.append('?');
         }
         else
         {
            redirectURL.append('&');
         }
         redirectURL.append(LoginOutcomeBean.PARAM_REDIRECT_URL);
         redirectURL.append('=');
         String url = uri;
         
         // Append the query string if necessary
         String queryString = req.getQueryString();
         if (queryString != null)
         {
            // Strip out leading ticket arguments
            queryString = queryString.replaceAll("(?<=^|&)" + ARG_TICKET + "(=[^&=]*)?&", "");
            // Strip out trailing ticket arguments
            queryString = queryString.replaceAll("(^|&)" + ARG_TICKET + "(=[^&=]*)?(?=&|$)", "");
            if (queryString.length() != 0)
            {
               url += "?" + queryString;
            }
         }
         redirectURL.append(URLEncoder.encode(url, "UTF-8"));
      }
      
      // If external authentication isn't in use (e.g. proxied share authentication), it's safe to return a redirect to the client
      if (AuthenticationHelper.getRemoteUser(sc, req) == null)
      {
         res.sendRedirect(redirectURL.toString());
      }
      // Otherwise, we must signal to the client with an unauthorized status code and rely on a browser refresh to do
      // the redirect for failover login (as we do with NTLM, Kerberos)
      else
      {
         res.setContentType("text/html; charset=UTF-8");
         res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

         final PrintWriter out = res.getWriter();
         out.println("<html><head>");
         out.println("<meta http-equiv=\"Refresh\" content=\"0; url=" + redirectURL + "\">");
         out.println("</head><body><p>Please <a href=\"" + redirectURL + "\">log in</a>.</p>");
         out.println("</body></html>");
         out.close();
      }      
   }
   
   /**
    * Apply the headers required to disallow caching of the response in the browser
    */
   public static void setNoCacheHeaders(HttpServletResponse res)
   {
      res.setHeader("Cache-Control", "no-cache");
      res.setHeader("Pragma", "no-cache");
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
    * @param context Faces context
    * @param args    The elements of the path to lookup
    * @param decode  True to decode the arg from UTF-8 format, false for no decoding
    */
   public static NodeRef resolveWebDAVPath(FacesContext context, String[] args, boolean decode)
   {
      WebApplicationContext wc = FacesContextUtils.getRequiredWebApplicationContext(context);
      return resolveWebDAVPath(wc, args, decode);
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
   private static NodeRef resolveWebDAVPath(final WebApplicationContext wc, final String[] args, final boolean decode)
   {
      return AuthenticationUtil.runAs(new RunAsWork<NodeRef>()
      {
         public NodeRef doWork() throws Exception
         {
            NodeRef nodeRef = null;
            
            List<String> paths = new ArrayList<String>(args.length - 1);

            FileInfo file = null;
            try
            {
               // create a list of path elements (decode the URL as we go)
               for (int x = 1; x < args.length; x++)
               {
                  paths.add(decode ? URLDecoder.decode(args[x]) : args[x]);
               }
               
               if (logger.isDebugEnabled())
                  logger.debug("Attempting to resolve webdav path: " + paths);

               // get the company home node to start the search from
               nodeRef = new NodeRef(Repository.getStoreRef(), Application.getCompanyRootId());
               
               TenantService tenantService = (TenantService)wc.getBean("tenantService");         
               if (tenantService != null && tenantService.isEnabled())
               {
                  if (logger.isDebugEnabled())
                     logger.debug("MT is enabled.");
                  
                  NodeService nodeService = (NodeService) wc.getBean("NodeService");
                  SearchService searchService = (SearchService) wc.getBean("SearchService");
                  NamespaceService namespaceService = (NamespaceService) wc.getBean("NamespaceService");
                  
                  // TODO: since these constants are used more widely than just the WebDAVServlet, 
                  // they should be defined somewhere other than in that servlet
                  String rootPath = wc.getServletContext().getInitParameter(BaseServlet.KEY_ROOT_PATH);
                  
                  // note: rootNodeRef is required (for storeRef part)
                  nodeRef = tenantService.getRootNode(nodeService, searchService, namespaceService, rootPath, nodeRef);
               }
               
               if (paths.size() != 0)
               {
                  FileFolderService ffs = (FileFolderService)wc.getBean("FileFolderService");
                  file = ffs.resolveNamePath(nodeRef, paths);
                  nodeRef = file.getNodeRef();
               }
               
               if (logger.isDebugEnabled())
                  logger.debug("Resolved webdav path to NodeRef: " + nodeRef);
            }
            catch (FileNotFoundException fne)
            {
               if (logger.isWarnEnabled())
                  logger.warn("Failed to resolve webdav path", fne);
               
               nodeRef = null;
            }
            return nodeRef;
         }
      }, AuthenticationUtil.getSystemUserName());
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
