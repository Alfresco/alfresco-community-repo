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
package org.alfresco.repo.domain.node;

import java.util.List;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.node.NodeDAO.NodeRefQueryCallback;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.springframework.context.ApplicationContext;

/**
 * Additional tests for the Node DAO.
 * 
 * @see NodeDAO 
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class NodeDAOTest extends TestCase
{
    private ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private TransactionService transactionService;
    private RetryingTransactionHelper txnHelper;
    private NodeDAO nodeDAO;
    
    @Override
    public void setUp()
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        transactionService = serviceRegistry.getTransactionService();
        txnHelper = transactionService.getRetryingTransactionHelper();
        txnHelper.setMinRetryWaitMs(10);
        txnHelper.setRetryWaitIncrementMs(10);
        txnHelper.setMaxRetryWaitMs(50);
        
        nodeDAO = (NodeDAO) ctx.getBean("nodeDAO");
    }
    
    public void testTransaction() throws Throwable
    {
        RetryingTransactionCallback<Long> getTxnIdCallback = new RetryingTransactionCallback<Long>()
        {
            public Long execute() throws Throwable
            {
                return nodeDAO.getCurrentTransactionId();
            }
        };
        // No txn
        try
        {
            getTxnIdCallback.execute();
            fail("Should have failed when running outside of a transaction");
        }
        catch (Throwable e)
        {
            // Expected
        }
        // Read-only
        try
        {
            txnHelper.doInTransaction(getTxnIdCallback, true);
            fail("Should have failed when running in read-only transaction");
        }
        catch (Throwable e)
        {
            // Expected
        }
        // First success
        Long txnId1 = txnHelper.doInTransaction(getTxnIdCallback);
        assertNotNull("No txn ID 1", txnId1);
        Long txnId2 = txnHelper.doInTransaction(getTxnIdCallback);
        assertNotNull("No txn ID 2", txnId2);
        assertTrue("2nd ID should be larger than first", txnId2.compareTo(txnId1) > 0);
    }
    
    /**
     * TODO: Clean up after test or force indexing on the stores
     */
    public void xtestNewStore() throws Exception
    {
        final StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_TEST, getName() + "-" + GUID.generate());
        RetryingTransactionCallback<Pair<Long, NodeRef>> callback = new RetryingTransactionCallback<Pair<Long, NodeRef>>()
        {
            public Pair<Long, NodeRef> execute() throws Throwable
            {
                return nodeDAO.newStore(storeRef);
            }
        };
        Pair<Long, NodeRef> rootNodePair = txnHelper.doInTransaction(callback);
        assertNotNull(rootNodePair);
        assertEquals("Root node has incorrect store", storeRef, rootNodePair.getSecond().getStoreRef());
        // Should fail second time
        try
        {
            txnHelper.doInTransaction(callback);
            fail("Should not be able to create same store.");
        }
        catch (Throwable e)
        {
            // Expected
        }
    }
    
    public void testGetNodesWithAspects() throws Throwable
    {
        NodeRefQueryCallback callback = new NodeRefQueryCallback()
        {
            public boolean handle(Pair<Long, NodeRef> nodePair)
            {
                // Don't need more
                return false;
            }
        };
        nodeDAO.getNodesWithAspect(ContentModel.ASPECT_AUDITABLE, 1L, 5, callback);
    }
    
    public void testGetPrimaryChildAcls() throws Throwable
    {
        List<NodeIdAndAclId> acls = nodeDAO.getPrimaryChildrenAcls(1L);
        assertNotNull("Null list", acls);
    }
}