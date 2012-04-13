package org.alfresco.repo.node.cleanup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
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
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.webscripts.GUID;

public class TransactionCleanupTest
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private TransactionService transactionService;
    private NodeService nodeService;
    private SearchService searchService;
    private MutableAuthenticationService authenticationService;
    private NodeDAO nodeDAO;
    private DeletedNodeCleanupWorker worker;

    private NodeRef nodeRef1;
    private NodeRef nodeRef2;
    private NodeRef nodeRef3;
    private NodeRef nodeRef4;
    private NodeRef nodeRef5;
    private RetryingTransactionHelper helper;

    @Before
    public void before()
    {
    	ServiceRegistry serviceRegistry = (ServiceRegistry)ctx.getBean("ServiceRegistry");
		this.transactionService = serviceRegistry.getTransactionService();
		this.authenticationService = (MutableAuthenticationService)ctx.getBean("authenticationService");
		this.nodeService = serviceRegistry.getNodeService();
		this.searchService = serviceRegistry.getSearchService();
		this.nodeDAO = (NodeDAO)ctx.getBean("nodeDAO");
        this.worker = (DeletedNodeCleanupWorker)ctx.getBean("nodeCleanup.deletedNodeCleanup");
        this.worker.setMinPurgeAgeDays(0);

    	this.helper = transactionService.getRetryingTransactionHelper();
        authenticationService.authenticate("admin", "admin".toCharArray());

		StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
		ResultSet resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, "PATH:\"/app:company_home\"");
		final NodeRef companyHome = resultSet.getNodeRef(0);
		resultSet.close();

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
    	UpdateNode updateNode4 = new UpdateNode(nodeRef4);
    	UpdateNode updateNode5 = new UpdateNode(nodeRef5);

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
    		if(i == 2)
    		{
    			String txnId3 = helper.doInTransaction(updateNode3, false, true);
    			txnIds3.add(txnId3);
    		}
    		if(i == 3)
    		{
    			String txnId4 = helper.doInTransaction(updateNode4, false, true);
    			txnIds4.add(txnId4);
    		}
    		if(i == 4)
    		{
    			String txnId5 = helper.doInTransaction(updateNode5, false, true);
    			txnIds5.add(txnId5);
    		}
    	}

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

    	long start = System.currentTimeMillis();
    	Long minTxnId = nodeDAO.getMinTxnId();

    	final Map<NodeRef, List<String>> txnIds = createTransactions();
    	final List<String> txnIds1 = txnIds.get(nodeRef1);
    	final List<String> txnIds2 = txnIds.get(nodeRef2);
    	final List<String> txnIds3 = txnIds.get(nodeRef3);
    	final List<String> txnIds4 = txnIds.get(nodeRef4);
    	final List<String> txnIds5 = txnIds.get(nodeRef5);

    	// run the transaction cleaner
        worker.setPurgeSize(5); // small purge size
    	worker.doClean();

    	// Get transactions committed after the test started
    	List<Transaction> txns = nodeDAO.getTxnsByCommitTimeAscending(Long.valueOf(start), Long.valueOf(Long.MAX_VALUE), Integer.MAX_VALUE, null, false);
    	
    	List<String> expectedUnusedTxnIds = new ArrayList<String>(10);
    	expectedUnusedTxnIds.addAll(txnIds1.subList(0, txnIds1.size() - 1));

    	List<String> expectedUsedTxnIds = new ArrayList<String>(5);
    	expectedUsedTxnIds.add(txnIds1.get(txnIds1.size() - 1));
    	expectedUsedTxnIds.addAll(txnIds2);
    	expectedUsedTxnIds.addAll(txnIds3);
    	expectedUsedTxnIds.addAll(txnIds4);
    	expectedUsedTxnIds.addAll(txnIds5);

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

    	assertEquals(5, numFoundUsedTxnIds);

    	List<Long> txnsUnused = nodeDAO.getTxnsUnused(minTxnId, Long.MAX_VALUE, Integer.MAX_VALUE);
    	assertEquals(0, txnsUnused.size());
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
}
