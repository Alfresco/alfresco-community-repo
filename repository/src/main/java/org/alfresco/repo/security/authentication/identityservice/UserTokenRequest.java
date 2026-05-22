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

/**
 * Credential pair presented to a {@link UserTokenProvider}.
 *
 * <p>
 * Centralises null-validation of the inputs (mirrors the pattern in {@link IdentityServiceFacade.AuthorizationGrant#password(String, String)}) so that every {@link UserTokenProvider} implementation can rely on non-null fields. {@link #toString()} is overridden to suppress the password (the record-default would echo the {@code char[]} array's identity hash) and to keep the user name visible for log/diagnostic purposes.
 * </p>
 *
 * <p>
 * <b>Password lifetime:</b> the supplied {@code char[]} reference is held by this record only for the duration of the {@code getUserToken(...)} call. Implementations MUST NOT retain a reference to the array (or to the request) past method return.
 * </p>
 */
public record UserTokenRequest(String username, char[] password)
{
    public UserTokenRequest
    {
        requireNonNull(username, "username");
        requireNonNull(password, "password");
    }

    /**
     * Overrides the record-generated {@code toString()} to suppress the password component. Only the user name is exposed so accidental log/print statements cannot leak the credential.
     */
    @Override
    public String toString()
    {
        return "UserTokenRequest[username=" + username + "]";
    }
}
