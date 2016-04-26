
package org.alfresco.repo.virtual.ref;

public class ResourceProcessingError extends Exception
{
    private static final long serialVersionUID = 191847639145802931L;

    public ResourceProcessingError()
    {
        super();
    }

    public ResourceProcessingError(String message, Throwable cause, boolean enableSuppression,
                boolean writableStackTrace)
    {
        super(message,
              cause,
              enableSuppression,
              writableStackTrace);
    }

    public ResourceProcessingError(String message, Throwable cause)
    {
        super(message,
              cause);
    }

    public ResourceProcessingError(String message)
    {
        super(message);
    }

    public ResourceProcessingError(Throwable cause)
    {
        super(cause);
    }

}
