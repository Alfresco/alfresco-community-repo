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

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.remoteconnector.RemoteConnectorRequest;
import org.alfresco.service.cmr.remoteconnector.RemoteConnectorService;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;

/**
 * Helper wrapper around a Remote Request, to be performed by the
 *  {@link RemoteConnectorService}.
 * 
 * @author Nick Burch
 * @since 4.0.2
 */
public class RemoteConnectorRequestImpl implements RemoteConnectorRequest
{
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    
    private final String url;
    private final String methodName;
    private final HttpMethodBase method;
    private final List<Header> headers = new ArrayList<Header>();
    private RequestEntity requestBody;
    
    public RemoteConnectorRequestImpl(String url, String methodName)
    {
        this(url, buildHttpClientMethod(url, methodName));
    }
    public RemoteConnectorRequestImpl(String url, Class<? extends HttpMethodBase> method)
    {
        this(url, buildHttpClientMethod(url, method));
    }
    private RemoteConnectorRequestImpl(String url, HttpMethodBase method)
    {
        this.url = url;
        this.method = method;
        this.methodName = method.getName();
    }
    
    protected static HttpMethodBase buildHttpClientMethod(String url, String method)
    {
        if ("GET".equals(method))
        {
            return new GetMethod(url);
        }
        if ("POST".equals(method))
        {
            return new PostMethod(url);
        }
        if ("PUT".equals(method))
        {
            return new PutMethod(url);
        }
        if ("DELETE".equals(method))
        {
            return new DeleteMethod(url);
        }
        if (TestingMethod.METHOD_NAME.equals(method))
        {
            return new TestingMethod(url);
        }
        throw new UnsupportedOperationException("Method '"+method+"' not supported");
    }
    protected static HttpMethodBase buildHttpClientMethod(String url, Class<? extends HttpMethodBase> method)
    {
        HttpMethodBase request = null;
        try
        {
            request = method.getConstructor(String.class).newInstance(url);
        }
        catch(Exception e)
        {
            throw new AlfrescoRuntimeException("HttpClient broken", e);
        }
        return request;
    }
    
    public String getURL()
    {
        return url;
    }
    public String getMethod()
    {
        return methodName;
    }
    public HttpMethodBase getMethodInstance()
    {
        return method;
    }
    
    public String getContentType()
    {
        for (Header hdr : headers)
        {
            if (HEADER_CONTENT_TYPE.equals( hdr.getName() ))
            {
                return hdr.getValue();
            }
        }
        return null;
    }
    public void setContentType(String contentType)
    {
        for (Header hdr : headers)
        {
            if (HEADER_CONTENT_TYPE.equals( hdr.getName() ))
            {
                hdr.setValue(contentType);
                return;
            }
        }
        headers.add(new Header(HEADER_CONTENT_TYPE, contentType));
    }
    
    public RequestEntity getRequestBody()
    {
        return requestBody;
    }
    public void setRequestBody(String body)
    {
        try
        {
            requestBody = new StringRequestEntity(body, getContentType(), "UTF-8");
        }
        catch (UnsupportedEncodingException e) {} // Can't occur
    }
    public void setRequestBody(byte[] body)
    {
        requestBody = new ByteArrayRequestEntity(body);
    }
    public void setRequestBody(InputStream body)
    {
        requestBody = new InputStreamRequestEntity(body);
    }
    public void setRequestBody(RequestEntity body)
    {
        requestBody = body;
    }
    
    public Header[] getRequestHeaders()
    {
        return headers.toArray(new Header[headers.size()]);
    }
    public void addRequestHeader(Header header)
    {
        addRequestHeaders(new Header[] {header});
    }
    public void addRequestHeader(String name, String value)
    {
        addRequestHeader(new Header(name,value));
    }
    public void addRequestHeaders(Header[] headers)
    {
        for (Header newHdr : headers)
        {
            // See if we already have one of these headers
            Header existingHdr = null;
            for (Header hdr : this.headers)
            {
                if (newHdr.getName().equals( hdr.getName() ))
                {
                    existingHdr = hdr;
                }
            }
            
            // Update or add as needed
            if (existingHdr != null)
            {
                existingHdr.setValue(newHdr.getValue());
            }
            else
            {
                this.headers.add(newHdr);
            }
        }
    }
    
    /**
     * An HttpClient Method implementation for the method "TESTING",
     *  which we use in certain unit tests
     */
    private static class TestingMethod extends GetMethod
    {
        private static final String METHOD_NAME = "TESTING";
        
        private TestingMethod(String url)
        {
            super(url);
        }
        
        @Override
        public String getName()
        {
            return METHOD_NAME;
        }
    }
}
