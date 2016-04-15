package org.alfresco.service.cmr.action;

/**
 * This exception should be thrown when an {@link Action} has not been run successfully due to
 * a transient condition and where it is possible that a subsequent request to execute the
 * same action might succeed.
 * <p/>
 * An example of this would be the case where a request to create a thumbnail
 * has failed because the necessary thumbnailing software is not available e.g. because the OpenOffice.org process
 * is not currently running.
 * <p/>
 * The {@link ActionService} can be configured to run a {@link Action#setCompensatingAction(Action) compensating action}
 * when another action fails with an exception. If however the exception thrown is an instance of {@link ActionServiceTransientException}
 * then this compensating action will not be run.
 * 
 * @author Neil Mc Erlean
 * @since 4.0.1
 */
public class ActionServiceTransientException extends ActionServiceException 
{
    private static final long serialVersionUID = 3257571685241467958L;
    
    public ActionServiceTransientException(String msgId)
    {
        super(msgId);
    }
    
    public ActionServiceTransientException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }
    
    public ActionServiceTransientException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }
    
    public ActionServiceTransientException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }
}
