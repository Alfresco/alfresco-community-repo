/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.repo.webdav;

import jakarta.servlet.http.HttpServletResponse;

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
