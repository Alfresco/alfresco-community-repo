package org.alfresco.rest.framework.core.exceptions;


/**
 * Attempt to update a stale entity
 *
 * @author Gethin James
 */
public class StaleEntityException extends ApiException
{
    private static final long serialVersionUID = -3841266915415302734L;
    
    public static String DEFAULT_MESSAGE_ID = "framework.exception.StaleEntity";
    
    public StaleEntityException()
    {
        super(DEFAULT_MESSAGE_ID);
    }
    
    public StaleEntityException(String msgId)
    {
        super(msgId);
    }
}
