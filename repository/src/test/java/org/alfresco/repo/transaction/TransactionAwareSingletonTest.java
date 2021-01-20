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

import java.util.Random;

import javax.transaction.UserTransaction;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.BaseSpringTest;
import org.junit.Before;
import org.junit.Test;

/**
 * @see org.alfresco.repo.transaction.TransactionAwareSingleton
 *
 * @author Derek Hulley
 */
public class TransactionAwareSingletonTest extends BaseSpringTest
{
    private static Random rand = new Random();

    /** the instance to test */
    private TransactionAwareSingleton<Integer> singleton = new TransactionAwareSingleton<Integer>();
    private static final Integer INTEGER_ONE = new Integer(1);
    private static final Integer INTEGER_TWO = new Integer(2);

    private TransactionService transactionService;

    @Before
    public void setUp() throws Exception
    {
        transactionService = (TransactionService) applicationContext.getBean("transactionComponent");
    }

    @Test
    public void testCommit() throws Throwable
    {
        UserTransaction txn = transactionService.getUserTransaction();
        try
        {
            txn.begin();

            singleton.put(INTEGER_ONE);
            check(INTEGER_ONE, true);
            check(null, false);

            // commit
            txn.commit();
        }
        catch (Throwable e)
        {
            try { txn.rollback(); } catch (Throwable ee) {}
            throw e;
        }
        check(INTEGER_ONE, true);
        check(INTEGER_ONE, false);
    }

    @Test
    public void testRollback() throws Throwable
    {
        UserTransaction txn = transactionService.getUserTransaction();
        try
        {
            txn.begin();

            singleton.put(INTEGER_TWO);
            check(INTEGER_TWO, true);
            check(null, false);

            // rollback
            txn.rollback();
        }
        catch (Throwable e)
        {
            try { txn.rollback(); } catch (Throwable ee) {}
            throw e;
        }
        check(null, true);
        check(null, false);
    }

    private static final int THREAD_COUNT = 20;
    @Test
    public void testThreadsCommit() throws Throwable
    {
        TestThread[] threads = new TestThread[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++)
        {
            TestThread thread = new TestThread(true);
            thread.start();
            threads[i] = thread;
        }
        // wait for them to complete
        for (int i = 0; i < THREAD_COUNT; i++)
        {
            while (threads[i].finished == false)
            {
                synchronized(this)
                {
                    try { wait(20); } catch (Throwable e) {}
                }
            }
            if (threads[i].error != null)
            {
                throw threads[i].error;
            }
        }
    }
    @Test
    public void testThreadsRollback() throws Throwable
    {
        TestThread[] threads = new TestThread[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++)
        {
            TestThread thread = new TestThread(false);
            thread.start();
            threads[i] = thread;
        }
    }

    /**
     * Dumps random values into
     * @author Derek Hulley
     */
    private class TestThread extends Thread
    {
        private boolean finished = false;
        private Throwable error;
        private boolean commit;
        private Integer value = new Integer((int)System.nanoTime());

        public TestThread(boolean commit)
        {
            this.commit = commit;
        }
        @Override
        public synchronized void run()
        {
            UserTransaction txn = transactionService.getUserTransaction();
            try
            {
                txn.begin();

                singleton.put(value);

                // wait for some random time
                try
                {
                    // DH:  The "+1" is necessary to ensure that wait(0) is never called
                    wait((long)(rand.nextDouble() * 1000.0) + 1);   // wait up to a second
                }
                catch (InterruptedException e)
                {
                    // ignore
                }

                // check
                check(value, true);

                if (commit)
                {
                    txn.commit();
                }
                else
                {
                    // rollback
                    txn.rollback();
                }
            }
            catch (Throwable e)
            {
                try { txn.rollback(); } catch (Throwable ee) {}
                this.error = e;
            }
            if (!commit)
            {
                try
                {
                    // no thread changes
                    check(null, false);
                }
                catch (Throwable e)
                {
                    error = e;
                }
            }
            finished = true;
        }
    }

    private void check(final Integer expected, boolean inTransaction)
    {
        RetryingTransactionCallback<Object> checkWork = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
                Integer actual = singleton.get();
                if (expected == null)
                {
                    assertNull("Expected null but got " + actual, actual);
                }
                else
                {
                    assertTrue("Values don't match: " + expected + " != " + actual, actual.equals(expected));
                }
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(checkWork, false, !inTransaction);
    }
}