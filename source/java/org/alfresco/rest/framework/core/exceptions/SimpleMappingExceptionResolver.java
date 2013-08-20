package org.alfresco.rest.framework.core.exceptions;

import java.util.Map;

/**
 * Exception Resolver that uses a simple Map to resolve the Status codes and error responses for exceptions
 *
 * @author Gethin James
 */
public class SimpleMappingExceptionResolver implements ExceptionResolver<Exception>
{
    private Map<String,Integer> exceptionMappings;
    
    @Override
    public ErrorResponse resolveException(Exception ex)
    {
        ErrorResponse response = null;
        
        if (this.exceptionMappings != null) {
            response = findMatchingException(this.exceptionMappings, ex);
        }
        return response;
    }

    private ErrorResponse findMatchingException(Map<String, Integer> exceptionMappings, Exception ex)
    {
        Integer statusCode = matchException(ex.getClass());
        if (statusCode != null)
        {
            return makeErrorResponse(ex,statusCode);
        }
        else 
        {
            return null;            
        }
    }

    /**
     * Loop up the class hierarchy trying to match a class
     * 
     * @param ex Exceptions
     * @return Integer Http status code
     */
    @SuppressWarnings("unchecked")
    private Integer matchException(Class<? extends Exception> ex)
    {
        Integer statusCode = exceptionMappings.get(ex.getName());
        if (statusCode == null && !(Exception.class.getName().equals(ex.getName())))
        {
            statusCode = matchException((Class<? extends Exception>) ex.getSuperclass());
        }
        return statusCode;
    }

    private ErrorResponse makeErrorResponse(Exception ex, Integer statusCode)
    {
        if (ex instanceof ApiException)
        {
            ApiException apEx = (ApiException) ex;
            return new ErrorResponse(apEx.getMsgId(), statusCode, ex.getLocalizedMessage(), ex.getStackTrace(), apEx.getAdditionalState());
        } 
        else
        {
            return new ErrorResponse(DefaultExceptionResolver.DEFAULT_MESSAGE_ID, statusCode, ex.getLocalizedMessage(), ex.getStackTrace(), null); 
        }
    }

    public void setExceptionMappings(Map<String, Integer> exceptionMappings)
    {
        this.exceptionMappings = exceptionMappings;
    }

}
