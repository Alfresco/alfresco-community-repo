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
package org.alfresco.web.api;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;


/**
 * Entry point for web based services (REST style)
 * 
 * @author davidc
 */
public class APIServlet extends HttpServlet
{
    private static final long serialVersionUID = 4209892938069597860L;

    // Logger
    private static final Log logger = LogFactory.getLog(APIServlet.class);

    // API Services
    private APIServiceRegistry apiServiceRegistry;
    
    
    @Override
    public void init() throws ServletException
    {
        super.init();

        // Retrieve all web api services and index by http url & http method
        ApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        initContext(context);
        apiServiceRegistry = new APIServiceRegistry(context);
    }


// TODO:
// - authentication (as suggested in http://www.xml.com/pub/a/2003/12/17/dive.html)
              

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        long start = System.currentTimeMillis();
        APIRequest request = new APIRequest(req);
        APIResponse response = new APIResponse(res);

        try
        {
            //
            // Execute appropriate service
            //
            // TODO: Handle errors (with appropriate HTTP error responses) 
    
            APIRequest.HttpMethod method = request.getHttpMethod();
            String uri = request.getPathInfo();
    
            if (logger.isDebugEnabled())
                logger.debug("Processing request ("  + request.getHttpMethod() + ") " + request.getRequestURL() + (request.getQueryString() != null ? "?" + request.getQueryString() : "") + " (agent: " + request.getAgent() + ")");
            
            APIService service = apiServiceRegistry.get(method, uri);
            if (service != null)
            {
                // TODO: Wrap in single transaction
                service.execute(request, response);
            }
            else
            {
                if (logger.isDebugEnabled())
                    logger.debug("Request does not map to service.");
    
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                // TODO: add appropriate error detail 
            }
        }
        // TODO: exception handling
        finally
        {
            long end = System.currentTimeMillis();
            if (logger.isDebugEnabled())
                logger.debug("Processed request (" + request.getHttpMethod() + ") " + request.getRequestURL() + (request.getQueryString() != null ? "?" + request.getQueryString() : "") + " in " + (end - start) + "ms");
        }
    }

    /**
     * Initialise any API beans that require a servlet context
     * 
     * @param appContext  application context
     */
    private void initContext(ApplicationContext appContext)
    {
        ServletContext servletContext = getServletContext();
        Map<String, APIContextAware> contextAwareMap = appContext.getBeansOfType(APIContextAware.class, false, false);
        for (APIContextAware contextAware: contextAwareMap.values())
        {
            contextAware.setAPIContext(servletContext);
        }
    }
    
}
