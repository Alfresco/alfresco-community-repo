package org.alfresco.repo.site;

/**
 * Site does not exist exception
 * 
 * @author Roy Wetherall
 */
public class SiteDoesNotExistException extends SiteServiceException
{
    /** Serial version UID */
    private static final long serialVersionUID = -58321344792182609L;
    /** The error message label for this */
    private static final String MSG_SITE_NO_EXIST = "site_service.site_no_exist";
    
    /**
     * Constructor
     */
    public SiteDoesNotExistException(String shortName)
    {
        super(MSG_SITE_NO_EXIST, new Object[]{shortName});
    }
}
