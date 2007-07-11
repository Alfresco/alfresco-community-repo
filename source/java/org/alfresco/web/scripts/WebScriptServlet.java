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

import org.alfresco.config.Config;
import org.alfresco.config.ConfigService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.web.config.ServerConfigElement;
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

    // Component Dependencies
    private DeclarativeWebScriptRegistry registry;
    private ServiceRegistry serviceRegistry;
    private WebScriptServletAuthenticator authenticator;
    protected ConfigService configService;

    /** Host Server Configuration */
    private static ServerConfigElement serverConfig;

    
    @Override
    public void init() throws ServletException
    {
        super.init();
        ApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        registry = (DeclarativeWebScriptRegistry)context.getBean("webscripts.registry");
        serviceRegistry = (ServiceRegistry)context.getBean(ServiceRegistry.SERVICE_REGISTRY);
        configService = (ConfigService)context.getBean("webClientConfigService");

        // retrieve authenticator via servlet initialisation parameter
        String authenticatorId = getInitParameter("authenticator");
        if (authenticatorId == null || authenticatorId.length() == 0)
        {
            authenticatorId = "webscripts.authenticator.webclient";
        }
        Object bean = context.getBean(authenticatorId);
        if (bean == null || !(bean instanceof WebScriptServletAuthenticator))
        {
            throw new ServletException("Initialisation parameter 'authenticator' does not refer to a Web Script authenticator (" + authenticatorId + ")");
        }
        authenticator = (WebScriptServletAuthenticator)bean;
        
        // retrieve host server configuration 
        Config config = configService.getConfig("Server");
        serverConfig = (ServerConfigElement)config.getConfigElement(ServerConfigElement.CONFIG_ELEMENT_ID);
    }


    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        if (logger.isDebugEnabled())
            logger.debug("Processing request ("  + req.getMethod() + ") " + req.getRequestURL() + (req.getQueryString() != null ? "?" + req.getQueryString() : ""));
        
        res.setHeader("Cache-Control", "no-cache");
        res.setHeader("Pragma", "no-cache");
        
        WebScriptRuntime runtime = new WebScriptServletRuntime(registry, serviceRegistry, authenticator, req, res, serverConfig);
        runtime.executeScript();
    }
    
}
