/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import org.springframework.security.oauth2.client.registration.ClientRegistration;

import org.alfresco.repo.security.authentication.identityservice.user.DecodedTokenUser;
import org.alfresco.repo.security.authentication.identityservice.user.UserInfoAttrMapping;

/**
 * Allows to interact with the Identity Service
 */
public interface IdentityServiceFacade
{
    /**
     * Returns {@link AccessToken} based authorization for provided {@link AuthorizationGrant}.
     * 
     * @param grant
     *            the OAuth2 grant provided by the Resource Owner.
     * @return {@link AccessTokenAuthorization} containing access token and optional refresh token.
     * @throws {@link
     *             AuthorizationException} when provided grant cannot be exchanged for the access token.
     */
    AccessTokenAuthorization authorize(AuthorizationGrant grant) throws AuthorizationException;

    /**
     * Decodes the access token into the {@link DecodedAccessToken} which contains claims connected with a given token.
     * 
     * @param token
     *            {@link String} with encoded access token value.
     * @return {@link DecodedAccessToken} containing decoded claims.
     * @throws {@link
     *             TokenDecodingException} when token decoding failed.
     */
    DecodedAccessToken decodeToken(String token) throws TokenDecodingException;

    /**
     * Gets claims about the authenticated user, such as name and email address, via the UserInfo endpoint of the OpenID provider.
     * 
     * @param token
     *            {@link String} with encoded access token value.
     * @param userInfoAttrMapping
     *            {@link UserInfoAttrMapping} containing the mapping of claims.
     * @return {@link DecodedTokenUser} containing user claims or {@link Optional#empty()} if the token does not contain a username claim.
     */
    Optional<DecodedTokenUser> getUserInfo(String token, UserInfoAttrMapping userInfoAttrMapping);

    /**
     * Gets a client registration
     */
    ClientRegistration getClientRegistration();

    class IdentityServiceFacadeException extends RuntimeException
    {
        public IdentityServiceFacadeException(String message)
        {
            super(message);
        }

        IdentityServiceFacadeException(String message, Throwable cause)
        {
            super(message, cause);
        }
    }

    class AuthorizationException extends IdentityServiceFacadeException
    {
        AuthorizationException(String message)
        {
            super(message);
        }

        AuthorizationException(String message, Throwable cause)
        {
            super(message, cause);
        }
    }

    class UserInfoException extends IdentityServiceFacadeException
    {

        UserInfoException(String message)
        {
            super(message);
        }

        UserInfoException(String message, Throwable cause)
        {
            super(message, cause);
        }
    }

    class TokenDecodingException extends IdentityServiceFacadeException
    {
        TokenDecodingException(String message)
        {
            super(message);
        }

        TokenDecodingException(String message, Throwable cause)
        {
            super(message, cause);
        }
    }

    /**
     * Represents access token authorization with optional refresh token.
     */
    interface AccessTokenAuthorization
    {
        /**
         * Required {@link AccessToken}
         * 
         * @return {@link AccessToken}
         */
        AccessToken getAccessToken();

        /**
         * Optional refresh token.
         * 
         * @return Refresh token or {@code null}
         */
        String getRefreshTokenValue();
    }

    interface AccessToken
    {
        String getTokenValue();

        Instant getExpiresAt();
    }

    interface DecodedAccessToken extends AccessToken
    {
        Object getClaim(String claim);
    }

    class AuthorizationGrant
    {
        private final String username;
        private final String password;
        private final String refreshToken;
        private final String authorizationCode;
        private final String redirectUri;
        private static boolean passwordGrantEnabled;

        private AuthorizationGrant(String username, String password, String refreshToken, String authorizationCode, String redirectUri)
        {
            this.username = username;
            this.password = password;
            this.refreshToken = refreshToken;
            this.authorizationCode = authorizationCode;
            this.redirectUri = redirectUri;
        }

        public static AuthorizationGrant password(String username, String password)
        {
            return new AuthorizationGrant(requireNonNull(username), requireNonNull(password), null, null, null);
        }

        public static AuthorizationGrant refreshToken(String refreshToken)
        {
            return new AuthorizationGrant(null, null, requireNonNull(refreshToken), null, null);
        }

        public static AuthorizationGrant authorizationCode(String authorizationCode, String redirectUri)
        {
            return new AuthorizationGrant(null, null, null, requireNonNull(authorizationCode), requireNonNull(redirectUri));
        }

        static void setPasswordGrantEnabled(boolean passwordGrantEnabled) {
            AuthorizationGrant.passwordGrantEnabled = passwordGrantEnabled;
        }

        boolean isPassword()
        {
            if (nonNull(username) && !passwordGrantEnabled) {
                throw new AuthorizationException("Password Grant Flow is deprecated and disabled by configuration property 'identity-service.authentication.enable-username-password-authentication=false'.");
            }
            return nonNull(username);
        }

        boolean isRefreshToken()
        {
            return nonNull(refreshToken);
        }

        boolean isAuthorizationCode()
        {
            return nonNull(authorizationCode);
        }

        String getUsername()
        {
            return username;
        }

        String getPassword()
        {
            return password;
        }

        String getRefreshToken()
        {
            return refreshToken;
        }

        String getAuthorizationCode()
        {
            return authorizationCode;
        }

        String getRedirectUri()
        {
            return redirectUri;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }
            AuthorizationGrant that = (AuthorizationGrant) o;
            return Objects.equals(username, that.username) &&
                    Objects.equals(password, that.password) &&
                    Objects.equals(refreshToken, that.refreshToken) &&
                    Objects.equals(authorizationCode, that.authorizationCode) &&
                    Objects.equals(redirectUri, that.redirectUri);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(username, password, refreshToken, authorizationCode, redirectUri);
        }
    }
}
