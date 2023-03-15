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

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;

import org.alfresco.repo.security.authentication.identityservice.IdentityServiceAuthenticationComponent.OAuth2Client.CredentialsVerificationException;
import org.alfresco.repo.security.authentication.identityservice.OAuth2ClientFactoryBean.SpringOAuth2Client;
import org.junit.Test;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

public class SpringOAuth2ClientUnitTest
{
    private static final String USER_NAME = "user";
    private static final String PASSWORD = "password";

    @Test
    public void shouldRecoverFromInitialAuthorizationServerUnavailability()
    {
        final OAuth2AuthorizedClient authorizedClient = mock(OAuth2AuthorizedClient.class);
        when(authorizedClient.getAccessToken()).thenReturn(mock(OAuth2AccessToken.class));
        final OAuth2AuthorizedClientManager authClientManager = mock(OAuth2AuthorizedClientManager.class);
        when(authClientManager.authorize(any())).thenReturn(authorizedClient);

        final SpringOAuth2Client client = new SpringOAuth2Client(faultySupplier(3, authClientManager));

        assertThatExceptionOfType(CredentialsVerificationException.class)
                .isThrownBy(() -> client.verifyCredentials(USER_NAME, PASSWORD))
                .havingCause().withNoCause().withMessage("Expected failure #1");
        verifyNoInteractions(authClientManager);

        assertThatExceptionOfType(CredentialsVerificationException.class)
                .isThrownBy(() -> client.verifyCredentials(USER_NAME, PASSWORD))
                .havingCause().withNoCause().withMessage("Expected failure #2");
        verifyNoInteractions(authClientManager);

        assertThatExceptionOfType(CredentialsVerificationException.class)
                .isThrownBy(() -> client.verifyCredentials(USER_NAME, PASSWORD))
                .havingCause().withNoCause().withMessage("Expected failure #3");
        verifyNoInteractions(authClientManager);

        client.verifyCredentials(USER_NAME, PASSWORD);
        verify(authClientManager).authorize(argThat(r -> r.getPrincipal() != null && USER_NAME.equals(r.getPrincipal().getPrincipal())));
    }

    @Test
    public void shouldThrowVerificationExceptionOnFailure()
    {
        final OAuth2AuthorizedClientManager authClientManager = mock(OAuth2AuthorizedClientManager.class);
        when(authClientManager.authorize(any())).thenThrow(new RuntimeException("Expected"));

        final SpringOAuth2Client client = new SpringOAuth2Client(() -> authClientManager);

        assertThatExceptionOfType(CredentialsVerificationException.class)
                .isThrownBy(() -> client.verifyCredentials(USER_NAME, PASSWORD))
                .havingCause().withNoCause().withMessage("Expected");
    }

    @Test
    public void shouldAvoidCreatingMultipleInstanceOfOAuth2AuthorizedClientManager()
    {
        final OAuth2AuthorizedClient authorizedClient = mock(OAuth2AuthorizedClient.class);
        when(authorizedClient.getAccessToken()).thenReturn(mock(OAuth2AccessToken.class));
        final OAuth2AuthorizedClientManager authClientManager = mock(OAuth2AuthorizedClientManager.class);
        when(authClientManager.authorize(any())).thenReturn(authorizedClient);
        final Supplier<OAuth2AuthorizedClientManager> supplier = mock(Supplier.class);
        when(supplier.get()).thenReturn(authClientManager);

        final SpringOAuth2Client client = new SpringOAuth2Client(supplier);

        client.verifyCredentials(USER_NAME, PASSWORD);
        client.verifyCredentials(USER_NAME, PASSWORD);
        client.verifyCredentials(USER_NAME, PASSWORD);
        verify(supplier, times(1)).get();
        verify(authClientManager, times(3)).authorize(any());
    }

    private Supplier<OAuth2AuthorizedClientManager> faultySupplier(int numberOfInitialFailures, OAuth2AuthorizedClientManager authClientManager)
    {
        final int[] counter = new int[]{0};
        return () -> {
            if (counter[0]++ < numberOfInitialFailures)
            {
                throw new RuntimeException("Expected failure #" + counter[0]);
            }
            return authClientManager;
        };
    }

}