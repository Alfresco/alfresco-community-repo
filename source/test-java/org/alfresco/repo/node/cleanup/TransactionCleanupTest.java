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
package org.alfresco.repo.node.cleanup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.Transaction;
import org.alfresco.repo.node.db.DeletedNodeCleanupWorker;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.webscripts.GUID;

/**
 * @author Derek Hulley
 * @since 4.0
 */
@Category(OwnJVMTestsCategory.class)
public class TransactionCleanupTest
{
    private static Log logger = LogFactory.getLog(TransactionCleanupTest.class);
    
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private TransactionService transactionService;
    private NodeService nodeService;
    private SearchService searchService;
    private MutableAuthenticationService authenticationService;
    private NodeDAO nodeDAO;
    private SimpleCache<Serializable, Serializable> nodesCache;
    private DeletedNodeCleanupWorker worker;

    private NodeRef nodeRef1;
    private NodeRef nodeRef2;
    private NodeRef nodeRef3;
    private NodeRef nodeRef4;
    private NodeRef nodeRef5;
    private RetryingTransactionHelper helper;

    @SuppressWarnings("unchecked")
    @Before
    public void before()
    {
    	ServiceRegistry serviceRegistry = (ServiceRegistry)ctx.getBean("ServiceRegistry");
        NamespaceService namespaceService = serviceRegistry.getNamespaceService();
		this.transactionService = serviceRegistry.getTransactionService();
		this.authenticationService = (MutableAuthenticationService)ctx.getBean("authenticationService");
		this.nodeService = serviceRegistry.getNodeService();
		this.searchService = serviceRegistry.getSearchService();
		this.nodeDAO = (NodeDAO)ctx.getBean("nodeDAO");
		this.nodesCache = (SimpleCache<Serializable, Serializable>) ctx.getBean("node.nodesSharedCache");
        this.worker = (DeletedNodeCleanupWorker)ctx.getBean("nodeCleanup.deletedNodeCleanup");
        this.worker.setMinPurgeAgeDays(0);

    	this.helper = transactionService.getRetryingTransactionHelper();
        authenticationService.authenticate("admin", "admin".toCharArray());
        
		StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
		NodeRef storeRoot = nodeService.getRootNode(storeRef);
		List<NodeRef> nodeRefs = searchService.selectNodes(
		        storeRoot, "/app:company_home", null, namespaceService, false);
		final NodeRef companyHome = nodeRefs.get(0);

    	RetryingTransactionHelper.RetryingTransactionCallback<NodeRef> createNode = new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
    	{
			@Override
			public NodeRef execute() throws Throwable
			{
				return nodeService.createNode(companyHome, ContentModel.ASSOC_CONTAINS, QName.createQName("test", GUID.generate()), ContentModel.TYPE_CONTENT).getChildRef();
			}
    	};
    	this.nodeRef1 = helper.doInTransaction(createNode, false, true);
    	this.nodeRef2 = helper.doInTransaction(createNode, false, true);
    	this.nodeRef3 = helper.doInTransaction(createNode, false, true);
    	this.nodeRef4 = helper.doInTransaction(createNode, false, true);
    	this.nodeRef5 = helper.doInTransaction(createNode, false, true);
    }

    private Map<NodeRef, List<String>> createTransactions()
    {
    	Map<NodeRef, List<String>> txnIds = new HashMap<NodeRef, List<String>>();

    	UpdateNode updateNode1 = new UpdateNode(nodeRef1);
    	UpdateNode updateNode2 = new UpdateNode(nodeRef2);
    	UpdateNode updateNode3 = new UpdateNode(nodeRef3);
    	DeleteNode deleteNode4 = new DeleteNode(nodeRef4);
    	DeleteNode deleteNode5 = new DeleteNode(nodeRef5);

    	List<String> txnIds1 = new ArrayList<String>();
    	List<String> txnIds2 = new ArrayList<String>();
    	List<String> txnIds3 = new ArrayList<String>();
    	List<String> txnIds4 = new ArrayList<String>();
    	List<String> txnIds5 = new ArrayList<String>();
    	txnIds.put(nodeRef1, txnIds1);
    	txnIds.put(nodeRef2, txnIds2);
    	txnIds.put(nodeRef3, txnIds3);
    	txnIds.put(nodeRef4, txnIds4);
    	txnIds.put(nodeRef5, txnIds5);

    	for(int i = 0; i < 10; i++)
    	{
    		String txnId1 = helper.doInTransaction(updateNode1, false, true);
    		txnIds1.add(txnId1);
    		if(i == 0)
    		{
    			String txnId2 = helper.doInTransaction(updateNode2, false, true);
    			txnIds2.add(txnId2);
    		}
    		if(i == 1)
    		{
    			String txnId3 = helper.doInTransaction(updateNode3, false, true);
    			txnIds3.add(txnId3);
    		}
    	}
        String txnId4 = helper.doInTransaction(deleteNode4, false, true);
        txnIds4.add(txnId4);
        String txnId5 = helper.doInTransaction(deleteNode5, false, true);
        txnIds5.add(txnId5);

		return txnIds;
    }
    
    private boolean containsTransaction(List<Transaction> txns, String txnId)
    {
    	boolean found = false;
    	for(Transaction tx : txns)
    	{
    		if(tx.getChangeTxnId().equals(txnId))
    		{
    			found = true;
        		break;
    		}
    	}
    	return found;
    }

    @Test
    public void testPurgeUnusedTransactions() throws Exception
    {
    	// Execute transactions that update a number of nodes. For nodeRef1, all but the last txn will be unused.

    	// run the transaction cleaner to clean up any existing unused transactions
    	worker.doClean();

    	final long start = System.currentTimeMillis();
    	final Long minTxnId = nodeDAO.getMinTxnId();

    	final Map<NodeRef, List<String>> txnIds = createTransactions();
    	final List<String> txnIds1 = txnIds.get(nodeRef1);
    	final List<String> txnIds2 = txnIds.get(nodeRef2);
    	final List<String> txnIds3 = txnIds.get(nodeRef3);
    	// Pure delete: final List<String> txnIds4 = txnIds.get(nodeRef4);
    	// Pure delete: final List<String> txnIds5 = txnIds.get(nodeRef5);
        
        // Double-check that n4 and n5 are present in deleted form
    	nodesCache.clear();
    	UserTransaction txn = transactionService.getUserTransaction(true);
    	txn.begin();
    	try
    	{
            assertNotNull("Node 4 is deleted but not purged", nodeDAO.getNodeRefStatus(nodeRef4));
            assertNotNull("Node 5 is deleted but not purged", nodeDAO.getNodeRefStatus(nodeRef5));
    	}
    	finally
    	{
    	    txn.rollback();
    	}

    	// run the transaction cleaner
        worker.setPurgeSize(5); // small purge size
    	List<String> reports = worker.doClean();
    	for (String report : reports)
        {
            logger.debug(report);
        }

    	// Get transactions committed after the test started
    	RetryingTransactionCallback<List<Transaction>> getTxnsCallback = new RetryingTransactionCallback<List<Transaction>>()
        {
            @Override
            public List<Transaction> execute() throws Throwable
            {
                return nodeDAO.getTxnsByCommitTimeAscending(Long.valueOf(start), Long.valueOf(Long.MAX_VALUE), Integer.MAX_VALUE, null, false);
            }
        };
    	List<Transaction> txns = transactionService.getRetryingTransactionHelper().doInTransaction(getTxnsCallback, true, false);
    	
    	List<String> expectedUnusedTxnIds = new ArrayList<String>(10);
    	expectedUnusedTxnIds.addAll(txnIds1.subList(0, txnIds1.size() - 1));

    	List<String> expectedUsedTxnIds = new ArrayList<String>(5);
    	expectedUsedTxnIds.add(txnIds1.get(txnIds1.size() - 1));
    	expectedUsedTxnIds.addAll(txnIds2);
    	expectedUsedTxnIds.addAll(txnIds3);
    	// 4 and 5 should not be in the list because they are deletes

    	// check that the correct transactions have been purged i.e. all except the last one to update the node
    	// i.e. in this case, all but the last one in txnIds1
    	int numFoundUnusedTxnIds = 0;
    	for(String txnId : expectedUnusedTxnIds)
    	{
    		if(!containsTransaction(txns, txnId))
    		{
    			numFoundUnusedTxnIds++;
    		}
    		else if(txnIds1.contains(txnId))
    		{
    			fail("Unused transaction(s) were not purged: " + txnId);
    		}
    	}
    	assertEquals(9, numFoundUnusedTxnIds);

    	// check that the correct transactions remain i.e. all those in txnIds2, txnIds3, txnIds4 and txnIds5
    	int numFoundUsedTxnIds = 0;
    	for(String txnId : expectedUsedTxnIds)
    	{
    		if(containsTransaction(txns, txnId))
    		{
    			numFoundUsedTxnIds++;
    		}
    	}

    	assertEquals(3, numFoundUsedTxnIds);

        // Get transactions committed after the test started
        RetryingTransactionCallback<List<Long>> getTxnsUnusedCallback = new RetryingTransactionCallback<List<Long>>()
        {
            @Override
            public List<Long> execute() throws Throwable
            {
                return nodeDAO.getTxnsUnused(minTxnId, Long.MAX_VALUE, Integer.MAX_VALUE);
            }
        };
        List<Long> txnsUnused = transactionService.getRetryingTransactionHelper().doInTransaction(getTxnsUnusedCallback, true, false);
    	assertEquals(0, txnsUnused.size());
    	
    	// Double-check that n4 and n5 were removed as well
        nodesCache.clear();
    	assertNull("Node 4 was not cleaned up", nodeDAO.getNodeRefStatus(nodeRef4));
        assertNull("Node 5 was not cleaned up", nodeDAO.getNodeRefStatus(nodeRef5));
    }
    
    @After
    public void after()
    {
		ApplicationContextHelper.closeApplicationContext();
    }
    
    private class UpdateNode implements RetryingTransactionCallback<String>
    {
    	private NodeRef nodeRef;

    	UpdateNode(NodeRef nodeRef)
    	{
    		this.nodeRef = nodeRef;
    	}

		@Override
		public String execute() throws Throwable
		{
			nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, GUID.generate());
			String txnId = AlfrescoTransactionSupport.getTransactionId();

			return txnId;
		}
    };
    
    private class DeleteNode implements RetryingTransactionCallback<String>
    {
        private NodeRef nodeRef;

        DeleteNode(NodeRef nodeRef)
        {
            this.nodeRef = nodeRef;
        }

        @Override
        public String execute() throws Throwable
        {
            nodeService.addAspect(nodeRef, ContentModel.ASPECT_TEMPORARY, null);
            nodeService.deleteNode(nodeRef);
            String txnId = AlfrescoTransactionSupport.getTransactionId();

            return txnId;
        }
    };
}
