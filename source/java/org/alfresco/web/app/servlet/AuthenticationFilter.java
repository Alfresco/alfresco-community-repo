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
import javax.servlet.http.HttpServletResponse;

import org.springframework.extensions.config.ConfigService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.filter.beans.DependencyInjectedFilter;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.alfresco.web.config.ClientConfigElement;
import org.springframework.context.ApplicationEvent;

/**
 * @author Kevin Roast
 * 
 * Servlet filter responsible for redirecting to the login page for the Web Client if the user
 * does not have a valid ticket.
 * <p>
 * The current ticket is validated for each page request and the login page is shown if the
 * ticket has expired.
 * <p>
 * Note that this filter is only active when the system is running in a servlet container -
 * the AlfrescoFacesPortlet will be used for a JSR-168 Portal environment.
 */
public class AuthenticationFilter extends AbstractLifecycleBean implements DependencyInjectedFilter
{

    private String loginPage;
    private ConfigService configService;

    /**
     * @param configService
     *            the configService to set
     */
    public void setConfigService(ConfigService configService)
    {
        this.configService = configService;
    }
    
    /* (non-Javadoc)
     * @see org.springframework.extensions.surf.util.AbstractLifecycleBean#onBootstrap(org.springframework.context.ApplicationEvent)
     */
    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        if (this.loginPage == null)
        {
            ClientConfigElement clientConfig = (ClientConfigElement) this.configService.getGlobalConfig()
                    .getConfigElement(ClientConfigElement.CONFIG_ELEMENT_ID);
    
            if (clientConfig != null)
            {
                this.loginPage = clientConfig.getLoginPage();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.springframework.extensions.surf.util.AbstractLifecycleBean#onShutdown(org.springframework.context.ApplicationEvent)
     */
    @Override
    protected void onShutdown(ApplicationEvent event)
    {
    }

    public void doFilter(ServletContext context, ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException
    {
        HttpServletRequest httpReq = (HttpServletRequest) req;
        HttpServletResponse httpRes = (HttpServletResponse) res;

        // allow the login page to proceed
        if (!httpReq.getRequestURI().endsWith(this.loginPage))
        {
            AuthenticationStatus status = AuthenticationHelper.authenticate(context, httpReq, httpRes, false);

            if (status == AuthenticationStatus.Success || status == AuthenticationStatus.Guest)
            {
                // continue filter chaining
                chain.doFilter(req, res);
            }
            else
            {
                // authentication failed - so end servlet execution and redirect to login page
                // also save the requested URL so the login page knows where to redirect too later
                BaseServlet.redirectToLoginPage(httpReq, httpRes, context);
            }
        }
        else
        {
            // continue filter chaining
            chain.doFilter(req, res);
            AuthenticationUtil.clearCurrentSecurityContext();
        }
    }    
}
