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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.repo.cache.SimpleCache;

/**
 * Default {@link CredentialValidationCache} backed by an Alfresco {@link SimpleCache}.
 *
 * <p>
 * The cache key is an HMAC-SHA256 of {@code userName + 0x00 + password} computed with a configured cluster-shared secret. As a result, neither the password nor any access/refresh token is ever stored in the cache; only an opaque, salted hash of the credentials is used as the lookup key. A different password for the same user produces a different key and therefore a natural cache miss, forcing re-validation against the Identity Service.
 * </p>
 *
 * <p>
 * The cache fails closed: if it is disabled, or if the shared secret is missing, lookups return {@link Optional#empty()} and writes are silently ignored. The component using this cache will then fall back to the regular authorize-on-every-request behaviour.
 * </p>
 *
 * <p>
 * Per-entry validity is taken from the access-token expiration returned by the Identity Provider, optionally clamped by configured min/max bounds and reduced by a small clock-skew margin so the local view never trusts a credential past the IdP-issued lifetime.
 * </p>
 */
@SuppressWarnings("PMD.UseVarargs")
public class DefaultCredentialValidationCache implements CredentialValidationCache
{
    private static final Log LOGGER = LogFactory.getLog(DefaultCredentialValidationCache.class);
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final SimpleCache<String, CredentialValidationCacheEntry> backingCache;
    private final boolean enabled;
    private final byte[] sharedSecret;
    private final long clockSkewMillis;
    private final long minTtlMillis;
    private final long maxTtlMillis;

    public DefaultCredentialValidationCache(
            SimpleCache<String, CredentialValidationCacheEntry> backingCache,
            boolean enabled,
            String sharedSecret,
            long clockSkewMillis,
            long minTtlMillis,
            long maxTtlMillis)
    {
        this.backingCache = backingCache;
        this.clockSkewMillis = Math.max(0L, clockSkewMillis);
        this.minTtlMillis = Math.max(0L, minTtlMillis);
        this.maxTtlMillis = Math.max(this.minTtlMillis, maxTtlMillis);

        if (!enabled)
        {
            this.enabled = false;
            this.sharedSecret = null;
            return;
        }

        if (StringUtils.isBlank(sharedSecret))
        {
            LOGGER.warn("Identity Service credential validation cache is enabled but no shared secret is configured "
                    + "(identity-service.authentication.credentialValidationCache.sharedSecret). "
                    + "The cache will be disabled until a cluster-shared secret is configured.");
            this.enabled = false;
            this.sharedSecret = null;
            return;
        }

        if (backingCache == null)
        {
            LOGGER.warn("Identity Service credential validation cache is enabled but no backing cache is wired. "
                    + "The cache will be disabled.");
            this.enabled = false;
            this.sharedSecret = null;
            return;
        }

        this.enabled = true;
        this.sharedSecret = sharedSecret.getBytes(StandardCharsets.UTF_8);

        if (LOGGER.isInfoEnabled())
        {
            LOGGER.info("Identity Service credential validation cache enabled "
                    + "(clockSkewMs=" + this.clockSkewMillis
                    + ", minTtlMs=" + this.minTtlMillis
                    + ", maxTtlMs=" + this.maxTtlMillis + ").");
        }
    }

    @Override
    public boolean isEnabled()
    {
        return enabled;
    }

    @Override
    public Optional<CredentialValidationCacheEntry> get(String userName, char[] password)
    {
        if (!enabled)
        {
            return Optional.empty();
        }
        final String key;
        try
        {
            key = deriveKey(userName, password);
        }
        catch (RuntimeException ex)
        {
            LOGGER.warn("Failed to derive credential validation cache key on read: " + ex.getMessage());
            return Optional.empty();
        }

        try
        {
            final CredentialValidationCacheEntry entry = backingCache.get(key);
            if (entry == null)
            {
                return Optional.empty();
            }
            if (entry.isExpired(System.currentTimeMillis()))
            {
                // Defensive eviction so the local view stays aligned with token lifetime
                backingCache.remove(key);
                return Optional.empty();
            }
            return Optional.of(entry);
        }
        catch (RuntimeException ex)
        {
            LOGGER.warn("Failed to read credential validation cache entry: " + ex.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void put(String userName, char[] password, CredentialValidationCacheEntry entry)
    {
        if (!enabled || entry == null)
        {
            return;
        }
        final long now = System.currentTimeMillis();
        final long bounded = boundValidUntil(now, entry.getValidUntilEpochMillis());
        if (bounded <= now)
        {
            // Token TTL is below the configured minimum, do not cache
            return;
        }
        final CredentialValidationCacheEntry stored = (bounded == entry.getValidUntilEpochMillis())
                ? entry
                : new CredentialValidationCacheEntry(entry.getNormalizedUsername(), bounded);

        try
        {
            backingCache.put(deriveKey(userName, password), stored);
        }
        catch (RuntimeException ex)
        {
            LOGGER.warn("Failed to store credential validation cache entry: " + ex.getMessage());
        }
    }

    @Override
    public void invalidate(String userName, char[] password)
    {
        if (!enabled)
        {
            return;
        }
        try
        {
            backingCache.remove(deriveKey(userName, password));
        }
        catch (RuntimeException ex)
        {
            LOGGER.warn("Failed to invalidate credential validation cache entry: " + ex.getMessage());
        }
    }

    /**
     * Constrain the IdP-issued validity to a safe local window.
     */
    private long boundValidUntil(long now, long validUntil)
    {
        long ttl = validUntil - now - clockSkewMillis;
        if (ttl <= 0L)
        {
            return now;
        }
        if (ttl < minTtlMillis)
        {
            return now;
        }
        if (ttl > maxTtlMillis)
        {
            ttl = maxTtlMillis;
        }
        return now + ttl;
    }

    private String deriveKey(String userName, char[] password)
    {
        byte[] passwordBytes = null;
        try
        {
            final Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(sharedSecret, HMAC_ALGORITHM));
            mac.update(safe(userName).getBytes(StandardCharsets.UTF_8));
            mac.update((byte) 0);
            passwordBytes = charsToBytes(password);
            mac.update(passwordBytes);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal());
        }
        catch (Exception e)
        {
            // Refuse to fall back to a weaker keying scheme; surface as a runtime error
            // so the caller treats the lookup as a miss and bypasses the cache for this request.
            throw new IllegalStateException("Failed to derive credential validation cache key.", e);
        }
        finally
        {
            if (passwordBytes != null)
            {
                Arrays.fill(passwordBytes, (byte) 0);
            }
        }
    }

    private static String safe(String s)
    {
        return s == null ? "" : s;
    }

    private static byte[] charsToBytes(char[] chars)
    {
        if (chars == null)
        {
            return new byte[0];
        }
        final ByteBuffer buffer = StandardCharsets.UTF_8.encode(CharBuffer.wrap(chars));
        final byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        // Best-effort wipe of the intermediate buffer
        if (buffer.hasArray())
        {
            Arrays.fill(buffer.array(), buffer.arrayOffset(), buffer.arrayOffset() + buffer.capacity(), (byte) 0);
        }
        return bytes;
    }
}
