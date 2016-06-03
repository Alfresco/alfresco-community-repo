
package org.alfresco.repo.virtual.page;

public class PageCollationException extends Exception
{

    /**
     * 
     */
    private static final long serialVersionUID = -6956517169771939562L;

    public PageCollationException()
    {
        super();
    }

    public PageCollationException(String message, Throwable cause, boolean enableSuppression,
                boolean writableStackTrace)
    {
        super(message,
              cause,
              enableSuppression,
              writableStackTrace);
    }

    public PageCollationException(String message, Throwable cause)
    {
        super(message,
              cause);
    }

    public PageCollationException(String message)
    {
        super(message);
    }

    public PageCollationException(Throwable cause)
    {
        super(cause);
    }

}
