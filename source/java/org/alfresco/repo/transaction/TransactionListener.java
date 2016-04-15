package org.alfresco.repo.transaction;

/**
 * Listener for Alfresco-specific transaction callbacks.
 *
 * @see org.alfresco.repo.transaction.AlfrescoTransactionSupport
 * 
 * @author Derek Hulley
 */
public interface TransactionListener extends org.alfresco.util.transaction.TransactionListener
{
    /**
     * @deprecated      No longer supported
     */
    void flush();
    
    /**
     * Called before a transaction is committed.
     * <p>
     * All transaction resources are still available.
     * 
     * @param readOnly true if the transaction is read-only
     */
    void beforeCommit(boolean readOnly);
    
    /**
     * Invoked before transaction commit/rollback.  Will be called after
     * {@link #beforeCommit(boolean) } even if {@link #beforeCommit(boolean)}
     * failed.
     * <p>
     * All transaction resources are still available.
     */
    void beforeCompletion();
    
    /**
     * Invoked after transaction commit.
     * <p>
     * Any exceptions generated here will only be logged and will have no effect
     * on the state of the transaction.
     * <p>
     * Although all transaction resources are still available, this method should
     * be used only for cleaning up resources after a commit has occured.
     */
    void afterCommit();

    /**
     * Invoked after transaction rollback.
     * <p>
     * Any exceptions generated here will only be logged and will have no effect
     * on the state of the transaction.
     * <p>
     * Although all transaction resources are still available, this method should
     * be used only for cleaning up resources after a rollback has occured.
     */
    void afterRollback();
    
}
