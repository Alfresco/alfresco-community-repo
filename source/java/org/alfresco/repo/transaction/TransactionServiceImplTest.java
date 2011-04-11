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

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.ReadOnlyServerException;
import org.alfresco.util.ApplicationContextHelper;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * @see org.alfresco.repo.transaction.TransactionServiceImpl
 * 
 * @author Derek Hulley
 */
public class TransactionServiceImplTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private PlatformTransactionManager transactionManager;
    private TransactionServiceImpl transactionService;
    private NodeService nodeService;
    
    private final QName vetoName = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "TransactionServiceImplTest");
    
    private Dialect dialect;
    
    public void setUp() throws Exception
    {
        transactionManager = (PlatformTransactionManager) ctx.getBean("transactionManager");
        transactionService = new TransactionServiceImpl();
        transactionService.setTransactionManager(transactionManager);   
        transactionService.setAllowWrite(true, vetoName);
        transactionService.setAuthenticationContext((AuthenticationContext) ctx.getBean("authenticationContext"));
        transactionService.setSysAdminParams((SysAdminParams) ctx.getBean("sysAdminParams"));
        
        nodeService = (NodeService) ctx.getBean("dbNodeService");
        
        dialect = (Dialect) ctx.getBean("dialect");
    }
    
    public void testPropagatingTxn() throws Exception
    {
        // start a transaction
        UserTransaction txnOuter = transactionService.getUserTransaction();
        txnOuter.begin();
        String txnIdOuter = AlfrescoTransactionSupport.getTransactionId();
        
        // start a propagating txn
        UserTransaction txnInner = transactionService.getUserTransaction();
        txnInner.begin();
        String txnIdInner = AlfrescoTransactionSupport.getTransactionId();
        
        // the txn IDs should be the same
        assertEquals("Txn ID not propagated", txnIdOuter, txnIdInner);
        
        // rollback the inner
        txnInner.rollback();
        
        // check both transactions' status
        assertEquals("Inner txn not marked rolled back", Status.STATUS_ROLLEDBACK, txnInner.getStatus());
        assertEquals("Outer txn not marked for rolled back", Status.STATUS_MARKED_ROLLBACK, txnOuter.getStatus());
        
        try
        {
            txnOuter.commit();
            fail("Outer txn not marked for rollback");
        }
        catch (RollbackException e)
        {
            // expected
            txnOuter.rollback();
        }
    }
    
    public void testNonPropagatingTxn() throws Exception
    {
        // start a transaction
        UserTransaction txnOuter = transactionService.getUserTransaction();
        txnOuter.begin();
        String txnIdOuter = AlfrescoTransactionSupport.getTransactionId();
        
        // start a propagating txn
        UserTransaction txnInner = transactionService.getNonPropagatingUserTransaction();
        txnInner.begin();
        String txnIdInner = AlfrescoTransactionSupport.getTransactionId();
        
        // the txn IDs should be different
        assertNotSame("Txn ID not propagated", txnIdOuter, txnIdInner);
        
        // rollback the inner
        txnInner.rollback();

        // outer should commit without problems
        txnOuter.commit();
    }
    
    public void testReadOnlyTxn() throws Exception
    {
        // start a read-only transaction
        transactionService.setAllowWrite(false, vetoName);
        
        UserTransaction txn = transactionService.getUserTransaction();
        txn.begin();
        
        // do some writing
        try
        {
            nodeService.createStore(
                    StoreRef.PROTOCOL_WORKSPACE,
                    getName() + "_" + System.currentTimeMillis());
            txn.commit();
            fail("Read-only transaction wasn't detected");
        }
        catch (ReadOnlyServerException e)
        {
            // This is now thrown at the lower layers, but it *is* possible for one of the later
            // exceptions to get through: Fixed ALF-3884: Share does not report access denied exceptions correctly
            @SuppressWarnings("unused")
            int i = 0;
        }
        catch (InvalidDataAccessApiUsageException e)
        {
            // expected this ...
            @SuppressWarnings("unused")
            int i = 0;
        }
        catch (TransientDataAccessResourceException e)
        {
            // or this - for MySQL (java.sql.SQLException: Connection is read-only. Queries leading to data modification are not allowed.)
            @SuppressWarnings("unused")
            int i = 0;
        }
        catch (IllegalStateException e)
        {
            // or this - for MS SQLServer, DB2, Oracle (via AbstractNodeDAOImpl.getCurrentTransaction)
            @SuppressWarnings("unused")
            int i = 0;
        }
        catch (UncategorizedSQLException e)
        {
            // or this - for PostgreSQL (org.postgresql.util.PSQLException: ERROR: transaction is read-only)
            if (dialect instanceof PostgreSQLDialect)
            {
                // ALF-4226
                @SuppressWarnings("unused")
                int i = 0;
                
            }
            else
            {
                throw e;
            }
        }
        finally
        {
            transactionService.setAllowWrite(true, vetoName);
            try
            {
                txn.rollback();
            }
            catch (Throwable e) {}
        }
    }
    
    /**
     * Test the write veto
     * @throws Exception
     */
    public void testReadOnlyVetoTxn() throws Exception
    {
       
        QName v1 = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "V1");
        QName v2 = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "V2");
        QName v3 = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "V2");
        try
        {
            // start a read-only transaction
            transactionService.setAllowWrite(false, v1);
            transactionService.setAllowWrite(false, v2);
        
            assertFalse("v1 AND v2 veto not read only", transactionService.getAllowWrite());
            
            transactionService.setAllowWrite(true, v2);
            assertFalse("v1 not read only", transactionService.getAllowWrite());
            
            transactionService.setAllowWrite(true, v1);
            assertTrue("v1 still read only", transactionService.getAllowWrite());
        
            /**
             * Remove non existent veto
             */
            transactionService.setAllowWrite(true, v3);
            assertTrue("v3 veto", transactionService.getAllowWrite());
            
            
        }
        finally
        {
            transactionService.setAllowWrite(true, v1);
            transactionService.setAllowWrite(true, v2);
            transactionService.setAllowWrite(true, v3);
        }
    }
           
    public void testGetRetryingTransactionHelper()
    {
        RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                return null;
            }
        };
        
        assertFalse("Retriers must be new instances",
                transactionService.getRetryingTransactionHelper() == transactionService.getRetryingTransactionHelper());
        
        transactionService.setAllowWrite(true, vetoName);
        transactionService.getRetryingTransactionHelper().doInTransaction(callback, true);
        transactionService.getRetryingTransactionHelper().doInTransaction(callback, false);

        transactionService.setAllowWrite(false, vetoName);
        transactionService.getRetryingTransactionHelper().doInTransaction(callback, true);
        try
        {
            transactionService.getRetryingTransactionHelper().doInTransaction(callback, false);
            fail("Expected AccessDeniedException when starting to write to a read-only transaction service.");
        }
        catch (AccessDeniedException e)
        {
            // Expected
        }
        
        // Now check that we can force writable transactions
        RetryingTransactionHelper helper = transactionService.getRetryingTransactionHelper();
        helper.setForceWritable(true);
        helper.doInTransaction(callback, true);
        helper.doInTransaction(callback, false);
        
        transactionService.setAllowWrite(true, vetoName);
    }
}
