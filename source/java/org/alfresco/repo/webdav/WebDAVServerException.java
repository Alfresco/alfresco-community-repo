/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
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
