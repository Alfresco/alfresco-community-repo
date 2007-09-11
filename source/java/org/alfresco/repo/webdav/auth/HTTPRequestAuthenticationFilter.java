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
 * http://www.alfresco.com/legal/licensing
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
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * WebDAV Authentication Filter Class for SSO linke SiteMinder and IChains
 */
public class HTTPRequestAuthenticationFilter implements Filter
{
    // Debug logging

    private static Log logger = LogFactory.getLog(HTTPRequestAuthenticationFilter.class);

    // Authenticated user session object name

    public final static String AUTHENTICATION_USER = "_alfDAVAuthTicket";

    // Allow an authenitcation ticket to be passed as part of a request to bypass authentication

    private static final String ARG_TICKET = "ticket";

    // Servlet context

    private ServletContext m_context;

    // Various services required by NTLM authenticator

    private AuthenticationService m_authService;

    private PersonService m_personService;

    private NodeService m_nodeService;

    private TransactionService m_transactionService;

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
        m_nodeService = serviceRegistry.getNodeService();
        m_authService = serviceRegistry.getAuthenticationService();
        m_transactionService = serviceRegistry.getTransactionService();
        m_personService = (PersonService) ctx.getBean("PersonService"); // transactional and permission-checked
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

        HttpServletRequest httpReq = (HttpServletRequest) req;
        HttpServletResponse httpResp = (HttpServletResponse) resp;

        // Get the user details object from the session

        WebDAVUser user = (WebDAVUser) httpReq.getSession().getAttribute(AUTHENTICATION_USER);

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

            if ((authHdr != null) || (authHdr.length() > 0))
            {

                // Get the user

                String userName = "";
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
                                logger.debug("Extracted null or empty user name from pattern "
                                        + m_authPatternString + " against " + authHdr);
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

                UserTransaction tx = null;
                try
                {
                    tx = m_transactionService.getUserTransaction();
                    tx.begin();
                    // Authenticate the user

                    m_authComponent.setCurrentUser(userName);

                    // Get the user node and home folder

                    NodeRef personNodeRef = m_personService.getPerson(userName);
                    NodeRef homeSpaceRef = (NodeRef) m_nodeService.getProperty(personNodeRef,
                            ContentModel.PROP_HOMEFOLDER);

                    // Setup User object and Home space ID etc.

                    user = new WebDAVUser(userName, m_authService.getCurrentTicket(), homeSpaceRef);

                    tx.commit();
                    tx = null;
                    
                    httpReq.getSession().setAttribute(AUTHENTICATION_USER, user);
                }
                catch (AuthenticationException ex)
                {
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("Failed", ex);
                    }
                    user = null;
                    // Do nothing, user object will be null
                }
                catch (NoSuchPersonException e)
                {
                    // Do nothing, user object will be null
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("Failed", e);
                    }
                    user = null;
                }
                catch (Exception e)
                {
                    // Do nothing, user object will be null
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("Failed", e);
                    }
                    user = null;
                }
                finally
                {
                    try
                    {
                        if (tx != null)
                        {
                            tx.rollback();
                        }
                    }
                    catch (Exception tex)
                    {
                    }
                }
            }
            else
            {
                // Check if the request includes an authentication ticket

                String ticket = req.getParameter(ARG_TICKET);

                if (ticket != null && ticket.length() > 0)
                {
                    // Debug

                    if (logger.isDebugEnabled())
                        logger.debug("Logon via ticket from "
                                + req.getRemoteHost() + " (" + req.getRemoteAddr() + ":" + req.getRemotePort() + ")"
                                + " ticket=" + ticket);

                    UserTransaction tx = null;
                    try
                    {
                        // Validate the ticket

                        m_authService.validate(ticket);

                        // Need to create the User instance if not already available

                        String currentUsername = m_authService.getCurrentUserName();

                        // Start a transaction

                        tx = m_transactionService.getUserTransaction();
                        tx.begin();

                        NodeRef personRef = m_personService.getPerson(currentUsername);
                        user = new WebDAVUser(currentUsername, m_authService.getCurrentTicket(), personRef);
                        NodeRef homeRef = (NodeRef) m_nodeService.getProperty(personRef, ContentModel.PROP_HOMEFOLDER);

                        // Check that the home space node exists - else Login cannot proceed

                        if (m_nodeService.exists(homeRef) == false)
                        {
                            throw new InvalidNodeRefException(homeRef);
                        }
                        user.setHomeNode(homeRef);

                        tx.commit();
                        tx = null;

                        // Store the User object in the Session - the authentication servlet will then proceed

                        httpReq.getSession().setAttribute(AUTHENTICATION_USER, user);
                    }
                    catch (AuthenticationException authErr)
                    {
                        // Clear the user object to signal authentication failure
                        if(logger.isDebugEnabled())
                        {
                            logger.debug("Failed", authErr);
                        }
                        user = null;
                    }
                    catch (Throwable e)
                    {
                        // Clear the user object to signal authentication failure
                        if(logger.isDebugEnabled())
                        {
                            logger.debug("Failed", e);
                        }
                        user = null;
                    }
                    finally
                    {
                        try
                        {
                            if (tx != null)
                            {
                                tx.rollback();
                            }
                        }
                        catch (Exception tex)
                        {
                        }
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
        else
        {
            // Setup the authentication context

            m_authService.validate(user.getTicket());
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
}
