/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
