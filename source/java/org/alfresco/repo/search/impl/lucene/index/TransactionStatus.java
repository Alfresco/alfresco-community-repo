package org.alfresco.repo.search.impl.lucene.index;

import javax.transaction.Status;


/**
 * Status of indexes that make up the whole index. This starts with the value from javax.transaction.Status.
 * 
 * Lifecycle ---------
 * 
 * As a transaction starts, the delta is ACTIVE It may be MARKED_ROLLBACK -> ROLLED BACK -> PREPARING -> PREPARED -> COMMITTING -> COMMITTED... with roll back at any time
 * 
 * If the index has any delayed indexing it commits to COMMITTED_REQUIRES_REINDEX and then the overlay can go from -> COMMITTED_REINDEXING -> COMMITTED_REINDEXED
 * 
 * If there was no reindexing required the delat commits as COMMITTED
 * 
 * A delta changes to an index overlay as it is committed.
 * 
 * For an overlay in COMMITTED or COMMITTED_REINDEXED it can have its delete list applied to sub indexes. At this point it becomes a sub index.
 * 
 * @author Andy Hind
 */

public enum TransactionStatus
{
    
    /**
     * Active TX
     */
    ACTIVE
    {
        public boolean isCommitted()
        {
            return false;
        }
        
        public boolean isTransient()
        {
            return true;
        }
        
        public boolean canBeReordered()
        {
            return true;
        }
        
        public boolean follows(TransactionStatus previous)
        {
            return previous == null;
        }
        
        public int getStatus()
        {
            return Status.STATUS_ACTIVE;
        }
    },

    /**
     * TX marked for rollback
     */
    MARKED_ROLLBACK
    {
        public boolean isCommitted()
        {
            return false;
        }
        
        public boolean isTransient()
        {
            return true;
        }
        
        public boolean canBeReordered()
        {
            return true;
        }
        
        public boolean follows(TransactionStatus previous)
        {
            return previous.allowsRollbackOrMark(previous);
        }
        
        public int getStatus()
        {
            return Status.STATUS_MARKED_ROLLBACK;
        }
    },

    /**
     * TX prepared
     */
    PREPARED
    {
        public boolean isCommitted()
        {
            return false;
        }
        
        public boolean isTransient()
        {
            return false;
        }

        public boolean canBeReordered()
        {
            return false;
        }
        
        public boolean follows(TransactionStatus previous)
        {
            return previous == TransactionStatus.PREPARING;
        }
        
        public int getStatus()
        {
            return Status.STATUS_PREPARED;
        }
    },

    /**
     * TX Committed
     */
    COMMITTED
    {
        public boolean isCommitted()
        {
            return true;
        }

        public boolean isTransient()
        {
            return false;
        }
        
        public boolean canBeReordered()
        {
            return false;
        }
        
        public boolean follows(TransactionStatus previous)
        {
            return previous == TransactionStatus.COMMITTING;
        }
        
        public int getStatus()
        {
            return Status.STATUS_COMMITTED;
        }
    },

    /**
     * TX rolled back
     */
    ROLLEDBACK
    {
        public boolean isCommitted()
        {
            return false;
        }
        
        public boolean isTransient()
        {
            return true;
        }
        
        public boolean canBeReordered()
        {
            return true;
        }
        
        public boolean follows(TransactionStatus previous)
        {
            return previous == TransactionStatus.ROLLINGBACK;
        }
        
        public int getStatus()
        {
            return Status.STATUS_ROLLEDBACK;
        }
    },

    /**
     * TX state is unknown
     */
    UNKNOWN
    {
        public boolean isCommitted()
        {
            return false;
        }
        
        public boolean isTransient()
        {
            return true;
        }
        
        public boolean canBeReordered()
        {
            return true;
        }
        
        public boolean follows(TransactionStatus previous)
        {
            return false;
        }
        
        public int getStatus()
        {
            return Status.STATUS_UNKNOWN;
        }
    },

    /**
     * No transaction
     */
    NO_TRANSACTION
    {
        public boolean isCommitted()
        {
            return false;
        }
        
        public boolean isTransient()
        {
            return true;
        }
        
        public boolean canBeReordered()
        {
            return true;
        }
        
        public boolean follows(TransactionStatus previous)
        {
            return false;
        }
        
        public int getStatus()
        {
            return Status.STATUS_NO_TRANSACTION;
        }
    },

    /**
     * TX is preparing
     */
    PREPARING
    {
        public boolean isCommitted()
        {
            return false;
        }
        
        public boolean isTransient()
        {
            return true;
        }
        
        public boolean canBeReordered()
        {
            return true;
        }
        
        public boolean follows(TransactionStatus previous)
        {
            return previous == TransactionStatus.ACTIVE;
        }
        
        public int getStatus()
        {
            return Status.STATUS_PREPARING;
        }
    },

    /**
     * TX is committing
     */
    COMMITTING
    {
        public boolean isCommitted()
        {
            return false;
        }
        
        public boolean isTransient()
        {
            return true;
        }
        
        public boolean canBeReordered()
        {
            return false;
        }
        
        public boolean follows(TransactionStatus previous)
        {
            return previous == TransactionStatus.PREPARED;
        }
        
        public int getStatus()
        {
            return Status.STATUS_COMMITTING;
        }
    },

    /**
     * TX rolling back
     */
    ROLLINGBACK
    {
        public boolean isCommitted()
        {
            return false;
        }

        public boolean isTransient()
        {
            return true;
        }
        
        public boolean canBeReordered()
        {
            return true;
        }
        
        public boolean follows(TransactionStatus previous)
        {
            return previous.allowsRollbackOrMark(previous);
        }
        
        public int getStatus()
        {
            return Status.STATUS_ROLLING_BACK;
        }
    },

    /**
     * This entry is the source for an active merge. The result will be in a new index.
     */
    MERGE
    {
        public boolean isCommitted()
        {
            return true;
        }

        public boolean isTransient()
        {
            return false;
        }
        
        public boolean canBeReordered()
        {
            return false;
        }
        
        public boolean follows(TransactionStatus previous)
        {
            return false;
        }
        
        public int getStatus()
        {
            return Status.STATUS_COMMITTED;
        }
    },

    /**
     * A new index element that is being made by a merge.
     */
    MERGE_TARGET
    {
        public boolean isCommitted()
        {
            return false;
        }
        
        public boolean isTransient()
        {
            return false;
        }
        
        public boolean canBeReordered()
        {
            return false;
        }
        
        public boolean follows(TransactionStatus previous)
        {
            return false;
        }
        
        public int getStatus()
        {
            return Status.STATUS_ACTIVE;
        }
    },


    /**
     * Pending deleted are being committed to for the delta.
     */
    COMMITTED_DELETING
    {
        public boolean isCommitted()
        {
            return true;
        }

        public boolean isTransient()
        {
            return false;
        }
        
        public boolean canBeReordered()
        {
            return false;
        }
        
        public boolean follows(TransactionStatus previous)
        {
            return false;
        }
        
        public int getStatus()
        {
            return Status.STATUS_COMMITTED;
        }
    },

    /**
     * An entry that may be deleted
     */
    DELETABLE
    {
        public boolean isCommitted()
        {
            return false;
        }
        
        public boolean isTransient()
        {
            return false;
        }
        
        public boolean canBeReordered()
        {
            return false;
        }
        
        public boolean follows(TransactionStatus previous)
        {
            return true;
        }
        
        public int getStatus()
        {
            return Status.STATUS_UNKNOWN;
        }
    };

    /**
     * Is this a commited inex entry?
     * @return - true if committed
     */
    public abstract boolean isCommitted();
    
    /**
     * Is this transient 
     * @return - true if no information needs to be persisted
     */
    public abstract boolean isTransient();
    
    /**
     * Can this be reordered with respect to other TXs
     * @return - true if this can be reordered (fixed after prepare)
     */
    public abstract boolean canBeReordered();

    /**
     * Can this state follow the one given?
     * @param previous state
     * @return - true if transition to this state is allowed
     */
    public abstract boolean follows(TransactionStatus previous);
    
    /**
     * Get the javax.transaction.Status best matching this state
     * 
     * @return - the int TX state
     */
    public abstract int getStatus();

    private boolean allowsRollbackOrMark(TransactionStatus previous)
    {
        switch (previous)
        {
        case ACTIVE:
        case MARKED_ROLLBACK:
        case PREPARED:
        case PREPARING:
        case COMMITTING:
            return true;
        default:
            return false;
        }
    }
}