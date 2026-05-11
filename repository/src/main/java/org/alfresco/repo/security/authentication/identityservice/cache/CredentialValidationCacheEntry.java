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
 * Records the fact that a given credential was successfully validated by the Identity Service, the normalized principal name to use as the current user, and the moment in time after which this assertion must no longer be trusted.
 * </p>
 *
 * <p>
 * This entry MUST NOT contain any password, access token or refresh token. The {@link #validUntilEpochMillis} is intended to be derived from the access-token expiration time returned by the Identity Provider so that the cache view stays bounded by the IdP's own session lifetime.
 * </p>
 */
public final class CredentialValidationCacheEntry implements Serializable
{
    @Serial
    private static final long serialVersionUID = 1L;

    private final String normalizedUsername;
    private final long validUntilEpochMillis;

    public CredentialValidationCacheEntry(String normalizedUsername, long validUntilEpochMillis)
    {
        this.normalizedUsername = Objects.requireNonNull(normalizedUsername, "normalizedUsername");
        this.validUntilEpochMillis = validUntilEpochMillis;
    }

    public String getNormalizedUsername()
    {
        return normalizedUsername;
    }

    public long getValidUntilEpochMillis()
    {
        return validUntilEpochMillis;
    }

    public boolean isExpired(long nowEpochMillis)
    {
        return nowEpochMillis >= validUntilEpochMillis;
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
        return validUntilEpochMillis == that.validUntilEpochMillis
                && Objects.equals(normalizedUsername, that.normalizedUsername);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(normalizedUsername, validUntilEpochMillis);
    }

    @Override
    public String toString()
    {
        return "CredentialValidationCacheEntry{normalizedUsername='" + normalizedUsername
                + "', validUntilEpochMillis=" + validUntilEpochMillis + "}";
    }
}
