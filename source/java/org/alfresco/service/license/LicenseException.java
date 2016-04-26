package org.alfresco.service.license;


/**
 * Base Exception of License Exceptions.
 * 
 * @author David Caruana
 */
public class LicenseException extends RuntimeException
{
    private static final long serialVersionUID = -6463994144095426247L;

    
    public LicenseException(String msg)
    {
       super(msg);
    }
    
    public LicenseException(String msg, Throwable cause)
    {
       super(msg, cause);
    }

}
