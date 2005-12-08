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
