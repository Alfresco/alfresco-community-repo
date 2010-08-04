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

import java.util.ConcurrentModificationException;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.error.ExceptionStackUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.hibernate.dialect.AlfrescoSQLServerDialect;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
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
import org.alfresco.util.ApplicationContextHelper;
import org.apache.commons.lang.mutable.MutableInt;
import org.hibernate.SessionFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Tests the transaction retrying behaviour with various failure modes.
 * 
 * @see RetryingTransactionHelper
 * @see TransactionService
 * 
 * @author Derek Hulley
 * @since 2.1
 */
public class RetryingTransactionHelperTest extends TestCase
{
    private static final QName PROP_CHECK_VALUE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "check_value");
    
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private ServiceRegistry serviceRegistry;
    private AuthenticationComponent authenticationComponent;
    private TransactionService transactionService;
    private NodeService nodeService;
    private RetryingTransactionHelper txnHelper;
    
    private Dialect dialect;
    
    private NodeRef rootNodeRef;
    private NodeRef workingNodeRef;
    
    @Override
    public void setUp() throws Exception
    {
        dialect = (Dialect) ctx.getBean("dialect");
        
        serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        authenticationComponent = (AuthenticationComponent) ctx.getBean("authenticationComponent");
        transactionService = serviceRegistry.getTransactionService();
        nodeService = serviceRegistry.getNodeService();
        txnHelper = transactionService.getRetryingTransactionHelper();
        
        // authenticate
        authenticationComponent.setSystemUserAsCurrentUser();
        
        StoreRef storeRef = nodeService.createStore(
                StoreRef.PROTOCOL_WORKSPACE,
                "test-" + getName() + "-" + System.currentTimeMillis());
        rootNodeRef = nodeService.getRootNode(storeRef);
        // Create a node to work on
        workingNodeRef = nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, getName()),
                ContentModel.TYPE_CMOBJECT).getChildRef();
    }
    
    @Override
    public void tearDown() throws Exception
    {
        try { authenticationComponent.clearCurrentSecurityContext(); } catch (Throwable e) {}
    }
    
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
    public void testNestedWithPropogation()
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
    public void testNestedWithoutPropogation()
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
     * 
     * Note: skip test for PostgreSQL and MS SQL Server
     */
    public void testNestedWithoutPropogationConcurrentUntilFailureNotPostgreSQLOrMSSQL() throws InterruptedException
    {
        final RetryingTransactionHelper txnHelperForTest = transactionService.getRetryingTransactionHelper();
        txnHelperForTest.setMaxRetries(1);
        
        if ((dialect instanceof PostgreSQLDialect) || (dialect instanceof AlfrescoSQLServerDialect))
        {
            // NOOP
            // skip test for PostgreSQL and MS SQL Server to avoid hang if concurrently "nested" (in terms of Spring) since the initial transaction does not complete
            // see testConcurrencyRetryingNoFailure instead
            // Note: PostgreSQL does not support lock timeout
            // Note: MS SQL Server does not support lock wait timeout by default (will wait forever) unless it is overridden on each connection ("set lock_timout")
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
    
    public void testLostConnectionRecovery()
    {
        RetryingTransactionCallback<Object> killConnectionCallback = new RetryingTransactionCallback<Object>()
        {
            private boolean killed = false;
            public Object execute() throws Throwable
            {
                // Do some work
                nodeService.deleteNode(workingNodeRef);
                // Successful upon retry
                if (killed)
                {
                    return null;
                }
                // Kill the connection the first time
                HibernateConnectionKiller killer = new HibernateConnectionKiller();
                killer.setSessionFactory((SessionFactory)ctx.getBean("sessionFactory"));
                killer.killConnection();
                killed = true;
                return null;
            }
        };
        // This should work
        txnHelper.doInTransaction(killConnectionCallback);
    }
    
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
    
    /**
     * Helper class to kill the session's DB connection
     */
    private class HibernateConnectionKiller extends HibernateDaoSupport
    {
        @SuppressWarnings("deprecation")
        private void killConnection() throws Exception
        {
            getSession().connection().rollback();
        }
    }
}
