package org.alfresco.service.cmr.version;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Version service exception class.
 * 
 * @author Roy Wetherall
 */
public class VersionServiceException extends AlfrescoRuntimeException
{   
    private static final long serialVersionUID = 3544671772030349881L;
    
    public VersionServiceException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

    public VersionServiceException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }

    public VersionServiceException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    public VersionServiceException(String msgId)
    {
        super(msgId);
    }
}
