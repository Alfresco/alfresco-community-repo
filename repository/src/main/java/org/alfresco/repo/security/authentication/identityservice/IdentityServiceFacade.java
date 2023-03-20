/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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

import java.util.Optional;

/**
 * Allows to interact with the Identity Service
 */
interface IdentityServiceFacade
{
    /**
     * Verifies provided user credentials. The OAuth2's Client role is only used to verify the user credentials (Resource Owner Password
     * Credentials Flow) this is why there is an explicit method for verifying these.
     *
     * @param username user's name
     * @param password user's password
     * @throws CredentialsVerificationException when the verification failed or couldn't be performed
     */
    void verifyCredentials(String username, String password);

    /**
     * Extracts username from provided token
     *
     * @param token token representation
     * @return possible username
     */
    Optional<String> extractUsernameFromToken(String token);

    class IdentityServiceFacadeException extends RuntimeException
    {
        IdentityServiceFacadeException(String message)
        {
            super(message);
        }

        IdentityServiceFacadeException(String message, Throwable cause)
        {
            super(message, cause);
        }
    }
    class CredentialsVerificationException extends IdentityServiceFacadeException
    {
        CredentialsVerificationException(String message)
        {
            super(message);
        }

        CredentialsVerificationException(String message, Throwable cause)
        {
            super(message, cause);
        }
    }

    class TokenException extends IdentityServiceFacadeException
    {
        TokenException(String message)
        {
            super(message);
        }

        TokenException(String message, Throwable cause)
        {
            super(message, cause);
        }
    }
}
