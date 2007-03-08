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
package org.alfresco.web.scripts;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;


/**
 * Entry point for Web Scripts
 * 
 * @author davidc
 */
public class WebScriptServlet extends HttpServlet
{
    private static final long serialVersionUID = 4209892938069597860L;

    // Logger
    private static final Log logger = LogFactory.getLog(WebScriptServlet.class);

    // Web Scripts
    private DeclarativeWebScriptRegistry registry;
    
    
    @Override
    public void init() throws ServletException
    {
        super.init();
        ApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        registry = (DeclarativeWebScriptRegistry)context.getBean("webscripts.registry");
        registry.initWebScripts();
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
            // Execute appropriate web scripy
            //
            // TODO: Handle errors (with appropriate HTTP error responses) 
    
            String uri = req.getPathInfo();
    
            if (logger.isDebugEnabled())
                logger.debug("Processing request ("  + req.getMethod() + ") " + req.getRequestURL() + (req.getQueryString() != null ? "?" + req.getQueryString() : ""));
  
            WebScriptMatch match = registry.findWebScript(req.getMethod(), uri);
            if (match != null)
            {
                // setup web script context
                WebScriptRequest apiReq = new WebScriptRequest(req, match);
                WebScriptResponse apiRes = new WebScriptResponse(res);
                
                if (logger.isDebugEnabled())
                    logger.debug("Agent: " + apiReq.getAgent());
                
                // execute service
                match.getWebScript().execute(apiReq, apiRes);
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
    
}
