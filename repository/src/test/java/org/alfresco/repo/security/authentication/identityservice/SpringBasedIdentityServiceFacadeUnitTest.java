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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.AuthorizationException;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.AuthorizationGrant;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.TokenDecodingException;
import org.alfresco.repo.security.authentication.identityservice.user.UserInfoAttrMapping;

public class SpringBasedIdentityServiceFacadeUnitTest
{
    private static final String USER_NAME = "user";
    private static final String PASSWORD = "password";
    private static final String TOKEN = "tEsT-tOkEn";
    private static final UserInfoAttrMapping USER_INFO_ATTR_MAPPING = new UserInfoAttrMapping("preferred_username", "given_name", "family_name", "email");

    @Test
    public void shouldThrowVerificationExceptionOnFailure()
    {
        final RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.getErrorHandler()).thenReturn(new DefaultResponseErrorHandler());
        final JwtDecoder jwtDecoder = mock(JwtDecoder.class);

        final SpringBasedIdentityServiceFacade facade = new SpringBasedIdentityServiceFacade(restTemplate, testRegistration(), jwtDecoder);

        assertThatExceptionOfType(AuthorizationException.class)
                .isThrownBy(() -> facade.authorize(AuthorizationGrant.password(USER_NAME, PASSWORD)))
                .withMessageContaining("Failed to obtain access token");
    }

    @Test
    public void shouldThrowTokenExceptionOnFailure()
    {
        final RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.getErrorHandler()).thenReturn(new DefaultResponseErrorHandler());
        final JwtDecoder jwtDecoder = mock(JwtDecoder.class);
        when(jwtDecoder.decode(TOKEN)).thenThrow(new RuntimeException("Expected"));

        final SpringBasedIdentityServiceFacade facade = new SpringBasedIdentityServiceFacade(restTemplate, testRegistration(), jwtDecoder);

        assertThatExceptionOfType(TokenDecodingException.class)
                .isThrownBy(() -> facade.decodeToken(TOKEN))
                .havingCause().withNoCause().withMessage("Expected");
    }

    @Test
    public void shouldReturnEmptyOptionalOnFailure()
    {
        final RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.getErrorHandler()).thenReturn(new DefaultResponseErrorHandler());
        final JwtDecoder jwtDecoder = mock(JwtDecoder.class);
        final SpringBasedIdentityServiceFacade facade = new SpringBasedIdentityServiceFacade(restTemplate, testRegistration(), jwtDecoder);

        assertThat(facade.getUserInfo(TOKEN, USER_INFO_ATTR_MAPPING).isEmpty()).isTrue();

    }

    private ClientRegistration testRegistration()
    {
        return ClientRegistration.withRegistrationId("test")
                .tokenUri("http://localhost")
                .clientId("test")
                .clientSecret("test-secret")
                .userInfoUri("http://localhost/userinfo")
                .authorizationGrantType(AuthorizationGrantType.TOKEN_EXCHANGE)
                .build();
    }
}
