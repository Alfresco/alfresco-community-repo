/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.repo.transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.transaction.Status;
import javax.transaction.UserTransaction;
import org.alfresco.error.ExceptionStackUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.dialect.Dialect;
import org.alfresco.repo.domain.dialect.MySQLInnoDBDialect;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.alfresco.util.transaction.TransactionListenerAdapter;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.ConcurrencyFailureException;

/**
 * Tests the transaction retrying behaviour with various failure modes.
 *
 * @see RetryingTransactionHelper
 * @see TransactionService
 *
 * @author Derek Hulley
 * @since 2.1
 */
public class RetryingTransactionHelperTest extends BaseSpringTest
{
    private static Log logger = LogFactory.getLog("org.alfresco.repo.transaction.RetryingTransactionHelperTest");

    private static final QName PROP_CHECK_VALUE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "check_value");

    private ServiceRegistry serviceRegistry;
    private AuthenticationComponent authenticationComponent;
    private TransactionService transactionService;
    private NodeService nodeService;
    private RetryingTransactionHelper txnHelper;

    private Dialect dialect;

    private NodeRef rootNodeRef;
    private NodeRef workingNodeRef;

    @Before
    public void setUp() throws Exception
    {
        dialect = (Dialect) applicationContext.getBean("dialect");

        serviceRegistry = (ServiceRegistry) applicationContext.getBean(ServiceRegistry.SERVICE_REGISTRY);
        authenticationComponent = (AuthenticationComponent) applicationContext.getBean("authenticationComponent");
        transactionService = serviceRegistry.getTransactionService();
        nodeService = serviceRegistry.getNodeService();
        txnHelper = transactionService.getRetryingTransactionHelper();

        // authenticate
        authenticationComponent.setSystemUserAsCurrentUser();

        StoreRef storeRef = nodeService.createStore(
                StoreRef.PROTOCOL_WORKSPACE,
                "test-" + System.currentTimeMillis());
        rootNodeRef = nodeService.getRootNode(storeRef);
        // Create a node to work on
        workingNodeRef = nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, GUID.generate()),
                ContentModel.TYPE_CMOBJECT).getChildRef();
    }

    @After
    public void tearDown() throws Exception
    {
        try { authenticationComponent.clearCurrentSecurityContext(); } catch (Throwable e) {}
    }

    @Test
    public void testSetUp() throws Exception
    {
        assertNotNull(rootNodeRef);
        assertNotNull(workingNodeRef);
    }

    /**
     * Get the count, which is 0 to start each test
     *
     * @return          Returns the current count
     */
    private Long getCheckValue()
    {
        Long checkValue = (Long) nodeService.getProperty(workingNodeRef, PROP_CHECK_VALUE);
        if (checkValue == null)
        {
            checkValue = new Long(0);
            nodeService.setProperty(workingNodeRef, PROP_CHECK_VALUE, checkValue);
        }
        return checkValue;
    }

    /**
     * Increment the test count, which is 0 to start each test
     *
     * @return          Returns the current count
     */
    private Long incrementCheckValue()
    {
        Long checkValue = getCheckValue();
        checkValue = new Long(checkValue.longValue() + 1L);
        nodeService.setProperty(workingNodeRef, PROP_CHECK_VALUE, checkValue);
        return checkValue;
    }

    /**
     * @return                          Never returns anything
     * @throws InvalidNodeRefException  <b>ALWAYS</b>
     */
    private Long blowUp()
    {
        NodeRef invalidNodeRef = new NodeRef(workingNodeRef.getStoreRef(), "BOGUS");
        nodeService.setProperty(invalidNodeRef, PROP_CHECK_VALUE, null);
        fail("Expected to generate an InvalidNodeRefException");
        return null;
    }

    /**
     * Check that it works without complications.
     */
    @Test
    public void testSuccessNoRetry()
    {
        long beforeValue = getCheckValue();
        RetryingTransactionCallback<Long> callback = new RetryingTransactionCallback<Long>()
        {
            public Long execute() throws Throwable
            {
                return incrementCheckValue();
            }
        };
        long txnValue = txnHelper.doInTransaction(callback);
        long afterValue = getCheckValue();
        assertEquals("The value must have increased", beforeValue + 1, afterValue);
        assertEquals("The txn value must be the same as the value after", afterValue, txnValue);
    }

    /**
     * Check that the transaction state can be fetched in and around the transaction.
     * This also checks that any mischievous attempts to manipulate the transaction
     * (other than setRollback) are detected.
     */
    @Test
    public void testUserTransactionStatus()
    {
        UserTransaction txnBefore = RetryingTransactionHelper.getActiveUserTransaction();
        assertNull("Did not expect to get an active UserTransaction", txnBefore);

        RetryingTransactionCallback<Long> callbackOuter = new RetryingTransactionCallback<Long>()
        {
            public Long execute() throws Throwable
            {
                final UserTransaction txnOuter = RetryingTransactionHelper.getActiveUserTransaction();
                assertNotNull("Expected an active UserTransaction", txnOuter);
                assertEquals(
                        "Should be read-write txn",
                        TxnReadState.TXN_READ_WRITE, AlfrescoTransactionSupport.getTransactionReadState());
                assertEquals("Expected state is active", Status.STATUS_ACTIVE, txnOuter.getStatus());
                RetryingTransactionCallback<Long> callbackInner = new RetryingTransactionCallback<Long>()
                {
                    public Long execute() throws Throwable
                    {
                        UserTransaction txnInner = RetryingTransactionHelper.getActiveUserTransaction();
                        assertNotNull("Expected an active UserTransaction", txnInner);
                        assertEquals(
                                "Should be read-only txn",
                                TxnReadState.TXN_READ_ONLY, AlfrescoTransactionSupport.getTransactionReadState());
                        assertEquals("Expected state is active", Status.STATUS_ACTIVE, txnInner.getStatus());
                        // Check blow up
                        try
                        {
                            txnInner.commit();
                            fail("Should not be able to commit the UserTransaction.  It is for info only.");
                        }
                        catch (Throwable e)
                        {
                            // Expected
                        }
                        // Force a rollback
                        txnInner.setRollbackOnly();
                        // Done
                        return null;
                    }
                };
                return txnHelper.doInTransaction(callbackInner, true, true);
            }
        };
        txnHelper.doInTransaction(callbackOuter);

        UserTransaction txnAfter = RetryingTransactionHelper.getActiveUserTransaction();
        assertNull("Did not expect to get an active UserTransaction", txnAfter);
    }

    /**
     * Check that the retries happening for simple concurrency exceptions
     */
    @Test
    public void testSuccessWithRetry()
    {
        RetryingTransactionCallback<Long> callback = new RetryingTransactionCallback<Long>()
        {
            private int maxCalls = 3;
            private int callCount = 0;
            public Long execute() throws Throwable
            {
                callCount++;
                Long checkValue = incrementCheckValue();
                if (callCount == maxCalls)
                {
                    return checkValue;
                }
                else
                {
                    throw new ConcurrencyFailureException("Testing");
                }
            }
        };
        long txnValue = txnHelper.doInTransaction(callback);
        assertEquals("Only one increment expected", 1, txnValue);
    }

    /**
     * Checks that a non-retrying exception is passed out and that the transaction is rolled back.
     */
    @Test
    public void testNonRetryingFailure()
    {
        RetryingTransactionCallback<Long> callback = new RetryingTransactionCallback<Long>()
        {
            public Long execute() throws Throwable
            {
                incrementCheckValue();
                return blowUp();
            }
        };
        try
        {
            txnHelper.doInTransaction(callback);
            fail("Wrapper didn't generate an exception");
        }
        catch (InvalidNodeRefException e)
        {
            // Correct
        }
        catch (Throwable e)
        {
            fail("Incorrect exception from wrapper: " + e);
        }
        // Check that the value didn't change
        long checkValue = getCheckValue();
        assertEquals("Check value should not have changed", 0, checkValue);
    }

    /**
     * Sometimes, exceptions or other states may cause the transaction to be marked for
     * rollback without an exception being generated.  This tests that the exception stays
     * absorbed and that another isn't generated, but that the transaction was rolled back
     * properly.
     */
    @Test
    public void testNonRetryingSilentRollback()
    {
        RetryingTransactionCallback<Long> callback = new RetryingTransactionCallback<Long>()
        {
            public Long execute() throws Throwable
            {
                incrementCheckValue();
                try
                {
                    return blowUp();
                }
                catch (InvalidNodeRefException e)
                {
                    // Expected, but absorbed
                }
                return null;
            }
        };
        txnHelper.doInTransaction(callback);
        long checkValue = getCheckValue();
        assertEquals("Check value should not have changed", 0, checkValue);
    }

    /**
     * Checks nesting of two transactions with <code>requiresNew == false</code>
     */
    @Test
    public void testNestedWithPropagation()
    {
        RetryingTransactionCallback<Long> callback = new RetryingTransactionCallback<Long>()
        {
            public Long execute() throws Throwable
            {
                RetryingTransactionCallback<Long> callbackInner = new RetryingTransactionCallback<Long>()
                {
                    public Long execute() throws Throwable
                    {
                        incrementCheckValue();
                        incrementCheckValue();
                        return getCheckValue();
                    }
                };
                txnHelper.doInTransaction(callbackInner, false, false);
                incrementCheckValue();
                incrementCheckValue();
                return getCheckValue();
            }
        };
        long checkValue = txnHelper.doInTransaction(callback);
        assertEquals("Nesting requiresNew==false didn't work", 4, checkValue);
    }

    /**
     * Checks nesting of two transactions with <code>requiresNew == true</code>
     */
    @Test
    public void testNestedWithoutPropagation()
    {
        RetryingTransactionCallback<Long> callback = new RetryingTransactionCallback<Long>()
        {
            public Long execute() throws Throwable
            {
                RetryingTransactionCallback<Long> callbackInner = new RetryingTransactionCallback<Long>()
                {
                    public Long execute() throws Throwable
                    {
                        incrementCheckValue();
                        incrementCheckValue();
                        return getCheckValue();
                    }
                };
                txnHelper.doInTransaction(callbackInner, false, true);
                incrementCheckValue();
                incrementCheckValue();
                return getCheckValue();
            }
        };
        long checkValue = txnHelper.doInTransaction(callback);
        assertEquals("Nesting requiresNew==true didn't work", 4, checkValue);
    }

    /**
     * Checks nesting of two transactions with <code>requiresNew == true</code>,
     * but where the two transactions get involved in a concurrency struggle.
     * <p/>
     * Note: skip test for non-MySQL
     */
    @Test
    public void testNestedWithoutPropagationConcurrentUntilFailureMySQL() throws InterruptedException
    {
        final RetryingTransactionHelper txnHelperForTest = transactionService.getRetryingTransactionHelper();
        txnHelperForTest.setMaxRetries(1);

        if (! (dialect instanceof MySQLInnoDBDialect))
        {
            // NOOP - skip test for non-MySQL DB dialects to avoid hang if concurrently "nested" (in terms of Spring)
            // since the initial transaction does not complete
            // see testConcurrencyRetryingNoFailure instead
            logger.warn("NOTE: Skipping testNestedWithoutPropogationConcurrentUntilFailureMySQLOnly for dialect: "+dialect);
        }
        else
        {
            RetryingTransactionCallback<Long> callback = new RetryingTransactionCallback<Long>()
            {
                public Long execute() throws Throwable
                {
                    RetryingTransactionCallback<Long> callbackInner = new RetryingTransactionCallback<Long>()
                    {
                        public Long execute() throws Throwable
                        {
                            incrementCheckValue();
                            return getCheckValue();
                        }
                    };
                    incrementCheckValue();
                    txnHelperForTest.doInTransaction(callbackInner, false, true);
                    return getCheckValue();
                }
            };
            try
            {
                txnHelperForTest.doInTransaction(callback);
                fail("Concurrent nested access not leading to failure");
            }
            catch (Throwable e)
            {
                Throwable validCause = ExceptionStackUtil.getCause(e, RetryingTransactionHelper.RETRY_EXCEPTIONS);
                assertNotNull("Unexpected cause of the failure", validCause);
            }
        }
    }

    @Test
    public void testConcurrencyRetryingNoFailure() throws InterruptedException
    {
        Thread t1 = new Thread(new ConcurrentTransaction(5000));
        t1.start();

        Thread.sleep(1000);

        Thread t2 = new Thread(new ConcurrentTransaction(10));
        t2.start();

        t1.join();
        t2.join();
    }

    private class ConcurrentTransaction implements Runnable
    {
        private long wait;

        public ConcurrentTransaction(long wait)
        {
            this.wait = wait;
        }

        public void run()
        {
            AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

            final RetryingTransactionHelper txnHelperForTest = transactionService.getRetryingTransactionHelper();

            RetryingTransactionCallback<Long> callback = new RetryingTransactionCallback<Long>()
            {
                public Long execute() throws Throwable
                {
                    incrementCheckValue();

                    System.out.println("Wait started: "+Thread.currentThread()+" ("+wait+")");
                    Thread.sleep(wait);
                    System.out.println("Wait finished: "+Thread.currentThread()+" ("+wait+")");

                    return getCheckValue();
                }
            };
            try
            {
                System.out.println("Txn start: "+Thread.currentThread()+" ("+wait+")");
                txnHelperForTest.doInTransaction(callback);
                System.out.println("Txn finish: "+Thread.currentThread()+" ("+wait+")");
            }
            catch (Throwable e)
            {
                Throwable validCause = ExceptionStackUtil.getCause(e, RetryingTransactionHelper.RETRY_EXCEPTIONS);
                assertNotNull("Unexpected cause of the failure", validCause);
            }
        }
    }

    @Test
    public void testZeroAndNegativeRetries()
    {
        final MutableInt callCount = new MutableInt(0);
        RetryingTransactionCallback<Long> callback = new RetryingTransactionCallback<Long>()
        {
            public Long execute() throws Throwable
            {
                callCount.setValue(callCount.intValue() + 1);
                throw new ConcurrentModificationException();
            }
        };
        // No retries
        callCount.setValue(0);
        txnHelper.setMaxRetries(0);
        try
        {
            txnHelper.doInTransaction(callback);
        }
        catch (ConcurrentModificationException e)
        {
            // Expected
        }
        assertEquals("Should have been called exactly once", 1, callCount.intValue());

        // Negative retries
        callCount.setValue(0);
        txnHelper.setMaxRetries(-1);
        try
        {
            txnHelper.doInTransaction(callback);
        }
        catch (ConcurrentModificationException e)
        {
            // Expected
        }
        assertEquals("Should have been called exactly once", 1, callCount.intValue());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testTimeLimit()
    {
        final RetryingTransactionHelper txnHelper = new RetryingTransactionHelper();
        txnHelper.setTransactionService(transactionService);
        txnHelper.setMaxExecutionMs(3000);
        final List<Throwable> caughtExceptions = Collections.synchronizedList(new LinkedList<Throwable>());

        // Try submitting a request after a timeout
        runThreads(txnHelper, caughtExceptions, new Pair(0, 1000), new Pair(0, 5000), new Pair(4000, 1000));
        assertEquals("Expected 1 exception", 1, caughtExceptions.size());
        assertTrue("Excpected TooBusyException", caughtExceptions.get(0) instanceof TooBusyException);

        // Stay within timeout limits
        caughtExceptions.clear();
        runThreads(txnHelper, caughtExceptions, new Pair(0, 1000), new Pair(0, 2000), new Pair(0, 1000), new Pair(1000, 1000), new Pair(1000, 2000), new Pair(2000, 1000));
        if (caughtExceptions.size() > 0)
        {
            throw new RuntimeException("Unexpected exception", caughtExceptions.get(0));
        }
    }

    @Test
    public void testALF_17631()
    {
        final MutableInt callCount = new MutableInt(0);
        RetryingTransactionCallback<Long> callback = new RetryingTransactionCallback<Long>()
        {
            public Long execute() throws Throwable
            {
                callCount.setValue(callCount.intValue() + 1);
                throw new InvalidNodeRefException(new NodeRef("test", "test", "test"));
            }
        };

        txnHelper.setMaxRetries(3);
        try
        {
            txnHelper.doInTransaction(callback);
        }
        catch (InvalidNodeRefException e)
        {
            // Expected
        }
        assertEquals("Should have been called exactly once", 1, callCount.intValue());

        callCount.setValue(0);

        List<Class<?>> extraExceptions = new ArrayList<Class<?>>(1);
        extraExceptions.add(InvalidNodeRefException.class);
        txnHelper.setExtraExceptions(extraExceptions);

        try
        {
            txnHelper.doInTransaction(callback);
        }
        catch (InvalidNodeRefException e)
        {
            // Expected
        }
        assertEquals("Should have been called tree times", 3, callCount.intValue());

    }

    @Test
    public void testForceWritable() throws Exception
    {
        authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());

        final RetryingTransactionCallback<Void> doNothingCallback = new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                return null;
            }
        };

        TransactionServiceImpl txnService = (TransactionServiceImpl) transactionService;
        txnService.setAllowWrite(false, QName.createQName("{test}testForceWritable"));
        try
        {
            final RetryingTransactionHelper vetoedTxnHelper = txnService.getRetryingTransactionHelper();
            // We can execute read-only
            vetoedTxnHelper.doInTransaction(doNothingCallback, true, false);
            // We fail on write
            try
            {
                vetoedTxnHelper.doInTransaction(doNothingCallback, false, false);
                fail("Failed to prevent read-write txn in vetoed txn helper.");
            }
            catch (RuntimeException e)
            {
                // Expected
            }

            // Now call the vetoed callback in one that has been forced writable
            // A failure would be one of the causes of MNT-11310.
            RetryingTransactionHelper forcedTxnHelper = txnService.getRetryingTransactionHelper();
            forcedTxnHelper.setForceWritable(true);
            forcedTxnHelper.doInTransaction(new RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    // Participate in the outer transaction
                    vetoedTxnHelper.doInTransaction(doNothingCallback, false, false);
                    return null;
                }
            }, false);

            // However, if we attempt to force a new transaction, then the forcing should have no effect
            try
            {
                forcedTxnHelper.doInTransaction(new RetryingTransactionCallback<Void>()
                {
                    @Override
                    public Void execute() throws Throwable
                    {
                        // Start a new transaction
                        vetoedTxnHelper.doInTransaction(doNothingCallback, false, true);
                        return null;
                    }
                }, false);
                fail("Inner, non-propagating transactions should still fall foul of the write veto.");
            }
            catch (AccessDeniedException e)
            {
                // Expected
            }
        }
        finally
        {
            txnService.setAllowWrite(true, QName.createQName("{test}testForceWritable"));
        }
    }

    @Test
    public void testStartNewTransaction() throws Exception
    {
        // MNT-10096
        class CustomListenerAdapter extends TransactionListenerAdapter
        {
            private String newTxnId;

            @Override
            public void afterRollback()
            {
                newTxnId = txnHelper.doInTransaction(new RetryingTransactionCallback<String>()
                {
                    @Override
                    public String execute() throws Throwable
                    {
                        return AlfrescoTransactionSupport.getTransactionId();
                    }
                }, true, false);
            }
        }

        UserTransaction txn = transactionService.getUserTransaction();
        txn.begin();
        String txnId = AlfrescoTransactionSupport.getTransactionId();
        CustomListenerAdapter listener = new CustomListenerAdapter();

        AlfrescoTransactionSupport.bindListener(listener);
        txn.rollback();

        assertFalse("New transaction has not started", txnId.equals(listener.newTxnId));
    }

    @SuppressWarnings("unchecked")
    private void runThreads(
            final RetryingTransactionHelper txnHelper,
            final List<Throwable> caughtExceptions,
            final Pair<Integer, Integer>... startDurationPairs)
    {
        ExecutorService executorService = new ThreadPoolExecutor(10, 10, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(10));

        class Work implements Runnable
        {
            private final CountDownLatch startLatch;
            private final long endTime;

            public Work(CountDownLatch startLatch, long endTime)
            {
                this.startLatch = startLatch;
                this.endTime = endTime;
            }

            public void run()
            {
                try
                {
                    txnHelper.doInTransaction(new RetryingTransactionCallback<Void>()
                    {

                        public Void execute() throws Throwable
                        {
                            // Signal that we've started
                            startLatch.countDown();

                            long duration = endTime - System.currentTimeMillis();
                            if (duration > 0)
                            {
                                Thread.sleep(duration);
                            }
                            return null;
                        }
                    });
                }
                catch (Throwable e)
                {
                    caughtExceptions.add(e);
                    // We never got a chance to signal we had started so do it now
                    if (startLatch.getCount() > 0)
                    {
                        startLatch.countDown();
                    }
                }
            }
        }
        ;

        // Schedule the transactions at their required start times
        long startTime = System.currentTimeMillis();
        long currentStart = 0;
        for (Pair<Integer, Integer> pair : startDurationPairs)
        {
            int start = pair.getFirst();
            long now = System.currentTimeMillis();
            long then = startTime + start;
            if (then > now)
            {
                try
                {
                    Thread.sleep(then - now);
                }
                catch (InterruptedException e)
                {
                }
                currentStart = start;
            }
            CountDownLatch startLatch = new CountDownLatch(1);
            Runnable work = new Work(startLatch, startTime + currentStart + pair.getSecond());
            executorService.execute(work);
            try
            {
                // Wait for the thread to get up and running. We need them starting in sequence
                startLatch.await(60, TimeUnit.SECONDS);
            }
            catch (InterruptedException e)
            {
            }
        }
        // Wait for the threads to have finished
        executorService.shutdown();
        try
        {
            executorService.awaitTermination(60, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
        }

    }
}