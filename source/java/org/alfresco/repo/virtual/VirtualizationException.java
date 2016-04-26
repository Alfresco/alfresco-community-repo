
package org.alfresco.repo.virtual;

public class VirtualizationException extends RuntimeException
{

    /**
     * 
     */
    private static final long serialVersionUID = -5117143626926061650L;

    public VirtualizationException()
    {
        super();
    }

    public VirtualizationException(String message, Throwable cause, boolean enableSuppression,
                boolean writableStackTrace)
    {
        super(message,
              cause,
              enableSuppression,
              writableStackTrace);
    }

    public VirtualizationException(String message, Throwable cause)
    {
        super(message,
              cause);
    }

    public VirtualizationException(String message)
    {
        super(message);
    }

    public VirtualizationException(Throwable cause)
    {
        super(cause);
    }

}
