/*
 * Copyright (C) 2005-2021 Alfresco Software Limited.
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

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;

/**
 * Since Apache HttpClient 3.1 doesn't support including custom headers by default,
 * this class is adding that custom headers every time a method is invoked.
 */
public class RequestHeadersHttpClient extends HttpClient
{
    
    private Map<String, String> defaultHeaders;
    
    public RequestHeadersHttpClient(MultiThreadedHttpConnectionManager connectionManager)
    {
        super(connectionManager);
    }

    public Map<String, String> getDefaultHeaders()
    {
        return defaultHeaders;
    }

    public void setDefaultHeaders(Map<String, String> defaultHeaders)
    {
        this.defaultHeaders = defaultHeaders;
    }
    
    private void addDefaultHeaders(HttpMethod method)
    {
        if (defaultHeaders != null)
        {
            defaultHeaders.forEach((k,v) -> {
                Header h = method.getRequestHeader(k);
                boolean add = h == null || h.getValue() == null || !h.getValue().equals(v);
                if (add)
                {
                    method.addRequestHeader(k, v);
                }
            });
        }        
    }
    
    @Override
    public int executeMethod(HttpMethod method) throws IOException, HttpException
    {
        addDefaultHeaders(method);
        return super.executeMethod(method);
    }

    @Override
    public int executeMethod(HostConfiguration hostConfiguration, HttpMethod method) throws IOException, HttpException
    {
        addDefaultHeaders(method);
        return super.executeMethod(hostConfiguration, method);
    }

    @Override
    public int executeMethod(HostConfiguration hostconfig, HttpMethod method, HttpState state)
                throws IOException, HttpException
    {
        addDefaultHeaders(method);
        return super.executeMethod(hostconfig, method, state);
    }

}
