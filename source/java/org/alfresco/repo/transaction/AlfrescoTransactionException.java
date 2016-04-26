package org.alfresco.repo.transaction;

import org.springframework.transaction.TransactionException;

/**
 * Simple concrete implementation of the base class.
 * 
 * @author Derek Hulley
 */
public class AlfrescoTransactionException extends TransactionException
{
    private static final long serialVersionUID = 3643033849898962687L;

    public AlfrescoTransactionException(String msg)
    {
        super(msg);
    }

    public AlfrescoTransactionException(String msg, Throwable ex)
    {
        super(msg, ex);
    }
}
