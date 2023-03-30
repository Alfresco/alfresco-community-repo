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

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.IdentityServiceFacadeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenDecoderFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.web.client.RestTemplate;

/**
 *
 * Creates an instance of {@link IdentityServiceFacade}. <br>
 * This factory can return a null if it is disabled.
 *
 */
public class IdentityServiceFacadeFactoryBean implements FactoryBean<IdentityServiceFacade>
{
    private static final Log LOGGER = LogFactory.getLog(IdentityServiceFacadeFactoryBean.class);
    private boolean enabled;
    private SpringBasedIdentityServiceFacadeFactory factory;

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public void setIdentityServiceConfig(IdentityServiceConfig identityServiceConfig)
    {
        factory = new SpringBasedIdentityServiceFacadeFactory(identityServiceConfig);
    }

    @Override
    public IdentityServiceFacade getObject() throws Exception
    {
        // The creation of the client can be disabled for testing or when the username/password authentication is not required,
        // for instance when Keycloak is configured for 'bearer only' authentication or Direct Access Grants are disabled.
        if (!enabled)
        {
            return null;
        }

        return new LazyInstantiatingIdentityServiceFacade(factory::createIdentityServiceFacade);
    }

    @Override
    public Class<?> getObjectType()
    {
        return IdentityServiceFacade.class;
    }

    @Override
    public boolean isSingleton()
    {
        return true;
    }

    private static IdentityServiceFacadeException authorizationServerCantBeUsedException(RuntimeException cause)
    {
        return new IdentityServiceFacadeException("Unable to use the Authorization Server.", cause);
    }

    // The target facade is created lazily to improve resiliency on Identity Service
    // (Keycloak/Authorization Server) failures when Spring Context is starting up.
    static class LazyInstantiatingIdentityServiceFacade implements IdentityServiceFacade
    {
        private final AtomicReference<IdentityServiceFacade> targetFacade = new AtomicReference<>();
        private final Supplier<IdentityServiceFacade> targetFacadeCreator;

        LazyInstantiatingIdentityServiceFacade(Supplier<IdentityServiceFacade> targetFacadeCreator)
        {
            this.targetFacadeCreator = requireNonNull(targetFacadeCreator);
        }

        @Override
        public AccessTokenAuthorization authorize(AuthorizationGrant grant) throws AuthorizationException
        {
            return getTargetFacade().authorize(grant);
        }

        @Override
        public DecodedAccessToken decodeToken(String token) throws TokenDecodingException
        {
            return getTargetFacade().decodeToken(token);
        }

        private IdentityServiceFacade getTargetFacade()
        {
            return ofNullable(targetFacade.get())
                    .orElseGet(() -> targetFacade.updateAndGet(prev ->
                            ofNullable(prev).orElseGet(this::createTargetFacade)));
        }

        private IdentityServiceFacade createTargetFacade()
        {
            try
            {
                return targetFacadeCreator.get();
            }
            catch (IdentityServiceFacadeException e)
            {
                throw e;
            }
            catch (RuntimeException e)
            {
                LOGGER.warn("Failed to instantiate IdentityServiceFacade.", e);
                throw authorizationServerCantBeUsedException(e);
            }
        }
    }

    private static class SpringBasedIdentityServiceFacadeFactory
    {
        private static final long CLOCK_SKEW_MS = 0;
        private final IdentityServiceConfig config;

        SpringBasedIdentityServiceFacadeFactory(IdentityServiceConfig config)
        {
            this.config = Objects.requireNonNull(config);
        }

        private IdentityServiceFacade createIdentityServiceFacade()
        {
            //Here we preserve the behaviour of previously used Keycloak Adapter
            // * Client is authenticating itself using basic auth
            // * Resource Owner Password Credentials Flow is used to authenticate Resource Owner

            final RestTemplate restTemplate = createRestTemplate();
            final ClientRegistration clientRegistration = createClientRegistration();
            final JwtDecoder jwtDecoder = createJwtDecoder(clientRegistration);

            return new SpringBasedIdentityServiceFacade(restTemplate, clientRegistration, jwtDecoder);
        }

        private RestTemplate createRestTemplate()
        {
            final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(config.getClientConnectionTimeout());
            requestFactory.setReadTimeout(config.getClientSocketTimeout());

            final RestTemplate restTemplate = new RestTemplate(Arrays.asList(new FormHttpMessageConverter(), new OAuth2AccessTokenResponseHttpMessageConverter()));
            restTemplate.setRequestFactory(requestFactory);
            restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());

            return restTemplate;
        }

        private ClientRegistration createClientRegistration()
        {
            try
            {
                return ClientRegistrations
                        .fromIssuerLocation(config.getIssuerUrl())
                        .clientId(config.getResource())
                        .clientSecret(config.getClientSecret())
                        .authorizationGrantType(AuthorizationGrantType.PASSWORD)
                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                        .registrationId("ids")
                        .build();
            }
            catch (RuntimeException e)
            {
                LOGGER.warn("Failed to create ClientRegistration.", e);
                throw authorizationServerCantBeUsedException(e);
            }
        }

        private JwtDecoder createJwtDecoder(ClientRegistration clientRegistration)
        {
            final OidcIdTokenDecoderFactory decoderFactory = new OidcIdTokenDecoderFactory();
            decoderFactory.setJwtValidatorFactory(c -> new DelegatingOAuth2TokenValidator<>(
                    new JwtTimestampValidator(Duration.of(CLOCK_SKEW_MS, ChronoUnit.MILLIS)),
                    new JwtIssuerValidator(c.getProviderDetails().getIssuerUri()),
                    new JwtClaimValidator<String>("typ", "Bearer"::equals),
                    new JwtClaimValidator<String>(JwtClaimNames.SUB, Objects::nonNull)

            ));
            try
            {
                return decoderFactory.createDecoder(clientRegistration);
            }
            catch (RuntimeException e)
            {
                LOGGER.warn("Failed to create JwtDecoder.", e);
                throw authorizationServerCantBeUsedException(e);
            }
        }
    }
}
