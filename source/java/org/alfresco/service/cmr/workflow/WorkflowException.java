package org.alfresco.service.cmr.workflow;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Base Exception of Workflow Exceptions.
 * 
 * @author David Caruana
 */
public class WorkflowException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = -7338963365877285084L;

    public WorkflowException(String msgId)
    {
       super(msgId);
    }
    
    public WorkflowException(String msgId, Throwable cause)
    {
       super(msgId, cause);
    }

    public WorkflowException(String msgId, Object ... args)
    {
        super(msgId, args);
    }

    public WorkflowException(String msgId, Throwable cause, Object ... args)
    {
        super(msgId, args, cause);
    }
}
