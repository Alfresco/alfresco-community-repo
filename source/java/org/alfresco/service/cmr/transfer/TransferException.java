package org.alfresco.service.cmr.transfer;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Transfer service exception class
 * 
 * @author Mark Rogers
 */
public class TransferException extends AlfrescoRuntimeException 
{
    /**
	 * Serial version UID 
	 */
	private static final long serialVersionUID = 3257571685241467958L;

    public TransferException(String msgId)
    {
        super(msgId);
    }

    public TransferException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    public TransferException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }

    public TransferException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }
}
