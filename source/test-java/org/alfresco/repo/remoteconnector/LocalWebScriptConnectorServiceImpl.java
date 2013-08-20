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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.sf.acegisecurity.Authentication;

import org.alfresco.service.cmr.remoteconnector.RemoteConnectorClientException;
import org.alfresco.service.cmr.remoteconnector.RemoteConnectorRequest;
import org.alfresco.service.cmr.remoteconnector.RemoteConnectorResponse;
import org.alfresco.service.cmr.remoteconnector.RemoteConnectorServerException;
import org.alfresco.service.cmr.remoteconnector.RemoteConnectorService;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.repo.web.scripts.servlet.BasicHttpAuthenticatorFactory;
import org.alfresco.repo.web.scripts.servlet.LocalTestRunAsAuthenticatorFactory.LocalTestRunAsAuthenticator;
import org.alfresco.repo.web.scripts.servlet.BasicHttpAuthenticatorFactory.BasicHttpAuthenticator;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.Authenticator;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer;
import org.springframework.extensions.webscripts.TestWebScriptServer.Request;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;
import org.springframework.extensions.webscripts.servlet.ServletAuthenticatorFactory;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletResponse;

/**
 * Testing implementation of {@link RemoteConnectorService} which talks to
 *  the local webscripts only
 *  
 * @author Nick Burch
 * @since 4.0.2
 */
public class LocalWebScriptConnectorServiceImpl implements RemoteConnectorService
{
    /**
     * The logger
     */
    private static Log logger = LogFactory.getLog(LocalWebScriptConnectorServiceImpl.class);
    
    public static final String LOCAL_BASE_URL = "http://localhost:8080/alfresco/";
    private static final String SERVICE_URL = "/service/";
    public static final String LOCAL_SERVICE_URL = LOCAL_BASE_URL + "service/";
    
    private WebScriptHelper helper;
    private LocalAndRemoteAuthenticator auth;
    
    public LocalWebScriptConnectorServiceImpl(BaseWebScriptTest webScriptTest) throws Exception
    {
        this.helper = new WebScriptHelper(webScriptTest);
        this.auth = new LocalAndRemoteAuthenticator(webScriptTest);
    }
    
    /**
     * Builds a new Request object
     */
    public RemoteConnectorRequest buildRequest(String url, String method)
    {
        // Ensure we accept this URL
        if (url.startsWith(LOCAL_BASE_URL))
        {
            // Good, that's probably us, make it a relative url
            url = url.substring(LOCAL_BASE_URL.length()-1);
            
            // Make sure it's a service one
            if (url.startsWith(SERVICE_URL))
            {
                // Strip off and use
                url = url.substring(SERVICE_URL.length()-1);
            }
            else
            {
                throw new IllegalArgumentException("Only /service/ local URLs are supported, can't handle " + url);
            }
        }
        else
        {
            throw new IllegalArgumentException("Not a local URL: " + url);
        }
        
        // Build and return
        return new RemoteConnectorRequestImpl(url, method);
    }

    /**
     * Builds a new Request object, using HttpClient method descriptions
     */
    public RemoteConnectorRequest buildRequest(String url, Class<? extends HttpMethodBase> method)
    {
        // Get the method name
        String methodName;
        try
        {
            HttpMethodBase httpMethod = method.getConstructor(String.class).newInstance(url);
            methodName = httpMethod.getName();
        }
        catch(Exception e)
        {
            throw new AlfrescoRuntimeException("Error identifying method name", e);
        }
        
        // Build and return
        return buildRequest(url, methodName);
    }
    
    /**
     * Executes the specified request, and return the response
     */
    public RemoteConnectorResponse executeRequest(RemoteConnectorRequest request) 
            throws IOException, AuthenticationException, RemoteConnectorClientException, RemoteConnectorServerException
    {
        // Convert the request object
        RemoteConnectorRequestImpl requestImpl = (RemoteConnectorRequestImpl)request;
        Request req = new Request(request.getMethod(), request.getURL());
        req.setType(request.getContentType());
        
        if (request.getRequestBody() != null)
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            requestImpl.getRequestBody().writeRequest(baos);
            req.setBody(baos.toByteArray());
        }
        
        // Log
        if (logger.isInfoEnabled())
            logger.info("Performing local " + request.getMethod() + " request to " + request.getURL()); 

        // Capture the user details, as they may be changed during the request processing
        Authentication fullAuth = AuthenticationUtil.getFullAuthentication();
        String runAsUser = AuthenticationUtil.getRunAsUser();
        
        // If they've specified Authentication details in the request, clear our security context
        //  and switch to that user, to avoid our context confusing the real request
        Header authHeader = null;
        Map<String,String> headers = new HashMap<String, String>();
        for (Header header : request.getRequestHeaders())
        {
            if (header.getName().equals("Authorization"))
            {
                authHeader = header;
            }
            headers.put(header.getName(), header.getValue());
        }
        if (authHeader != null)
        {
            AuthenticationUtil.clearCurrentSecurityContext();
            if (logger.isDebugEnabled())
                logger.debug("HTTP Authorization found for the request, clearing security context, Auth is " + authHeader);
        }
        req.setHeaders(headers);
        
        // Execute the request against the WebScript Test Framework
        Response resp;
        try
        {
            resp = helper.sendRequest(req, -1);
        }
        catch (Exception e)
        {
            throw new AlfrescoRuntimeException("Problem requesting", e);
        }
        
        // Reset the user details, now we're done performing the request
        AuthenticationUtil.setFullAuthentication(fullAuth);
        if (runAsUser != null && !runAsUser.equals(fullAuth.getName()))
        {
            AuthenticationUtil.setRunAsUser(runAsUser);
        }
        
        // Log
        if (logger.isInfoEnabled())
            logger.info("Response to request was " + resp.getStatus() + " - " + resp);
        
        // Check the status for specific typed exceptions
        if (resp.getStatus() == Status.STATUS_UNAUTHORIZED)
        {
            throw new AuthenticationException("Not Authorized to access this resource");
        }
        if (resp.getStatus() == Status.STATUS_FORBIDDEN)
        {
            throw new AuthenticationException("Forbidden to access this resource");
        }
        
        // Check for failures where we don't care about the response body
        if (resp.getStatus() >= 500 && resp.getStatus() <= 599)
        {
            throw new RemoteConnectorServerException(resp.getStatus(), "(not available)");
        }
        
        // Convert the response into our required format
        String charset = null;
        String contentType = resp.getContentType();
        if (contentType != null && contentType.contains("charset="))
        {
            int splitAt = contentType.indexOf("charset=") + "charset=".length();
            charset = contentType.substring(splitAt);
        }
        
        InputStream body = new ByteArrayInputStream(resp.getContentAsByteArray());
        Header[] respHeaders = new Header[0]; // TODO Can't easily get the list...
        
        RemoteConnectorResponse response = new RemoteConnectorResponseImpl(
                request, contentType, charset, resp.getStatus(), respHeaders, body);
        
        // If it's a client error, let them know what went wrong
        if (resp.getStatus() >= 400 && resp.getStatus() <= 499)
        {
            throw new RemoteConnectorClientException(resp.getStatus(), "(not available)", response);
        }
        
        // Otherwise return the response for processing
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
        return RemoteConnectorServiceImpl.doExecuteJSONRequest(request, this);
    }
    
    private static class WebScriptHelper
    {
        private BaseWebScriptTest test;
        private Method sendRequest;
        
        private WebScriptHelper(BaseWebScriptTest test) throws Exception
        {
            this.test = test;
            
            sendRequest = BaseWebScriptTest.class.getDeclaredMethod("sendRequest", Request.class, Integer.TYPE);
            sendRequest.setAccessible(true);
        }
        
        private Response sendRequest(Request request, int expectedStatus) throws Exception
        {
            return (Response)sendRequest.invoke(test, request, expectedStatus);
        }
    }
    
    /**
     * A wrapper around {@link BasicHttpAuthenticator}, which uses the
     *  Authentication Context if present, otherwise HTTP Auth
     */
    private static class LocalAndRemoteAuthenticator implements ServletAuthenticatorFactory
    {
        private BasicHttpAuthenticatorFactory httpAuthFactory;
        
        private LocalAndRemoteAuthenticator(BaseWebScriptTest test) throws Exception
        {
            // Get the test server
            Method getServer = BaseWebScriptTest.class.getDeclaredMethod("getServer");
            getServer.setAccessible(true);
            TestWebScriptServer server = (TestWebScriptServer)getServer.invoke(test);
            
            // Grab the real auth factory from the context
            httpAuthFactory = (BasicHttpAuthenticatorFactory)server.getApplicationContext().getBean("webscripts.authenticator.basic");
            
            // Wire us into the test
            test.setCustomAuthenticatorFactory(this);
            server.setServletAuthenticatorFactory(this);
        }

        @Override
        public Authenticator create(WebScriptServletRequest req, WebScriptServletResponse res)
        {
            // Do we have current details?
            if (AuthenticationUtil.getFullyAuthenticatedUser() != null)
            {
                // There are already details existing
                // Allow these to be kept and used
                String fullUser = AuthenticationUtil.getFullyAuthenticatedUser();
                logger.debug("Existing Authentication found, remaining as " + fullUser);
                return new LocalTestRunAsAuthenticator(fullUser);
            }
            
            // Fall back to the http auth one
            logger.debug("No existing Authentication found, using regular HTTP Auth");
            return httpAuthFactory.create(req, res);
        }
    }
}