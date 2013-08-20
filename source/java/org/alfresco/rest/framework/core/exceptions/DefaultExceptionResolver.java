package org.alfresco.rest.framework.core.exceptions;

import javax.servlet.http.HttpServletResponse;

/**
 * Default exception resolver for cases when no other exception resolver will do.
 *
 * @author Gethin James
 */
public class DefaultExceptionResolver implements ExceptionResolver<Exception>
{
    public static final String DEFAULT_MESSAGE_ID = "framework.exception.ApiDefault";
    
    @Override
    public ErrorResponse resolveException(Exception ex)
    {
        return new ErrorResponse(DEFAULT_MESSAGE_ID, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getLocalizedMessage(), ex.getStackTrace(), null);
    }

}
