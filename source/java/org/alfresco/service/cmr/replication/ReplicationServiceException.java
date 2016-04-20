package org.alfresco.service.cmr.replication;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Replication Service Exception Class
 * 
 * @author Nick Burch
 */
public class ReplicationServiceException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 3961767729869569556L;

    /**
     * Constructs a Replication Service Exception with the specified message.
     * 
     * @param message 	the message string
     */
    public ReplicationServiceException(String message) 
    {
       super(message);
    }

    /**
     * Constructs a Replication Service Exception with the specified message and source exception.
     * 
     * @param message   the message string
     * @param source	the source exception
     */
	 public ReplicationServiceException(String message, Throwable source) 
    {
       super(message, source);
    }
}
