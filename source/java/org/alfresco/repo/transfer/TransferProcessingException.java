
package org.alfresco.repo.transfer;

import org.alfresco.service.cmr.transfer.TransferException;

/**
 * @author brian
 *
 */
public class TransferProcessingException extends TransferException
{
    /**
     * 
     */
    private static final long serialVersionUID = 2547803698674661069L;

    /**
     * @param msgId String
     * @param msgParams Object[]
     * @param cause Throwable
     */
    public TransferProcessingException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }

    /**
     * @param msgId String
     * @param msgParams Object[]
     */
    public TransferProcessingException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    /**
     * @param msgId String
     * @param cause Throwable
     */
    public TransferProcessingException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

    /**
     * @param msgId String
     */
    public TransferProcessingException(String msgId)
    {
        super(msgId);
    }
}
