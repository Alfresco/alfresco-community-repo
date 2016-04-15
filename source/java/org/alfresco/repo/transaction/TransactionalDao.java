package org.alfresco.repo.transaction;


/**
 * Contract for a DAO to interact with a transaction.
 * 
 * @author davidc
 */
public interface TransactionalDao
{
    /**
     * Are there any pending changes which must be synchronized with the store?
     * 
     * @return true => changes are pending
     */
    public boolean isDirty();
    
    /**
     * This callback provides a chance for the DAO to do any pre-commit work.
     * 
     * @param readOnly          <tt>true</tt> if the transaction was read-only
     * 
     * @since 1.4.5
     */
    public void beforeCommit(boolean readOnly);
}
