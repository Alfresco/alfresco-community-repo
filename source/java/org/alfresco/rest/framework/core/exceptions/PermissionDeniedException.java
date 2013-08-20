package org.alfresco.rest.framework.core.exceptions;

/**
 * Permission was denied
 *
 * @author Gethin James
 */
public class PermissionDeniedException extends ApiException
{
    private static final long serialVersionUID = 7263627408581826884L;
    
    public static String DEFAULT_MESSAGE_ID = "framework.exception.PermissionDenied";
    
    public PermissionDeniedException()
    {
        super(DEFAULT_MESSAGE_ID);
    }
        
    public PermissionDeniedException(String msgId)
    {
        super(msgId);
    }

}
