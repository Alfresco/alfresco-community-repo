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
import static java.util.function.Predicate.not;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.DefaultJWKSetCache;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.ResourceRetriever;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;

import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.IdentityServiceFacadeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenDecoderFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistration.Builder;
import org.springframework.security.oauth2.client.registration.ClientRegistration.ProviderDetails;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.converter.ClaimTypeConverter;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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
        factory = new SpringBasedIdentityServiceFacadeFactory(
                new HttpClientProvider(identityServiceConfig)::createHttpClient,
                new ClientRegistrationProvider(identityServiceConfig)::createClientRegistration,
                new JwtDecoderProvider(identityServiceConfig)::createJwtDecoder
        );
    }

    @Override
    public IdentityServiceFacade getObject() throws Exception
    {
        // The creation of the client can be disabled for testing or when the username/password authentication is not required,
        // for instance when Identity Service is configured for 'bearer only' authentication or Direct Access Grants are disabled.
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
        private final Supplier<HttpClient> httpClientProvider;
        private final Function<RestOperations, ClientRegistration> clientRegistrationProvider;
        private final BiFunction<RestOperations, ProviderDetails, JwtDecoder> jwtDecoderProvider;

        SpringBasedIdentityServiceFacadeFactory(
                Supplier<HttpClient> httpClientProvider,
                Function<RestOperations, ClientRegistration> clientRegistrationProvider,
                BiFunction<RestOperations, ProviderDetails, JwtDecoder> jwtDecoderProvider)
        {
            this.httpClientProvider = Objects.requireNonNull(httpClientProvider);
            this.clientRegistrationProvider = Objects.requireNonNull(clientRegistrationProvider);
            this.jwtDecoderProvider = Objects.requireNonNull(jwtDecoderProvider);
        }

        private IdentityServiceFacade createIdentityServiceFacade()
        {
            //Here we preserve the behaviour of previously used Keycloak Adapter
            // * Client is authenticating itself using basic auth
            // * Resource Owner Password Credentials Flow is used to authenticate Resource Owner

            final ClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClientProvider.get());
            final RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
            final ClientRegistration clientRegistration = clientRegistrationProvider.apply(restTemplate);
            final JwtDecoder jwtDecoder = jwtDecoderProvider.apply(restTemplate, clientRegistration.getProviderDetails());

            return new SpringBasedIdentityServiceFacade(createOAuth2RestTemplate(httpRequestFactory), clientRegistration, jwtDecoder);
        }

        private RestTemplate createOAuth2RestTemplate(ClientHttpRequestFactory requestFactory)
        {
            final RestTemplate restTemplate = new RestTemplate(Arrays.asList(new FormHttpMessageConverter(), new OAuth2AccessTokenResponseHttpMessageConverter()));
            restTemplate.setRequestFactory(requestFactory);
            restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());

            return restTemplate;
        }
    }

    private static class HttpClientProvider
    {
        private final IdentityServiceConfig config;

        private HttpClientProvider(IdentityServiceConfig config)
        {
            this.config = Objects.requireNonNull(config);
        }

        private HttpClient createHttpClient()
        {
            try
            {
                HttpClientBuilder clientBuilder = HttpClients.custom();
                applyConfiguration(clientBuilder);
                return clientBuilder.build();
            }
            catch (Exception e)
            {
                throw new IllegalStateException("Failed to create ClientHttpRequestFactory. " + e.getMessage(), e);
            }
        }

        private void applyConfiguration(HttpClientBuilder builder) throws Exception
        {
            final PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder = PoolingHttpClientConnectionManagerBuilder.create();

            applyConnectionConfiguration(connectionManagerBuilder);
            applySSLConfiguration(connectionManagerBuilder);

            builder.setConnectionManager(connectionManagerBuilder.build());
        }

        private void applyConnectionConfiguration(PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder)
        {
            final ConnectionConfig connectionConfig = ConnectionConfig.custom()
                    .setConnectTimeout(config.getClientConnectionTimeout(), TimeUnit.MILLISECONDS)
                    .setSocketTimeout(config.getClientSocketTimeout(), TimeUnit.MILLISECONDS)
                    .build();

            connectionManagerBuilder.setMaxConnTotal(config.getConnectionPoolSize());
            connectionManagerBuilder.setDefaultConnectionConfig(connectionConfig);
        }

        private void applySSLConfiguration(PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder) throws Exception
        {
            SSLContextBuilder sslContextBuilder = null;
            if (config.isDisableTrustManager())
            {
                sslContextBuilder = SSLContexts.custom()
                                               .loadTrustMaterial(TrustAllStrategy.INSTANCE);

            }
            else if (isDefined(config.getTruststore()))
            {
                final char[] truststorePassword = asCharArray(config.getTruststorePassword(), null);
                sslContextBuilder = SSLContexts.custom()
                                               .loadTrustMaterial(new File(config.getTruststore()), truststorePassword);
            }

            if (isDefined(config.getClientKeystore()))
            {
                if (sslContextBuilder == null)
                {
                    sslContextBuilder = SSLContexts.custom();
                }
                final char[] keystorePassword = asCharArray(config.getClientKeystorePassword(), null);
                final char[] keyPassword = asCharArray(config.getClientKeyPassword(), keystorePassword);
                sslContextBuilder.loadKeyMaterial(new File(config.getClientKeystore()), keystorePassword, keyPassword);
            }

            final SSLConnectionSocketFactoryBuilder sslConnectionSocketFactoryBuilder = SSLConnectionSocketFactoryBuilder.create();

            if (sslContextBuilder != null)
            {
                sslConnectionSocketFactoryBuilder.setSslContext(sslContextBuilder.build());
            }

            if (config.isDisableTrustManager() || config.isAllowAnyHostname())
            {
                sslConnectionSocketFactoryBuilder.setHostnameVerifier(NoopHostnameVerifier.INSTANCE);
            }
            final SSLConnectionSocketFactory sslConnectionSocketFactory = sslConnectionSocketFactoryBuilder.build();
            connectionManagerBuilder.setSSLSocketFactory(sslConnectionSocketFactory);
        }

        private char[] asCharArray(String value, char[] nullValue)
        {
            return Optional.ofNullable(value)
                           .filter(not(String::isBlank))
                           .map(String::toCharArray)
                           .orElse(nullValue);
        }
    }

    private static class ClientRegistrationProvider
    {
        private final IdentityServiceConfig config;

        private ClientRegistrationProvider(IdentityServiceConfig config)
        {
            this.config = Objects.requireNonNull(config);
        }

        public ClientRegistration createClientRegistration(final RestOperations rest)
        {
            return possibleMetadataURIs()
                    .stream()
                    .map(u -> extractMetadata(rest, u))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst()
                    .map(this::createBuilder)
                    .map(this::configureClientAuthentication)
                    .map(Builder::build)
                    .orElseThrow(() -> new IllegalStateException("Failed to create ClientRegistration."));
        }

        private ClientRegistration.Builder createBuilder(OIDCProviderMetadata metadata)
        {
            final String authUri = Optional.of(metadata)
                                           .map(OIDCProviderMetadata::getAuthorizationEndpointURI)
                                           .map(URI::toASCIIString)
                                           .orElse(null);
            return ClientRegistration
                    .withRegistrationId("ids")
                    .authorizationUri(authUri)
                    .tokenUri(metadata.getTokenEndpointURI().toASCIIString())
                    .jwkSetUri(metadata.getJWKSetURI().toASCIIString())
                    .issuerUri(config.getIssuerUrl())
                    .authorizationGrantType(AuthorizationGrantType.PASSWORD);
        }

        private Builder configureClientAuthentication(Builder builder)
        {
            builder.clientId(config.getResource());
            if (config.isPublicClient())
            {
                return builder.clientSecret(null)
                              .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST);
            }
            return builder.clientSecret(config.getClientSecret())
                          .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
        }

        private Optional<OIDCProviderMetadata> extractMetadata(RestOperations rest, URI metadataUri)
        {
            final String response;
            try
            {
                final ResponseEntity<String> r = rest.exchange(RequestEntity.get(metadataUri).build(), String.class);
                if (r.getStatusCode() != HttpStatus.OK || !r.hasBody())
                {
                    LOGGER.warn("Unexpected response from " + metadataUri + ". Status code: " + r.getStatusCode() + ", has body: " + r.hasBody() + ".");
                    return Optional.empty();
                }
                response = r.getBody();
            }
            catch (Exception e)
            {
                LOGGER.warn("Failed to get response from " + metadataUri + ". " + e.getMessage(), e);
                return Optional.empty();
            }
            try
            {
                return Optional.of(OIDCProviderMetadata.parse(response));
            }
            catch (Exception e)
            {
                LOGGER.warn("Failed to parse metadata. " + e.getMessage(), e);
                return Optional.empty();
            }
        }

        private Collection<URI> possibleMetadataURIs()
        {
            return List.of(UriComponentsBuilder.fromUriString(config.getIssuerUrl())
                                               .pathSegment(".well-known", "openid-configuration")
                                               .build().toUri());
        }
    }

    static class JwtDecoderProvider
    {
        private static final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.RS256;
        private final IdentityServiceConfig config;

        JwtDecoderProvider(IdentityServiceConfig config)
        {
            this.config = Objects.requireNonNull(config);
        }

        public JwtDecoder createJwtDecoder(RestOperations rest, ProviderDetails providerDetails)
        {
            try
            {
                final NimbusJwtDecoder decoder = buildJwtDecoder(rest, providerDetails);

                decoder.setJwtValidator(createJwtTokenValidator(providerDetails));
                decoder.setClaimSetConverter(new ClaimTypeConverter(OidcIdTokenDecoderFactory.createDefaultClaimTypeConverters()));

                return decoder;
            } catch (RuntimeException e)
            {
                LOGGER.warn("Failed to create JwtDecoder.", e);
                throw authorizationServerCantBeUsedException(e);
            }
        }

        private NimbusJwtDecoder buildJwtDecoder(RestOperations rest, ProviderDetails providerDetails)
        {
            if (isDefined(config.getRealmKey()))
            {
                final RSAPublicKey publicKey = parsePublicKey(config.getRealmKey());
                return NimbusJwtDecoder.withPublicKey(publicKey)
                                       .signatureAlgorithm(SIGNATURE_ALGORITHM)
                                       .build();
            }

            final String jwkSetUri = requireValidJwkSetUri(providerDetails);
            return NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
                                   .jwsAlgorithm(SIGNATURE_ALGORITHM)
                                   .restOperations(rest)
                                   .jwtProcessorCustomizer(this::reconfigureJWKSCache)
                                   .build();
        }

        private void reconfigureJWKSCache(ConfigurableJWTProcessor<SecurityContext> jwtProcessor)
        {
            final Optional<RemoteJWKSet<SecurityContext>> jwkSource = ofNullable(jwtProcessor)
                    .map(ConfigurableJWTProcessor::getJWSKeySelector)
                    .filter(JWSVerificationKeySelector.class::isInstance).map(o -> (JWSVerificationKeySelector<SecurityContext>)o)
                    .map(JWSVerificationKeySelector::getJWKSource)
                    .filter(RemoteJWKSet.class::isInstance).map(o -> (RemoteJWKSet<SecurityContext>)o);
            if (jwkSource.isEmpty())
            {
                LOGGER.warn("Not able to reconfigure the JWK Cache. Unexpected JWKSource.");
                return;
            }

            final Optional<URL> jwkSetUrl = jwkSource.map(RemoteJWKSet::getJWKSetURL);
            if (jwkSetUrl.isEmpty())
            {
                LOGGER.warn("Not able to reconfigure the JWK Cache. Unknown JWKSetURL.");
                return;
            }

            final Optional<ResourceRetriever> resourceRetriever = jwkSource.map(RemoteJWKSet::getResourceRetriever);
            if (resourceRetriever.isEmpty())
            {
                LOGGER.warn("Not able to reconfigure the JWK Cache. Unknown ResourceRetriever.");
                return;
            }

            final DefaultJWKSetCache cache = new DefaultJWKSetCache(config.getPublicKeyCacheTtl(), -1, TimeUnit.SECONDS);
            final JWKSource<SecurityContext> cachingJWKSource = new RemoteJWKSet<>(jwkSetUrl.get(), resourceRetriever.get(), cache);

            jwtProcessor.setJWSKeySelector(new JWSVerificationKeySelector<>(
                    JWSAlgorithm.parse(SIGNATURE_ALGORITHM.getName()),
                    cachingJWKSource));
        }

        private OAuth2TokenValidator<Jwt> createJwtTokenValidator(ProviderDetails providerDetails)
        {
            return new DelegatingOAuth2TokenValidator<>(
                    new JwtTimestampValidator(Duration.of(0, ChronoUnit.MILLIS)),
                    new JwtIssuerValidator(providerDetails.getIssuerUri()),
                    new JwtClaimValidator<String>("typ", "Bearer"::equals),
                    new JwtClaimValidator<String>(JwtClaimNames.SUB, Objects::nonNull));
        }

        private RSAPublicKey parsePublicKey(String pem)
        {
            try
            {
                return tryToParsePublicKey(pem);
            }
            catch (Exception e)
            {
                if (isPemFormatException(e))
                {
                    //For backward compatibility with Keycloak adapter
                    return tryToParsePublicKey("-----BEGIN PUBLIC KEY-----\n" + pem + "\n-----END PUBLIC KEY-----");
                }
                throw e;
            }
        }

        private RSAPublicKey tryToParsePublicKey(String pem)
        {
            final InputStream pemStream = new ByteArrayInputStream(pem.getBytes(StandardCharsets.UTF_8));
            return RsaKeyConverters.x509().convert(pemStream);
        }

        private boolean isPemFormatException(Exception e)
        {
            return e.getMessage() != null && e.getMessage().contains("-----BEGIN PUBLIC KEY-----");
        }

        private String requireValidJwkSetUri(ProviderDetails providerDetails)
        {
            final String uri = providerDetails.getJwkSetUri();
            if (!isDefined(uri)) {
                OAuth2Error oauth2Error = new OAuth2Error("missing_signature_verifier",
                        "Failed to find a Signature Verifier for: '"
                                + providerDetails.getIssuerUri()
                                + "'. Check to ensure you have configured the JwkSet URI.",
                        null);
                throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
            }
            return uri;
        }
    }

    private static boolean isDefined(String value)
    {
        return value != null && !value.isBlank();
    }
}
