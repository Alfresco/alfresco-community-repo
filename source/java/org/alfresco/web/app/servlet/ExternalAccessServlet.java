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
import java.text.MessageFormat;
import java.util.Stack;
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
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.BrowseBean;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.bean.dashboard.DashboardManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.shared_impl.renderkit.ViewSequenceUtils;

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
   
   public final static String OUTCOME_DOCDETAILS          = "showDocDetails";
   public final static String OUTCOME_SPACEDETAILS        = "showSpaceDetails";
   public final static String OUTCOME_BROWSE              = "browse";
   public final static String OUTCOME_MYALFRESCO          = "myalfresco";
   public final static String OUTCOME_LOGOUT              = "logout";
   public final static String OUTCOME_DIALOG              = "dialog";
   public final static String OUTCOME_WIZARD              = "wizard";
   
   private static final String ARG_TEMPLATE  = "template";
   private static final String ARG_PAGE = "page";
   
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
      
      setNoCacheHeaders(res);
      
      uri = uri.substring(req.getContextPath().length());
      StringTokenizer t = new StringTokenizer(uri, "/");
      int tokenCount = t.countTokens();
      if (tokenCount < 2)
      {
         throw new IllegalArgumentException("Externally addressable URL did not contain all required args: " + uri); 
      }
      
      t.nextToken();    // skip servlet name
      
      String outcome = t.nextToken();
      
      // get rest of the tokens arguments
      String[] args = new String[tokenCount - 2];
      for (int i=0; i<tokenCount - 2; i++)
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
      
      // as we are potentially coming in from an external app reset the view stack
      Stack viewStack = (Stack)fc.getExternalContext().getSessionMap().get("_alfViewStack");
      if (viewStack != null)
      {
         viewStack.clear();
         
         if (logger.isDebugEnabled())
            logger.debug("Cleared view stack");
      }
      
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
            // check that the user has at least READ access - else redirect to an error or login page
            if (!checkAccess(req, res, nodeRef, PermissionService.READ_CONTENT, true))
            {
               return;
            }
            
            // setup the Document on the browse bean
            browseBean.setupContentAction(nodeRef.getId(), true);
         }
         
         fc.getExternalContext().getSessionMap().put(AlfrescoNavigationHandler.EXTERNAL_CONTAINER_SESSION, Boolean.TRUE);
         // perform the appropriate JSF navigation outcome
         NavigationHandler navigationHandler = fc.getApplication().getNavigationHandler();
         navigationHandler.handleNavigation(fc, null, "dialog:" + OUTCOME_DOCDETAILS);
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
            // check that the user has at least READ access - else redirect to an error or login page
            if (!checkAccess(req, res, nodeRef, PermissionService.READ_CONTENT, true))
            {
               return;
            }
            
            // setup the Space on the browse bean
            browseBean.setupSpaceAction(nodeRef.getId(), true);
         }
         fc.getExternalContext().getSessionMap().put(AlfrescoNavigationHandler.EXTERNAL_CONTAINER_SESSION, Boolean.TRUE);         
         // perform the appropriate JSF navigation outcome
         NavigationHandler navigationHandler = fc.getApplication().getNavigationHandler();
         navigationHandler.handleNavigation(fc, null, "dialog:" + OUTCOME_SPACEDETAILS);
      }
      else if (OUTCOME_BROWSE.equals(outcome))
      {
         NodeRef nodeRef = null;
         
         if (args.length != 0 && args[0].equals(WebDAVServlet.WEBDAV_PREFIX))
         {
            nodeRef = resolveWebDAVPath(fc, args);
         }
         else if (args.length >= 3)
         {
            int offset = 0;

            offset = args.length - 3;
            StoreRef storeRef = new StoreRef(args[0+offset], args[1+offset]);
            nodeRef = new NodeRef(storeRef, args[2+offset]);
         }
         
         if (nodeRef != null)
         {
            // check that the user has at least READ access - else redirect to an error or login page
            if (!checkAccess(req, res, nodeRef, PermissionService.READ_CONTENT, true))
            {
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
      else if (OUTCOME_MYALFRESCO.equals(outcome))
      {
         // setup the Dashboard Manager ready for the page we want to display
         if (req.getParameter(ARG_PAGE) != null)
         {
            DashboardManager manager = (DashboardManager)FacesHelper.getManagedBean(fc, DashboardManager.BEAN_NAME);
            manager.getPageConfig().setCurrentPage(req.getParameter(ARG_PAGE));
         }
         
         // perform the appropriate JSF navigation outcome
         NavigationHandler navigationHandler = fc.getApplication().getNavigationHandler();
         navigationHandler.handleNavigation(fc, null, outcome);
      }
      else if (OUTCOME_DIALOG.equals(outcome) || OUTCOME_WIZARD.equals(outcome))
      {
         if (args.length != 0)
         {
            if (args.length > 1)
            {
            	String currentNodeId = null;
            	
                if (args[1].equals(WebDAVServlet.WEBDAV_PREFIX))
                {
                	// Drop the first argument
                    String[] args2 = new String[ args.length -1 ];
                    for (int i=1; i<args.length; i++)
                    {
                       args2[i-1] = args[i];
                       
                       if (logger.isDebugEnabled())
                       {
                           logger.debug("Added segment " + args2[i-1]);
                       }
                    }
                	
                	NodeRef nodeRef = resolveWebDAVPath(fc, args2);
                	currentNodeId = nodeRef.getId();
                }
                else
                {
                	currentNodeId = args[1];
                }
                
                if (logger.isDebugEnabled())
                {
                    logger.debug("currentNodeId: " + currentNodeId);
                }
            	
               // if a GUID was passed, use it to init the NavigationBean current context
               NavigationBean navigator = (NavigationBean)FacesHelper.getManagedBean(fc, NavigationBean.BEAN_NAME);
               navigator.setCurrentNodeId(currentNodeId);
               browseBean.setupSpaceAction(currentNodeId, true);
               
               // setup the Document on the browse bean
               // avoid java.lang.NullPointerException
               // at org.alfresco.web.bean.content.InviteContentUsersWizard.getPermissionsForType(InviteContentUsersWizard.java:49)
               // at org.alfresco.web.bean.wizard.BaseInviteUsersWizard.getRoles(BaseInviteUsersWizard.java:562)
               browseBean.setupContentAction(currentNodeId, true);
            }
            
            NavigationHandler navigationHandler = fc.getApplication().getNavigationHandler();
            navigationHandler.handleNavigation(fc, null, outcome + ':' + args[0]);
         }
      }
      else if (OUTCOME_LOGOUT.equals(outcome))
      {
         // special case for logout
         // invalidate ticket and clear the Security context for this thread
         Application.logOut(fc);
         res.sendRedirect(req.getContextPath() + FACES_SERVLET + Application.getLoginPage(getServletContext()));
         return;
      }
      
      // perform the forward to the page processed by the Faces servlet
      String viewId = fc.getViewRoot().getViewId();
      ViewSequenceUtils.nextViewSequence(fc);      
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
   private static final String EXTERNAL_URL  = "/n/{0}";
   private static final String EXTERNAL_URL_ARGS  = "/n/{0}/{1}";
}
