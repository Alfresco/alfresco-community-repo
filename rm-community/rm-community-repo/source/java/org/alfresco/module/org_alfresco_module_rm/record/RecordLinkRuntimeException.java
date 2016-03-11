package org.alfresco.module.org_alfresco_module_rm.record;

import org.alfresco.error.AlfrescoRuntimeException;


/**
 * Record link exception class
 * 
 * @author Roy Wetherall
 * @since 2.3
 */
public class RecordLinkRuntimeException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 5202539484220535897L;

    public RecordLinkRuntimeException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

    public RecordLinkRuntimeException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }

    public RecordLinkRuntimeException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    public RecordLinkRuntimeException(String msgId)
    {
        super(msgId);
    }
}
