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

import static java.util.Objects.requireNonNull;

import static org.alfresco.repo.security.authentication.identityservice.IdentityServiceMetadataKey.AUDIENCE;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import com.nimbusds.openid.connect.sdk.claims.PersonClaims;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.client.endpoint.AbstractOAuth2AuthorizationGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2PasswordGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2PasswordGrantRequestEntityConverter;
import org.springframework.security.oauth2.client.endpoint.OAuth2RefreshTokenGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.RestClientRefreshTokenTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistration.ProviderDetails;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import org.alfresco.repo.security.authentication.identityservice.client.AlfrescoDefaultPasswordTokenResponseClient;
import org.alfresco.repo.security.authentication.identityservice.user.DecodedTokenUser;
import org.alfresco.repo.security.authentication.identityservice.user.UserInfoAttrMapping;

class SpringBasedIdentityServiceFacade implements IdentityServiceFacade
{
    private static final Log LOGGER = LogFactory.getLog(SpringBasedIdentityServiceFacade.class);
    private static final Instant SOME_INSIGNIFICANT_DATE_IN_THE_PAST = Instant.MIN.plusSeconds(12345);
    private final Map<AuthorizationGrantType, OAuth2AccessTokenResponseClient> clients;
    private final DefaultOAuth2UserService defaultOAuth2UserService;
    private final ClientRegistration clientRegistration;
    private final JwtDecoder jwtDecoder;

    SpringBasedIdentityServiceFacade(RestOperations restOperations, ClientRegistration clientRegistration,
            JwtDecoder jwtDecoder)
    {
        requireNonNull(restOperations);
        this.clientRegistration = requireNonNull(clientRegistration);
        this.jwtDecoder = requireNonNull(jwtDecoder);
        this.clients = Map.of(
                AuthorizationGrantType.AUTHORIZATION_CODE, createAuthorizationCodeClient(restOperations),
                AuthorizationGrantType.REFRESH_TOKEN, createRefreshTokenClient(restOperations),
                AuthorizationGrantType.PASSWORD, createPasswordClient(restOperations, clientRegistration));
        this.defaultOAuth2UserService = createOAuth2UserService(restOperations);
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

    @Override
    public Optional<DecodedTokenUser> getUserInfo(String token, UserInfoAttrMapping userInfoAttrMapping)
    {
        try
        {
            return Optional.ofNullable(defaultOAuth2UserService.loadUser(new OAuth2UserRequest(clientRegistration, getSpringAccessToken(token))))
                    .flatMap(oAuth2User -> mapOAuth2UserToDecodedTokenUser(oAuth2User, userInfoAttrMapping));
        }
        catch (OAuth2AuthenticationException exception)
        {
            LOGGER.warn("User Info Request failed: " + exception.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public ClientRegistration getClientRegistration()
    {
        return clientRegistration;
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
            final OAuth2AccessToken expiredAccessToken = getSpringAccessToken("JUST_FOR_FULFILLING_THE_SPRING_API");
            final OAuth2RefreshToken refreshToken = new OAuth2RefreshToken(grant.getRefreshToken(), null);

            return new OAuth2RefreshTokenGrantRequest(clientRegistration, expiredAccessToken, refreshToken,
                    clientRegistration.getScopes());
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
                            .build());
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

    private static OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> createAuthorizationCodeClient(
            RestOperations rest)
    {
        final RestClientAuthorizationCodeTokenResponseClient client = new RestClientAuthorizationCodeTokenResponseClient();
        client.setRestClient(RestClient.create((RestTemplate) rest));
        return client;
    }

    private static OAuth2AccessTokenResponseClient<OAuth2RefreshTokenGrantRequest> createRefreshTokenClient(
            RestOperations rest)
    {
        final RestClientRefreshTokenTokenResponseClient client = new RestClientRefreshTokenTokenResponseClient();
        client.setRestClient(RestClient.create((RestTemplate) rest));
        return client;
    }

    private static DefaultOAuth2UserService createOAuth2UserService(RestOperations rest)
    {
        final DefaultOAuth2UserService userService = new DefaultOAuth2UserService();
        userService.setRestOperations(rest);
        return userService;
    }

    private Optional<DecodedTokenUser> mapOAuth2UserToDecodedTokenUser(OAuth2User oAuth2User, UserInfoAttrMapping userInfoAttrMapping)
    {
        var preferredUsername = Optional.ofNullable(oAuth2User.getAttribute(PersonClaims.PREFERRED_USERNAME_CLAIM_NAME))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .filter(StringUtils::isNotEmpty);
        var userName = Optional.ofNullable(oAuth2User.getName()).filter(username -> !username.isEmpty()).or(() -> preferredUsername);
        return userName.map(name -> DecodedTokenUser.validateAndCreate(name,
                oAuth2User.getAttribute(userInfoAttrMapping.firstNameClaim()),
                oAuth2User.getAttribute(userInfoAttrMapping.lastNameClaim()),
                oAuth2User.getAttribute(userInfoAttrMapping.emailClaim())));
    }

    private static OAuth2AccessTokenResponseClient<OAuth2PasswordGrantRequest> createPasswordClient(RestOperations rest,
            ClientRegistration clientRegistration)
    {
        final AlfrescoDefaultPasswordTokenResponseClient client = new AlfrescoDefaultPasswordTokenResponseClient(rest);

        Optional.of(clientRegistration)
                .map(ClientRegistration::getProviderDetails)
                .map(ProviderDetails::getConfigurationMetadata)
                .map(metadata -> metadata.get(AUDIENCE.getValue()))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .ifPresent(audienceValue -> {
                    final OAuth2PasswordGrantRequestEntityConverter requestEntityConverter = new OAuth2PasswordGrantRequestEntityConverter();
                    requestEntityConverter.addParametersConverter(audienceParameterConverter(audienceValue));
                    client.setRequestEntityConverter(requestEntityConverter);
                });
        return client;
    }

    private static Converter<OAuth2PasswordGrantRequest, MultiValueMap<String, String>> audienceParameterConverter(
            String audienceValue)
    {
        return (grantRequest) -> {
            MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
            parameters.set("audience", audienceValue);

            return parameters;
        };
    }

    private static OAuth2AccessToken getSpringAccessToken(String token)
    {
        // Just for fulfilling the Spring API
        return new OAuth2AccessToken(
                TokenType.BEARER,
                token,
                SOME_INSIGNIFICANT_DATE_IN_THE_PAST,
                SOME_INSIGNIFICANT_DATE_IN_THE_PAST.plusSeconds(1));
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

    private static class SpringDecodedAccessToken extends SpringAccessToken implements DecodedAccessToken
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
