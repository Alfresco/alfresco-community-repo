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
package org.alfresco.repo.security.authentication.identityservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;

import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacadeFactoryBean.ClientRegistrationProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.web.client.RestTemplate;

public class ClientRegistrationProviderUnitTest
{
    private final String CLIENT_ID = "alfresco";
    private final String OPENID_CONFIGURATION = "{\"token_endpoint\":\"https://login.serviceonline.com/common/oauth2/v2.0/token\",\"token_endpoint_auth_methods_supported\":[\"client_secret_post\",\"private_key_jwt\",\"client_secret_basic\"],\"jwks_uri\":\"https://login.serviceonline.com/common/discovery/v2.0/keys\",\"response_modes_supported\":[\"query\",\"fragment\",\"form_post\"],\"subject_types_supported\":[\"pairwise\"],\"id_token_signing_alg_values_supported\":[\"RS256\"],\"response_types_supported\":[\"code\",\"id_token\",\"code id_token\",\"id_token token\"],\"scopes_supported\":[\"openid\",\"profile\",\"email\",\"offline_access\"],\"issuer\":\"https://login.serviceonline.com/alfresco/v2.0\",\"request_uri_parameter_supported\":false,\"userinfo_endpoint\":\"https://graph.service.com/oidc/userinfo\",\"authorization_endpoint\":\"https://login.serviceonline.com/common/oauth2/v2.0/authorize\",\"device_authorization_endpoint\":\"https://login.serviceonline.com/common/oauth2/v2.0/devicecode\",\"http_logout_supported\":true,\"frontchannel_logout_supported\":true,\"end_session_endpoint\":\"https://login.serviceonline.com/common/oauth2/v2.0/logout\",\"claims_supported\":[\"sub\",\"iss\",\"cloud_instance_name\",\"cloud_instance_host_name\",\"cloud_graph_host_name\",\"msgraph_host\",\"aud\",\"exp\",\"iat\",\"auth_time\",\"acr\",\"nonce\",\"preferred_username\",\"name\",\"tid\",\"ver\",\"at_hash\",\"c_hash\",\"email\"],\"kerberos_endpoint\":\"https://login.serviceonline.com/common/kerberos\",\"tenant_region_scope\":null,\"cloud_instance_name\":\"serviceonline.com\",\"cloud_graph_host_name\":\"graph.oidc.net\",\"msgraph_host\":\"graph.service.com\",\"rbac_url\":\"https://pas.oidc.net\"}";
    private final String DISCOVERY_PATH_SEGEMENTS = "/.well-known/openid-configuration";
    private final String AUTH_SERVER = "https://login.serviceonline.com";

    private IdentityServiceConfig config;
    private RestTemplate restTemplate;
    private OIDCProviderMetadata oidcResponse;

    private ArgumentCaptor<RequestEntity> requestEntityCaptor = ArgumentCaptor.forClass(RequestEntity.class);

    @Before
    public void setup() throws ParseException
    {
        config = new IdentityServiceConfig();
        config.setAuthServerUrl(AUTH_SERVER);
        config.setResource(CLIENT_ID);

        restTemplate = mock(RestTemplate.class);
        ResponseEntity responseEntity = mock(ResponseEntity.class);
        when(restTemplate.exchange(requestEntityCaptor.capture(), eq(String.class))).thenReturn(responseEntity);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(responseEntity.hasBody()).thenReturn(true);
        when(responseEntity.getBody()).thenReturn("");

        oidcResponse = spy(OIDCProviderMetadata.parse(OPENID_CONFIGURATION));
    }

    @Test
    public void shouldCreateClientRegistration()
    {
        config.setIssuerUrl("https://login.serviceonline.com/alfresco/v2.0");
        try (MockedStatic<OIDCProviderMetadata> providerMetadata = Mockito.mockStatic(OIDCProviderMetadata.class))
        {
            providerMetadata.when(() -> OIDCProviderMetadata.parse(any(String.class))).thenReturn(oidcResponse);

            ClientRegistration clientRegistration = new ClientRegistrationProvider(config).createClientRegistration(
                restTemplate);
            assertThat(clientRegistration).isNotNull();
            assertThat(clientRegistration.getClientId()).isNotNull();
            assertThat(clientRegistration.getProviderDetails().getAuthorizationUri()).isNotNull();
            assertThat(clientRegistration.getProviderDetails().getTokenUri()).isNotNull();
            assertThat(clientRegistration.getProviderDetails().getJwkSetUri()).isNotNull();
            assertThat(clientRegistration.getProviderDetails().getUserInfoEndpoint()).isNotNull();
            assertThat(clientRegistration.getProviderDetails().getIssuerUri()).isNotNull();
            assertThat(requestEntityCaptor.getValue().getUrl().toASCIIString()).isEqualTo(
                AUTH_SERVER + DISCOVERY_PATH_SEGEMENTS);
        }
    }

    @Test
    public void shouldCreateClientRegistrationWithoutIssuerConfigured()
    {
        config.setIssuerUrl(null);
        try (MockedStatic<OIDCProviderMetadata> providerMetadata = Mockito.mockStatic(OIDCProviderMetadata.class))
        {
            providerMetadata.when(() -> OIDCProviderMetadata.parse(any(String.class))).thenReturn(oidcResponse);

            ClientRegistration clientRegistration = new ClientRegistrationProvider(config).createClientRegistration(
                restTemplate);
            assertThat(clientRegistration).isNotNull();
            assertThat(clientRegistration.getClientId()).isNotNull();
            assertThat(clientRegistration.getProviderDetails().getAuthorizationUri()).isNotNull();
            assertThat(clientRegistration.getProviderDetails().getTokenUri()).isNotNull();
            assertThat(clientRegistration.getProviderDetails().getJwkSetUri()).isNotNull();
            assertThat(clientRegistration.getProviderDetails().getUserInfoEndpoint()).isNotNull();
            assertThat(clientRegistration.getProviderDetails().getIssuerUri()).isNotNull();
            assertThat(requestEntityCaptor.getValue().getUrl().toASCIIString()).isEqualTo(
                AUTH_SERVER + DISCOVERY_PATH_SEGEMENTS);
        }
    }

    @Test
    public void shouldThrowIdentityServiceExceptionIfIssuerIsNotValid()
    {
        config.setIssuerUrl("https://invalidissuer.com");
        try (MockedStatic<OIDCProviderMetadata> providerMetadata = Mockito.mockStatic(OIDCProviderMetadata.class))
        {
            providerMetadata.when(() -> OIDCProviderMetadata.parse(any(String.class))).thenReturn(oidcResponse);

            assertThrows(IdentityServiceException.class,
                () -> new ClientRegistrationProvider(config).createClientRegistration(restTemplate));
        }
    }

    @Test
    public void shouldThrowIdentityServiceExceptionIfIssuerIsNull()
    {
        try (MockedStatic<OIDCProviderMetadata> providerMetadata = Mockito.mockStatic(OIDCProviderMetadata.class))
        {
            when(oidcResponse.getIssuer()).thenReturn(null);

            providerMetadata.when(() -> OIDCProviderMetadata.parse(any(String.class))).thenReturn(oidcResponse);

            assertThrows(IdentityServiceException.class,
                () -> new ClientRegistrationProvider(config).createClientRegistration(restTemplate));
        }
    }

    @Test
    public void shouldThrowIdentityServiceExceptionIfTokenEndpointIsNull()
    {
        try (MockedStatic<OIDCProviderMetadata> providerMetadata = Mockito.mockStatic(OIDCProviderMetadata.class))
        {
            when(oidcResponse.getTokenEndpointURI()).thenReturn(null);
            providerMetadata.when(() -> OIDCProviderMetadata.parse(any(String.class))).thenReturn(oidcResponse);

            assertThrows(IdentityServiceException.class,
                () -> new ClientRegistrationProvider(config).createClientRegistration(restTemplate));
        }
    }

    @Test
    public void shouldThrowIdentityServiceExceptionIfAuthorizationEndpointIsNull()
    {
        try (MockedStatic<OIDCProviderMetadata> providerMetadata = Mockito.mockStatic(OIDCProviderMetadata.class))
        {
            when(oidcResponse.getAuthorizationEndpointURI()).thenReturn(null);
            providerMetadata.when(() -> OIDCProviderMetadata.parse(any(String.class))).thenReturn(oidcResponse);

            assertThrows(IdentityServiceException.class,
                () -> new ClientRegistrationProvider(config).createClientRegistration(restTemplate));
        }
    }

    @Test
    public void shouldThrowIdentityServiceExceptionIfUserInfoEndpointIsNull()
    {
        try (MockedStatic<OIDCProviderMetadata> providerMetadata = Mockito.mockStatic(OIDCProviderMetadata.class))
        {
            when(oidcResponse.getUserInfoEndpointURI()).thenReturn(null);
            providerMetadata.when(() -> OIDCProviderMetadata.parse(any(String.class))).thenReturn(oidcResponse);

            assertThrows(IdentityServiceException.class,
                () -> new ClientRegistrationProvider(config).createClientRegistration(restTemplate));
        }
    }

    @Test
    public void shouldThrowIdentityServiceExceptionIfJWKSetEndpointIsNull()
    {
        try (MockedStatic<OIDCProviderMetadata> providerMetadata = Mockito.mockStatic(OIDCProviderMetadata.class))
        {
            when(oidcResponse.getJWKSetURI()).thenReturn(null);
            providerMetadata.when(() -> OIDCProviderMetadata.parse(any(String.class))).thenReturn(oidcResponse);

            assertThrows(IdentityServiceException.class,
                () -> new ClientRegistrationProvider(config).createClientRegistration(restTemplate));
        }
    }

    @Test
    public void shouldCreateDiscoveryEndpointWithRealm()
    {
        config.setRealm("alfresco");
        config.setIssuerUrl("https://login.serviceonline.com/alfresco/v2.0");
        try (MockedStatic<OIDCProviderMetadata> providerMetadata = Mockito.mockStatic(OIDCProviderMetadata.class))
        {
            providerMetadata.when(() -> OIDCProviderMetadata.parse(any(String.class))).thenReturn(oidcResponse);

            new ClientRegistrationProvider(config).createClientRegistration(restTemplate);
            assertThat(requestEntityCaptor.getValue().getUrl().toASCIIString()).isEqualTo(
                AUTH_SERVER + "/realms/alfresco" + DISCOVERY_PATH_SEGEMENTS);
        }
    }

    @Test
    public void shouldSetAllSupportedScopes()
    {
        try (MockedStatic<OIDCProviderMetadata> providerMetadata = Mockito.mockStatic(OIDCProviderMetadata.class))
        {
            providerMetadata.when(() -> OIDCProviderMetadata.parse(any(String.class))).thenReturn(oidcResponse);

            ClientRegistration clientRegistration = new ClientRegistrationProvider(config).createClientRegistration(
                restTemplate);
            assertThat(
                clientRegistration.getScopes().containsAll(ClientRegistrationProvider.SUPPORTED_SCOPES)).isTrue();
        }
    }

    @Test
    public void shouldSetOneSupportedScope()
    {
        try (MockedStatic<OIDCProviderMetadata> providerMetadata = Mockito.mockStatic(OIDCProviderMetadata.class))
        {
            when(oidcResponse.getScopes()).thenReturn(new Scope("openid"));
            providerMetadata.when(() -> OIDCProviderMetadata.parse(any(String.class))).thenReturn(oidcResponse);

            ClientRegistration clientRegistration = new ClientRegistrationProvider(config).createClientRegistration(
                restTemplate);
            assertThat(clientRegistration.getScopes().size()).isEqualTo(1);
            assertThat(clientRegistration.getScopes().stream().findFirst().get()).isEqualTo("openid");
        }
    }

    @Test
    public void shouldCreateDiscoveryEndpointFromIssuer()
    {
        config.setAuthServerUrl(null);
        config.setIssuerUrl("https://login.serviceonline.com/alfresco/v2.0");
        try (MockedStatic<OIDCProviderMetadata> providerMetadata = Mockito.mockStatic(OIDCProviderMetadata.class))
        {
            providerMetadata.when(() -> OIDCProviderMetadata.parse(any(String.class))).thenReturn(oidcResponse);

            new ClientRegistrationProvider(config).createClientRegistration(restTemplate);
            assertThat(requestEntityCaptor.getValue().getUrl().toASCIIString()).isEqualTo(
                "https://login.serviceonline.com/alfresco/v2.0" + DISCOVERY_PATH_SEGEMENTS);
        }
    }
}
