package org.alfresco.repo.blog;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Blog integration runtime exception
 * 
 * @author Roy Wetherall
 */
public class BlogIntegrationRuntimeException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = -159901552962025003L;

    /**
     * Constructor 
     * 
     * @param msgId String
     */
    public BlogIntegrationRuntimeException(String msgId)
    {
        super(msgId);
    }

    /**
     * Constructor
     * 
     * @param msgId String
     * @param msgParams Object[]
     */
    public BlogIntegrationRuntimeException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    /**
     * Constructor
     * 
     * @param msgId String
     * @param cause Throwable
     */
    public BlogIntegrationRuntimeException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

    /**
     * Constructor
     * 
     * @param msgId String
     * @param msgParams Object[]
     * @param cause Throwable
     */
    public BlogIntegrationRuntimeException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }

}
