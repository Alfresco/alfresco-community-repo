/**
 * 
 */
package org.alfresco.repo.transaction;

import java.util.Random;

import org.alfresco.error.AlfrescoRuntimeException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.hibernate.StaleObjectStateException;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

/**
 * 
 * @author britt
 */
public class RetryingTransactionAdvice implements MethodInterceptor 
{
    private static Logger fgLogger = Logger.getLogger(RetryingTransactionAdvice.class);
    
    /**
     * The transaction manager instance.
     */
    private PlatformTransactionManager fTxnManager;
    
    /**
     * The TransactionDefinition.
     */
    private TransactionDefinition fDefinition;
    
    /**
     * The maximum number of retries.
     */
    private int fMaxRetries;
    
    /**
     * A Random number generator for getting retry intervals.
     */
    private Random fRandom;
    
    public RetryingTransactionAdvice()
    {
        fRandom = new Random(System.currentTimeMillis());
    }
    
    /**
     * Setter.
     */
    public void setTransactionManager(PlatformTransactionManager manager)
    {
        fTxnManager = manager;
    }

    /**
     * Setter.
     */
    public void setTransactionDefinition(TransactionDefinition def)
    {
        fDefinition = def;
    }
    
    /**
     * Setter.
     */
    public void setMaxRetries(int maxRetries)
    {
        fMaxRetries = maxRetries;
    }
    
    /* (non-Javadoc)
     * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
     */
    public Object invoke(MethodInvocation methodInvocation) throws Throwable 
    {
        RuntimeException lastException = null;
        for (int count = 0; fMaxRetries < -1 || count < fMaxRetries; count++)
        {
            TransactionStatus txn = null;
            boolean isNewTxn = false;
            try
            {
                txn = fTxnManager.getTransaction(fDefinition);
                isNewTxn = txn.isNewTransaction();
                MethodInvocation clone = ((ReflectiveMethodInvocation)methodInvocation).invocableClone();
                Object result = clone.proceed();
                if (isNewTxn)
                {
                    fTxnManager.commit(txn);
                }
                if (fgLogger.isDebugEnabled())
                {
                    if (count != 0)
                    {
                        fgLogger.debug("Transaction succeeded after " + count + " retries.");
                    }
                }                
                return result;
            }        
            catch (Throwable e)
            {
                if (txn != null && isNewTxn && !txn.isCompleted())
                {
                    fTxnManager.rollback(txn);
                }
                if (e instanceof ConcurrencyFailureException ||
                    e instanceof DeadlockLoserDataAccessException ||
                    e instanceof StaleObjectStateException ||
                    e instanceof LockAcquisitionException)
                {
                    if (!isNewTxn)
                    {
                        throw (RuntimeException)e;
                    }
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
                throw new AlfrescoRuntimeException("Failure in Transaction.", e);
            }
        }
        fgLogger.error("Txn Failed after " + fMaxRetries + " retries:", lastException);
        throw lastException;
    }
}
