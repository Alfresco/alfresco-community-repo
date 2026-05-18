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

/**
 * Provides a validated {@link UserToken} for a given user-name / password pair.
 *
 * <p>
 * Implementations may obtain the token directly from the Identity Service ({@link DirectUserTokenProvider}) or transparently cache previously obtained tokens ({@link CachingUserTokenProvider}). The contract from the consumer's point of view is the same: supply credentials, receive a validated token, or have an exception thrown if the credentials are not valid (or the IdP is unreachable). Consumers MUST NOT need to know which implementation is wired.
 * </p>
 *
 * <p>
 * Implementations are expected to be thread-safe.
 * </p>
 */
@SuppressWarnings("PMD.UseVarargs")
public interface UserTokenProvider
{
    /**
     * Authenticate the supplied credentials against the Identity Service (or a local cache thereof) and return the resulting validated user token.
     *
     * @param userName
     *            the user name presented by the caller
     * @param password
     *            the password presented by the caller; never stored as-is by any implementation
     * @return a validated {@link UserToken}
     * @throws org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.IdentityServiceFacadeException
     *             if the Identity Service rejects the credentials or cannot be reached
     */
    UserToken getUserToken(String userName, char[] password);
}
