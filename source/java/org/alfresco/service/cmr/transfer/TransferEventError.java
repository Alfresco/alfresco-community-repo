package org.alfresco.service.cmr.transfer;

import org.alfresco.repo.transfer.TransferEndEventImpl;

/**
 * Indicates the reason why a transfer failed
 */
public class TransferEventError extends TransferEndEventImpl
{
    private Exception exception;

    public void setException(Exception exception)
    {
        this.exception = exception;
    }

    public Exception getException()
    {
        return exception;
    }

}
