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

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.Header;
import org.springframework.extensions.webscripts.Status;

/**
 * Helper wrapper around a Remote Response, for a request that
 *  was executed by {@link RemoteConnectorService}.
 * 
 * @author Nick Burch
 * @since 4.0.2
 */
public interface RemoteConnectorResponse
{
    /**
     * @return The request that generated this response
     */
    RemoteConnectorRequest getRequest();
    
    /**
     * @return The HTTP {@link Status} Code for the response
     */
    int getStatus();
    
    /**
     * @return The raw response content type, if available
     */
    String getRawContentType();
    /**
     * @return The mimetype of the response content, if available
     */
    String getContentType();
    /**
     * @return The charset of the response content, if available
     */
    String getCharset();
    
    /**
     * @return All of the response headers
     */
    Header[] getResponseHeaders();
    
    /**
     * @return The response data, as a stream
     */
    InputStream getResponseBodyAsStream() throws IOException;
    /**
     * @return The response data, as a byte array
     */
    byte[] getResponseBodyAsBytes() throws IOException;
    /**
     * @return The response as a string, based on the response content type charset
     */
    String getResponseBodyAsString() throws IOException;
}
