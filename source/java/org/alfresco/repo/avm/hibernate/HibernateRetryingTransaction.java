package org.alfresco.repo.avm.hibernate;

/*
 * Copyright (C) 2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */

import java.util.Random;

import org.alfresco.repo.avm.AVMException;
import org.alfresco.repo.avm.AVMNotFoundException;
import org.alfresco.repo.avm.RetryingTransactionCallback;
import org.alfresco.repo.avm.RetryingTransaction;
import org.apache.log4j.Logger;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

/**
 * Helper for DAOs.
 * @author britt
 */
public class HibernateRetryingTransaction extends HibernateTemplate implements RetryingTransaction
{
    private static Logger fgLogger = Logger.getLogger(HibernateRetryingTransaction.class);
    
    /**
     * The transaction manager.
     */
    private PlatformTransactionManager fTransactionManager;
    
    /**
     * The read transaction definition.
     */
    private TransactionDefinition fReadDefinition;
    
    /**
     * The write transaction definition.
     */
    private TransactionDefinition fWriteDefinition;
    
    /**
     * The random number generator for inter-retry sleep.
     */
    private Random fRandom;
    
    /**
     * Make one up.
     * @param sessionFactory The SessionFactory.
     */
    public HibernateRetryingTransaction()
    {
        fRandom = new Random();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.hibernate.RetryingTransaction#perform(org.alfresco.repo.avm.hibernate.HibernateTxnCallback, boolean)
     */
    public void perform(RetryingTransactionCallback callback, boolean write)
    {
        while (true)
        {
            TransactionStatus status = null;
            try
            {
                status = 
                    fTransactionManager.getTransaction(write ? fWriteDefinition : fReadDefinition);
                execute(new HibernateCallbackWrapper(callback));
            }
            catch (Throwable t)
            {
                if (!status.isCompleted())
                {
                    fTransactionManager.rollback(status);
                }
                // If we've lost a race or we've deadlocked, retry.
                if (t instanceof DeadlockLoserDataAccessException)
                {
                    fgLogger.info("Deadlock");
                    try
                    {
                        long interval;
                        synchronized (fRandom)
                        {
                            interval = fRandom.nextInt(1000);
                        }
                        Thread.sleep(interval);
                    }
                    catch (InterruptedException ie)
                    {
                        // Do nothing.
                    }
                    continue;
                }
                if (t instanceof OptimisticLockingFailureException)
                {
                    fgLogger.info("Lost Race");
                    continue;
                }
                if (t instanceof AVMException)
                {
                    throw (AVMException)t;
                }
                if (t instanceof DataRetrievalFailureException)
                {
                    System.err.println("Data Retrieval Error.");
                    throw new AVMNotFoundException("Object not found.", t);
                }
                throw new AVMException("Unrecoverable error.", t);
            }
            try
            {
                fTransactionManager.commit(status);
            }
            catch (TransactionException te)
            {
                throw new AVMException("Transaction Exception.", te);
            }
            return;
        }
    }
    
    /**
     * Set the transaction manager we are operating under.
     * @param manager
     */
    public void setTransactionManager(PlatformTransactionManager manager)
    {
        fTransactionManager = manager;
    }
    
    /**
     * Set the read Transaction Definition.
     * @param def
     */
    public void setReadTransactionDefinition(TransactionDefinition def)
    {
        fReadDefinition = def;
    }
    
    /**
     * Set the write Transaction Definition.
     * @param def
     */
    public void setWriteTransactionDefinition(TransactionDefinition def)
    {
        fWriteDefinition = def;
    }
}
