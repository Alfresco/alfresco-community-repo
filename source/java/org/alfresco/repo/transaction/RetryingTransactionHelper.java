/**
 * 
 */
package org.alfresco.repo.transaction;

import java.util.Random;

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
     * been attempted. NB that this ignores transaction status and relies entirely
     * on thrown exceptions to decide to rollback.  Also this is non-reentrant, not
     * to be called within an existing transaction.
     * @param cb The callback containing the unit of work.
     * @param readOnly Whether this is a read only transaction.
     * @return The result of the unit of work.
     */
    public Object doInTransaction(Callback cb, boolean readOnly)
    {
        RuntimeException lastException = null;
        for (int count = 0; fMaxRetries < 0 || count < fMaxRetries; ++count)
        {
            UserTransaction txn = null;
            try
            {
                txn = fTxnService.getNonPropagatingUserTransaction(readOnly);
                txn.begin();
                Object result = cb.execute();
                txn.commit();
                if (fgLogger.isDebugEnabled())
                {
                    if (count != 0)
                    {
                        fgLogger.debug("Transaction succeeded after " + count + " retries");
                    }
                }
                return result;
            }
            catch (Exception e)
            {
                if (txn != null)
                {
                    try 
                    {
                        txn.rollback();
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
                if (e instanceof ConcurrencyFailureException ||
                    e instanceof DeadlockLoserDataAccessException ||
                    e instanceof StaleObjectStateException ||
                    e instanceof LockAcquisitionException)
                {
                    lastException = (RuntimeException)e;
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
                if (e instanceof RuntimeException)
                {
                    throw (RuntimeException)e;
                }
                throw new AlfrescoRuntimeException("Exception in Transaction.", e);
            }
        }
        throw lastException;
    }
}
