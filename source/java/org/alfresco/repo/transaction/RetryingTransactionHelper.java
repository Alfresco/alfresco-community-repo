/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.transaction;

import java.sql.BatchUpdateException;
import java.util.Random;

import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.error.ExceptionStackUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.transaction.TransactionService;
import org.apache.log4j.Logger;
import org.hibernate.StaleObjectStateException;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DeadlockLoserDataAccessException;

/**
 * A helper that runs a unit of work inside a UserTransaction, 
 * transparently retrying the unit of work if the cause of 
 * failure is an optimistic locking or deadlock condition.
 * 
 * @author britt
 */
public class RetryingTransactionHelper
{
    private static final String MSG_READ_ONLY = "permissions.err_read_only";
    private static Logger fgLogger = Logger.getLogger(RetryingTransactionHelper.class);
    
    /**
     * Exceptions that trigger retries.
     */
    public static final Class[] RETRY_EXCEPTIONS;
    static
    {
        RETRY_EXCEPTIONS = new Class[] {
                ConcurrencyFailureException.class,
                DeadlockLoserDataAccessException.class,
                StaleObjectStateException.class,
                LockAcquisitionException.class,
                BatchUpdateException.class
                };
    }
    
    /**
     * Reference to the TransactionService instance.
     */
    private TransactionService fTxnService;
    
    /**
     * The maximum number of retries. -1 for infinity.
     */
    private int fMaxRetries;
    
    /**
     * Whether the the transactions may only be reads
     */
    private boolean readOnly;
    
    /**
     * Random number generator for retry delays.
     */
    private Random fRandom;
    
    /**
     * Callback interface
     * @author britt
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
        fRandom = new Random(System.currentTimeMillis());
    }
    
    // Setters.
    /**
     * Set the TransactionService.
     */
    public void setTransactionService(TransactionService service)
    {
        fTxnService = service;
    }
    
    /**
     * Set the maximimum number of retries. -1 for infinity.
     */
    public void setMaxRetries(int maxRetries)
    {
        fMaxRetries = maxRetries;
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
        // Track the last exception caught, so that we
        // can throw it if we run out of retries.
        RuntimeException lastException = null;
        for (int count = 0; fMaxRetries < 0 || count < fMaxRetries; ++count)
        {
            UserTransaction txn = null;
            boolean isNew = false;
            try
            {
                if (requiresNew)
                {
                    txn = fTxnService.getNonPropagatingUserTransaction();
                }
                else
                {
                    txn = fTxnService.getUserTransaction(readOnly);
                }
                // Only start a transaction if required.  This check isn't necessary as the transactional
                // behaviour ensures that the appropriate propogation is performed.  It is a useful and
                // simple optimization.
                isNew = requiresNew || txn.getStatus() == Status.STATUS_NO_TRANSACTION;
                if (isNew)
                {
                    txn.begin();
                }
                // Do the work.
                R result = cb.execute();
                // Only commit if we 'own' the transaction.
                if (isNew)
                {
                    if (txn.getStatus() == Status.STATUS_MARKED_ROLLBACK)
                    {
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
                if (fgLogger.isDebugEnabled())
                {
                    if (count != 0)
                    {
                        fgLogger.debug(
                                "Transaction succeeded after " + count +
                                " retries on thread " + Thread.currentThread().getName());
                    }
                }
                return result;
            }
            catch (Throwable e)
            {
                // Somebody else 'owns' the transaction, so just rethrow.
                if (!isNew)
                {
                    if (e instanceof RuntimeException)
                    {
                        throw (RuntimeException)e;
                    }
                    else
                    {
                        throw new AlfrescoRuntimeException(
                                "Exception from transactional callback: " + cb,
                                e);
                    }
                }
                // Rollback if we can.
                if (txn != null)
                {
                    try 
                    {
                        if (txn.getStatus() != Status.STATUS_ROLLEDBACK)
                        {
                            txn.rollback();
                        }
                    } 
                    catch (IllegalStateException e1) 
                    {
                        throw new AlfrescoRuntimeException("Failure during rollback: " + cb, e1);
                    } 
                    catch (SecurityException e1) 
                    {
                        throw new AlfrescoRuntimeException("Failure during rollback: " + cb, e1);
                    }
                    catch (SystemException e1) 
                    {
                        throw new AlfrescoRuntimeException("Failure during rollback: " + cb, e1);
                    }
                }
                lastException = (e instanceof RuntimeException) ? 
                        (RuntimeException)e : new AlfrescoRuntimeException("Unknown Exception in Transaction.", e);
                // Check if there is a cause for retrying
                Throwable retryCause = extractRetryCause(e);
                if (retryCause != null)
                {
                    // Sleep a random amount of time before retrying.
                    // The sleep interval increases with the number of retries.
                    try
                    {
                        Thread.sleep(fRandom.nextInt(500 * count + 500));
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
    
    /**
     * Sometimes, the exception means retry and sometimes not.
     * 
     * @param cause     the cause to examine
     * @return          Returns the original cause if it is a valid retry cause, otherwise <tt>null</tt>
     */
    private Throwable extractRetryCause(Throwable cause)
    {
        Throwable retryCause = ExceptionStackUtil.getCause(cause, RETRY_EXCEPTIONS);
        if (retryCause == null)
        {
            return null;
        }
        else if (retryCause instanceof BatchUpdateException)
        {
            if (retryCause.getMessage().contains("Lock wait"))
            {
                // It is valid
                return retryCause;
            }
            else
            {
                // Not valid
                return null;
            }
        }
        else
        {
            return retryCause;
        }
    }
}
