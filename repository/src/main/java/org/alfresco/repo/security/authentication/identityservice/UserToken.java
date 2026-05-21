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
 * <b>Bearer-credential handling:</b> {@link #tokenString()} returns a bearer credential. Callers MUST NOT log, persist, or replicate it across processes. {@link #toString()} is deliberately overridden to omit the token (the record-default would echo every component) so that accidental {@code logger.debug(token)} calls cannot leak the credential.
 * </p>
 */
public record UserToken(String normalizedUsername, String tokenString) implements Serializable
{
    public UserToken
    {
        Objects.requireNonNull(normalizedUsername, "normalizedUsername");
        Objects.requireNonNull(tokenString, "tokenString");
    }

    /**
     * Overrides the record-generated {@code toString()} to suppress the bearer token. Only the principal name is exposed so accidental log/print statements cannot leak the credential.
     */
    @Override
    public String toString()
    {
        return "UserToken[normalizedUsername=" + normalizedUsername + "]";
    }
}
