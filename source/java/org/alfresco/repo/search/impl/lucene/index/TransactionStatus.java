/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
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
            return (previous == null) || (previous == ACTIVE);
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
            return previous.allowsRollbackOrMark(previous) || (previous == MARKED_ROLLBACK);
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
            return (previous == TransactionStatus.PREPARING) || (previous == PREPARED);
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
            return (previous == TransactionStatus.COMMITTING) || (previous == COMMITTED);
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
            return (previous == TransactionStatus.ROLLINGBACK) || (previous == ROLLEDBACK);
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
            return (previous == UNKNOWN);
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
            return (previous == NO_TRANSACTION);
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
            return (previous == TransactionStatus.ACTIVE) || (previous == PREPARING);
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
            return (previous == TransactionStatus.PREPARED) || (previous == COMMITTING);
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
            return previous.allowsRollbackOrMark(previous) || (previous == ROLLINGBACK);
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
            return (previous == MERGE);
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
            return (previous == MERGE_TARGET);
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
            return (previous == COMMITTED_DELETING);
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