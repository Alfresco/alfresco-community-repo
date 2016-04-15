package org.alfresco.service.cmr.remoteticket;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Exception thrown if an error was received when attempting
 *  to talk with a remote system, meaning that it is unavailable.
 *  
 * @author Nick Burch
 * @since 4.0.2
 */
public class RemoteSystemUnavailableException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 5346482391129538502L;

    public RemoteSystemUnavailableException(String message) 
    {
        super(message);
    }
    
    public RemoteSystemUnavailableException(String message, Throwable source) 
    {
        super(message, source);
    }
}
