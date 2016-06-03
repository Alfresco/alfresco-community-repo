package org.alfresco.repo.site;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Site service exception
 * 
 * @author Roy Wetherall
 */
public class SiteServiceException extends AlfrescoRuntimeException
{
    /** Serial version UID */
    private static final long serialVersionUID = -5838634544722182609L;
    
    /**
     * Constructor
     * 
     * @param msgId             message id
     */
    public SiteServiceException(String msgId)
    {
        super(msgId);
    }
    
    /**
     * Constructor
     * 
     * @param msgId         message id
     * @param msgParams     message params
     */
    public SiteServiceException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    /**
     * Constructor
     * 
     * @param msgId     message id
     * @param cause     causing exception
     */
    public SiteServiceException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }
    
    /**
     * Constructor 
     * 
     * @param msgId         message id
     * @param msgParams     message params
     * @param cause         causing exception
     */
    public SiteServiceException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }
}
