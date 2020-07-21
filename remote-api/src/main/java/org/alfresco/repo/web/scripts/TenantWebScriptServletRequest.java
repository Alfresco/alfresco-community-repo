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

import org.springframework.extensions.config.ServerProperties;
import org.springframework.extensions.surf.util.URLDecoder;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Runtime;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;


/**
 * Web Script Request which can handle a tenant id in their servlet path
 * 
 * @author davidc
 */
public class TenantWebScriptServletRequest extends WebScriptServletRequest
{
    protected String tenant;
    protected String pathInfo;
    
    protected void parse()
    {
        String realPathInfo = getRealPathInfo();
        
        // remove tenant
        int idx = realPathInfo.indexOf('/', 1);
        tenant = realPathInfo.substring(1, idx == -1 ? realPathInfo.length() : idx);
        pathInfo = realPathInfo.substring(tenant.length() + 1);
    }

    /**
     * Construction
     *
     * @param container Runtime
     * @param req HttpServletRequest
     * @param serviceMatch Match
     * @param serverProperties ServerProperties
     */
    public TenantWebScriptServletRequest(Runtime container, HttpServletRequest req, Match serviceMatch, ServerProperties serverProperties)
    {
        super(container, req, serviceMatch, serverProperties);
        parse();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRequest#getServiceContextPath()
     */
    public String getServiceContextPath()
    {
        return getHttpServletRequest().getContextPath() + getHttpServletRequest().getServletPath() + "/" + tenant;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRequest#getPathInfo()
     */
    public String getPathInfo()
    {
        return pathInfo;
    }

    public String getTenant()
    {
        return tenant;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRequest#getPathInfo()
     */
    protected String getRealPathInfo()
    {
        // NOTE: Don't use req.getPathInfo() - it truncates the path at first semi-colon in Tomcat
        final String requestURI = getHttpServletRequest().getRequestURI();
        final String serviceContextPath = getHttpServletRequest().getContextPath() + getHttpServletRequest().getServletPath();
        String pathInfo;
        
        if (serviceContextPath.length() > requestURI.length())
        {
            // NOTE: assume a redirect has taken place e.g. tomcat welcome-page
            // NOTE: this is unlikely, and we'll take the hit if the path contains a semi-colon
            pathInfo = getHttpServletRequest().getPathInfo();
        }
        else
        {
            pathInfo = URLDecoder.decode(requestURI.substring(serviceContextPath.length()));
        }
        
        return pathInfo;
    }

}
