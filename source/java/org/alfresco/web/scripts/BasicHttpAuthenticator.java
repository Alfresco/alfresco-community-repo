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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.util.Base64;
import org.alfresco.web.scripts.WebScriptDescription.RequiredAuthentication;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * HTTP Basic Authentication Interceptor
 * 
 * @author davidc
 */
public class BasicHttpAuthenticator implements WebScriptServletAuthenticator
{
    // Logger
    private static final Log logger = LogFactory.getLog(BasicHttpAuthenticator.class);

    // dependencies
    private AuthenticationService authenticationService;
    
    /**
     * @param authenticationService
     */
    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }
    
    /* (non-Javadoc)
     * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
     */
    public void authenticate(RequiredAuthentication required, boolean isGuest, HttpServletRequest req, HttpServletResponse res)
    {
        boolean authorized = false;

        //
        // validate credentials
        // 

        String authorization = req.getHeader("Authorization");
        
        if (logger.isDebugEnabled())
        {
            logger.debug("HTTP Authorization provided: " + (authorization != null && authorization.length() > 0));
        }
        
        // authenticate as guest, if service allows
        if (isGuest)
        {
            if (logger.isDebugEnabled())
                logger.debug("Authenticating as Guest");

            authenticationService.authenticateAsGuest();
            authorized = true;
        }
        
        // authenticate as specified by HTTP Basic Authentication
        else if (authorization != null && authorization.length() > 0)
        {
            try
            {
                String[] authorizationParts = authorization.split(" ");
                if (!authorizationParts[0].equalsIgnoreCase("basic"))
                {
                    throw new WebScriptException("Authorization '" + authorizationParts[0] + "' not supported.");
                }
                String decodedAuthorisation = new String(Base64.decode(authorizationParts[1]));
                String[] parts = decodedAuthorisation.split(":");
                
                if (parts.length == 1)
                {
                    if (logger.isDebugEnabled())
                        logger.debug("Authenticating ticket " + parts[0]);

                    // assume a ticket has been passed
                    authenticationService.validate(parts[0]);
                    authorized = true;
                }
                else
                {
                    if (logger.isDebugEnabled())
                        logger.debug("Authenticating user " + parts[0]);

                    // assume username and password passed
                    if (parts[0].equals(AuthenticationUtil.getGuestUserName()))
                    {
                        if (required == RequiredAuthentication.guest)
                        {
                            authenticationService.authenticateAsGuest();
                            authorized = true;
                        }
                    }
                    else
                    {
                        authenticationService.authenticate(parts[0], parts[1].toCharArray());
                        authorized = true;
                    }
                }
            }
            catch(AuthenticationException e)
            {
                // failed authentication
            }
        }

        //
        // request credentials if not authorized
        //
        
        if (!authorized)
        {
            if (logger.isDebugEnabled())
                logger.debug("Requesting authorization credentials");
            
            res.setStatus(401);
            res.setHeader("WWW-Authenticate", "Basic realm=\"Alfresco\"");
        }
    }

}
