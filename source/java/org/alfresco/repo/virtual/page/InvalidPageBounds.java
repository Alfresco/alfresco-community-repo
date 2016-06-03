
package org.alfresco.repo.virtual.page;

public class InvalidPageBounds extends PageCollationException
{

    /**
     * 
     */
    private static final long serialVersionUID = -1197373266957787084L;

    public InvalidPageBounds()
    {
        super();
    }

    public InvalidPageBounds(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message,
              cause,
              enableSuppression,
              writableStackTrace);
    }

    public InvalidPageBounds(String message, Throwable cause)
    {
        super(message,
              cause);
    }

    public InvalidPageBounds(String message)
    {
        super(message);
    }

    public InvalidPageBounds(Throwable cause)
    {
        super(cause);
    }

}
