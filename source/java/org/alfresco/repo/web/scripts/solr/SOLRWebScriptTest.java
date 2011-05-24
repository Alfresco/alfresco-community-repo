package org.alfresco.repo.web.scripts.solr;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * Test the SOLR web scripts
 * 
 * @since 4.0
 */
// TODO check txn ids are correct - how to get txn ids?
// TODO create a node in txn1 and delete it in txn2 - only the delete in txn2 appears. Will SOLR not then see this as deletion of a non-existent node?
// TODO test getTxns: combinations of fromTxnId, fromCommitTime, maxResults
// TODO move/duplicate tests to SOLRDAO tests
public class SOLRWebScriptTest extends BaseWebScriptTest
{
    protected static final Log logger = LogFactory.getLog(SOLRWebScriptTest.class);

    private ApplicationContext ctx;
    private NodeDAO nodeDAO;
    private TransactionService transactionService;
    private NodeService nodeService;
    private ContentService contentService;
    private FileFolderService fileFolderService;
    private RetryingTransactionHelper txnHelper;

    private String admin;

    private StoreRef storeRef;
    private StoreRef storeRef1;
    private NodeRef rootNodeRef;
    private NodeRef rootNodeRef1;
    
    private NodeRef container1;
    private NodeRef container2;
    private NodeRef container3;
    private NodeRef container4;
    private NodeRef container5;
    private NodeRef content1;
    private NodeRef content2;
    private NodeRef content3;

    private long container1NodeID;
    private long container2NodeID;
    private long container3NodeID;
    private long container4NodeID;
    private long container5NodeID;
    private long content1NodeID;
    private long content2NodeID;
    private long content3NodeID;
    private long content4NodeID;
    private long content5NodeID;
    
    private JSONObject firstNode;
    private JSONObject secondNode;
    private JSONObject thirdNode;

    private NodeRef[][] contents = new NodeRef[10][100];
    private Long[][] nodeIDs = new Long[10][100];
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        ctx = getServer().getApplicationContext();

        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        transactionService = serviceRegistry.getTransactionService();
        nodeService = serviceRegistry.getNodeService();
        contentService = serviceRegistry.getContentService();
        fileFolderService = serviceRegistry.getFileFolderService();
        txnHelper = transactionService.getRetryingTransactionHelper();
        nodeDAO = (NodeDAO)ctx.getBean("nodeDAO");

        admin = AuthenticationUtil.getAdminUserName();

        AuthenticationUtil.setFullyAuthenticatedUser(admin);
        
        storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, getName() + ".1." + System.currentTimeMillis());
        rootNodeRef = nodeService.getRootNode(storeRef);
        
        storeRef1 = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, getName() + ".2." + System.currentTimeMillis());
        rootNodeRef1 = nodeService.getRootNode(storeRef1);
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    private JSONArray getNodes(GetNodesParameters parameters, Integer maxResults) throws Exception
    {
    	StringBuilder url = new StringBuilder("/api/solr/nodes?txnIds=");
    	url.append(join(parameters.getTransactionIds(), ","));
    	if(parameters.getFromNodeId() != null)
    	{
    		url.append("&fromNodeId=");
    		url.append(parameters.getFromNodeId());
    	}
    	if(parameters.getToNodeId() != null)
    	{
    		url.append("&toNodeId=");
    		url.append(parameters.getToNodeId());
    	}
        if(parameters.getExcludeAspects() != null)
        {
            url.append("&excludeAspects=");
            Set<QName> excludeAspects = parameters.getExcludeAspects();
            int i = 0;
            for(QName excludeAspect : excludeAspects)
            {
                url.append(excludeAspect.toString());
                if(i < (excludeAspects.size() - 1))
                {
                    url.append(",");
                }
                i++;
            }
        }
        if(parameters.getIncludeAspects() != null)
        {
            url.append("&includeAspects=");
            Set<QName> includeAspects = parameters.getIncludeAspects();
            int i = 0;
            for(QName includeAspect : includeAspects)
            {
                url.append(includeAspect.toString());
                if(i < (includeAspects.size() - 1))
                {
                    url.append(",");
                }
                i++;
            }
        }
        if(parameters.getStoreProtocol() != null)
        {
            url.append("&storeProtocol=");
            url.append(parameters.getStoreProtocol());
        }
        if(parameters.getStoreIdentifier() != null)
        {
            url.append("&storeIdentifier=");
            url.append(parameters.getStoreIdentifier());
        }
    	if(maxResults != null)
    	{
    		url.append("&maxResults=");
    		url.append(maxResults);
    	}

    	TestWebScriptServer.GetRequest req = new TestWebScriptServer.GetRequest(url.toString());
    	Response response = sendRequest(req, Status.STATUS_OK, admin);
        
//      assertEquals("Expected application/json content type", "application/json[;charset=UTF-8]", response.getContentType());
        
    	if(logger.isDebugEnabled())
    	{
    		logger.debug(response.getContentAsString());
    	}
    	
        JSONObject json = new JSONObject(response.getContentAsString());
        json.write(new PrintWriter(System.out));

        JSONArray nodes = json.getJSONArray("nodes");

        assertEquals("Node count is incorrect", nodes.length(), json.getInt("count"));

        return nodes;
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
            	
                container1NodeID = getNodeID(container1);
                content1NodeID = getNodeID(content1);

                if(logger.isDebugEnabled())
                {
                	logger.debug("content1 = " + content1);
                }
            	
            	return null;
            }
        });

        txnHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
            	FileInfo content2Info = fileFolderService.create(container1, "Content2", ContentModel.TYPE_CONTENT);
            	content2 = content2Info.getNodeRef();
                content2NodeID = getNodeID(content2);
                
                if(logger.isDebugEnabled())
                {
                	logger.debug("content2 = " + content2);
                }
            	
                nodeService.addAspect(content1, ContentModel.ASPECT_TEMPORARY, null);
            	fileFolderService.delete(content1);

            	return null;
            }
        });
    }
    
    private List<Long> getTransactionIds(JSONArray transactions) throws JSONException
    {
        List<Long> txnIds = new ArrayList<Long>(transactions.length());

        int numTxns = transactions.length();
        for(int i = 0; i < numTxns; i++)
        {
            JSONObject txn = transactions.getJSONObject(i);
            txnIds.add(txn.getLong("id"));
        }

        return txnIds;
    }

    public static String join(Collection s, String delimiter) {
        StringBuffer buffer = new StringBuffer();
        Iterator iter = s.iterator();
        while (iter.hasNext()) {
            buffer.append(iter.next());
            if (iter.hasNext()) {
                buffer.append(delimiter);
            }
        }
        return buffer.toString();
    }
    
    public void testGetTransactions1() throws Exception
    {
    	long fromCommitTime = System.currentTimeMillis();

    	buildTransactions1();

        String url = "/api/solr/transactions?fromCommitTime=" + fromCommitTime;
        TestWebScriptServer.GetRequest req = new TestWebScriptServer.GetRequest(url);
        Response response = sendRequest(req, Status.STATUS_OK, admin);

//        assertEquals("Expected application/json content type", "application/json[;charset=UTF-8]", response.getContentType());
        
        System.out.println(response.getContentAsString());
        JSONObject json = new JSONObject(response.getContentAsString());

        JSONArray transactions = json.getJSONArray("transactions");
    	assertEquals("Number of transactions is incorrect", 2, transactions.length());

        int[] updates = new int[] {1, 1};
    	int[] deletes = new int[] {0, 1};
        StringBuilder txnIds = new StringBuilder();
    	int numTxns = transactions.length();
    	List<Long> transactionIds = getTransactionIds(transactions);
    	for(int i = 0; i < numTxns; i++)
    	{
    		JSONObject txn = transactions.getJSONObject(i);
    		assertEquals("Number of deletes is incorrect", deletes[i], txn.getLong("deletes"));
    		assertEquals("Number of updates is incorrect", updates[i], txn.getLong("updates"));

//    		txnIds.append(txn.getString("id"));
//    		if(i < (numTxns - 1))
//    		{
//    			txnIds.append(",");
//    		}
    	}
    	
    	// get all nodes at once
    	if(logger.isDebugEnabled())
    	{
    		logger.debug("txnIds = " + txnIds.toString());
    	}
    	
    	GetNodesParameters parameters = new GetNodesParameters();
    	parameters.setTransactionIds(transactionIds);
    	JSONArray nodes = getNodes(parameters, null);
        if(logger.isDebugEnabled())
        {
        	logger.debug("nodes:");
        	logger.debug(nodes.toString(3));
        }
    	assertEquals("Number of nodes is incorrect", 3, nodes.length());

    	JSONObject lastNode = nodes.getJSONObject(2);
    	assertTrue("nodeID is missing", lastNode.has("nodeID"));
    	Long fromNodeId = lastNode.getLong("nodeID");
        if(logger.isDebugEnabled())
        {
        	logger.debug("fromNodeId = " + fromNodeId);
        }
    	assertNotNull("Unexpected null fromNodeId", fromNodeId);

        firstNode = nodes.getJSONObject(0);
        secondNode = nodes.getJSONObject(1);
        //assertEquals("Expected transaction ids to be the same", firstNode.getLong("txnID") == secondNode.getLong("txnID"));
        assertEquals("Expected node update", false, firstNode.getBoolean("deleted"));
        assertEquals("Expected node deleted", true, secondNode.getBoolean("deleted"));
        assertEquals("Node id is incorrect", container1NodeID, firstNode.getLong("nodeID"));
        assertEquals("Node id is incorrect", content1NodeID, secondNode.getLong("nodeID"));

    	// get first 2 nodes
        parameters = new GetNodesParameters();
        parameters.setTransactionIds(transactionIds);
    	nodes = getNodes(parameters, 2);
        if(logger.isDebugEnabled())
        {
        	logger.debug("nodes:");
        	logger.debug(nodes.toString(3));
        }
    	assertEquals("Number of nodes is incorrect", 2, nodes.length());

    	lastNode = nodes.getJSONObject(1);
    	assertTrue("nodeID is missing", lastNode.has("nodeID"));
    	fromNodeId = lastNode.getLong("nodeID");
        if(logger.isDebugEnabled())
        {
        	logger.debug("fromNodeId = " + fromNodeId);
        }
    	assertNotNull("Unexpected null fromNodeId", fromNodeId);

    	// get 4 nodes starting with fromNodeId, should return only 2 nodes (including fromNodeId)
        parameters = new GetNodesParameters();
        parameters.setTransactionIds(transactionIds);
        parameters.setFromNodeId(fromNodeId);
    	nodes = getNodes(parameters, 4);
        if(logger.isDebugEnabled())
        {
        	logger.debug("nodes:");
        	logger.debug(nodes.toString(3));
        }
    	assertEquals("Number of nodes is incorrect", 2, nodes.length());

        firstNode = nodes.getJSONObject(0);
        secondNode = nodes.getJSONObject(1);
        assertEquals("Expected node deleted", true, firstNode.getBoolean("deleted"));
        assertEquals("Expected node updated", false, secondNode.getBoolean("deleted"));
        assertEquals("Node id is incorrect", content1NodeID, firstNode.getLong("nodeID"));
        assertEquals("Node id is incorrect", content2NodeID, secondNode.getLong("nodeID"));
            	
    	// get 0 (all) nodes starting with fromNodeId, should return 2 nodes
        parameters = new GetNodesParameters();
        parameters.setTransactionIds(transactionIds);
        parameters.setFromNodeId(fromNodeId);
        nodes = getNodes(parameters, 0);
        if(logger.isDebugEnabled())
        {
        	logger.debug("nodes:");
        	logger.debug(nodes.toString(3));
        }
    	assertEquals("Number of nodes is incorrect", 2, nodes.length());

    	// get 2 nodes ending with toNodeId, should return 2 nodes
    	long toNodeId = content2NodeID;
        parameters = new GetNodesParameters();
        parameters.setTransactionIds(transactionIds);
        parameters.setToNodeId(toNodeId);
    	nodes = getNodes(parameters, 2);
        if(logger.isDebugEnabled())
        {
        	logger.debug("nodes:");
        	logger.debug(nodes.toString(3));
        }
    	assertEquals("Number of nodes is incorrect", 2, nodes.length());

        firstNode = nodes.getJSONObject(0);
        secondNode = nodes.getJSONObject(1);
        assertEquals("Expected node deleted", false, firstNode.getBoolean("deleted"));
        assertEquals("Node id is incorrect", container1NodeID, firstNode.getLong("nodeID"));
        assertEquals("Expected node updated", true, secondNode.getBoolean("deleted"));
        assertEquals("Node id is incorrect", content1NodeID, secondNode.getLong("nodeID"));

    	// get 1 node ending with toNodeId, should return 1 nodes
    	toNodeId = content2NodeID;
        parameters = new GetNodesParameters();
        parameters.setTransactionIds(transactionIds);
        parameters.setToNodeId(toNodeId);
    	nodes = getNodes(parameters, 1);
        if(logger.isDebugEnabled())
        {
        	logger.debug("nodes:");
        	logger.debug(nodes.toString(3));
        }
    	assertEquals("Number of nodes is incorrect", 1, nodes.length());

        firstNode = nodes.getJSONObject(0);
        assertEquals("Expected node updated", false, firstNode.getBoolean("deleted"));
        assertEquals("Node id is incorrect", container1NodeID, firstNode.getLong("nodeID"));

    	// get 3 nodes ending with toNodeId, should return 3 nodes
    	toNodeId = content2NodeID;
        parameters = new GetNodesParameters();
        parameters.setTransactionIds(transactionIds);
        parameters.setToNodeId(toNodeId);
    	nodes = getNodes(parameters, 3);
        if(logger.isDebugEnabled())
        {
        	logger.debug("nodes:");
        	logger.debug(nodes.toString(3));
        }
    	assertEquals("Number of nodes is incorrect", 3, nodes.length());

        firstNode = nodes.getJSONObject(0);
        assertEquals("Expected node updated", false, firstNode.getBoolean("deleted"));
        assertEquals("Node id is incorrect", container1NodeID, firstNode.getLong("nodeID"));

        secondNode = nodes.getJSONObject(1);
        assertEquals("Expected node deleted", true, secondNode.getBoolean("deleted"));
        assertEquals("Node id is incorrect", content1NodeID, secondNode.getLong("nodeID"));

        thirdNode = nodes.getJSONObject(2);
        assertEquals("Expected node updated", false, thirdNode.getBoolean("deleted"));
        assertEquals("Node id is incorrect", content2NodeID, thirdNode.getLong("nodeID"));
    }
    
    private void buildTransactions2()
    {
        txnHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                PropertyMap props = new PropertyMap();
                props.put(ContentModel.PROP_NAME, "Container2");
                container2 = nodeService.createNode(
                		rootNodeRef,
                        ContentModel.ASSOC_CHILDREN,
                        ContentModel.ASSOC_CHILDREN,
                        ContentModel.TYPE_FOLDER,
                        props).getChildRef();
                container2NodeID = getNodeID(container2);
                if(logger.isDebugEnabled())
                {
                	logger.debug("container2 = " + container2);
                }

            	FileInfo content1Info = fileFolderService.create(container2, "Content1", ContentModel.TYPE_CONTENT);
            	content1 = content1Info.getNodeRef();
                content1NodeID = getNodeID(content1);
            	
            	if(logger.isDebugEnabled())
                {
                	logger.debug("content1 = " + content1);
                }

            	FileInfo content2Info = fileFolderService.create(container2, "Content2", ContentModel.TYPE_CONTENT);
            	content2 = content2Info.getNodeRef();
                content2NodeID = getNodeID(content2);
            	if(logger.isDebugEnabled())
                {
                	logger.debug("content2 = " + content2);
                }
                
            	return null;
            }
        });

        txnHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
            	FileInfo content3Info = fileFolderService.create(container2, "Content3", ContentModel.TYPE_CONTENT);
            	content3 = content3Info.getNodeRef();
            	content3NodeID = getNodeID(content3);
                if(logger.isDebugEnabled())
                {
                	logger.debug("content3 = " + content3);
                }

                nodeService.addAspect(content1, ContentModel.ASPECT_TEMPORARY, null);
            	fileFolderService.delete(content1);
            	
            	nodeService.setProperty(content3, ContentModel.PROP_NAME, "Content 3 New Name");

            	return null;
            }
        });
    }

    public void testGetTransactions2() throws Exception
    {
    	long fromCommitTime = System.currentTimeMillis();

    	buildTransactions2();

        String url = "/api/solr/transactions?fromCommitTime=" + fromCommitTime;
        TestWebScriptServer.GetRequest req = new TestWebScriptServer.GetRequest(url);
        Response response = sendRequest(req, Status.STATUS_OK, admin);
        
        if(logger.isDebugEnabled())
        {
        	logger.debug(response.getContentAsString());
        }
        JSONObject json = new JSONObject(response.getContentAsString());

        JSONArray transactions = json.getJSONArray("transactions");
    	assertEquals("Number of transactions is incorrect", 2, transactions.length());

    	// first txn has 2 updates rather than three because content1 is deleted in txn 2 and therefore
    	// "belongs" to that txn (because txn2 was the last to alter the node)
        int[] updates = new int[] {2, 1};
    	int[] deletes = new int[] {0, 1};
    	StringBuilder txnIds = new StringBuilder();
    	int numTxns = transactions.length();
    	for(int i = 0; i < numTxns; i++)
    	{
    		JSONObject txn = transactions.getJSONObject(i);
    		assertEquals("Number of deletes is incorrect", deletes[i], txn.getLong("deletes"));
    		assertEquals("Number of updates is incorrect", updates[i], txn.getLong("updates"));

    		txnIds.append(txn.getString("id"));
    		if(i < (numTxns - 1))
    		{
    			txnIds.append(",");
    		}
    	}
    	
    	// get all nodes at once
        if(logger.isDebugEnabled())
        {
        	logger.debug("txnIds = " + txnIds.toString());
        }

        List<Long> transactionIds = getTransactionIds(transactions);
        
        // get all nodes in the txns
        GetNodesParameters parameters = new GetNodesParameters();
        parameters.setTransactionIds(transactionIds);
        JSONArray nodes = getNodes(parameters, null);
    	assertEquals("Number of nodes is incorrect", 4, nodes.length());
    	JSONObject lastNode = nodes.getJSONObject(nodes.length() - 1);
    	Long fromNodeId = lastNode.getLong("nodeID");
    	assertNotNull("Unexpected null fromNodeId", fromNodeId);

    	// get first 2 nodes
        parameters = new GetNodesParameters();
        parameters.setTransactionIds(transactionIds);
    	nodes = getNodes(parameters, 2);
        if(logger.isDebugEnabled())
        {
        	logger.debug("nodes:");
        	logger.debug(nodes.toString(3));
        }
    	assertEquals("Number of nodes is incorrect", 2, nodes.length());

        firstNode = nodes.getJSONObject(0);
    	assertTrue("nodeID is missing", firstNode.has("nodeID"));
        secondNode = nodes.getJSONObject(1);
    	assertTrue("nodeID is missing", secondNode.has("nodeID"));
    	fromNodeId = secondNode.getLong("nodeID");
        if(logger.isDebugEnabled())
        {
        	logger.debug("fromNodeId = " + fromNodeId);
        }
    	assertNotNull("Unexpected null nodeID", fromNodeId);

        //assertEquals("Expected transaction ids to be the same", firstNode.getLong("txnID"), secondNode.getLong("txnID"));
        assertEquals("Expected node update", false, firstNode.getBoolean("deleted"));
        assertEquals("Expected node delete", true, secondNode.getBoolean("deleted"));
        assertEquals("Incorrect node id", container2NodeID, firstNode.getLong("nodeID"));
        assertEquals("Incorrect node id", content1NodeID, secondNode.getLong("nodeID"));
        
    	// get 10 nodes (including fromNodeId) starting with fromNodeId, should return only 3 nodes
        parameters = new GetNodesParameters();
        parameters.setTransactionIds(transactionIds);
        parameters.setFromNodeId(fromNodeId);
    	nodes = getNodes(parameters, 10);
    	assertEquals("Number of nodes is incorrect", 3, nodes.length());

        firstNode = nodes.getJSONObject(0);
    	assertTrue("nodeID is missing", firstNode.has("nodeID"));
        secondNode = nodes.getJSONObject(1);
    	assertTrue("nodeID is missing", secondNode.has("nodeID"));
        thirdNode = nodes.getJSONObject(2);
    	assertTrue("nodeID is missing", thirdNode.has("nodeID"));

        assertEquals("Expected node delete", true, firstNode.getBoolean("deleted"));
        assertEquals("Expected node update", false, secondNode.getBoolean("deleted"));
        assertEquals("Expected node update", false, thirdNode.getBoolean("deleted"));
        assertEquals("Incorrect node id", content1NodeID, firstNode.getLong("nodeID"));
        assertEquals("Incorrect node id", content2NodeID, secondNode.getLong("nodeID"));
        assertEquals("Incorrect node id", content3NodeID, thirdNode.getLong("nodeID"));

        // test with from and to node ids
        parameters = new GetNodesParameters();
        parameters.setTransactionIds(transactionIds);
        parameters.setFromNodeId(container2NodeID);
        parameters.setToNodeId(content3NodeID);
        nodes = getNodes(parameters, 2);
        assertEquals("Number of nodes is incorrect", 2, nodes.length());

        firstNode = nodes.getJSONObject(0);
        assertTrue("nodeID is missing", firstNode.has("nodeID"));
        secondNode = nodes.getJSONObject(1);
        assertTrue("nodeID is missing", secondNode.has("nodeID"));
        assertEquals("Incorrect node id", container2NodeID, firstNode.getLong("nodeID"));
        assertEquals("Incorrect node id", content1NodeID, secondNode.getLong("nodeID"));

        // test right truncation
        parameters = new GetNodesParameters();
        parameters.setTransactionIds(transactionIds);
        parameters.setToNodeId(content3NodeID);
        nodes = getNodes(parameters, 2);
        assertEquals("Number of nodes is incorrect", 2, nodes.length());

        firstNode = nodes.getJSONObject(0);
        assertTrue("nodeID is missing", firstNode.has("nodeID"));
        secondNode = nodes.getJSONObject(1);
        assertTrue("nodeID is missing", secondNode.has("nodeID"));
        assertEquals("Incorrect node id", container2NodeID, firstNode.getLong("nodeID"));
        assertEquals("Incorrect node id", content1NodeID, secondNode.getLong("nodeID"));
        
        // test left truncation, specifying from node only
        parameters = new GetNodesParameters();
        parameters.setTransactionIds(transactionIds);
        parameters.setFromNodeId(container2NodeID);
        nodes = getNodes(parameters, 2);
        assertEquals("Number of nodes is incorrect", 2, nodes.length());

        firstNode = nodes.getJSONObject(0);
        assertTrue("nodeID is missing", firstNode.has("nodeID"));
        secondNode = nodes.getJSONObject(1);
        assertTrue("nodeID is missing", secondNode.has("nodeID"));
        assertEquals("Incorrect node id", container2NodeID, firstNode.getLong("nodeID"));
        assertEquals("Incorrect node id", content1NodeID, secondNode.getLong("nodeID"));
    }
    
    private void buildTransactions3()
    {
        txnHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                PropertyMap props = new PropertyMap();
                props.put(ContentModel.PROP_NAME, "Container3");
                container3 = nodeService.createNode(
                        rootNodeRef,
                        ContentModel.ASSOC_CHILDREN,
                        ContentModel.ASSOC_CHILDREN,
                        ContentModel.TYPE_FOLDER,
                        props).getChildRef();
                container3NodeID = getNodeID(container3);
                if(logger.isDebugEnabled())
                {
                    logger.debug("container3 = " + container3);
                }
                
                for(int i = 0; i < 100; i++)
                {
                    FileInfo content1Info = fileFolderService.create(container3, "Content" + i, ContentModel.TYPE_CONTENT);
                    contents[3][i] = content1Info.getNodeRef();
                    nodeIDs[3][i] = getNodeID(contents[3][i]);
                    
                    if(i % 2 == 1)
                    {
                        nodeService.addAspect(contents[3][i], ContentModel.ASPECT_TEMPORARY, null);
                    }
                }
                
                return null;
            }
        });
    }
    
    public void testGetNodesExcludeAspects() throws Exception
    {
        long fromCommitTime = System.currentTimeMillis();

        buildTransactions3();
        
        String url = "/api/solr/transactions?fromCommitTime=" + fromCommitTime;
        TestWebScriptServer.GetRequest req = new TestWebScriptServer.GetRequest(url);
        Response response = sendRequest(req, Status.STATUS_OK, admin);
        
        if(logger.isDebugEnabled())
        {
            logger.debug(response.getContentAsString());
        }
        JSONObject json = new JSONObject(response.getContentAsString());

        JSONArray transactions = json.getJSONArray("transactions");
        assertEquals("Number of transactions is incorrect", 1, transactions.length());

        // first txn has 2 updates rather than three because content1 is deleted in txn 2 and therefore
        // "belongs" to that txn (because txn2 was the last to alter the node)
        
        List<Long> transactionIds = getTransactionIds(transactions);

        Set<QName> excludeAspects = new HashSet<QName>(1);
        excludeAspects.add(ContentModel.ASPECT_TEMPORARY);
        
        // get all nodes, exclude nodes with temporary aspect
        GetNodesParameters parameters = new GetNodesParameters();
        parameters.setTransactionIds(transactionIds);
        parameters.setExcludeAspects(excludeAspects);
        JSONArray nodes = getNodes(parameters, 0);
        assertEquals("Number of nodes is incorrect", 51, nodes.length());
    }

    private void buildTransactions4()
    {
        txnHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                PropertyMap props = new PropertyMap();
                props.put(ContentModel.PROP_NAME, "Container4");
                container4 = nodeService.createNode(
                        rootNodeRef,
                        ContentModel.ASSOC_CHILDREN,
                        ContentModel.ASSOC_CHILDREN,
                        ContentModel.TYPE_FOLDER,
                        props).getChildRef();
                container4NodeID = getNodeID(container4);
                if(logger.isDebugEnabled())
                {
                    logger.debug("container4 = " + container4);
                }
                                
                for(int i = 0; i < 100; i++)
                {
                    FileInfo content1Info = fileFolderService.create(container4, "Content" + i, ContentModel.TYPE_CONTENT);
                    contents[4][i] = content1Info.getNodeRef();
                    nodeIDs[4][i] = getNodeID(contents[4][i]);
                    
                    if(i % 2 == 1)
                    {
                        nodeService.addAspect(contents[4][i], ContentModel.ASPECT_TEMPORARY, null);
                    }
                }
                
                return null;
            }
        });
        
        txnHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                PropertyMap props = new PropertyMap();
                props.put(ContentModel.PROP_NAME, "Container5");
                container5 = nodeService.createNode(
                        rootNodeRef1,
                        ContentModel.ASSOC_CHILDREN,
                        ContentModel.ASSOC_CHILDREN,
                        ContentModel.TYPE_FOLDER,
                        props).getChildRef();
                container5NodeID = getNodeID(container5);
                if(logger.isDebugEnabled())
                {
                    logger.debug("container5 = " + container5);
                }

                for(int i = 0; i < 100; i++)
                {
                    FileInfo content1Info = fileFolderService.create(container5, "Content" + i, ContentModel.TYPE_CONTENT);
                    contents[5][i] = content1Info.getNodeRef();
                    nodeIDs[5][i] = getNodeID(contents[5][i]);

                    if(i % 2 == 1)
                    {
                        nodeService.addAspect(contents[5][i], ContentModel.ASPECT_TEMPORARY, null);
                    }
                }

                return null;
            }
        });
    }
    
    public void testGetNodesStoreName() throws Exception
    {
        long fromCommitTime = System.currentTimeMillis();

        buildTransactions4();
        
        String url = "/api/solr/transactions?fromCommitTime=" + fromCommitTime;
        TestWebScriptServer.GetRequest req = new TestWebScriptServer.GetRequest(url);
        Response response = sendRequest(req, Status.STATUS_OK, admin);
        
        if(logger.isDebugEnabled())
        {
            logger.debug(response.getContentAsString());
        }
        JSONObject json = new JSONObject(response.getContentAsString());

        JSONArray transactions = json.getJSONArray("transactions");
        assertEquals("Number of transactions is incorrect", 2, transactions.length());

        // first txn has 2 updates rather than three because content1 is deleted in txn 2 and therefore
        // "belongs" to that txn (because txn2 was the last to alter the node)
        
        List<Long> transactionIds = getTransactionIds(transactions);

        // exact store name
        GetNodesParameters parameters = new GetNodesParameters();
        parameters.setTransactionIds(transactionIds);
        parameters.setStoreProtocol(storeRef.getProtocol());
        parameters.setStoreIdentifier(storeRef.getIdentifier());
        JSONArray nodes = getNodes(parameters, 0);
        assertEquals("Number of nodes is incorrect", 101, nodes.length());
        
        nodes = getNodes(parameters, 50);
        assertEquals("Number of nodes is incorrect", 50, nodes.length());
        
        // store protocol
        parameters = new GetNodesParameters();
        parameters.setTransactionIds(transactionIds);
        parameters.setStoreProtocol(storeRef.getProtocol());
        nodes = getNodes(parameters, 0);
        assertEquals("Number of nodes is incorrect", 202, nodes.length());

        // store identifier
        parameters = new GetNodesParameters();
        parameters.setTransactionIds(transactionIds);
        parameters.setStoreIdentifier(storeRef.getIdentifier());
        nodes = getNodes(parameters, 0);
        assertEquals("Number of nodes is incorrect", 101, nodes.length());
    }
    
/*    private void buildTransactions3()
    {
        txnHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                PropertyMap props = new PropertyMap();
                props.put(ContentModel.PROP_NAME, "Container1");
                container3 = nodeService.createNode(
                        rootNodeRef,
                        ContentModel.ASSOC_CHILDREN,
                        ContentModel.ASSOC_CHILDREN,
                        ContentModel.TYPE_FOLDER,
                        props).getChildRef();
                
                System.out.println("container1 = " + container1);

                FileInfo content1Info = fileFolderService.create(container3, "Content1", ContentModel.TYPE_CONTENT);
                content1 = content1Info.getNodeRef();
                
                container3NodeID = getNodeID(container3);
                content1NodeID = getNodeID(content1);
                
                ContentWriter writer = contentService.getWriter(content1Info.getNodeRef(), ContentModel.PROP_CONTENT, true);
                writer.putContent("test content");

                if(logger.isDebugEnabled())
                {
                    logger.debug("content1 = " + content1);
                }
                
                return null;
            }
        });
    }
    
    public void testGetContent() throws Exception
    {
        long nodeId = -1l;
        String propertyName = ContentModel.PROP_CONTENT.toString();

        buildTransactions3();
        
        String url = "/api/solr/content?nodeId=" + nodeId + "&propertyName=" + propertyName;
        TestWebScriptServer.GetRequest req = new TestWebScriptServer.GetRequest(url);
        Response response = sendRequest(req, Status.STATUS_OK, admin);
        if(logger.isDebugEnabled())
        {
            logger.debug("content1 = " + response.getContentAsString());
        }

        assertEquals("Content length is incorrect", "test content".length(), response.getContentLength());

    }*/
    
    private long getNodeID(NodeRef nodeRef)
    {
        Pair<Long, NodeRef> pair = nodeDAO.getNodePair(nodeRef);
        assertNotNull("Can't find node " + nodeRef, pair);
        return pair.getFirst();
    }
    
    private static class GetNodesParameters
    {
        private List<Long> transactionIds;
        private Long fromNodeId;
        private Long toNodeId;
        
        private String storeProtocol;
        private String storeIdentifier;
        
        private Set<QName> includeNodeTypes;
        private Set<QName> excludeNodeTypes;
        
        private Set<QName> includeAspects;
        private Set<QName> excludeAspects;
        
        public boolean getStoreFilter()
        {
            return (storeProtocol != null || storeIdentifier != null);
        }
        
        public void setStoreProtocol(String storeProtocol)
        {
            this.storeProtocol = storeProtocol;
        }

        public String getStoreProtocol()
        {
            return storeProtocol;
        }

        public void setStoreIdentifier(String storeIdentifier)
        {
            this.storeIdentifier = storeIdentifier;
        }

        public String getStoreIdentifier()
        {
            return storeIdentifier;
        }
        
        public void setTransactionIds(List<Long> txnIds)
        {
            this.transactionIds = txnIds;
        }

        public List<Long> getTransactionIds()
        {
            return transactionIds;
        }

        public Long getFromNodeId()
        {
            return fromNodeId;
        }

        public void setFromNodeId(Long fromNodeId)
        {
            this.fromNodeId = fromNodeId;
        }

        public Long getToNodeId()
        {
            return toNodeId;
        }

        public void setToNodeId(Long toNodeId)
        {
            this.toNodeId = toNodeId;
        }

        public Set<QName> getIncludeNodeTypes()
        {
            return includeNodeTypes;
        }

        public Set<QName> getExcludeNodeTypes()
        {
            return excludeNodeTypes;
        }

        public Set<QName> getIncludeAspects()
        {
            return includeAspects;
        }

        public Set<QName> getExcludeAspects()
        {
            return excludeAspects;
        }

        public void setIncludeNodeTypes(Set<QName> includeNodeTypes)
        {
            this.includeNodeTypes = includeNodeTypes;
        }

        public void setExcludeNodeTypes(Set<QName> excludeNodeTypes)
        {
            this.excludeNodeTypes = excludeNodeTypes;
        }

        public void setIncludeAspects(Set<QName> includeAspects)
        {
            this.includeAspects = includeAspects;
        }

        public void setExcludeAspects(Set<QName> excludeAspects)
        {
            this.excludeAspects = excludeAspects;
        }

    }
}