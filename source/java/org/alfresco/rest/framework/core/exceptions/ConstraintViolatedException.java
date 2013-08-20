package org.alfresco.rest.framework.core.exceptions;


/**
 * A constraint was violated
 *
 * @author Gethin James
 */
public class ConstraintViolatedException extends ApiException
{
    
    private static final long serialVersionUID = -6857652090677361159L;
    
    public static String DEFAULT_MESSAGE_ID = "framework.exception.ConstraintViolated";
    
    public ConstraintViolatedException()
    {
        super(DEFAULT_MESSAGE_ID);
    }
    
    public ConstraintViolatedException(String msgId)
    {
        super(msgId);
    }
}
