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

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.security.authentication.AbstractAuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.AccessTokenAuthorization;
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
    private static final Log LOG = LogFactory.getLog(IdentityServiceAuthenticationComponent.class);
    /** client used to authenticate user credentials against Authorization Server **/
    private IdentityServiceFacade identityServiceFacade;
    /** enabled flag for the identity service subsystem **/
    private boolean active;

    private IdentityServiceJITProvisioningHandler jitProvisioningHandler;

    private boolean allowGuestLogin;

    /**
     * Optional local-JVM cache that records the outcome of a successful credential validation against the Identity Service so that subsequent identical credential presentations can skip the round-trip to the Authorization Server until the previously issued access token can no longer be locally validated. May be {@code null} when the cache is not wired or disabled.
     */
    private CredentialValidationCache credentialValidationCache;

    /**
     * Latches the first decode-failure WARN so production triage is possible without DEBUG, while subsequent failures stay at DEBUG to avoid log flooding.
     */
    private final AtomicBoolean firstDecodeFailureLogged = new AtomicBoolean(false);

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
            if (LOG.isDebugEnabled())
            {
                LOG.debug("IdentityServiceFacade was not set, possibly due to the 'identity-service.authentication.enable-username-password-authentication=false' property.");
            }

            throw new AuthenticationException("User not authenticated because IdentityServiceFacade was not set.");
        }

        try
        {
            final CredentialValidationCacheEntry entry = getOrFetchUserToken(userName, password);
            // Verification was successful so treat as authenticated user
            setCurrentUser(entry.getNormalizedUsername());
        }
        catch (IdentityServiceFacadeException e)
        {
            invalidateCachedEntry(userName, password);
            throw new AuthenticationException("Failed to verify user credentials against the OAuth2 Authorization Server.", e);
        }
        catch (RuntimeException e)
        {
            invalidateCachedEntry(userName, password);
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
     * Look up a previously validated user token in the local cache, or obtain a fresh one from the Identity Service. A cache HIT is honoured only when (a) the cached access token still validates locally (signature + {@code exp} + {@code iss} via {@link IdentityServiceFacade#decodeToken(String)}) and (b) the cached principal is still backed by a local Person node. Otherwise the stale entry is invalidated and the regular authorize/JIT-provisioning path is used.
     */
    @SuppressWarnings("PMD.AvoidDeeplyNestedIfStmts")
    private CredentialValidationCacheEntry getOrFetchUserToken(String userName, char[] password)
    {
        final boolean cacheActive = credentialValidationCache != null && credentialValidationCache.isEnabled();
        if (!cacheActive)
        {
            return fetchUserTokenFromIdentityService(userName, password);
        }

        final Optional<CredentialValidationCacheEntry> cached = credentialValidationCache.get(userName, password);
        if (cached.isPresent())
        {
            final CredentialValidationCacheEntry entry = cached.get();
            if (isCachedTokenStillValid(entry))
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Credential validation cache HIT for user '" + userName
                            + "'. Skipping authorization request.");
                }
                return entry;
            }
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Credential validation cache HIT for user '" + userName
                        + "' but the cached entry is no longer usable. Invalidating and re-authorizing.");
            }
            credentialValidationCache.invalidate(userName, password);
        }

        final CredentialValidationCacheEntry fresh = fetchUserTokenFromIdentityService(userName, password);
        credentialValidationCache.put(userName, password, fresh);
        return fresh;
    }

    /**
     * Authorize the supplied credentials against the Identity Service and run JIT provisioning so a local Person record exists. Returns a cache entry containing the normalized principal name and the access-token string returned by the Authorization Server.
     */
    @SuppressWarnings("PMD.UseVarargs")
    private CredentialValidationCacheEntry fetchUserTokenFromIdentityService(String userName, char[] password)
    {
        final AccessTokenAuthorization accessTokenAuthorization = identityServiceFacade
                .authorize(AuthorizationGrant.password(userName, String.valueOf(password)));

        final String tokenValue = accessTokenAuthorization.getAccessToken().getTokenValue();

        final String normalizedUsername = jitProvisioningHandler
                .extractUserInfoAndCreateUserIfNeeded(tokenValue)
                .map(OIDCUserInfo::username)
                .orElseThrow(() -> new AuthenticationException(
                        "Failed to extract username from token and user info endpoint."));

        return new CredentialValidationCacheEntry(normalizedUsername, tokenValue);
    }

    /**
     * Re-validate a cached access token locally via {@link IdentityServiceFacade#decodeToken(String)}: signature, expiration ({@code exp}), {@code nbf}, issuer, and any configured {@code azp}/{@code aud} checks. No Identity Provider round-trip is incurred (the JWK set is cached). Returns {@code false} on any decoding/validation failure so the caller can fall through to a fresh authorize.
     *
     * <p>
     * The first failure reason for a given (component, JVM) is surfaced at WARN to make production triage possible without enabling DEBUG; expected later failures (e.g., natural {@code exp}) are kept at DEBUG so they don't flood the log. If decoding fails on every cached entry the operator sees a single INFO-style WARN that names the validator (issuer, audience, etc.) that rejects the token.
     * </p>
     */
    private boolean isCachedTokenStillValid(CredentialValidationCacheEntry entry)
    {
        try
        {
            identityServiceFacade.decodeToken(entry.getTokenString());
            return true;
        }
        catch (IdentityServiceFacadeException e)
        {
            if (firstDecodeFailureLogged.compareAndSet(false, true))
            {
                LOG.warn("Identity Service credential validation cache: cached access token failed local re-validation. "
                        + "Subsequent identical failures will be logged at DEBUG. Cause: " + e.getMessage());
            }
            else if (LOG.isDebugEnabled())
            {
                LOG.debug("Cached access token failed local re-validation: " + e.getMessage());
            }
            return false;
        }
    }

    @SuppressWarnings("PMD.UseVarargs")
    private void invalidateCachedEntry(String userName, char[] password)
    {
        if (credentialValidationCache != null && credentialValidationCache.isEnabled())
        {
            credentialValidationCache.invalidate(userName, password);
        }
    }

}
