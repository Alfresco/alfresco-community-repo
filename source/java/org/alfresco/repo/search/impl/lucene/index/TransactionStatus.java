package org.alfresco.repo.search.impl.lucene.index;

/**
 * Status of indexes that make up the whole index.
 * This starts with the value from javax.transaction.Status.
 * 
 * Lifecycle
 * ---------
 * 
 * As a transaction starts, the delta is ACTIVE
 * It may be MARKED_ROLLBACK -> ROLLED BACK
 * -> PREPARING -> PREPARED -> COMMITTING -> COMMITTED...
 * with roll back at any time
 * 
 * If the index has any delayed indexing it commits to
 * COMMITTED_REQUIRES_REINDEX
 * and then the overlay can go from -> COMMITTED_REINDEXING -> COMMITTED_REINDEXED
 * 
 * If there was no reindexing required the delat commits as COMMITTED
 * 
 * A delta changes to an index overlay as it is committed.
 * 
 * For an overlay in COMMITTED or COMMITTED_REINDEXED it can have its delete list applied 
 * to sub indexes. At this point it becomes a sub index.
 * 
 * @author Andy Hind
 */

public enum TransactionStatus
{
    // Match the order in javax.transaction.Status so ordinal values are correct
    ACTIVE,
    MARKED_ROLLBACK,
    PREPARED,
    COMMITTED,
    ROLLEDBACK,
    UNKNOWN,
    NO_TRANSACTION,
    PREPARING,
    COMMITTING,
    ROLLINGBACK,
    
    /*
     * This entry is the source for an active merge.
     * The result will be in a new index.
     */
    MERGING,
    
    /*
     * A new index element that is being made by a merge.
     */
    MERGING_TARGET,
    
    /*
     * These index overlays require reindexing
     */
    COMMITTED_REQUIRES_REINDEX,
    
    /*
     *  These index overlays are reindexing
     */
    COMMITTED_REINDEXING,
    
    /*
     * These index overlays have ben reindexed.
     */
    COMMITTED_REINDEXED,
    
    /*
     * An entry that may be deleted
     */
    DELETABLE;
}