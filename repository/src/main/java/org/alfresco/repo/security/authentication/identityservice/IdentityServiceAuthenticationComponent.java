/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.security.authentication.AbstractAuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.IdentityServiceFacadeException;

/**
 * Authenticates a user against the Identity Service (Keycloak / OAuth2 Authorization Server).
 *
 * <p>
 * Delegates the actual credential-validation work to a {@link UserTokenProvider} so the component remains agnostic of how the token is obtained: a {@link DirectUserTokenProvider} hits the IdP on every call, while a {@link CachingUserTokenProvider} transparently caches previously validated tokens. If no provider is wired (e.g., {@code identity-service.authentication.enable-username-password-authentication=false}) this authenticator falls through to the next one in the chain by throwing.
 * </p>
 */
public class IdentityServiceAuthenticationComponent extends AbstractAuthenticationComponent implements ActivateableBean
{
    private static final Log LOG = LogFactory.getLog(IdentityServiceAuthenticationComponent.class);

    /** Provider of validated user tokens (direct-to-IdP or caching, opaque to this component). */
    private UserTokenProvider userTokenProvider;
    /** Enabled flag for the identity-service subsystem. */
    private boolean active;
    private boolean allowGuestLogin;

    public void setUserTokenProvider(UserTokenProvider userTokenProvider)
    {
        this.userTokenProvider = userTokenProvider;
    }

    public void setAllowGuestLogin(boolean allowGuestLogin)
    {
        this.allowGuestLogin = allowGuestLogin;
    }

    @Override
    public void authenticateImpl(String userName, char[] password) throws AuthenticationException
    {
        if (userTokenProvider == null)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("UserTokenProvider was not set, possibly due to the 'identity-service.authentication.enable-username-password-authentication=false' property.");
            }

            throw new AuthenticationException("User not authenticated because UserTokenProvider was not set.");
        }

        try
        {
            final UserToken token = userTokenProvider.getUserToken(userName, password);
            // Verification was successful so treat as authenticated user
            setCurrentUser(token.getNormalizedUsername());
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
