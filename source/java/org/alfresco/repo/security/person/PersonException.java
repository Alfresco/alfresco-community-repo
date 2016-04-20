package org.alfresco.repo.security.person;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * All exceptions related to the person service.
 * 
 * @author Andy Hind
 */
public class PersonException extends AlfrescoRuntimeException
{

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 2802163127696444600L;

    public PersonException(String msgId)
    {
        super(msgId);
    }

    public PersonException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    public PersonException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

    public PersonException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }

}
