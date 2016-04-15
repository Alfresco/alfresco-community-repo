package org.alfresco.repo.domain.control;

import java.sql.Savepoint;

/**
 * DAO services for database control statements.  It is sometimes necessary to
 * issue control statements on a database connection; these are not usually
 * supported in the ANSI SQL standard.
 * 
 * @author Derek Hulley
 * @since 3.2SP1
 */
public interface ControlDAO
{
    /**
     * Begin batching prepared statements for later execution.
     * 
     * @see #executeBatch()
     */
    public void startBatch();
    
    /**
     * Execute statements that were queued for batching.
     * 
     * @see #startBatch()
     */
    public void executeBatch();
    
    /**
     * Create a "Save Point" in the current transaction, for later selective rollback.
     * Creation <b>must</b> be accompanied by a matching {@link #rollbackToSavepoint(Savepoint)}
     * or {@link #releaseSavepoint(Savepoint)}.
     * <code><pre>
     *  Savepoint savepoint = controlDAO.createSavepoint("functionF");
     *  try
     *  {
     *      // Do something that could fail e.g. blind insert that might violate unique constraints
     *      ...
     *      // Success, so remove savepoint or risk crashing on long-running transactions
     *      controlDAO.releaseSavepoint(savepoint);
     *  }
     *  catch (Throwable e)
     *  {
     *      controlDAO.rollbackToSavepoint(savepoint);
     *      // Throw something that client code might be able to react to or try something else
     *      ...
     *  }
     * </pre></code>
     * 
     * @param savepoint             the name of the save point
     * @return                      Returns the handle to the savepoint or <tt>null</tt> if the
     *                              implementation does not support it
     */
    public Savepoint createSavepoint(String savepoint);

    /**
     * Roll back to a previously-created "Save Point", discarding any intervening
     * changes to the current transaction.
     * 
     * @param savepoint             a previously-created savepoint
     * 
     * @see #createSavepoint(String)
     */
    public void rollbackToSavepoint(Savepoint savepoint);

    /**
     * Remove a previously-created "Save Point", writing any intervening updates
     * into the current transaction.
     * 
     * @param savepoint             the name of the save point
     * 
     * @see #createSavepoint(String)
     */
    public void releaseSavepoint(Savepoint savepoint);
    
    /**
     * Change the current transaction isolation level.
     * <p/>
     * <b>Note:</b> The isolation level should not - and for some DBs, cannot - be changed
     *              except at the very start of the transaction
     * 
     * @param isolationLevel        the transaction isolation level
     * @return                      Returns the previously-set isolation
     * @throws IllegalStateException    if the isolation level is invalid or cannot be changed
     */
    public int setTransactionIsolationLevel(int isolationLevel);
}
