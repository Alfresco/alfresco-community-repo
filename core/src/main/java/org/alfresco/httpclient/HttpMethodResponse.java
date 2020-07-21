/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
import java.io.InputStream;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;

/**
 * 
 * @since 4.0
 *
 */
public class HttpMethodResponse implements Response
{
    protected HttpMethod method;

    public HttpMethodResponse(HttpMethod method) throws IOException
    {
        this.method = method;
    }
    
    public void release()
    {
        method.releaseConnection();
    }

    public InputStream getContentAsStream() throws IOException
    {
        return method.getResponseBodyAsStream();
    }

    public String getContentType()
    {
        return getHeader("Content-Type");
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
