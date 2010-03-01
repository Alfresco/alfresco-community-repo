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
package org.alfresco.repo.lock;

import java.util.TreeSet;

import org.alfresco.repo.domain.locks.LockDAO;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * {@inheritDoc JobLockService}
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class JobLockServiceImpl implements JobLockService
{
    private static final String KEY_RESOURCE_LOCKS = "JobLockServiceImpl.Locks";
    
    private static Log logger = LogFactory.getLog(JobLockServiceImpl.class);
    
    private LockDAO lockDAO;
    private RetryingTransactionHelper retryingTransactionHelper;
    private int defaultRetryCount;
    private long defaultRetryWait;
    
    /**
     * Stateless listener that does post-transaction cleanup.
     */
    private final LockTransactionListener txnListener;
    
    public JobLockServiceImpl()
    {
        defaultRetryWait = 20;
        defaultRetryCount = 10;
        txnListener = new LockTransactionListener();
    }

    /**
     * Set the lock DAO
     */
    public void setLockDAO(LockDAO lockDAO)
    {
        this.lockDAO = lockDAO;
    }

    /**
     * Set the helper that will handle low-level concurrency conditions i.e. that
     * enforces optimistic locking and deals with stale state issues.
     */
    public void setRetryingTransactionHelper(RetryingTransactionHelper retryingTransactionHelper)
    {
        this.retryingTransactionHelper = retryingTransactionHelper;
    }

    /**
     * Set the maximum number of attempts to make at getting a lock
     * @param defaultRetryCount         the number of attempts
     */
    public void setDefaultRetryCount(int defaultRetryCount)
    {
        this.defaultRetryCount = defaultRetryCount;
    }

    /**
     * Set the default time to wait between attempts to acquire a lock
     * @param defaultRetryWait          the wait time in milliseconds
     */
    public void setDefaultRetryWait(long defaultRetryWait)
    {
        this.defaultRetryWait = defaultRetryWait;
    }

    /**
     * {@inheritDoc}
     */
    public void getTransactionalLock(QName lockQName, long timeToLive)
    {
        getTransactionalLock(lockQName, timeToLive, defaultRetryWait, defaultRetryCount);
    }

    /**
     * {@inheritDoc}
     */
    public void getTransactionalLock(QName lockQName, long timeToLive, long retryWait, int retryCount)
    {
        // Check that transaction is present
        final String txnId = AlfrescoTransactionSupport.getTransactionId();
        if (txnId == null)
        {
            throw new IllegalStateException("Locking requires an active transaction");
        }
        // Get the set of currently-held locks
        TreeSet<QName> heldLocks = TransactionalResourceHelper.getTreeSet(KEY_RESOURCE_LOCKS);
        // We don't want the lock registered as being held if something goes wrong
        TreeSet<QName> heldLocksTemp = new TreeSet<QName>(heldLocks);
        boolean added = heldLocksTemp.add(lockQName);
        if (!added)
        {
            // It's a refresh.  Ordering is not important here as we already hold the lock.
            refreshLock(txnId, lockQName, timeToLive);
        }
        else
        {
            QName lastLock = heldLocksTemp.last();
            if (lastLock.equals(lockQName))
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug(
                            "Attempting to acquire ordered lock: \n" +
                            "   Lock:     " + lockQName + "\n" +
                            "   TTL:      " + timeToLive + "\n" +
                            "   Txn:      " + txnId);
                }
                // If it was last in the set, then the order is correct and we use the
                // full retry behaviour.
                getLockImpl(txnId, lockQName, timeToLive, retryWait, retryCount);
            }
            else
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug(
                            "Attempting to acquire UNORDERED lock: \n" +
                            "   Lock:     " + lockQName + "\n" +
                            "   TTL:      " + timeToLive + "\n" +
                            "   Txn:      " + txnId);
                }
                // The lock request is made out of natural order.
                // Unordered locks do not get any retry behaviour
                getLockImpl(txnId, lockQName, timeToLive, retryWait, 1);
            }
        }
        // It went in, so add it to the transactionally-stored set
        heldLocks.add(lockQName);
        // Done
    }

    /**
     * {@inheritDoc}
     * 
     * @see #getLock(QName, long, long, int)
     */
    public String getLock(QName lockQName, long timeToLive)
    {
        return getLock(lockQName, timeToLive, defaultRetryWait, defaultRetryCount);
    }
    
    /**
     * {@inheritDoc}
     */
    public String getLock(QName lockQName, long timeToLive, long retryWait, int retryCount)
    {
        String lockToken = GUID.generate();
        getLockImpl(lockToken, lockQName, timeToLive, retryWait, retryCount);
        // Done
        return lockToken;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws LockAcquisitionException on failure
     */
    public void refreshLock(final String lockToken, final QName lockQName, final long timeToLive)
    {
        RetryingTransactionCallback<Object> refreshLockCallback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                lockDAO.refreshLock(lockQName, lockToken, timeToLive);
                return null;
            }
        };
        try
        {
            // It must succeed
            retryingTransactionHelper.doInTransaction(refreshLockCallback, false, true);
            // Success
            if (logger.isDebugEnabled())
            {
                logger.debug(
                        "Refreshed Lock: \n" +
                        "   Lock:     " + lockQName + "\n" +
                        "   TTL:      " + timeToLive + "\n" +
                        "   Txn:      " + lockToken);
            }
        }
        catch (LockAcquisitionException e)
        {
            // Failure
            if (logger.isDebugEnabled())
            {
                logger.debug(
                        "Lock refresh failed: \n" +
                        "   Lock:     " + lockQName + "\n" +
                        "   TTL:      " + timeToLive + "\n" +
                        "   Txn:      " + lockToken + "\n" +
                        "   Error:    " + e.getMessage());
            }
            throw e;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void releaseLock(final String lockToken, final QName lockQName)
    {
        RetryingTransactionCallback<Void> releaseCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                lockDAO.releaseLock(lockQName, lockToken);
                return null;
            }
        };
        retryingTransactionHelper.doInTransaction(releaseCallback, false, true);
    }

    /**
     * @throws LockAcquisitionException on failure
     */
    private void getLockImpl(final String lockToken, final QName lockQName, final long timeToLive, long retryWait, int retryCount)
    {
        RetryingTransactionCallback<Object> getLockCallback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                lockDAO.getLock(lockQName, lockToken, timeToLive);
                return null;
            }
        };
        try
        {
            int iterations = doWithRetry(getLockCallback, retryWait, retryCount);
            // Bind in a listener, if we are in a transaction
            if (AlfrescoTransactionSupport.getTransactionReadState() != TxnReadState.TXN_NONE)
            {
                AlfrescoTransactionSupport.bindListener(txnListener);
            }
            // Success
            if (logger.isDebugEnabled())
            {
                logger.debug(
                        "Acquired Lock: \n" +
                        "   Lock:     " + lockQName + "\n" +
                        "   TTL:      " + timeToLive + "\n" +
                        "   Txn:      " + lockToken + "\n" +
                        "   Attempts: " + iterations);
            }
        }
        catch (LockAcquisitionException e)
        {
            // Failure
            if (logger.isDebugEnabled())
            {
                logger.debug(
                        "Lock acquisition failed: \n" +
                        "   Lock:     " + lockQName + "\n" +
                        "   TTL:      " + timeToLive + "\n" +
                        "   Txn:      " + lockToken + "\n" +
                        "   Error:    " + e.getMessage());
            }
            throw e;
        }
    }
    
    /**
     * Does the high-level retrying around the callback
     */
    private int doWithRetry(RetryingTransactionCallback<? extends Object> callback, long retryWait, int retryCount)
    {
        int iteration = 0;
        LockAcquisitionException lastException = null;
        while (iteration++ < retryCount)
        {
            try
            {
                retryingTransactionHelper.doInTransaction(callback, false, true);
                // Success.  Clear the exception indicator! 
                lastException = null;
                break;
            }
            catch (LockAcquisitionException e)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Lock attempt " + iteration + " of " + retryCount + " failed: " + e.getMessage());
                }
                lastException = e;
                if (iteration >= retryCount)
                {
                    // Avoid an unnecessary wait if this is the last attempt
                    break;
                }
            }
            // Before running again, do a wait
            synchronized(callback)
            {
                try { callback.wait(retryWait); } catch (InterruptedException e) {}
            }
        }
        if (lastException == null)
        {
            // Success
            return iteration;
        }
        else
        {
            // Failure
            throw lastException;
        }
    }
    
    /**
     * Handles the transction synchronization activity, ensuring locks are rolled back as
     * required.
     * 
     * @author Derek Hulley
     * @since 3.2
     */
    private class LockTransactionListener extends TransactionListenerAdapter
    {
        /**
         * Release any open locks with extreme prejudice i.e. the commit will fail if the
         * locks cannot be released.  The locks are released in a single transaction -
         * ordering is therefore not important.  Should this fail, the post-commit phase
         * will do a final cleanup with individual locks.
         */
        @Override
        public void beforeCommit(boolean readOnly)
        {
            final String txnId = AlfrescoTransactionSupport.getTransactionId();
            final TreeSet<QName> heldLocks = TransactionalResourceHelper.getTreeSet(KEY_RESOURCE_LOCKS);
            // Shortcut if there are no locks
            if (heldLocks.size() == 0)
            {
                return;
            }
            // Clean up the locks
            RetryingTransactionCallback<Object> releaseCallback = new RetryingTransactionCallback<Object>()
            {
                public Object execute() throws Throwable
                {
                    // Any one of the them could fail
                    for (QName lockQName : heldLocks)
                    {
                        lockDAO.releaseLock(lockQName, txnId);
                    }
                    return null;
                }
            };
            retryingTransactionHelper.doInTransaction(releaseCallback, false, true);
            // So they were all successful
            heldLocks.clear();
        }

        /**
         * This will be called if something went wrong.  It might have been the lock releases, but
         * it could be anything else as well.  Each remaining lock is released with warnings where
         * it fails.
         */
        @Override
        public void afterRollback()
        {
            final String txnId = AlfrescoTransactionSupport.getTransactionId();
            final TreeSet<QName> heldLocks = TransactionalResourceHelper.getTreeSet(KEY_RESOURCE_LOCKS);
            // Shortcut if there are no locks
            if (heldLocks.size() == 0)
            {
                return;
            }
            // Clean up any remaining locks
            for (final QName lockQName : heldLocks)
            {
                RetryingTransactionCallback<Object> releaseCallback = new RetryingTransactionCallback<Object>()
                {
                    public Object execute() throws Throwable
                    {
                        lockDAO.releaseLock(lockQName, txnId);
                        return null;
                    }
                };
                try
                {
                    retryingTransactionHelper.doInTransaction(releaseCallback, false, true);
                }
                catch (Throwable e)
                {
                    // There is no point propagating this, so just log a warning and
                    // hope that it expires soon enough
                    logger.warn(
                            "Failed to release a lock in 'afterRollback':\n" +
                            "   Lock Name:  " + lockQName + "\n" +
                            "   Lock Token: " + txnId,
                            e);
                }
            }
        }
    }
}
