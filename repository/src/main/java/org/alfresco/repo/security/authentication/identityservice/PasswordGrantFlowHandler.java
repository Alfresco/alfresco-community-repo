/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2026 Alfresco Software Limited
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

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.ResourceOwnerPasswordCredentialsGrant;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.oauth2.sdk.token.Tokens;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;

public class PasswordGrantFlowHandler
{
    private final ClientRegistration clientRegistration;

    public PasswordGrantFlowHandler(ClientRegistration clientRegistration)
    {
        this.clientRegistration = clientRegistration;
    }

    /**
     * Handles the password grant flow for obtaining an access token. Replacement for Spring Security implementation removed in Spring 7.
     *
     * @see <a target="_blank" href= "https://tools.ietf.org/html/rfc6749#section-4.3.2">Section 4.3.2 Access Token Request (Resource Owner Password Credentials Grant)</a>
     * @see <a target="_blank" href= "https://tools.ietf.org/html/rfc6749#section-4.3.3">Section 4.3.3 Access Token Response (Resource Owner Password Credentials Grant)</a>
     * @deprecated The OAuth 2.0 Security Best Current Practice disallows the use of the Resource Owner Password Credentials grant. See reference <a target="_blank" href= "https://datatracker.ietf.org/doc/html/rfc9700#section-2.4">OAuth 2.0 Security Best Current Practice.</a>. It was added as backward compatibility with Spring Framework 7. Meant to be removed in the future.
     *
     * @param authorizationGrant
     *            the authorization grant
     * @return the access token authorization
     * @throws IOException
     *             if an I/O error occurs
     * @throws ParseException
     *             if parsing the token response fails
     */
    @Deprecated(since = "26.1")
    public IdentityServiceFacade.AccessTokenAuthorization passwordGrantFlow(IdentityServiceFacade.AuthorizationGrant authorizationGrant) throws IOException, ParseException
    {
        TokenRequest tokenRequest = prepareTokenRequest(authorizationGrant);

        var passwordGrantToken = handleTokenRequest(tokenRequest);

        return new NimbusAccessTokenAuthorization(passwordGrantToken.getTokens());
    }

    private static AccessTokenResponse handleTokenRequest(TokenRequest tokenRequest) throws IOException, ParseException
    {
        HTTPResponse httpResponse = tokenRequest.toHTTPRequest().send();
        TokenResponse tokenResponse = TokenResponse.parse(httpResponse);

        if (!tokenResponse.indicatesSuccess())
        {
            var errorResponse = tokenResponse.toErrorResponse();
            throw new OAuth2AuthorizationException(
                    new OAuth2Error(
                            errorResponse.getErrorObject().getCode(),
                            errorResponse.getErrorObject().getDescription(),
                            null));
        }

        return tokenResponse.toSuccessResponse();
    }

    private TokenRequest prepareTokenRequest(IdentityServiceFacade.AuthorizationGrant authorizationGrant)
    {
        Scope scope = Scope.parse(clientRegistration.getScopes());

        var tokenRequestBuilder = new TokenRequest.Builder(
                URI.create(clientRegistration.getProviderDetails().getTokenUri()),
                createClientAuthentication(),
                createPasswordGrant(authorizationGrant))
                        .scope(scope);

        createRequestMetadata().forEach(tokenRequestBuilder::customParameter);

        return tokenRequestBuilder.build();
    }

    private ResourceOwnerPasswordCredentialsGrant createPasswordGrant(IdentityServiceFacade.AuthorizationGrant authorizationGrant)
    {
        return new ResourceOwnerPasswordCredentialsGrant(authorizationGrant.getUsername(), new Secret(authorizationGrant.getPassword()));
    }

    private ClientAuthentication createClientAuthentication()
    {
        return new ClientSecretBasic(new ClientID(clientRegistration.getClientId()), new Secret(clientRegistration.getClientSecret()));
    }

    private Map<String, String[]> createRequestMetadata()
    {
        return clientRegistration.getProviderDetails().getConfigurationMetadata().entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new String[]{String.valueOf(e.getValue())}));
    }

    private record NimbusAccessTokenAuthorization(Tokens tokens) implements IdentityServiceFacade.AccessTokenAuthorization
    {
        @Override
        public IdentityServiceFacade.AccessToken getAccessToken()
        {
            return new NimbusAccessToken(tokens.getAccessToken());
        }

        @Override
        public String getRefreshTokenValue()
        {
            return Optional.ofNullable(tokens.getRefreshToken())
                    .map(RefreshToken::getValue)
                    .orElse(null);
        }
    }

    private record NimbusAccessToken(AccessToken token) implements IdentityServiceFacade.AccessToken
    {
        @Override
        public String getTokenValue()
        {
            return token.getValue();
        }

        @Override
        public Instant getExpiresAt()
        {
            long lifetime = token.getLifetime();
            if (lifetime <= 0)
            {
                return null;
            }
            return Instant.now().plusSeconds(lifetime);
        }
    }

}
