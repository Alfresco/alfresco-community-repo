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
package org.alfresco.repo.transaction;

import java.util.Random;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.repo.transaction.TransactionUtil.TransactionWork;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * @see org.alfresco.repo.transaction.TransactionAwareSingleton
 * 
 * @author Derek Hulley
 */
public class TransactionAwareSingletonTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    private static Random rand = new Random();
    
    /** the instance to test */
    private TransactionAwareSingleton<Integer> singleton = new TransactionAwareSingleton<Integer>();
    private static final Integer INTEGER_ONE = new Integer(1);
    private static final Integer INTEGER_TWO = new Integer(2);
    
    private TransactionService transactionService;
    
    public void setUp() throws Exception
    {
        transactionService = (TransactionService) ctx.getBean("transactionComponent");
    }
    
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
        TransactionWork<Object> checkWork = new TransactionWork<Object>()
        {
            public Object doWork() throws Exception
            {
                Integer actual = singleton.get();
                assertTrue("Values don't match: " + expected + " != " + actual, actual == expected);
                return null;
            }
        };
        if (inTransaction)
        {
            TransactionUtil.executeInUserTransaction(transactionService, checkWork);
        }
        else
        {
            TransactionUtil.executeInNonPropagatingUserTransaction(transactionService, checkWork);
        }
    }
}
