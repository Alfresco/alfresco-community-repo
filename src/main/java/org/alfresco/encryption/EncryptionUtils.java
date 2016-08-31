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
package org.alfresco.encryption;

import java.io.IOException;
import java.security.AlgorithmParameters;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpMethod;

/**
 * Various encryption utility methods.
 * 
 * @since 4.0
 */
public interface EncryptionUtils
{
    /**
     * Decrypt the response body of the http method
     * 
     * @param method
     * @return decrypted response body
     * @throws IOException
     */
    public byte[] decryptResponseBody(HttpMethod method) throws IOException;

    /**
     * Decrypt the body of the http request
     * 
     * @param req
     * @return decrypted response body
     * @throws IOException
     */
    public byte[] decryptBody(HttpServletRequest req) throws IOException;
    
    /**
     * Authenticate the http method response: validate the MAC, check that the remote IP is
     * as expected and that the timestamp is recent.
     * 
     * @param method
     * @param remoteIP
     * @param decryptedBody
     * @return true if the method reponse is authentic, false otherwise
     */
    public boolean authenticateResponse(HttpMethod method, String remoteIP, byte[] decryptedBody);

    /**
     * Authenticate the http request: validate the MAC, check that the remote IP is
     * as expected and that the timestamp is recent.
     * 
     * @param req
     * @param decryptedBody
     * @return true if the method request is authentic, false otherwise
     */
    public boolean authenticate(HttpServletRequest req, byte[] decryptedBody);
    
    /**
     * Encrypt the http method request body
     * 
     * @param method
     * @param message
     * @throws IOException
     */
    public void setRequestAuthentication(HttpMethod method, byte[] message) throws IOException;

    /**
     * Sets authentication headers on the HTTP response.
     * 
     * @param httpRequest
     * @param httpResponse
     * @param responseBody
     * @param params
     * @throws IOException
     */
    public void setResponseAuthentication(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
            byte[] responseBody, AlgorithmParameters params) throws IOException;
    
    /**
     * Set the algorithm parameters header on the method request
     * 
     * @param method
     * @param params
     * @throws IOException
     */
    public void setRequestAlgorithmParameters(HttpMethod method, AlgorithmParameters params) throws IOException;
}
