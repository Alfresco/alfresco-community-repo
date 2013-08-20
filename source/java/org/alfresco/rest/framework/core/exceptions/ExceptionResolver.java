package org.alfresco.rest.framework.core.exceptions;

/**
 * Loosely based on Spring's HandlerExceptionResolver
 * 
 * Resolves exception to the correct ErrorResponse
 *
 * @author Gethin James
 */
public interface ExceptionResolver<E extends Exception>
{
   ErrorResponse resolveException(E ex);
}
