package org.alfresco.service.cmr.view;


/**
 * Base Exception of Import Exceptions.
 * 
 * @author David Caruana
 */
public class ImporterException extends RuntimeException
{
    private static final long serialVersionUID = 3257008761007847733L;

    public ImporterException(String msg)
    {
       super(msg);
    }
    
    public ImporterException(String msg, Throwable cause)
    {
       super(msg, cause);
    }

}
