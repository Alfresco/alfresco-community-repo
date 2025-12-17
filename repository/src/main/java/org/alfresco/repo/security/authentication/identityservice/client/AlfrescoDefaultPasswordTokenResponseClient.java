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

package org.alfresco.repo.security.authentication.identityservice.client;

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
 * The Alfresco implementation of an {@link OAuth2AccessTokenResponseClient} for the {@link AuthorizationGrantType#PASSWORD password} grant. This implementation uses a {@link RestOperations} when requesting an access token credential at the Authorization Server's Token Endpoint.
 *
 * @see <a target="_blank" href= "https://tools.ietf.org/html/rfc6749#section-4.3.2">Section 4.3.2 Access Token Request (Resource Owner Password Credentials Grant)</a>
 * @see <a target="_blank" href= "https://tools.ietf.org/html/rfc6749#section-4.3.3">Section 4.3.3 Access Token Response (Resource Owner Password Credentials Grant)</a>
 * @deprecated The OAuth 2.0 Security Best Current Practice disallows the use of the Resource Owner Password Credentials grant. See reference <a target="_blank" href= "https://datatracker.ietf.org/doc/html/rfc9700#section-2.4">OAuth 2.0 Security Best Current Practice.</a>. It was added as backward compatibility with Spring Framework 7. Meant to be removed in the future.
 */

@Deprecated(since = "Spring 5.8", forRemoval = true)
public final class AlfrescoDefaultPasswordTokenResponseClient implements OAuth2AccessTokenResponseClient<OAuth2PasswordGrantRequest>
{
    private static final String INVALID_TOKEN_RESPONSE_ERROR_CODE = "invalid_token_response";

    private Converter<OAuth2PasswordGrantRequest, RequestEntity<?>> requestEntityConverter = new OAuth2PasswordGrantRequestEntityConverter();

    private final RestOperations restOperations;

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

        // As per spec, in Section 5.1 Successful Access Token Response https://tools.ietf.org/html/rfc6749#section-5.1
        // If AccessTokenResponse.scope is empty, then we assume all requested scopes were granted.
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
    public void setRequestEntityConverter(Converter<OAuth2PasswordGrantRequest, RequestEntity<?>> requestEntityConverter)
    {
        Assert.notNull(requestEntityConverter, "requestEntityConverter cannot be null");
        this.requestEntityConverter = requestEntityConverter;
    }
}
