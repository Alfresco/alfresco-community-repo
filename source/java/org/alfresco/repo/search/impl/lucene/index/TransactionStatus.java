package org.alfresco.repo.search.impl.lucene.index;


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
    
    // Match the order in javax.transaction.Status so ordinal values are correct
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
    },

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
    },

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
    },

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
    },

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
    },

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
    },

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
    },

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
    },

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
    },

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
    },

    /*
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
    },

    /*
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
    },

    /*
     * These index overlays require reindexing
     */
//    COMMITTED_REQUIRES_REINDEX
//    {
//        public boolean isCommitted()
//        {
//            return true;
//        }
//
//        public boolean isTransient()
//        {
//            return false;
//        }
//        
//        public boolean canBeReordered()
//        {
//            return false;
//        }
//        
//        public boolean follows(TransactionStatus previous)
//        {
//            return false;
//        }
//    },

    /*
     * These index overlays are reindexing
     */
//    COMMITTED_REINDEXING
//    {
//        public boolean isCommitted()
//        {
//            return true;
//        }
//
//        
//        public boolean canBeReordered()
//        {
//            return false;
//        }
//        
//        public boolean isTransient()
//        {
//            return false;
//        }
//        
//        public boolean follows(TransactionStatus previous)
//        {
//            return false;
//        }
//    },

    /*
     * These index overlays have ben reindexed.
     */
//    COMMITTED_REINDEXED
//    {
//        public boolean isCommitted()
//        {
//            return true;
//        }
//
//        public boolean isTransient()
//        {
//            return false;
//        }
//        
//        public boolean canBeReordered()
//        {
//            return false;
//        }
//        
//        public boolean follows(TransactionStatus previous)
//        {
//            return false;
//        }
//    },

    /*
     * Committed but the index still has deletions
     */

//    COMMITTED_WITH_DELETIONS
//    {
//        public boolean isCommitted()
//        {
//            return true;
//        }
//
//        public boolean isTransient()
//        {
//            return false;
//        }
//        
//        public boolean canBeReordered()
//        {
//            return false;
//        }
//        
//        public boolean follows(TransactionStatus previous)
//        {
//            return false;
//        }
//    },

    /*
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
    },

    /*
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
    };

    public abstract boolean isCommitted();
    
    public abstract boolean isTransient();
    
    public abstract boolean canBeReordered();

    public abstract boolean follows(TransactionStatus previous);

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