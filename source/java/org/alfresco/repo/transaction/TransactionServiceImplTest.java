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
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.TransientDataAccessResourceException;
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
    
    public void setUp() throws Exception
    {
        transactionManager = (PlatformTransactionManager) ctx.getBean("transactionManager");
        transactionService = new TransactionServiceImpl();
        transactionService.setTransactionManager(transactionManager);   
        transactionService.setAllowWrite(true);
        transactionService.setAuthenticationContext((AuthenticationContext) ctx.getBean("authenticationContext"));
        transactionService.setSysAdminParams((SysAdminParams) ctx.getBean("sysAdminParams"));

        nodeService = (NodeService) ctx.getBean("dbNodeService");
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
        transactionService.setAllowWrite(false);
        
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
        catch (InvalidDataAccessApiUsageException e)
        {
            @SuppressWarnings("unused")
            int i = 0;
            // expected this ...
        }
        catch (TransientDataAccessResourceException e)
        {
            @SuppressWarnings("unused")
            int i = 0;
            // or this
        }
        finally
        {
            try
            {
                txn.rollback();
            }
            catch (Throwable e) {}
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
        
        transactionService.setAllowWrite(true);
        transactionService.getRetryingTransactionHelper().doInTransaction(callback, true);
        transactionService.getRetryingTransactionHelper().doInTransaction(callback, false);

        transactionService.setAllowWrite(false);
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
    }
}
