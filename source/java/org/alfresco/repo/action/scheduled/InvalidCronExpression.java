package org.alfresco.repo.action.scheduled;

/**
 * Exception for invalid cron expressions
 * 
 * @author andyh
 *
 */
public class InvalidCronExpression extends ScheduledActionException
{

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -6618964886875008727L;

    /**
     * Invalid cron expression
     * 
     * @param msgId String
     */
    public InvalidCronExpression(String msgId)
    {
        super(msgId);
    }

    /**
     * Invalid cron expression
     * 
     * @param msgId String
     * @param msgParams Object[]
     */
    public InvalidCronExpression(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    /**
     * Invalid cron expression
     * 
     * @param msgId String
     * @param cause Throwable
     */
    public InvalidCronExpression(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

    /**
     * Invalid cron expression
     * @param msgId String
     * @param msgParams Object[]
     * @param cause Throwable
     */
    public InvalidCronExpression(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }

}
