/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.lock;

import static java.time.Duration.of;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.SECONDS;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.alfresco.repo.domain.locks.LockDAO;
import org.alfresco.repo.lock.JobLockService.JobLockRefreshCallback;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.testing.category.DBTests;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;

/**
 * Tests the high-level capabilities provided by the service implementation.  The DAO tests
 * stress the underlying database work, so we only need to deal with deadlock resolution, etc.
 * 
 * @see JobLockService      the service being tested
 * @see LockDAO             the DAO being indirectly tested
 * 
 * @author Derek Hulley
 * @since 3.2
 */
@SuppressWarnings("unused")
@Category({OwnJVMTestsCategory.class, DBTests.class})
public class JobLockServiceTest extends TestCase
{
    public static final String NAMESPACE = "http://www.alfresco.org/test/JobLockServiceTest";
    
    private static final Log logger = LogFactory.getLog(JobLockServiceTest.class);
    
    private ApplicationContext ctx;

    private TransactionService transactionService;
    private RetryingTransactionHelper txnHelper;
    private JobLockService jobLockService;
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
        ctx = ApplicationContextHelper.getApplicationContext();
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        transactionService = serviceRegistry.getTransactionService();
        txnHelper = transactionService.getRetryingTransactionHelper();
        
        jobLockService = (JobLockService) ctx.getBean("jobLockService");
        
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

    public void testSetUp()
    {
        assertNotNull(jobLockService);
    }
    
    public void testSimpleLock()
    {
        String lockToken = jobLockService.getLock(lockAAA, 20L);
        jobLockService.refreshLock(lockToken, lockAAA, 20L);
        jobLockService.releaseLock(lockToken, lockAAA);
        try
        {
            jobLockService.refreshLock(lockToken, lockAAA, 20L);
            fail("Lock refresh should have failed after release");
        }
        catch (LockAcquisitionException e)
        {
            // Expected
        }
        lockToken = jobLockService.getLock(lockAAA, 20L, 5L, 0);            // No retries
        jobLockService.refreshLock(lockToken, lockAAA, 20L);
        jobLockService.releaseLock(lockToken, lockAAA);

        try
        {
            jobLockService.getLock(lockAAA, 20L, 5L, -1);
            fail("getLock should have failed ");
        }
        catch (IllegalArgumentException expected)
        {
           expected.getMessage().contains("Job lock retry count cannot be negative");
        }

    }



    public void testEnforceTxn()
    {
        try
        {
            jobLockService.getTransactionalLock(lockAAA, 50L);
            fail("Service did not enforce the presence of a transaction");
        }
        catch (IllegalStateException e)
        {
            // Expected
        }
    }
    
    /**
     * Checks that the lock can be aquired by a read-only transaction i.e. that locking is
     * independent of the outer transaction.
     */
    public void testLockInReadOnly() throws Exception
    {
        RetryingTransactionCallback<Object> lockCallback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                jobLockService.getTransactionalLock(lockAAA, 500);
                return null;
            }
        };
        txnHelper.doInTransaction(lockCallback, true, true);
    }
    
    /**
     * Checks that locks are released on commit
     */
    public void testLockReleaseOnCommit() throws Exception
    {
        RetryingTransactionCallback<Object> lockCallback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                jobLockService.getTransactionalLock(lockAAA, 5000);
                return null;
            }
        };
        txnHelper.doInTransaction(lockCallback, true, true);
        // The lock should be free now, even though the TTL was high
        RetryingTransactionCallback<Object> lockCheckCallback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                jobLockService.getTransactionalLock(lockAAA, 50);
                return null;
            }
        };
        txnHelper.doInTransaction(lockCheckCallback, true, true);
    }
    
    /**
     * Checks that locks are released on rollback
     */
    public void testLockReleaseOnRollback() throws Exception
    {
        RetryingTransactionCallback<Object> lockCallback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                jobLockService.getTransactionalLock(lockAAA, 5000);
                throw new UnsupportedOperationException("ALERT!");
            }
        };
        try
        {
            txnHelper.doInTransaction(lockCallback, true, true);
            fail("Expected transaction failure");
        }
        catch (UnsupportedOperationException e)
        {
            // Expected
        }
        // The lock should be free now, even though the TTL was high
        RetryingTransactionCallback<Object> lockCheckCallback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                jobLockService.getTransactionalLock(lockAAA, 50);
                return null;
            }
        };
        txnHelper.doInTransaction(lockCheckCallback, true, true);
    }
    
    /**
     * Sets up two threads in a deadlock scenario.  Each of the threads has a long wait timeout
     * for the required locks.  If there were a deadlock, the shorter of the the wait times would
     * be how long it would take before one of them is thrown out.  Firstly, we check that one
     * of the threads <i>is</i> thrown out.  Then we check that the thread is thrown out quickly.
     */
    public synchronized void testDeadlockPrevention() throws Throwable
    {
        DeadlockingThread t1 = new DeadlockingThread(lockAAA, lockAAB);
        DeadlockingThread t2 = new DeadlockingThread(lockAAB, lockAAA);
        // Start them
        t1.start();
        t2.start();
        // They can take their first locks (there should be no contention)
        t1.incrementNextLock();
        t2.incrementNextLock();
        // Wait for them to do this
        try { this.wait(2000L); } catch (InterruptedException e) {}
        // Advance again
        t1.incrementNextLock();
        t2.incrementNextLock();
        // Wait for them to do this
        try { this.wait(2000L); } catch (InterruptedException e) {}
        // Advance again, to end threads
        t1.incrementNextLock();
        t2.incrementNextLock();
        // Wait for them to end (commit/rollback)
        try { this.wait(2000L); } catch (InterruptedException e) {}
        
        if (t1.otherFailure != null)
        {
            throw t1.otherFailure;
        }
        if (t2.otherFailure != null)
        {
            throw t2.otherFailure;
        }
        assertNull("T1 should have succeeded as the ordered locker: " + t1.lockFailure, t1.lockFailure);
        assertNotNull("T2 should have failed as the unordered locker.", t2.lockFailure);
    }
    
    private class DeadlockingThread extends Thread
    {
        private final QName[] lockQNames;
        private volatile int nextLock = -1;
        private LockAcquisitionException lockFailure;
        private Throwable otherFailure;
        
        private DeadlockingThread(QName ... lockQNames)
        {
            super("DeadlockingThread");
            this.lockQNames = lockQNames;
            setDaemon(true);
        }
        
        private void incrementNextLock()
        {
            nextLock++;
        }
        
        @Override
        public void run()
        {
            RetryingTransactionCallback<Object> runCallback = new RetryingTransactionCallback<Object>()
            {
                public synchronized Object execute() throws Throwable
                {
                    int currentLock = -1;
                    // Take the locks in turn, quitting when told to take a lock that's not there
                    while (currentLock < lockQNames.length - 1)
                    {
                        // Check if we have been instructed to take a lock
                        if (nextLock > currentLock)
                        {
                            // Advance and grab the lock
                            currentLock++;
                            jobLockService.getTransactionalLock(lockQNames[currentLock], 5000L);
                        }
                        else
                        {
                            // No advance, so wait a bit more
                            try { this.wait(20L); } catch (InterruptedException e) {}
                        }
                    }
                    return null;
                }
            };
            try
            {
                txnHelper.doInTransaction(runCallback, true);
            }
            catch (LockAcquisitionException e)
            {
                lockFailure = e;
            }
            catch (Throwable e)
            {
                otherFailure = e;
            }
        }
    }
    
    public synchronized void testLockCallbackReleaseInactive() throws Exception
    {
        final QName lockQName = QName.createQName(NAMESPACE, getName());
        final long lockTTL = 1000L;
        final String lockToken = jobLockService.getLock(lockQName, lockTTL);

        final int[] checked = new int[1];
        final int[] released = new int[1];
        // Immediately-inactive job
        JobLockRefreshCallback callback = new JobLockRefreshCallback()
        {
            @Override
            public boolean isActive()
            {
                checked[0]++;
                return false;
            }
            
            @Override
            public void lockReleased()
            {
                released[0]++;
            }
        };

        jobLockService.refreshLock(lockToken, lockQName, lockTTL, callback);
        // The first refresh will occur in 500ms
        wait(1000L);
        // Should have been released by now
        assertTrue("Expected lockReleased to have been called", released[0] > 0);
        try
        {
            jobLockService.getLock(lockQName, lockTTL);
        }
        catch (LockAcquisitionException e)
        {
            fail("Lock should have been released by callback infrastructure");
        }
        
        // Check that the timed callback is killed properly
        int checkedCount = checked[0];
        int releasedCount = released[0];
        wait(2000L);
        assertEquals("Lock callback timer was not terminated", checkedCount, checked[0]);
        assertEquals("Lock callback timer was not terminated", releasedCount, released[0]);
    }
    
    public synchronized void testLockCallbackReleaseSelf() throws Exception
    {
        // ACE-4347 extra debug logging just for this test so we can see what's going on when it next fails
        Level saveLogLevel = Logger.getLogger("org.alfresco.repo.lock").getLevel();
        Logger.getLogger("org.alfresco.repo.lock").setLevel(Level.ALL);
        try
        {
            final QName lockQName = QName.createQName(NAMESPACE, getName());
            final long lockTTL = 1000L;
            final String lockToken = jobLockService.getLock(lockQName, lockTTL);
    
            final int[] checked = new int[1];
            final int[] released = new int[1];
            // Immediately-inactive job, releasing the lock
            JobLockRefreshCallback callback = new JobLockRefreshCallback()
            {
                @Override
                public boolean isActive()
                {
                    checked[0]++;
                    jobLockService.releaseLock(lockToken, lockQName);
                    return false;
                }
                
                @Override
                public void lockReleased()
                {
                    released[0]++;
                }
            };
    
            jobLockService.refreshLock(lockToken, lockQName, lockTTL, callback);
            // The first refresh will occur in 500ms
            wait(1000L);
            // Should NOT get a callback saying that the lock has been released
            assertFalse("Lock should be optimistically released", released[0] > 0);
            try
            {
                jobLockService.getLock(lockQName, lockTTL);
            }
            catch (LockAcquisitionException e)
            {
                fail("Lock should have been released by callback infrastructure");
            }
            
            // Check that the timed callback is killed properly
            int checkedCount = checked[0];
            int releasedCount = released[0];
            if(logger.isDebugEnabled())
            {
                logger.debug("checkedCount=" + checkedCount + ",releasedCount=" + releasedCount);
            }
            wait(10000L);
            assertEquals("Lock callback timer was not terminated", checkedCount, checked[0]);
            assertEquals("Lock callback timer was not terminated", releasedCount, released[0]);
        }
        finally
        {
            Logger.getLogger("org.alfresco.repo.lock").setLevel(saveLogLevel);
        }
    }
    
    /**
     * Lets job "run" for 3 seconds and checks at 2s and 4s.
     */
    public synchronized void testLockCallbackReleaseTimed() throws Exception
    {
        final QName lockQName = QName.createQName(NAMESPACE, getName());
        final long lockTTL = 1000L;
        final long lockNow = System.currentTimeMillis();
        final String lockToken = jobLockService.getLock(lockQName, lockTTL);

        final int[] checked = new int[1];
        final int[] released = new int[1];
        // Do not release and remain active
        JobLockRefreshCallback callback = new JobLockRefreshCallback()
        {
            @Override
            public boolean isActive()
            {
                checked[0]++;
                if (System.currentTimeMillis() - lockNow > 3000L)
                {
                    return false;
                }
                else
                {
                    return true;
                }
            }
            
            @Override
            public void lockReleased()
            {
                released[0]++;
            }
        };

        jobLockService.refreshLock(lockToken, lockQName, lockTTL, callback);
        // The first refresh will occur in 500ms
        wait(2000L);
        
        assertTrue("Expected at least 2 active checks; only got " + checked[0], checked[0] >= 2);
        assertFalse("lockReleased should NOT have been called", released[0] > 0);
        try
        {
            jobLockService.getLock(lockQName, lockTTL);
            fail("Lock should still be held");
        }
        catch (LockAcquisitionException e)
        {
            // Expected
        }
        
        // Wait for another 2s to be sure that the lock is run to completion
        wait(2000L);

        // Check that the timed callback is killed properly
        int checkedCount = checked[0];
        int releasedCount = released[0];
        wait(2000L);
        assertEquals("Lock callback timer was not terminated", checkedCount, checked[0]);
        assertEquals("Lock callback timer was not terminated", releasedCount, released[0]);
    }
    
    public void testGetLockWithCallbackNullLock()       { runGetLockWithCallback(0); }
    public void testGetLockWithCallbackNullCallback()   { runGetLockWithCallback(1); }
    public void testGetLockWithCallbackShortTTL()       { runGetLockWithCallback(2); }
    public void testGetLockWithCallbackLocked()         { runGetLockWithCallback(3); }
    public void testGetLockWithCallbackNormal()         { runGetLockWithCallback(4); }
    
    public void runGetLockWithCallback(int t)
    {
        // ACE-4347 extra debug logging just for this test so we can see what's going on when it next fails
        Level saveLogLevel = Logger.getLogger("org.alfresco.repo.lock").getLevel();
        Logger.getLogger("org.alfresco.repo.lock").setLevel(Level.ALL);

        logger.debug("runGetLockWithCallback "+t+
            "\n----------------------------------------"+
            "\n"+Thread.currentThread().getStackTrace()[2].getMethodName()+
            "\n----------------------------------------");
        
        String token  = null;
        String tokenB = null;
        try 
        { 
            QName        lockName   = t==0 ? null : lockA;
            TestCallback callback   = t==1 ? null : new TestCallback();
            long         timeToLive = t==2 ? 1    : 500;

            if (t==3) 
            {
                // default num retries * default retry wait + time to create lock (pessimistic)
                long ttlLongerThanDefaultRetry = 10*20 + 2000;
                tokenB = jobLockService.getLock(lockA, ttlLongerThanDefaultRetry);
            }                

            token = jobLockService.getLock(lockName, timeToLive, callback);

            if (t<4) fail("expected getLock to fail");
            
            if (callback == null) throw new IllegalStateException();

            waitForAssertion(of(100, MILLIS), () -> {
                assertEquals(false,callback.released);
                assertEquals(0,callback.getIsActiveCount());
            });

            waitForAssertion(of(1, SECONDS), () -> {
                assertEquals(false, callback.released);
                assertEquals(1, callback.getIsActiveCount());
            });

            callback.isActive = false;

            waitForAssertion(of(2, SECONDS), () -> {
                assertEquals(true, callback.released);
                assertEquals(2, callback.getIsActiveCount());
            });
        }
        catch (IllegalArgumentException e)
        {            
            switch (t)
            {
                case 0: logger.debug("null lock      => exception as expected: "+e); break;
                case 1: logger.debug("null callback  => exception as expected: "+e); break;
                case 2: logger.debug("short ttl      => exception as expected: "+e); break;
                default: fail("exception not expected: "+e); break;
            }
        }
        catch (LockAcquisitionException e)
        {            
            switch (t)
            {
                case 3: logger.debug("already locked => exception as expected: "+e); break;
                default: fail("exception not expected: "+e); break;
            }
        }
        catch (Exception e)
        {
            fail("exception not expected: "+e);
        }
        finally
        {
            if (token != null) 
            {
                logger.debug("token should have been released");
                if (jobLockService.releaseLockVerify(token, lockA))
                {
                    fail("token not released");
                }
            }
            
            if (tokenB != null) 
            {
                logger.debug("tokenB should be released");
                jobLockService.releaseLockVerify(tokenB, lockA);
            }
            
            try
            {
                logger.debug("lock should have been released so check can acquire");
                String tokenC = jobLockService.getLock(lockA, 50);
                jobLockService.releaseLock(tokenC, lockA);
            }
            catch (LockAcquisitionException e)
            {
                fail("lock not released");
            }
            
            logger.debug("runGetLockWithCallback\n----------------------------------------");

            Logger.getLogger("org.alfresco.repo.lock").setLevel(saveLogLevel);
        }
    }

    private static void waitForAssertion(Duration timeout, Runnable assertion)
    {
        logger.debug("Waiting for assertion to succeed.");
        final long lastStep = 10;
        final long delayMillis = timeout.toMillis() > lastStep ? timeout.toMillis() / lastStep : 1;

        for (int s = 0; s <= lastStep; s++)
        {
            try
            {
                assertion.run();
                logger.debug("Assertion succeeded.");
                return;
            }
            catch (AssertionFailedError e)
            {
                if (s == lastStep)
                {
                    logger.debug("Assertion failed. No more waiting.");
                    throw e;
                }
                logger.debug("Assertion failed. Waiting until it succeeds.", e);
            }
            try
            {
                Thread.sleep(delayMillis);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
                fail("Thread has been interrupted.");
            }
        }

        throw new IllegalStateException("Unexpected.");
    }
    
    private class TestCallback implements JobLockRefreshCallback
    {
        public volatile AtomicInteger isActiveCounter = new AtomicInteger();
        public volatile boolean released;
        public volatile boolean isActive = true;

        @Override
        public boolean isActive()
        {
            final int currentIsActiveCount = isActiveCounter.incrementAndGet();
            logger.debug("TestCallback.isActive => "+isActive+" ("+currentIsActiveCount+")");
            return isActive;
        }

        @Override
        public void lockReleased()
        {
            logger.debug("TestCallback.lockReleased");
            released = true;
        }

        public int getIsActiveCount()
        {
            return isActiveCounter.get();
        }
    }    
}
