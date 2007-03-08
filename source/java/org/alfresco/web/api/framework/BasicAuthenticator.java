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
package org.alfresco.web.api.framework;

import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.util.Base64;
import org.alfresco.web.api.framework.APIDescription.RequiredAuthentication;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * HTTP Basic Authentication Interceptor
 * 
 * @author davidc
 */
public class BasicAuthenticator implements MethodInterceptor
{
    // Logger
    private static final Log logger = LogFactory.getLog(BasicAuthenticator.class);

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
    public Object invoke(MethodInvocation invocation)
        throws Throwable
    {
        boolean authorized = false;
        String currentUser = null;
        Object retVal = null;
        Object[] args = invocation.getArguments();
        APIRequest request = (APIRequest)args[0];
        APIService service = (APIService)invocation.getThis();
        APIDescription description = service.getDescription();

        try
        {
            //
            // Determine if user already authenticated
            //
            
            currentUser = AuthenticationUtil.getCurrentUserName();
            if (logger.isDebugEnabled())
                logger.debug("Current authentication: " + (currentUser == null ? "unauthenticated" : "authenticated as " + currentUser));
            
            //
            // validate credentials
            // 

            boolean isGuest = request.isGuest();
            String authorization = request.getHeader("Authorization");
            
            if (logger.isDebugEnabled())
            {
                logger.debug("Service authentication required: " + description.getRequiredAuthentication());
                logger.debug("Guest login: " + isGuest);
                logger.debug("Authorization provided (overrides Guest login): " + (authorization != null && authorization.length() > 0));
            }
            
            // authenticate as guest, if service allows
            if (((authorization == null || authorization.length() == 0) || isGuest)
                    && description.getRequiredAuthentication().equals(RequiredAuthentication.guest))
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
                        throw new APIException("Authorization '" + authorizationParts[0] + "' not supported.");
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
                            if (description.getRequiredAuthentication().equals(RequiredAuthentication.guest))
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
            // execute API service or request authorization
            //
            
            if (authorized)
            {
                retVal = invocation.proceed();
            }
            else
            {
                if (logger.isDebugEnabled())
                    logger.debug("Requesting authorization credentials");
                
                APIResponse response = (APIResponse)args[1];
                response.setStatus(401);
                response.setHeader("WWW-Authenticate", "Basic realm=\"Alfresco\"");
            }
        }
        finally
        {
            // reset authentication
            if (authorized)
            {
                authenticationService.clearCurrentSecurityContext();
                if (currentUser != null)
                {
                    AuthenticationUtil.setCurrentUser(currentUser);
                }
                
                if (logger.isDebugEnabled())
                    logger.debug("Authentication reset: " + (currentUser == null ? "unauthenticated" : "authenticated as " + currentUser));
            }
        }

        return retVal;
    }

}
