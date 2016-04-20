package org.alfresco.repo.security.authority;

import org.alfresco.error.AlfrescoRuntimeException;

public class AuthorityException extends AlfrescoRuntimeException
{

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -5367993045129604445L;

    public AuthorityException(String msgId)
    {
        super(msgId);
    }

    public AuthorityException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    public AuthorityException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

    public AuthorityException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }

}
