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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * <b>Storage scope:</b> the cached access token is a bearer credential and MUST NOT leave the JVM that obtained it. The backing {@link SimpleCache} is therefore expected to be configured as {@code cluster.type=local}. Cache keys are SHA-256 digests of {@code username + 0x00 + password} encoded as base64url, so the password and user name never appear in cleartext in the cache.
 * </p>
 */
@SuppressWarnings("PMD.GuardLogStatement")
public class CachingUserTokenProvider implements UserTokenProvider
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CachingUserTokenProvider.class);
    private static final String DIGEST_ALGORITHM = "SHA-256";

    private final UserTokenProvider delegate;
    private final SimpleCache<String, UserToken> backingCache;
    private final IdentityServiceFacade identityServiceFacade;

    public CachingUserTokenProvider(
            UserTokenProvider delegate,
            SimpleCache<String, UserToken> backingCache,
            IdentityServiceFacade identityServiceFacade)
    {
        this.delegate = requireNonNull(delegate, "delegate");
        this.backingCache = requireNonNull(backingCache, "backingCache");
        this.identityServiceFacade = requireNonNull(identityServiceFacade, "identityServiceFacade");

        LOGGER.info("Identity Service user-token cache enabled (local-JVM, {} keys).", DIGEST_ALGORITHM);
    }

    @Override
    @SuppressWarnings("PMD.AvoidDeeplyNestedIfStmts")
    public UserToken getUserToken(UserTokenRequest request)
    {
        final String cacheKey = deriveCacheKey(request);

        final UserToken cached = readCache(cacheKey);
        if (cached != null)
        {
            if (isCachedTokenStillValid(cached))
            {
                LOGGER.debug("User-token cache HIT for user '{}'. Skipping authorization request.", request.username());
                return cached;
            }
            LOGGER.debug("User-token cache HIT for user '{}' but the cached token is no longer valid. Invalidating and re-authorizing.", request.username());
            evict(cacheKey);
        }

        final UserToken fresh = delegate.getUserToken(request);
        writeCache(cacheKey, fresh);
        return fresh;
    }

    private UserToken readCache(String cacheKey)
    {
        return backingCache.get(cacheKey);
    }

    private void writeCache(String cacheKey, UserToken token)
    {
        try
        {
            backingCache.put(cacheKey, token);
        }
        catch (RuntimeException ex)
        {
            LOGGER.warn("Failed to store user-token cache entry", ex);
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
            LOGGER.warn("Failed to invalidate user-token cache entry", ex);
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
            identityServiceFacade.decodeToken(token.tokenString());
            return true;
        }
        catch (IdentityServiceFacadeException e)
        {
            LOGGER.debug("Cached access token failed local re-validation: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Derive the cache key as base64url(SHA-256(username || 0x00 || password)). The single-byte separator prevents accidental collisions between e.g. ("ab", "cd") and ("a", "bcd"). The intermediate password byte buffer is wiped after use. {@link UserTokenRequest} guarantees both components are non-null.
     */
    private static String deriveCacheKey(UserTokenRequest request)
    {
        byte[] passwordBytes = null;
        try
        {
            final MessageDigest digest = MessageDigest.getInstance(DIGEST_ALGORITHM);
            digest.update(request.username().getBytes(StandardCharsets.UTF_8));
            digest.update((byte) 0);
            passwordBytes = charsToBytes(request.password());
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

    @SuppressWarnings("PMD.UseVarargs")
    private static byte[] charsToBytes(char[] chars)
    {
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
