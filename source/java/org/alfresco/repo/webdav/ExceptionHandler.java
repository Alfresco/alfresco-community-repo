package org.alfresco.repo.webdav;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
