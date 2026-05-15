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

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Cache value for the {@link CredentialValidationCache}.
 *
 * <p>
 * Records the outcome of a successful credential validation against the Identity Service: the normalized principal name to use as the current user, and the access-token string that proved the validation. The token is retained so that the cache HIT path can re-validate it locally via {@link org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade#decodeToken(String)} (signature, {@code exp}, {@code iss}, etc.) without contacting the Authorization Server.
 * </p>
 *
 * <p>
 * <b>Storage scope:</b> entries of this type are intended to live exclusively in a local, single-JVM cache. They MUST NOT be replicated across the cluster nor persisted: the access-token string is a bearer credential and must not leave the JVM that obtained it. The associated cache region (see {@code cache.identityServiceCredentialValidationCache} in {@code caches.properties}) is configured as {@code cluster.type=local} for this reason.
 * </p>
 *
 * <p>
 * The entry never holds the user's password and {@link #toString()} never echoes the token value.
 * </p>
 */
public final class CredentialValidationCacheEntry implements Serializable
{
    @Serial
    private static final long serialVersionUID = 2L;

    private final String normalizedUsername;
    private final String tokenString;

    public CredentialValidationCacheEntry(String normalizedUsername, String tokenString)
    {
        this.normalizedUsername = Objects.requireNonNull(normalizedUsername, "normalizedUsername");
        this.tokenString = Objects.requireNonNull(tokenString, "tokenString");
    }

    public String getNormalizedUsername()
    {
        return normalizedUsername;
    }

    /**
     * @return the access-token string captured at validation time. Callers MUST treat this as a bearer credential and MUST NOT log it.
     */
    public String getTokenString()
    {
        return tokenString;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof CredentialValidationCacheEntry that))
        {
            return false;
        }
        return Objects.equals(normalizedUsername, that.normalizedUsername)
                && Objects.equals(tokenString, that.tokenString);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(normalizedUsername, tokenString);
    }

    /**
     * Deliberately omits the token value: only the principal name is exposed so accidental {@code toString()} logging cannot leak the bearer credential.
     */
    @Override
    public String toString()
    {
        return "CredentialValidationCacheEntry{normalizedUsername='" + normalizedUsername + "'}";
    }
}
