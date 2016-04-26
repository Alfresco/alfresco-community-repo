
package org.alfresco.repo.virtual.ref;

import org.alfresco.repo.virtual.VirtualizationException;

public class ProtocolMethodException extends VirtualizationException
{
    private static final long serialVersionUID = -7476127763746116107L;

    public ProtocolMethodException()
    {
        super();
    }

    public ProtocolMethodException(String message, Throwable cause, boolean enableSuppression,
                boolean writableStackTrace)
    {
        super(message,
              cause,
              enableSuppression,
              writableStackTrace);
    }

    public ProtocolMethodException(String message, Throwable cause)
    {
        super(message,
              cause);
    }

    public ProtocolMethodException(String message)
    {
        super(message);
    }

    public ProtocolMethodException(Throwable cause)
    {
        super(cause);
    }

}
