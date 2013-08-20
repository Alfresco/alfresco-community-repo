package org.alfresco.rest.framework.core.exceptions;

/**
 * The operation is unsupported.
 *
 * @author Gethin James
 */
public class UnsupportedResourceOperationException extends ApiException
{
    
    private static final long serialVersionUID = 4311271791719791823L;
    
    public static String DEFAULT_MESSAGE_ID = "framework.exception.UnsupportedResourceOperation";
    
    public UnsupportedResourceOperationException()
    {
        super(DEFAULT_MESSAGE_ID);
    }
    
    public UnsupportedResourceOperationException(String msgId)
    {
        super(msgId);
    }

    public UnsupportedResourceOperationException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }
}
