package org.alfresco.repo.webdav;

import javax.servlet.http.HttpServletResponse;

/**
 * Exception class that represents an error in the WebDAV protocol layer
 * 
 * @author gavinc
 */
public class WebDAVServerException extends Exception
{
    private static final long serialVersionUID = -2949418282738082368L;

    private int m_httpStatusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    private Throwable m_cause = null;

    /**
     * Constructs a WebDAVException
     * 
     * @param httpStatusCode The HTTP status code
     */
    public WebDAVServerException(int httpStatusCode)
    {
        this(httpStatusCode, null);
    }

    /**
     * Constructs a WebDAVException
     * 
     * @param httpStatusCode The HTTP status code
     * @param cause The cause of this exception
     */
    public WebDAVServerException(int httpStatusCode, Throwable cause)
    {
        super(Integer.toString(httpStatusCode));

        m_httpStatusCode = httpStatusCode;
        m_cause = cause;
    }

    /**
     * Returns the HTTP status code
     * 
     * @return The HTTP status code
     */
    public int getHttpStatusCode()
    {
        return m_httpStatusCode;
    }

    /**
     * Returns the cause of this exception
     * 
     * @return The cause of this exception
     */
    public Throwable getCause()
    {
        return m_cause;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        String strErrorMsg = "HTTP Status Code: " + m_httpStatusCode;

        if (m_cause != null)
        {
            strErrorMsg = strErrorMsg + " caused by: " + m_cause.toString();
        }

        return strErrorMsg;
    }
}
