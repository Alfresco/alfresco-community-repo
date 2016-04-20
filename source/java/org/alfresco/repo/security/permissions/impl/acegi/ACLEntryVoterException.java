package org.alfresco.repo.security.permissions.impl.acegi;

import org.alfresco.error.AlfrescoRuntimeException;

public class ACLEntryVoterException extends AlfrescoRuntimeException
{

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -674195849623480512L;

    public ACLEntryVoterException(String msg)
    {
        super(msg);
    }

    public ACLEntryVoterException(String msg, Throwable cause)
    {
        super(msg, cause);
    }

}
