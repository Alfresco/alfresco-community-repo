
package org.alfresco.repo.virtual.ref;

import org.alfresco.repo.virtual.VirtualizationException;

public class ReferenceEncodingException extends VirtualizationException
{
    private static final long serialVersionUID = 8952014414253439106L;

    public ReferenceEncodingException()
    {
        super();
    }

    public ReferenceEncodingException(String message, Throwable cause, boolean enableSuppression,
                boolean writableStackTrace)
    {
        super(message,
              cause,
              enableSuppression,
              writableStackTrace);
    }

    public ReferenceEncodingException(String message, Throwable cause)
    {
        super(message,
              cause);
    }

    public ReferenceEncodingException(String message)
    {
        super(message);
    }

    public ReferenceEncodingException(Throwable cause)
    {
        super(cause);
    }

}
