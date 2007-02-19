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

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * @see org.alfresco.repo.transaction.TransactionComponent
 * 
 * @author Derek Hulley
 */
public class TransactionComponentTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private PlatformTransactionManager transactionManager;
    private TransactionComponent transactionComponent;
    private NodeService nodeService;
    
    public void setUp() throws Exception
    {
        transactionManager = (PlatformTransactionManager) ctx.getBean("transactionManager");
        transactionComponent = new TransactionComponent();
        transactionComponent.setTransactionManager(transactionManager);
        transactionComponent.setAllowWrite(true);
        
        nodeService = (NodeService) ctx.getBean("dbNodeService");
    }
    
    public void testPropagatingTxn() throws Exception
    {
        // start a transaction
        UserTransaction txnOuter = transactionComponent.getUserTransaction();
        txnOuter.begin();
        String txnIdOuter = AlfrescoTransactionSupport.getTransactionId();
        
        // start a propagating txn
        UserTransaction txnInner = transactionComponent.getUserTransaction();
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
        UserTransaction txnOuter = transactionComponent.getUserTransaction();
        txnOuter.begin();
        String txnIdOuter = AlfrescoTransactionSupport.getTransactionId();
        
        // start a propagating txn
        UserTransaction txnInner = transactionComponent.getNonPropagatingUserTransaction();
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
        transactionComponent.setAllowWrite(false);
        
        UserTransaction txn = transactionComponent.getUserTransaction();
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
            int i = 0;
            // expected
        }
    }
}
