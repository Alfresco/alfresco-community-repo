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
package org.alfresco.repo.remoteconnector;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.service.cmr.remoteconnector.RemoteConnectorRequest;
import org.alfresco.service.cmr.remoteconnector.RemoteConnectorResponse;
import org.alfresco.service.cmr.remoteconnector.RemoteConnectorService;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;
import org.springframework.extensions.webscripts.Status;

/**
 * HttpClient powered implementation of {@link RemoteConnectorService}, which 
 *  performs requests to remote HTTP servers
 *  
 * @author Nick Burch
 * @since 4.0.2
 */
public class RemoteConnectorServiceImpl implements RemoteConnectorService
{
    /**
     * The logger
     */
    private static Log logger = LogFactory.getLog(RemoteConnectorServiceImpl.class);
    private static final long MAX_BUFFER_RESPONSE_SIZE = 10*1024*1024;
            
    private HttpClient httpClient;
    
    public RemoteConnectorServiceImpl()
    {
        httpClient = new HttpClient();
        httpClient.setHttpConnectionManager(new MultiThreadedHttpConnectionManager());
    }
    
    /**
     * Builds a new Request object
     */
    public RemoteConnectorRequest buildRequest(String url, String method)
    {
        return new RemoteConnectorRequestImpl(url, method);
    }

    /**
     * Builds a new Request object, using HttpClient method descriptions
     */
    public RemoteConnectorRequest buildRequest(String url, Class<? extends HttpMethodBase> method)
    {
        return new RemoteConnectorRequestImpl(url, method);
    }
    
    /**
     * Executes the specified request, and return the response
     */
    public RemoteConnectorResponse executeRequest(RemoteConnectorRequest request) throws IOException, AuthenticationException
    {
        RemoteConnectorRequestImpl reqImpl = (RemoteConnectorRequestImpl)request;
        HttpMethodBase httpRequest = reqImpl.getMethodInstance();
        
        // Attach the headers to the request
        for (Header hdr : request.getRequestHeaders())
        {
            httpRequest.addRequestHeader(hdr);
        }
        
        // Attach the body, if possible
        if (httpRequest instanceof EntityEnclosingMethod)
        {
            if (request.getRequestBody() != null)
            {
                ((EntityEnclosingMethod)httpRequest).setRequestEntity( reqImpl.getRequestBody() );
            }
        }
        
        // Log what we're doing
        if (logger.isDebugEnabled())
            logger.debug("Performing " + request.getMethod() + " request to " + request.getURL());
        
        // Perform the request
        int status = httpClient.executeMethod(httpRequest);
        String statusText = httpRequest.getStatusText();
        
        Header[] responseHdrs = httpRequest.getResponseHeaders();
        Header responseContentTypeH = httpRequest.getResponseHeader(RemoteConnectorRequestImpl.HEADER_CONTENT_TYPE);
        String responseCharSet = httpRequest.getResponseCharSet();
        String responseContentType = (responseContentTypeH != null ? responseContentTypeH.getValue() : null);
        
        
        // Decide on how best to handle the response, based on the size
        // Ideally, we want to close the HttpClient resources immediately, but
        //  that isn't possible for very large responses
        RemoteConnectorResponse response = null;
        if (httpRequest.getResponseContentLength() > MAX_BUFFER_RESPONSE_SIZE)
        {
            // Need to wrap the InputStream in something that'll close
            InputStream wrappedStream = new HttpClientReleasingInputStream(httpRequest);
            
            // Now build the response
            response = new RemoteConnectorResponseImpl(request, responseContentType, responseCharSet,
                                                       responseHdrs, wrappedStream);
        }
        else
        {
            // Fairly small response, just keep the bytes and make life simple
            response = new RemoteConnectorResponseImpl(request, responseContentType, responseCharSet,
                                                       responseHdrs, httpRequest.getResponseBody());
            
            // Now we have the bytes, we can close the HttpClient resources
            httpRequest.releaseConnection();
            httpRequest = null;
        }
        
        
        // Log the response
        if (logger.isDebugEnabled())
            logger.debug("Response was " + status + " " + statusText);
        
        // Decide if we should throw an exception
        if (status == Status.STATUS_FORBIDDEN)
        {
            // Tidy if needed
            if (httpRequest != null)
                httpRequest.releaseConnection();
            // Then report the error
            throw new AuthenticationException(statusText);
        }
        if (status == Status.STATUS_INTERNAL_SERVER_ERROR)
        {
            // Tidy if needed
            if (httpRequest != null)
                httpRequest.releaseConnection();
            // Then report the error
            throw new IOException(statusText);
        }
        // TODO Handle the rest of the different status codes

        
        // Return our created response
        return response;
    }
    
    /**
     * Executes the given request, requesting a JSON response, and
     *  returns the parsed JSON received back
     *  
     * @throws ParseException If the response is not valid JSON
     */
    public JSONObject executeJSONRequest(RemoteConnectorRequest request) throws ParseException, IOException, AuthenticationException
    {
        return doExecuteJSONRequest(request, this);
    }
    
    public static JSONObject doExecuteJSONRequest(RemoteConnectorRequest request, RemoteConnectorService service) throws ParseException, IOException, AuthenticationException
    {
        // Set as JSON
        request.setContentType(MimetypeMap.MIMETYPE_JSON);
        
        // Perform the request
        RemoteConnectorResponse response = service.executeRequest(request);
        
        // Parse this as JSON
        JSONParser parser = new JSONParser();
        String jsonText = response.getResponseBodyAsString();
        Object json = parser.parse(jsonText);
        
        // Check it's the right type and return
        if (json instanceof JSONObject)
        {
            return (JSONObject)json;
        }
        else
        {
            throw new ParseException(0, json);
        }
    }
    
    private static class HttpClientReleasingInputStream extends FilterInputStream
    {
        private HttpMethodBase httpRequest;
        private HttpClientReleasingInputStream(HttpMethodBase httpRequest) throws IOException
        {
            super(httpRequest.getResponseBodyAsStream());
            this.httpRequest = httpRequest;
        }

        @Override
        public void close() throws IOException
        {
            // Tidy the main stream
            super.close();
            
            // Now release the underlying resources
            if (httpRequest != null)
            {
                httpRequest.releaseConnection();
                httpRequest = null;
            }
        }
    }
}
