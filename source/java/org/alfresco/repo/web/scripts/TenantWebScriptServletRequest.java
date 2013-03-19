/**
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This file is part of the Spring Surf Extension project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
     * @param container  request generator
     * @param req
     * @param serviceMatch
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
