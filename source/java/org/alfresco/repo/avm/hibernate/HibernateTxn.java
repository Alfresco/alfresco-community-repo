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
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.alfresco.repo.avm.AVMException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StaleStateException;
import org.hibernate.Transaction;
import org.hibernate.exception.GenericJDBCException;
import org.hibernate.exception.LockAcquisitionException;

/**
 * Helper for DAOs.
 * @author britt
 */
public class HibernateTxn
{
    /**
     * The SessionFactory.
     */
    private SessionFactory fSessionFactory;
    
    /**
     * The random number generator for inter-retry sleep.
     */
    private Random fRandom;
    
    /**
     * The BFL.
     */
    private ReentrantReadWriteLock fLock;
    
    /**
     * Make one up.
     * @param sessionFactory The SessionFactory.
     */
    public HibernateTxn(SessionFactory sessionFactory)
    {
        fSessionFactory = sessionFactory;
        fRandom = new Random();
        fLock = new ReentrantReadWriteLock(true);  // Make the lock fair.
    }
    
    /**
     * Perform a set of operations under a single Hibernate transaction.
     * Keep trying if the operation fails because of a concurrency issue.
     * @param callback The worker.
     * @param write Whether this is a write operation.
     * @return Whether the operation finished with a commit.
     */
    public void perform(HibernateTxnCallback callback, boolean write)
    {
        Session sess = null;
        Transaction txn = null;
        while (true)
        {
            try
            {
                /*
                if (write)
                {
                    fLock.writeLock().lock();
                }
                else
                {
                    fLock.readLock().lock();
                }
                */
                sess = fSessionFactory.openSession();
                txn = sess.beginTransaction();
                callback.perform(sess);
                txn.commit();
                return;
            }
            catch (Throwable t)
            {
                // TODO Add appropriate logging.
                if (txn != null)
                {
                    try
                    {
                        txn.rollback();
                    }
                    catch (HibernateException he)
                    {
                        // Do nothing.
                    }
                    // If we've lost a race or we've deadlocked, retry.
                    if (t instanceof StaleStateException ||
                        t instanceof GenericJDBCException ||
                        t instanceof LockAcquisitionException)
                    {
                        if (t instanceof StaleStateException)
                        {
//                            System.err.println("Lost Race");
                        }
                        else
                        {
//                            System.err.println("Deadlock");
                            try
                            {
                                long interval;
                                synchronized (fRandom)
                                {
                                    interval = fRandom.nextInt(1000);
                                }
                                Thread.sleep(interval);
                                continue;
                            }
                            catch (InterruptedException ie)
                            {
                               // Do nothing.
                            }
                        }
                        continue;
                    }
                }
                if (t instanceof AVMException)
                {
                    throw (AVMException)t;
                }
                // TODO Crack t into more useful exception types.
                // Otherwise nothing we can do except throw.
                throw new AVMException("Unrecoverable error", t);
            }
            finally
            {
                /*
                if (write)
                {
                    fLock.writeLock().unlock();
                }
                else
                {
                    fLock.readLock().unlock();
                }
                */
                if (sess != null)
                {
                    try
                    {
                        sess.close();
                    }
                    catch (HibernateException he)
                    {
                        // Do nothing.
                    }
                }
            }
        }
    }
}
