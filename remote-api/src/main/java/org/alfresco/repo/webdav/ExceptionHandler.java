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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Create a suitable HttpServletResponse when face with an exception.
 * 
 * @author Matt Ward
 */
public class ExceptionHandler
{
    private static final Log logger = LogFactory.getLog(ExceptionHandler.class); 
    private Throwable e;
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    
    /**
     * Create an ExceptionHandler.
     * 
     * @param e Throwable
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     */
    public ExceptionHandler(Throwable e, HttpServletRequest request, HttpServletResponse response)
    {
        this.e = e;
        this.request = request;
        this.response = response;
    }
    

    public void handle() throws IOException
    {
        if (!(e instanceof WebDAVServerException) && e.getCause() != null)
        {
            if (e.getCause() instanceof WebDAVServerException)
            {
                e = e.getCause();
            }
        }
        // Work out how to handle the error
        if (e instanceof WebDAVServerException)
        {
            WebDAVServerException error = (WebDAVServerException) e;
            if (error.getCause() != null)
            {
                logger.error("Exception thrown.", e);
            }

            if (logger.isDebugEnabled())
            {
                // Show what status code the method sent back
                
                logger.debug(request.getMethod() + " is returning status code: " + error.getHttpStatusCode());
            }

            if (response.isCommitted())
            {
                logger.warn("Could not return the status code to the client as the response has already been committed!");
            }
            else
            {
                response.sendError(error.getHttpStatusCode());
            }
        }
        else
        {
            logger.error("Exception thrown.", e);

            if (response.isCommitted())
            {
                logger.warn("Could not return the internal server error code to the client as the response has already been committed!");
            }
            else
            {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }
}
