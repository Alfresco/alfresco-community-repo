package org.alfresco.repo.forms;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Exception used by the Form services
 *
 * @author Gavin Cornwell
 */
public class FormException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 688834574410335422L;

    public FormException(String msgId)
    {
        super(msgId);
    }
    
    public FormException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }
    
    public FormException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }
}
