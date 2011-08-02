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
package org.alfresco.repo.domain.locks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import junit.framework.TestCase;

import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * @see LockDAO
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class LockDAOTest extends TestCase
{
    public static final String NAMESPACE = "http://www.alfresco.org/test/LockDAOTest";
    
    private ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private TransactionService transactionService;
    private RetryingTransactionHelper txnHelper;
    private LockDAO lockDAO;
    // Lock names for the tests
    private QName lockA;
    private QName lockAA;
    private QName lockAAA;
    private QName lockAAB;
    private QName lockAAC;
    private QName lockAB;
    private QName lockABA;
    private QName lockABB;
    private QName lockABC;
    
    @Override
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        transactionService = serviceRegistry.getTransactionService();
        txnHelper = transactionService.getRetryingTransactionHelper();
        txnHelper.setMinRetryWaitMs(10);
        txnHelper.setRetryWaitIncrementMs(10);
        txnHelper.setMaxRetryWaitMs(50);
        
        lockDAO = (LockDAO) ctx.getBean("lockDAO");
        // Get the test name
        String testName = getName();
        // Build lock names for the test
        lockA = QName.createQName(NAMESPACE, "a-" + testName);
        lockAA = QName.createQName(NAMESPACE, "a-" + testName + ".a-" + testName);
        lockAAA = QName.createQName(NAMESPACE, "a-" + testName + ".a-" + testName + ".a-" + testName);
        lockAAB = QName.createQName(NAMESPACE, "a-" + testName + ".a-" + testName + ".b-" + testName);
        lockAAC = QName.createQName(NAMESPACE, "a-" + testName + ".a-" + testName + ".c-" + testName);
        lockAB = QName.createQName(NAMESPACE, "a-" + testName + ".b-" + testName);
        lockABA = QName.createQName(NAMESPACE, "a-" + testName + ".b-" + testName + ".a-" + testName);
        lockABB = QName.createQName(NAMESPACE, "a-" + testName + ".b-" + testName + ".b-" + testName);
        lockABC = QName.createQName(NAMESPACE, "a-" + testName + ".b-" + testName + ".c-" + testName);
    }
    
    private String lock(final QName lockName, final long timeToLive, boolean expectSuccess)
    {
        try
        {
            String token = lock(lockName, timeToLive);
            if (!expectSuccess)
            {
                fail("Expected lock " + lockName + " to have been denied");
            }
            return token;
        }
        catch (LockAcquisitionException e)
        {
            if (expectSuccess)
            {
                // oops
                throw new RuntimeException("Expected to get lock " + lockName + " with TTL of " + timeToLive, e);
            }
            else
            {
                return null;
            }
        }
    }
    /**
     * Do the lock in a new transaction
     * @return              Returns the lock token or <tt>null</tt> if it didn't work
     * @throws  LockAcquisitionException on failure
     */
    private String lock(final QName lockName, final long timeToLive)
    {
        RetryingTransactionCallback<String> callback = new RetryingTransactionCallback<String>()
        {
            public String execute() throws Throwable
            {
                String txnId = AlfrescoTransactionSupport.getTransactionId();
                lockDAO.getLock(lockName, txnId, timeToLive);
                return txnId;
            }
        };
        return txnHelper.doInTransaction(callback);
    }
    
    private void refresh(final QName lockName, final String lockToken, final long timeToLive, boolean expectSuccess)
    {
        RetryingTransactionCallback<Boolean> callback = new RetryingTransactionCallback<Boolean>()
        {
            public Boolean execute() throws Throwable
            {
                lockDAO.refreshLock(lockName, lockToken, timeToLive);
                return Boolean.TRUE;
            }
        };
        try
        {
            txnHelper.doInTransaction(callback);
            if (!expectSuccess)
            {
                fail("Expected to have failed to refresh lock " + lockName);
            }
        }
        catch (LockAcquisitionException e)
        {
            if (expectSuccess)
            {
                throw new RuntimeException("Expected to have refreshed lock " + lockName, e);
            }
        }
    }
    
    private void release(final QName lockName, final String lockToken, boolean expectSuccess)
    {
        RetryingTransactionCallback<Boolean> callback = new RetryingTransactionCallback<Boolean>()
        {
            public Boolean execute() throws Throwable
            {
                lockDAO.releaseLock(lockName, lockToken);
                return Boolean.TRUE;
            }
        };
        try
        {
            txnHelper.doInTransaction(callback);
            if (!expectSuccess)
            {
                fail("Expected to have failed to release lock " + lockName);
            }
        }
        catch (LockAcquisitionException e)
        {
            if (expectSuccess)
            {
                throw new RuntimeException("Expected to have released lock " + lockName, e);
            }
        }
    }
    
    public void testGetLockBasic() throws Exception
    {
        lock(lockAAA, 500L, true);
    }
    
    /**
     * Ensure that the lock tables and queries scale
     */
    public void testLockTableScaling() throws Exception
    {
        int count = 500;
        long before = System.currentTimeMillis();
        for (int i = 1; i <= count; i++)
        {
            QName lockName = QName.createQName(lockAAA.getNamespaceURI(), lockAAA.getLocalName() + "-" + i);
            lock(lockName, 500L, true);
            if (i % 100 == 0)
            {
                long after = System.currentTimeMillis();
                System.out.println("Creation of " + i + " locks took " + (after-before)/1000 + "s");
            }
        }
    }
    
    public void testGetLockFailureBasic() throws Exception
    {
        lock(lockAAA, 500L, true);
        lock(lockAAA, 0L, false);
    }
    
    public void testSharedLocks() throws Exception
    {
        lock(lockAAA, 500L, true);
        lock(lockAAB, 500L, true);
        lock(lockAAC, 500L, true);
        lock(lockABA, 500L, true);
        lock(lockABB, 500L, true);
        lock(lockABC, 500L, true);
    }
    
    public void testExclusiveLockBlockedByShared() throws Exception
    {
        lock(lockAAA, 5000L, true);
        lock(lockAA, 5000L, false);
        lock(lockAB, 5000L, true);
        lock(lockA, 5000L, false);
        lock(lockABA, 5000L, false);
    }
    
    public void testReleaseLockBasic() throws Exception
    {
        String token = lock(lockAAA, 500000L, true);
        release(lockAAA, token, true);
        token = lock(lockAAA, 0L, true);
    }
    
    public void testSharedLockAndRelease() throws Exception
    {
        String tokenAAA = lock(lockAAA, 5000L, true);
        String tokenAAB = lock(lockAAB, 5000L, true);
        String tokenAAC = lock(lockAAC, 5000L, true);
        String tokenABA = lock(lockABA, 5000L, true);
        String tokenABB = lock(lockABB, 5000L, true);
        String tokenABC = lock(lockABC, 5000L, true);
        // Can't lock shared resources
        lock(lockAA, 0L, false);
        lock(lockAB, 0L, false);
        lock(lockA, 0L, false);
        // Release a lock and check again
        release(lockAAA, tokenAAA, true);
        lock(lockAA, 0L, false);
        lock(lockAB, 0L, false);
        lock(lockA, 0L, false);
        // Release a lock and check again
        release(lockAAB, tokenAAB, true);
        lock(lockAA, 0L, false);
        lock(lockAB, 0L, false);
        lock(lockA, 0L, false);
        // Release a lock and check again
        release(lockAAC, tokenAAC, true);
        String tokenAA = lock(lockAA, 5000L, true);                 // This should be open now
        lock(lockAB, 0L, false);
        lock(lockA, 0L, false);
        // Release a lock and check again
        release(lockABA, tokenABA, true);
        lock(lockAB, 0L, false);
        lock(lockA, 0L, false);
        // Release a lock and check again
        release(lockABB, tokenABB, true);
        lock(lockAB, 0L, false);
        lock(lockA, 0L, false);
        // Release a lock and check again
        release(lockABC, tokenABC, true);
        String tokenAB = lock(lockAB, 5000L, true);
        lock(lockA, 0L, false);
        // Release AA and AB
        release(lockAA, tokenAA, true);
        release(lockAB, tokenAB, true);
        String tokenA = lock(lockA, 5000L, true);
        // ... and release
        release(lockA, tokenA, true);
    }

    public synchronized void testLockExpiry() throws Exception
    {
        lock(lockAAA, 50L, true);
        this.wait(100L);
        lock(lockAA, 50L, true);
        this.wait(100L);
        lock(lockA, 100L, true);
    }

    /**
     * Check that locks grabbed away due to expiry cannot be released
     * @throws Exception
     */
    public synchronized void testLockExpiryAndRelease() throws Exception
    {
        String tokenAAA = lock(lockAAA, 500L, true);
        release(lockAAA, tokenAAA, true);
        tokenAAA = lock(lockAAA, 50L, true);        // Make sure we can re-acquire the lock
        this.wait(100L);                            // Wait for expiry
        String grabbedTokenAAAA = lock(lockAAA, 50L, true); // Grabbed lock over the expiry
        release(lockAAA, tokenAAA, false);          // Can't release any more
        this.wait(100L);                            // Wait for expiry
        release(lockAAA, grabbedTokenAAAA, true);   // Proof that expiry, on it's own, doesn't prevent release
    }
    
    public synchronized void testLockRefresh() throws Exception
    {
        String tokenAAA = lock(lockAAA, 1000L, true);
        // Loop, refreshing and testing
        for (int i = 0; i < 40; i++)
        {
            wait(50L);
            // It will have expired, but refresh it anyway
            refresh(lockAAA, tokenAAA, 1000L, true);
            // Check that it is still holding
            lock(lockAAA, 0L, false);
        }
    }
    
    /**
     * Uses a thread lock to ensure that the lock DAO only allows locks through one at a time.
     */
    public synchronized void xtestConcurrentLockAcquisition() throws Exception
    {
        ReentrantLock threadLock = new ReentrantLock();
        GetLockThread[] threads = new GetLockThread[5];
        for (int i = 0; i < threads.length; i++)
        {
            threads[i] = new GetLockThread(threadLock);
            threads[i].start();
        }
        // Wait a bit and see if any encountered errors
        boolean allDone = false;
        waitLoop:
        for (int waitLoop = 0; waitLoop < 500; waitLoop++)
        {
            wait(1000L);
            for (int i = 0; i < threads.length; i++)
            {
                if (!threads[i].isDone())
                {
                    continue waitLoop;
                }
            }
            // All the threads are done
            allDone = true;
            break;
        }
        // Check that all the threads got a turn
        if (!allDone)
        {
            fail("Not all threads managed to acquire the lock");
        }
        // Get errors
        StringBuilder errors = new StringBuilder(512);
        for (int i = 0; i < threads.length; i++)
        {
            if (threads[i].error != null)
            {
                errors.append("\nThread ").append(i).append(" error: ").append(threads[i].error); 
            }
        }
        if (errors.toString().length() > 0)
        {
            fail(errors.toString());
        }
    }

    /**
     * Checks that the lock via the DAO forces a serialization
     */
    private class GetLockThread extends Thread
    {
        private final ReentrantLock threadLock;
        private boolean done;
        private String error;
        private GetLockThread(ReentrantLock threadLock)
        {
            this.threadLock = threadLock;
            this.done = false;
            this.error = null;
            setDaemon(true);
        }
        @Override
        public synchronized void run()
        {
            boolean gotLock = false;
            try
            {
                String tokenAAA = null;
                while (true)
                {
                    try
                    {
                        tokenAAA = lock(lockAAA, 100000L);      // Lock for a long time
                        // Success
                        break;
                    }
                    catch (LockAcquisitionException e)
                    {
                        // OK.  Keep trying.
                    }
                    try { wait(20L); } catch (InterruptedException e) {}
                }
                gotLock = threadLock.tryLock(0, TimeUnit.MILLISECONDS);
                if (!gotLock)
                {
                    error = "Got lock via DAO but not via thread lock";
                    return;
                }
                release(lockAAA, tokenAAA, true);
            }
            catch (Throwable e)
            {
                error = e.getMessage();
            }
            finally
            {
                done = true;
                if (gotLock)
                {
                    threadLock.unlock();
                }
            }
        }
        public synchronized boolean isDone()
        {
            return done;
        }
    }
}
