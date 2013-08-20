package org.alfresco.rest.framework.core.exceptions;

/**
 * An invalid argument was received
 *
 * @author Gethin James
 */
public class InvalidArgumentException extends ApiException
{
    private static final long serialVersionUID = -2132534145204191093L;
    
    public static String DEFAULT_MESSAGE_ID = "framework.exception.InvalidArgument";
    public static String DEFAULT_INVALID_API = "framework.exception.InvalidApiArgument";
    
    public InvalidArgumentException()
    {
        super(DEFAULT_MESSAGE_ID);
    }
    
    public InvalidArgumentException(String msgId)
    {
        super(msgId);
    }

    public InvalidArgumentException(String msgId, Object[] invalidObjects)
    {
        super(msgId, invalidObjects);
    }
}
