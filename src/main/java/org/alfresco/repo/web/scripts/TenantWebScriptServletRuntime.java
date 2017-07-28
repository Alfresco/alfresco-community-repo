/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.repo.web.scripts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.extensions.config.ServerProperties;
import org.springframework.extensions.surf.util.URLDecoder;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.RuntimeContainer;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.ServletAuthenticatorFactory;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRuntime;


/**
 * HTTP Servlet Web Script Runtime which can handle a tenant id in a web script path
 * 
 * @author davidc
 */
public class TenantWebScriptServletRuntime extends WebScriptServletRuntime
{
    public TenantWebScriptServletRuntime(RuntimeContainer container, ServletAuthenticatorFactory authFactory, HttpServletRequest req, HttpServletResponse res, ServerProperties serverProperties)
    {
        super(container, authFactory, req, res, serverProperties);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRuntime#getScriptUrl()
     */
    @Override
    protected String getScriptUrl()
    {
        // NOTE: Don't use req.getPathInfo() - it truncates the path at first semi-colon in Tomcat
        final String requestURI = req.getRequestURI();
        final String serviceContextPath = req.getContextPath() + req.getServletPath();
        String pathInfo;
        
        if (serviceContextPath.length() > requestURI.length())
        {
            // NOTE: assume a redirect has taken place e.g. tomcat welcome-page
            // NOTE: this is unlikely, and we'll take the hit if the path contains a semi-colon
            pathInfo = req.getPathInfo();
        }
        else
        {
            pathInfo = URLDecoder.decode(requestURI.substring(serviceContextPath.length()));
        }
        
        // ensure tenant is specified at beginning of path
        // NOTE: must contain at least root / and single character for tenant name
        if (pathInfo.length() < 2)
        {
            throw new WebScriptException("Missing tenant name in path: " + pathInfo);
        }
        // remove tenant
        int idx = pathInfo.indexOf('/', 1);
        pathInfo = pathInfo.substring(idx == -1 ? pathInfo.length() : idx);
        return pathInfo;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRuntime#createRequest(org.alfresco.web.scripts.WebScriptMatch)
     */
    @Override
    protected WebScriptRequest createRequest(Match match)
    {
        // TODO: construct org.springframework.extensions.webscripts.servlet.WebScriptServletResponse when
        //       org.alfresco.web.scripts.WebScriptServletResponse (deprecated) is removed
        servletReq = new TenantWebScriptServletRequest(this, req, match, serverProperties);
        return servletReq;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptContainer#getName()
     */
    public String getName()
    {
        return "TenantServletRuntime";
    }

}
