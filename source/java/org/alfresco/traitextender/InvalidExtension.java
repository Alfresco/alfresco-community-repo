
package org.alfresco.traitextender;

/**
 * Signals an invalid extension state or extension definition.
 *
 * @author Bogdan Horje
 */
public class InvalidExtension extends RuntimeException
{
    private static final long serialVersionUID = -7146808120353555462L;

    public InvalidExtension()
    {
        super();
    }

    public InvalidExtension(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message,
              cause,
              enableSuppression,
              writableStackTrace);
    }

    public InvalidExtension(String message, Throwable cause)
    {
        super(message,
              cause);
    }

    public InvalidExtension(String message)
    {
        super(message);
    }

    public InvalidExtension(Throwable cause)
    {
        super(cause);
    }

}
