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
package org.alfresco.repo.security.authentication.identityservice.authentication;

import static org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.AuthorizationGrant.authorizationCode;
import static org.alfresco.repo.security.authentication.identityservice.IdentityServiceMetadataKey.SCOPES_SUPPORTED;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.Identifier;
import com.nimbusds.oauth2.sdk.id.State;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistration.ProviderDetails;
import org.springframework.web.util.UriComponentsBuilder;

import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.external.ExternalUserAuthenticator;
import org.alfresco.repo.security.authentication.external.RemoteUserMapper;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceConfig;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.AccessTokenAuthorization;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.AuthorizationException;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.AuthorizationGrant;

public abstract class AbstractIdentityServiceAuthenticator implements ExternalUserAuthenticator
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractIdentityServiceAuthenticator.class);

    protected IdentityServiceConfig identityServiceConfig;
    protected IdentityServiceFacade identityServiceFacade;
    protected AdminAuthenticationCookiesService cookiesService;
    protected RemoteUserMapper remoteUserMapper;

    public static final String ALFRESCO_ACCESS_TOKEN = "ALFRESCO_ACCESS_TOKEN";
    public static final String ALFRESCO_REFRESH_TOKEN = "ALFRESCO_REFRESH_TOKEN";
    public static final String ALFRESCO_TOKEN_EXPIRATION = "ALFRESCO_TOKEN_EXPIRATION";

    protected abstract String getConfiguredRedirectPath();

    protected abstract Set<String> getConfiguredScopes();

    @Override
    public String getUserId(HttpServletRequest request, HttpServletResponse response)
    {
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

        HttpServletRequest wrappedRequest = newRequestWrapper(Map.of("Authorization", "Bearer " + bearerToken), request);
        return remoteUserMapper.getRemoteUser(wrappedRequest);
    }

    @Override
    public void requestAuthentication(HttpServletRequest request, HttpServletResponse response)
    {
        try
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Responding with the authentication challenge");
            }
            String authenticationRequest = buildAuthRequestUrl(request);
            response.sendRedirect(authenticationRequest);
        }
        catch (IOException e)
        {
            LOGGER.error("Error while trying to respond with the authentication challenge: {}", e.getMessage(), e);
            throw new AuthenticationException(e.getMessage(), e);
        }
    }

    protected String getRedirectUri(String requestURL)
    {
        return buildRedirectUri(requestURL, getConfiguredRedirectPath());
    }

    public String buildAuthRequestUrl(HttpServletRequest request)
    {
        ClientRegistration clientRegistration = identityServiceFacade.getClientRegistration();
        State state = new State();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(clientRegistration.getProviderDetails()
                .getAuthorizationUri())
                .queryParam("client_id", clientRegistration.getClientId())
                .queryParam("redirect_uri", getRedirectUri(request.getRequestURL().toString()))
                .queryParam("response_type", "code")
                .queryParam("scope", String.join("+", getConfiguredScopes(clientRegistration)))
                .queryParam("state", state.toString());

        if (StringUtils.isNotBlank(identityServiceConfig.getAudience()))
        {
            builder.queryParam("audience", identityServiceConfig.getAudience());
        }

        return builder.build()
                .toUriString();
    }

    private Set<String> getConfiguredScopes(ClientRegistration clientRegistration)
    {
        return Optional.ofNullable(clientRegistration.getProviderDetails())
                .map(ProviderDetails::getConfigurationMetadata)
                .map(metadata -> metadata.get(SCOPES_SUPPORTED.getValue()))
                .filter(Scope.class::isInstance)
                .map(Scope.class::cast)
                .map(this::getSupportedScopes)
                .orElse(clientRegistration.getScopes());
    }

    private Set<String> getSupportedScopes(Scope scopes)
    {
        Set<String> configuredScopes = getConfiguredScopes();
        return scopes.stream()
                .map(Identifier::getValue)
                .filter(configuredScopes::contains)
                .collect(Collectors.toSet());
    }

    protected String buildRedirectUri(String requestURL, String overridePath)
    {
        try
        {
            URI originalUri = new URI(requestURL);
            String path = overridePath != null ? overridePath : originalUri.getPath();

            URI redirectUri = new URI(
                    originalUri.getScheme(),
                    originalUri.getAuthority(),
                    path,
                    originalUri.getQuery(),
                    originalUri.getFragment());

            return redirectUri.toASCIIString();
        }
        catch (URISyntaxException e)
        {
            LOGGER.error("Redirect URI construction failed: {}", e.getMessage(), e);
            throw new AuthenticationException(e.getMessage(), e);
        }
    }

    public void challenge(HttpServletRequest request, HttpServletResponse response)
    {
        try
        {
            response.sendRedirect(buildAuthRequestUrl(request));
        }
        catch (IOException e)
        {
            throw new AuthenticationException("Auth redirect failed", e);
        }
    }

    protected String retrieveTokenUsingAuthCode(HttpServletRequest request, HttpServletResponse response, String code)
    {
        try
        {
            AccessTokenAuthorization accessTokenAuthorization = identityServiceFacade.authorize(authorizationCode(code, getRedirectUri(request.getRequestURL()
                    .toString())));
            addCookies(response, accessTokenAuthorization);
            return accessTokenAuthorization.getAccessToken()
                    .getTokenValue();
        }
        catch (AuthorizationException exception)
        {
            LOGGER.warn("Error while trying to retrieve token using Authorization Code: {}", exception.getMessage());
            return null;
        }
    }

    protected String refreshTokenIfNeeded(HttpServletRequest request, HttpServletResponse response, String bearerToken)
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
                LOGGER.debug("Token refresh failed: {}", e.getMessage());
            }
            bearerToken = null;
            resetCookies(response);
        }

        return bearerToken;
    }

    private static boolean isAuthTokenExpired(String authTokenExpiration)
    {
        return authTokenExpiration == null || Instant.now()
                .compareTo(Instant.ofEpochMilli(Long.parseLong(authTokenExpiration))) >= 0;
    }

    private String refreshAuthToken(String refreshToken, HttpServletResponse response)
    {
        AccessTokenAuthorization accessTokenAuthorization = identityServiceFacade.authorize(AuthorizationGrant.refreshToken(refreshToken));
        if (accessTokenAuthorization == null || accessTokenAuthorization.getAccessToken() == null)
        {
            throw new AuthenticationException("Refresh token response is invalid.");
        }
        addCookies(response, accessTokenAuthorization);
        return accessTokenAuthorization.getAccessToken()
                .getTokenValue();

    }

    protected void addCookies(HttpServletResponse response, AccessTokenAuthorization accessTokenAuthorization)
    {
        cookiesService.addCookie(ALFRESCO_ACCESS_TOKEN, accessTokenAuthorization.getAccessToken()
                .getTokenValue(), response);
        cookiesService.addCookie(ALFRESCO_TOKEN_EXPIRATION, String.valueOf(accessTokenAuthorization.getAccessToken()
                .getExpiresAt()
                .toEpochMilli()), response);
        cookiesService.addCookie(ALFRESCO_REFRESH_TOKEN, accessTokenAuthorization.getRefreshTokenValue(), response);
    }

    protected void resetCookies(HttpServletResponse response)
    {
        cookiesService.resetCookie(ALFRESCO_TOKEN_EXPIRATION, response);
        cookiesService.resetCookie(ALFRESCO_ACCESS_TOKEN, response);
        cookiesService.resetCookie(ALFRESCO_REFRESH_TOKEN, response);
    }

    protected HttpServletRequest newRequestWrapper(Map<String, String> headers, HttpServletRequest request)
    {
        return new AdditionalHeadersHttpServletRequestWrapper(headers, request);
    }

    // Setters
    public void setIdentityServiceConfig(IdentityServiceConfig config)
    {
        this.identityServiceConfig = config;
    }

    public void setIdentityServiceFacade(IdentityServiceFacade facade)
    {
        this.identityServiceFacade = facade;
    }

    public void setCookiesService(AdminAuthenticationCookiesService service)
    {
        this.cookiesService = service;
    }

    public void setRemoteUserMapper(RemoteUserMapper mapper)
    {
        this.remoteUserMapper = mapper;
    }
}
