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
package org.alfresco.repo.transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * Tests integration between our <tt>UserTransaction</tt> implementation and
 * our <tt>TransactionManager</tt>.
 * 
 * @see org.alfresco.repo.transaction.AlfrescoTransactionManager
 * @see org.alfresco.util.transaction.SpringAwareUserTransaction
 * 
 * @author Derek Hulley
 */
public class AlfrescoTransactionSupportTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private ServiceRegistry serviceRegistry;
    TransactionService transactionService;
    
    public void setUp() throws Exception
    {
        serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        transactionService = serviceRegistry.getTransactionService();
    }
    
    public void testTransactionId() throws Exception
    {
        // get a user transaction
        TransactionService transactionService = serviceRegistry.getTransactionService();
        UserTransaction txn = transactionService.getUserTransaction();
        assertNull("Thread shouldn't have a txn ID", AlfrescoTransactionSupport.getTransactionId());
        assertEquals("No transaction start time expected", -1, AlfrescoTransactionSupport.getTransactionStartTime());
        
        // begin the txn
        txn.begin();
        String txnId = AlfrescoTransactionSupport.getTransactionId();
        assertNotNull("Expected thread to have a txn id", txnId);
        long txnStartTime = AlfrescoTransactionSupport.getTransactionStartTime();
        assertTrue("Expected a transaction start time", txnStartTime > 0);
        
        // check that the txn id and time doesn't change
        String txnIdCheck = AlfrescoTransactionSupport.getTransactionId();
        assertEquals("Transaction ID changed on same thread", txnId, txnIdCheck);
        long txnStartTimeCheck = AlfrescoTransactionSupport.getTransactionStartTime();
        assertEquals("Transaction start time changed on same thread", txnStartTime, txnStartTimeCheck);
        
        // begin a new, inner transaction
        {
            UserTransaction txnInner = transactionService.getNonPropagatingUserTransaction();
            
            String txnIdInner = AlfrescoTransactionSupport.getTransactionId();
            assertEquals("Inner transaction not started, so txn ID should not change", txnId, txnIdInner);
            long txnStartTimeInner = AlfrescoTransactionSupport.getTransactionStartTime();
            assertEquals("Inner transaction not started, so txn start time should not change", txnStartTime, txnStartTimeInner);
            
            // begin the nested txn
            txnInner.begin();
            // check the ID for the outer transaction
            txnIdInner = AlfrescoTransactionSupport.getTransactionId();
            assertNotSame("Inner txn ID must be different from outer txn ID", txnIdInner, txnId);
            // Check the time against the outer transaction
            txnStartTimeInner = AlfrescoTransactionSupport.getTransactionStartTime();
            assertTrue(
                    "Inner transaction start time should be greater or equal (accuracy) to the outer's",
                    txnStartTime <= txnStartTimeInner);
            
            // rollback the nested txn
            txnInner.rollback();
            txnIdCheck = AlfrescoTransactionSupport.getTransactionId();
            assertEquals("Txn ID not popped inner txn completion", txnId, txnIdCheck);
        }
        
        // rollback
        txn.rollback();
        assertNull("Thread shouldn't have a txn ID after rollback", AlfrescoTransactionSupport.getTransactionId());
        
        // start a new transaction
        txn = transactionService.getUserTransaction();
        txn.begin();
        txnIdCheck = AlfrescoTransactionSupport.getTransactionId();
        assertNotSame("New transaction has same ID", txnId, txnIdCheck);
        
        // rollback
        txn.rollback();
        assertNull("Thread shouldn't have a txn ID after rollback", AlfrescoTransactionSupport.getTransactionId());
    }
    
    public void testListener() throws Exception
    {
        final List<String> strings = new ArrayList<String>(1);

        // anonymous inner class to test it
        TransactionListener listener = new TransactionListener()
        {
            public void flush()
            {
                strings.add("flush");
            }
            public void beforeCommit(boolean readOnly)
            {
                strings.add("beforeCommit");
            }
            public void beforeCompletion()
            {
                strings.add("beforeCompletion");
            }
            public void afterCommit()
            {
                strings.add("afterCommit");
            }
            public void afterRollback()
            {
                strings.add("afterRollback");
            }
        };
        
        // begin a transaction
        UserTransaction txn = transactionService.getUserTransaction();
        txn.begin();
        
        // register it
        AlfrescoTransactionSupport.bindListener(listener);

        // test commit
        txn.commit();
        assertTrue("beforeCommit not called on listener", strings.contains("beforeCommit"));
        assertTrue("beforeCompletion not called on listener", strings.contains("beforeCompletion"));
        assertTrue("afterCommit not called on listener", strings.contains("afterCommit"));
    }
    
    /**
     * Tests the condition whereby a listener can cause failure by attempting to bind itself to
     * the transaction in the pre-commit callback.  This is caused by the listener set being
     * modified during calls to the listeners.
     */
    public void testPreCommitListenerBinding() throws Exception
    {
        final String beforeCommit = "beforeCommit";
        final String afterCommitInner = "afterCommit - inner";
        final String afterCommitOuter = "afterCommit = outer";
        
        // the listeners will play with this
        final List<String> testList = new ArrayList<String>(1);
        testList.add(beforeCommit);
        testList.add(afterCommitInner);
        testList.add(afterCommitOuter);
        
        final TransactionListener listener = new TransactionListenerAdapter()
        {
            @Override
            public int hashCode()
            {
                // force this listener to be first in the bound set
                return 100;
            }
            @Override
            public void beforeCommit(boolean readOnly)
            {
                testList.remove(beforeCommit);
                TransactionListener postCommitListener = new TransactionListenerAdapter()
                {
                    @Override
                    public void afterCommit()
                    {
                        testList.remove(afterCommitInner);
                    }
                };
                // register bogus on the transaction
                AlfrescoTransactionSupport.bindListener(postCommitListener);
            }
            @Override
            public void afterCommit()
            {
                testList.remove(afterCommitOuter);
            }
        };
        final TransactionListener dummyListener = new TransactionListenerAdapter()
        {
            @Override
            public int hashCode()
            {
                // force the dummy listener to be AFTER the binding listener
                return 200;
            }
        };
        // start a transaction
        RetryingTransactionCallback<Object> bindWork = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
                // just bind the listener to the transaction
                AlfrescoTransactionSupport.bindListener(dummyListener);
                AlfrescoTransactionSupport.bindListener(listener);
                return null;
            }
        };
        // kick it all off
        transactionService.getRetryingTransactionHelper().doInTransaction(bindWork);
        
        // make sure that the binding all worked
        assertTrue("Expected callbacks not all processed: " + testList, testList.size() == 0);
    }
    
    public void testReadWriteStateRetrieval() throws Exception
    {
        final TxnReadState[] postCommitReadState = new TxnReadState[1];
        final TransactionListenerAdapter getReadStatePostCommit = new TransactionListenerAdapter()
        {
            @Override
            public void afterCommit()
            {
                postCommitReadState[0] = AlfrescoTransactionSupport.getTransactionReadState();
            }
        };

        RetryingTransactionCallback<TxnReadState> getReadStateWork = new RetryingTransactionCallback<TxnReadState>()
        {
            public TxnReadState execute() throws Exception
            {
                // Register to list to post-commit
                AlfrescoTransactionSupport.bindListener(getReadStatePostCommit);
                
                return AlfrescoTransactionSupport.getTransactionReadState();
            }
        };

        // Check TXN_NONE
        TxnReadState checkTxnReadState = AlfrescoTransactionSupport.getTransactionReadState();
        assertEquals("Expected 'no transaction'", TxnReadState.TXN_NONE, checkTxnReadState);
        assertNull("Expected no post-commit read state", postCommitReadState[0]);
        // Check TXN_READ_ONLY
        checkTxnReadState = transactionService.getRetryingTransactionHelper().doInTransaction(getReadStateWork, true);
        assertEquals("Expected 'read-only transaction'", TxnReadState.TXN_READ_ONLY, checkTxnReadState);
        assertEquals("Expected 'no transaction'", TxnReadState.TXN_NONE, postCommitReadState[0]);
        // check TXN_READ_WRITE
        checkTxnReadState = transactionService.getRetryingTransactionHelper().doInTransaction(getReadStateWork, false);
        assertEquals("Expected 'read-write transaction'", TxnReadState.TXN_READ_WRITE, checkTxnReadState);
        assertEquals("Expected 'no transaction'", TxnReadState.TXN_NONE, postCommitReadState[0]);
    }
    
    public void testResourceHelper() throws Exception
    {
        // start a transaction
        RetryingTransactionCallback<Object> testWork = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
                // Check map access
                Map<String, String> map = TransactionalResourceHelper.getMap("abc");
                assertNotNull("Map not created", map);
                map.put("1", "ONE");
                Map<String, String> mapCheck = TransactionalResourceHelper.getMap("abc");
                assertTrue("Same map not retrieved", map == mapCheck);
                // Check counter
                assertEquals("Transactional count incorrect. ", 0, TransactionalResourceHelper.getCount("myCount"));
                assertEquals("Transactional count incorrect. ", -1, TransactionalResourceHelper.decrementCount("myCount", true));
                assertEquals("Transactional count incorrect. ", -2, TransactionalResourceHelper.decrementCount("myCount", true));
                assertEquals("Transactional count incorrect. ", -2, TransactionalResourceHelper.getCount("myCount"));
                assertEquals("Transactional count incorrect. ", -1, TransactionalResourceHelper.incrementCount("myCount"));
                assertEquals("Transactional count incorrect. ", 0, TransactionalResourceHelper.incrementCount("myCount"));
                assertEquals("Transactional count incorrect. ", 1, TransactionalResourceHelper.incrementCount("myCount"));
                assertEquals("Transactional count incorrect. ", 1, TransactionalResourceHelper.getCount("myCount"));
                assertEquals("Transactional count incorrect. ", 1, TransactionalResourceHelper.getCount("myCount"));
                TransactionalResourceHelper.resetCount("myCount");
                assertEquals("Transactional count incorrect. ", 0, TransactionalResourceHelper.getCount("myCount"));
                assertEquals("Transactional count incorrect. ", 0, TransactionalResourceHelper.decrementCount("myCount", false));
                assertEquals("Transactional count incorrect. ", 0, TransactionalResourceHelper.decrementCount("myCount", false));
                // Done
                return null;
            }
        };
        // kick it all off
        transactionService.getRetryingTransactionHelper().doInTransaction(testWork);
    }
}
