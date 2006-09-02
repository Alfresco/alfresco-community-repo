package org.alfresco.repo.avm;

/**
 * Interface for a retrying transaction. All operations, so far,
 * in the AVM repository are idempotent and can thus be retried
 * when a transaction fails for synchronization reasons.
 * @author britt
 */
public interface RetryingTransactionHelper
{
    /**
     * Perform a set of operations under a single transaction.
     * Keep trying if the operation fails because of a concurrency issue.
     * @param callback The worker.
     * @param write Whether this is a write operation.
     */
    public void perform(RetryingTransactionCallback callback, boolean write);
}