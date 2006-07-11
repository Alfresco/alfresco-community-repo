package org.alfresco.repo.avm;


public interface RetryingTransaction
{

    /**
     * Perform a set of operations under a single transaction.
     * Keep trying if the operation fails because of a concurrency issue.
     * @param callback The worker.
     * @param write Whether this is a write operation.
     */
    public void perform(RetryingTransactionCallback callback, boolean write);

}