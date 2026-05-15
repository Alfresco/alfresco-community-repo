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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.repo.cache.SimpleCache;

/**
 * Default {@link CredentialValidationCache} backed by an Alfresco {@link SimpleCache}.
 *
 * <p>
 * The cache key is a SHA-256 digest of {@code userName + 0x00 + password} encoded as base64url. As a result, neither the password nor the user name appear in the cache key in clear form, and a different password for the same user produces a different key, forcing re-validation against the Identity Service. Because the backing cache is required to be local to a single JVM, no cluster-shared salt or HMAC secret is necessary; SHA-256 is used to avoid review concerns associated with weaker digests while remaining inexpensive on the per-request hot path.
 * </p>
 *
 * <p>
 * Validity of cached entries is delegated to the caller: this class never inspects the token beyond storing it. Callers are expected to revalidate the token (signature, {@code exp}, etc.) on every HIT through the Identity Service facade. The cache region's {@code timeToLiveSeconds} acts only as a bound on heap usage.
 * </p>
 *
 * <p>
 * The cache fails closed: if it is disabled, or if the backing region is missing, lookups return {@link Optional#empty()} and writes are silently ignored. The component using this cache then falls back to the regular authorize-on-every-request behaviour.
 * </p>
 */
@SuppressWarnings("PMD.UseVarargs")
public class DefaultCredentialValidationCache implements CredentialValidationCache
{
    private static final Log LOGGER = LogFactory.getLog(DefaultCredentialValidationCache.class);
    private static final String DIGEST_ALGORITHM = "SHA-256";

    private final SimpleCache<String, CredentialValidationCacheEntry> backingCache;
    private final boolean enabled;

    public DefaultCredentialValidationCache(
            SimpleCache<String, CredentialValidationCacheEntry> backingCache,
            boolean enabled)
    {
        this.backingCache = backingCache;

        if (!enabled)
        {
            this.enabled = false;
            return;
        }

        if (backingCache == null)
        {
            LOGGER.warn("Identity Service credential validation cache is enabled but no backing cache is wired. "
                    + "The cache will be disabled.");
            this.enabled = false;
            return;
        }

        this.enabled = true;

        if (LOGGER.isInfoEnabled())
        {
            LOGGER.info("Identity Service credential validation cache enabled (local-JVM, " + DIGEST_ALGORITHM + " keys).");
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
            return Optional.ofNullable(backingCache.get(key));
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
        try
        {
            backingCache.put(deriveKey(userName, password), entry);
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
     * Derive the cache key as base64url(SHA-256(userName || 0x00 || password)). The single-byte separator prevents accidental collisions between e.g. ("ab", "cd") and ("a", "bcd"). The intermediate password byte buffer is wiped after use.
     */
    private static String deriveKey(String userName, char[] password)
    {
        byte[] passwordBytes = null;
        try
        {
            final MessageDigest digest = MessageDigest.getInstance(DIGEST_ALGORITHM);
            digest.update(safe(userName).getBytes(StandardCharsets.UTF_8));
            digest.update((byte) 0);
            passwordBytes = charsToBytes(password);
            digest.update(passwordBytes);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest.digest());
        }
        catch (NoSuchAlgorithmException e)
        {
            // SHA-256 is mandated by every JRE; surface as a runtime error so the caller treats
            // the lookup as a miss and bypasses the cache for this request.
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
