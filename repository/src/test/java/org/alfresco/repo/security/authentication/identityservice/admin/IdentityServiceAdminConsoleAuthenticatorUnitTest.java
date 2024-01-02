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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.time.Instant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.alfresco.repo.security.authentication.external.RemoteUserMapper;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.AccessToken;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.AccessTokenAuthorization;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.AuthorizationException;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.AuthorizationGrant;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistration.ProviderDetails;

@SuppressWarnings("PMD.AvoidStringBufferField")
public class IdentityServiceAdminConsoleAuthenticatorUnitTest
{

    private static final String ALFRESCO_ACCESS_TOKEN = "ALFRESCO_ACCESS_TOKEN";
    private static final String ALFRESCO_REFRESH_TOKEN = "ALFRESCO_REFRESH_TOKEN";
    private static final String ALFRESCO_TOKEN_EXPIRATION = "ALFRESCO_TOKEN_EXPIRATION";

    @Mock
    HttpServletRequest request;
    @Mock
    HttpServletResponse response;
    @Mock
    IdentityServiceFacade identityServiceFacade;
    @Mock
    AdminConsoleAuthenticationCookiesService cookiesService;
    @Mock
    RemoteUserMapper remoteUserMapper;
    @Mock
    AccessTokenAuthorization accessTokenAuthorization;
    @Mock
    AccessToken accessToken;
    @Captor
    ArgumentCaptor<AdminConsoleHttpServletRequestWrapper> requestCaptor;

    IdentityServiceAdminConsoleAuthenticator authenticator;

    StringBuffer adminConsoleURL = new StringBuffer("http://localhost:8080/admin-console");

    @Before
    public void setup()
    {
        initMocks(this);
        ClientRegistration clientRegistration = mock(ClientRegistration.class);
        ProviderDetails providerDetails = mock(ProviderDetails.class);
        when(clientRegistration.getProviderDetails()).thenReturn(providerDetails);
        when(clientRegistration.getClientId()).thenReturn("alfresco");
        when(providerDetails.getAuthorizationUri()).thenReturn("http://localhost:8999/auth");
        when(identityServiceFacade.getClientRegistration()).thenReturn(clientRegistration);
        when(request.getRequestURL()).thenReturn(adminConsoleURL);
        when(remoteUserMapper.getRemoteUser(request)).thenReturn(null);

        authenticator = new IdentityServiceAdminConsoleAuthenticator();
        authenticator.setActive(true);
        authenticator.setIdentityServiceFacade(identityServiceFacade);
        authenticator.setCookiesService(cookiesService);
        authenticator.setRemoteUserMapper(remoteUserMapper);
    }

    @Test
    public void shouldCallRemoteMapperIfTokenIsInCookies()
    {
        when(cookiesService.getCookie(ALFRESCO_ACCESS_TOKEN, request)).thenReturn("JWT_TOKEN");
        when(cookiesService.getCookie(ALFRESCO_TOKEN_EXPIRATION, request)).thenReturn(
            String.valueOf(Instant.now().plusSeconds(60).toEpochMilli()));
        when(remoteUserMapper.getRemoteUser(requestCaptor.capture())).thenReturn("admin");

        String username = authenticator.getAdminConsoleUser(request, response);

        assertEquals("Bearer JWT_TOKEN", requestCaptor.getValue().getHeader("Authorization"));
        assertEquals("admin", username);
        assertTrue(authenticator.isActive());
    }

    @Test
    public void shouldRefreshExpiredTokenAndCallRemoteMapper()
    {
        when(cookiesService.getCookie(ALFRESCO_ACCESS_TOKEN, request)).thenReturn("EXPIRED_JWT_TOKEN");
        when(cookiesService.getCookie(ALFRESCO_REFRESH_TOKEN, request)).thenReturn("REFRESH_TOKEN");
        when(cookiesService.getCookie(ALFRESCO_TOKEN_EXPIRATION, request)).thenReturn(
            String.valueOf(Instant.now().minusSeconds(60).toEpochMilli()));
        when(accessToken.getTokenValue()).thenReturn("REFRESHED_JWT_TOKEN");
        when(accessToken.getExpiresAt()).thenReturn(Instant.now().plusSeconds(60));
        when(accessTokenAuthorization.getAccessToken()).thenReturn(accessToken);
        when(accessTokenAuthorization.getRefreshTokenValue()).thenReturn("REFRESH_TOKEN");
        when(identityServiceFacade.authorize(any(AuthorizationGrant.class))).thenReturn(accessTokenAuthorization);
        when(remoteUserMapper.getRemoteUser(requestCaptor.capture())).thenReturn("admin");

        String username = authenticator.getAdminConsoleUser(request, response);

        verify(cookiesService).addCookie(ALFRESCO_ACCESS_TOKEN, "REFRESHED_JWT_TOKEN", response);
        verify(cookiesService).addCookie(ALFRESCO_REFRESH_TOKEN, "REFRESH_TOKEN", response);
        assertEquals("Bearer REFRESHED_JWT_TOKEN", requestCaptor.getValue().getHeader("Authorization"));
        assertEquals("admin", username);
    }

    @Test
    public void shouldCallAuthChallenge() throws IOException
    {
        String authenticationRequest = "http://localhost:8999/auth?client_id=alfresco&redirect_uri=" + adminConsoleURL
            + "&response_type=code&scope=openid";
        authenticator.requestAuthentication(request, response);

        verify(response).sendRedirect(authenticationRequest);
    }

    @Test
    public void shouldResetCookiesAndCallAuthChallenge() throws IOException
    {
        when(cookiesService.getCookie(ALFRESCO_ACCESS_TOKEN, request)).thenReturn("EXPIRED_JWT_TOKEN");
        when(cookiesService.getCookie(ALFRESCO_REFRESH_TOKEN, request)).thenReturn("REFRESH_TOKEN");
        when(cookiesService.getCookie(ALFRESCO_TOKEN_EXPIRATION, request)).thenReturn(
            String.valueOf(Instant.now().minusSeconds(60).toEpochMilli()));

        when(identityServiceFacade.authorize(any(AuthorizationGrant.class))).thenThrow(AuthorizationException.class);

        String username = authenticator.getAdminConsoleUser(request, response);

        verify(cookiesService).resetCookie(ALFRESCO_ACCESS_TOKEN, response);
        verify(cookiesService).resetCookie(ALFRESCO_REFRESH_TOKEN, response);
        verify(cookiesService).resetCookie(ALFRESCO_TOKEN_EXPIRATION, response);
        assertNull(username);
    }

    @Test
    public void shouldAuthorizeCodeAndSetCookies()
    {
        when(request.getParameter("code")).thenReturn("auth_code");
        when(accessToken.getTokenValue()).thenReturn("JWT_TOKEN");
        when(accessToken.getExpiresAt()).thenReturn(Instant.now().plusSeconds(60));
        when(accessTokenAuthorization.getAccessToken()).thenReturn(accessToken);
        when(accessTokenAuthorization.getRefreshTokenValue()).thenReturn("REFRESH_TOKEN");
        when(identityServiceFacade.authorize(
            AuthorizationGrant.authorizationCode("auth_code", adminConsoleURL.toString())))
            .thenReturn(accessTokenAuthorization);
        when(remoteUserMapper.getRemoteUser(requestCaptor.capture())).thenReturn("admin");

        String username = authenticator.getAdminConsoleUser(request, response);

        verify(cookiesService).addCookie(ALFRESCO_ACCESS_TOKEN, "JWT_TOKEN", response);
        verify(cookiesService).addCookie(ALFRESCO_REFRESH_TOKEN, "REFRESH_TOKEN", response);
        assertEquals("Bearer JWT_TOKEN", requestCaptor.getValue().getHeader("Authorization"));
        assertEquals("admin", username);
    }

    @Test
    public void shouldExtractUsernameFromAuthorizationHeader()
    {
        when(remoteUserMapper.getRemoteUser(request)).thenReturn("admin");

        String username = authenticator.getAdminConsoleUser(request, response);

        assertEquals("admin", username);
    }
}
