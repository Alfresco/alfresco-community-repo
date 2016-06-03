package org.alfresco.service.cmr.repository;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * @author Kevin Roast
 */
public class TemplateException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 2863142603098852564L;

    /**
     * @param msgId String
     */
    public TemplateException(String msgId)
    {
        super(msgId);
    }

    /**
     * @param msgId String
     * @param cause Throwable
     */
    public TemplateException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }
    
    /**
     * @param msgId String
     * @param params Object[]
     */
    public TemplateException(String msgId, Object[] params)
    {
        super(msgId, params);
    }
    
    /**
     * @param msgId String
     * @param msgParams Object[]
     * @param cause Throwable
     */
    public TemplateException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }
}
