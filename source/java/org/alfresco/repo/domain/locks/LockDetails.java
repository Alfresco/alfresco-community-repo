package org.alfresco.repo.domain.locks;

import org.alfresco.service.namespace.QName;

/**
 * Class to contain details regarding a lock.  A lock is specific to a given
 * qualified name.  For any given lock, there may exist an <b>EXCLUSIVE</b>
 * lock <u>or</u> several <b>SHARED</b> locks.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class LockDetails
{
    /**
     * The type of lock
     * 
     * @author Derek Hulley
     * @since 3.2
     */
    public enum LockType
    {
        /**
         * A lock held by a specific transaction.  No other (exclusive or shared) locks
         * may be held for the same qualified name.
         */
        EXCLUSIVE,
        /**
         * A lock that may be held by several transactions when no exclusive lock is held
         * for the same qualified name.
         */
        SHARED;
    }
    
    private final String txnId;
    private final QName lockQName;
    private final LockType lockType;

    /**
     * 
     * @param txnId             the transaction holding the lock
     * @param lockQName         the qualified name of the lock
     * @param lockType          the type of lock
     */
    public LockDetails(String txnId, QName lockQName, LockType lockType)
    {
        this.txnId = txnId;
        this.lockQName = lockQName;
        this.lockType = lockType;
    }

    /**
     * @return                  Returns the transaction holding the lock
     */
    public String getTxnId()
    {
        return txnId;
    }

    /**
     * @return                  Returns the qualified name of the lock
     */
    public QName getLockQName()
    {
        return lockQName;
    }

    /**
     * @return                  Returns the lock type
     */
    public LockType getLockType()
    {
        return lockType;
    }
}
