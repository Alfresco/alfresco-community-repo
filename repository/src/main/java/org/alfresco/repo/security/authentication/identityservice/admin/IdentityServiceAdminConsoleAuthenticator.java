/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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
package org.alfresco.repo.security.authentication.identityservice.admin;

import static org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.AuthorizationGrant.authorizationCode;
import static org.alfresco.repo.security.authentication.identityservice.IdentityServiceMetadataKeys.SCOPES_SUPPORTED_METADATA;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.Identifier;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.external.AdminConsoleAuthenticator;
import org.alfresco.repo.security.authentication.external.RemoteUserMapper;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceConfig;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.AccessTokenAuthorization;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.AuthorizationException;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.AuthorizationGrant;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistration.ProviderDetails;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * An {@link AdminConsoleAuthenticator} implementation to extract an externally authenticated user ID
 * or to initiate the OIDC authorization code flow.
 */
public class IdentityServiceAdminConsoleAuthenticator implements AdminConsoleAuthenticator, ActivateableBean
{
    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityServiceAdminConsoleAuthenticator.class);

    private static final String ALFRESCO_ACCESS_TOKEN = "ALFRESCO_ACCESS_TOKEN";
    private static final String ALFRESCO_REFRESH_TOKEN = "ALFRESCO_REFRESH_TOKEN";
    private static final String ALFRESCO_TOKEN_EXPIRATION = "ALFRESCO_TOKEN_EXPIRATION";
    private static final Set<String> SCOPES = Set.of("openid", "profile", "email", "offline_access");

    private IdentityServiceConfig identityServiceConfig;
    private IdentityServiceFacade identityServiceFacade;
    private AdminConsoleAuthenticationCookiesService cookiesService;
    private RemoteUserMapper remoteUserMapper;
    private boolean isEnabled;

    @Override
    public String getAdminConsoleUser(HttpServletRequest request, HttpServletResponse response)
    {
        // Try to extract username from the authorization header
        String username = remoteUserMapper.getRemoteUser(request);
        if (username != null)
        {
            return username;
        }

        String bearerToken = cookiesService.getCookie(ALFRESCO_ACCESS_TOKEN, request);

        if (bearerToken != null)
        {
            bearerToken = refreshTokenIfNeeded(request, response, bearerToken);
        }
        else
        {
            String code = request.getParameter("code");
            if (code != null)
            {
                bearerToken = retrieveTokenUsingAuthCode(request, response, code);
            }
        }

        if (bearerToken == null)
        {
            return null;
        }

        return remoteUserMapper.getRemoteUser(decorateBearerHeader(bearerToken, request));
    }

    @Override
    public void requestAuthentication(HttpServletRequest request, HttpServletResponse response)
    {
        respondWithAuthChallenge(request, response);
    }

    private void respondWithAuthChallenge(HttpServletRequest request, HttpServletResponse response)
    {
        try
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Responding with the authentication challenge");
            }
            response.sendRedirect(getAuthenticationRequest(request));
        }
        catch (IOException e)
        {
            LOGGER.error("Error while trying to respond with the authentication challenge: {}", e.getMessage(), e);
            throw new AuthenticationException(e.getMessage(), e);
        }
    }

    private String retrieveTokenUsingAuthCode(HttpServletRequest request, HttpServletResponse response, String code)
    {
        String bearerToken = null;
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Retrieving a response using the Authorization Code at the Token Endpoint");
        }
        try
        {
            AccessTokenAuthorization accessTokenAuthorization = identityServiceFacade.authorize(
                authorizationCode(code, request.getRequestURL().toString()));
            addCookies(response, accessTokenAuthorization);
            bearerToken = accessTokenAuthorization.getAccessToken().getTokenValue();
        }
        catch (AuthorizationException exception)
        {
            if (LOGGER.isWarnEnabled())
            {
                LOGGER.warn(
                    "Error while trying to retrieve a response using the Authorization Code at the Token Endpoint: {}",
                    exception.getMessage());
            }
        }
        return bearerToken;
    }

    private String refreshTokenIfNeeded(HttpServletRequest request, HttpServletResponse response, String bearerToken)
    {
        String refreshToken = cookiesService.getCookie(ALFRESCO_REFRESH_TOKEN, request);
        String authTokenExpiration = cookiesService.getCookie(ALFRESCO_TOKEN_EXPIRATION, request);
        try
        {
            if (isAuthTokenExpired(authTokenExpiration))
            {
                bearerToken = refreshAuthToken(refreshToken, response);
            }
        }
        catch (Exception e)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Error while trying to refresh Auth Token: {}", e.getMessage());
            }
            bearerToken = null;
            resetCookies(response);
        }
        return bearerToken;
    }

    private void addCookies(HttpServletResponse response, AccessTokenAuthorization accessTokenAuthorization)
    {
        cookiesService.addCookie(ALFRESCO_ACCESS_TOKEN, accessTokenAuthorization.getAccessToken().getTokenValue(), response);
        cookiesService.addCookie(ALFRESCO_TOKEN_EXPIRATION, String.valueOf(
            accessTokenAuthorization.getAccessToken().getExpiresAt().toEpochMilli()), response);
        cookiesService.addCookie(ALFRESCO_REFRESH_TOKEN, accessTokenAuthorization.getRefreshTokenValue(), response);
    }

    private String getAuthenticationRequest(HttpServletRequest request)
    {

        ClientRegistration clientRegistration = identityServiceFacade.getClientRegistration();

        UriComponentsBuilder authRequestBuilder = UriComponentsBuilder.fromUriString(clientRegistration.getProviderDetails().getAuthorizationUri())
            .queryParam("client_id", clientRegistration.getClientId())
            .queryParam("redirect_uri", getRedirectUri(request.getRequestURL().toString()))
            .queryParam("response_type", "code")
            .queryParam("scope", String.join("+", getScopes(clientRegistration)));

        if(StringUtils.isNotBlank(identityServiceConfig.getAudience()))
        {
            authRequestBuilder.queryParam("audience", identityServiceConfig.getAudience());
        }

        return authRequestBuilder.build().toUriString();
    }

    private Set<String> getScopes(ClientRegistration clientRegistration)
    {
        return Optional.ofNullable(clientRegistration.getProviderDetails())
            .map(ProviderDetails::getConfigurationMetadata)
            .map(metadata -> metadata.get(SCOPES_SUPPORTED_METADATA))
            .filter(Scope.class::isInstance)
            .map(scope -> (Scope) scope)
            .map(this::getSupportedScopes)
            .orElse(clientRegistration.getScopes());
    }

    private Set<String> getSupportedScopes(Scope scopes)
    {
        return scopes.stream()
            .filter(scope -> SCOPES.contains(scope.getValue()))
            .map(Identifier::getValue)
            .collect(Collectors.toSet());
    }

    private String getRedirectUri(String requestURL)
    {
        try
        {
            URI originalUri = new URI(requestURL);
            URI redirectUri = new URI(originalUri.getScheme(), originalUri.getAuthority(), identityServiceConfig.getAdminConsoleRedirectPath(), originalUri.getQuery(), originalUri.getFragment());
            return redirectUri.toASCIIString();
        }
        catch (URISyntaxException e)
        {
            LOGGER.error("Error while trying to get the redirect URI and respond with the authentication challenge: {}", e.getMessage(), e);
            throw new AuthenticationException(e.getMessage(), e);
        }
    }

    private void resetCookies(HttpServletResponse response)
    {
        cookiesService.resetCookie(ALFRESCO_TOKEN_EXPIRATION, response);
        cookiesService.resetCookie(ALFRESCO_ACCESS_TOKEN, response);
        cookiesService.resetCookie(ALFRESCO_REFRESH_TOKEN, response);
    }

    private String refreshAuthToken(String refreshToken, HttpServletResponse response)
    {
        AccessTokenAuthorization accessTokenAuthorization = doRefreshAuthToken(refreshToken);
        addCookies(response, accessTokenAuthorization);
        return accessTokenAuthorization.getAccessToken().getTokenValue();
    }

    private AccessTokenAuthorization doRefreshAuthToken(String refreshToken)
    {
        AccessTokenAuthorization accessTokenAuthorization = identityServiceFacade.authorize(
            AuthorizationGrant.refreshToken(refreshToken));
        if (accessTokenAuthorization == null || accessTokenAuthorization.getAccessToken() == null)
        {
            throw new AuthenticationException("AccessTokenResponse is null or empty");
        }
        return accessTokenAuthorization;
    }

    private static boolean isAuthTokenExpired(String authTokenExpiration)
    {
        return Instant.now().compareTo(Instant.ofEpochMilli(Long.parseLong(authTokenExpiration))) >= 0;
    }

    private HttpServletRequest decorateBearerHeader(String authToken, HttpServletRequest servletRequest)
    {
        Map<String, String> additionalHeaders = new HashMap<>();
        additionalHeaders.put("Authorization", "Bearer " + authToken);
        return new AdminConsoleHttpServletRequestWrapper(additionalHeaders, servletRequest);
    }

    public void setIdentityServiceFacade(
        IdentityServiceFacade identityServiceFacade)
    {
        this.identityServiceFacade = identityServiceFacade;
    }

    public void setRemoteUserMapper(RemoteUserMapper remoteUserMapper)
    {
        this.remoteUserMapper = remoteUserMapper;
    }

    public void setCookiesService(
        AdminConsoleAuthenticationCookiesService cookiesService)
    {
        this.cookiesService = cookiesService;
    }

    public void setIdentityServiceConfig(
        IdentityServiceConfig identityServiceConfig)
    {
        this.identityServiceConfig = identityServiceConfig;
    }

    @Override
    public boolean isActive()
    {
        return this.isEnabled;
    }

    public void setActive(boolean isEnabled)
    {
        this.isEnabled = isEnabled;
    }
}
