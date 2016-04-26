package org.alfresco.repo.security.authority;

public class UnknownAuthorityException extends AuthorityException
{

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 4639634037108317201L;

    public UnknownAuthorityException(String msgId)
    {
        super(msgId);
    }

    public UnknownAuthorityException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    public UnknownAuthorityException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

    public UnknownAuthorityException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }

}
