/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.publishing.flickr.springsocial.api.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.social.ExpiredAuthorizationException;
import org.springframework.social.InsufficientPermissionException;
import org.springframework.social.InternalServerErrorException;
import org.springframework.social.InvalidAuthorizationException;
import org.springframework.social.MissingAuthorizationException;
import org.springframework.social.NotAuthorizedException;
import org.springframework.social.OperationNotPermittedException;
import org.springframework.social.ResourceNotFoundException;
import org.springframework.social.RevokedAuthorizationException;
import org.springframework.social.UncategorizedApiException;
import org.springframework.social.facebook.api.NotAFriendException;
import org.springframework.social.facebook.api.ResourceOwnershipException;
import org.springframework.web.client.DefaultResponseErrorHandler;

/**
 * 
 * @author Brian
 * @since 4.0
 */
class FlickrErrorHandler extends DefaultResponseErrorHandler
{

    @Override
    public void handleError(ClientHttpResponse response) throws IOException
    {
        Map<String, String> errorDetails = extractErrorDetailsFromResponse(response);
        if (errorDetails == null)
        {
            handleUncategorizedError(response, errorDetails);
        }

        handleFlickrError(response.getStatusCode(), errorDetails);

        // if not otherwise handled, do default handling and wrap with
        // UncategorizedApiException
        handleUncategorizedError(response, errorDetails);
    }

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getBody()));
        return super.hasError(response) || (reader.ready() && reader.readLine().startsWith("{\"error\":"));
    }

    /**
     * Examines the error data returned from Facebook and throws the most
     * applicable exception.
     * 
     * @param errorDetails
     *            a Map containing a "type" and a "message" corresponding to the
     *            Graph API's error response structure.
     */
    void handleFlickrError(HttpStatus statusCode, Map<String, String> errorDetails)
    {
        // Can't trust the type to be useful. It's often OAuthException, even
        // for things not OAuth-related.
        // Can rely only on the message (which itself isn't very consistent).
        String message = errorDetails.get("message");

        if (statusCode == HttpStatus.OK)
        {
            if (message.contains("Some of the aliases you requested do not exist"))
            {
                throw new ResourceNotFoundException(message);
            }
        }
        else if (statusCode == HttpStatus.BAD_REQUEST)
        {
            if (message.contains("Unknown path components"))
            {
                throw new ResourceNotFoundException(message);
            }
            else if (message.equals("An access token is required to request this resource."))
            {
                throw new MissingAuthorizationException();
            }
            else if (message.equals("An active access token must be used to query information about the current user."))
            {
                throw new MissingAuthorizationException();
            }
            else if (message.startsWith("Error validating access token"))
            {
                if (message.contains("Session has expired at unix time"))
                {
                    throw new ExpiredAuthorizationException();
                }
                else if (message
                        .contains("The session has been invalidated because the user has changed the password."))
                {
                    throw new RevokedAuthorizationException();
                }
                else if (message.contains("The session is invalid because the user logged out."))
                {
                    throw new RevokedAuthorizationException();
                }
                else if (message.contains("has not authorized application"))
                {
                    // Per https://developers.facebook.com/blog/post/500/, this
                    // could be in the message when the user removes the
                    // application.
                    // In reality,
                    // "The session has been invalidated because the user has changed the password."
                    // is what you get in that case.
                    // Leaving this check in place in case there FB does return
                    // this message (could be a bug in FB?)
                    throw new RevokedAuthorizationException();
                }
                else
                {
                    throw new InvalidAuthorizationException(message);
                }
            }
            else if (message.equals("Error validating application."))
            { // Access token with incorrect app ID
                throw new InvalidAuthorizationException(message);
            }
            else if (message.equals("Invalid access token signature."))
            { // Access token that fails signature validation
                throw new InvalidAuthorizationException(message);
            }
        }
        else if (statusCode == HttpStatus.UNAUTHORIZED)
        {
            throw new NotAuthorizedException(message);
        }
        else if (statusCode == HttpStatus.FORBIDDEN)
        {
            if (message.contains("Requires extended permission"))
            {
                String requiredPermission = message.split(": ")[1];
                throw new InsufficientPermissionException(requiredPermission);
            }
            else
            {
                throw new OperationNotPermittedException(message);
            }
        }
        else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR)
        {
            if (message.equals("User must be an owner of the friendlist"))
            { // watch for pattern in similar message in other resources
                throw new ResourceOwnershipException(message);
            }
            else if (message.equals("The member must be a friend of the current user."))
            {
                throw new NotAFriendException(message);
            }
            else
            {
                throw new InternalServerErrorException(message);
            }
        }
    }

    private void handleUncategorizedError(ClientHttpResponse response, Map<String, String> errorDetails)
    {
        try
        {
            super.handleError(response);
        }
        catch (Exception e)
        {
            if (errorDetails != null)
            {
                throw new UncategorizedApiException(errorDetails.get("message"), e);
            }
            else
            {
                throw new UncategorizedApiException("No error details from Facebook", e);
            }
        }
    }

    /*
     * Attempts to extract Facebook error details from the response. Returns
     * null if the response doesn't match the expected JSON error response.
     */
    private Map<String, String> extractErrorDetailsFromResponse(ClientHttpResponse response) throws IOException
    {
        return null;
    }
}
