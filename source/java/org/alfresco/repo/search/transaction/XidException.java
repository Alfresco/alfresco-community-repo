package org.alfresco.repo.search.transaction;

public class XidException extends RuntimeException
{

    /**
     * 
     */
    private static final long serialVersionUID = 3257847696969840185L;

    public XidException()
    {
        super();
    }

    public XidException(String message)
    {
        super(message);
    }

    public XidException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public XidException(Throwable cause)
    {
        super(cause);
    }

}
