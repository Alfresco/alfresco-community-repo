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
package org.alfresco.repo.webdav;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;

import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Servlet that accepts WebDAV requests for the hub. The request is served by the hub's content
 * repository framework and the response sent back using the WebDAV protocol.
 * 
 * @author gavinc
 */
public class WebDAVServlet extends HttpServlet
{
    private static final long serialVersionUID = 6900069445027527165L;

    // Logging
    private static Log logger = LogFactory.getLog("org.alfresco.webdav.protocol");
    
    // Constants
    public static final String WEBDAV_PREFIX = "webdav"; 
    private static final String INTERNAL_SERVER_ERROR = "Internal Server Error: ";

    // Init parameter names
    private static final String BEAN_INIT_PARAMS = "webdav.initParams";
    
    // Service registry, used by methods to find services to process requests
    private ServiceRegistry m_serviceRegistry;
    
    // Transaction service, each request is wrapped in a transaction
    private TransactionService m_transactionService;

    // WebDAV method handlers
    protected Hashtable<String,Class<? extends WebDAVMethod>> m_davMethods;
    
    // Root node
    private static MTNodesCache m_rootNodes;
    
    // WebDAV helper class
    private WebDAVHelper m_davHelper;

    /**
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException
    {
        long startTime = 0;
        if (logger.isInfoEnabled())
        {
            startTime = System.currentTimeMillis();
        }

        try
        {
            // Create the appropriate WebDAV method for the request and execute it
            final WebDAVMethod method = createMethod(request, response);

            if (method == null)
            {
                if ( logger.isErrorEnabled())
                    logger.error("WebDAV method not implemented - " + request.getMethod());
                
                // Return an error status
                
                response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
                return;
            }
            else if (method.getRootNodeRef() == null)
            {
                if ( logger.isErrorEnabled())
                    logger.error("No root node for request");
                
                // Return an error status
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            // Execute the WebDAV request, which must take care of its own transaction
            method.execute();
        }
        catch (Throwable e)
        {
            if (!(e instanceof WebDAVServerException) && e.getCause() != null)
            {
                if (e.getCause() instanceof WebDAVServerException)
                {
                    e = e.getCause();
                }
            }
            // Work out how to handle the error
            if (e instanceof WebDAVServerException)
            {
                WebDAVServerException error = (WebDAVServerException) e;
                if (error.getCause() != null)
                {
                    StringWriter writer = new StringWriter();
                    PrintWriter print = new PrintWriter(writer);
                    error.printStackTrace(print);
                    logger.error(print.toString(), e);
                }

                if (logger.isDebugEnabled())
                {
                    // Show what status code the method sent back
                    
                    logger.debug(request.getMethod() + " is returning status code: " + error.getHttpStatusCode());
                }

                if (response.isCommitted())
                {
                    logger.warn("Could not return the status code to the client as the response has already been committed!");
                }
                else
                {
                    response.sendError(error.getHttpStatusCode());
                }
            }
            else
            {
                StringWriter writer = new StringWriter();
                PrintWriter print = new PrintWriter(writer);
                e.printStackTrace(print);
                logger.error(print.toString(), e);

                if (response.isCommitted())
                {
                    logger.warn("Could not return the internal server error code to the client as the response has already been committed!");
                }
                else
                {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            }
        }
        finally
        {
            if (logger.isInfoEnabled())
            {
                logger.info(request.getMethod() + " took " + (System.currentTimeMillis()-startTime) + "ms to execute ["+request.getRequestURI()+"]");
            }
        }
    }

    /**
     * Create a WebDAV method handler
     * 
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @return WebDAVMethod
     */
    private WebDAVMethod createMethod(HttpServletRequest request, HttpServletResponse response)
    {
        // Get the type of the current request
        
        String strHttpMethod = request.getMethod();

        if (logger.isDebugEnabled())
            logger.debug("WebDAV request " + strHttpMethod + " on path "
                    + request.getRequestURI());

        Class<? extends WebDAVMethod> methodClass = m_davMethods.get(strHttpMethod);
        WebDAVMethod method = null;

        if ( methodClass != null)
        {
            try
            {
                // Create the handler method
                
                method = methodClass.newInstance();
                NodeRef rootNodeRef = m_rootNodes.getNodeForCurrentTenant();
                method.setDetails(request, response, m_davHelper, rootNodeRef);
            }
            catch (Exception ex)
            {
                // Debug
                
                if ( logger.isDebugEnabled())
                    logger.debug(ex);
            }
        }

        // Return the WebDAV method handler, or null if not supported
        
        return method;
    }

    /**
     * Initialize the servlet
     * 
     * @param config ServletConfig
     * @exception ServletException
     */
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);

        // Get service registry        
        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        
        // If no context has been initialised, exit silently so config changes can be made
        if (context == null)
        {
            return;
        }
        
        // Get global configuration properties
        WebApplicationContext wc = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        WebDAVInitParameters initParams = (WebDAVInitParameters) wc.getBean(BEAN_INIT_PARAMS);

        // Render this servlet permanently unavailable if its enablement property is not set
        if (!initParams.getEnabled())
        {
            throw new UnavailableException("WebDAV not enabled.");
        }
        
        // Get root paths
        
        String storeValue = initParams.getStoreName();
        String rootPath = initParams.getRootPath();

        // Get beans
        
        m_serviceRegistry = (ServiceRegistry)context.getBean(ServiceRegistry.SERVICE_REGISTRY);
        
        m_transactionService = m_serviceRegistry.getTransactionService();
        TenantService tenantService = (TenantService) context.getBean("tenantService");
        AuthenticationService authService = (AuthenticationService) context.getBean("authenticationService");
        NodeService nodeService = (NodeService) context.getBean("NodeService");
        SearchService searchService = (SearchService) context.getBean("SearchService");
        NamespaceService namespaceService = (NamespaceService) context.getBean("NamespaceService");
        
        // Create the WebDAV helper
        m_davHelper = new WebDAVHelper(m_serviceRegistry, authService);
        
        // Initialize the root node

        initializeRootNode(storeValue, rootPath, context, nodeService, searchService, namespaceService, tenantService, m_transactionService);
        
        // Create the WebDAV methods table
        
        m_davMethods = new Hashtable<String, Class<? extends WebDAVMethod>>();
        
        m_davMethods.put(WebDAV.METHOD_PROPFIND, PropFindMethod.class);
        m_davMethods.put(WebDAV.METHOD_PROPPATCH, PropPatchMethod.class);
        m_davMethods.put(WebDAV.METHOD_COPY, CopyMethod.class);
        m_davMethods.put(WebDAV.METHOD_DELETE, DeleteMethod.class);
        m_davMethods.put(WebDAV.METHOD_GET, GetMethod.class);
        m_davMethods.put(WebDAV.METHOD_HEAD, HeadMethod.class);
        m_davMethods.put(WebDAV.METHOD_LOCK, LockMethod.class);
        m_davMethods.put(WebDAV.METHOD_MKCOL, MkcolMethod.class);
        m_davMethods.put(WebDAV.METHOD_MOVE, MoveMethod.class);
        m_davMethods.put(WebDAV.METHOD_OPTIONS, OptionsMethod.class);
        m_davMethods.put(WebDAV.METHOD_POST, PostMethod.class);
        m_davMethods.put(WebDAV.METHOD_PUT, PutMethod.class);
        m_davMethods.put(WebDAV.METHOD_UNLOCK, UnlockMethod.class);
    }
    
	
    /**
     * @param storeValue
     * @param rootPath
     * @param context
     * @param nodeService
     * @param searchService
     * @param namespaceService
     * @param tenantService
     * @param m_transactionService
     */
    private void initializeRootNode(String storeValue, String rootPath, WebApplicationContext context, NodeService nodeService, SearchService searchService,
            NamespaceService namespaceService, TenantService tenantService, TransactionService m_transactionService)
    {

        // Use the system user as the authenticated context for the filesystem initialization

        AuthenticationContext authComponent = (AuthenticationContext) context.getBean("authenticationContext");
        authComponent.setSystemUserAsCurrentUser();

        // Wrap the initialization in a transaction

        UserTransaction tx = m_transactionService.getUserTransaction(true);

        try
        {
            // Start the transaction

            if (tx != null)
                tx.begin();

            m_rootNodes = new MTNodesCache(new StoreRef(storeValue), rootPath, nodeService, searchService, namespaceService, tenantService);

            // Commit the transaction

            tx.commit();
        }
        catch (Exception ex)
        {
            logger.error(ex);
        }
        finally
        {
            // Clear the current system user

            authComponent.clearCurrentSecurityContext();
        }
    }
    
    /**
     * 
     * @return root node for WebDAV
     */
    public static NodeRef getWebdavRootNode()
    {
        return m_rootNodes.getNodeForCurrentTenant();
    }
    
    /**
     * Bean to hold injected initialization parameters.
     * 
     * @author Derek Hulley
     * @since V3.5 Team
     */
    public static class WebDAVInitParameters
    {
        private boolean enabled = false;
        private String storeName;
        private String rootPath;
        public boolean getEnabled()
        {
            return enabled;
        }
        public void setEnabled(boolean enabled)
        {
            this.enabled = enabled;
        }
        /**
         * @return              Returns the name of the store
         * @throws ServletException if the store name was not set
         */
        public String getStoreName() throws ServletException
        {
            if (!PropertyCheck.isValidPropertyString(storeName))
            {
                throw new ServletException("WebDAV missing 'storeName' value.");
            }
            return storeName;
        }
        public void setStoreName(String storeName)
        {
            this.storeName = storeName;
        }
        /**
         * @return              Returns the WebDAV root path within the store
         * @throws ServletException if the root path was not set
         */
        public String getRootPath() throws ServletException
        {
            if (!PropertyCheck.isValidPropertyString(rootPath))
            {
                throw new ServletException("WebDAV missing 'rootPath' value.");
            }
            return rootPath;
        }
        public void setRootPath(String rootPath)
        {
            this.rootPath = rootPath;
        }
    }
}
