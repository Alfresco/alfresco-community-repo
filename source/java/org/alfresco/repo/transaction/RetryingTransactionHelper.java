/**
 * 
 */
package org.alfresco.repo.transaction;

import java.util.Random;

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
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
 * @author britt
 */
public class RetryingTransactionHelper
{
    private static Logger fgLogger = Logger.getLogger(RetryingTransactionHelper.class);
    
    /**
     * Reference to the TransactionService instance.
     */
    private TransactionService fTxnService;
    
    /**
     * The maximum number of retries. -1 for infinity.
     */
    private int fMaxRetries;
    
    /**
     * Random number generator for retry delays.
     */
    private Random fRandom;
    
    /**
     * Callback interface
     * @author britt
     */
    public interface Callback
    {
        public Object execute();
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
     * Execute a callback in a transaction until it succeeds, fails 
     * because of an error not the result of an optimistic locking failure,
     * or a deadlock loser failure, or until a maximum number of retries have
     * been attempted. 
     * @param cb The callback containing the unit of work.
     * @param readOnly Whether this is a read only transaction.
     * @return The result of the unit of work.
     */
    public Object doInTransaction(Callback cb, boolean readOnly)
    {
        // Track the last exception caught, so that we
        // can throw it if we run out of retries.
        RuntimeException lastException = null;
        for (int count = 0; fMaxRetries < 0 || count < fMaxRetries; ++count)
        {
            UserTransaction txn = null;
            boolean isNew = false;
            try
            {
                txn = fTxnService.getUserTransaction(readOnly);
                // Do we need to handle transaction demarcation.  If 
                // no, we cannot do retries, that will be up to the containing
                // transaction.
                isNew = txn.getStatus() == Status.STATUS_NO_TRANSACTION;
                if (isNew)
                {
                    txn.begin();
                }
                // Do the work.
                Object result = cb.execute();
                // Only commit if we 'own' the transaction.
                if (isNew)
                {
                    txn.commit();
                }
                if (fgLogger.isDebugEnabled())
                {
                    if (count != 0)
                    {
                        fgLogger.debug("Transaction succeeded after " + count + " retries");
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
                        throw new AlfrescoRuntimeException("Unknown Exception.", e);
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
                        throw new AlfrescoRuntimeException("Failure during rollback.", e1);
                    } 
                    catch (SecurityException e1) 
                    {
                        throw new AlfrescoRuntimeException("Failure during rollback.", e1);
                    }
                    catch (SystemException e1) 
                    {
                        throw new AlfrescoRuntimeException("Failure during rollback.", e1);
                    }
                }
                // This handles the case of an unexpected rollback in 
                // the UserTransaction.
                if (e instanceof RollbackException)
                {
                    RollbackException re = (RollbackException)e;
                    e = re.getCause();
                }
                // These are the 'OK' exceptions. These mean we can retry.
                if (e instanceof ConcurrencyFailureException ||
                    e instanceof DeadlockLoserDataAccessException ||
                    e instanceof StaleObjectStateException ||
                    e instanceof LockAcquisitionException)
                {
                    lastException = (RuntimeException)e;
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
                    continue;
                }
                // It was a 'bad' exception.
                if (e instanceof RuntimeException)
                {
                    throw (RuntimeException)e;
                }
                throw new AlfrescoRuntimeException("Exception in Transaction.", e);
            }
        }
        // We've worn out our welcome and retried the maximum number of times.
        // So, fail.
        throw lastException;
    }
}
