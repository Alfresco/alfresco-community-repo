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
package org.alfresco.repo.security.authentication.identityservice.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import org.alfresco.repo.cache.MemoryCache;
import org.alfresco.repo.cache.SimpleCache;

public class DefaultCredentialValidationCacheTest
{
    private static final String SHARED_CREDSEC = "test-shared-secret-not-real";
    private static final long CLOCK_SKEW_MS = 1_000L;
    private static final long MIN_TTL_MS = 1_000L;
    private static final long MAX_TTL_MS = 60_000L;

    private SimpleCache<String, CredentialValidationCacheEntry> backing;

    @Before
    public void setUp()
    {
        backing = new MemoryCache<>();
    }

    @Test
    public void disabledCacheIsInert()
    {
        DefaultCredentialValidationCache cache = new DefaultCredentialValidationCache(
                backing, false, SHARED_CREDSEC, CLOCK_SKEW_MS, MIN_TTL_MS, MAX_TTL_MS);

        assertFalse(cache.isEnabled());

        cache.put("alice", "secret".toCharArray(),
                new CredentialValidationCacheEntry("alice", System.currentTimeMillis() + 30_000L));
        assertEquals(Optional.empty(), cache.get("alice", "secret".toCharArray()));
        assertTrue(backing.getKeys().isEmpty());
    }

    @Test
    public void blankSharedSecretFailsClosed()
    {
        DefaultCredentialValidationCache cache = new DefaultCredentialValidationCache(
                backing, true, "   ", CLOCK_SKEW_MS, MIN_TTL_MS, MAX_TTL_MS);

        assertFalse(cache.isEnabled());

        cache.put("alice", "secret".toCharArray(),
                new CredentialValidationCacheEntry("alice", System.currentTimeMillis() + 30_000L));
        assertEquals(Optional.empty(), cache.get("alice", "secret".toCharArray()));
    }

    @Test
    public void hitWhenSameCredentialsBelowTtl()
    {
        DefaultCredentialValidationCache cache = new DefaultCredentialValidationCache(
                backing, true, SHARED_CREDSEC, CLOCK_SKEW_MS, MIN_TTL_MS, MAX_TTL_MS);

        long validUntil = System.currentTimeMillis() + 30_000L;
        cache.put("alice", "secret".toCharArray(), new CredentialValidationCacheEntry("alice", validUntil));

        Optional<CredentialValidationCacheEntry> hit = cache.get("alice", "secret".toCharArray());
        assertTrue("Expected cache HIT", hit.isPresent());
        assertEquals("alice", hit.get().getNormalizedUsername());
    }

    @Test
    public void missWhenPasswordChanges()
    {
        DefaultCredentialValidationCache cache = new DefaultCredentialValidationCache(
                backing, true, SHARED_CREDSEC, CLOCK_SKEW_MS, MIN_TTL_MS, MAX_TTL_MS);

        long validUntil = System.currentTimeMillis() + 30_000L;
        cache.put("alice", "secret-A".toCharArray(), new CredentialValidationCacheEntry("alice", validUntil));

        assertTrue(cache.get("alice", "secret-A".toCharArray()).isPresent());
        assertFalse(
                "Different password must miss because the HMAC-derived key is different",
                cache.get("alice", "secret-B".toCharArray()).isPresent());
    }

    @Test
    public void missWhenUserChanges()
    {
        DefaultCredentialValidationCache cache = new DefaultCredentialValidationCache(
                backing, true, SHARED_CREDSEC, CLOCK_SKEW_MS, MIN_TTL_MS, MAX_TTL_MS);

        long validUntil = System.currentTimeMillis() + 30_000L;
        cache.put("alice", "secret".toCharArray(), new CredentialValidationCacheEntry("alice", validUntil));

        assertFalse(cache.get("bob", "secret".toCharArray()).isPresent());
    }

    @Test
    public void expiredEntryIsEvictedOnRead()
    {
        DefaultCredentialValidationCache cache = new DefaultCredentialValidationCache(
                backing, true, SHARED_CREDSEC, CLOCK_SKEW_MS, MIN_TTL_MS, MAX_TTL_MS);

        long alreadyExpired = System.currentTimeMillis() - 1_000L;
        // Insert directly through the cache API; bound logic should reject it as too short
        cache.put("alice", "secret".toCharArray(), new CredentialValidationCacheEntry("alice", alreadyExpired));

        assertFalse(cache.get("alice", "secret".toCharArray()).isPresent());
        assertTrue("Expired entry must not remain in the backing cache", backing.getKeys().isEmpty());
    }

    @Test
    public void invalidateRemovesEntry()
    {
        DefaultCredentialValidationCache cache = new DefaultCredentialValidationCache(
                backing, true, SHARED_CREDSEC, CLOCK_SKEW_MS, MIN_TTL_MS, MAX_TTL_MS);

        long validUntil = System.currentTimeMillis() + 30_000L;
        cache.put("alice", "secret".toCharArray(), new CredentialValidationCacheEntry("alice", validUntil));
        assertTrue(cache.get("alice", "secret".toCharArray()).isPresent());

        cache.invalidate("alice", "secret".toCharArray());
        assertFalse(cache.get("alice", "secret".toCharArray()).isPresent());
    }

    @Test
    public void backingCacheNeverContainsRawSecrets()
    {
        DefaultCredentialValidationCache cache = new DefaultCredentialValidationCache(
                backing, true, SHARED_CREDSEC, CLOCK_SKEW_MS, MIN_TTL_MS, MAX_TTL_MS);

        long validUntil = System.currentTimeMillis() + 30_000L;
        cache.put("alice", "very-secret-pwd".toCharArray(),
                new CredentialValidationCacheEntry("alice", validUntil));

        for (String key : backing.getKeys())
        {
            assertNotEquals("Cache key must not be the raw user name", "alice", key);
            assertFalse("Cache key must not contain the raw password", key.contains("very-secret-pwd"));
        }
        for (String key : backing.getKeys())
        {
            CredentialValidationCacheEntry entry = backing.get(key);
            assertFalse("Cache value must not echo the password",
                    entry.toString().contains("very-secret-pwd"));
        }
    }

    @Test
    public void differentSharedSecretProducesDifferentKey()
    {
        DefaultCredentialValidationCache cacheA = new DefaultCredentialValidationCache(
                backing, true, "secret-A", CLOCK_SKEW_MS, MIN_TTL_MS, MAX_TTL_MS);

        SimpleCache<String, CredentialValidationCacheEntry> backingB = new MemoryCache<>();
        DefaultCredentialValidationCache cacheB = new DefaultCredentialValidationCache(
                backingB, true, "secret-B", CLOCK_SKEW_MS, MIN_TTL_MS, MAX_TTL_MS);

        long validUntil = System.currentTimeMillis() + 30_000L;
        cacheA.put("alice", "pwd".toCharArray(), new CredentialValidationCacheEntry("alice", validUntil));
        cacheB.put("alice", "pwd".toCharArray(), new CredentialValidationCacheEntry("alice", validUntil));

        String keyA = backing.getKeys().iterator().next();
        String keyB = backingB.getKeys().iterator().next();
        assertNotEquals("Different shared secrets must produce different keys", keyA, keyB);
    }

    @Test
    public void maxTtlClampsAggressiveExpiry()
    {
        DefaultCredentialValidationCache cache = new DefaultCredentialValidationCache(
                backing, true, SHARED_CREDSEC, CLOCK_SKEW_MS, MIN_TTL_MS, MAX_TTL_MS);

        long farFuture = System.currentTimeMillis() + (10L * MAX_TTL_MS);
        cache.put("alice", "secret".toCharArray(), new CredentialValidationCacheEntry("alice", farFuture));

        Optional<CredentialValidationCacheEntry> hit = cache.get("alice", "secret".toCharArray());
        assertTrue(hit.isPresent());
        long upperBound = System.currentTimeMillis() + MAX_TTL_MS + 1_000L;
        assertTrue(
                "Stored validUntil must be clamped by maxTtlMs",
                hit.get().getValidUntilEpochMillis() <= upperBound);
    }

    @Test
    public void ttlBelowMinIsRejected()
    {
        DefaultCredentialValidationCache cache = new DefaultCredentialValidationCache(
                backing, true, SHARED_CREDSEC, CLOCK_SKEW_MS, MIN_TTL_MS, MAX_TTL_MS);

        long shortTtl = System.currentTimeMillis() + (CLOCK_SKEW_MS + MIN_TTL_MS / 2);
        cache.put("alice", "secret".toCharArray(), new CredentialValidationCacheEntry("alice", shortTtl));

        assertFalse(
                "TTL below configured minimum should not be cached",
                cache.get("alice", "secret".toCharArray()).isPresent());
    }
}
