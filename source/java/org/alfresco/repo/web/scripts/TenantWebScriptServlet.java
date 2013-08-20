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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.servlet.WebScriptServlet;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRuntime;


/**
 * Entry point for web scripts which can accept a tenant id in their servlet path
 * 
 * @author davidc
 */
public class TenantWebScriptServlet extends WebScriptServlet
{
//    public static final String SYSTEM_TENANT = "-system-";
//    public static final String DEFAULT_TENANT = "-default-";
    
    private static final long serialVersionUID = 2954663814419046489L;
    
    // Logger
    private static final Log logger = LogFactory.getLog(TenantWebScriptServlet.class);
    
    protected WebScriptServletRuntime getRuntime(HttpServletRequest req, HttpServletResponse res)
    {
        WebScriptServletRuntime runtime = new TenantWebScriptServletRuntime(container, authenticatorFactory, req, res, serverProperties);
        return runtime;
    }

    /* (non-Javadoc) 
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        if (logger.isDebugEnabled())
            logger.debug("Processing tenant request ("  + req.getMethod() + ") " + req.getRequestURL() + (req.getQueryString() != null ? "?" + req.getQueryString() : ""));
        
        if (req.getCharacterEncoding() == null)
        {
            req.setCharacterEncoding("UTF-8");
        }
        
        setLanguageFromRequestHeader(req);
        
        try
        {
        	WebScriptServletRuntime runtime = getRuntime(req, res);
            runtime.executeScript();
        }
        finally
        {
            // clear threadlocal
            I18NUtil.setLocale(null);
            // clear authentication and tenant context
            AuthenticationUtil.clearCurrentSecurityContext();
        }
    }
}
