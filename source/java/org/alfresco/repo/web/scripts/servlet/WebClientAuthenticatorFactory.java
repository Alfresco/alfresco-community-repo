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
package org.alfresco.repo.web.scripts.servlet;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.web.app.servlet.AuthenticationHelper;
import org.alfresco.web.app.servlet.AuthenticationStatus;
import org.alfresco.web.app.servlet.BaseServlet;
import org.springframework.extensions.webscripts.Authenticator;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.Description.RequiredAuthentication;
import org.springframework.extensions.webscripts.servlet.ServletAuthenticatorFactory;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.ServletContextAware;


/**
 * Alfresco Web Client Authentication
 * 
 * @author davidc
 */
public class WebClientAuthenticatorFactory implements ServletAuthenticatorFactory, ServletContextAware
{
    // Logger
    private static final Log logger = LogFactory.getLog(WebClientAuthenticator.class);

    // dependencies
    private ServletContext context;


    /* (non-Javadoc)
     * @see org.springframework.web.context.ServletContextAware#setServletContext(javax.servlet.ServletContext)
     */
    public void setServletContext(ServletContext context)
    {
        this.context = context;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.servlet.ServletAuthenticatorFactory#create(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public Authenticator create(WebScriptServletRequest req, WebScriptServletResponse res)
    {
        return new WebClientAuthenticator(req, res);
    }


    /**
     * Alfresco Web Client Authentication
     * 
     * @author davidc
     */
    public class WebClientAuthenticator implements Authenticator
    {
        // dependencies
        private WebScriptServletRequest servletReq;
        private WebScriptServletResponse servletRes;
        
        private String ticket;
        
        /**
         * Construct
         * 
         * @param authenticationService
         * @param req
         * @param res
         */
        public WebClientAuthenticator(WebScriptServletRequest req, WebScriptServletResponse res)
        {
            this.servletReq = req;
            this.servletRes = res;
            
            this.ticket = req.getParameter("ticket");
        }
        
        /* (non-Javadoc)
         * @see org.alfresco.web.scripts.WebScriptServletAuthenticator#authenticate(org.alfresco.web.scripts.WebScriptDescription.RequiredAuthentication, boolean, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
         */
        public boolean authenticate(RequiredAuthentication required, boolean isGuest)
        {
            AuthenticationStatus status = null;
    
            try
            {
                //
                // validate credentials
                // 
                HttpServletRequest req = servletReq.getHttpServletRequest();
                HttpServletResponse res = servletRes.getHttpServletResponse();
                
                
                if (logger.isDebugEnabled())
                {
                    logger.debug("Alfresco ticket provided: " + (ticket != null && ticket.length() > 0));
                }
            
                if (! emptyCredentials())
                {
                    if (logger.isDebugEnabled())
                        logger.debug("Authenticating ticket " + ticket);
                    
                    status = AuthenticationHelper.authenticate(context, req, res, ticket);
                }
                else
                {
                    if (isGuest && RequiredAuthentication.guest == required)
                    {
                        if (logger.isDebugEnabled())
                            logger.debug("Authenticating as Guest");
                        
                        status = AuthenticationHelper.authenticate(context, req, res, true);
                    }
                    else
                    {
                        if (logger.isDebugEnabled())
                            logger.debug("Authenticating session");
                        
                        status = AuthenticationHelper.authenticate(context, req, res, false, false);
                    }
                }
        
                //
                // if not authorized, redirect to login page
                //
                if (status == null || status == AuthenticationStatus.Failure)
                {
                    // ALF-13194: The client has asserted itself as guest, but guest authentication is forbidden. Signal
                    // with a 401 response rather than the login page!
                    if (isGuest && RequiredAuthentication.guest == required)
                    {
                        res.setStatus(401);
                    }
                    else
                    {
                        // authentication failed - now need to display the login page to the user, if asked to
                        if (logger.isDebugEnabled())
                            logger.debug("Redirecting to Alfresco Login");
        
                        BaseServlet.redirectToLoginPage(req, res, context);
                    }
                }
            }
            catch(IOException e)
            {
                throw new WebScriptException("Failed to authenticate", e);
            }
            
            return !(status == null || status == AuthenticationStatus.Failure);
        }
        
        /* (non-Javadoc)
         * @see org.alfresco.web.scripts.Authenticator#emptyCredentials()
         */
        public boolean emptyCredentials()
        {
            return (ticket == null || ticket.length() == 0);
        }
    }

}