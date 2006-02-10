/*
 * Copyright (C) 2005 Alfresco, Inc.
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

import java.util.ArrayList;
import java.util.List;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.repo.transaction.TransactionUtil.TransactionWork;
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
        
        // begine the txn
        txn.begin();
        String txnId = AlfrescoTransactionSupport.getTransactionId();
        assertNotNull("Expected thread to have a txn id", txnId);
        
        // check that the txn id doesn't change
        String txnIdCheck = AlfrescoTransactionSupport.getTransactionId();
        assertEquals("Transaction ID changed on same thread", txnId, txnIdCheck);
        
        // begin a new, inner transaction
        {
            UserTransaction txnInner = transactionService.getNonPropagatingUserTransaction();
            
            String txnIdInner = AlfrescoTransactionSupport.getTransactionId();
            assertEquals("Inner transaction not started, so txn ID should not change", txnId, txnIdInner);
            
            // begin the nested txn
            txnInner.begin();
            // check the ID for the outer transaction
            txnIdInner = AlfrescoTransactionSupport.getTransactionId();
            assertNotSame("Inner txn ID must be different from outer txn ID", txnIdInner, txnId);
            
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

        // test flush
        AlfrescoTransactionSupport.flush();
        assertTrue("flush not called on listener", strings.contains("flush"));
        
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
        TransactionWork<Object> bindWork = new TransactionWork<Object>()
        {
            public Object doWork() throws Exception
            {
                // just bind the listener to the transaction
                AlfrescoTransactionSupport.bindListener(dummyListener);
                AlfrescoTransactionSupport.bindListener(listener);
                return null;
            }
        };
        // kick it all off
        TransactionUtil.executeInNonPropagatingUserTransaction(transactionService, bindWork);
        
        // make sure that the binding all worked
        assertTrue("Expected callbacks not all processed: " + testList, testList.size() == 0);
    }
}
