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

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Result of a successful user authentication against the Identity Service.
 *
 * <p>
 * Carries the normalized principal name (the canonical user identifier resolved by the IdP, which may differ from the credentials supplied by the caller) and the access-token string returned by the IdP. The token is exposed so that components (e.g., a caching {@link UserTokenProvider}) can re-validate it locally without contacting the IdP again.
 * </p>
 *
 * <p>
 * <b>Bearer-credential handling:</b> {@link #getTokenString()} returns a bearer credential. Callers MUST NOT log, persist, or replicate it across processes. {@link #toString()} deliberately omits the token to reduce the chance of accidental leakage.
 * </p>
 */
public final class UserToken implements Serializable
{
    @Serial
    private static final long serialVersionUID = 1L;

    private final String normalizedUsername;
    private final String tokenString;

    public UserToken(String normalizedUsername, String tokenString)
    {
        this.normalizedUsername = Objects.requireNonNull(normalizedUsername, "normalizedUsername");
        this.tokenString = Objects.requireNonNull(tokenString, "tokenString");
    }

    public String getNormalizedUsername()
    {
        return normalizedUsername;
    }

    /**
     * @return the access-token string captured at validation time. Treat as a bearer credential.
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
        if (!(o instanceof UserToken that))
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
        return "UserToken{normalizedUsername='" + normalizedUsername + "'}";
    }
}
