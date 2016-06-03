package org.alfresco.repo.transaction;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.error.AlfrescoRuntimeException;

/**
 * An exception thrown by {@link RetryingTransactionHelper} when its maxExecutionMs property is set and there isn't
 * enough capacity to execute / retry the transaction.
 * 
 * @author dward
 */
@AlfrescoPublicApi
public class TooBusyException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 1L;

    /**
     * @param msgId String
     */
    public TooBusyException(String msgId)
    {
        super(msgId);
    }

    /**
     * @param msgId String
     * @param msgParams Object[]
     */
    public TooBusyException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    /**
     * @param msgId String
     * @param cause Throwable
     */
    public TooBusyException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

    /**
     * @param msgId String
     * @param msgParams Object[]
     * @param cause Throwable
     */
    public TooBusyException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }

}
