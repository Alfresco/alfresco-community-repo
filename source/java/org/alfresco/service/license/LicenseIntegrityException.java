package org.alfresco.service.license;

/**
 * An instance of this class is thrown if the integrity of a GenericCertificate has been detected to be compromised.
 */
public class LicenseIntegrityException extends Exception
{

    private static final long serialVersionUID = 112424979852827947L;

    public LicenseIntegrityException(String msg)
    {
        super(msg);
    }

    public LicenseIntegrityException(String msg, Throwable cause)
    {
        super(msg, cause);
    }
}
