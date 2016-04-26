package org.alfresco.repo.audit.generator;

import java.io.Serializable;

import org.alfresco.repo.transaction.AlfrescoTransactionSupport;

/**
 * Gives back the currently transaction ID.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class TransactionIdDataGenerator extends AbstractDataGenerator
{
    /**
     * @return              Returns the current transaction ID (<tt>null</tt> if not in a transction)
     */
    public Serializable getData() throws Throwable
    {
        return AlfrescoTransactionSupport.getTransactionId();
    }
}
