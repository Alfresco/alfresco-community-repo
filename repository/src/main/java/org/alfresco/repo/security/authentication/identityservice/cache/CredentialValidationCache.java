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

import java.util.Optional;

/**
 * Caches the outcome of a successful credential validation against the Identity Service so that subsequent identical credential presentations within the token-lifetime window can skip the round-trip to the Authorization Server.
 *
 * <p>
 * Implementations MUST be thread-safe and MUST NOT cache passwords nor any access/refresh token issued by the Authorization Server. The cache is intended only to record the assertion <i>"these credentials were observed to be valid until time T"</i>, with T aligned to the token expiration time returned by the Identity Provider.
 * </p>
 *
 * <p>
 * The cache is fail-closed: when disabled or misconfigured, lookups behave as misses and writes are silently ignored, so the calling component falls back to the regular authorize-on-every-request behaviour.
 * </p>
 */
@SuppressWarnings("PMD.UseVarargs")
public interface CredentialValidationCache
{
    /**
     * @return {@code true} when the cache is enabled and properly configured.
     */
    boolean isEnabled();

    /**
     * Look up a previously recorded validation outcome for the supplied credentials.
     *
     * @param userName
     *            the user name presented by the caller
     * @param password
     *            the password presented by the caller (never stored as-is)
     * @return non-empty if a non-expired entry was found; empty otherwise
     */
    Optional<CredentialValidationCacheEntry> get(String userName, char[] password);

    /**
     * Record a successful validation outcome.
     *
     * @param userName
     *            the user name that was validated
     * @param password
     *            the password that was validated (never stored as-is)
     * @param entry
     *            non-secret outcome metadata (normalized username + validity window)
     */
    void put(String userName, char[] password, CredentialValidationCacheEntry entry);

    /**
     * Remove any cached validation outcome for the supplied credentials. Used to defensively clear stale entries when the upstream Authorization Server rejects a validation.
     */
    void invalidate(String userName, char[] password);
}
