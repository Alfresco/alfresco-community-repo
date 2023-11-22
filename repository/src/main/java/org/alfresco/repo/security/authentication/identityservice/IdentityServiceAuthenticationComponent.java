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

import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.security.authentication.AbstractAuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.AuthorizationGrant;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.IdentityServiceFacadeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * Authenticates a user against Identity Service (Keycloak/Authorization Server).
 * {@link IdentityServiceFacade} is used to verify provided user credentials. User is set as the current user if the
 * user credentials are valid.
 * <br>
 * The {@link IdentityServiceAuthenticationComponent#identityServiceFacade} can be null in which case this authenticator
 * will just fall through to the next one in the chain.
 *
 */
public class IdentityServiceAuthenticationComponent extends AbstractAuthenticationComponent implements ActivateableBean
{
    private final Log LOGGER = LogFactory.getLog(IdentityServiceAuthenticationComponent.class);
    /** client used to authenticate user credentials against Authorization Server **/
    private IdentityServiceFacade identityServiceFacade;
    /** enabled flag for the identity service subsystem**/
    private boolean active;

    private IdentityServiceJITProvisioningHandler identityServiceJITProvisioningHandler;

    private boolean allowGuestLogin;

    public void setIdentityServiceFacade(IdentityServiceFacade identityServiceFacade)
    {
        this.identityServiceFacade = identityServiceFacade;
    }

    public void setAllowGuestLogin(boolean allowGuestLogin)
    {
        this.allowGuestLogin = allowGuestLogin;
    }

    public void setIdentityServiceJITProvisioning(IdentityServiceJITProvisioningHandler identityServiceJITProvisioningHandler)
    {
        this.identityServiceJITProvisioningHandler = identityServiceJITProvisioningHandler;
    }

    public void authenticateImpl(String userName, char[] password) throws AuthenticationException
    {
        if (identityServiceFacade == null)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("IdentityServiceFacade was not set, possibly due to the 'identity-service.authentication.enable-username-password-authentication=false' property.");
            }

            throw new AuthenticationException("User not authenticated because IdentityServiceFacade was not set.");
        }

        try
        {
            // Attempt to verify user credentials
            IdentityServiceFacade.AccessTokenAuthorization accessTokenAuthorization = identityServiceFacade.authorize(AuthorizationGrant.password(userName, new String(password)));

            String normalizedUsername = identityServiceJITProvisioningHandler.extractUserInfoAndCreateUserIfNeeded(accessTokenAuthorization.getAccessToken().getTokenValue())
                        .map(OIDCUserInfo::username)
                        .orElseThrow(() -> new AuthenticationException("Failed to extract username from token and user info endpoint."));
            // Verification was successful so treat as authenticated user
            setCurrentUser(normalizedUsername);
        }
        catch (IdentityServiceFacadeException e)
        {
            throw new AuthenticationException("Failed to verify user credentials against the OAuth2 Authorization Server.", e);
        }
        catch (RuntimeException e)
        {
            throw new AuthenticationException("Failed to verify user credentials.", e);
        }
    }

    public void setActive(boolean active)
    {
        this.active = active;
    }

    @Override
    public boolean isActive()
    {
        return active;
    }

    @Override
    protected boolean implementationAllowsGuestLogin()
    {
        return allowGuestLogin;
    }
}
