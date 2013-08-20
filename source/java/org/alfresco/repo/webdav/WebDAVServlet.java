/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
import java.util.Hashtable;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.FileFilterMode;
import org.alfresco.util.FileFilterMode.Client;
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
    //private static final String INTERNAL_SERVER_ERROR = "Internal Server Error: ";
    
    // Init parameter names
    private static final String BEAN_INIT_PARAMS = "webdav.initParams";
    
    // Service registry, used by methods to find services to process requests
    private ServiceRegistry serviceRegistry;
    
    private TransactionService transactionService;
    private static TenantService tenantService;
    private static NodeService nodeService;
    private static SearchService searchService;
    private static NamespaceService namespaceService;
    
    // WebDAV method handlers
    protected Hashtable<String,Class<? extends WebDAVMethod>> m_davMethods;
    
    // note: cache is tenant-aware (if using TransctionalCache impl)
    
    private static SimpleCache<String, NodeRef> singletonCache; // eg. for webdavRootNodeRef
    private static final String KEY_WEBDAV_ROOT_NODEREF = "key.webdavRoot.noderef";
    
    private static String rootPath;
    
    private static NodeRef defaultRootNode; // for default domain
    
    // WebDAV helper class
    private WebDAVHelper m_davHelper;
    private ActivityPoster activityPoster;

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

        FileFilterMode.setClient(Client.webdav);

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
            ExceptionHandler exHandler = new ExceptionHandler(e, request, response);
            exHandler.handle();
        }
        finally
        {
            if (logger.isInfoEnabled())
            {
                logger.info(request.getMethod() + " took " + (System.currentTimeMillis()-startTime) + "ms to execute ["+request.getRequestURI()+"]");
            }

            FileFilterMode.clearClient();
        }
    }

    /**
     * Create a WebDAV method handler
     * 
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @return WebDAVMethod
     */
    protected WebDAVMethod createMethod(HttpServletRequest request, HttpServletResponse response)
    {
        // Get the type of the current request
        
        String strHttpMethod = request.getMethod();

        if (logger.isDebugEnabled())
            logger.debug("WebDAV request " + strHttpMethod + " on path "
                    + request.getRequestURI());

        Class<? extends WebDAVMethod> methodClass = m_davMethods.get(strHttpMethod);
        WebDAVMethod method = null;

        if (methodClass != null)
        {
            try
            {
                // Create the handler method    
                method = methodClass.newInstance();
                method.setDetails(request, response, m_davHelper, getRootNodeRef());
                
                // A very few WebDAV methods produce activity posts.
                if (method instanceof ActivityPostProducer)
                {
                    ActivityPostProducer activityPostProducer = (ActivityPostProducer) method;
                    activityPostProducer.setActivityPoster(activityPoster);
                }
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
    
    private static NodeRef getRootNodeRef()
    {
        NodeRef rootNodeRef = singletonCache.get(KEY_WEBDAV_ROOT_NODEREF);
        
        if (rootNodeRef == null)
        {
            rootNodeRef = tenantService.getRootNode(nodeService, searchService, namespaceService, rootPath, defaultRootNode);
            singletonCache.put(KEY_WEBDAV_ROOT_NODEREF, rootNodeRef);
        }
        
        return rootNodeRef;
    }

    /**
     * Initialize the servlet
     * 
     * @param config ServletConfig
     * @exception ServletException
     */
    @SuppressWarnings("unchecked")
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
        
        rootPath = initParams.getRootPath();
        
        // Get beans
        
        serviceRegistry = (ServiceRegistry)context.getBean(ServiceRegistry.SERVICE_REGISTRY);
        
        transactionService = serviceRegistry.getTransactionService();
        tenantService = (TenantService) context.getBean("tenantService");
        
        nodeService = (NodeService) context.getBean("NodeService");
        searchService = (SearchService) context.getBean("SearchService");
        namespaceService = (NamespaceService) context.getBean("NamespaceService");
        ActivityService activityService = (ActivityService) context.getBean("activityService");
        singletonCache = (SimpleCache<String, NodeRef>)context.getBean("immutableSingletonCache");
        
        
        
        // Collaborator used by WebDAV methods to create activity posts.
        activityPoster = new ActivityPosterImpl("WebDAV", activityService);
        
        // Create the WebDAV helper
        m_davHelper = (WebDAVHelper) context.getBean("webDAVHelper");
        
        // Initialize the root node
        initializeRootNode(storeValue, rootPath, context, nodeService, searchService, namespaceService, tenantService, transactionService);
        
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

    protected WebDAVHelper getDAVHelper()
    {
        return m_davHelper;
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
            
            StoreRef storeRef = new StoreRef(storeValue);
            
            if (nodeService.exists(storeRef) == false)
            {
                throw new RuntimeException("No store for path: " + storeRef);
            }
            
            NodeRef storeRootNodeRef = nodeService.getRootNode(storeRef);
            
            List<NodeRef> nodeRefs = searchService.selectNodes(storeRootNodeRef, rootPath, null, namespaceService, false);
            
            if (nodeRefs.size() > 1)
            {
                throw new RuntimeException("Multiple possible children for : \n" + "   path: " + rootPath + "\n" + "   results: " + nodeRefs);
            }
            else if (nodeRefs.size() == 0)
            {
                throw new RuntimeException("Node is not found for : \n" + "   root path: " + rootPath);
            }
            
            defaultRootNode = nodeRefs.get(0);
            
            // Commit the transaction
            if (tx != null)
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
        return getRootNodeRef();
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
        private String urlPathPrefix;
        
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
        
        /**
         * Get the path prefix that generated URLs should exhibit, e.g.
         * <pre>
         *   http://server.name&lt;prefix&gt;/path/to/file.txt
         * </pre>
         * In the default set up this would be of the form /context-path/servlet-name e.g. /alfresco/webdav:
         * <pre>
         *   http://server.name/alfresco/webdav/path/to/file.txt
         * </pre>
         * however if using URL rewriting rules or a reverse proxy in front of the webdav server
         * you may choose to use, for example / for shorter URLs.
         * <pre>
         *   http://server.name/path/to/file.txt
         * </pre>
         * <p>
         * Leaving this property blank will cause the prefix used to be /context-path/servlet-name
         * 
         * @return the urlPathPrefix
         */
        public String getUrlPathPrefix()
        {
            return urlPathPrefix;
        }
        
        /**
         * See {@link #getUrlPathPrefix()}
         * 
         * @param urlPathPrefix
         */
        public void setUrlPathPrefix(String urlPathPrefix)
        {
            this.urlPathPrefix = urlPathPrefix;
        }
    }
}
