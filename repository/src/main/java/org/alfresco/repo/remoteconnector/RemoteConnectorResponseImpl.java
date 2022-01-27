/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.alfresco.service.cmr.remoteconnector.RemoteConnectorRequest;
import org.alfresco.service.cmr.remoteconnector.RemoteConnectorResponse;
import org.alfresco.service.cmr.remoteconnector.RemoteConnectorService;
import org.apache.commons.httpclient.Header;
import org.apache.commons.io.IOUtils;


/**
 * Helper wrapper around a Remote Request, to be performed by the
 *  {@link RemoteConnectorService}.
 * 
 * @author Nick Burch
 * @since 4.0.2
 */
public class RemoteConnectorResponseImpl implements RemoteConnectorResponse
{
    private RemoteConnectorRequest request;
    private String contentType;
    private String charset;
    
    private int status;
    private Header[] headers;
    
    private InputStream bodyStream;
    private byte[] bodyBytes;
    
    /**
     * Creates a new Response object with the data coming from a stream.
     * Because of the HttpClient lifecycle, a HttpClient response 
     *  InputStream shouldn't be used as cleanup is needed 
     */
    public RemoteConnectorResponseImpl(RemoteConnectorRequest request, String contentType, 
            String charset, int status, Header[] headers, InputStream response)
    {
        this.request = request;
        this.contentType = contentType;
        this.charset = charset;
        this.headers = headers;
        this.status = status;
        this.bodyStream = response;
        this.bodyBytes = null;
    }
    public RemoteConnectorResponseImpl(RemoteConnectorRequest request, String contentType, 
            String charset, int status, Header[] headers, byte[] response)
    {
        this(request, contentType, charset, status, headers, new ByteArrayInputStream(ensureBytes(response)));
        this.bodyBytes = ensureBytes(response);
    }
    /**
     * HttpClient will return a null response body for things like 204 (No Content).
     * We want to treat that as an empty byte array, to meet our contracts
     */
    private static byte[] ensureBytes(byte[] bytes)
    {
        if (bytes == null) return EMPTY_BYTE_ARRAY;
        return bytes;
    }
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    
    @Override
    public int getStatus()
    {
        return status;
    }

    @Override
    public String getCharset()
    {
        return charset;
    }

    @Override
    public String getContentType()
    {
        int split = contentType.indexOf(';');
        if (split == -1)
        {
            return contentType;
        }
        else
        {
            return contentType.substring(0, split);
        }
    }

    @Override
    public String getRawContentType()
    {
        return contentType;
    }

    @Override
    public RemoteConnectorRequest getRequest()
    {
        return request;
    }

    @Override
    public Header[] getResponseHeaders()
    {
        return headers;
    }
    
    @Override
    public byte[] getResponseBodyAsBytes() throws IOException
    {
        if (bodyBytes == null)
        {
            bodyBytes = IOUtils.toByteArray(bodyStream);
            bodyStream.close();
            
            // Build a new stream version in case they also want that
            bodyStream = new ByteArrayInputStream(bodyBytes);
        }
        return bodyBytes;
    }

    @Override
    public InputStream getResponseBodyAsStream()
    {
        return bodyStream;
    }

    @Override
    public String getResponseBodyAsString() throws IOException
    {
        String charset = this.charset;
        if (charset == null)
        {
            charset = "UTF-8";
        }
        
        return new String(getResponseBodyAsBytes(), charset);
    }
}
