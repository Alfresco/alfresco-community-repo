
package org.alfresco.repo.virtual;

public class ActualEnvironmentException extends VirtualizationException
{

    /**
     * 
     */
    private static final long serialVersionUID = 6411382816440744840L;

    public ActualEnvironmentException()
    {
        super();
    }

    public ActualEnvironmentException(String message, Throwable cause, boolean enableSuppression,
                boolean writableStackTrace)
    {
        super(message,
              cause,
              enableSuppression,
              writableStackTrace);
    }

    public ActualEnvironmentException(String message, Throwable cause)
    {
        super(message,
              cause);
    }

    public ActualEnvironmentException(String message)
    {
        super(message);
    }

    public ActualEnvironmentException(Throwable cause)
    {
        super(cause);
    }

}
