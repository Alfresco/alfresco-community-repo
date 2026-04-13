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

import java.util.Map;

/**
 * 
 * @since 4.0
 *
 */
public class Request
{
    private String method;
    private String uri;
    private Map<String, String> args;
    private Map<String, String> headers;
    private byte[] body;
    private String encoding = "UTF-8";
    private String contentType;
    
    public Request(Request req)
    {
        this.method = req.method;
        this.uri= req.uri;
        this.args = req.args;
        this.headers = req.headers;
        this.body = req.body;
        this.encoding = req.encoding;
        this.contentType = req.contentType;
    }
    
    public Request(String method, String uri)
    {
        this.method = method;
        this.uri = uri;
    }
    
    public String getMethod()
    {
        return method;
    }
    
    public String getUri()
    {
        return uri;
    }
    
    public String getFullUri()
    {
        // calculate full uri
        String fullUri = uri == null ? "" : uri;
        if (args != null && args.size() > 0)
        {
            char prefix = (uri.indexOf('?') == -1) ? '?' : '&';
            for (Map.Entry<String, String> arg : args.entrySet())
            {
                fullUri += prefix + arg.getKey() + "=" + (arg.getValue() == null ? "" : arg.getValue());
                prefix = '&';
            }
        }
        
        return fullUri;
    }
    
    public Request setArgs(Map<String, String> args)
    {
        this.args = args;
        return this;
    }
    
    public Map<String, String> getArgs()
    {
        return args;
    }

    public Request setHeaders(Map<String, String> headers)
    {
        this.headers = headers;
        return this;
    }
    
    public Map<String, String> getHeaders()
    {
        return headers;
    }
    
    public Request setBody(byte[] body)
    {
        this.body = body;
        return this;
    }
    
    public byte[] getBody()
    {
        return body;
    }
    
    public Request setEncoding(String encoding)
    {
        this.encoding = encoding;
        return this;
    }
    
    public String getEncoding()
    {
        return encoding;
    }

    public Request setType(String contentType)
    {
        this.contentType = contentType;
        return this;
    }
    
    public String getType()
    {
        return contentType;
    }
}
