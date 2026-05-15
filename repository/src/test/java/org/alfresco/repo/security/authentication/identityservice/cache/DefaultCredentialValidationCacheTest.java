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
    private static final String SAMPLE_TOKEN = "header.payload.signature";

    private SimpleCache<String, CredentialValidationCacheEntry> backing;

    @Before
    public void setUp()
    {
        backing = new MemoryCache<>();
    }

    @Test
    public void disabledCacheIsInert()
    {
        DefaultCredentialValidationCache cache = new DefaultCredentialValidationCache(backing, false);

        assertFalse(cache.isEnabled());

        cache.put("alice", "secret".toCharArray(),
                new CredentialValidationCacheEntry("alice", SAMPLE_TOKEN));
        assertEquals(Optional.empty(), cache.get("alice", "secret".toCharArray()));
        assertTrue("Disabled cache must not write to the backing store", backing.getKeys().isEmpty());
    }

    @Test
    public void nullBackingCacheFailsClosed()
    {
        DefaultCredentialValidationCache cache = new DefaultCredentialValidationCache(null, true);

        assertFalse(cache.isEnabled());

        cache.put("alice", "secret".toCharArray(),
                new CredentialValidationCacheEntry("alice", SAMPLE_TOKEN));
        assertEquals(Optional.empty(), cache.get("alice", "secret".toCharArray()));
    }

    @Test
    public void hitWhenSameCredentials()
    {
        DefaultCredentialValidationCache cache = new DefaultCredentialValidationCache(backing, true);

        cache.put("alice", "secret".toCharArray(),
                new CredentialValidationCacheEntry("alice", SAMPLE_TOKEN));

        Optional<CredentialValidationCacheEntry> hit = cache.get("alice", "secret".toCharArray());
        assertTrue("Expected cache HIT", hit.isPresent());
        assertEquals("alice", hit.get().getNormalizedUsername());
        assertEquals(SAMPLE_TOKEN, hit.get().getTokenString());
    }

    @Test
    public void missWhenPasswordChanges()
    {
        DefaultCredentialValidationCache cache = new DefaultCredentialValidationCache(backing, true);

        cache.put("alice", "secret-A".toCharArray(),
                new CredentialValidationCacheEntry("alice", SAMPLE_TOKEN));

        assertTrue(cache.get("alice", "secret-A".toCharArray()).isPresent());
        assertFalse(
                "Different password must miss because the digest-derived key is different",
                cache.get("alice", "secret-B".toCharArray()).isPresent());
    }

    @Test
    public void missWhenUserChanges()
    {
        DefaultCredentialValidationCache cache = new DefaultCredentialValidationCache(backing, true);

        cache.put("alice", "secret".toCharArray(),
                new CredentialValidationCacheEntry("alice", SAMPLE_TOKEN));

        assertFalse(cache.get("bob", "secret".toCharArray()).isPresent());
    }

    @Test
    public void invalidateRemovesEntry()
    {
        DefaultCredentialValidationCache cache = new DefaultCredentialValidationCache(backing, true);

        cache.put("alice", "secret".toCharArray(),
                new CredentialValidationCacheEntry("alice", SAMPLE_TOKEN));
        assertTrue(cache.get("alice", "secret".toCharArray()).isPresent());

        cache.invalidate("alice", "secret".toCharArray());
        assertFalse(cache.get("alice", "secret".toCharArray()).isPresent());
    }

    @Test
    public void putOverwritesPreviousEntryForSameCredentials()
    {
        DefaultCredentialValidationCache cache = new DefaultCredentialValidationCache(backing, true);

        cache.put("alice", "secret".toCharArray(),
                new CredentialValidationCacheEntry("alice", "first.token.value"));
        cache.put("alice", "secret".toCharArray(),
                new CredentialValidationCacheEntry("alice", "second.token.value"));

        Optional<CredentialValidationCacheEntry> hit = cache.get("alice", "secret".toCharArray());
        assertTrue(hit.isPresent());
        assertEquals("Latest put must replace the previous entry", "second.token.value", hit.get().getTokenString());
    }

    @Test
    public void backingCacheNeverContainsRawSecretsInKeys()
    {
        DefaultCredentialValidationCache cache = new DefaultCredentialValidationCache(backing, true);

        cache.put("alice", "very-secret-pwd".toCharArray(),
                new CredentialValidationCacheEntry("alice", SAMPLE_TOKEN));

        for (String key : backing.getKeys())
        {
            assertNotEquals("Cache key must not be the raw user name", "alice", key);
            assertFalse("Cache key must not contain the raw password", key.contains("very-secret-pwd"));
        }
    }

    @Test
    public void entryToStringDoesNotEchoToken()
    {
        CredentialValidationCacheEntry entry = new CredentialValidationCacheEntry("alice", "supersecret.jwt.payload");

        assertFalse(
                "Entry toString() must not echo the token value to keep accidental logging safe",
                entry.toString().contains("supersecret.jwt.payload"));
    }
}
