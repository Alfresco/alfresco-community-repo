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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import org.alfresco.repo.cache.MemoryCache;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.TokenDecodingException;

/**
 * Pure unit tests for {@link CachingUserTokenProvider}: cache hits, misses, invalidation triggered by failed local re-validation, and the property that the SHA-256 cache key is sensitive to both user name and password.
 */
public class CachingUserTokenProviderTest
{
    private static final String TEST_USER = "alice";
    private static final char[] TEST_PASS = "p@ssw0rd".toCharArray();
    private static final String TOKEN_A = "token.A";
    private static final String TOKEN_B = "token.B";

    private UserTokenProvider delegate;
    private MemoryCache<String, UserToken> backingCache;
    private IdentityServiceFacade facade;
    private CachingUserTokenProvider caching;

    @Before
    public void setUp()
    {
        delegate = mock(UserTokenProvider.class);
        backingCache = new MemoryCache<>();
        facade = mock(IdentityServiceFacade.class);
        caching = new CachingUserTokenProvider(delegate, backingCache, facade);
    }

    @Test
    public void firstCallDelegatesAndPopulatesCache()
    {
        when(delegate.getUserToken(eq(TEST_USER), any())).thenReturn(new UserToken(TEST_USER, TOKEN_A));

        UserToken result = caching.getUserToken(TEST_USER, TEST_PASS.clone());

        assertEquals(TEST_USER, result.getNormalizedUsername());
        assertEquals(TOKEN_A, result.getTokenString());
        verify(delegate, times(1)).getUserToken(eq(TEST_USER), any());
        verify(facade, never()).decodeToken(any());
        assertEquals("First call must populate exactly one cache entry", 1, backingCache.getKeys().size());
    }

    @Test
    public void secondCallServedFromCacheWhenLocalValidationSucceeds()
    {
        when(delegate.getUserToken(eq(TEST_USER), any())).thenReturn(new UserToken(TEST_USER, TOKEN_A));

        UserToken first = caching.getUserToken(TEST_USER, TEST_PASS.clone());
        UserToken second = caching.getUserToken(TEST_USER, TEST_PASS.clone());

        assertSame("HIT must return the same cached UserToken instance", first, second);
        verify(delegate, times(1)).getUserToken(eq(TEST_USER), any());
        verify(facade, times(1)).decodeToken(TOKEN_A);
    }

    @Test
    public void cachedTokenInvalidatedWhenLocalValidationFails()
    {
        when(delegate.getUserToken(eq(TEST_USER), any()))
                .thenReturn(new UserToken(TEST_USER, TOKEN_A))
                .thenReturn(new UserToken(TEST_USER, TOKEN_B));

        caching.getUserToken(TEST_USER, TEST_PASS.clone());
        // Stale -> the next decodeToken call rejects the cached token
        doThrow(new TokenDecodingException("expired"))
                .when(facade).decodeToken(TOKEN_A);

        UserToken refreshed = caching.getUserToken(TEST_USER, TEST_PASS.clone());

        assertEquals("Stale entry must be replaced with a freshly issued token",
                TOKEN_B, refreshed.getTokenString());
        verify(delegate, times(2)).getUserToken(eq(TEST_USER), any());
        verify(facade, times(1)).decodeToken(TOKEN_A);
    }

    @Test
    public void differentPasswordsProduceDifferentCacheKeys()
    {
        when(delegate.getUserToken(eq(TEST_USER), any())).thenReturn(new UserToken(TEST_USER, TOKEN_A));

        caching.getUserToken(TEST_USER, "one".toCharArray());
        caching.getUserToken(TEST_USER, "two".toCharArray());

        Collection<String> keys = backingCache.getKeys();
        assertEquals("Different passwords for the same user must derive different cache keys",
                2, keys.size());
        for (String key : keys)
        {
            assertFalse("Cache key must not echo the raw password", key.contains("one"));
            assertFalse("Cache key must not echo the raw password", key.contains("two"));
            assertFalse("Cache key must not echo the raw user name", key.contains(TEST_USER));
        }
    }

    @Test
    public void differentUsersProduceDifferentCacheKeys()
    {
        when(delegate.getUserToken(any(), any())).thenReturn(new UserToken("u", TOKEN_A));

        caching.getUserToken("alice", TEST_PASS.clone());
        caching.getUserToken("bob", TEST_PASS.clone());

        assertEquals("Different users with the same password must derive different cache keys",
                2, backingCache.getKeys().size());
    }

    @Test
    public void delegateFailureDoesNotPopulateCache()
    {
        doThrow(new RuntimeException("IdP unavailable"))
                .when(delegate).getUserToken(eq(TEST_USER), any());

        assertThrows(RuntimeException.class, () -> caching.getUserToken(TEST_USER, TEST_PASS.clone()));
        assertTrue("Failed authorize must not populate the cache",
                backingCache.getKeys().isEmpty());
    }

    @Test
    public void constructorRejectsNullDelegate()
    {
        assertThrows(NullPointerException.class,
                () -> new CachingUserTokenProvider(null, backingCache, facade));
    }

    @Test
    public void constructorRejectsNullBackingCache()
    {
        assertThrows(NullPointerException.class,
                () -> new CachingUserTokenProvider(delegate, null, facade));
    }

    @Test
    public void constructorRejectsNullFacade()
    {
        assertThrows(NullPointerException.class,
                () -> new CachingUserTokenProvider(delegate, backingCache, null));
    }

    @Test
    public void cacheKeyIsStableAcrossCallsForSameCredentials()
    {
        when(delegate.getUserToken(eq(TEST_USER), any())).thenReturn(new UserToken(TEST_USER, TOKEN_A));

        caching.getUserToken(TEST_USER, TEST_PASS.clone());
        caching.getUserToken(TEST_USER, TEST_PASS.clone());

        assertEquals("Identical (user, password) pairs must collapse to a single cache key",
                1, backingCache.getKeys().size());
    }

    @Test
    public void userTokenToStringDoesNotEchoToken()
    {
        UserToken token = new UserToken(TEST_USER, TOKEN_A);
        assertFalse("toString() must not leak the bearer token", token.toString().contains(TOKEN_A));
    }

    @Test
    public void differentTokensAreNotEqual()
    {
        UserToken a = new UserToken(TEST_USER, TOKEN_A);
        UserToken b = new UserToken(TEST_USER, TOKEN_B);
        assertNotEquals("Different tokens must not be equal", a, b);
    }
}
