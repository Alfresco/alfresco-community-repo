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