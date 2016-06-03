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