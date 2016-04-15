/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
