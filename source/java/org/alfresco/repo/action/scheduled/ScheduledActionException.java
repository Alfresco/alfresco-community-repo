package org.alfresco.repo.action.scheduled;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Base exception for sceduled actions.
 * 
 * @author Andy Hind
 */
public class ScheduledActionException extends AlfrescoRuntimeException
{

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -543079391770744598L;

    /**
     * Exception generated from scheduled actions
     * 
     * @param msgId String
     */
    public ScheduledActionException(String msgId)
    {
        super(msgId);
    }

    /**
     * Exception generated from scheduled actions
     * 
     * @param msgId String
     * @param msgParams Object[]
     */
    public ScheduledActionException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    /**
     * Exception generated from scheduled actions
     * 
     * @param msgId String
     * @param cause Throwable
     */
    public ScheduledActionException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

    /**
     * Exception generated from scheduled actions
     * 
     * @param msgId String
     * @param msgParams Object[]
     * @param cause Throwable
     */
    public ScheduledActionException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }

}
