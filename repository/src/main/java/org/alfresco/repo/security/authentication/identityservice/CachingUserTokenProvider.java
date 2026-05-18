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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.IdentityServiceFacadeException;

/**
 * Decorator {@link UserTokenProvider} that adds local-JVM caching of previously validated user tokens.
 *
 * <p>
 * Wraps a delegate {@link UserTokenProvider} (typically {@link DirectUserTokenProvider}) and consults a {@link SimpleCache} before delegating. On a cache HIT the previously stored access token is re-validated locally via {@link IdentityServiceFacade#decodeToken(String)} (signature, {@code exp}, {@code iss}, optionally {@code azp} / {@code aud}); if validation succeeds the cached entry is returned without contacting the Identity Provider. On a MISS, an invalid cached entry, or any failure during local validation the call is delegated to the wrapped provider and the resulting {@link UserToken} is then cached.
 * </p>
 *
 * <p>
 * <b>Encapsulation:</b> all cache behaviour (key derivation, lookup, validity check, invalidation, population) lives entirely in this class. Consumers see only the {@link UserTokenProvider} contract; they never observe a cache, an entry, or a key. This makes it safe to swap caching on/off at the wiring layer without touching consumer code.
 * </p>
 *
 * <p>
 * <b>Storage scope:</b> the cached access token is a bearer credential and MUST NOT leave the JVM that obtained it. The backing {@link SimpleCache} is therefore expected to be configured as {@code cluster.type=local}. Cache keys are SHA-256 digests of {@code userName + 0x00 + password} encoded as base64url, so the password and user name never appear in cleartext in the cache.
 * </p>
 */
public class CachingUserTokenProvider implements UserTokenProvider
{
    private static final Log LOGGER = LogFactory.getLog(CachingUserTokenProvider.class);
    private static final String DIGEST_ALGORITHM = "SHA-256";

    private final UserTokenProvider delegate;
    private final SimpleCache<String, UserToken> backingCache;
    private final IdentityServiceFacade identityServiceFacade;

    /**
     * Latches the first decode-failure WARN so production triage is possible without DEBUG, while subsequent failures stay at DEBUG to avoid log flooding.
     */
    private final AtomicBoolean firstDecodeFailureLogged = new AtomicBoolean(false);

    public CachingUserTokenProvider(
            UserTokenProvider delegate,
            SimpleCache<String, UserToken> backingCache,
            IdentityServiceFacade identityServiceFacade)
    {
        this.delegate = requireNonNull(delegate, "delegate");
        this.backingCache = requireNonNull(backingCache, "backingCache");
        this.identityServiceFacade = requireNonNull(identityServiceFacade, "identityServiceFacade");

        if (LOGGER.isInfoEnabled())
        {
            LOGGER.info("Identity Service user-token cache enabled (local-JVM, " + DIGEST_ALGORITHM + " keys).");
        }
    }

    @Override
    public UserToken getUserToken(String userName, char[] password)
    {
        final String cacheKey = deriveCacheKey(userName, password);

        final UserToken cached = readCache(cacheKey);
        if (cached != null)
        {
            if (isCachedTokenStillValid(cached))
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("User-token cache HIT for user '" + userName
                            + "'. Skipping authorization request.");
                }
                return cached;
            }
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("User-token cache HIT for user '" + userName
                        + "' but the cached token is no longer valid. Invalidating and re-authorizing.");
            }
            evict(cacheKey);
        }

        final UserToken fresh = delegate.getUserToken(userName, password);
        writeCache(cacheKey, fresh);
        return fresh;
    }

    private UserToken readCache(String cacheKey)
    {
        try
        {
            return backingCache.get(cacheKey);
        }
        catch (RuntimeException ex)
        {
            LOGGER.warn("Failed to read user-token cache entry: " + ex.getMessage());
            return null;
        }
    }

    private void writeCache(String cacheKey, UserToken token)
    {
        try
        {
            backingCache.put(cacheKey, token);
        }
        catch (RuntimeException ex)
        {
            LOGGER.warn("Failed to store user-token cache entry: " + ex.getMessage());
        }
    }

    private void evict(String cacheKey)
    {
        try
        {
            backingCache.remove(cacheKey);
        }
        catch (RuntimeException ex)
        {
            LOGGER.warn("Failed to invalidate user-token cache entry: " + ex.getMessage());
        }
    }

    /**
     * Re-validate a cached access token locally via {@link IdentityServiceFacade#decodeToken(String)}: signature, expiration ({@code exp}), {@code nbf}, issuer, and any configured {@code azp}/{@code aud} checks. No Identity Provider round-trip is incurred (the JWK set is cached). Returns {@code false} on any decoding/validation failure so the caller can fall through to a fresh authorize.
     *
     * <p>
     * The first failure reason for a given (provider, JVM) is surfaced at WARN to make production triage possible without enabling DEBUG; expected later failures (e.g., natural {@code exp}) are kept at DEBUG so they don't flood the log.
     * </p>
     */
    private boolean isCachedTokenStillValid(UserToken token)
    {
        try
        {
            identityServiceFacade.decodeToken(token.getTokenString());
            return true;
        }
        catch (IdentityServiceFacadeException e)
        {
            if (firstDecodeFailureLogged.compareAndSet(false, true))
            {
                LOGGER.warn("Identity Service user-token cache: cached access token failed local re-validation. "
                        + "Subsequent identical failures will be logged at DEBUG. Cause: " + e.getMessage());
            }
            else if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Cached access token failed local re-validation: " + e.getMessage());
            }
            return false;
        }
    }

    /**
     * Derive the cache key as base64url(SHA-256(userName || 0x00 || password)). The single-byte separator prevents accidental collisions between e.g. ("ab", "cd") and ("a", "bcd"). The intermediate password byte buffer is wiped after use.
     */
    private static String deriveCacheKey(String userName, char[] password)
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
            throw new IllegalStateException("Failed to derive user-token cache key.", e);
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
