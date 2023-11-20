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

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.oauth2.client.endpoint.AbstractOAuth2AuthorizationGrantRequest;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.DefaultPasswordTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.DefaultRefreshTokenTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2PasswordGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2RefreshTokenGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.client.RestOperations;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

class SpringBasedIdentityServiceFacade implements IdentityServiceFacade
{
    private static final Log LOGGER = LogFactory.getLog(SpringBasedIdentityServiceFacade.class);
    private static final Instant SOME_INSIGNIFICANT_DATE_IN_THE_PAST = Instant.MIN.plusSeconds(12345);
    private final Map<AuthorizationGrantType, OAuth2AccessTokenResponseClient> clients;
    private final ClientRegistration clientRegistration;
    private final JwtDecoder jwtDecoder;

    SpringBasedIdentityServiceFacade(RestOperations restOperations, ClientRegistration clientRegistration, JwtDecoder jwtDecoder)
    {
        requireNonNull(restOperations);
        this.clientRegistration = requireNonNull(clientRegistration);
        this.jwtDecoder = requireNonNull(jwtDecoder);
        this.clients = Map.of(
                AuthorizationGrantType.AUTHORIZATION_CODE, createAuthorizationCodeClient(restOperations),
                AuthorizationGrantType.REFRESH_TOKEN, createRefreshTokenClient(restOperations),
                AuthorizationGrantType.PASSWORD, createPasswordClient(restOperations));
    }

    @Override
    public AccessTokenAuthorization authorize(AuthorizationGrant authorizationGrant)
    {
        final AbstractOAuth2AuthorizationGrantRequest request = createRequest(authorizationGrant);
        final OAuth2AccessTokenResponseClient client = getClient(request);

        final OAuth2AccessTokenResponse response;
        try
        {
            response = client.getTokenResponse(request);
        }
        catch (OAuth2AuthorizationException e)
        {
            LOGGER.debug("Failed to authorize against Authorization Server. Reason: " + e.getError() + ".");
            throw new AuthorizationException("Failed to obtain access token. " + e.getError(), e);
        }
        catch (RuntimeException e)
        {
            LOGGER.warn("Failed to authorize against Authorization Server. Reason: " + e.getMessage());
            throw new AuthorizationException("Failed to obtain access token.", e);
        }

        return new SpringAccessTokenAuthorization(response);
    }

    public UserInfo getUserInfo(String token){
        try
        {
            if(token == null || token.isEmpty())
            {
                return null;
            }
            HTTPResponse httpResponse = new UserInfoRequest(new URI(clientRegistration.getProviderDetails().getUserInfoEndpoint().getUri()), new BearerAccessToken(token))
                        .toHTTPRequest()
                        .send();
            UserInfoResponse userInfoResponse = UserInfoResponse.parse(httpResponse);
            return  userInfoResponse.toSuccessResponse().getUserInfo();
        }
        catch (IOException | ParseException | URISyntaxException e)
        {
            LOGGER.warn("Failed to get user information. Reason: " + e.getMessage());
            throw new UserInfoException(e.getMessage());
        }
    }

    @Override
    public DecodedAccessToken decodeToken(String token)
    {
        final Jwt validToken;
        try
        {
            validToken = jwtDecoder.decode(token);
        }
        catch (RuntimeException e)
        {
            throw new TokenDecodingException("Failed to decode token. " + e.getMessage(), e);
        }
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Bearer token outcome: " + validToken.getClaims());
        }
        return new SpringDecodedAccessToken(validToken);
    }

    private AbstractOAuth2AuthorizationGrantRequest createRequest(AuthorizationGrant grant)
    {
        if (grant.isPassword())
        {
            return new OAuth2PasswordGrantRequest(clientRegistration, grant.getUsername(), grant.getPassword());
        }

        if (grant.isRefreshToken())
        {
            final OAuth2AccessToken expiredAccessToken = new OAuth2AccessToken(
                    TokenType.BEARER,
                    "JUST_FOR_FULFILLING_THE_SPRING_API",
                    SOME_INSIGNIFICANT_DATE_IN_THE_PAST,
                    SOME_INSIGNIFICANT_DATE_IN_THE_PAST.plusSeconds(1));
            final OAuth2RefreshToken refreshToken = new OAuth2RefreshToken(grant.getRefreshToken(), null);

            return new OAuth2RefreshTokenGrantRequest(clientRegistration, expiredAccessToken, refreshToken, clientRegistration.getScopes());
        }

        if (grant.isAuthorizationCode())
        {
            final OAuth2AuthorizationExchange authzExchange = new OAuth2AuthorizationExchange(
                    OAuth2AuthorizationRequest.authorizationCode()
                                              .clientId(clientRegistration.getClientId())
                                              .authorizationUri(clientRegistration.getProviderDetails().getAuthorizationUri())
                                              .redirectUri(grant.getRedirectUri())
                                              .scopes(clientRegistration.getScopes())
                                              .build(),
                    OAuth2AuthorizationResponse.success(grant.getAuthorizationCode())
                                               .redirectUri(grant.getRedirectUri())
                                               .build()
            );
            return new OAuth2AuthorizationCodeGrantRequest(clientRegistration, authzExchange);
        }

        throw new UnsupportedOperationException("Unsupported grant type.");
    }

    private OAuth2AccessTokenResponseClient getClient(AbstractOAuth2AuthorizationGrantRequest request)
    {
        final AuthorizationGrantType grantType = request.getGrantType();
        final OAuth2AccessTokenResponseClient client = clients.get(grantType);
        if (client == null)
        {
            throw new UnsupportedOperationException("Unsupported grant type `" + grantType + "`.");
        }
        return client;
    }

    private static OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> createAuthorizationCodeClient(RestOperations rest)
    {
        final DefaultAuthorizationCodeTokenResponseClient client = new DefaultAuthorizationCodeTokenResponseClient();
        client.setRestOperations(rest);
        return client;
    }

    private static OAuth2AccessTokenResponseClient<OAuth2RefreshTokenGrantRequest> createRefreshTokenClient(RestOperations rest)
    {
        final DefaultRefreshTokenTokenResponseClient client = new DefaultRefreshTokenTokenResponseClient();
        client.setRestOperations(rest);
        return client;
    }

    private static OAuth2AccessTokenResponseClient<OAuth2PasswordGrantRequest> createPasswordClient(RestOperations rest)
    {
        final DefaultPasswordTokenResponseClient client = new DefaultPasswordTokenResponseClient();
        client.setRestOperations(rest);
        return client;
    }

    private static class SpringAccessTokenAuthorization implements AccessTokenAuthorization
    {
        private final OAuth2AccessTokenResponse tokenResponse;

        private SpringAccessTokenAuthorization(OAuth2AccessTokenResponse tokenResponse)
        {
            this.tokenResponse = requireNonNull(tokenResponse);
        }

        @Override
        public AccessToken getAccessToken()
        {
            return new SpringAccessToken(tokenResponse.getAccessToken());
        }

        @Override
        public String getRefreshTokenValue()
        {
            return Optional.of(tokenResponse)
                           .map(OAuth2AccessTokenResponse::getRefreshToken)
                           .map(AbstractOAuth2Token::getTokenValue)
                           .orElse(null);
        }
    }

    private static class SpringAccessToken implements AccessToken
    {
        private final AbstractOAuth2Token token;

        private SpringAccessToken(AbstractOAuth2Token token)
        {
            this.token = requireNonNull(token);
        }

        @Override
        public String getTokenValue()
        {
            return token.getTokenValue();
        }

        @Override
        public Instant getExpiresAt()
        {
            return token.getExpiresAt();
        }
    }

    private static class SpringDecodedAccessToken extends SpringAccessToken  implements DecodedAccessToken
    {
        private final Jwt jwt;

        private SpringDecodedAccessToken(Jwt jwt)
        {
            super(jwt);
            this.jwt = jwt;
        }

        @Override
        public Object getClaim(String claim)
        {
            return jwt.getClaim(claim);
        }
    }
}
