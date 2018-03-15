/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.security.authentication.token;

import javax.servlet.http.HttpServletRequest;

import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.authentication.external.RemoteUserMapper;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.keycloak.adapters.BasicAuthRequestAuthenticator;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.spi.AuthOutcome;
import org.keycloak.representations.AccessToken;

/**
 * A {@link RemoteUserMapper} implementation that detects and validates JWTs.
 * 
 * @author Gavin Cornwell
 */
public class TokenRemoteUserMapper implements RemoteUserMapper, ActivateableBean
{
    private static Log logger = LogFactory.getLog(TokenRemoteUserMapper.class);
    
    /** Is the mapper enabled */
    private boolean isEnabled;
    
    /** Are token validation failures handled silently? */
    private boolean isValidationFailureSilent;

    /** The person service. */
    private PersonService personService;
    
    /** The Keycloak deployment object */
    private KeycloakDeployment keycloakDeployment;

    /**
     * Sets the active flag
     * 
     * @param isEnabled true to enable the subsystem
     */
    public void setActive(boolean isEnabled)
    {
        this.isEnabled = isEnabled;
    }

    /**
     * Determines whether token validation failures are silent
     * 
     * @param silent true to silently fail, false to throw an exception
     */
    public void setValidationFailureSilent(boolean silent)
    {
        this.isValidationFailureSilent = silent;
    }
    
    /**
     * Sets the person service.
     * 
     * @param personService
     *            the person service
     */
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    public void setKeycloakDeployment(KeycloakDeployment deployment)
    {
        this.keycloakDeployment = deployment;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.web.app.servlet.RemoteUserMapper#getRemoteUser(javax.servlet.http.HttpServletRequest)
     */
    public String getRemoteUser(HttpServletRequest request)
    {
        if (logger.isDebugEnabled())
            logger.debug("Retrieving username from http request...");
        
        if (!this.isEnabled)
        {
            if (logger.isDebugEnabled())
                logger.debug("TokenRemoteUserMapper is disabled, returning null.");
            
            return null;
        }
        
        String headerUserId = extractUserFromHeader(request);
        
        if (headerUserId != null)
        {
            // Normalize the user ID taking into account case sensitivity settings
            String normalizedUserId =  normalizeUserId(headerUserId);
            
            if (logger.isDebugEnabled())
                logger.debug("Returning username: " + normalizedUserId);
            
            return normalizedUserId;
        }
        
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.management.subsystems.ActivateableBean#isActive()
     */
    public boolean isActive()
    {
        return this.isEnabled;
    }
    
    /**
     * Extracts the user name from the JWT in the given request.
     * 
     * @param request The request containing the JWT
     * @return The user name or null if it can not be determined
     */
    private String extractUserFromHeader(HttpServletRequest request)
    {
        String userName = null;
        
        AlfrescoKeycloakHttpFacade facade = new AlfrescoKeycloakHttpFacade(request);
        
        // try authenticating with bearer token first
        if (logger.isDebugEnabled())
        {
            logger.debug("Trying bearer token...");
        }
    
        AlfrescoBearerTokenRequestAuthenticator tokenAuthenticator = 
                    new AlfrescoBearerTokenRequestAuthenticator(this.keycloakDeployment);
        AuthOutcome tokenOutcome = tokenAuthenticator.authenticate(facade);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Bearer token outcome: " + tokenOutcome);
        }
        
        if (tokenOutcome == AuthOutcome.FAILED && !isValidationFailureSilent)
        {
            throw new AuthenticationException("Token validation failed: " + 
                        tokenAuthenticator.getValidationFailureDescription());
        }
        
        if (tokenOutcome == AuthOutcome.AUTHENTICATED)
        {
            userName = extractUserFromToken(tokenAuthenticator.getToken());
        }
        else
        {
            // if bearer token failed, try basic auth, if enabled
            if (this.keycloakDeployment.isEnableBasicAuth())
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Trying basic auth...");
                }
                
                BasicAuthRequestAuthenticator basicAuthenticator = 
                            new BasicAuthRequestAuthenticator(this.keycloakDeployment);
                AuthOutcome basicOutcome = basicAuthenticator.authenticate(facade);
                
                if (logger.isDebugEnabled())
                {
                    logger.debug("Basic auth outcome: " + basicOutcome);
                }
                
                // if auth was successful, extract username and return
                if (basicOutcome == AuthOutcome.AUTHENTICATED)
                {
                    userName = extractUserFromToken(basicAuthenticator.getToken());
                }
            }
        }
        
        return userName;
    }
    
    private String extractUserFromToken(AccessToken jwt)
    {
        // retrieve the preferred_username claim
        String userName = jwt.getPreferredUsername();
        
        if (logger.isDebugEnabled())
            logger.debug("Extracted username: " + userName);
        
        return userName;
    }
    
    /**
     * Normalizes a user id, taking into account existing user accounts and case sensitivity settings.
     * 
     * @param userId
     *            the user id
     * @return the string
     */
    private String normalizeUserId(final String userId)
    {
        if (userId == null)
        {
            return null;
        }
        
        String normalized = AuthenticationUtil.runAs(new RunAsWork<String>()
        {
            public String doWork() throws Exception
            {
                return personService.getUserIdentifier(userId);
            }
        }, AuthenticationUtil.getSystemUserName());
        
        if (logger.isDebugEnabled())
            logger.debug("Normalized user name for '" + userId + "': " + normalized);
        
        return normalized == null ? userId : normalized;
    }
}
