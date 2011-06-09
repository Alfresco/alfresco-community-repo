/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.solr;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
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
    private NamespaceService namespaceService;

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

    private ArrayList<NodeRef> contents = new ArrayList<NodeRef>(100);
    private List<Long> nodeIDs = new ArrayList<Long>(100);
    
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
        namespaceService = serviceRegistry.getNamespaceService();
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

    private JSONArray getTransactions(long fromCommitTime) throws Exception
    {
        String url = "/api/solr/transactions?fromCommitTime=" + fromCommitTime;
        TestWebScriptServer.GetRequest req = new TestWebScriptServer.GetRequest(url);
        long startTime = System.currentTimeMillis();
        Response response = sendRequest(req, Status.STATUS_OK, admin);
        long endTime = System.currentTimeMillis();

        if(logger.isDebugEnabled())
        {
            logger.debug(response.getContentAsString());
        }
        JSONObject json = new JSONObject(response.getContentAsString());

        JSONArray transactions = json.getJSONArray("transactions");
        
        System.out.println("Got " + transactions.length() + " txns in " + (endTime - startTime) + " ms");
        
        return transactions;
    }

    public void testAclChangeSetsGet() throws Exception
    {
        String url = "/api/solr/aclchangesets?fromTime=" + 0L + "&fromId=" + 0L;
        TestWebScriptServer.GetRequest req = new TestWebScriptServer.GetRequest(url);
        long startTime = System.currentTimeMillis();
        Response response = sendRequest(req, Status.STATUS_OK, admin);
        long endTime = System.currentTimeMillis();

        if(logger.isDebugEnabled())
        {
            logger.debug(response.getContentAsString());
        }
        JSONObject json = new JSONObject(response.getContentAsString());

        JSONArray aclChangeSets = json.getJSONArray("aclChangeSets");
        
        System.out.println("Got " + aclChangeSets.length() + " txns in " + (endTime - startTime) + " ms");
    }

    private JSONArray getNodes(GetNodesParameters parameters, int maxResults, int expectedNumNodes) throws Exception
    {
    	StringBuilder url = new StringBuilder("/api/solr/nodes");
        
        JSONObject json = new JSONObject();
        if(parameters.getTransactionIds() != null)
        {
            JSONArray array = new JSONArray();
            for(Long txnId : parameters.getTransactionIds())
            {
                array.put(txnId);
            }
            json.put("txnIds", array);
        }
    	
    	if(parameters.getFromNodeId() != null)
    	{
    	    json.put("fromNodeId", parameters.getFromNodeId());
    	}

    	if(parameters.getToNodeId() != null)
    	{
            json.put("toNodeId", parameters.getToNodeId());
    	}
    	
        if(parameters.getExcludeAspects() != null)
        {
            JSONArray array = new JSONArray();
            for(QName excludeAspect : parameters.getExcludeAspects())
            {
                array.put(excludeAspect.toString());
            }
            json.put("excludeAspects", array);
        }
        
        if(parameters.getIncludeAspects() != null)
        {
            JSONArray array = new JSONArray();
            for(QName includeAspect : parameters.getIncludeAspects())
            {
                array.put(includeAspect.toString());
            }
            json.put("includeAspects", array);
        }

        if(parameters.getStoreProtocol() != null)
        {
            json.put("storeProtocol", parameters.getStoreProtocol());
        }

        if(parameters.getStoreIdentifier() != null)
        {
            json.put("storeIdentifier", parameters.getStoreIdentifier());
        }
            
        json.put("maxResults", maxResults);

        TestWebScriptServer.PostRequest req = new TestWebScriptServer.PostRequest(url.toString(), json.toString(), "application/json");
 
        long startTime = System.currentTimeMillis();
    	Response response = sendRequest(req, Status.STATUS_OK, admin);
        long endTime = System.currentTimeMillis();
        
//      assertEquals("Expected application/json content type", "application/json[;charset=UTF-8]", response.getContentType());
        
    	if(logger.isDebugEnabled())
    	{
    		logger.debug(response.getContentAsString());
    	}
    	//System.out.println("getNodes: " + response.getContentAsString());
        JSONObject jsonResponse = new JSONObject(response.getContentAsString());
        jsonResponse.write(new PrintWriter(System.out));

        JSONArray nodes = jsonResponse.getJSONArray("nodes");

        //assertEquals("Node count is incorrect", nodes.length(), json.getInt("count"));

        System.out.println("Got " + nodes.length() + " nodes in " + (endTime - startTime) + " ms");

        assertEquals("Number of returned node meta data results is incorrect", expectedNumNodes, nodes.length());

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
                    NodeRef nodeRef = content1Info.getNodeRef();
                    contents.add(nodeRef);
                    nodeIDs.add(Long.valueOf(getNodeID(nodeRef)));
                    
                    if(i % 2 == 1)
                    {
                        nodeService.addAspect(nodeRef, ContentModel.ASPECT_TEMPORARY, null);
                    }
                }
                
                return null;
            }
        });
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
                    NodeRef nodeRef = content1Info.getNodeRef();
                    contents.add(nodeRef);
                    nodeIDs.add(getNodeID(nodeRef));
                    
                    if(i % 2 == 1)
                    {
                        nodeService.addAspect(nodeRef, ContentModel.ASPECT_TEMPORARY, null);
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
                    NodeRef nodeRef = content1Info.getNodeRef();
                    contents.add(nodeRef);
                    nodeIDs.add(getNodeID(nodeRef));

                    if(i % 2 == 1)
                    {
                        nodeService.addAspect(nodeRef, ContentModel.ASPECT_TEMPORARY, null);
                    }
                }

                return null;
            }
        });
    }
    
    private NodeRef container6;
    
    private void buildTransactions5()
    {
        txnHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                PropertyMap props = new PropertyMap();
                props.put(ContentModel.PROP_NAME, "Container6");
                container6 = nodeService.createNode(
                        rootNodeRef,
                        ContentModel.ASSOC_CHILDREN,
                        ContentModel.ASSOC_CHILDREN,
                        ContentModel.TYPE_FOLDER,
                        props).getChildRef();

                FileInfo contentInfo = fileFolderService.create(container6, "Content1", ContentModel.TYPE_CONTENT);
                contents.add(contentInfo.getNodeRef());

                Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>();
                aspectProperties.put(ContentModel.PROP_AUTHOR, "steve");
                nodeService.addAspect(contentInfo.getNodeRef(), ContentModel.ASPECT_AUTHOR, aspectProperties);

                return null;
            }
        });
    }

    private JSONArray getNodesMetaData(List<Long> nodeIds, int maxResults, int numMetaDataNodes) throws Exception
    {
        StringBuilder url = new StringBuilder("/api/solr/metadata");

        JSONObject json = new JSONObject();
        if(nodeIds != null && nodeIds.size() > 0)
        {
            JSONArray array = new JSONArray();
            for(Long nodeId : nodeIds)
            {
                array.put(nodeId);
            }
            json.put("nodeIds", array);
        }

        json.put("maxResults", maxResults);

        TestWebScriptServer.PostRequest req = new TestWebScriptServer.PostRequest(url.toString(), json.toString(), "application/json");
        long startTime = System.currentTimeMillis();
        Response response = sendRequest(req, Status.STATUS_OK, admin);
        long endTime = System.currentTimeMillis();

        String content = response.getContentAsString();
        
        if(logger.isDebugEnabled())
        {
            logger.debug("nodesMetaData = " + content);
        }

        JSONObject jsonResponse = new JSONObject(content);

        JSONArray nodes = jsonResponse.getJSONArray("nodes");

        System.out.println("Got metadata for " + nodes.length() + " nodes in " + (endTime - startTime) + " ms");
        
        assertEquals("Number of returned nodes is incorrect", numMetaDataNodes, nodes.length());
        
        return nodes;
    }
    
    private void buildTransactions6()
    {
        txnHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                PropertyMap props = new PropertyMap();
                props.put(ContentModel.PROP_NAME, "Container6");
                NodeRef container6 = nodeService.createNode(
                        rootNodeRef,
                        ContentModel.ASSOC_CHILDREN,
                        ContentModel.ASSOC_CHILDREN,
                        ContentModel.TYPE_FOLDER,
                        props).getChildRef();
                long container6NodeID = getNodeID(container6);
                if(logger.isDebugEnabled())
                {
                    logger.debug("container6 = " + container6);
                }
                
                for(int i = 0; i < 2000; i++)
                {
                    FileInfo content1Info = fileFolderService.create(container6, "Content" + i, ContentModel.TYPE_CONTENT);
                    NodeRef nodeRef = content1Info.getNodeRef();
                    contents.add(nodeRef);
                    
                    if(i % 2 == 1)
                    {
                        nodeService.addAspect(nodeRef, ContentModel.ASPECT_TEMPORARY, null);
                    }
                }
                
                return null;
            }
        });
    }
    
/*    public void testGetTransactions1() throws Exception
    {
        long fromCommitTime = System.currentTimeMillis();

        buildTransactions1();

        String url = "/api/solr/transactions?fromCommitTime=" + fromCommitTime;
        TestWebScriptServer.GetRequest req = new TestWebScriptServer.GetRequest(url);
        Response response = sendRequest(req, Status.STATUS_OK, admin);
        
//        assertEquals("Expected application/json content type", "application/json[;charset=UTF-8]", response.getContentType());
        
//        System.out.println(response.getContentAsString());
        JSONObject json = new JSONObject(response.getContentAsString());

        JSONArray transactions = json.getJSONArray("transactions");
        assertEquals("Number of transactions is incorrect", 2, transactions.length());

        int[] updates = new int[] {1, 1};
        int[] deletes = new int[] {0, 1};
        //StringBuilder txnIds = new StringBuilder();
        int numTxns = transactions.length();
        List<Long> transactionIds = getTransactionIds(transactions);
        for(int i = 0; i < numTxns; i++)
        {
            JSONObject txn = transactions.getJSONObject(i);
            assertEquals("Number of deletes is incorrect", deletes[i], txn.getLong("deletes"));
            assertEquals("Number of updates is incorrect", updates[i], txn.getLong("updates"));

//          txnIds.append(txn.getString("id"));
//          if(i < (numTxns - 1))
//          {
//              txnIds.append(",");
//          }
        }
        
        // get all nodes at once
        if(logger.isDebugEnabled())
        {
            logger.debug("txnIds = " + transactions.toString());
        }
        
        GetNodesParameters parameters = new GetNodesParameters();
        parameters.setTransactionIds(transactionIds);
        JSONArray nodes = getNodes(parameters, 0, 3);
        if(logger.isDebugEnabled())
        {
            logger.debug("nodes:");
            logger.debug(nodes.toString(3));
        }

        JSONObject lastNode = nodes.getJSONObject(2);
        assertTrue("nodeID is missing", lastNode.has("id"));
        Long fromNodeId = lastNode.getLong("id");
        if(logger.isDebugEnabled())
        {
            logger.debug("fromNodeId = " + fromNodeId);
        }
        assertNotNull("Unexpected null fromNodeId", fromNodeId);

        firstNode = nodes.getJSONObject(0);
        secondNode = nodes.getJSONObject(1);
        //assertEquals("Expected transaction ids to be the same", firstNode.getLong("txnID") == secondNode.getLong("txnID"));
        assertEquals("Expected node update", "u", firstNode.getString("status"));
        assertEquals("Expected node deleted", "d", secondNode.getString("status"));
        assertEquals("Node id is incorrect", container1NodeID, firstNode.getLong("id"));
        assertEquals("Node id is incorrect", content1NodeID, secondNode.getLong("id"));

        // get first 2 nodes
        parameters = new GetNodesParameters();
        parameters.setTransactionIds(transactionIds);
        nodes = getNodes(parameters, 2, 2);
        if(logger.isDebugEnabled())
        {
            logger.debug("nodes:");
            logger.debug(nodes.toString(3));
        }

        lastNode = nodes.getJSONObject(1);
        assertTrue("nodeID is missing", lastNode.has("id"));
        fromNodeId = lastNode.getLong("id");
        if(logger.isDebugEnabled())
        {
            logger.debug("fromNodeId = " + fromNodeId);
        }
        assertNotNull("Unexpected null fromNodeId", fromNodeId);

        // get 4 nodes starting with fromNodeId, should return only 2 nodes (including fromNodeId)
        parameters = new GetNodesParameters();
        parameters.setTransactionIds(transactionIds);
        parameters.setFromNodeId(fromNodeId);
        nodes = getNodes(parameters, 4, 2);
        if(logger.isDebugEnabled())
        {
            logger.debug("nodes:");
            logger.debug(nodes.toString(3));
        }

        firstNode = nodes.getJSONObject(0);
        secondNode = nodes.getJSONObject(1);
        assertEquals("Expected node deleted", "d", firstNode.getString("status"));
        assertEquals("Expected node updated", "u", secondNode.getString("status"));
        assertEquals("Node id is incorrect", content1NodeID, firstNode.getLong("id"));
        assertEquals("Node id is incorrect", content2NodeID, secondNode.getLong("id"));
                
        // get 0 (all) nodes starting with fromNodeId, should return 2 nodes
        parameters = new GetNodesParameters();
        parameters.setTransactionIds(transactionIds);
        parameters.setFromNodeId(fromNodeId);
        nodes = getNodes(parameters, 0, 2);
        if(logger.isDebugEnabled())
        {
            logger.debug("nodes:");
            logger.debug(nodes.toString(3));
        }

        // get 2 nodes ending with toNodeId, should return 2 nodes
        long toNodeId = content2NodeID;
        parameters = new GetNodesParameters();
        parameters.setTransactionIds(transactionIds);
        parameters.setToNodeId(toNodeId);
        nodes = getNodes(parameters, 2, 2);
        if(logger.isDebugEnabled())
        {
            logger.debug("nodes:");
            logger.debug(nodes.toString(3));
        }

        firstNode = nodes.getJSONObject(0);
        secondNode = nodes.getJSONObject(1);
        assertEquals("Expected node deleted", "u", firstNode.getString("status"));
        assertEquals("Node id is incorrect", container1NodeID, firstNode.getLong("id"));
        assertEquals("Expected node updated", "d", secondNode.getString("status"));
        assertEquals("Node id is incorrect", content1NodeID, secondNode.getLong("id"));

        // get 1 node ending with toNodeId, should return 1 nodes
        toNodeId = content2NodeID;
        parameters = new GetNodesParameters();
        parameters.setTransactionIds(transactionIds);
        parameters.setToNodeId(toNodeId);
        nodes = getNodes(parameters, 1, 1);
        if(logger.isDebugEnabled())
        {
            logger.debug("nodes:");
            logger.debug(nodes.toString(3));
        }

        firstNode = nodes.getJSONObject(0);
        assertEquals("Expected node updated", "u", firstNode.getString("status"));
        assertEquals("Node id is incorrect", container1NodeID, firstNode.getLong("id"));

        // get 3 nodes ending with toNodeId, should return 3 nodes
        toNodeId = content2NodeID;
        parameters = new GetNodesParameters();
        parameters.setTransactionIds(transactionIds);
        parameters.setToNodeId(toNodeId);
        nodes = getNodes(parameters, 3, 3);
        if(logger.isDebugEnabled())
        {
            logger.debug("nodes:");
            logger.debug(nodes.toString(3));
        }

        firstNode = nodes.getJSONObject(0);
        assertEquals("Expected node updated", "u", firstNode.getString("status"));
        assertEquals("Node id is incorrect", container1NodeID, firstNode.getLong("id"));

        secondNode = nodes.getJSONObject(1);
        assertEquals("Expected node deleted", "d", secondNode.getString("status"));
        assertEquals("Node id is incorrect", content1NodeID, secondNode.getLong("id"));

        thirdNode = nodes.getJSONObject(2);
        assertEquals("Expected node updated", "u", thirdNode.getString("status"));
        assertEquals("Node id is incorrect", content2NodeID, thirdNode.getLong("id"));
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
        JSONArray nodes = getNodes(parameters, 0, 101);
        
        nodes = getNodes(parameters, 50, 50);
        
        // store protocol
        parameters = new GetNodesParameters();
        parameters.setTransactionIds(transactionIds);
        parameters.setStoreProtocol(storeRef.getProtocol());
        nodes = getNodes(parameters, 0, 202);

        // store identifier
        parameters = new GetNodesParameters();
        parameters.setTransactionIds(transactionIds);
        parameters.setStoreIdentifier(storeRef.getIdentifier());
        nodes = getNodes(parameters, 0, 101);
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
            logger.debug("txns =");
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
        JSONArray nodes = getNodes(parameters, 0, 4);
        JSONObject lastNode = nodes.getJSONObject(nodes.length() - 1);
        Long fromNodeId = lastNode.getLong("id");
        assertNotNull("Unexpected null fromNodeId", fromNodeId);

        // get first 2 nodes
        parameters = new GetNodesParameters();
        parameters.setTransactionIds(transactionIds);
        nodes = getNodes(parameters, 2, 2);
        if(logger.isDebugEnabled())
        {
            logger.debug("nodes:");
            logger.debug(nodes.toString(3));
        }

        firstNode = nodes.getJSONObject(0);
        assertTrue("nodeID is missing", firstNode.has("id"));
        secondNode = nodes.getJSONObject(1);
        assertTrue("nodeID is missing", secondNode.has("id"));
        fromNodeId = secondNode.getLong("id");
        if(logger.isDebugEnabled())
        {
            logger.debug("fromNodeId = " + fromNodeId);
        }
        assertNotNull("Unexpected null nodeID", fromNodeId);

        //assertEquals("Expected transaction ids to be the same", firstNode.getLong("txnID"), secondNode.getLong("txnID"));
        assertEquals("Expected node update", "u", firstNode.getString("status"));
        assertEquals("Expected node delete", "d", secondNode.getString("status"));
        assertEquals("Incorrect node id", container2NodeID, firstNode.getLong("id"));
        assertEquals("Incorrect node id", content1NodeID, secondNode.getLong("id"));
        
        // get 10 nodes (including fromNodeId) starting with fromNodeId, should return only 3 nodes
        parameters = new GetNodesParameters();
        parameters.setTransactionIds(transactionIds);
        parameters.setFromNodeId(fromNodeId);
        nodes = getNodes(parameters, 10, 3);

        firstNode = nodes.getJSONObject(0);
        assertTrue("nodeID is missing", firstNode.has("id"));
        secondNode = nodes.getJSONObject(1);
        assertTrue("nodeID is missing", secondNode.has("id"));
        thirdNode = nodes.getJSONObject(2);
        assertTrue("nodeID is missing", thirdNode.has("id"));

        assertEquals("Expected node delete", "d", firstNode.getString("status"));
        assertEquals("Expected node update", "u", secondNode.getString("status"));
        assertEquals("Expected node update", "u", thirdNode.getString("status"));
        assertEquals("Incorrect node id", content1NodeID, firstNode.getLong("id"));
        assertEquals("Incorrect node id", content2NodeID, secondNode.getLong("id"));
        assertEquals("Incorrect node id", content3NodeID, thirdNode.getLong("id"));

        // test with from and to node ids
        parameters = new GetNodesParameters();
        parameters.setTransactionIds(transactionIds);
        parameters.setFromNodeId(container2NodeID);
        parameters.setToNodeId(content3NodeID);
        nodes = getNodes(parameters, 2, 2);

        firstNode = nodes.getJSONObject(0);
        assertTrue("nodeID is missing", firstNode.has("id"));
        secondNode = nodes.getJSONObject(1);
        assertTrue("nodeID is missing", secondNode.has("id"));
        assertEquals("Incorrect node id", container2NodeID, firstNode.getLong("id"));
        assertEquals("Incorrect node id", content1NodeID, secondNode.getLong("id"));

        // test right truncation
        parameters = new GetNodesParameters();
        parameters.setTransactionIds(transactionIds);
        parameters.setToNodeId(content3NodeID);
        nodes = getNodes(parameters, 2, 2);

        firstNode = nodes.getJSONObject(0);
        assertTrue("nodeID is missing", firstNode.has("id"));
        secondNode = nodes.getJSONObject(1);
        assertTrue("nodeID is missing", secondNode.has("id"));
        assertEquals("Incorrect node id", container2NodeID, firstNode.getLong("id"));
        assertEquals("Incorrect node id", content1NodeID, secondNode.getLong("id"));
        
        // test left truncation, specifying from node only
        parameters = new GetNodesParameters();
        parameters.setTransactionIds(transactionIds);
        parameters.setFromNodeId(container2NodeID);
        nodes = getNodes(parameters, 2, 2);

        firstNode = nodes.getJSONObject(0);
        assertTrue("nodeID is missing", firstNode.has("id"));
        secondNode = nodes.getJSONObject(1);
        assertTrue("nodeID is missing", secondNode.has("id"));
        assertEquals("Incorrect node id", container2NodeID, firstNode.getLong("id"));
        assertEquals("Incorrect node id", content1NodeID, secondNode.getLong("id"));
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
            logger.debug("txns = ");
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
        JSONArray nodes = getNodes(parameters, 0, 51);
    }*/
    
    public void testNodeMetaData() throws Exception
    {
        long fromCommitTime = System.currentTimeMillis();

        buildTransactions5();

        JSONArray transactions = getTransactions(fromCommitTime);
        assertEquals("Number of transactions is incorrect", 1, transactions.length());

        List<Long> transactionIds = getTransactionIds(transactions);

        GetNodesParameters params = new GetNodesParameters();
        params.setTransactionIds(transactionIds);
        JSONArray nodes = getNodes(params, 0, 2);
        
        List<Long> nodeIds = new ArrayList<Long>(nodes.length());
        for(int i = 0; i < nodes.length(); i++)
        {
            JSONObject node = nodes.getJSONObject(i);
            nodeIds.add(node.getLong("id"));
        }
        
        JSONArray nodesMetaData = getNodesMetaData(nodeIds, 0, 2);

        // test second entry (second node created in buildTransactions)
        NodeRef expectedNodeRef = contents.get(0);
        
        JSONObject node = nodesMetaData.getJSONObject(1);
        NodeRef nodeRef = new NodeRef(node.getString("nodeRef"));

        assertEquals("NodeRef is incorrect", expectedNodeRef, nodeRef);
        
        JSONArray aspects = node.getJSONArray("aspects");
        JSONObject properties = node.getJSONObject("properties");
        Map<QName, String> propertyMap = getPropertyMap(properties);
        
//        assertEquals("Incorrect number of aspects", 1, aspects.length());
        assertTrue("Expected author aspect", containsAspect(aspects, ContentModel.ASPECT_AUTHOR));
        assertTrue("Expected author property", containsProperty(propertyMap, ContentModel.PROP_AUTHOR, "steve"));
        
        JSONArray paths = node.getJSONArray("paths");
        List<Path> expectedPaths = nodeService.getPaths(expectedNodeRef, false);
        for(int i = 0; i < paths.length(); i++)
        {
            JSONObject o = paths.getJSONObject(i);
            String path = o.getString("path");
            String qname = o.has("qname") ? o.getString("qname") : null;
            String expectedPath = expectedPaths.get(i).toString();
            assertEquals("Path element " + i + " is incorrect", expectedPath, path);
            assertNull("qname should be null", qname);
        }
    }

    public void testNodeMetaDataManyNodes() throws Exception
    {
        long fromCommitTime = System.currentTimeMillis();

        buildTransactions6();

        JSONArray transactions = getTransactions(fromCommitTime);
        assertEquals("Number of transactions is incorrect", 1, transactions.length());

        List<Long> transactionIds = getTransactionIds(transactions);

        GetNodesParameters params = new GetNodesParameters();
        params.setTransactionIds(transactionIds);
        JSONArray nodes = getNodes(params, 0, 2001);
        
        List<Long> nodeIds = new ArrayList<Long>(nodes.length());
        for(int i = 0; i < nodes.length(); i++)
        {
            JSONObject node = nodes.getJSONObject(i);
            nodeIds.add(node.getLong("id"));
        }

        // make sure caches are warm - time last call
        JSONArray nodesMetaData = getNodesMetaData(nodeIds, 0, 2001);
        nodesMetaData = getNodesMetaData(nodeIds, 0, 2001);

        // sleep for a couple of seconds
        try
        {
            Thread.sleep(2000);
        }
        catch(InterruptedException e)
        {
            // ignore
        }
        nodesMetaData = getNodesMetaData(nodeIds, 0, 2001);
        
        nodesMetaData = getNodesMetaData(nodeIds, 1000, 1000);
        nodesMetaData = getNodesMetaData(nodeIds, 600, 600);
        nodesMetaData = getNodesMetaData(nodeIds, 300, 300);
        nodesMetaData = getNodesMetaData(nodeIds, 100, 100);
        nodesMetaData = getNodesMetaData(nodeIds, 50, 50);

        // clear out caches
        nodeDAO.clear();

        nodesMetaData = getNodesMetaData(nodeIds, 0, 2001);
    }
    
    private boolean containsAspect(JSONArray aspectsArray, QName aspect) throws Exception
    {
        if(aspect == null)
        {
            throw new IllegalArgumentException("aspect cannot be null");
        }

        boolean success = false;
        for(int i = 0; i < aspectsArray.length(); i++)
        {
            String qName = aspectsArray.getString(i);
            if(aspect.equals(QName.createQName(qName, namespaceService)))
            {
                success |= true;
                break;
            }
        }
        
        return success;
    }

    private Map<QName, String> getPropertyMap(JSONObject properties) throws Exception
    {
        Map<QName, String> propertyMap = new HashMap<QName, String>(properties.length());
        @SuppressWarnings("rawtypes")
        Iterator propNames = properties.keys();
        while(propNames.hasNext())
        {
            String propName = (String)propNames.next();
            String value = properties.getString(propName);

            propertyMap.put(QName.createQName(propName, namespaceService), value);
        }

        return propertyMap;
    }
    
    private boolean containsProperty(Map<QName, String> propertyMap, QName propName, String propValue) throws Exception
    {
        if(propName == null)
        {
            throw new IllegalArgumentException("propName cannot be null");
        }

        String value = propertyMap.get(propName);
        return (value == null ? false : value.equals(propValue));
//        boolean success = false;
//        for(int i = 0; i < propertiesArray.length(); i++)
//        {
//            JSONObject prop = propertiesArray.getJSONObject(i);
//            prop.keys();
//            String qName = prop.getString("name");
//            String value = prop.getString("value");
//            if(qName.equals(QName.createQName(qName)))
//            {
//                success |= (propValue == null ? true : value.equals(propValue));
//            }
//            if(success)
//            {
//                break;
//            }
//        }
//        
//        return success;
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
}