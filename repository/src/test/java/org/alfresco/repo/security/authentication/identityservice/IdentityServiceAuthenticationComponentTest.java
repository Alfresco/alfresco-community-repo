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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.ConnectException;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.alfresco.error.ExceptionStackUtil;
import org.alfresco.repo.cache.MemoryCache;
import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.AccessTokenAuthorization;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.AuthorizationException;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.AuthorizationGrant;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.TokenDecodingException;
import org.alfresco.repo.security.authentication.identityservice.cache.CredentialValidationCacheEntry;
import org.alfresco.repo.security.authentication.identityservice.cache.DefaultCredentialValidationCache;
import org.alfresco.repo.security.authentication.identityservice.user.OIDCUserInfo;
import org.alfresco.repo.security.sync.UserRegistrySynchronizer;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.BaseSpringTest;

public class IdentityServiceAuthenticationComponentTest extends BaseSpringTest
{
    private static final String CACHED_TOKEN = "cached.jwt.value";
    private static final String FRESH_TOKEN = "fresh.jwt.value";

    private final IdentityServiceAuthenticationComponent authComponent = new IdentityServiceAuthenticationComponent();

    @Autowired
    private AuthenticationContext authenticationContext;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserRegistrySynchronizer userRegistrySynchronizer;

    @Autowired
    private NodeService nodeService;

    @Autowired
    private PersonService personService;

    private IdentityServiceJITProvisioningHandler jitProvisioning;
    private IdentityServiceFacade mockIdentityServiceFacade;

    @Before
    public void setUp()
    {
        authComponent.setAuthenticationContext(authenticationContext);
        authComponent.setTransactionService(transactionService);
        authComponent.setUserRegistrySynchronizer(userRegistrySynchronizer);
        authComponent.setNodeService(nodeService);
        authComponent.setPersonService(personService);

        jitProvisioning = mock(IdentityServiceJITProvisioningHandler.class);
        mockIdentityServiceFacade = mock(IdentityServiceFacade.class);
        authComponent.setJitProvisioningHandler(jitProvisioning);
        authComponent.setIdentityServiceFacade(mockIdentityServiceFacade);
    }

    @After
    public void tearDown()
    {
        authenticationContext.clearCurrentSecurityContext();
    }

    @Test(expected = AuthenticationException.class)
    public void testAuthenticationFail()
    {
        final AuthorizationGrant grant = AuthorizationGrant.password("username", "password");

        doThrow(new AuthorizationException("Failed")).when(mockIdentityServiceFacade).authorize(grant);

        authComponent.authenticateImpl("username", "password".toCharArray());
    }

    @Test(expected = AuthenticationException.class)
    public void testAuthenticationFail_connectionException()
    {
        final AuthorizationGrant grant = AuthorizationGrant.password("username", "password");

        doThrow(new AuthorizationException("Couldn't connect to server", new ConnectException("ConnectionRefused")))
                .when(mockIdentityServiceFacade).authorize(grant);

        try
        {
            authComponent.authenticateImpl("username", "password".toCharArray());
        }
        catch (RuntimeException ex)
        {
            Throwable cause = ExceptionStackUtil.getCause(ex, ConnectException.class);
            assertNotNull(cause);
            throw ex;
        }
    }

    @Test(expected = AuthenticationException.class)
    public void testAuthenticationFail_otherException()
    {
        final AuthorizationGrant grant = AuthorizationGrant.password("username", "password");

        doThrow(new RuntimeException("Some other errors!"))
                .when(mockIdentityServiceFacade)
                .authorize(grant);

        authComponent.authenticateImpl("username", "password".toCharArray());
    }

    @Test
    public void testAuthenticationPass()
    {
        final AuthorizationGrant grant = AuthorizationGrant.password("username", "password");
        AccessTokenAuthorization authorization = mock(AccessTokenAuthorization.class);
        IdentityServiceFacade.AccessToken accessToken = mock(IdentityServiceFacade.AccessToken.class);

        when(authorization.getAccessToken()).thenReturn(accessToken);
        when(accessToken.getTokenValue()).thenReturn(FRESH_TOKEN);
        when(mockIdentityServiceFacade.authorize(grant)).thenReturn(authorization);
        when(jitProvisioning.extractUserInfoAndCreateUserIfNeeded(FRESH_TOKEN))
                .thenReturn(Optional.of(new OIDCUserInfo("username", "", "", "")));

        authComponent.authenticateImpl("username", "password".toCharArray());

        // Check that the authenticated user has been set
        assertEquals("User has not been set as expected.", "username", authenticationContext.getCurrentUserName());
    }

    @Test(expected = AuthenticationException.class)
    public void testFallthroughWhenIdentityServiceFacadeIsNull()
    {
        authComponent.setIdentityServiceFacade(null);
        authComponent.authenticateImpl("username", "password".toCharArray());
    }

    @Test
    public void testSettingAllowGuestUser()
    {
        authComponent.setAllowGuestLogin(true);
        assertTrue(authComponent.guestUserAuthenticationAllowed());

        authComponent.setAllowGuestLogin(false);
        assertFalse(authComponent.guestUserAuthenticationAllowed());
    }

    /**
     * On a cache HIT the cached token is re-validated locally via {@code decodeToken}; if validation succeeds, the Authorization Server must NOT be contacted.
     */
    @Test
    public void testCacheHitSkipsFacadeAuthorizationWhenTokenIsStillValid()
    {
        DefaultCredentialValidationCache cache = newCache();
        authComponent.setCredentialValidationCache(cache);

        cache.put("username", "password".toCharArray(),
                new CredentialValidationCacheEntry("username", CACHED_TOKEN));

        // decodeToken succeeds; only the absence of an exception matters
        when(mockIdentityServiceFacade.decodeToken(CACHED_TOKEN)).thenReturn(null);

        authComponent.authenticateImpl("username", "password".toCharArray());

        assertEquals("Cached principal must be set as current user",
                "username", authenticationContext.getCurrentUserName());
        verify(mockIdentityServiceFacade, times(1)).decodeToken(CACHED_TOKEN);
        verify(mockIdentityServiceFacade, never()).authorize(any());
    }

    /**
     * After a cache MISS triggers a successful authorize(), the resulting access token must be cached so a subsequent identical request can be served from the cache (verified by re-validating the cached token instead of calling authorize() again).
     */
    @Test
    public void testCacheMissPopulatesCacheAfterSuccessfulAuthorization()
    {
        DefaultCredentialValidationCache cache = newCache();
        authComponent.setCredentialValidationCache(cache);

        AuthorizationGrant grant = AuthorizationGrant.password("username", "password");
        AccessTokenAuthorization authorization = mock(AccessTokenAuthorization.class);
        IdentityServiceFacade.AccessToken accessToken = mock(IdentityServiceFacade.AccessToken.class);
        when(authorization.getAccessToken()).thenReturn(accessToken);
        when(accessToken.getTokenValue()).thenReturn(FRESH_TOKEN);
        when(mockIdentityServiceFacade.authorize(grant)).thenReturn(authorization);
        when(jitProvisioning.extractUserInfoAndCreateUserIfNeeded(FRESH_TOKEN))
                .thenReturn(Optional.of(new OIDCUserInfo("username", "", "", "")));
        when(mockIdentityServiceFacade.decodeToken(FRESH_TOKEN)).thenReturn(null);

        authComponent.authenticateImpl("username", "password".toCharArray());
        assertEquals("username", authenticationContext.getCurrentUserName());

        authenticationContext.clearCurrentSecurityContext();

        // Second call with the same credentials must be served from the cache
        authComponent.authenticateImpl("username", "password".toCharArray());
        assertEquals("username", authenticationContext.getCurrentUserName());

        verify(mockIdentityServiceFacade, times(1)).authorize(grant);
        verify(mockIdentityServiceFacade, times(1)).decodeToken(FRESH_TOKEN);
    }

    /**
     * If the cached access token can no longer be locally validated (e.g. expired or signing key rotated) the cache HIT must be discarded and the regular authorize/JIT path must run. Note that the successful re-authorization then repopulates the cache with a fresh entry, so post-call cache emptiness cannot be used to verify invalidation; we spy on the cache to assert {@code invalidate(...)} was called explicitly.
     */
    @Test
    public void testCacheHitFallsThroughWhenCachedTokenIsInvalid()
    {
        DefaultCredentialValidationCache cache = spy(newCache());
        authComponent.setCredentialValidationCache(cache);

        cache.put("username", "password".toCharArray(),
                new CredentialValidationCacheEntry("username", CACHED_TOKEN));

        // Simulate "token expired" or any other local validation failure
        doThrow(new TokenDecodingException("Token expired"))
                .when(mockIdentityServiceFacade).decodeToken(CACHED_TOKEN);

        AuthorizationGrant grant = AuthorizationGrant.password("username", "password");
        AccessTokenAuthorization authorization = mock(AccessTokenAuthorization.class);
        IdentityServiceFacade.AccessToken accessToken = mock(IdentityServiceFacade.AccessToken.class);
        when(authorization.getAccessToken()).thenReturn(accessToken);
        when(accessToken.getTokenValue()).thenReturn(FRESH_TOKEN);
        when(mockIdentityServiceFacade.authorize(grant)).thenReturn(authorization);
        when(jitProvisioning.extractUserInfoAndCreateUserIfNeeded(FRESH_TOKEN))
                .thenReturn(Optional.of(new OIDCUserInfo("username", "", "", "")));

        authComponent.authenticateImpl("username", "password".toCharArray());

        assertEquals("username", authenticationContext.getCurrentUserName());
        verify(mockIdentityServiceFacade, times(1)).authorize(grant);
        verify(jitProvisioning, times(1)).extractUserInfoAndCreateUserIfNeeded(FRESH_TOKEN);
        verify(cache, times(1)).invalidate(eq("username"), any(char[].class));
    }

    @Test(expected = AuthenticationException.class)
    public void testFailedAuthorizationDoesNotPopulateCache()
    {
        DefaultCredentialValidationCache cache = newCache();
        authComponent.setCredentialValidationCache(cache);

        AuthorizationGrant grant = AuthorizationGrant.password("username", "wrong-password");
        doThrow(new AuthorizationException("Failed")).when(mockIdentityServiceFacade).authorize(grant);

        try
        {
            authComponent.authenticateImpl("username", "wrong-password".toCharArray());
        }
        finally
        {
            assertFalse("A failed authorization must not populate the cache",
                    cache.get("username", "wrong-password".toCharArray()).isPresent());
        }
    }

    private static DefaultCredentialValidationCache newCache()
    {
        return new DefaultCredentialValidationCache(new MemoryCache<>(), true);
    }
}
