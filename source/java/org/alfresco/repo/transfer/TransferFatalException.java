
package org.alfresco.repo.transfer;

import org.alfresco.service.cmr.transfer.TransferException;

/**
 * @author brian
 *
 */
public class TransferFatalException extends TransferException
{
    private static final long serialVersionUID = 1022985703059592513L;

    /**
     * @param msgId String
     * @param msgParams Object[]
     * @param cause Throwable
     */
    public TransferFatalException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }

    /**
     * @param msgId String
     * @param msgParams Object[]
     */
    public TransferFatalException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    /**
     * @param msgId String
     * @param cause Throwable
     */
    public TransferFatalException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

    /**
     * @param msgId String
     */
    public TransferFatalException(String msgId)
    {
        super(msgId);
    }
}
