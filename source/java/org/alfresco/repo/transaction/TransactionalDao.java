package org.alfresco.repo.transaction;


/**
 * Contract for a DAO to interact with a transaction.
 * 
 * @author davidc
 */
public interface TransactionalDao
{
    /**
     * Allows the dao to flush any consuming resources.  This mechanism is
     * used primarily during long-lived transactions to ensure that system resources
     * are not used up.
     * <p>
     * This method must not be used for implementing business logic.
     */
    void flush();
    
    /**
     * Are there any pending changes which must be synchronized with the store?
     * 
     * @return true => changes are pending
     */
    public boolean isDirty();
    
    /**
     * This callback provides a chance for the DAO to do any pre-commit work.
     * 
     * @since 1.4.5
     */
    public void beforeCommit();
}
