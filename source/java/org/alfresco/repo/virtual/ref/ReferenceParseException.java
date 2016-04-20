
package org.alfresco.repo.virtual.ref;

import org.alfresco.repo.virtual.VirtualizationException;

public class ReferenceParseException extends VirtualizationException
{
    private static final long serialVersionUID = 7234372861104307635L;

    public ReferenceParseException()
    {
        super();
    }

    public ReferenceParseException(String message, Throwable cause, boolean enableSuppression,
                boolean writableStackTrace)
    {
        super(message,
              cause,
              enableSuppression,
              writableStackTrace);
    }

    public ReferenceParseException(String message, Throwable cause)
    {
        super(message,
              cause);
    }

    public ReferenceParseException(String message)
    {
        super(message);
    }

    public ReferenceParseException(Throwable cause)
    {
        super(cause);
    }

}
