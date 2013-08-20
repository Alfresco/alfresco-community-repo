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
package org.alfresco.repo.domain.usage;

import java.util.Set;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.node.ChildAssocEntity;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * @see UsageDAO
 * 
 * @author janv
 * @since 3.4
 */
public class UsageDAOTest extends TestCase
{
    private ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private TransactionService transactionService;
    private RetryingTransactionHelper txnHelper;
    
    private UsageDAO usageDAO;
    private NodeDAO nodeDAO;
    
    private final static StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
    
    @Override
    public void setUp() throws Exception
    {
        transactionService = (TransactionService)ctx.getBean("transactionService");
        txnHelper = transactionService.getRetryingTransactionHelper();
        
        usageDAO = (UsageDAO)ctx.getBean("usageDAO");
        nodeDAO = (NodeDAO)ctx.getBean("nodeDAO");
    }
    
    private NodeRef getRootNodeRef()
    {
        return nodeDAO.getRootNode(storeRef).getSecond();
    }
    
    private long getNodeId(NodeRef nodeRef)
    {
        return nodeDAO.getNodePair(nodeRef).getFirst();
    }
    
    private NodeRef createNode(long parentNodeId)
    {
        ChildAssocEntity assoc = nodeDAO.newNode(
                parentNodeId,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.ASSOC_CHILDREN,
                storeRef,
                null,
                ContentModel.TYPE_CONTENT,
                I18NUtil.getLocale(),
                null,
                null);
        
        return assoc.getChildNode().getNodeRef();
    }
    
    public void testCreateAndDeleteUsageDeltas() throws Exception
    {
        RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                NodeRef rootNodeRef = getRootNodeRef();
                long rootNodeId = getNodeId(rootNodeRef);
                usageDAO.deleteDeltas(rootNodeId);
//                assertEquals(0L, usageDAO.getTotalDeltaSize(rootNodeRef, false));
                
                Set<NodeRef> usageDeltaNodes = usageDAO.getUsageDeltaNodes();
                for (NodeRef nodeRef : usageDeltaNodes)
                {
                    long nodeId = getNodeId(nodeRef);
                    usageDAO.deleteDeltas(nodeId);
                }
                
                assertEquals(0, usageDAO.getUsageDeltaNodes().size());
                
                usageDAO.insertDelta(rootNodeRef, 100L);
                assertEquals(100L, usageDAO.getTotalDeltaSize(rootNodeRef, false));
                
                assertEquals(1, usageDAO.getUsageDeltaNodes().size());
                
                usageDAO.insertDelta(rootNodeRef, 1000L);
                assertEquals(1100L, usageDAO.getTotalDeltaSize(rootNodeRef, false));
                
                assertEquals(1, usageDAO.getUsageDeltaNodes().size());
                
                usageDAO.insertDelta(rootNodeRef, -500L);
                assertEquals(600L, usageDAO.getTotalDeltaSize(rootNodeRef, false));
                
                assertEquals(1, usageDAO.getUsageDeltaNodes().size());
                
                NodeRef nodeRef1 = createNode(rootNodeId);
                long nodeId1 = getNodeId(nodeRef1);
                assertEquals(0L, usageDAO.getTotalDeltaSize(nodeRef1, false));
                
                NodeRef nodeRef2 = createNode(rootNodeId);
                long nodeId2 = getNodeId(nodeRef2);
                assertEquals(0L, usageDAO.getTotalDeltaSize(nodeRef2, false));
                
                assertEquals(1, usageDAO.getUsageDeltaNodes().size());
                
                usageDAO.insertDelta(nodeRef1, 200L);
                assertEquals(200L, usageDAO.getTotalDeltaSize(nodeRef1, false));
                
                assertEquals(2, usageDAO.getUsageDeltaNodes().size());
                
                usageDAO.insertDelta(nodeRef2, -400L);
                assertEquals(-400L, usageDAO.getTotalDeltaSize(nodeRef2, false));
                
                assertEquals(3, usageDAO.getUsageDeltaNodes().size());
                
                usageDAO.deleteDeltas(rootNodeId);
                assertEquals(0L, usageDAO.getTotalDeltaSize(rootNodeRef, false));
                
                assertEquals(2, usageDAO.getUsageDeltaNodes().size());
                
                usageDAO.deleteDeltas(nodeId1);
                assertEquals(0L, usageDAO.getTotalDeltaSize(nodeRef1, false));
                
                assertEquals(1, usageDAO.getUsageDeltaNodes().size());
                
                usageDAO.deleteDeltas(nodeId2);
                assertEquals(0L, usageDAO.getTotalDeltaSize(nodeRef2, false));
                
                assertEquals(0, usageDAO.getUsageDeltaNodes().size());
                
                return null;
            }
        };
        
        txnHelper.doInTransaction(callback);
    }
    
    public void testCreateUsageDeltasWithRollback() throws Exception
    {
        RetryingTransactionCallback<Void> voidCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                NodeRef rootNodeRef = getRootNodeRef();
                long rootNodeId = getNodeId(rootNodeRef);
                
                usageDAO.deleteDeltas(rootNodeId);
                assertEquals(0L, usageDAO.getTotalDeltaSize(rootNodeRef, false));
                
                Set<NodeRef> usageDeltaNodes = usageDAO.getUsageDeltaNodes();
                for (NodeRef nodeRef : usageDeltaNodes)
                {
                    long nodeId = getNodeId(nodeRef);
                    usageDAO.deleteDeltas(nodeId);
                }
                
                assertEquals(0, usageDAO.getUsageDeltaNodes().size());
                
                return null;
            }
        };
        
        txnHelper.doInTransaction(voidCallback);
        
        voidCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                NodeRef usageNodeRef = getRootNodeRef();
                
                usageDAO.insertDelta(usageNodeRef, 100L);
                
                // Now force a rollback
                throw new RuntimeException("Forced");
            }
        };
        
        try
        {
            txnHelper.doInTransaction(voidCallback);
            fail("Transaction didn't roll back");
        }
        catch (RuntimeException e)
        {
            // Expected
        }
        
        // Check that it doesn't exist
        voidCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                NodeRef usageNodeRef = getRootNodeRef();
                
                assertEquals(0L, usageDAO.getTotalDeltaSize(usageNodeRef, false));
                
                assertEquals(0, usageDAO.getUsageDeltaNodes().size());
                
                return null;
            }
        };
        
        txnHelper.doInTransaction(voidCallback);
    }
}
