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
        catch (IllegalStateException e) 
        {
           if(e.getMessage().contains("getOutputStream() has already been called for this response"))
           {
               if(logger.isDebugEnabled())
               {
                   logger.warn("Client has cut off communication", e);
               }
               else
               {
                   logger.warn("Client has cut off communication");
               }
           }
           else
           {
               throw e;
           }
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
