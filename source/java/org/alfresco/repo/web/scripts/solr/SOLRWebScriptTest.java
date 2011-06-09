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
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.solr.Acl;
import org.alfresco.repo.solr.AclChangeSet;
import org.alfresco.repo.solr.AclReaders;
import org.alfresco.repo.solr.SOLRTrackingComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
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
public class SOLRWebScriptTest extends BaseWebScriptTest
{
    protected static final Log logger = LogFactory.getLog(SOLRWebScriptTest.class);

    private ApplicationContext ctx;
    private SOLRTrackingComponent solrTrackingComponent;
    private NodeDAO nodeDAO;
    private TransactionService transactionService;
    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private RetryingTransactionHelper txnHelper;
    private NamespaceService namespaceService;

    private String admin;

    private StoreRef storeRef;
    private NodeRef rootNodeRef;
    private NodeRef container3;

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
        fileFolderService = serviceRegistry.getFileFolderService();
        namespaceService = serviceRegistry.getNamespaceService();
        txnHelper = transactionService.getRetryingTransactionHelper();
        nodeDAO = (NodeDAO)ctx.getBean("nodeDAO");
        solrTrackingComponent = (SOLRTrackingComponent) ctx.getBean("solrTrackingComponent");

        admin = AuthenticationUtil.getAdminUserName();

        AuthenticationUtil.setFullyAuthenticatedUser(admin);
        
        storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, getName() + ".1." + System.currentTimeMillis());
        rootNodeRef = nodeService.getRootNode(storeRef);
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
        
        logger.debug("Got " + transactions.length() + " txns in " + (endTime - startTime) + " ms");
        
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
        
        logger.debug("Got " + aclChangeSets.length() + " txns in " + (endTime - startTime) + " ms");
    }

    public void testAclsGet() throws Exception
    {
        List<AclChangeSet> aclChangeSets = solrTrackingComponent.getAclChangeSets(null, null, 100);
        if (aclChangeSets.size() == 0)
        {
            return;         // Can't test, but very unlikely
        }
        // Build JSON using these
        JSONObject json = new JSONObject();
        JSONArray aclChangeSetIdsJSON = new JSONArray();
        int count = 0;
        List<Long> aclChangeSetIds = new ArrayList<Long>();
        for (AclChangeSet aclChangeSet : aclChangeSets)
        {
            if (count >= 512)
            {
                break;
            }
            if (aclChangeSet.getAclCount() == 0)
            {
                continue;           // No ACLs
            }
            Long aclChangeSetId = aclChangeSet.getId();
            aclChangeSetIdsJSON.put(aclChangeSetId);
            aclChangeSetIds.add(aclChangeSetId);
            count++;
        }
        json.put("aclChangeSetIds", aclChangeSetIdsJSON);
        
        String url = "/api/solr/acls";
        TestWebScriptServer.PostRequest req = new TestWebScriptServer.PostRequest(url, json.toString(), "application/json");
        Response response = sendRequest(req, Status.STATUS_OK, admin);
        if(logger.isDebugEnabled())
        {
            logger.debug(response.getContentAsString());
        }
        json = new JSONObject(response.getContentAsString());
        JSONArray acls = json.getJSONArray("acls");
        
        // Check
        List<Acl> aclsCheck = solrTrackingComponent.getAcls(aclChangeSetIds, null, 512);
        assertEquals("Script and API returned different number of results", aclsCheck.size(), acls.length());
    }
    
    public void testAclReadersGet() throws Exception
    {
        List<AclChangeSet> aclChangeSets = solrTrackingComponent.getAclChangeSets(null, null, 1024);
        List<Long> aclChangeSetIds = new ArrayList<Long>(50);
        for (AclChangeSet aclChangeSet : aclChangeSets)
        {
            if (aclChangeSet.getAclCount() > 0)
            {
                aclChangeSetIds.add(aclChangeSet.getId());
                break;
            }
        }
        if (aclChangeSetIds.size() == 0)
        {
            // No ACLs; not likely
        }
        List<Acl> acls = solrTrackingComponent.getAcls(aclChangeSetIds, null, 1024);
        List<Long> aclIds = new ArrayList<Long>(acls.size());
        JSONObject json = new JSONObject();
        JSONArray aclIdsJSON = new JSONArray();
        for (Acl acl : acls)
        {
            Long aclId = acl.getId();
            aclIds.add(aclId);
            aclIdsJSON.put(aclId);
        }
        json.put("aclIds", aclIdsJSON);
        
        // Now get the readers
        List<AclReaders> aclsReaders = solrTrackingComponent.getAclsReaders(aclIds);
        assertEquals("Should have same number of ACLs as supplied", aclIds.size(), aclsReaders.size());
        assertTrue("Must have *some* ACLs here", aclIds.size() > 0);
        Map<Long, Set<String>> readersByAclId = new HashMap<Long, Set<String>>();
        for (AclReaders aclReaders : aclsReaders)
        {
            readersByAclId.put(aclReaders.getAclId(), aclReaders.getReaders());
        }
        
        // Now query using the webscript
        String url = "/api/solr/aclsReaders";
        TestWebScriptServer.PostRequest req = new TestWebScriptServer.PostRequest(url, json.toString(), "application/json");
        Response response = sendRequest(req, Status.STATUS_OK, admin);
        if(logger.isDebugEnabled())
        {
            logger.debug(response.getContentAsString());
        }
        json = new JSONObject(response.getContentAsString());
        JSONArray aclsReadersJSON = json.getJSONArray("aclsReaders");
        // Check
        assertEquals("Script and API returned different number of results", readersByAclId.size(), aclsReadersJSON.length());
        
        // Iterate of the JSON and ensure that the list of ACL readers is correct
        for (int i = 0; i < aclsReadersJSON.length(); i++)
        {
            // Choose an ACL and check the readers
            JSONObject aclReadersJSON = aclsReadersJSON.getJSONObject(i);
            Long aclIdJSON = aclReadersJSON.getLong("aclId");
            Set<String> readersCheck = readersByAclId.get(aclIdJSON);
            JSONArray readersJSON = aclReadersJSON.getJSONArray("readers");
            assertEquals("Readers list for ACL " + aclIdJSON + " is wrong. ", readersCheck.size(), readersJSON.length());
            for (int j = 0; j < readersJSON.length(); j++)
            {
                String readerJSON = readersJSON.getString(j);
                assertTrue("Found reader not in check set: " + readerJSON, readersCheck.contains(readerJSON));
            }
        }
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
    	//logger.debug("getNodes: " + response.getContentAsString());
        JSONObject jsonResponse = new JSONObject(response.getContentAsString());
        jsonResponse.write(new PrintWriter(System.out));

        JSONArray nodes = jsonResponse.getJSONArray("nodes");

        //assertEquals("Node count is incorrect", nodes.length(), json.getInt("count"));

        logger.debug("Got " + nodes.length() + " nodes in " + (endTime - startTime) + " ms");

        assertEquals("Number of returned node meta data results is incorrect", expectedNumNodes, nodes.length());

        return nodes;
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

    public static String join(Collection<Object> s, String delimiter)
    {
        StringBuffer buffer = new StringBuffer();
        Iterator<Object> iter = s.iterator();
        while (iter.hasNext())
        {
            buffer.append(iter.next());
            if (iter.hasNext())
            {
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

        logger.debug("Got metadata for " + nodes.length() + " nodes in " + (endTime - startTime) + " ms");
        
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
        @SuppressWarnings("unused")
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
    }
    
    public void DISABLED_testGetContent() throws Exception
    {
        long nodeId = -1l;
        String propertyName = ContentModel.PROP_CONTENT.toString();

        buildTransactions3();
        
        String url = "/api/solr/content?nodeId=" + nodeId + "&propertyName=" + propertyName;
        TestWebScriptServer.GetRequest req = new TestWebScriptServer.GetRequest(url);
        Response response = sendRequest(req, Status.STATUS_OK, admin);
        if (logger.isDebugEnabled())
        {
            logger.debug("content1 = " + response.getContentAsString());
        }
        assertEquals("Content length is incorrect", "test content".length(), response.getContentLength());
    }
    
    private long getNodeID(NodeRef nodeRef)
    {
        Pair<Long, NodeRef> pair = nodeDAO.getNodePair(nodeRef);
        assertNotNull("Can't find node " + nodeRef, pair);
        return pair.getFirst();
    }
}