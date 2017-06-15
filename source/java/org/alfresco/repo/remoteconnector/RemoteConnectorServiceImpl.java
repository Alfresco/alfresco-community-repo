/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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
package org.alfresco.repo.remoteconnector;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.service.cmr.remoteconnector.RemoteConnectorClientException;
import org.alfresco.service.cmr.remoteconnector.RemoteConnectorRequest;
import org.alfresco.service.cmr.remoteconnector.RemoteConnectorResponse;
import org.alfresco.service.cmr.remoteconnector.RemoteConnectorServerException;
import org.alfresco.service.cmr.remoteconnector.RemoteConnectorService;
import org.alfresco.util.HttpClientHelper;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.ProxyHost;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.Status;

/**
 * HttpClient powered implementation of {@link RemoteConnectorService}, which performs requests to remote HTTP servers.
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
 
    private static ProxyHost httpProxyHost;
    private static ProxyHost httpsProxyHost;
    private static Credentials httpProxyCredentials;
    private static Credentials httpsProxyCredentials;
    private static AuthScope httpAuthScope;
    private static AuthScope httpsAuthScope;
            
    /**
     * Initialise the HTTP Proxy Hosts and Params Factory
     */
    static
    {
        // Create an HTTP Proxy Host if appropriate system property set
        httpProxyHost = HttpClientHelper.createProxyHost("http.proxyHost", "http.proxyPort", 80);
        httpProxyCredentials = HttpClientHelper.createProxyCredentials("http.proxyUser", "http.proxyPassword");
        httpAuthScope = createProxyAuthScope(httpProxyHost);
        
        // Create an HTTPS Proxy Host if appropriate system property set
        httpsProxyHost = HttpClientHelper.createProxyHost("https.proxyHost", "https.proxyPort", 443);
        httpsProxyCredentials = HttpClientHelper.createProxyCredentials("https.proxyUser", "https.proxyPassword");
        httpsAuthScope = createProxyAuthScope(httpsProxyHost);
    }
    
    public RemoteConnectorServiceImpl()
    {}
    
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
    public RemoteConnectorResponse executeRequest(RemoteConnectorRequest request) throws IOException, AuthenticationException,
        RemoteConnectorClientException, RemoteConnectorServerException
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
        
        // Grab our thread local HttpClient instance
        // Remember - we must then clean it up!
        HttpClient httpClient = HttpClientHelper.getHttpClient();
        
        // The url should already be vetted by the RemoteConnectorRequest
        URL url = new URL(request.getURL());
        
        // Use the appropriate Proxy Host if required
        if (httpProxyHost != null && url.getProtocol().equals("http") && HttpClientHelper.requiresProxy(url.getHost()))
        {
            httpClient.getHostConfiguration().setProxyHost(httpProxyHost);
            if (logger.isDebugEnabled())
            {
                logger.debug(" - using HTTP proxy host for: " + url);
            }
            if (httpProxyCredentials != null)
            {
                httpClient.getState().setProxyCredentials(httpAuthScope, httpProxyCredentials);
                if (logger.isDebugEnabled())
                {
                    logger.debug(" - using HTTP proxy credentials for proxy: " + httpProxyHost.getHostName());
                }
            }
        }
        else if (httpsProxyHost != null && url.getProtocol().equals("https") && HttpClientHelper.requiresProxy(url.getHost()))
        {
            httpClient.getHostConfiguration().setProxyHost(httpsProxyHost);
            if (logger.isDebugEnabled())
            {
                logger.debug(" - using HTTPS proxy host for: " + url);
            }
            if (httpsProxyCredentials != null)
            {
                httpClient.getState().setProxyCredentials(httpsAuthScope, httpsProxyCredentials);
                if (logger.isDebugEnabled())
                {
                    logger.debug(" - using HTTPS proxy credentials for proxy: " + httpsProxyHost.getHostName());
                }
            }
        } 
        else 
        {
            //host should not be proxied remove any configured proxies
            httpClient.getHostConfiguration().setProxyHost(null);
            httpClient.getState().clearProxyCredentials();
        }
        
        // Log what we're doing
        if (logger.isDebugEnabled()) 
        {
            logger.debug("Performing " + request.getMethod() + " request to " + request.getURL());
            for (Header hdr : request.getRequestHeaders())
            {
                logger.debug("Header: " + hdr );
            }
            Object requestBody = null;
            if (request != null)
            {
                requestBody = request.getRequestBody();
            }
            if (requestBody != null && requestBody instanceof StringRequestEntity)
            {
                StringRequestEntity re = (StringRequestEntity)request.getRequestBody();
                // remove credentials from logs, such as "username":"John.Doe@test.com" and "password":"123456abc"
                // the strings can include double quotes, therefore we should check for proper end, it can be either
                // a comma "," or "}"
                // REPO-1471
                String payload = re.getContent(); // returns a new string, should be safe to modify
                String usernameString = "\"username\"";
                String passwordString = "\"password\"";
                String hiddenString = "\"<hidden>\"";
                List<String> matches = new LinkedList<>();
                Matcher m = Pattern.compile(usernameString + ":\"(.+)\",|" +
                                            usernameString + ":\"(.+)\"}|" +
                                            passwordString + ":\"(.+)\",|" +
                                            passwordString + ":\"(.+)\"}").matcher(payload);
                while(m.find())
                {
                    matches.add(m.group());
                }
                for (String match: matches)
                {
                    if (match.contains(usernameString))
                    {
                        payload = payload.replace(
                                match.substring(
                                        match.indexOf(usernameString) + usernameString.length() + 1, // 1 is semicolon
                                        match.lastIndexOf("\"") + 1), // 1 is a double quote
                                hiddenString);
                    }
                    else if (match.contains(passwordString))
                    {
                        payload = payload.replace(
                                match.substring(
                                        match.indexOf(passwordString) + passwordString.length() + 1, // 1 is semicolon
                                        match.lastIndexOf("\"") + 1), // 1 is a double quote
                                hiddenString);
                    }

                }

                logger.debug("Payload (string): " + payload);
            }
            else if (requestBody != null && requestBody instanceof ByteArrayRequestEntity)
            {
                ByteArrayRequestEntity re = (ByteArrayRequestEntity)request.getRequestBody();
                logger.debug("Payload (byte array): " + re.getContent().toString());
            }
            else
            {
                logger.debug("Payload is not of a readable type.");
            }
        }
        
        // Perform the request, and wrap the response
        int status = -1;
        String statusText = null;
        RemoteConnectorResponse response = null;
        try
        {
            status = httpClient.executeMethod(httpRequest);
            statusText = httpRequest.getStatusText();
            
            Header[] responseHdrs = httpRequest.getResponseHeaders();
            Header responseContentTypeH = httpRequest.getResponseHeader(RemoteConnectorRequestImpl.HEADER_CONTENT_TYPE);
            String responseCharSet = httpRequest.getResponseCharSet();
            String responseContentType = (responseContentTypeH != null ? responseContentTypeH.getValue() : null);
            
            if(logger.isDebugEnabled())
            {
                logger.debug("response url=" + request.getURL() + ", length =" + httpRequest.getResponseContentLength() + ", responceContentType " + responseContentType + ", statusText =" + statusText );
            }
            
            // Decide on how best to handle the response, based on the size
            // Ideally, we want to close the HttpClient resources immediately, but
            //  that isn't possible for very large responses
            // If we can close immediately, it makes cleanup simpler and fool-proof
            if (httpRequest.getResponseContentLength() > MAX_BUFFER_RESPONSE_SIZE || httpRequest.getResponseContentLength() == -1 )
            {
                if(logger.isTraceEnabled())
                {
                	logger.trace("large response (or don't know length) url=" + request.getURL());
                }
                
                // Need to wrap the InputStream in something that'll close
                InputStream wrappedStream = new HttpClientReleasingInputStream(httpRequest);
                httpRequest = null;
                
                // Now build the response
                response = new RemoteConnectorResponseImpl(request, responseContentType, responseCharSet,
                                                           status, responseHdrs, wrappedStream);
            }
            else
            {
                if(logger.isTraceEnabled())
                {
                    logger.debug("small response for url=" + request.getURL());
                }
                // Fairly small response, just keep the bytes and make life simple
                response = new RemoteConnectorResponseImpl(request, responseContentType, responseCharSet,
                                                           status, responseHdrs, httpRequest.getResponseBody());
                
                // Now we have the bytes, we can close the HttpClient resources
                httpRequest.releaseConnection();
                httpRequest = null;
            }
        }
        finally
        {
            // Make sure, problems or not, we always tidy up (if not large stream based)
            // This is important because we use a thread local HttpClient instance
            if (httpRequest != null)
            {
                httpRequest.releaseConnection();
                httpRequest = null;
            }
        }
        
        
        // Log the response
        if (logger.isDebugEnabled())
            logger.debug("Response was " + status + " " + statusText);
        
        // Decide if we should throw an exception
        if (status >= 300)
        {
            // Tidy if needed
            if (httpRequest != null)
                httpRequest.releaseConnection();
            
            // Specific exceptions
            if (status == Status.STATUS_FORBIDDEN ||
                status == Status.STATUS_UNAUTHORIZED)
            {
            	// TODO Forbidden may need to be handled differently.
            	// TODO Need to get error message into the AuthenticationException
                throw new AuthenticationException(statusText);
            }
            
            // Server side exceptions
            if (status >= 500 && status <= 599)
            {
                logger.error("executeRequest: remote connector server exception: ["+status+"] "+statusText);
                throw new RemoteConnectorServerException(status, statusText);
            }
            if(status == Status.STATUS_PRECONDITION_FAILED)
            {
                logger.error("executeRequest: remote connector client exception: ["+status+"] "+statusText);
                throw new RemoteConnectorClientException(status, statusText, response);
            }
            else
            {
                // Client request exceptions
                if (httpRequest != null)
                {
                    // Response wasn't too big and is available, supply it
                    logger.error("executeRequest: remote connector client exception: ["+status+"] "+statusText);
                    throw new RemoteConnectorClientException(status, statusText, response);
                }
                else
                {
                    // Response was too large, report without it
                    logger.error("executeRequest: remote connector client exception: ["+status+"] "+statusText);
                    throw new RemoteConnectorClientException(status, statusText, null);
                }
            }
        }
        
        // If we get here, then the request/response was all fine
        // So, return our created response
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

        /**
         * In case the caller has neglected to close the Stream, warn
         *  (as this will break things for other users!) and then close 
         */
        @Override
        protected void finalize() throws Throwable
        {
            if (httpRequest != null)
            {
                logger.warn("RemoteConnector response InputStream wasn't closed but must be! This can cause issues for " +
                		    "other requests in this Thread!");
                
                httpRequest.releaseConnection();
                httpRequest = null;
            }
         
            // Let the InputStream tidy up if it wants to too
            super.finalize();
        }
    }
        
    /**
     * Create suitable AuthScope for ProxyHost.
     * If the ProxyHost is null, no AuthsScope will be created.
     * @param proxyHost ProxyHost
     * @return Authscope for provided ProxyHost, null otherwise.
     */
    private static AuthScope createProxyAuthScope(final ProxyHost proxyHost)
    {
        AuthScope authScope = null;
        if (proxyHost !=  null) 
        {
            authScope = new AuthScope(proxyHost.getHostName(), proxyHost.getPort());
        }
        return authScope;
    }
    
}
