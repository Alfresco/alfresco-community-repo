/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.security.authentication.identityservice;

import javax.servlet.http.HttpServletRequest;

import java.util.Optional;

import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.authentication.external.RemoteUserMapper;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.TokenException;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;

/**
 * A {@link RemoteUserMapper} implementation that detects and validates JWTs
 * issued by the Alfresco Identity Service.
 * 
 * @author Gavin Cornwell
 */
public class IdentityServiceRemoteUserMapper implements RemoteUserMapper, ActivateableBean
{
    private static final Log LOGGER = LogFactory.getLog(IdentityServiceRemoteUserMapper.class);
    
    /** Is the mapper enabled */
    private boolean isEnabled;
    
    /** Are token validation failures handled silently? */
    private boolean isValidationFailureSilent;

    /** The person service. */
    private PersonService personService;

    private BearerTokenResolver bearerTokenResolver;
    private IdentityServiceFacade identityServiceFacade;

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

    public void setBearerTokenResolver(BearerTokenResolver bearerTokenResolver)
    {
        this.bearerTokenResolver = bearerTokenResolver;
    }

    public void setIdentityServiceFacade(IdentityServiceFacade identityServiceFacade)
    {
        this.identityServiceFacade = identityServiceFacade;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.web.app.servlet.RemoteUserMapper#getRemoteUser(javax.servlet.http.HttpServletRequest)
     */
    @Override
    public String getRemoteUser(HttpServletRequest request)
    {
        LOGGER.trace("Retrieving username from http request...");

        if (!this.isEnabled)
        {
            LOGGER.debug("IdentityServiceRemoteUserMapper is disabled, returning null.");
            return null;
        }
        try
        {
            String headerUserId = extractUserFromHeader(request);

            if (headerUserId != null)
            {
                // Normalize the user ID taking into account case sensitivity settings
                String normalizedUserId =  normalizeUserId(headerUserId);
                LOGGER.trace("Returning userId: " + AuthenticationUtil.maskUsername(normalizedUserId));

                return normalizedUserId;
            }
        }
        catch (TokenException e)
        {
            if (!isValidationFailureSilent)
            {
                throw new AuthenticationException("Failed to extract username from token: " + e.getMessage(), e);
            }
            LOGGER.error("Failed to authenticate user using IdentityServiceRemoteUserMapper: " + e.getMessage(), e);
        }
        catch (RuntimeException e)
        {
            LOGGER.error("Failed to authenticate user using IdentityServiceRemoteUserMapper: " + e.getMessage(), e);
        }
        LOGGER.trace("Could not identify a userId. Returning null.");
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
        // try authenticating with bearer token first
        LOGGER.debug("Trying bearer token...");

        final String bearerToken;
        try
        {
            bearerToken = bearerTokenResolver.resolve(request);
        }
        catch (OAuth2AuthenticationException e)
        {
            LOGGER.debug("Failed to resolve Bearer token.", e);
            return null;
        }

        final Optional<String> possibleUsername = Optional.ofNullable(bearerToken)
                                                          .flatMap(identityServiceFacade::extractUsernameFromToken);
        if (possibleUsername.isEmpty())
        {
            LOGGER.debug("User could not be authenticated by IdentityServiceRemoteUserMapper.");
            return null;
        }

        String username = possibleUsername.get();
        LOGGER.trace("Extracted username: " + AuthenticationUtil.maskUsername(username));

        return username;
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
        
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Normalized user name for '" + AuthenticationUtil.maskUsername(userId) + "': " + AuthenticationUtil.maskUsername(normalized));
        }
        
        return normalized == null ? userId : normalized;
    }
}
