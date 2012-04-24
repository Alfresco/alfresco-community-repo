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
package org.alfresco.service.cmr.remoteconnector;

import java.io.InputStream;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.methods.RequestEntity;

/**
 * Helper wrapper around a Remote Request, to be performed by the
 *  {@link RemoteConnectorService}.
 * To have one of these created for you, use
 *  {@link RemoteConnectorService#buildRequest(String, String)}
 * 
 * @author Nick Burch
 * @since 4.0.2
 */
public interface RemoteConnectorRequest
{
    /**
     * @return the URL this request is for
     */
    String getURL();
    /**
     * @return the HTTP Method this request will execute (eg POST, GET)
     */
    String getMethod();
    
    /**
     * @return The Content Type of the request
     */
    String getContentType();
    /**
     * Sets the Content Type to send for the request 
     */
    void setContentType(String contentType);
    
    /**
     * Returns the Request Body, for use by the {@link RemoteConnectorService}
     *  which created this
     */
    Object getRequestBody();
    
    void setRequestBody(String body);
    void setRequestBody(byte[] body);
    void setRequestBody(InputStream body);
    void setRequestBody(RequestEntity body);
    
    Header[] getRequestHeaders();
    void addRequestHeader(String name, String value);
    void addRequestHeader(Header header);
    void addRequestHeaders(Header[] headers);
}
