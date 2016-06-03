package org.alfresco.repo.search;

/**
 * Indexer implementations that work with spring transactions
 * @author andyh
 *
 */
public interface TransactionSynchronisationAwareIndexer
{
    /**
     * Commit
     */
    public void commit();
    /**
     * Rollback
     */
    public void rollback();
    /**
     * Prepare
     * @return the return tx state
     */
    public int prepare();
    /**
     * Report if there are any chenges to commit
     * @return false if read only access (by use not declaration)
     */
    public boolean isModified();
}
