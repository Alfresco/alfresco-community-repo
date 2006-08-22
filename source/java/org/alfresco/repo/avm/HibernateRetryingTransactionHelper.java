package org.alfresco.repo.avm;

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

import org.apache.log4j.Logger;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

/**
 * Helper for DAOs.
 * @author britt
 */
public class HibernateRetryingTransactionHelper extends HibernateTemplate implements RetryingTransactionHelper
{
    private static Logger fgLogger = Logger.getLogger(HibernateRetryingTransactionHelper.class);
    
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
    public HibernateRetryingTransactionHelper()
    {
        fRandom = new Random();
    }

    /**
     * Run an idempotent transaction, repeating invocations as necessary
     * for failures caused by deadlocks and lost races.
     * @param callback The callback containging the work to do.
     * @param write Whether this is a write transaction.
     */
    public void perform(RetryingTransactionCallback callback, boolean write)
    {
        while (true)
        {
            TransactionStatus status = null;
            boolean newTxn = true;
            try
            {
                status = 
                    fTransactionManager.getTransaction(write ? fWriteDefinition : fReadDefinition);
                newTxn = status.isNewTransaction();
                execute(new HibernateCallbackWrapper(callback));
                if (newTxn)
                {
                    fTransactionManager.commit(status);
                }
                return;
            }
            catch (Throwable t)
            {
                if (status == null)
                {
                    t.printStackTrace(System.err);
                    throw new AVMException("Unrecoverable error.", t);
                }
                if (newTxn && !status.isCompleted())
                {
                    fTransactionManager.rollback(status);
                }
                if (!newTxn)
                {
                    if (t instanceof AVMException)
                    {
                        throw (AVMException)t;
                    }
                    throw new AVMException("Unrecoverable error.", t);
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
