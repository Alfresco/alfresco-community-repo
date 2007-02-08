/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.webdav;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;

import org.alfresco.filesys.server.config.ServerConfiguration;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.repo.transaction.TransactionUtil.TransactionWork;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;
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
    public static final String KEY_STORE = "store";
    public static final String KEY_ROOT_PATH = "rootPath";
    
    // Service registry, used by methods to find services to process requests
    private ServiceRegistry m_serviceRegistry;
    
    // Transaction service, each request is wrapped in a transaction
    private TransactionService m_transactionService;
    
    // WebDAV method handlers
    private Hashtable<String,Class> m_davMethods;
    
    // Root node
    private NodeRef m_rootNodeRef;
    
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
        if (logger.isDebugEnabled())
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

            // Execute the WebDAV request, wrapped in a transaction
            TransactionWork<Object> methodWork = new TransactionWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    method.execute();
                    return null;
                }
            };
            TransactionUtil.executeInUserTransaction(m_transactionService, methodWork);
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
                    logger.error(INTERNAL_SERVER_ERROR, error.getCause());
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
                logger.error(INTERNAL_SERVER_ERROR, e);

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
            if (logger.isDebugEnabled())
            {
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                logger.debug(request.getMethod() + " took " + duration + "ms to execute");
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

        Class methodClass = m_davMethods.get(strHttpMethod);
        WebDAVMethod method = null;

        if ( methodClass != null)
        {
            try
            {
                // Create the handler method
                
                method = (WebDAVMethod) methodClass.newInstance();
                method.setDetails(request, response, m_davHelper, m_rootNodeRef);
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
        
        WebApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        m_serviceRegistry = (ServiceRegistry)context.getBean(ServiceRegistry.SERVICE_REGISTRY);
        
        m_transactionService = m_serviceRegistry.getTransactionService();
        
        AuthenticationService authService = (AuthenticationService) context.getBean("authenticationService");
        NodeService nodeService = (NodeService) context.getBean("NodeService");
        SearchService searchService = (SearchService) context.getBean("SearchService");
        NamespaceService namespaceService = (NamespaceService) context.getBean("NamespaceService");
        
        // Create the WebDAV helper

        m_davHelper = new WebDAVHelper(m_serviceRegistry, authService);
        
        // Initialize the root node
        
        ServerConfiguration fileSrvConfig = (ServerConfiguration) context.getBean(ServerConfiguration.SERVER_CONFIGURATION);
        if ( fileSrvConfig == null)
            throw new ServletException("File server configuration not available");

        // Use the system user as the authenticated context for the filesystem initialization

        AuthenticationComponent authComponent = fileSrvConfig.getAuthenticationComponent();
        authComponent.setCurrentUser( authComponent.getSystemUserName());
        
        // Wrap the initialization in a transaction
        
        UserTransaction tx = m_transactionService.getUserTransaction(true);
        
        try
        {
            // Start the transaction
            
            if ( tx != null)
                tx.begin();
            
            // Get the store
            
            String storeValue = config.getInitParameter(KEY_STORE);
            if (storeValue == null)
            {
                throw new ServletException("Device missing init value: " + KEY_STORE);
            }
            StoreRef storeRef = new StoreRef(storeValue);
            
            // Connect to the repo and ensure that the store exists
            
            if (! nodeService.exists(storeRef))
            {
                throw new ServletException("Store not created prior to application startup: " + storeRef);
            }
            NodeRef storeRootNodeRef = nodeService.getRootNode(storeRef);
            
            // Get the root path
            
            String rootPath = config.getInitParameter(KEY_ROOT_PATH);
            if (rootPath == null)
            {
                throw new ServletException("Device missing init value: " + KEY_ROOT_PATH);
            }
            
            // Find the root node for this device
            
            List<NodeRef> nodeRefs = searchService.selectNodes(storeRootNodeRef, rootPath, null, namespaceService, false);
            
            if (nodeRefs.size() > 1)
            {
                throw new ServletException("Multiple possible roots for device: \n" +
                        "   root path: " + rootPath + "\n" +
                        "   results: " + nodeRefs);
            }
            else if (nodeRefs.size() == 0)
            {
                // nothing found
                throw new ServletException("No root found for device: \n" +
                        "   root path: " + rootPath);
            }
            else
            {
                // we found a node
                m_rootNodeRef = nodeRefs.get(0);
            }
            
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
        
        // Create the WebDAV methods table
        
        m_davMethods = new Hashtable<String,Class>();
        
        m_davMethods.put(WebDAV.METHOD_PROPFIND, PropFindMethod.class);
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
}
