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