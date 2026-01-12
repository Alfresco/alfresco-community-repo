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

import static com.nimbusds.jose.HeaderParameterNames.KEY_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.openid.connect.sdk.claims.PersonClaims;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistration.ProviderDetails;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.client.RestOperations;

import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacadeFactoryBean.JwtAudienceValidator;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacadeFactoryBean.JwtDecoderProvider;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacadeFactoryBean.JwtIssuerValidator;

public class IdentityServiceFacadeFactoryBeanTest
{
    private static final String EXPECTED_ISSUER = "expected-issuer";
    private static final String EXPECTED_AUDIENCE = "expected-audience";
    public final IdentityServiceConfig config = mock(IdentityServiceConfig.class);
    public final RestOperations restOperations = mock(RestOperations.class);
    public final ResponseEntity responseEntity = mock(ResponseEntity.class);
    public final ProviderDetails providerDetails = mock(ProviderDetails.class);

    @Test
    public void shouldCreateJwtDecoderWithoutIDSWhenPublicKeyIsProvided()
    {
        when(config.getRealmKey()).thenReturn(
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAve3MabX/rp3LbE7/zNqKxuid8WT7y4qSXsNaiPvl/OVbNWW/cu5td1VndItYhH6/gL7Z5W/r4MOeTlz/fOdXfjrRJou2f3UiPQwLV9RdOH3oS4/BUe+sviD8Q3eRfWBWWz3yw8f2YNtD4bMztIMMjqthvwdEEb9S9jbxxD0o71Bsrz/FwPi7HhSDA+Z/p01Hct8m4wx13ZlKRd4YjyC12FBmi9MSgsrFuWzyQHhHTeBDoALpfuiut3rhVxUtFmVTpy6p9vil7C5J5pok4MXPH0dJCyDNQz05ww5+fD+tfksIEpFeokRpN226F+P21oQVFUWwYIaXaFlG/hfvwmnlfQIDAQAB");
        when(config.isClientIdValidationDisabled()).thenReturn(true);
        when(providerDetails.getIssuerUri()).thenReturn("https://my.issuer");

        final JwtDecoderProvider provider = new JwtDecoderProvider(config);

        final JwtDecoder decoder = provider.createJwtDecoder(null, providerDetails);

        final Jwt decodedToken = decoder.decode(
                "eyJhbGciOiJSUzI1NiJ9.eyJleHAiOjIxNDc0ODM2NDcsImp0aSI6IjEyMzQiLCJpc3MiOiJodHRwczovL215Lmlzc3VlciIsInN1YiI6ImFiYzEyMyIsInR5cCI6IkJlYXJlciIsInByZWZlcnJlZF91c2VybmFtZSI6InBpb3RyZWsifQ.k_KaOrLLh3QsT8mKphkcz2vKpulgxp92UoEDccpHJ1mxE3Pa3gFXPKTj4goUBKXieGPZRMvBDhfWNxMvRYZPiQr2NXJKapkh0bTd0qoaSWz9ICe9Nu3eg7_VA_nwUVPz_35wwmrxgVk0_kpUYQN_VtaO7ZgFE2sJzFjbkVls5aqfAMnEjEgQl837hqZvmlW2ZRWebtxXfQxAjtp0gcTg-xtAHKIINYo_1_uAtt_H9L8KqFaioxrVAEDDIlcKnb-Ks3Y62CrZauaGUJeN_aNj2gdOpdkhvCw79yJyZSGZ7okjGbidCNSAf7Bo2Y6h3dP1Gga7kRmD648ftZESrNvbyg");
        assertThat(decodedToken).isNotNull();

        final Map<String, Object> claims = decodedToken.getClaims();
        assertThat(claims).isNotNull()
                .isNotEmpty()
                .containsEntry(PersonClaims.PREFERRED_USERNAME_CLAIM_NAME, "piotrek");
    }

    @Test
    public void shouldAcceptAndDecodeAtJwtToken() throws JOSEException
    {
        when(config.isClientIdValidationDisabled()).thenReturn(true);
        when(config.getSignatureAlgorithms()).thenReturn(Set.of(SignatureAlgorithm.PS256, SignatureAlgorithm.ES512));
        when(restOperations.exchange(any(), any(Class.class))).thenReturn(responseEntity);

        final RSAKey rsaKey = getRsaKey();
        final RSAKey rsaPublicJWK = rsaKey.toPublicJWK();
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(responseEntity.getBody()).thenReturn(String.format("{\"keys\": [%s]}", rsaPublicJWK.toJSONString()));

        final SignedJWT signedJWT = getSignedJWT(rsaKey, "at+jwt", "userA", "https://my.issuer");
        signedJWT.sign(new RSASSASigner(rsaKey));

        when(providerDetails.getIssuerUri()).thenReturn("https://my.issuer");
        when(providerDetails.getJwkSetUri()).thenReturn("https://my.jwkSetUri");

        final JwtDecoderProvider provider = new JwtDecoderProvider(config);

        final JwtDecoder decoder = provider.createJwtDecoder(restOperations, providerDetails);
        final Jwt decodedToken = decoder.decode(signedJWT.serialize());
        assertThat(decodedToken).isNotNull();

        final Map<String, Object> claims = decodedToken.getClaims();
        assertThat(claims).isNotNull()
                .isNotEmpty()
                .containsEntry(PersonClaims.PREFERRED_USERNAME_CLAIM_NAME, "userA");
    }

    @Test
    public void shouldFailWithNotMatchingAlgorithm() throws JOSEException
    {
        when(config.isClientIdValidationDisabled()).thenReturn(true);
        when(config.getSignatureAlgorithms()).thenReturn(Set.of(SignatureAlgorithm.RS256));

        when(restOperations.exchange(any(), any(Class.class))).thenReturn(responseEntity);

        final RSAKey rsaKey = getRsaKey();
        final RSAKey rsaPublicJWK = rsaKey.toPublicJWK();
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(responseEntity.getBody()).thenReturn(String.format("{\"keys\": [%s]}", rsaPublicJWK.toJSONString()));

        final SignedJWT signedJWT = getSignedJWT(rsaKey, "at+jwt", "userA", "https://my.issuer");
        signedJWT.sign(new RSASSASigner(rsaKey));

        when(providerDetails.getIssuerUri()).thenReturn("https://my.issuer");
        when(providerDetails.getJwkSetUri()).thenReturn("https://my.jwkSetUri");

        final JwtDecoderProvider provider = new JwtDecoderProvider(config);

        final JwtDecoder decoder = provider.createJwtDecoder(restOperations, providerDetails);
        assertThrows(BadJwtException.class, () -> decoder.decode(signedJWT.serialize()));
    }

    @Test
    public void shouldFailWithNotAllowedJOSEHeaderTyp() throws JOSEException
    {
        when(config.isClientIdValidationDisabled()).thenReturn(true);
        when(config.getSignatureAlgorithms()).thenReturn(Set.of(SignatureAlgorithm.PS256));
        when(config.getPublicKeyCacheTtl()).thenReturn(5);
        when(restOperations.exchange(any(), any(Class.class))).thenReturn(responseEntity);

        final RSAKey rsaKey = getRsaKey();
        final RSAKey rsaPublicJWK = rsaKey.toPublicJWK();
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(responseEntity.getBody()).thenReturn(String.format("{\"keys\": [%s]}", rsaPublicJWK.toJSONString()));

        final SignedJWT signedJWT = getSignedJWT(rsaKey, "not-allowed-type", "userA", "https://my.issuer");
        signedJWT.sign(new RSASSASigner(rsaKey));

        when(providerDetails.getIssuerUri()).thenReturn("https://my.issuer");
        when(providerDetails.getJwkSetUri()).thenReturn("https://my.jwkSetUri");

        final JwtDecoderProvider provider = new JwtDecoderProvider(config);

        final JwtDecoder decoder = provider.createJwtDecoder(restOperations, providerDetails);
        assertThrows(BadJwtException.class, () -> decoder.decode(signedJWT.serialize()));
    }

    @Test
    public void shouldFailWithNotMatchingIssuerURIs()
    {
        final JwtIssuerValidator issuerValidator = new JwtIssuerValidator(EXPECTED_ISSUER);

        final OAuth2TokenValidatorResult validationResult = issuerValidator.validate(
                tokenWithIssuer("different-issuer"));
        assertThat(validationResult).isNotNull();
        assertThat(validationResult.hasErrors()).isTrue();
        assertThat(validationResult.getErrors()).hasSize(1);

        final OAuth2Error error = validationResult.getErrors().iterator().next();
        assertThat(error).isNotNull();
        assertThat(error.getDescription()).contains(EXPECTED_ISSUER, "different-issuer");
    }

    @Test
    public void shouldFailWithNullIssuerURI()
    {
        final JwtIssuerValidator issuerValidator = new JwtIssuerValidator(EXPECTED_ISSUER);

        final OAuth2TokenValidatorResult validationResult = issuerValidator.validate(tokenWithIssuer(null));
        assertThat(validationResult).isNotNull();
        assertThat(validationResult.hasErrors()).isTrue();
        assertThat(validationResult.getErrors()).hasSize(1);

        final OAuth2Error error = validationResult.getErrors().iterator().next();
        assertThat(error).isNotNull();
        assertThat(error.getDescription()).contains(EXPECTED_ISSUER, "null");
    }

    @Test
    public void shouldSucceedWithMatchingIssuerURI()
    {
        final JwtIssuerValidator issuerValidator = new JwtIssuerValidator(EXPECTED_ISSUER);

        final OAuth2TokenValidatorResult validationResult = issuerValidator.validate(tokenWithIssuer(EXPECTED_ISSUER));
        assertThat(validationResult).isNotNull();
        assertThat(validationResult.hasErrors()).isFalse();
        assertThat(validationResult.getErrors()).isEmpty();
    }

    @Test
    public void shouldFailWithNotMatchingAudienceList()
    {
        final JwtAudienceValidator audienceValidator = new JwtAudienceValidator(EXPECTED_AUDIENCE);

        final OAuth2TokenValidatorResult validationResult = audienceValidator.validate(
                tokenWithAudience(List.of("different-audience")));
        assertThat(validationResult).isNotNull();
        assertThat(validationResult.hasErrors()).isTrue();
        assertThat(validationResult.getErrors()).hasSize(1);

        final OAuth2Error error = validationResult.getErrors().iterator().next();
        assertThat(error).isNotNull();
        assertThat(error.getDescription()).contains(EXPECTED_AUDIENCE);
    }

    @Test
    public void shouldFailWithNullAudience()
    {
        final JwtAudienceValidator audienceValidator = new JwtAudienceValidator(EXPECTED_AUDIENCE);

        final OAuth2TokenValidatorResult validationResult = audienceValidator.validate(tokenWithAudience(null));
        assertThat(validationResult).isNotNull();
        assertThat(validationResult.hasErrors()).isTrue();
        assertThat(validationResult.getErrors()).hasSize(1);

        final OAuth2Error error = validationResult.getErrors().iterator().next();
        assertThat(error).isNotNull();
        assertThat(error.getDescription()).contains(EXPECTED_AUDIENCE);
    }

    @Test
    public void shouldSucceedWithMatchingAudienceList()
    {
        final JwtAudienceValidator audienceValidator = new JwtAudienceValidator(EXPECTED_AUDIENCE);

        final OAuth2TokenValidatorResult validationResult = audienceValidator.validate(
                tokenWithAudience(List.of(EXPECTED_AUDIENCE)));
        assertThat(validationResult).isNotNull();
        assertThat(validationResult.hasErrors()).isFalse();
        assertThat(validationResult.getErrors()).isEmpty();
    }

    @Test
    public void shouldSucceedWithMatchingSingleAudience()
    {
        final JwtAudienceValidator audienceValidator = new JwtAudienceValidator(EXPECTED_AUDIENCE);

        final Jwt token = Jwt.withTokenValue(UUID.randomUUID().toString())
                .claim("aud", EXPECTED_AUDIENCE)
                .header("JUST", "FOR TESTING")
                .build();
        final OAuth2TokenValidatorResult validationResult = audienceValidator.validate(token);
        assertThat(validationResult).isNotNull();
        assertThat(validationResult.hasErrors()).isFalse();
        assertThat(validationResult.getErrors()).isEmpty();
    }

    private static RSAKey getRsaKey() throws JOSEException
    {
        return new RSAKeyGenerator(2048)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(new Algorithm("PS256"))
                .keyID(KEY_ID)
                .generate();
    }

    private static SignedJWT getSignedJWT(RSAKey rsaKey, String type, String usernameClaim, String issuer)
    {
        final JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .claim(PersonClaims.PREFERRED_USERNAME_CLAIM_NAME, usernameClaim)
                .build();
        return new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.PS256)
                .type(new JOSEObjectType(type))
                .keyID(rsaKey.getKeyID()).build(), claimsSet);
    }

    private Jwt tokenWithIssuer(String issuer)
    {
        return Jwt.withTokenValue(UUID.randomUUID().toString())
                .issuer(issuer)
                .header("JUST", "FOR TESTING")
                .build();
    }

    private Jwt tokenWithAudience(Collection<String> audience)
    {
        return Jwt.withTokenValue(UUID.randomUUID().toString())
                .audience(audience)
                .header("JUST", "FOR TESTING")
                .build();
    }

}
