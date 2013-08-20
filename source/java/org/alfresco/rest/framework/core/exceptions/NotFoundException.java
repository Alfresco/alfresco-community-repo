package org.alfresco.rest.framework.core.exceptions;



/**
 * The addressed entity or function was not found
 *
 * @author Gethin James
 */
public class NotFoundException extends ApiException
{
    private static final long serialVersionUID = -4477985281644251575L;
    
    public static String DEFAULT_MESSAGE_ID = "framework.exception.NotFound";
    
    public NotFoundException()
    {
        super(DEFAULT_MESSAGE_ID);
    }
    
    public NotFoundException(String msgId)
    {
        super(msgId);
    }
    
    public NotFoundException(String msgId, String[] notFoundObjects)
    {
        super(msgId, notFoundObjects);
    }

}
