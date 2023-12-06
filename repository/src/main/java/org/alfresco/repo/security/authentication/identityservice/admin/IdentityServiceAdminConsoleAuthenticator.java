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
package org.alfresco.repo.security.authentication.identityservice.admin;

import static org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.AuthorizationGrant.authorizationCode;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.external.AdminConsoleAuthenticator;
import org.alfresco.repo.security.authentication.external.RemoteUserMapper;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.AccessTokenAuthorization;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.AuthorizationGrant;

public class IdentityServiceAdminConsoleAuthenticator implements AdminConsoleAuthenticator, ActivateableBean
{
    private static final String ALFRESCO_ACCESS_TOKEN = "ALFRESCO_ACCESS_TOKEN";
    private static final String ALFRESCO_REFRESH_TOKEN = "ALFRESCO_REFRESH_TOKEN";
    private static final String ALFRESCO_TOKEN_EXPIRATION = "ALFRESCO_TOKEN_EXPIRATION";
    private IdentityServiceFacade identityServiceFacade;
    private AdminConsoleAuthenticationCookiesService cookiesService;
    private RemoteUserMapper remoteUserMapper;
    private boolean isEnabled;

    @Override
    public String getAdminConsoleUser(HttpServletRequest request, HttpServletResponse response)
    {
        String bearerToken = cookiesService.getCookie(ALFRESCO_ACCESS_TOKEN, request);
        String refreshToken = cookiesService.getCookie(ALFRESCO_REFRESH_TOKEN, request);
        String authTokenExpiration = cookiesService.getCookie(ALFRESCO_TOKEN_EXPIRATION, request);
        if (bearerToken != null)
        {
            try
            {
                if (isAuthTokenExpired(authTokenExpiration))
                {
                    bearerToken = refreshAuthToken(refreshToken, response);
                }
            }
            catch (Exception e)
            {
                bearerToken = null;
                resetCookies(response);
            }
        }
        else
        {
            String code = request.getParameter("code");
            if (code != null)
            {
                AccessTokenAuthorization accessTokenAuthorization = identityServiceFacade.authorize(
                    authorizationCode(code, request.getRequestURL().toString()));
                bearerToken = addCookies(response, accessTokenAuthorization);
            }
        }

        if (bearerToken == null)
        {
            try
            {
                response.sendRedirect(getAuthenticationRequest(request));
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
            return null;
        }

        return remoteUserMapper.getRemoteUser(decorateBearerHeader(bearerToken, request));
    }

    private String addCookies(HttpServletResponse response, AccessTokenAuthorization accessTokenAuthorization)
    {
        String bearerToken = accessTokenAuthorization.getAccessToken().getTokenValue();
        cookiesService.addCookie(ALFRESCO_ACCESS_TOKEN, bearerToken, response);
        cookiesService.addCookie(ALFRESCO_TOKEN_EXPIRATION, String.valueOf(
            accessTokenAuthorization.getAccessToken().getExpiresAt().toEpochMilli()), response);
        cookiesService.addCookie(ALFRESCO_REFRESH_TOKEN, accessTokenAuthorization.getRefreshTokenValue(), response);
        return bearerToken;
    }

    private String getAuthenticationRequest(HttpServletRequest request)
    {
        return identityServiceFacade.getClientRegistration().getProviderDetails().getAuthorizationUri()
            + "?client_id="
            + identityServiceFacade.getClientRegistration().getClientId()
            + "&redirect_uri="
            + request.getRequestURL()
            + "&response_type=code"
            + "&scope=openid";
    }

    private void resetCookies(HttpServletResponse response)
    {
        cookiesService.resetCookie(ALFRESCO_TOKEN_EXPIRATION, response);
        cookiesService.resetCookie(ALFRESCO_ACCESS_TOKEN, response);
        cookiesService.resetCookie(ALFRESCO_REFRESH_TOKEN, response);
    }

    private String refreshAuthToken(String refreshToken, HttpServletResponse response)
    {
        return addCookies(response, doRefreshAuthToken(refreshToken));
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
