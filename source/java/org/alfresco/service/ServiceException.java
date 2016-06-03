package org.alfresco.service;


/**
 * Base Exception of Service Exceptions.
 * 
 * @author David Caruana
 */
public class ServiceException extends RuntimeException
{
    private static final long serialVersionUID = 3257008761007847733L;

    public ServiceException(String msg)
    {
       super(msg);
    }
    
    public ServiceException(String msg, Throwable cause)
    {
       super(msg, cause);
    }

}
