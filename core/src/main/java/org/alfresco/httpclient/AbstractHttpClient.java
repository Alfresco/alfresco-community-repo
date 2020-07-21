/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.httpclient;

import java.io.IOException;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractHttpClient implements AlfrescoHttpClient
{
    private static final Log logger = LogFactory.getLog(AlfrescoHttpClient.class);
    
    public static final String ALFRESCO_DEFAULT_BASE_URL = "/alfresco";
    
    public static final int DEFAULT_SAVEPOST_BUFFER = 4096;
    
    // Remote Server access
    protected HttpClient httpClient = null;
    
    private String baseUrl = ALFRESCO_DEFAULT_BASE_URL;

    public AbstractHttpClient(HttpClient httpClient)
    {
        this.httpClient = httpClient;
    }
    
    protected HttpClient getHttpClient()
    {
        return httpClient;
    }
    
    /**
     * @return the baseUrl
     */
    public String getBaseUrl()
    {
        return baseUrl;
    }

    /**
     * @param baseUrl the baseUrl to set
     */
    public void setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    private boolean isRedirect(HttpMethod method)
    {
        switch (method.getStatusCode()) {
        case HttpStatus.SC_MOVED_TEMPORARILY:
        case HttpStatus.SC_MOVED_PERMANENTLY:
        case HttpStatus.SC_SEE_OTHER:
        case HttpStatus.SC_TEMPORARY_REDIRECT:
            if (method.getFollowRedirects()) {
                return true;
            } else {
                return false;
            }
        default:
            return false;
        }
    }
    
    /**
     * Send Request to the repository
     */
    protected HttpMethod sendRemoteRequest(Request req) throws AuthenticationException, IOException
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("");
            logger.debug("* Request: " + req.getMethod() + " " + req.getFullUri() + (req.getBody() == null ? "" : "\n" + new String(req.getBody(), "UTF-8")));
        }

        HttpMethod method = createMethod(req);

        // execute method
        executeMethod(method);

        // Deal with redirect
        if(isRedirect(method))
        {
            Header locationHeader = method.getResponseHeader("location");
            if (locationHeader != null)
            {
                String redirectLocation = locationHeader.getValue();
                method.setURI(new URI(redirectLocation, true));
                httpClient.executeMethod(method);
            }
        }

        return method;
    }
    
    protected long executeMethod(HttpMethod method) throws HttpException, IOException
    {
        // execute method

        long startTime = System.currentTimeMillis();

        // TODO: Pool, and sent host configuration and state on execution
        getHttpClient().executeMethod(method);

        return System.currentTimeMillis() - startTime;
    }

    protected HttpMethod createMethod(Request req) throws IOException
    {
        StringBuilder url = new StringBuilder(128);
        url.append(baseUrl);
        url.append("/service/");
        url.append(req.getFullUri());

        // construct method
        HttpMethod httpMethod = null;
        String method = req.getMethod();
        if(method.equalsIgnoreCase("GET"))
        {
            GetMethod get = new GetMethod(url.toString());
            httpMethod = get;
            httpMethod.setFollowRedirects(true);
        }
        else if(method.equalsIgnoreCase("POST"))
        {
            PostMethod post = new PostMethod(url.toString());
            httpMethod = post;
            ByteArrayRequestEntity requestEntity = new ByteArrayRequestEntity(req.getBody(), req.getType());
            if (req.getBody().length > DEFAULT_SAVEPOST_BUFFER)
            {
                post.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, true);
            }
            post.setRequestEntity(requestEntity);
            // Note: not able to automatically follow redirects for POST, this is handled by sendRemoteRequest
        }
        else if(method.equalsIgnoreCase("HEAD"))
        {
            HeadMethod head = new HeadMethod(url.toString());
            httpMethod = head;
            httpMethod.setFollowRedirects(true);
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
        
        return httpMethod;
    }

    /* (non-Javadoc)
     * @see org.alfresco.httpclient.AlfrescoHttpClient#close()
     */
    @Override
    public void close()
    {
       if(httpClient != null)
       {
           HttpConnectionManager connectionManager = httpClient.getHttpConnectionManager();
           if(connectionManager instanceof MultiThreadedHttpConnectionManager)
           {
               ((MultiThreadedHttpConnectionManager)connectionManager).shutdown();
           }
       }
        
    }
    
    

}
