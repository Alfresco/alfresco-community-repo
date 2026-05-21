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

import static java.util.Objects.requireNonNull;

import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.AccessTokenAuthorization;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.AuthorizationGrant;
import org.alfresco.repo.security.authentication.identityservice.user.OIDCUserInfo;

/**
 * {@link UserTokenProvider} that authenticates against the Identity Service on every call.
 *
 * <p>
 * Performs the OAuth2 Resource-Owner-Password-Credentials grant and runs Just-In-Time provisioning so the local Person record exists before the resulting {@link UserToken} is returned. This implementation has no caching of any kind; every invocation is a round-trip to the Authorization Server.
 * </p>
 */
public class DirectUserTokenProvider implements UserTokenProvider
{
    private final IdentityServiceFacade identityServiceFacade;
    private final IdentityServiceJITProvisioningHandler jitProvisioningHandler;

    public DirectUserTokenProvider(
            IdentityServiceFacade identityServiceFacade,
            IdentityServiceJITProvisioningHandler jitProvisioningHandler)
    {
        this.identityServiceFacade = requireNonNull(identityServiceFacade, "identityServiceFacade");
        this.jitProvisioningHandler = requireNonNull(jitProvisioningHandler, "jitProvisioningHandler");
    }

    @Override
    public UserToken getUserToken(UserTokenRequest request)
    {
        final AccessTokenAuthorization authorization = identityServiceFacade
                .authorize(AuthorizationGrant.password(request.username(), String.valueOf(request.password())));

        final String tokenValue = authorization.getAccessToken().getTokenValue();

        final String normalizedUsername = jitProvisioningHandler
                .extractUserInfoAndCreateUserIfNeeded(tokenValue)
                .map(OIDCUserInfo::username)
                .orElseThrow(() -> new AuthenticationException(
                        "Failed to extract username from token and user info endpoint."));

        return new UserToken(normalizedUsername, tokenValue);
    }
}
