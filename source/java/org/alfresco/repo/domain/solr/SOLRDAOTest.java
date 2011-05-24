package org.alfresco.repo.domain.solr;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.node.Node;
import org.alfresco.repo.domain.solr.SOLRDAO.NodeQueryCallback;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.PropertyMap;
import org.springframework.context.ConfigurableApplicationContext;

public class SOLRDAOTest extends TestCase
{
    private ConfigurableApplicationContext ctx = (ConfigurableApplicationContext) ApplicationContextHelper.getApplicationContext();

    private AuthenticationComponent authenticationComponent;
    private TransactionService transactionService;
    private RetryingTransactionHelper txnHelper;
    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private SOLRDAO solrDAO;
    
    private StoreRef storeRef;
    private NodeRef rootNodeRef;
    private NodeRef container1;
    private NodeRef content1;
    private NodeRef content2;
    
    @Override
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        transactionService = serviceRegistry.getTransactionService();
        txnHelper = transactionService.getRetryingTransactionHelper();
        
        solrDAO = (SOLRDAO)ctx.getBean("solrDAO");
        nodeService = (NodeService)ctx.getBean("NodeService");
        fileFolderService = (FileFolderService)ctx.getBean("FileFolderService");
        authenticationComponent = (AuthenticationComponent)ctx.getBean("authenticationComponent");
        
        authenticationComponent.setSystemUserAsCurrentUser();
        
        storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, getName() + System.currentTimeMillis());
        rootNodeRef = nodeService.getRootNode(storeRef);
    }

    private void buildTransactions1()
    {
        txnHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                PropertyMap props = new PropertyMap();
                props.put(ContentModel.PROP_NAME, "Container1");
                container1 = nodeService.createNode(
                		rootNodeRef,
                        ContentModel.ASSOC_CHILDREN,
                        ContentModel.ASSOC_CHILDREN,
                        ContentModel.TYPE_FOLDER,
                        props).getChildRef();
                
            	System.out.println("container1 = " + container1);

            	FileInfo content1Info = fileFolderService.create(container1, "Content1", ContentModel.TYPE_CONTENT);
            	content1 = content1Info.getNodeRef();
            	
            	System.out.println("content1 = " + content1);
            	
            	return null;
            }
        });

        txnHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
            	FileInfo content2Info = fileFolderService.create(container1, "Content2", ContentModel.TYPE_CONTENT);
            	content2 = content2Info.getNodeRef();

            	System.out.println("content2 = " + content2);
            	
            	fileFolderService.delete(content1);

            	return null;
            }
        });
    }
    
    private void buildTransactions2()
    {
        txnHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                PropertyMap props = new PropertyMap();
                props.put(ContentModel.PROP_NAME, "Container1");
                container1 = nodeService.createNode(
                		rootNodeRef,
                        ContentModel.ASSOC_CHILDREN,
                        ContentModel.ASSOC_CHILDREN,
                        ContentModel.TYPE_FOLDER,
                        props).getChildRef();
                
            	System.out.println("container1 = " + container1);

            	FileInfo content1Info = fileFolderService.create(container1, "Content1", ContentModel.TYPE_CONTENT);
            	content1 = content1Info.getNodeRef();
            	
            	System.out.println("content1 = " + content1);
            	
            	return null;
            }
        });

        txnHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
            	FileInfo content2Info = fileFolderService.create(container1, "Content2", ContentModel.TYPE_CONTENT);
            	content2 = content2Info.getNodeRef();

            	System.out.println("content2 = " + content2);
            	
            	fileFolderService.delete(content1);

            	return null;
            }
        });
    }
    
    public void testQueryTransactions1()
    {
        long startTime = System.currentTimeMillis();

        buildTransactions1();
        
        List<Transaction> txns = solrDAO.getTransactions(null, startTime, 0);
    	assertEquals("Number of transactions is incorrect", 2, txns.size());

    	int[] updates = new int[] {1, 1};
    	int[] deletes = new int[] {0, 1};
    	List<Long> txnIds = new ArrayList<Long>(txns.size());
    	int i = 0;
    	for(Transaction txn : txns)
    	{
    		assertEquals("Number of deletes is incorrect", deletes[i], txn.getDeletes());
    		assertEquals("Number of updates is incorrect", updates[i], txn.getUpdates());
    		i++;

    		txnIds.add(txn.getId());
    	}
    	
    	TestNodeQueryCallback nodeQueryCallback = new TestNodeQueryCallback(container1, content1, content2);
    	NodeParameters nodeParameters = new NodeParameters();
    	nodeParameters.setTransactionIds(txnIds);
    	solrDAO.getNodes(nodeParameters, 0, nodeQueryCallback);
    	
    	assertEquals("Unxpected nodes", 3, nodeQueryCallback.getSuccessCount());
    }
    
    public void testQueryTransactions2()
    {
        long startTime = System.currentTimeMillis();

        buildTransactions2();
        
        List<Transaction> txns = solrDAO.getTransactions(null, startTime, 0);
    	assertEquals("Number of transactions is incorrect", 2, txns.size());

    	int[] updates = new int[] {1, 1};
    	int[] deletes = new int[] {0, 1};
    	List<Long> txnIds = new ArrayList<Long>(txns.size());
    	int i = 0;
    	for(Transaction txn : txns)
    	{
    		assertEquals("Number of deletes is incorrect", deletes[i], txn.getDeletes());
    		assertEquals("Number of updates is incorrect", updates[i], txn.getUpdates());
    		i++;

    		txnIds.add(txn.getId());
    	}
    	
    	TestNodeQueryCallback nodeQueryCallback = new TestNodeQueryCallback(container1, content1, content2);
        NodeParameters nodeParameters = new NodeParameters();
        nodeParameters.setTransactionIds(txnIds);
    	solrDAO.getNodes(nodeParameters, 0, nodeQueryCallback);
    	
    	assertEquals("Unxpected nodes", 3, nodeQueryCallback.getSuccessCount());
    }

    
    private static class TestNodeQueryCallback implements NodeQueryCallback
    {
    	private int successCount = 0;
    	private NodeRef container1;
    	private NodeRef content1;
    	private NodeRef content2;
    	
		public TestNodeQueryCallback(NodeRef container1,
				NodeRef content1, NodeRef content2) {
			super();
			this.container1 = container1;
			this.content1 = content1;
			this.content2 = content2;
		}

		@Override
		public boolean handleNode(Node node) {
    		NodeRef nodeRef = node.getNodeRef();
    		Boolean isDeleted = node.getDeleted();
    		
    		System.out.println("Node: " + node.toString());
    		
    		if(nodeRef.equals(container1) && !isDeleted)
    		{
    			successCount++;
    		}

    		if(nodeRef.equals(content1) && isDeleted)
    		{
    			successCount++;
    		}

    		if(nodeRef.equals(content2) && !isDeleted)
    		{
    			successCount++;
    		}
			return true;
		}
		
		public int getSuccessCount()
		{
			return successCount;
		}
    }

}
