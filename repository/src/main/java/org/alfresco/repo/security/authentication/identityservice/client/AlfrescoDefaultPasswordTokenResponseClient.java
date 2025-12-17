package org.alfresco.repo.security.authentication.identityservice.client;

/*
 * Copyright 2004-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2PasswordGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2PasswordGrantRequestEntityConverter;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

/**
 * The default implementation of an {@link OAuth2AccessTokenResponseClient} for the {@link AuthorizationGrantType#PASSWORD password} grant. This implementation uses a {@link RestOperations} when requesting an access token credential at the Authorization Server's Token Endpoint.
 *
 * @author Joe Grandja
 * @since 5.2
 *
 *        Modified for Alfresco purposes - to add compatibility with Spring Framework 7
 *
 * @see OAuth2AccessTokenResponseClient
 * @see OAuth2PasswordGrantRequest
 * @see OAuth2AccessTokenResponse
 * @see <a target="_blank" href= "https://tools.ietf.org/html/rfc6749#section-4.3.2">Section 4.3.2 Access Token Request (Resource Owner Password Credentials Grant)</a>
 * @see <a target="_blank" href= "https://tools.ietf.org/html/rfc6749#section-4.3.3">Section 4.3.3 Access Token Response (Resource Owner Password Credentials Grant)</a>
 * @deprecated The OAuth 2.0 Security Best Current Practice disallows the use of the Resource Owner Password Credentials grant. See reference <a target="_blank" href= "https://datatracker.ietf.org/doc/html/rfc9700#section-2.4">OAuth 2.0 Security Best Current Practice.</a>
 */

@Deprecated(since = "Spring 5.8")
public final class AlfrescoDefaultPasswordTokenResponseClient
        implements OAuth2AccessTokenResponseClient<OAuth2PasswordGrantRequest>
{

    private static final String INVALID_TOKEN_RESPONSE_ERROR_CODE = "invalid_token_response";

    private Converter<OAuth2PasswordGrantRequest, RequestEntity<?>> requestEntityConverter = new OAuth2PasswordGrantRequestEntityConverter();

    private RestOperations restOperations;

    public AlfrescoDefaultPasswordTokenResponseClient(RestOperations restOperations)
    {
        this.restOperations = restOperations;
    }

    @Override
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2PasswordGrantRequest passwordGrantRequest)
    {
        Assert.notNull(passwordGrantRequest, "passwordGrantRequest cannot be null");
        RequestEntity<?> request = this.requestEntityConverter.convert(passwordGrantRequest);
        ResponseEntity<OAuth2AccessTokenResponse> response = getResponse(request);
        // As per spec, in Section 5.1 Successful Access Token Response
        // https://tools.ietf.org/html/rfc6749#section-5.1
        // If AccessTokenResponse.scope is empty, then we assume all requested scopes were
        // granted.
        // However, we use the explicit scopes returned in the response (if any).
        return response.getBody();
    }

    private ResponseEntity<OAuth2AccessTokenResponse> getResponse(RequestEntity<?> request)
    {
        try
        {
            return this.restOperations.exchange(request, OAuth2AccessTokenResponse.class);
        }
        catch (RestClientException ex)
        {
            OAuth2Error oauth2Error = new OAuth2Error(INVALID_TOKEN_RESPONSE_ERROR_CODE,
                    "An error occurred while attempting to retrieve the OAuth 2.0 Access Token Response: "
                            + ex.getMessage(),
                    null);
            throw new OAuth2AuthorizationException(oauth2Error, ex);
        }
    }

    /**
     * Sets the {@link Converter} used for converting the {@link OAuth2PasswordGrantRequest} to a {@link RequestEntity} representation of the OAuth 2.0 Access Token Request.
     * 
     * @param requestEntityConverter
     *            the {@link Converter} used for converting to a {@link RequestEntity} representation of the Access Token Request
     */
    public void setRequestEntityConverter(
            Converter<OAuth2PasswordGrantRequest, RequestEntity<?>> requestEntityConverter)
    {
        Assert.notNull(requestEntityConverter, "requestEntityConverter cannot be null");
        this.requestEntityConverter = requestEntityConverter;
    }
}
