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
package org.alfresco.repo.transaction;

import java.lang.reflect.Method;
import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import net.sf.ehcache.distribution.RemoteCacheException;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.error.ExceptionStackUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.StaleObjectStateException;
import org.hibernate.StaleStateException;
import org.hibernate.cache.CacheException;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.LockAcquisitionException;
import org.hibernate.exception.SQLGrammarException;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.jdbc.JdbcUpdateAffectedIncorrectNumberOfRowsException;
import org.springframework.jdbc.UncategorizedSQLException;

import com.ibatis.common.jdbc.exception.NestedSQLException;

/**
 * A helper that runs a unit of work inside a UserTransaction,
 * transparently retrying the unit of work if the cause of
 * failure is an optimistic locking or deadlock condition.
 * <p>
 * Defaults:
 * <ul>
 *   <li><b>maxRetries: 20</b></li>
 *   <li><b>minRetryWaitMs: 100</b></li>
 *   <li><b>maxRetryWaitMs: 2000</b></li>
 *   <li><b>retryWaitIncrementMs: 100</b></li>
 * </ul>
 * <p>
 * To get details of 'why' transactions are retried use the following log level:<br>
 * <b>Summary: log4j.logger.org.alfresco.repo.transaction.RetryingTransactionHelper=INFO</b><br>
 * <b>Details: log4j.logger.org.alfresco.repo.transaction.RetryingTransactionHelper=DEBUG</b><br>
 * 
 *
 * @author Derek Hulley
 */
public class RetryingTransactionHelper
{
    private static final String MSG_READ_ONLY = "permissions.err_read_only";
    private static final String KEY_ACTIVE_TRANSACTION = "RetryingTransactionHelper.ActiveTxn";
    private static Log    logger = LogFactory.getLog(RetryingTransactionHelper.class);

    /**
     * Exceptions that trigger retries.
     */
    @SuppressWarnings("unchecked")
    public static final Class[] RETRY_EXCEPTIONS;
    static
    {
        RETRY_EXCEPTIONS = new Class[] {
                ConcurrencyFailureException.class,
                DeadlockLoserDataAccessException.class,
                StaleObjectStateException.class,
                JdbcUpdateAffectedIncorrectNumberOfRowsException.class,     // Similar to StaleObjectState
                LockAcquisitionException.class,
                ConstraintViolationException.class,
                UncategorizedSQLException.class,
                SQLException.class,
                NestedSQLException.class,
                BatchUpdateException.class,
                DataIntegrityViolationException.class,
                StaleStateException.class,
                ObjectNotFoundException.class,
                CacheException.class,                       // Usually a cache replication issue
                RemoteCacheException.class,                 // A cache replication issue
                SQLGrammarException.class // Actually specific to MS SQL Server 2005 - we check for this
                };
    }

    /**
     * Reference to the TransactionService instance.
     */
    private TransactionService txnService;

//    /** Performs post-failure exception neatening */
//    private ExceptionTransformer exceptionTransformer;
    /** The maximum number of retries. -1 for infinity. */
    private int maxRetries;
    /** The minimum time to wait between retries. */
    private int minRetryWaitMs;
    /** The maximum time to wait between retries. */
    private int maxRetryWaitMs;
    /** How much to increase the wait time with each retry. */
    private int retryWaitIncrementMs;

    /**
     * Optional time limit for execution time. When non-zero, retries will not continue when the projected time is
     * beyond this time.
     */
    private long maxExecutionMs;

    /** Map of transaction start times to thread stack traces. Only maintained when maxExecutionMs is set. */
    private SortedMap <Long, List<Throwable>> txnsInProgress = new TreeMap<Long, List<Throwable>>();
    
    /** The number of concurrently exeucting transactions. Only maintained when maxExecutionMs is set. */
    private int txnCount;
    
    /**
     * Whether the the transactions may only be reads
     */
    private boolean readOnly;
    
    /**
     * Random number generator for retry delays.
     */
    private Random random;

    /**
     * Callback interface
     * @author Derek Hulley
     */
    public interface RetryingTransactionCallback<Result>
    {
        /**
         * Perform a unit of transactional work.
         *
         * @return              Return the result of the unit of work
         * @throws Throwable    This can be anything and will guarantee either a retry or a rollback
         */
        public Result execute() throws Throwable;
    };

    /**
     * Default constructor.
     */
    public RetryingTransactionHelper()
    {
        this.random = new Random(System.currentTimeMillis());
        this.maxRetries = 20;
        this.minRetryWaitMs = 100;
        this.maxRetryWaitMs = 2000;
        this.retryWaitIncrementMs = 100;
    }

    // Setters.

//    /**
//     * Optionally set the component that will transform or neaten any exceptions that are
//     * propagated.
//     */
//    public void setExceptionTransformer(ExceptionTransformer exceptionTransformer)
//    {
//        this.exceptionTransformer = exceptionTransformer;
//    }
//
    /**
     * Set the TransactionService.
     */
    public void setTransactionService(TransactionService service)
    {
        this.txnService = service;
    }

    /**
     * Set the maximimum number of retries. -1 for infinity.
     */
    public void setMaxRetries(int maxRetries)
    {
        this.maxRetries = maxRetries;
    }

    public void setMinRetryWaitMs(int minRetryWaitMs)
    {
        this.minRetryWaitMs = minRetryWaitMs;
    }

    public void setMaxRetryWaitMs(int maxRetryWaitMs)
    {
        this.maxRetryWaitMs = maxRetryWaitMs;
    }

    public void setRetryWaitIncrementMs(int retryWaitIncrementMs)
    {
        if (retryWaitIncrementMs <= 0)
        {
            throw new IllegalArgumentException("'retryWaitIncrementMs' must be a positive integer.");
        }
        this.retryWaitIncrementMs = retryWaitIncrementMs;
    }

    public void setMaxExecutionMs(long maxExecutionMs)
    {
        this.maxExecutionMs = maxExecutionMs;
    }

    /**
     * Set whether this helper only supports read transactions.
     */
    public void setReadOnly(boolean readOnly)
    {
        this.readOnly = readOnly;
    }

    /**
     * Execute a callback in a transaction until it succeeds, fails
     * because of an error not the result of an optimistic locking failure,
     * or a deadlock loser failure, or until a maximum number of retries have
     * been attempted.
     * <p>
     * If there is already an active transaction, then the callback is merely
     * executed and any retry logic is left to the caller.  The transaction
     * will attempt to be read-write.
     *
     * @param cb                The callback containing the unit of work.
     * @return                  Returns the result of the unit of work.
     * @throws                  RuntimeException  all checked exceptions are converted
     */
    public <R> R doInTransaction(RetryingTransactionCallback<R> cb)
    {
        return doInTransaction(cb, false, false);
    }

    /**
     * Execute a callback in a transaction until it succeeds, fails
     * because of an error not the result of an optimistic locking failure,
     * or a deadlock loser failure, or until a maximum number of retries have
     * been attempted.
     * <p>
     * If there is already an active transaction, then the callback is merely
     * executed and any retry logic is left to the caller.
     *
     * @param cb                The callback containing the unit of work.
     * @param readOnly          Whether this is a read only transaction.
     * @return                  Returns the result of the unit of work.
     * @throws                  RuntimeException  all checked exceptions are converted
     */
    public <R> R doInTransaction(RetryingTransactionCallback<R> cb, boolean readOnly)
    {
        return doInTransaction(cb, readOnly, false);
    }

    /**
     * Execute a callback in a transaction until it succeeds, fails
     * because of an error not the result of an optimistic locking failure,
     * or a deadlock loser failure, or until a maximum number of retries have
     * been attempted.
     * <p>
     * It is possible to force a new transaction to be created or to partake in
     * any existing transaction.
     *
     * @param cb                The callback containing the unit of work.
     * @param readOnly          Whether this is a read only transaction.
     * @param requiresNew       <tt>true</tt> to force a new transaction or
     *                          <tt>false</tt> to partake in any existing transaction.
     * @return                  Returns the result of the unit of work.
     * @throws                  RuntimeException  all checked exceptions are converted
     */
    public <R> R doInTransaction(RetryingTransactionCallback<R> cb, boolean readOnly, boolean requiresNew)
    {
        if (this.readOnly && !readOnly)
        {
            throw new AccessDeniedException(MSG_READ_ONLY);
        }

        // First validate the requiresNew setting
		boolean startingNew = requiresNew;
        if (!startingNew)
        {
            TxnReadState readState = AlfrescoTransactionSupport.getTransactionReadState();
            switch (readState)
            {
                case TXN_READ_ONLY:
                    if (!readOnly)
                    {
                        // The current transaction is read-only, but a writable transaction is requested
                        throw new AlfrescoRuntimeException("Read-Write transaction started within read-only transaction");
                    }
                    // We are in a read-only transaction and this is what we require so continue with it.
                    break;
                case TXN_READ_WRITE:
                    // We are in a read-write transaction.  It cannot be downgraded so just continue with it.
                    break;
                case TXN_NONE:
                    // There is no current transaction so we need a new one.
                    startingNew = true;
                    break;
                default:
                    throw new RuntimeException("Unknown transaction state: " + readState);
            }
        }

        // If we are time limiting, set ourselves a time limit and maintain the count of concurrent transactions
        long startTime = 0;
		Throwable stackTrace = null;
        if (startingNew && maxExecutionMs > 0)
        {
            startTime = System.currentTimeMillis();
            synchronized (this)
            {
                if (txnCount > 0)
                {
                    // If this transaction would take us above our ceiling, reject it
                    long oldestStart = txnsInProgress.firstKey();
                    long oldestDuration = startTime - oldestStart;
                    if (oldestDuration > maxExecutionMs)
                    {
                        throw new TooBusyException("Too busy: " + txnCount + " transactions. Oldest " + oldestDuration + " milliseconds", txnsInProgress.get(oldestStart).get(0));
                    }
                }
				// Record the start time and stack trace of the starting thread
                List<Throwable> traces = txnsInProgress.get(startTime);
                if (traces == null)
                {
                    traces = new LinkedList<Throwable>();
                    txnsInProgress.put(startTime, traces);
                }
				stackTrace = new Exception("Stack trace");
                traces.add(stackTrace);
                ++txnCount;
            }
        }

        try
        {        
            // Track the last exception caught, so that we
            // can throw it if we run out of retries.
            RuntimeException lastException = null;
            for (int count = 0; count == 0 || count < maxRetries; count++)
            {
                UserTransaction txn = null;
                try
                {
                    if (startingNew)
                    {
                        txn = requiresNew ? txnService.getNonPropagatingUserTransaction(readOnly) : txnService
                                .getUserTransaction(readOnly);
                        txn.begin();
                        // Wrap it to protect it
                        UserTransactionProtectionAdvise advise = new UserTransactionProtectionAdvise();
                        ProxyFactory proxyFactory = new ProxyFactory(txn);
                        proxyFactory.addAdvice(advise);
                        UserTransaction wrappedTxn = (UserTransaction) proxyFactory.getProxy();
                        // Store the UserTransaction for static retrieval.  There is no need to unbind it
                        // because the transaction management will do that for us.
                        AlfrescoTransactionSupport.bindResource(KEY_ACTIVE_TRANSACTION, wrappedTxn);
                    }
                    // Do the work.
                    R result = cb.execute();
                    // Only commit if we 'own' the transaction.
                    if (txn != null)
                    {
                        if (txn.getStatus() == Status.STATUS_MARKED_ROLLBACK)
                        {
                            if (logger.isDebugEnabled())
                            {
                                logger.debug("\n" +
                                            "Transaction marked for rollback: \n" +
                                            "   Thread: " + Thread.currentThread().getName() + "\n" +
                                            "   Txn:    " + txn + "\n" +
                                            "   Iteration: " + count);
                            }
                            // Something caused the transaction to be marked for rollback
                            // There is no recovery or retrying with this
                            txn.rollback();
                        }
                        else
                        {
                            // The transaction hasn't been flagged for failure so the commit
                            // sould still be good.
                            txn.commit();
                        }
                    }
                    if (logger.isDebugEnabled())
                    {
                        if (count != 0)
                        {
                            logger.debug("\n" +
                                    "Transaction succeeded: \n" +
                                    "   Thread: " + Thread.currentThread().getName() + "\n" +
                                    "   Txn:    " + txn + "\n" +
                                    "   Iteration: " + count);
                        }
                    }
                    return result;
                }
                catch (Throwable e)
                {
                    // Somebody else 'owns' the transaction, so just rethrow.
                    if (txn == null)
                    {
                        RuntimeException ee = AlfrescoRuntimeException.makeRuntimeException(
                                e, "Exception from transactional callback: " + cb);
                        throw ee;
                    }
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("\n" +
                                "Transaction commit failed: \n" +
                                "   Thread: " + Thread.currentThread().getName() + "\n" +
                                "   Txn:    " + txn + "\n" +
                                "   Iteration: " + count + "\n" +
                                "   Exception follows:",
                                e);
                    }
                    // Rollback if we can.
                    if (txn != null)
                    {
                        try
                        {
                            int txnStatus = txn.getStatus();
                            // We can only rollback if a transaction was started (NOT NO_TRANSACTION) and
                            // if that transaction has not been rolled back (NOT ROLLEDBACK).
                            // If an exception occurs while the transaction is being created (e.g. no database connection)
                            // then the status will be NO_TRANSACTION.
                            if (txnStatus != Status.STATUS_NO_TRANSACTION && txnStatus != Status.STATUS_ROLLEDBACK)
                            {
                                txn.rollback();
                            }
                        }
                        catch (Throwable e1)
                        {
                            // A rollback failure should not preclude a retry, but logging of the rollback failure is required
                            logger.error("Rollback failure.  Normal retry behaviour will resume.", e1);
                        }
                    }
                    if (e instanceof RollbackException)
                    {
                        lastException = (e.getCause() instanceof RuntimeException) ?
                             (RuntimeException)e.getCause() : new AlfrescoRuntimeException("Exception in Transaction.", e.getCause());
                    }
                    else
                    {
                        lastException = (e instanceof RuntimeException) ?
                             (RuntimeException)e : new AlfrescoRuntimeException("Exception in Transaction.", e);
                    }
                    // Check if there is a cause for retrying
                    Throwable retryCause = extractRetryCause(e);
                    if (retryCause != null)
                    {
                        // Sleep a random amount of time before retrying.
                        // The sleep interval increases with the number of retries.
                        int sleepIntervalRandom = (count > 0 &&  retryWaitIncrementMs > 0)
                                                    ? random.nextInt(count * retryWaitIncrementMs)
                                                    : minRetryWaitMs;
                        int sleepInterval = Math.min(maxRetryWaitMs, sleepIntervalRandom);
                        sleepInterval = Math.max(sleepInterval, minRetryWaitMs);
                        if (logger.isInfoEnabled() && !logger.isDebugEnabled())
                        {
                            String msg = String.format(
                                    "Retrying %s: count %2d; wait: %1.1fs; msg: \"%s\"; exception: (%s)",
                                    Thread.currentThread().getName(),
                                    count, (double)sleepInterval/1000D,
                                    retryCause.getMessage(),
                                    retryCause.getClass().getName());
                            logger.info(msg);
                        }
                        try
                        {
                            Thread.sleep(sleepInterval);
                        }
                        catch (InterruptedException ie)
                        {
                            // Do nothing.
                        }
                        // Try again
                        continue;
                    }
                    else
                    {
                        // It was a 'bad' exception.
                        throw lastException;
                    }
                }
            }
            // We've worn out our welcome and retried the maximum number of times.
            // So, fail.
            throw lastException;
        }
        finally
        {
            if (startingNew && maxExecutionMs > 0)
            {
                synchronized (this)
                {
                    txnCount--;
                    List<Throwable> traces = txnsInProgress.get(startTime);
                    if (traces != null)
                    {
                        if (traces.size() == 1)
                        {
                            txnsInProgress.remove(startTime);
                        }
                        else
                        {
                            traces.remove(stackTrace);
                        }
                    }
                }
            }                        
        }
    }

    /**
     * Sometimes, the exception means retry and sometimes not.
     *
     * @param cause     the cause to examine
     * @return          Returns the original cause if it is a valid retry cause, otherwise <tt>null</tt>
     */
    public static Throwable extractRetryCause(Throwable cause)
    {
        Throwable retryCause = ExceptionStackUtil.getCause(cause, RETRY_EXCEPTIONS);
        
        if (retryCause == null)
        {
            return null;
        }
        else if (retryCause instanceof SQLGrammarException
                && ((SQLGrammarException) retryCause).getErrorCode() != 3960)
        {
           return null;
        }
        else if (retryCause instanceof NestedSQLException || retryCause instanceof UncategorizedSQLException)
        {
            // The exception will have been caused by something else, so check that instead
            if (retryCause.getCause() != null && retryCause.getCause() != retryCause)
            {
                // We dig further into this
                cause = retryCause.getCause();
                // Check for SQL-related "deadlock" messages
                if (retryCause.getMessage().toLowerCase().contains("deadlock"))
                {
                    // The word "deadlock" is usually an indication that we need to resolve with a retry.
                    return retryCause;
                }
                else if (retryCause.getMessage().toLowerCase().contains("constraint"))
                {
                    // The word "constraint" is also usually an indication or a concurrent update
                    return retryCause;
                }
                // Recurse
                return extractRetryCause(cause);
            }
            else
            {
                return null;
            }
        }
        // A simple match
        return retryCause;
    }
    
    /**
     * Utility method to get the active transaction.  The transaction status can be queried and
     * marked for rollback.
     * <p>
     * <b>NOTE:</b> Any attempt to actually commit or rollback the transaction will cause failures.
     * 
     * @return          Returns the currently active user transaction or <tt>null</tt> if
     *                  there isn't one.
     */
    public static UserTransaction getActiveUserTransaction()
    {
        // Dodge if there is no wrapping transaction
        if (AlfrescoTransactionSupport.getTransactionReadState() == TxnReadState.TXN_NONE)
        {
            return null;
        }
        // Get the current transaction.  There might not be one if the transaction was not started using
        // this class i.e. it wasn't started with retries.
        UserTransaction txn = (UserTransaction) AlfrescoTransactionSupport.getResource(KEY_ACTIVE_TRANSACTION);
        if (txn == null)
        {
            return null;
        }
        // Done
        return txn;
    }
    
    private static class UserTransactionProtectionAdvise implements MethodBeforeAdvice
    {
        public void before(Method method, Object[] args, Object target) throws Throwable
        {
            String methodName = method.getName();
            if (methodName.equals("begin") || methodName.equals("commit") || methodName.equals("rollback"))
            {
                throw new IllegalAccessException(
                        "The user transaction cannot be manipulated from within the transactional work load");
            }
        }
    }
}
