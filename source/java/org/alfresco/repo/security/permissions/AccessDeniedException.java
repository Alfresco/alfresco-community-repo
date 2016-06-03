package org.alfresco.repo.security.permissions;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Runtime access denied exception that is exposed
 * 
 * @author Andy Hind
 */
public class AccessDeniedException extends AlfrescoRuntimeException
{

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -4451661115250681152L;

    public AccessDeniedException(String msg)
    {
        super(msg);
    }

    public AccessDeniedException(String msg, Throwable cause)
    {
        super(msg, cause);
    }

}
