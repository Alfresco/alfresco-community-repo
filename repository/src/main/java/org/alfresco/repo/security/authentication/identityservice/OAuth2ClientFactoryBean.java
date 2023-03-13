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

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.alfresco.repo.security.authentication.identityservice.IdentityServiceAuthenticationComponent.OAuth2Client;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder.PasswordGrantBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.endpoint.DefaultPasswordTokenResponseClient;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 *
 * Creates an instance of {@link OAuth2Client}. <br>
 * The creation of {@link OAuth2Client} requires connection to the Identity Service (Keycloak), disable this factory if
 * the server cannot be reached. <br>
 * This factory can return a null if it is disabled.
 *
 */
public class OAuth2ClientFactoryBean implements FactoryBean<OAuth2Client>
{

    private static final Log LOGGER = LogFactory.getLog(OAuth2ClientFactoryBean.class);
    private IdentityServiceConfig identityServiceConfig;
    private boolean enabled;

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public void setIdentityServiceConfig(IdentityServiceConfig identityServiceConfig)
    {
        this.identityServiceConfig = identityServiceConfig;
    }

    @Override
    public OAuth2Client getObject() throws Exception
    {
        // The creation of the client can be disabled for testing or when the username/password authentication is not required,
        // for instance when Keycloak is configured for 'bearer only' authentication or Direct Access Grants are disabled.
        if (!enabled)
        {
            return null;
        }

        // The OAuth2AuthorizedClientManager isn't created upfront to make the code resilient to Identity Service being down.
        // If it's down the Application Context will start and when it's back online it can be used.
        return new SpringOAuth2Client(this::createOAuth2AuthorizedClientManager);
    }

    private OAuth2AuthorizedClientManager createOAuth2AuthorizedClientManager()
    {
        //Here we preserve the behaviour of previously used Keycloak Adapter
        // * Client is authenticating itself using basic auth
        // * Resource Owner Password Credentials Flow is used to authenticate Resource Owner
        // * There is no caching of authenticated clients (NoStoredAuthorizedClient)
        // * There is only one Authorization Server/Client pair (SingleClientRegistration)

        final ClientRegistration clientRegistration = ClientRegistrations
                .fromIssuerLocation(identityServiceConfig.getIssuerUrl())
                .clientId(identityServiceConfig.getResource())
                .clientSecret(identityServiceConfig.getClientSecret())
                .authorizationGrantType(AuthorizationGrantType.PASSWORD)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .registrationId(SpringOAuth2Client.CLIENT_REGISTRATION_ID)
                .build();

        final AuthorizedClientServiceOAuth2AuthorizedClientManager oauth2 =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        new SingleClientRegistration(clientRegistration),
                        new NoStoredAuthorizedClient());
        oauth2.setContextAttributesMapper(OAuth2AuthorizeRequest::getAttributes);
        oauth2.setAuthorizedClientProvider(OAuth2AuthorizedClientProviderBuilder.builder()
                                                                                .password(this::configureTimeouts)
                                                                                .build());

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug(" Created OAuth2 Client");
            LOGGER.debug(" OAuth2 Issuer URL: " + clientRegistration.getProviderDetails().getIssuerUri());
            LOGGER.debug(" OAuth2 ClientId: " + clientRegistration.getClientId());
        }

        return oauth2;
    }

    private void configureTimeouts(PasswordGrantBuilder builder)
    {
        final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(identityServiceConfig.getClientConnectionTimeout());
        requestFactory.setReadTimeout(identityServiceConfig.getClientSocketTimeout());

        final RestTemplate restTemplate = new RestTemplate(Arrays.asList(new FormHttpMessageConverter(), new OAuth2AccessTokenResponseHttpMessageConverter()));
        restTemplate.setRequestFactory(requestFactory);
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());

        final DefaultPasswordTokenResponseClient client = new DefaultPasswordTokenResponseClient();
        client.setRestOperations(restTemplate);

        builder.accessTokenResponseClient(client);
    }

    @Override
    public Class<?> getObjectType()
    {
        return OAuth2Client.class;
    }

    @Override
    public boolean isSingleton()
    {
        return true;
    }

    static class SpringOAuth2Client implements OAuth2Client
    {
        private static final String CLIENT_REGISTRATION_ID = "ids";
        private final Supplier<OAuth2AuthorizedClientManager> authorizedClientManagerSupplier;
        private final AtomicReference<OAuth2AuthorizedClientManager> authorizedClientManager = new AtomicReference<>();

        public SpringOAuth2Client(Supplier<OAuth2AuthorizedClientManager> authorizedClientManagerSupplier)
        {
            this.authorizedClientManagerSupplier = Objects.requireNonNull(authorizedClientManagerSupplier);
        }

        @Override
        public void verifyCredentials(String userName, String password)
        {
            final OAuth2AuthorizedClientManager clientManager;
            try
            {
                clientManager = getAuthorizedClientManager();
            }
            catch (RuntimeException e)
            {
                LOGGER.warn("Failed to instantiate OAuth2AuthorizedClientManager.", e);
                throw new CredentialsVerificationException("Unable to use the Authorization Server.", e);
            }

            final OAuth2AuthorizedClient authorizedClient;
            try
            {
                final OAuth2AuthorizeRequest authRequest = createPasswordCredentialsRequest(userName, password);
                authorizedClient = clientManager.authorize(authRequest);
            }
            catch (OAuth2AuthorizationException e)
            {
                LOGGER.debug("Failed to authorize against Authorization Server. Reason: " + e.getError() + ".");
                throw new CredentialsVerificationException("Authorization against the Authorization Server failed with " + e.getError() + ".", e);
            }
            catch (RuntimeException e)
            {
                LOGGER.warn("Failed to authorize against Authorization Server. Reason: " + e.getMessage());
                throw new CredentialsVerificationException("Failed to authorize against Authorization Server.", e);
            }

            if (authorizedClient == null || authorizedClient.getAccessToken() == null)
            {
                throw new CredentialsVerificationException("Resource Owner Password Credentials is not supported by the Authorization Server.");
            }
        }

        private OAuth2AuthorizedClientManager getAuthorizedClientManager()
        {
            final OAuth2AuthorizedClientManager current = authorizedClientManager.get();
            if (current != null)
            {
                return current;
            }
            return authorizedClientManager
                    .updateAndGet(prev -> prev != null ? prev : authorizedClientManagerSupplier.get());
        }

        private OAuth2AuthorizeRequest createPasswordCredentialsRequest(String userName, String password)
        {
            return OAuth2AuthorizeRequest
                    .withClientRegistrationId(CLIENT_REGISTRATION_ID)
                    .principal(userName)
                    .attribute(OAuth2AuthorizationContext.USERNAME_ATTRIBUTE_NAME, userName)
                    .attribute(OAuth2AuthorizationContext.PASSWORD_ATTRIBUTE_NAME, password)
                    .build();
        }
    }

    private static class NoStoredAuthorizedClient implements OAuth2AuthorizedClientService
    {

        @Override
        public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String clientRegistrationId, String principalName)
        {
            return null;
        }

        @Override
        public void saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient, Authentication principal)
        {
            //do nothing
        }

        @Override
        public void removeAuthorizedClient(String clientRegistrationId, String principalName)
        {
            //do nothing
        }
    }

    private static class SingleClientRegistration implements ClientRegistrationRepository
    {
        private final ClientRegistration clientRegistration;

        private SingleClientRegistration(ClientRegistration clientRegistration)
        {
            this.clientRegistration = Objects.requireNonNull(clientRegistration);
        }

        @Override
        public ClientRegistration findByRegistrationId(String registrationId)
        {
            return Objects.equals(registrationId, clientRegistration.getRegistrationId()) ? clientRegistration : null;
        }
    }
}
