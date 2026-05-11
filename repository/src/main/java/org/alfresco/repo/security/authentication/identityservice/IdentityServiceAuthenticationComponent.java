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

import java.time.Instant;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.security.authentication.AbstractAuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.AuthorizationGrant;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.IdentityServiceFacadeException;
import org.alfresco.repo.security.authentication.identityservice.cache.CredentialValidationCache;
import org.alfresco.repo.security.authentication.identityservice.cache.CredentialValidationCacheEntry;
import org.alfresco.repo.security.authentication.identityservice.user.OIDCUserInfo;

/**
 *
 * Authenticates a user against Identity Service (Keycloak/Authorization Server). {@link IdentityServiceFacade} is used to verify provided user credentials. User is set as the current user if the user credentials are valid. <br>
 * The {@link IdentityServiceAuthenticationComponent#identityServiceFacade} can be null in which case this authenticator will just fall through to the next one in the chain.
 *
 */
public class IdentityServiceAuthenticationComponent extends AbstractAuthenticationComponent implements ActivateableBean
{
    private final Log LOGGER = LogFactory.getLog(IdentityServiceAuthenticationComponent.class);
    /** client used to authenticate user credentials against Authorization Server **/
    private IdentityServiceFacade identityServiceFacade;
    /** enabled flag for the identity service subsystem **/
    private boolean active;

    private IdentityServiceJITProvisioningHandler jitProvisioningHandler;

    private boolean allowGuestLogin;

    /**
     * Optional cache that records the outcome of a successful credential validation against the Identity Service so that subsequent identical credential presentations within the token-lifetime window can skip the round-trip to the Authorization Server. May be {@code null} when the cache is not wired or disabled.
     */
    private CredentialValidationCache credentialValidationCache;

    public void setIdentityServiceFacade(IdentityServiceFacade identityServiceFacade)
    {
        this.identityServiceFacade = identityServiceFacade;
    }

    public void setAllowGuestLogin(boolean allowGuestLogin)
    {
        this.allowGuestLogin = allowGuestLogin;
    }

    public void setJitProvisioningHandler(IdentityServiceJITProvisioningHandler jitProvisioningHandler)
    {
        this.jitProvisioningHandler = jitProvisioningHandler;
    }

    public void setCredentialValidationCache(CredentialValidationCache credentialValidationCache)
    {
        this.credentialValidationCache = credentialValidationCache;
    }

    @Override
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

        // Cache lookup: skip the round-trip to the Identity Provider when this exact credential
        // pair has been validated recently and the recorded validity window has not yet elapsed.
        // The cache stores neither the password nor any access/refresh token.
        final boolean cacheActive = credentialValidationCache != null && credentialValidationCache.isEnabled();
        if (cacheActive)
        {
            final Optional<CredentialValidationCacheEntry> cached = credentialValidationCache.get(userName, password);
            if (cached.isPresent())
            {
                final String cachedNormalizedUsername = cached.get().getNormalizedUsername();
                if (cachedPrincipalIsStillProvisioned(cachedNormalizedUsername))
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("Credential validation cache HIT for user '" + userName + "'. Skipping authorization request.");
                    }
                    setCurrentUser(cachedNormalizedUsername);
                    return;
                }
                // The cached principal no longer has a backing Person node, but JIT provisioning
                // is enabled and could (re-)create it. Invalidate the entry and fall through to
                // the regular authorize / JIT-provisioning path below so the local user state is
                // restored.
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Credential validation cache HIT for user '" + userName
                            + "' but local Person '" + cachedNormalizedUsername
                            + "' no longer exists. Invalidating entry and re-authorizing.");
                }
                credentialValidationCache.invalidate(userName, password);
            }
        }

        try
        {
            // Attempt to verify user credentials
            IdentityServiceFacade.AccessTokenAuthorization accessTokenAuthorization = identityServiceFacade.authorize(AuthorizationGrant.password(userName, String.valueOf(password)));

            String normalizedUsername = jitProvisioningHandler.extractUserInfoAndCreateUserIfNeeded(accessTokenAuthorization.getAccessToken().getTokenValue())
                    .map(OIDCUserInfo::username)
                    .orElseThrow(() -> new AuthenticationException("Failed to extract username from token and user info endpoint."));

            // Record the successful validation. Validity is bounded by the access-token expiration
            // returned by the Identity Provider so the cache view stays aligned with IdP configuration.
            if (cacheActive)
            {
                final Instant expiresAt = accessTokenAuthorization.getAccessToken().getExpiresAt();
                if (expiresAt != null)
                {
                    credentialValidationCache.put(userName, password,
                            new CredentialValidationCacheEntry(normalizedUsername, expiresAt.toEpochMilli()));
                }
            }

            // Verification was successful so treat as authenticated user
            setCurrentUser(normalizedUsername);
        }
        catch (IdentityServiceFacadeException e)
        {
            if (cacheActive)
            {
                credentialValidationCache.invalidate(userName, password);
            }
            throw new AuthenticationException("Failed to verify user credentials against the OAuth2 Authorization Server.", e);
        }
        catch (RuntimeException e)
        {
            if (cacheActive)
            {
                credentialValidationCache.invalidate(userName, password);
            }
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

    /**
     * Defensive check used on credential validation cache HITs so the shortcut never authenticates a principal whose backing Person node has been deleted (or was never created because the originating transaction was read-only). When JIT provisioning is enabled but the Person is missing, the cache HIT must be discarded so the regular authorize/JIT path can re-establish local user state.
     *
     * <p>
     * When JIT provisioning is disabled ({@code createMissingPeople == false}), or when no {@link org.alfresco.service.cmr.security.PersonService} is wired, behaviour is unchanged from the pre-cache flow: the existence check is skipped because a fresh authorize() would not create the Person either.
     * </p>
     */
    private boolean cachedPrincipalIsStillProvisioned(String normalizedUsername)
    {
        try
        {
            final org.alfresco.service.cmr.security.PersonService personService = getPersonService();
            if (personService == null || !personService.createMissingPeople())
            {
                return true;
            }
            return personService.personExists(normalizedUsername);
        }
        catch (RuntimeException ex)
        {
            // If the Person check itself fails (e.g. transient DB issue) honour the cache HIT
            // rather than penalising the authentication path. This is the same posture the rest
            // of the cache uses: degrade gracefully toward existing behaviour.
            LOGGER.warn("Failed to verify Person existence for cached credential validation; honouring cache HIT defensively. "
                    + ex.getMessage());
            return true;
        }
    }
}
