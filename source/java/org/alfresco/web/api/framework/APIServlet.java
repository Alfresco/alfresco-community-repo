/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.api.framework;

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
    private DeclarativeAPIRegistry apiServiceRegistry;
    
    
    @Override
    public void init() throws ServletException
    {
        super.init();

        // Retrieve all web api services and index by http url & http method
        ApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        initContext(context);
        apiServiceRegistry = (DeclarativeAPIRegistry)context.getBean("web.api.framework.registry");
        apiServiceRegistry.initServices();
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

        try
        {
            //
            // Execute appropriate service
            //
            // TODO: Handle errors (with appropriate HTTP error responses) 
    
            String uri = req.getPathInfo();
    
            if (logger.isDebugEnabled())
                logger.debug("Processing request ("  + req.getMethod() + ") " + req.getRequestURL() + (req.getQueryString() != null ? "?" + req.getQueryString() : ""));
  
            APIServiceMatch match = apiServiceRegistry.findService(req.getMethod(), uri);
            if (match != null)
            {
                // setup service context
                APIRequest apiReq = new APIRequest(req, match);
                APIResponse apiRes = new APIResponse(res);
                
                if (logger.isDebugEnabled())
                    logger.debug("Agent: " + apiReq.getAgent());
                
                // execute service
                match.getService().execute(apiReq, apiRes);
            }
            else
            {
                if (logger.isDebugEnabled())
                    logger.debug("Request does not map to service.");
    
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                // TODO: add appropriate error detail 
            }
        }
        // TODO: exception handling
        finally
        {
            long end = System.currentTimeMillis();
            if (logger.isDebugEnabled())
                logger.debug("Processed request (" + req.getMethod() + ") " + req.getRequestURL() + (req.getQueryString() != null ? "?" + req.getQueryString() : "") + " in " + (end - start) + "ms");
        }
    }

    /**
     * Initialise any API beans that require a servlet context
     * 
     * @param appContext  application context
     */
    @SuppressWarnings("unchecked")
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
