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
package org.alfresco.repo.web.scripts;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestListener;
import junit.textui.ResultPrinter;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.web.scripts.servlet.LocalTestRunAsAuthenticatorFactory;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.springframework.extensions.webscripts.TestWebScriptServer;
import org.springframework.extensions.webscripts.TestWebScriptServer.Request;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;
import org.springframework.extensions.webscripts.servlet.ServletAuthenticatorFactory;

/**
 * Base unit test class for web scripts.
 * 
 * @author Roy Wetherall
 */
public abstract class BaseWebScriptTest extends TestCase
{
    // Test Listener
    private WebScriptTestListener listener = null;
    private boolean traceReqRes = false;

    // Local Server access
    private String customContext = null;
    private ServletAuthenticatorFactory customAuthenticatorFactory = null;
    
    // Remote Server access
    private String defaultRunAs = null;
    private RemoteServer remoteServer = null;
    private HttpClient httpClient = null;

    
    /**
     * Web Script Test Listener
     */
    public static interface WebScriptTestListener extends TestListener
    {
        public void addLog(Test test, String log);
    }
    
    /**
     * Default Test Listener
     */
    public static class BaseWebScriptTestListener extends ResultPrinter implements WebScriptTestListener
    {
        /**
         * Construct
         * 
         * @param writer
         */
        public BaseWebScriptTestListener(PrintStream writer)
        {
            super(writer);
        }

        /* (non-Javadoc)
         * @see junit.textui.ResultPrinter#addError(junit.framework.Test, java.lang.Throwable)
         */
        @Override
        public void addError(Test test, Throwable t)
        {
            getWriter().println("*** Error: " + ((BaseWebScriptTest)test).getName());
            t.printStackTrace(getWriter());
        }

        /* (non-Javadoc)
         * @see junit.textui.ResultPrinter#addFailure(junit.framework.Test, junit.framework.AssertionFailedError)
         */
        @Override
        public void addFailure(Test test, AssertionFailedError t)
        {
            getWriter().println("*** Failed: " + ((BaseWebScriptTest)test).getName());
            t.printStackTrace(getWriter());
        }

        /* (non-Javadoc)
         * @see junit.textui.ResultPrinter#endTest(junit.framework.Test)
         */
        @Override
        public void endTest(Test test)
        {
            getWriter().println();
            getWriter().println("*** Test completed: " + ((BaseWebScriptTest)test).getName());
        }

        /* (non-Javadoc)
         * @see junit.textui.ResultPrinter#startTest(junit.framework.Test)
         */
        @Override
        public void startTest(Test test)
        {
            getWriter().println();
            getWriter().println("*** Test started: " + ((BaseWebScriptTest)test).getName() + " (remote: " + (((BaseWebScriptTest)test).getRemoteServer() != null));
        }

        /**
         * Add an arbitrary log statement
         * 
         * @param  test
         * @param  log
         */
        public void addLog(Test test, String log)
        {
            this.getWriter().println(log);
        }
    }
    
    
    /**
     * Sets custom context for Test Web Script Server (in-process only)
     * @param customContext
     */
    public void setCustomContext(String customContext)
    {
        this.customContext = customContext;
    }
    
    /**
     * Sets Test Listener
     * 
     * @param resultPrinter
     */
    public void setListener(WebScriptTestListener listener)
    {
        this.listener = listener;
    }

    /**
     * Sets whether to trace request / response bodies
     * 
     * @param traceReqRes
     */
    public void setTraceReqRes(boolean traceReqRes)
    {
        this.traceReqRes = traceReqRes;
    }

    /**
     * Set Remote Server context
     * 
     * @param server  remote server
     */
    public void setRemoteServer(RemoteServer server)
    {
        remoteServer = server;
    }

    /**
     * Gets Remote Server
     * 
     * @return
     */
    public RemoteServer getRemoteServer()
    {
        return remoteServer;
    }
    
    /**
     * Set Default Local Run As User
     * 
     * @param localRunAs
     */
    public void setDefaultRunAs(String localRunAs)
    {
        this.defaultRunAs = localRunAs;
    }

    /**
     * Get Default Local Run As User
     * 
     * @return  localRunAs
     */
    public String getDefaultRunAs()
    {
        return defaultRunAs;
    }

    /**
     * Returns the custom {@link ServletAuthenticatorFactory} which is injected into
     *  an {@link TestWebScriptServer} instances that are returned, if any.
     * Default is not to alter the {@link ServletAuthenticatorFactory}
     */
    public ServletAuthenticatorFactory getCustomAuthenticatorFactory()
    {
        return customAuthenticatorFactory;
    }

    /**
     * Sets that a custom {@link ServletAuthenticatorFactory} should be injected into
     *  instances of {@link TestWebScriptServer} returned from {@link #getServer()}
     */
    public void setCustomAuthenticatorFactory(ServletAuthenticatorFactory customAuthenticatorFactory)
    {
        this.customAuthenticatorFactory = customAuthenticatorFactory;
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        if (remoteServer != null)
        {
            httpClient = new HttpClient();
            httpClient.getParams().setBooleanParameter(HttpClientParams.PREEMPTIVE_AUTHENTICATION, true);
            if (remoteServer.username != null)
            {
                httpClient.getState().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), new UsernamePasswordCredentials(remoteServer.username, remoteServer.password));
            }
        }
    }
    
    /**
     * Get the server for the previously-supplied {@link #setCustomContext(String) custom context}
     */
    public TestWebScriptServer getServer()
    {
        TestWebScriptServer server;
        if (customContext == null)
        {
            server = TestWebScriptRepoServer.getTestServer();
        }
        else
        {
            server = TestWebScriptRepoServer.getTestServer(customContext);
        }
        
        if (customAuthenticatorFactory != null)
        {
            server.setServletAuthenticatorFactory(customAuthenticatorFactory);
        }
        return server;
    }
    

    /**
     * Is Log Enabled?
     * 
     * @return  true => enabled
     */
    protected boolean isLogEnabled()
    {
        return listener != null;
    }
    
    /**
     * Log Message to Test Listener
     * 
     * @param log
     */
    protected void log(String log)
    {
        if (listener != null)
        {
            listener.addLog(this, log);
        }
    }
    
    /**
     * Send Request to Test Web Script Server (as admin)
     * 
     * @param req
     * @param expectedStatus
     * @return response
     * @throws IOException
     */
    protected Response sendRequest(Request req, int expectedStatus)
        throws IOException
    {
        return sendRequest(req, expectedStatus, null);
    }
    
    /**
     * Send Request
     * 
     * @param req
     * @param expectedStatus
     * @param asUser
     * @return response
     * @throws IOException
     */
    protected Response sendRequest(Request req, int expectedStatus, String asUser)
        throws IOException
    {
        if (traceReqRes && isLogEnabled())
        {
            log("");
            log("* Request: " + req.getMethod() + " " + req.getFullUri() + (req.getBody() == null ? "" : "\n" + new String(req.getBody(), "UTF-8")));
        }

        Response res = null;
        if (remoteServer == null)
        {
            res = sendLocalRequest(req, expectedStatus, asUser);
        }
        else
        {
            res = sendRemoteRequest(req, expectedStatus);
        }
        
        if (traceReqRes && isLogEnabled())
        {
            log("");
            
            log("* Response: " + res.getStatus() + " " + res.getContentType() + " " + req.getMethod() + " " + req.getFullUri() + "\n");
//            if(res.getContentType().equals("application/json"))
//            {
//		    	JSONObject jsonRsp = (JSONObject)JSONValue.parse(res.getContentAsString());
//		    	StringBuilder sb = new StringBuilder();
//		    	toJSONString(jsonRsp, sb);
//		    	log(sb.toString());
//            }
//            else
//            {
            	log(res.getContentAsString());
            //}
        }
        
        if (expectedStatus > 0 && expectedStatus != res.getStatus())
        {
            fail("Status code " + res.getStatus() + " returned, but expected " + expectedStatus + " for " + req.getFullUri() + " (" + req.getMethod() + ")\n" + res.getContentAsString());
        }
        
        return res;
    }
    
//    private void toJSONString(JSONObject map, StringWriter writer) throws IOException
//    {
//		if(map == null)
//		{
//			writer.write("null");
//			return;
//		}
//		
//		boolean first = true;
//		@SuppressWarnings("rawtypes")
//		Iterator iter=map.entrySet().iterator();
//		
//		writer.write('{');
//		while(iter.hasNext())
//		{
//            if(first)
//            {
//                first = false;
//            }
//            else
//            {
//            	writer.write(',');
//            }
//			Map.Entry entry=(Map.Entry)iter.next();
//			writer.write('\"');
//			writer.write(JSONObject.escape(String.valueOf(entry.getKey())));
//			writer.write('\"');
//			writer.write(':');
//			JSONValue.writeJSONString(entry.getValue(), writer);
//		}
//		writer.write('}');
//    }

    /**
     * Send Local Request to Test Web Script Server
     * 
     * @param req
     * @param expectedStatus
     * @param asUser
     * @return response
     * @throws IOException
     */
    protected Response sendLocalRequest(final Request req, final int expectedStatus, String asUser)
        throws IOException
    {
        asUser = (asUser == null) ? defaultRunAs : asUser;
        
        TestWebScriptServer tws = getServer();
        if (AuthenticationUtil.isMtEnabled())
        {
            // MT repository container requires non-none authentication (ie. guest or higher)
            // If the servlet authenticator is still the default, substitute in a custom one
            // (If they test has already changed the authenticator, then stay with that)
            if (customAuthenticatorFactory == null)
            {
                tws.setServletAuthenticatorFactory(new LocalTestRunAsAuthenticatorFactory());
            }
        }
        
        if (asUser == null)
        {
            return getServer().submitRequest(req.getMethod(), req.getFullUri(), req.getHeaders(), req.getBody(), req.getEncoding(), req.getType());
        }
        else
        {
            // send request in context of specified user
            return AuthenticationUtil.runAs(new RunAsWork<Response>()
            {
                @SuppressWarnings("synthetic-access")
                public Response doWork() throws Exception
                {
                    return getServer().submitRequest(req.getMethod(), req.getFullUri(), req.getHeaders(), req.getBody(), req.getEncoding(), req.getType());
                }
            }, asUser);
        }
    }
    
    /**
     * Send Remote Request to stand-alone Web Script Server
     * 
     * @param req
     * @param expectedStatus
     * @param asUser
     * @return response
     * @throws IOException
     */
    protected Response sendRemoteRequest(Request req, int expectedStatus)
        throws IOException
    {
        String uri = req.getFullUri();
        if (!uri.startsWith("http"))
        {
            uri = remoteServer.baseAddress + uri;
        }
        
        // construct method
        HttpMethod httpMethod = null;
        String method = req.getMethod();
        if (method.equalsIgnoreCase("GET"))
        {
            GetMethod get = new GetMethod(req.getFullUri());
            httpMethod = get;
        }
        else if (method.equalsIgnoreCase("POST"))
        {
            PostMethod post = new PostMethod(req.getFullUri());
            post.setRequestEntity(new ByteArrayRequestEntity(req.getBody(), req.getType()));
            httpMethod = post;
        }
        else if (method.equalsIgnoreCase("PATCH"))
        {
            PatchMethod post = new PatchMethod(req.getFullUri());
            post.setRequestEntity(new ByteArrayRequestEntity(req.getBody(), req.getType()));
            httpMethod = post;
        }
        else if (method.equalsIgnoreCase("PUT"))
        {
            PutMethod put = new PutMethod(req.getFullUri());
            put.setRequestEntity(new ByteArrayRequestEntity(req.getBody(), req.getType()));
            httpMethod = put;
        }
        else if (method.equalsIgnoreCase("DELETE"))
        {
            DeleteMethod del = new DeleteMethod(req.getFullUri());
            httpMethod = del;
        }
        else
        {
            throw new AlfrescoRuntimeException("Http Method " + method + " not supported");
        }
        if (req.getHeaders() != null)
        {
            for (Map.Entry<String, String> header : req.getHeaders().entrySet())
            {
                httpMethod.setRequestHeader(header.getKey(), header.getValue());
            }
        }

        // execute method
        httpClient.executeMethod(httpMethod);
        return new HttpMethodResponse(httpMethod);
    }
    
    /**
     * PATCH method
     */
    public static class PatchMethod extends EntityEnclosingMethod
    {
        public PatchMethod(String uri)
        {
            super(uri);
        }

        @Override
        public String getName()
        {
            return "PATCH";
        }
    }
    
    
    /**
     * Remote Context
     */
    public static class RemoteServer
    {
        public String baseAddress;
        public String username;
        public String password;
    }

    /**
     * HttpMethod wrapped as Web Script Test Response
     */
    public static class HttpMethodResponse
        implements Response
    {
        private HttpMethod method;
        
        public HttpMethodResponse(HttpMethod method)
        {
            this.method = method;
        }

        public byte[] getContentAsByteArray()
        {
            try
            {
                return method.getResponseBody();
            }
            catch (IOException e)
            {
                return null;
            }
        }

        public String getContentAsString() throws UnsupportedEncodingException
        {
            try
            {
                return method.getResponseBodyAsString();
            }
            catch (IOException e)
            {
                return null;
            }
        }

        public String getContentType()
        {
            return getHeader("Content-Type");
        }

        public int getContentLength()
        {
            try
            {
                return method.getResponseBody().length;
            }
            catch (IOException e)
            {
                return 0;
            }
        }

        public String getHeader(String name)
        {
            Header header = method.getResponseHeader(name);
            return (header != null) ? header.getValue() : null;
        }

        public int getStatus()
        {
            return method.getStatusCode();
        }

    }
    
}
