/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.util.transaction;

import java.util.NoSuchElementException;

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 * @see org.alfresco.util.transaction.SpringAwareUserTransaction
 * 
 * @author Derek Hulley
 */
public class SpringAwareUserTransactionTest extends TestCase
{
    private DummyTransactionManager transactionManager;
    private FailingTransactionManager failingTransactionManager;
    private UserTransaction txn;
    
    public SpringAwareUserTransactionTest()
    {
        super();
    }
    
    @Override
    protected void setUp() throws Exception
    {
        transactionManager = new DummyTransactionManager();
        failingTransactionManager = new FailingTransactionManager();
        txn = getTxn();
    }
    
    private UserTransaction getTxn()
    {
        return new SpringAwareUserTransaction(
                transactionManager,
                false,
                TransactionDefinition.ISOLATION_DEFAULT,
                TransactionDefinition.PROPAGATION_REQUIRED,
                TransactionDefinition.TIMEOUT_DEFAULT);
    }
    
    public void testSetUp() throws Exception
    {
        assertNotNull(transactionManager);
        assertNotNull(txn);
    }
    
    private void checkNoStatusOnThread()
    {
        try
        {
            TransactionAspectSupport.currentTransactionStatus();
            fail("Spring transaction info is present outside of transaction boundaries");
        }
        catch (NoTransactionException e)
        {
            // expected
        }
    }
    
    public void testNoTxnStatus() throws Exception
    {
        checkNoStatusOnThread();
        assertEquals("Transaction status is not correct",
                Status.STATUS_NO_TRANSACTION,
                txn.getStatus());
        assertEquals("Transaction manager not set up correctly",
                txn.getStatus(),
                transactionManager.getStatus());
    }

    public void testSimpleTxnWithCommit() throws Throwable
    {
        testNoTxnStatus();
        try
        {
            txn.begin();
            assertEquals("Transaction status is not correct",
                    Status.STATUS_ACTIVE,
                    txn.getStatus());
            assertEquals("Transaction manager not called correctly",
                    txn.getStatus(),
                    transactionManager.getStatus());

            txn.commit();
            assertEquals("Transaction status is not correct",
                    Status.STATUS_COMMITTED,
                    txn.getStatus());
            assertEquals("Transaction manager not called correctly",
                    txn.getStatus(),
                    transactionManager.getStatus());
        }
        catch (Throwable e)
        {
            // unexpected exception - attempt a cleanup
            try
            {
                txn.rollback();
            }
            catch (Throwable ee)
            {
                e.printStackTrace();
            }
            throw e;
        }
        checkNoStatusOnThread();
    }
    
    public void testSimpleTxnWithRollback() throws Exception
    {
        testNoTxnStatus();
        try
        {
            txn.begin();

            throw new Exception("Blah");
        }
        catch (Throwable e)
        {
            txn.rollback();
        }
        assertEquals("Transaction status is not correct",
                Status.STATUS_ROLLEDBACK,
                txn.getStatus());
        assertEquals("Transaction manager not called correctly",
                txn.getStatus(),
                transactionManager.getStatus());
        checkNoStatusOnThread();
    }
    
    public void testNoBeginCommit() throws Exception
    {
        testNoTxnStatus();
        try
        {
            txn.commit();
            fail("Failed to detected no begin");
        }
        catch (IllegalStateException e)
        {
            // expected
        }
        checkNoStatusOnThread();
    }
    
    public void testPostRollbackCommitDetection() throws Exception
    {
        testNoTxnStatus();

        txn.begin();
        txn.rollback();
        try
        {
            txn.commit();
            fail("Failed to detect rolled back txn");
        }
        catch (RollbackException e)
        {
            // expected
        }
        checkNoStatusOnThread();
    }
    
    public void testPostSetRollbackOnlyCommitDetection() throws Exception
    {
        testNoTxnStatus();

        txn.begin();
        txn.setRollbackOnly();
        try
        {
            txn.commit();
            fail("Failed to detect set rollback");
        }
        catch (RollbackException e)
        {
            // expected
            txn.rollback();
        }
        checkNoStatusOnThread();
    }
    
    public void testMismatchedBeginCommit() throws Exception
    {
        UserTransaction txn1 = getTxn();
        UserTransaction txn2 = getTxn();

        testNoTxnStatus();

        txn1.begin();
        txn2.begin();
        
        txn2.commit();
        txn1.commit();
        
        checkNoStatusOnThread();
        
        txn1 = getTxn();
        txn2 = getTxn();
        
        txn1.begin();
        txn2.begin();
        
        try
        {
            txn1.commit();
            fail("Failure to detect mismatched transaction begin/commit");
        }
        catch (RuntimeException e)
        {
            // expected
        }
        txn2.commit();
        txn1.commit();

        checkNoStatusOnThread();
    }

    /**
     * Test for leaked transactions (no guarantee it will succeed due to reliance
     * on garbage collector), so disabled by default.
     * 
     * Also, if it succeeds, transaction call stack tracing will be enabled
     * potentially hitting the performance of all subsequent tests.
     * 
     * @throws Exception
     */
    public void xtestLeakedTransactionLogging() throws Exception
    {
        assertFalse(SpringAwareUserTransaction.isCallStackTraced());
        
        TrxThread t1 = new TrxThread();
        t1.start();
        System.gc();
        Thread.sleep(1000);

        TrxThread t2 = new TrxThread();
        t2.start();
        System.gc();
        Thread.sleep(1000);
        
        assertTrue(SpringAwareUserTransaction.isCallStackTraced());
        
        TrxThread t3 = new TrxThread();
        t3.start();
        System.gc();
        Thread.sleep(3000);
        System.gc();
        Thread.sleep(3000);
    }
    
    private class TrxThread extends Thread
    {
        public void run()
        {
            try
            {
                getTrx();
            }
            catch (Exception e) {}
        }
        
        public void getTrx() throws Exception
        {
            UserTransaction txn = getTxn();
            txn.begin();
            txn = null;
        }
    }
    
    public void testConnectionPoolException() throws Exception
    {
        testNoTxnStatus();
        txn = getFailingTxn();
        try
        {
            txn.begin();
            fail("ConnectionPoolException should be thrown.");
        }
        catch (ConnectionPoolException cpe)
        {
            // Expected fail
        }
    }
    
    private UserTransaction getFailingTxn()
    {
        return new SpringAwareUserTransaction(
                failingTransactionManager,
                false,
                TransactionDefinition.ISOLATION_DEFAULT,
                TransactionDefinition.PROPAGATION_REQUIRED,
                TransactionDefinition.TIMEOUT_DEFAULT);
    }
    
    /**
     * Used to check that the transaction manager is being called correctly
     * 
     * @author Derek Hulley
     */
    @SuppressWarnings("serial")
    private static class DummyTransactionManager extends AbstractPlatformTransactionManager
    {
        private int status = Status.STATUS_NO_TRANSACTION;
        private Object txn = new Object();
        
        /**
         * @return Returns one of the {@link Status Status.STATUS_XXX} constants
         */
        public int getStatus()
        {
            return status;
        }

        protected void doBegin(Object arg0, TransactionDefinition arg1)
        {
            status = Status.STATUS_ACTIVE;
        }

        protected void doCommit(DefaultTransactionStatus arg0)
        {
            status = Status.STATUS_COMMITTED;
        }

        protected Object doGetTransaction()
        {
            return txn;
        }

        protected void doRollback(DefaultTransactionStatus arg0)
        {
            status = Status.STATUS_ROLLEDBACK;
        }
    }
    
    /**
     * Throws {@link NoSuchElementException} on begin()
     * 
     * @author alex.mukha
     */
    private static class FailingTransactionManager extends AbstractPlatformTransactionManager
    {
        private static final long serialVersionUID = 1L;
        private int status = Status.STATUS_NO_TRANSACTION;
        private Object txn = new Object();
        
        /**
         * @return Returns one of the {@link Status Status.STATUS_XXX} constants
         */
        @SuppressWarnings("unused")
        public int getStatus()
        {
            return status;
        }

        protected void doBegin(Object arg0, TransactionDefinition arg1)
        {
            throw new CannotCreateTransactionException("Test exception.");
        }

        protected void doCommit(DefaultTransactionStatus arg0)
        {
            status = Status.STATUS_COMMITTED;
        }

        protected Object doGetTransaction()
        {
            return txn;
        }

        protected void doRollback(DefaultTransactionStatus arg0)
        {
            status = Status.STATUS_ROLLEDBACK;
        }
    }
}
