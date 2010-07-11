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

package org.alfresco.repo.webdav.auth;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.SessionUser;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * WebDAV Authentication Filter Class for SSO linke SiteMinder and IChains
 */
public class HTTPRequestAuthenticationFilter extends BaseAuthenticationFilter implements Filter
{
    // Debug logging

    private static Log logger = LogFactory.getLog(HTTPRequestAuthenticationFilter.class);

    // Servlet context

    private ServletContext m_context;

    private String httpServletRequestAuthHeaderName;
    
    private AuthenticationComponent m_authComponent;

    // By default match everything if this is not set
    private String m_authPatternString = null;

    private Pattern m_authPattern = null;

    /**
     * Initialize the filter
     * 
     * @param config
     *            FitlerConfig
     * @exception ServletException
     */
    public void init(FilterConfig config) throws ServletException
    {
        // Save the context

        m_context = config.getServletContext();

        // Setup the authentication context

        WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(m_context);

        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        setNodeService(serviceRegistry.getNodeService());
        setAuthenticationService(serviceRegistry.getAuthenticationService());
        setTransactionService(serviceRegistry.getTransactionService());
        setPersonService((PersonService) ctx.getBean("PersonService")); // transactional and permission-checked
        m_authComponent = (AuthenticationComponent) ctx.getBean("authenticationComponent");
        
        httpServletRequestAuthHeaderName = config.getInitParameter("httpServletRequestAuthHeaderName");
        if(httpServletRequestAuthHeaderName == null)
        {
            httpServletRequestAuthHeaderName = "x-user";
        }
        this.m_authPatternString = config.getInitParameter("authPatternString");
        if (this.m_authPatternString != null)
        {
            try
            {
                m_authPattern = Pattern.compile(this.m_authPatternString);
            }
            catch (PatternSyntaxException e)
            {
                logger.warn("Invalid pattern: " + this.m_authPatternString, e);
                m_authPattern = null;
            }
        }
        
    }

    /**
     * Run the authentication filter
     * 
     * @param req
     *            ServletRequest
     * @param resp
     *            ServletResponse
     * @param chain
     *            FilterChain
     * @exception ServletException
     * @exception IOException
     */
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException,
            ServletException
    {
        // Assume it's an HTTP request

        final HttpServletRequest httpReq = (HttpServletRequest) req;
        HttpServletResponse httpResp = (HttpServletResponse) resp;

        // Get the user details object from the session

        SessionUser user = (SessionUser) httpReq.getSession().getAttribute(AUTHENTICATION_USER);

        if (user == null)
        {
            // Check for the auth header

            String authHdr = httpReq.getHeader(httpServletRequestAuthHeaderName);
            if (logger.isDebugEnabled())
            {
                if (authHdr == null)
                {
                    logger.debug("Header not found: " + httpServletRequestAuthHeaderName);
                }
                else
                {
                    logger.debug("Header is <" + authHdr + ">");
                }
            }

            // Throw an error if we have an unknown authentication

            if ((authHdr != null) && (authHdr.length() > 0))
            {

                // Get the user

                final String userName;
                if (m_authPattern != null)
                {
                    Matcher matcher = m_authPattern.matcher(authHdr);
                    if (matcher.matches())
                    {
                        userName = matcher.group();
                        if ((userName == null) || (userName.length() < 1))
                        {
                            if (logger.isDebugEnabled())
                            {
                                logger.debug("Extracted null or empty user name from pattern " + m_authPatternString
                                        + " against " + authHdr);
                            }
                            reject(httpReq, httpResp);
                            return;
                        }
                    }
                    else
                    {
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("no pattern match for " + m_authPatternString + " against " + authHdr);
                        }
                        reject(httpReq, httpResp);
                        return;
                    }
                }
                else
                {
                    userName = authHdr;
                }

                if (logger.isDebugEnabled())
                {
                    logger.debug("User = " + userName);
                }

                // Get the authorization header

                user = transactionService.getRetryingTransactionHelper().doInTransaction(
                        new RetryingTransactionHelper.RetryingTransactionCallback<SessionUser>()
                        {

                            public SessionUser execute() throws Throwable
                            {
                                try
                                {
                                    // Authenticate the user

                                    m_authComponent.clearCurrentSecurityContext();
                                    m_authComponent.setCurrentUser(userName);

                                    return createUserEnvironment(httpReq.getSession(), userName, authenticationService
                                            .getCurrentTicket(), true);
                                }
                                catch (AuthenticationException ex)
                                {
                                    if (logger.isDebugEnabled())
                                    {
                                        logger.debug("Failed", ex);
                                    }
                                    return null;
                                    // Perhaps auto-creation/import is disabled
                                }
                            }
                        });

            }
            else
            {
                // Check if the request includes an authentication ticket

                String ticket = req.getParameter(ARG_TICKET);

                if (ticket != null && ticket.length() > 0)
                {
                    // Debug

                    if (logger.isDebugEnabled())
                        logger.debug("Logon via ticket from " + req.getRemoteHost() + " (" + req.getRemoteAddr() + ":"
                                + req.getRemotePort() + ")" + " ticket=" + ticket);

                    try
                    {
                        // Validate the ticket
                        authenticationService.validate(ticket);

                        // Need to create the User instance if not already available
                        user = createUserEnvironment(httpReq.getSession(), authenticationService.getCurrentUserName(),
                                ticket, true);
                    }
                    catch (AuthenticationException authErr)
                    {
                        // Clear the user object to signal authentication failure
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Failed", authErr);
                        }
                        user = null;
                    }
                }
            }

            // Check if the user is authenticated, if not then prompt again

            if (user == null)
            {
                // No user/ticket, force the client to prompt for logon details
                reject(httpReq, httpResp);
                return;
            }
        }

        // Chain other filters

        chain.doFilter(req, resp);
    }

    private void reject( HttpServletRequest httpReq, HttpServletResponse httpResp) throws IOException
    {
        httpResp.setHeader("WWW-Authenticate", "BASIC realm=\"Alfresco DAV Server\"");
        httpResp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpResp.flushBuffer();
        return;
    }
    
    /**
     * Cleanup filter resources
     */
    public void destroy()
    {
        // Nothing to do
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
