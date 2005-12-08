/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain an
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
package org.alfresco.repo.webdav;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;

import org.alfresco.filesys.server.config.ServerConfiguration;
import org.alfresco.filesys.server.core.InvalidDeviceInterfaceException;
import org.alfresco.filesys.server.core.ShareType;
import org.alfresco.filesys.server.core.SharedDevice;
import org.alfresco.filesys.server.core.SharedDeviceList;
import org.alfresco.filesys.server.filesys.DiskInterface;
import org.alfresco.filesys.server.filesys.DiskSharedDevice;
import org.alfresco.filesys.smb.server.repo.ContentContext;
import org.alfresco.filesys.smb.server.repo.ContentDiskInterface;
import org.alfresco.repo.webdav.auth.AuthenticationFilter;
import org.alfresco.repo.webdav.auth.WebDAVUser;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthenticationService;
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
    public static final String WEBDAV_PREFIX = "webdav"; 
    
    private static final long serialVersionUID = 6900069445027527165L;

    // Logging
    
    private static Log logger = LogFactory.getLog("org.alfresco.webdav.protocol");
    
    // Error message(s)
    
    private static final String INTERNAL_SERVER_ERROR = "Internal Server Error: ";

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

        // Wrap the request in a transaction
        
        UserTransaction tx = m_transactionService.getUserTransaction();

        try
        {
            // Create the appropriate WebDAV method for the request and execute it
            
            WebDAVMethod method = createMethod(request, response);

            if (method == null)
            {
                // Debug
                
                if ( logger.isErrorEnabled())
                    logger.error("WebDAV method not implemented - " + request.getMethod());
                
                // Return an error status
                
                response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
                return;
            }
            else if ( method.getRootNodeRef() == null)
            {
                // Debug
                
                if ( logger.isErrorEnabled())
                    logger.error("No root node for request");
                
                // Return an error status
                
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            // Execute the WebDAV request, wrapped in a transaction
            
            tx.begin();
            method.execute();
            tx.commit();
        }
        catch (Exception e)
        {
            // Whatever happened we need to rollback the transaction
            
            try
            {
                tx.rollback();
            }
            catch (Exception ex)
            {
                logger.warn("Failed to rollback transaction", ex);
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
        
        // Create the WebDAV helper

        m_davHelper = new WebDAVHelper(m_serviceRegistry, authService);
        
        // Initialize the root node
        //
        // For now we get the details from the first available shared filesystem that is configured
        
        ServerConfiguration fileSrvConfig = (ServerConfiguration) context.getBean(ServerConfiguration.SERVER_CONFIGURATION);
        if ( fileSrvConfig == null)
            throw new ServletException("File server configuration not available");

        DiskSharedDevice filesys = fileSrvConfig.getPrimaryFilesystem();
        
        if ( filesys != null)
        {
            // Get the root node from the filesystem
            
            ContentContext contentCtx = (ContentContext) filesys.getContext();
            m_rootNodeRef = contentCtx.getRootNode();
        }
        else
        {
            logger.warn("No default root node for WebDAV, using home node only");
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
