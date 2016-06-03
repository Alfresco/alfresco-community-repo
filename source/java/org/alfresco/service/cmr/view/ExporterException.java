package org.alfresco.service.cmr.view;


/**
 * Base Exception of Export Exceptions.
 * 
 * @author David Caruana
 */
public class ExporterException extends RuntimeException
{
    private static final long serialVersionUID = 3257008761007847733L;

    public ExporterException(String msg)
    {
       super(msg);
    }
    
    public ExporterException(String msg, Throwable cause)
    {
       super(msg, cause);
    }

}
