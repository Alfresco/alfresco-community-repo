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
 * An exception thrown when the remote server indicates that it
 *  has encountered a problem with the request, and cannot process
 *  it. This typically means a 5xx response.
 *  
 * @author Nick Burch
 * @since 4.0.3
 */
public class RemoteConnectorServerException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = -639209368873463536L;
    private final int statusCode;
    private final String statusText;
    
    public RemoteConnectorServerException(int statusCode, String statusText)
    {
        super(statusText);
        this.statusCode = statusCode;
        this.statusText = statusText;
    }

    public int getStatusCode()
    {
        return statusCode;
    }

    public String getStatusText()
    {
        return statusText;
    }
}