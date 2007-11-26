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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.scripts;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.web.app.servlet.AuthenticationHelper;
import org.alfresco.web.app.servlet.AuthenticationStatus;
import org.alfresco.web.app.servlet.BaseServlet;
import org.alfresco.web.scripts.WebScriptDescription.RequiredAuthentication;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.ServletContextAware;

/**
 * Alfresco Web Client Authentication Interceptor
 * 
 * @author davidc
 */
public class WebClientAuthenticator implements WebScriptServletAuthenticator, ServletContextAware
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
     * @see org.alfresco.web.scripts.WebScriptServletAuthenticator#authenticate(org.alfresco.web.scripts.WebScriptDescription.RequiredAuthentication, boolean, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public boolean authenticate(RequiredAuthentication required, boolean isGuest, WebScriptServletRequest preq, WebScriptServletResponse pres)
    {
        AuthenticationStatus status = null;

        try
        {
            //
            // validate credentials
            // 
            HttpServletRequest req = preq.getHttpServletRequest();
            HttpServletResponse res = pres.getHttpServletResponse();
            String ticket = req.getParameter("ticket");
            
            if (logger.isDebugEnabled())
            {
                logger.debug("Alfresco ticket provided: " + (ticket != null && ticket.length() > 0));
            }
        
            if (ticket != null && ticket.length() > 0)
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
                // authentication failed - now need to display the login page to the user, if asked to
                if (logger.isDebugEnabled())
                    logger.debug("Redirecting to Alfresco Login");
    
                BaseServlet.redirectToLoginPage(req, res, context);
            }
        }
        catch(IOException e)
        {
            throw new WebScriptException("Failed to authenticate", e);
        }
        
        return !(status == null || status == AuthenticationStatus.Failure);
    }
}
