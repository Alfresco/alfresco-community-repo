/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.domain.node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.cache.TransactionalCache;
import org.alfresco.repo.cache.TransactionalCache.ValueHolder;
import org.alfresco.repo.domain.node.NodeDAO.NodeRefQueryCallback;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.Pair;
import org.alfresco.util.testing.category.DBTests;
import org.alfresco.util.testing.category.LuceneTests;
import org.junit.Assert;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;

/**
 * Additional tests for the Node DAO.
 * 
 * @see NodeDAO 
 * 
 * @author Derek Hulley
 * @since 3.4
 */
@Category({OwnJVMTestsCategory.class, DBTests.class, LuceneTests.class})
public class NodeDAOTest extends TestCase
{
    private ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private TransactionService transactionService;
    private RetryingTransactionHelper txnHelper;
    private NodeDAO nodeDAO;
    private SimpleCache<Serializable, ValueHolder<Node>> rootNodesCache;    
    @SuppressWarnings("unchecked")
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
        rootNodesCache = (SimpleCache<Serializable, ValueHolder<Node>>) ctx.getBean("node.rootNodesSharedCache");
    }
    
    public void testTransaction() throws Throwable
    {
        final boolean[] newTxn = new boolean[] {false};
        RetryingTransactionCallback<Long> getTxnIdCallback = new RetryingTransactionCallback<Long>()
        {
            public Long execute() throws Throwable
            {
                return nodeDAO.getCurrentTransactionId(newTxn[0]);
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
        assertNull("No Txn ID should be present in read-only txn", txnHelper.doInTransaction(getTxnIdCallback, true));
        // First success
        Long txnId1 = txnHelper.doInTransaction(getTxnIdCallback);
        assertNull("No Txn ID should be present in untouched txn", txnId1);
        // Second success
        newTxn[0] = true;
        Long txnId2 = txnHelper.doInTransaction(getTxnIdCallback);
        assertNotNull("Txn ID should be present by forcing it", txnId2);
    }
    
    public void testSelectNodePropertiesByTypes() throws Exception
    {
        final Set<QName> qnames = Collections.singleton(ContentModel.PROP_NAME);
        RetryingTransactionCallback<List<NodePropertyEntity>> callback = new RetryingTransactionCallback<List<NodePropertyEntity>>()
        {
            public List<NodePropertyEntity> execute() throws Throwable
            {
                return nodeDAO.selectNodePropertiesByTypes(qnames);
            }
        };
        List<NodePropertyEntity> props = txnHelper.doInTransaction(callback, true);
        if (props.size() == 0)
        {
            return;
        }
        NodePropertyEntity prop = props.get(0);
        String value = prop.getValue().getStringValue();
        assertNotNull(value);
    }
    
    public void testSelectNodePropertiesByDataType() throws Exception
    {
        // Prepare the bits that repeat the actual query
        final AtomicLong min = new AtomicLong(0L);
        final AtomicLong max = new AtomicLong(0L);
        RetryingTransactionCallback<List<NodePropertyEntity>> callback = new RetryingTransactionCallback<List<NodePropertyEntity>>()
        {
            public List<NodePropertyEntity> execute() throws Throwable
            {
                long minNodeId = min.get();
                long maxNodeId = max.get();
                return nodeDAO.selectNodePropertiesByDataType(DataTypeDefinition.TEXT, minNodeId, maxNodeId);
            }
        };
        
        // Get the current max node id
        Long minNodeId = nodeDAO.getMinNodeId();
        if (minNodeId == null)
        {
            return;                 // there are no nodes!
        }
        Long maxNodeId = nodeDAO.getMaxNodeId();            // won't be null at this point as we have a min
        min.set(minNodeId.longValue());
        
        // Iterate across all nodes in the system
        while (min.longValue() <= maxNodeId.longValue())
        {
            max.set(min.get() + 1000L);                     // 1K increments
            
            // Get the properties
            List<NodePropertyEntity> props = txnHelper.doInTransaction(callback, true);
            for (NodePropertyEntity prop : props)
            {
                // Check the property
                Long nodeId = prop.getNodeId();
                assertNotNull(nodeId);
                assertTrue("the min should be inclusive.", min.longValue() <= nodeId.longValue());
                assertTrue("the max should be exclusive.", max.longValue() > nodeId.longValue());
                NodePropertyValue propVal = prop.getValue();
                assertNotNull(propVal);
                assertEquals("STRING", propVal.getActualTypeString());
                String value = (String) propVal.getValue(DataTypeDefinition.TEXT);
                assertNotNull(value);
                // This all checks out
            }
            
            // Shift the window up
            min.set(max.get());
        }
    }
    
    public void testGetNodeIdsIntervalForType() throws Exception
    {
        // Different calls with equivalent parameters should return the same values
        Pair<Long, Long> interval1 = getNodeIdsInterval(0L, System.currentTimeMillis());
        Pair<Long, Long> interval2 = getNodeIdsInterval(null, System.currentTimeMillis());
        Pair<Long, Long> interval3 = getNodeIdsInterval(null, null);
        
        assertEquals(interval1.getFirst(), interval2.getFirst());
        assertEquals(interval2.getFirst(), interval3.getFirst());
        
        assertEquals(interval1.getSecond(), interval2.getSecond());
        assertEquals(interval2.getSecond(), interval3.getSecond());
    }
    
    private Pair<Long, Long> getNodeIdsInterval(final Long minTxnTime, final Long maxTxnTime)
    {
        RetryingTransactionCallback<Pair<Long, Long>> callback = new RetryingTransactionCallback<Pair<Long, Long>>()
        {
            public Pair<Long, Long> execute() throws Throwable
            {
                return nodeDAO.getNodeIdsIntervalForType(ContentModel.TYPE_FOLDER, minTxnTime, maxTxnTime);
            }
        };
        
        return txnHelper.doInTransaction(callback, true);
    }
    
    public void testSelectChildAssocsWithoutNodeAssocsOfTypes()
    {
        final StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
        final Long parentId = nodeDAO.getRootNode(storeRef).getFirst();
        
        final AtomicLong min = new AtomicLong(0L);
        final AtomicLong max = new AtomicLong(0L);
        final Set<QName> typeAssocsToExclude = Collections.singleton(QName.createQName("noType"));
        
        RetryingTransactionCallback<List<Node>> callback = new RetryingTransactionCallback<List<Node>>()
        {
            public List<Node> execute() throws Throwable
            {
                long minNodeId = min.get();
                long maxNodeId = max.get();
                return nodeDAO.selectChildAssocsWithoutNodeAssocsOfTypes(parentId, minNodeId, maxNodeId, typeAssocsToExclude);
            }
        };
        
        // Get the current node range
        Pair<Long, Long> nodeRange = getNodeIdsInterval(0L, System.currentTimeMillis());
                
        Long minNodeId = nodeRange.getFirst();
        Long maxNodeId = nodeRange.getSecond();
        
        min.set(minNodeId.longValue());

        // Iterate across the nodes in the [minNodeId, maxNodeId] interval
        while (min.longValue() <= maxNodeId.longValue())
        {
            max.set(min.get() + 100L);  // 100 increments
            // Get the nodes
            List<Node> nodes = txnHelper.doInTransaction(callback, true);
            for (Node node : nodes)
            {
                Long nodeId = node.getId();
                assertNotNull(nodeId);
                assertTrue("the min should be inclusive.", min.longValue() <= nodeId.longValue());
                assertTrue("the max should be exclusive.", max.longValue() > nodeId.longValue());
            }

            // Shift the window up
            min.set(max.get());
        }
    }
    
    public void testGetNodesWithAspects() throws Throwable
    {
        final NodeRefQueryCallback callback = new NodeRefQueryCallback()
        {
            public boolean handle(Pair<Long, NodeRef> nodePair)
            {
                // Don't need more
                return false;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                nodeDAO.getNodesWithAspects(
                        Collections.singleton(ContentModel.ASPECT_AUDITABLE),
                        1L, 1000L,
                        callback);
                return null;
            }
        }, true);
    }
    
    public void testGetMinMaxNodeId() throws Exception
    {
        Long minNodeId = nodeDAO.getMinNodeId();
        assertNotNull(minNodeId);
        assertTrue(minNodeId.longValue() > 0L);
        Long maxNodeId = nodeDAO.getMaxNodeId();
        assertNotNull(maxNodeId);
        assertTrue(maxNodeId.longValue() > minNodeId.longValue());
    }
    
    public void testGetPrimaryChildAcls() throws Throwable
    {
        List<NodeIdAndAclId> acls = nodeDAO.getPrimaryChildrenAcls(1L);
        assertNotNull("Null list", acls);
    }
    
    public void testGetStoreId() throws Throwable
    {
        // Get all stores
        List<Pair<Long, StoreRef>> storePairs = nodeDAO.getStores();
        // Check each one
        for (Pair<Long, StoreRef> storePair : storePairs)
        {
            StoreRef storeRef = storePair.getSecond();
            // Check
            Pair<Long, StoreRef> checkStorePair = nodeDAO.getStore(storeRef);
            assertEquals("Store pair did not match. ", storePair, checkStorePair);
        }
    }
    
    public void testCacheNodes() throws Throwable
    {
        Long minNodeId = nodeDAO.getMinNodeId();
        final List<Long> nodeIds = new ArrayList<Long>(10000);
        for (long i = 0; i < 1000; i++)
        {
            nodeIds.add(Long.valueOf(minNodeId.longValue() + i));
        }
        RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                nodeDAO.cacheNodesById(nodeIds);
                return null;
            }
        };
        txnHelper.doInTransaction(callback, true);
    }
    
    /**
     * Ensure that the {@link NodeEntity} values cached as root nodes are valid instances.
     * <p/>
     * ACE-987: NPE in NodeEntity during post-commit write through to shared cache
     */
    public void testRootNodeCacheEntries() throws Throwable
    {
        // Get the stores
        List<Pair<Long, StoreRef>> storeRefPairs = nodeDAO.getStores();
        assertTrue("No stores in the system.", storeRefPairs.size() > 0);
        // Drop all cache entries and reload them one by one
        for (Pair<Long, StoreRef> storeRefPair : storeRefPairs)
        {
            StoreRef storeRef = storeRefPair.getSecond();
            nodeDAO.getRootNode(storeRef);
        }
        // The cache should be populated again
        Collection<Serializable> keys = rootNodesCache.getKeys();
        assertTrue("Cache entries were not populated. ", keys.size() > 0);
        // Check each root node
        for (Serializable key : keys)
        {
            NodeEntity node = (NodeEntity) TransactionalCache.getSharedCacheValue(rootNodesCache, key);
            
            // Create a good value
            NodeEntity clonedNode = (NodeEntity) node.clone();
            // Run equals and hashcode
            node.hashCode();
            Assert.assertEquals(node, clonedNode);          // Does NPE check implicitly
        }
    }

    public void testGetMinTxInNodeIdRange()
    {
        Long fromNodeId = nodeDAO.getMinNodeId();
        Long toNodeId = nodeDAO.getMaxNodeId();
        
        Long minTxCommitTime = nodeDAO.getMinTxnCommitTime();
        
        Long minTxCommitTimeInRange = nodeDAO.getMinTxInNodeIdRange(fromNodeId, toNodeId);
        assertEquals(minTxCommitTime, minTxCommitTimeInRange);
    }
    
    public void testGetMaxTxInNodeIdRange()
    {
        Long fromNodeId = nodeDAO.getMinNodeId();
        Long toNodeId = nodeDAO.getMaxNodeId();
        
        Long maxTxCommitTime = nodeDAO.getMaxTxnCommitTime();
        
        Long maxTxCommitTimeInRange = nodeDAO.getMaxTxInNodeIdRange(fromNodeId, toNodeId);
        assertEquals(maxTxCommitTime, maxTxCommitTimeInRange);
    }
    
    public void testGetNextTxCommitTime()
    {
        Long fromCommitTime = nodeDAO.getMinTxnCommitTime();
        
        Long nextTxnCommitTime = nodeDAO.getNextTxCommitTime(fromCommitTime);
        assertTrue(nextTxnCommitTime >= fromCommitTime);
    }

}