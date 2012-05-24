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

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * An exception thrown when the remote server indicates that the
 *  client has made a mistake with the request.
 * This exception is normally thrown for responses in the 4xx range,
 *  eg if a 404 (not found) is returned by the remote server.
 * 
 * Provided that the response was not too large, the response from
 *  the server will also be available.
 *  
 * @author Nick Burch
 * @since 4.0.3
 */
public class RemoteConnectorClientException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = -639209368873463536L;
    private final int statusCode;
    private final String statusText;
    private final RemoteConnectorResponse response;
    
    public RemoteConnectorClientException(int statusCode, String statusText, 
            RemoteConnectorResponse response)
    {
        super(statusText);
        this.statusCode = statusCode;
        this.statusText = statusText;
        this.response = response;
    }

    public int getStatusCode()
    {
        return statusCode;
    }

    public String getStatusText()
    {
        return statusText;
    }

    public RemoteConnectorResponse getResponse()
    {
        return response;
    }
}