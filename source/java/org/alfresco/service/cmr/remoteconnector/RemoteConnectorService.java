/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.service.cmr.remoteconnector;

import java.io.IOException;

import org.alfresco.repo.security.authentication.AuthenticationException;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 * Helper Service for performing remote web requests from within
 *  the repository tier.
 * 
 * The default implementation of the service works with HttpClient
 *  internally, but other implementations (such as testing loopback)
 *  can be used.
 *  
 * @author Nick Burch
 * @since 4.0.2
 */
public interface RemoteConnectorService
{
    /**
     * Builds a new Request object, to talk to the given URL 
     *  with the supplied method
     */
    RemoteConnectorRequest buildRequest(String url, String method);

    /**
     * Executes the specified request, and return the response.
     * 
     * @throws IOException If there was a problem with the communication to the server
     * @throws AuthenticationException If the authentication details supplied were not accepted
     * @throws RemoteConnectorClientException If the server indicates the client request was invalid
     * @throws RemoteConnectorServerException If the server was itself unable to perform the request
     */
    RemoteConnectorResponse executeRequest(RemoteConnectorRequest request) throws IOException, AuthenticationException,
        RemoteConnectorClientException, RemoteConnectorServerException;
    
    /**
     * Executes the given request, requesting a JSON response, and
     *  returns the parsed JSON received back
     *  
     * @throws ParseException If the response is not valid JSON
     */
    JSONObject executeJSONRequest(RemoteConnectorRequest request) throws IOException, AuthenticationException, ParseException;
}