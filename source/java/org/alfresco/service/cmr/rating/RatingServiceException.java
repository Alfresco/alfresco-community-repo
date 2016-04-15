package org.alfresco.service.cmr.rating;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Rating Service Exception Class.
 * 
 * @author Neil McErlean
 * @since 3.4
 */
public class RatingServiceException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 6035456870472850041L;

    /**
     * Constructs a Rating Service Exception with the specified message.
     * 
     * @param message 	the message string
     */
    public RatingServiceException(String message) 
    {
        super(message);
    }

    /**
     * Constructs a Rating Service Exception with the specified message and source exception.
     * 
     * @param message   the message string
     * @param source	the source exception
     */
    public RatingServiceException(String message, Throwable source) 
    {
        super(message, source);
    }
}
