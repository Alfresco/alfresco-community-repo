package org.alfresco.cmis.exception;

public class InvalidCmisObjectException extends RuntimeException
{
    private static final long serialVersionUID = 1L;
    public InvalidCmisObjectException(String reason)
    {
        super(reason);
    }
}
