/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.web.app.servlet;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.extensions.surf.util.URLDecoder;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.RuntimeContainer;
import org.springframework.extensions.webscripts.Description.RequiredAuthentication;

/**
 * WebScript aware NTLM Authentication Filter Class.
 * 
 * Takes into account the authentication setting in the descriptor for the webscript.
 * If authentication is not required then simply chains. Otherwise will delegate
 * back to the usual web-client NTLM filter code path.
 * 
 * @author Kevin Roast
 */
public class WebScriptNTLMAuthenticationFilter extends NTLMAuthenticationFilter
{
    private RuntimeContainer container;        
    
    /**
     * @param container the container to set
     */
    public void setContainer(RuntimeContainer container)
    {
        this.container = container;
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.webdav.auth.BaseNTLMAuthenticationFilter#doFilter(javax.servlet.ServletContext, javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletContext context, ServletRequest sreq, ServletResponse sresp, FilterChain chain)
            throws IOException, ServletException
    {
        // Get the HTTP request/response
        HttpServletRequest req = (HttpServletRequest)sreq;
        
        // find a webscript match for the requested URI
        String requestURI = req.getRequestURI();
        String pathInfo = requestURI.substring((req.getContextPath() + req.getServletPath()).length());
        
        if (getLogger().isDebugEnabled())
            getLogger().debug("Processing request: " + requestURI + " SID:" +
                    (req.getSession(false) != null ? req.getSession().getId() : null));
        
        Match match = container.getRegistry().findWebScript(req.getMethod(), URLDecoder.decode(pathInfo));
        if (match != null && match.getWebScript() != null)
        {
            // check the authentication required - if none then we don't want any of
            // the filters down the chain to require any authentication checks
            if (RequiredAuthentication.none == match.getWebScript().getDescription().getRequiredAuthentication())
            {
                if (getLogger().isDebugEnabled())
                    getLogger().debug("Found webscript with no authentication - set NO_AUTH_REQUIRED flag.");
                req.setAttribute(AbstractAuthenticationFilter.NO_AUTH_REQUIRED, Boolean.TRUE);
            }
        }
        
        super.doFilter(context, sreq, sresp, chain);
    }
}
