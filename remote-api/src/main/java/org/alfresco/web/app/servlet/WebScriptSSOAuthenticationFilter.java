/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
package org.alfresco.web.app.servlet;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.web.filter.beans.DependencyInjectedFilter;
import org.alfresco.repo.webdav.auth.BaseAuthenticationFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.URLDecoder;
import org.springframework.extensions.webscripts.Description.RequiredAuthentication;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.RuntimeContainer;

/**
 * WebScript aware Authentication Filter Class. Takes into account the authentication setting in the descriptor for the
 * webscript before chaining to the downstream authentication filters. If authentication is not required then chains
 * with the NO_AUTH_REQUIRED request attribute set, which should cause any downstream authentication filter to bypass
 * authentication checks.
 * 
 * @author Kevin Roast
 * @author dward
 */
public class WebScriptSSOAuthenticationFilter extends BaseAuthenticationFilter implements DependencyInjectedFilter,
        ActivateableBean
{
    private static final Log logger = LogFactory.getLog(WebScriptSSOAuthenticationFilter.class);
    private RuntimeContainer container;        
    private boolean isActive = true;

    /**
     * @param container the container to set
     */
    public void setContainer(RuntimeContainer container)
    {
        this.container = container;
    }
   
    /**
     * Activates or deactivates the bean
     * 
     * @param active
     *            <code>true</code> if the bean is active and initialization should complete
     */
    public final void setActive(boolean active)
    {
        this.isActive = active;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.management.subsystems.ActivateableBean#isActive()
     */
    public final boolean isActive()
    {
        return isActive;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.webdav.auth.BaseNTLMAuthenticationFilter#doFilter(jakarta.servlet.ServletContext, jakarta.servlet.ServletRequest, jakarta.servlet.ServletResponse, jakarta.servlet.FilterChain)
     */
    public void doFilter(ServletContext context, ServletRequest sreq, ServletResponse sresp, FilterChain chain)
            throws IOException, ServletException
    {
        // Get the HTTP request/response
        HttpServletRequest req = (HttpServletRequest)sreq;
        
        // find a webscript match for the requested URI
        String requestURI = req.getRequestURI();
        String pathInfo = requestURI.substring((req.getContextPath() + req.getServletPath()).length());

        if (getLogger().isTraceEnabled())
        {
            getLogger().trace("Processing request: " + requestURI + " SID:" + (req.getSession(false) != null ? req.getSession().getId() : null));
        }
        
        Match match = container.getRegistry().findWebScript(req.getMethod(), URLDecoder.decode(pathInfo));
        if (match != null && match.getWebScript() != null)
        {
            // check the authentication required - if none then we don't want any of
            // the filters down the chain to require any authentication checks
            if (RequiredAuthentication.none == match.getWebScript().getDescription().getRequiredAuthentication())
            {
                if (getLogger().isDebugEnabled())
                {
                    getLogger().debug("Found webscript with no authentication - set NO_AUTH_REQUIRED flag.");
                }
                req.setAttribute(NO_AUTH_REQUIRED, Boolean.TRUE);
            }
        }

        chain.doFilter(sreq, sresp);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.webdav.auth.BaseAuthenticationFilter#getLogger()
     */
    @Override
    protected Log getLogger()
    {
        return logger;
    }
}
