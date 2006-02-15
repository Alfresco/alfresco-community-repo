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
import java.text.MessageFormat;
import java.util.StringTokenizer;

import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.webdav.WebDAVServlet;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.BrowseBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Servlet allowing external URL access to various global JSF views in the Web Client.
 * <p>
 * The servlet accepts a well formed URL that can easily be generated from a Content or Space NodeRef.
 * The URL also specifies the JSF "outcome" to be executed which provides the correct JSF View to be
 * displayed. The JSF "outcome" must equate to a global navigation rule or it will not be displayed.
 * Servlet URL is of the form:
 * <p>
 * <code>http://&lt;server&gt;/alfresco/navigate/&lt;outcome&gt;[/&lt;workspace&gt;/&lt;store&gt;/&lt;nodeId&gt;]</code> or <br/>
 * <code>http://&lt;server&gt;/alfresco/navigate/&lt;outcome&gt;[/webdav/&lt;path/to/node&gt;]</code>
 * <p>
 * Like most Alfresco servlets, the URL may be followed by a valid 'ticket' argument for authentication:
 * ?ticket=1234567890
 * <p>
 * And/or also followed by the "?guest=true" argument to force guest access login for the URL.
 * 
 * @author Kevin Roast
 */
public class ExternalAccessServlet extends BaseServlet
{
   private static final long serialVersionUID = -4118907921337237802L;
   
   private static Log logger = LogFactory.getLog(ExternalAccessServlet.class);
   
   private final static String OUTCOME_DOCDETAILS   = "showDocDetails";
   private final static String OUTCOME_SPACEDETAILS = "showSpaceDetails";
   private final static String OUTCOME_BROWSE       = "browse";
   private final static String OUTCOME_LOGOUT       = "logout";
   
   private static final String ARG_TEMPLATE  = "template";
   
   /**
    * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    */
   protected void service(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException
   {
      String uri = req.getRequestURI();
      
      if (logger.isDebugEnabled())
         logger.debug("Processing URL: " + uri + (req.getQueryString() != null ? ("?" + req.getQueryString()) : ""));
      
      AuthenticationStatus status = servletAuthenticate(req, res);
      if (status == AuthenticationStatus.Failure)
      {
         return;
      }
      
      StringTokenizer t = new StringTokenizer(uri, "/");
      int count = t.countTokens();
      if (count < 3)
      {
         throw new IllegalArgumentException("Externally addressable URL did not contain all required args: " + uri); 
      }
      t.nextToken();    // skip web app name
      t.nextToken();    // skip servlet name
      
      String outcome = t.nextToken();
      
      // get rest of the tokens arguments
      String[] args = new String[count - 3];
      for (int i=0; i<count - 3; i++)
      {
         args[i] = t.nextToken();
      }
      
      if (logger.isDebugEnabled())
         logger.debug("External outcome found: " + outcome);
      
      // we almost always need this bean reference
      FacesContext fc = FacesHelper.getFacesContext(req, res, getServletContext());
      BrowseBean browseBean = (BrowseBean)FacesHelper.getManagedBean(fc, "BrowseBean");
      
      // get services we need
      ServiceRegistry serviceRegistry = getServiceRegistry(getServletContext());
      PermissionService permissionService = serviceRegistry.getPermissionService();
      
      // setup is required for certain outcome requests
      if (OUTCOME_DOCDETAILS.equals(outcome))
      {
         NodeRef nodeRef = null;
         
         if (args[0].equals(WebDAVServlet.WEBDAV_PREFIX))
         {
            nodeRef = resolveWebDAVPath(fc, args);
         }
         else if (args.length == 3)
         {
            StoreRef storeRef = new StoreRef(args[0], args[1]);
            nodeRef = new NodeRef(storeRef, args[2]);
         }
         
         if (nodeRef != null)
         {
            // check that the user has at least READ access - else redirect to the login page
            if (permissionService.hasPermission(nodeRef, PermissionService.READ) == AccessStatus.DENIED)
            {
               if (logger.isDebugEnabled())
                  logger.debug("User does not have permissions to READ NodeRef: " + nodeRef.toString());
               redirectToLoginPage(req, res, getServletContext());
               return;
            }
            
            // setup the Document on the browse bean
            browseBean.setupContentAction(nodeRef.getId(), true);
         }
         
         // perform the appropriate JSF navigation outcome
         NavigationHandler navigationHandler = fc.getApplication().getNavigationHandler();
         navigationHandler.handleNavigation(fc, null, outcome);
      }
      else if (OUTCOME_SPACEDETAILS.equals(outcome))
      {
         NodeRef nodeRef = null;
         
         if (args[0].equals(WebDAVServlet.WEBDAV_PREFIX))
         {
            nodeRef = resolveWebDAVPath(fc, args);
         }
         else if (args.length == 3)
         {
            StoreRef storeRef = new StoreRef(args[0], args[1]);
            nodeRef = new NodeRef(storeRef, args[2]);
         }
         
         if (nodeRef != null)
         {
            // check that the user has at least READ access - else redirect to the login page
            if (permissionService.hasPermission(nodeRef, PermissionService.READ) == AccessStatus.DENIED)
            {
               if (logger.isDebugEnabled())
                  logger.debug("User does not have permissions to READ NodeRef: " + nodeRef.toString());
               redirectToLoginPage(req, res, getServletContext());
               return;
            }
            
            // setup the Space on the browse bean
            browseBean.setupSpaceAction(nodeRef.getId(), true);
         }
         
         // perform the appropriate JSF navigation outcome
         NavigationHandler navigationHandler = fc.getApplication().getNavigationHandler();
         navigationHandler.handleNavigation(fc, null, outcome);
      }
      else if (OUTCOME_BROWSE.equals(outcome))
      {
         if (args != null && args.length >= 3)
         {
            NodeRef nodeRef = null;
            int offset = 0;

            offset = args.length - 3;
            StoreRef storeRef = new StoreRef(args[0+offset], args[1+offset]);
            nodeRef = new NodeRef(storeRef, args[2+offset]);
            
            // check that the user has at least READ access - else redirect to the login page
            if (permissionService.hasPermission(nodeRef, PermissionService.READ) == AccessStatus.DENIED)
            {
               if (logger.isDebugEnabled())
                  logger.debug("User does not have permissions to READ NodeRef: " + nodeRef.toString());
               redirectToLoginPage(req, res, getServletContext());
               return;
            }
            
            // this call sets up the current node Id, and updates or initialises the
            // breadcrumb component with the selected node as appropriate.
            browseBean.updateUILocation(nodeRef);
            
            // force a "late" refresh of the BrowseBean to handle external servlet access URL
            browseBean.externalAccessRefresh();
            
            // check for view mode first argument
            if (args[0].equals(ARG_TEMPLATE))
            {
               browseBean.setDashboardView(true);
            }
            
            // the above calls into BrowseBean setup the NavigationHandler automatically
         }
         else
         {
            // perform the appropriate JSF navigation outcome
            NavigationHandler navigationHandler = fc.getApplication().getNavigationHandler();
            navigationHandler.handleNavigation(fc, null, outcome);
         }
      }
      else if (OUTCOME_LOGOUT.equals(outcome))
      {
         // special case for logout
         req.getSession().invalidate();
         res.sendRedirect(req.getContextPath() + FACES_SERVLET + Application.getLoginPage(getServletContext()));
         return;
      }
      
      // perform the forward to the page processed by the Faces servlet
      String viewId = fc.getViewRoot().getViewId();
      getServletContext().getRequestDispatcher(FACES_SERVLET + viewId).forward(req, res);
   }
   
   /**
    * Generate a URL to the External Access Servlet.
    * Allows access to JSF views (via an "outcome" ID) from external URLs.
    * 
    * @param outcome
    * @param args
    * 
    * @return URL
    */
   public final static String generateExternalURL(String outcome, String args)
   {
      if (args == null)
      {
         return MessageFormat.format(EXTERNAL_URL, new Object[] {outcome} );
      }
      else
      {
         return MessageFormat.format(EXTERNAL_URL_ARGS, new Object[] {outcome, args} );
      }
   }
   
   // example: http://<server>/alfresco/navigate/<outcome>[/<workspace>/<store>/<nodeId>]
   private static final String EXTERNAL_URL  = "/navigate/{0}";
   private static final String EXTERNAL_URL_ARGS  = "/navigate/{0}/{1}";
}
