
package org.alfresco.traitextender;

/**
 * Trait-extension runtime target-exception wrapper.
 *
 * @author Bogdan Horje
 */
public class ExtensionTargetException extends RuntimeException
{
    private static final long serialVersionUID = -502697833178766952L;

    public ExtensionTargetException()
    {
        super();
    }

    public ExtensionTargetException(String message, Throwable cause, boolean enableSuppression,
                boolean writableStackTrace)
    {
        super(message,
              cause,
              enableSuppression,
              writableStackTrace);
    }

    public ExtensionTargetException(String message, Throwable cause)
    {
        super(message,
              cause);
    }

    public ExtensionTargetException(String message)
    {
        super(message);
    }

    public ExtensionTargetException(Throwable cause)
    {
        super(cause);
    }

}
