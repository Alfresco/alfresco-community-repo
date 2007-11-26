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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.web.config.ServerConfigElement;
import org.alfresco.web.scripts.WebScriptDescription.RequiredAuthentication;


/**
 * HTTP Servlet Web Script Runtime
 * 
 * @author davidc
 */
public class WebScriptServletRuntime extends WebScriptRuntime
{
    protected HttpServletRequest req;
    protected HttpServletResponse res;
    protected WebScriptServletAuthenticator authenticator;
    protected ServerConfigElement serverConfig;
    

    /**
     * Construct
     * 
     * @param registry
     * @param serviceRegistry
     * @param authenticator
     * @param req
     * @param res
     */
    public WebScriptServletRuntime(WebScriptRegistry registry, ServiceRegistry serviceRegistry, WebScriptServletAuthenticator authenticator,
            HttpServletRequest req, HttpServletResponse res, ServerConfigElement serverConfig)
    {
        super(registry, serviceRegistry);
        this.req = req;
        this.res = res;
        this.authenticator = authenticator;
        this.serverConfig = serverConfig;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRuntime#getScriptMethod()
     */
    @Override
    protected String getScriptMethod()
    {
        // Is this an overloaded POST request?
        String method = req.getMethod();
        if (method.equalsIgnoreCase("post"))
        {
            boolean overloadParam = false;
            String overload = req.getHeader("X-HTTP-Method-Override");
            if (overload == null || overload.length() == 0)
            {
                overload = req.getParameter("alf_method");
                overloadParam = true;
            }
            if (overload != null && overload.length() > 0)
            {
                if (logger.isDebugEnabled())
                    logger.debug("POST is tunnelling method '" + overload + "' as specified by " + (overloadParam ? "alf_method parameter" : "X-HTTP-Method-Override header"));
                    
                method = overload;
            }
        }
        
        return method;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRuntime#getScriptUrl()
     */
    @Override
    protected String getScriptUrl()
    {
        // NOTE: Don't use req.getPathInfo() - it truncates the path at first semi-colon in Tomcat
        String requestURI = req.getRequestURI();
        String pathInfo = requestURI.substring((req.getContextPath() + req.getServletPath()).length());
        try
        {
            return URLDecoder.decode(pathInfo, "UTF-8");
        }
        catch(UnsupportedEncodingException e)
        {
            throw new WebScriptException("Failed to retrieve path info", e);
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRuntime#createRequest(org.alfresco.web.scripts.WebScriptMatch)
     */
    @Override
    protected WebScriptRequest createRequest(WebScriptMatch match)
    {
        return new WebScriptServletRequest(serverConfig, req, match);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRuntime#createResponse()
     */
    @Override
    protected WebScriptResponse createResponse()
    {
        return new WebScriptServletResponse(res);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRuntime#authenticate(org.alfresco.web.scripts.WebScriptDescription.RequiredAuthentication, boolean)
     */
    @Override
    protected boolean authenticate(RequiredAuthentication required, boolean isGuest, WebScriptRequest req, WebScriptResponse res)
    {
        boolean authorised = true;
        if (authenticator != null)
        {
            authorised = authenticator.authenticate(required, isGuest, (WebScriptServletRequest)req, (WebScriptServletResponse)res);
        }
        return authorised;
    }
}
