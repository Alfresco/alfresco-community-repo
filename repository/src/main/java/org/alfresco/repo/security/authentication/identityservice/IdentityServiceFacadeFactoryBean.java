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

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;

import static org.alfresco.repo.security.authentication.identityservice.IdentityServiceMetadataKey.AUDIENCE;
import static org.alfresco.repo.security.authentication.identityservice.IdentityServiceMetadataKey.SCOPES_SUPPORTED;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSetBasedJWKSource;
import com.nimbusds.jose.jwk.source.JWKSetSource;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.Identifier;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.openid.connect.sdk.claims.PersonClaims;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
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
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.core.converter.ClaimTypeConverter;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.IdentityServiceFacadeException;
import org.alfresco.repo.security.authentication.identityservice.user.DecodedTokenUser;
import org.alfresco.repo.security.authentication.identityservice.user.UserInfoAttrMapping;

/**
 * Creates an instance of {@link IdentityServiceFacade}. <br>
 * This factory can return a null if it is disabled.
 */
public class IdentityServiceFacadeFactoryBean implements FactoryBean<IdentityServiceFacade>
{
    private static final Log LOGGER = LogFactory.getLog(IdentityServiceFacadeFactoryBean.class);

    private static final JOSEObjectType AT_JWT = new JOSEObjectType("at+jwt");
    private static final String DEFAULT_ISSUER_ATTR = "issuer";

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
                new JwtDecoderProvider(identityServiceConfig)::createJwtDecoder);
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

        @Override
        public Optional<DecodedTokenUser> getUserInfo(String token, UserInfoAttrMapping userInfoAttrMapping)
        {
            return getTargetFacade().getUserInfo(token, userInfoAttrMapping);
        }

        @Override
        public ClientRegistration getClientRegistration()
        {
            return getTargetFacade().getClientRegistration();
        }

        private IdentityServiceFacade getTargetFacade()
        {
            return ofNullable(targetFacade.get())
                    .orElseGet(() -> targetFacade.updateAndGet(prev -> ofNullable(prev).orElseGet(this::createTargetFacade)));
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
            this.httpClientProvider = requireNonNull(httpClientProvider);
            this.clientRegistrationProvider = requireNonNull(clientRegistrationProvider);
            this.jwtDecoderProvider = requireNonNull(jwtDecoderProvider);
        }

        private IdentityServiceFacade createIdentityServiceFacade()
        {
            // Here we preserve the behaviour of previously used Keycloak Adapter
            // * Client is authenticating itself using basic auth
            // * Resource Owner Password Credentials Flow is used to authenticate Resource Owner

            final ClientHttpRequestFactory httpRequestFactory = new CustomClientHttpRequestFactory(
                    httpClientProvider.get());
            final RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
            final ClientRegistration clientRegistration = clientRegistrationProvider.apply(restTemplate);
            final JwtDecoder jwtDecoder = jwtDecoderProvider.apply(restTemplate,
                    clientRegistration.getProviderDetails());

            return new SpringBasedIdentityServiceFacade(createOAuth2RestTemplate(httpRequestFactory),
                    clientRegistration, jwtDecoder);
        }

        private RestTemplate createOAuth2RestTemplate(ClientHttpRequestFactory requestFactory)
        {
            final RestTemplate restTemplate = new RestTemplate(
                    Arrays.asList(new FormHttpMessageConverter(), new OAuth2AccessTokenResponseHttpMessageConverter(), new MappingJackson2HttpMessageConverter()));
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
            this.config = requireNonNull(config);
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

        private void applySSLConfiguration(PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder)
                throws Exception
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

        private char[] asCharArray(String value, char... nullValue)
        {
            return ofNullable(value)
                    .filter(not(String::isBlank))
                    .map(String::toCharArray)
                    .orElse(nullValue);
        }
    }

    static class ClientRegistrationProvider
    {
        private final IdentityServiceConfig config;

        ClientRegistrationProvider(IdentityServiceConfig config)
        {
            this.config = requireNonNull(config);
        }

        public ClientRegistration createClientRegistration(final RestOperations rest)
        {
            return possibleMetadataURIs()
                    .stream()
                    .map(u -> extractMetadata(rest, u))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst()
                    .map(this::validateDiscoveryDocument)
                    .map(this::createBuilder)
                    .map(this::configureClientAuthentication)
                    .map(Builder::build)
                    .orElseThrow(() -> new IllegalStateException("Failed to create ClientRegistration."));
        }

        private OIDCProviderMetadata validateDiscoveryDocument(OIDCProviderMetadata metadata)
        {
            validateOIDCEndpoint(metadata.getTokenEndpointURI(), "Token");
            validateOIDCEndpoint(metadata.getAuthorizationEndpointURI(), "Authorization");
            validateOIDCEndpoint(metadata.getUserInfoEndpointURI(), "User Info");
            validateOIDCEndpoint(metadata.getJWKSetURI(), "JWK Set");

            if (metadata.getIssuer() != null)
            {
                try
                {
                    URI metadataIssuerURI = new URI(metadata.getIssuer().getValue());
                    validateOIDCEndpoint(metadataIssuerURI, "Issuer");
                    if (StringUtils.isNotBlank(config.getIssuerUrl()) &&
                            !metadataIssuerURI.equals(URI.create(config.getIssuerUrl())))
                    {
                        throw new IdentityServiceException("Failed to create ClientRegistration. "
                                + "The Issuer value from the OIDC Discovery Endpoint does not align with the provided Issuer. Expected `%s` but found `%s`"
                                        .formatted(config.getIssuerUrl(), metadata.getIssuer().getValue()));
                    }
                }
                catch (URISyntaxException e)
                {
                    throw new IdentityServiceException("The provided Issuer value could not be parsed as a URI reference.", e);
                }
            }
            else
            {
                throw new IdentityServiceException("The Issuer retrieved from the OIDC Discovery Endpoint cannot be null.");
            }

            return metadata;
        }

        private void validateOIDCEndpoint(URI value, String endpointName)
        {
            if (value == null || value.toASCIIString().isBlank())
            {
                throw new IdentityServiceException("The `%s` Endpoint retrieved from the OIDC Discovery Endpoint cannot be empty.".formatted(endpointName));
            }
        }

        private ClientRegistration.Builder createBuilder(OIDCProviderMetadata metadata)
        {
            final String authUri = Optional.of(metadata)
                    .map(OIDCProviderMetadata::getAuthorizationEndpointURI)
                    .map(URI::toASCIIString)
                    .orElse(null);

            var metadataIssuer = getMetadataIssuer(metadata, config);
            final String issuerUri = metadataIssuer
                    .orElseGet(() -> (StringUtils.isNotBlank(config.getRealm()) && StringUtils.isBlank(config.getIssuerUrl())) ? config.getAuthServerUrl() : config.getIssuerUrl());

            final var usernameAttribute = StringUtils.isNotBlank(config.getPrincipalAttribute()) ? config.getPrincipalAttribute() : PersonClaims.PREFERRED_USERNAME_CLAIM_NAME;

            return ClientRegistration
                    .withRegistrationId("ids")
                    .authorizationUri(authUri)
                    .tokenUri(metadata.getTokenEndpointURI().toASCIIString())
                    .jwkSetUri(metadata.getJWKSetURI().toASCIIString())
                    .issuerUri(issuerUri)
                    .userInfoUri(metadata.getUserInfoEndpointURI().toASCIIString())
                    .userNameAttributeName(usernameAttribute)
                    .scope(getSupportedScopes(metadata.getScopes()))
                    .providerConfigurationMetadata(createMetadata(metadata))
                    .authorizationGrantType(AuthorizationGrantType.TOKEN_EXCHANGE);
        }

        private Map<String, Object> createMetadata(OIDCProviderMetadata metadata)
        {
            Map<String, Object> configurationMetadata = new LinkedHashMap<>();
            if (metadata.getScopes() != null)
            {
                configurationMetadata.put(SCOPES_SUPPORTED.getValue(), metadata.getScopes());
            }
            if (StringUtils.isNotBlank(config.getAudience()))
            {
                configurationMetadata.put(AUDIENCE.getValue(), config.getAudience());
            }
            return configurationMetadata;
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

        private Set<String> getSupportedScopes(Scope scopes)
        {
            return scopes.stream()
                    .filter(this::hasPasswordGrantScope)
                    .map(Identifier::getValue)
                    .collect(Collectors.toSet());
        }

        private boolean hasPasswordGrantScope(Scope.Value scope)
        {
            return config.getPasswordGrantScopes().contains(scope.getValue());
        }

        private Optional<OIDCProviderMetadata> extractMetadata(RestOperations rest, URI metadataUri)
        {
            final String response;
            try
            {
                final ResponseEntity<String> r = rest.exchange(RequestEntity.get(metadataUri).build(), String.class);
                if (r.getStatusCode() != HttpStatus.OK || !r.hasBody())
                {
                    LOGGER.warn("Unexpected response from " + metadataUri + ". Status code: " + r.getStatusCode()
                            + ", has body: " + r.hasBody() + ".");
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
            if (StringUtils.isBlank(config.getAuthServerUrl()) && StringUtils.isBlank(config.getIssuerUrl()))
            {
                throw new IdentityServiceException(
                        "Failed to create ClientRegistration. The values of issuer url and auth server url cannot both be empty.");
            }

            String baseUrl = StringUtils.isNotBlank(config.getAuthServerUrl()) ? config.getAuthServerUrl() : config.getIssuerUrl();

            return List.of(UriComponentsBuilder.fromUriString(baseUrl)
                    .pathSegment(".well-known", "openid-configuration")
                    .build().toUri());
        }
    }

    private static Optional<String> getMetadataIssuer(OIDCProviderMetadata metadata, IdentityServiceConfig config)
    {
        return DEFAULT_ISSUER_ATTR.equals(config.getIssuerAttribute()) ? Optional.of(metadata)
                .map(OIDCProviderMetadata::getIssuer)
                .map(Issuer::getValue)
                : Optional.of(metadata)
                        .map(OIDCProviderMetadata::getCustomParameters)
                        .map(map -> map.get(config.getIssuerAttribute()))
                        .filter(String.class::isInstance)
                        .map(String.class::cast);
    }

    static class JwtDecoderProvider
    {
        private static final SignatureAlgorithm DEFAULT_SIGNATURE_ALGORITHM = SignatureAlgorithm.RS256;
        private final IdentityServiceConfig config;
        private final Set<SignatureAlgorithm> signatureAlgorithms;

        JwtDecoderProvider(IdentityServiceConfig config)
        {
            this.config = requireNonNull(config);
            this.signatureAlgorithms = ofNullable(config.getSignatureAlgorithms())
                    .filter(not(Set::isEmpty))
                    .orElseGet(() -> {
                        LOGGER.warn("Unable to find any valid signature algorithms in the configuration. "
                                + "Using the default signature algorithm: " + DEFAULT_SIGNATURE_ALGORITHM.getName() + ".");
                        return Set.of(DEFAULT_SIGNATURE_ALGORITHM);
                    });
        }

        public JwtDecoder createJwtDecoder(RestOperations rest, ProviderDetails providerDetails)
        {
            try
            {
                final NimbusJwtDecoder decoder = buildJwtDecoder(rest, providerDetails);

                decoder.setJwtValidator(createJwtTokenValidator(providerDetails));
                decoder.setClaimSetConverter(
                        new ClaimTypeConverter(OidcIdTokenDecoderFactory.createDefaultClaimTypeConverters()));

                return decoder;
            }
            catch (RuntimeException e)
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
                        .signatureAlgorithm(DEFAULT_SIGNATURE_ALGORITHM)
                        .build();
            }
            final String jwkSetUri = requireValidJwkSetUri(providerDetails);
            final NimbusJwtDecoder.JwkSetUriJwtDecoderBuilder decoderBuilder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri);
            signatureAlgorithms.forEach(decoderBuilder::jwsAlgorithm);
            return decoderBuilder
                    .restOperations(rest)
                    .jwtProcessorCustomizer(this::reconfigureJWKSCache)
                    .build();
        }

        private void reconfigureJWKSCache(ConfigurableJWTProcessor<SecurityContext> jwtProcessor)
        {
            final JWKSource<SecurityContext> cachingJWKSource;

            final Optional<JWKSetSource<SecurityContext>> jwkSource = ofNullable(jwtProcessor)
                    .map(ConfigurableJWTProcessor::getJWSKeySelector)
                    .filter(JWSVerificationKeySelector.class::isInstance)
                    .map(o -> (JWKSetBasedJWKSource<SecurityContext>) ((JWSVerificationKeySelector<SecurityContext>) o).getJWKSource())
                    .map(JWKSetBasedJWKSource::getJWKSetSource);

            if (jwkSource.isEmpty())
            {
                LOGGER.warn("Not able to reconfigure the JWK Cache. Unexpected JWKSource.");
                return;
            }

            cachingJWKSource = JWKSourceBuilder.create(jwkSource.get())
                    .cache(config.getPublicKeyCacheTtl() * 1000L, -1)
                    .rateLimited(false)
                    .refreshAheadCache(false)
                    .retrying(true)
                    .build();

            jwtProcessor.setJWSKeySelector(new JWSVerificationKeySelector<>(
                    signatureAlgorithms.stream()
                            .map(signatureAlgorithm -> JWSAlgorithm.parse(signatureAlgorithm.getName()))
                            .collect(Collectors.toSet()),
                    cachingJWKSource));

            jwtProcessor.setJWSTypeVerifier(new CustomJOSEObjectTypeVerifier(JOSEObjectType.JWT, AT_JWT));
        }

        private OAuth2TokenValidator<Jwt> createJwtTokenValidator(ProviderDetails providerDetails)
        {
            List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
            validators.add(new JwtTimestampValidator(Duration.of(config.getJwtClockSkewMs(), ChronoUnit.MILLIS)));
            validators.add(new JwtIssuerValidator(providerDetails.getIssuerUri()));
            if (!config.isClientIdValidationDisabled())
            {
                validators.add(new JwtClaimValidator<String>("azp", config.getResource()::equals));
            }
            if (StringUtils.isNotBlank(config.getAudience()))
            {
                validators.add(new JwtAudienceValidator(config.getAudience()));
            }
            return new DelegatingOAuth2TokenValidator<>(validators);
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
                    // For backward compatibility with Keycloak adapter
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
            if (!isDefined(uri))
            {
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

    static class JwtIssuerValidator implements OAuth2TokenValidator<Jwt>
    {
        private final String requiredIssuer;

        public JwtIssuerValidator(String issuer)
        {
            this.requiredIssuer = requireNonNull(issuer, "issuer cannot be null");
        }

        @Override
        public OAuth2TokenValidatorResult validate(Jwt token)
        {
            requireNonNull(token, "token cannot be null");
            final Object issuer = token.getClaim(JwtClaimNames.ISS);
            if (issuer != null && requiredIssuer.equals(issuer.toString()))
            {
                return OAuth2TokenValidatorResult.success();
            }

            final OAuth2Error error = new OAuth2Error(
                    OAuth2ErrorCodes.INVALID_TOKEN,
                    "The iss claim is not valid. Expected `%s` but got `%s`.".formatted(requiredIssuer, issuer),
                    "https://tools.ietf.org/html/rfc6750#section-3.1");
            return OAuth2TokenValidatorResult.failure(error);
        }

    }

    static class JwtAudienceValidator implements OAuth2TokenValidator<Jwt>
    {
        private final String configuredAudience;

        public JwtAudienceValidator(String configuredAudience)
        {
            this.configuredAudience = configuredAudience;
        }

        @Override
        public OAuth2TokenValidatorResult validate(Jwt token)
        {
            requireNonNull(token, "token cannot be null");
            final Object audience = token.getClaim(JwtClaimNames.AUD);
            if (audience != null)
            {
                if (audience instanceof List && ((List<String>) audience).contains(configuredAudience))
                {
                    return OAuth2TokenValidatorResult.success();
                }
                if (audience instanceof String && audience.equals(configuredAudience))
                {
                    return OAuth2TokenValidatorResult.success();
                }
            }

            final OAuth2Error error = new OAuth2Error(
                    OAuth2ErrorCodes.INVALID_TOKEN,
                    "The aud claim is not valid. Expected configured audience `%s` not found.".formatted(configuredAudience),
                    "https://tools.ietf.org/html/rfc6750#section-3.1");
            return OAuth2TokenValidatorResult.failure(error);
        }
    }

    static class CustomClientHttpRequestFactory extends HttpComponentsClientHttpRequestFactory
    {
        CustomClientHttpRequestFactory(HttpClient httpClient)
        {
            super(httpClient);
        }

        @Override
        public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException
        {
            /* This is to avoid the Brotli content encoding that is not well-supported by the combination of the Apache Http Client and the Spring RestTemplate */
            ClientHttpRequest request = super.createRequest(uri, httpMethod);
            request.getHeaders()
                    .add("Accept-Encoding", "gzip, deflate");
            return request;
        }
    }

    static class CustomJOSEObjectTypeVerifier extends DefaultJOSEObjectTypeVerifier<SecurityContext>
    {
        public CustomJOSEObjectTypeVerifier(JOSEObjectType... allowedTypes)
        {
            super(Set.of(allowedTypes));
        }

        @Override
        public void verify(JOSEObjectType type, SecurityContext context) throws BadJOSEException
        {
            super.verify(type, context);
        }
    }

    private static boolean isDefined(String value)
    {
        return value != null && !value.isBlank();
    }
}
