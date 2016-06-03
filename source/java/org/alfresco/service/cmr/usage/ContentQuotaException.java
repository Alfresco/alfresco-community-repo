package org.alfresco.service.cmr.usage;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * 
 */
public class ContentQuotaException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 1346806021547860709L;
    
    public ContentQuotaException(String msg)
    {
        super(msg);
    }
    
    public ContentQuotaException(String msg, Throwable cause)
    {
        super(msg, cause);
    }
}
