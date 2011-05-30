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
package org.alfresco.repo.domain.solr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.node.Node;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.solr.SOLRDAO.NodeMetaDataQueryCallback;
import org.alfresco.repo.domain.solr.SOLRDAO.NodeQueryCallback;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.PropertyMap;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Tests for the SOLR DAO
 *
 * @since 4.0
 */
// TODO test fromTxnId, toTxnId for getNodes
// TODO nodes created and deleted in the txns will be reported back as deleted. Is this desirable?
// TODO getNodes fromId, toId test
// TODO test filtering for getNodeMetaData
public class SOLRDAOTest extends TestCase
{
    private ConfigurableApplicationContext ctx = (ConfigurableApplicationContext) ApplicationContextHelper.getApplicationContext();
    private static enum NodeStatus
    {
        UPDATED, DELETED;
    }

    private AuthenticationComponent authenticationComponent;
    private TransactionService transactionService;
    private RetryingTransactionHelper txnHelper;
    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private NodeDAO nodeDAO;
    private SOLRDAO solrDAO;
    
    private StoreRef storeRef;
    private NodeRef rootNodeRef;
    
    @Override
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        transactionService = serviceRegistry.getTransactionService();
        txnHelper = transactionService.getRetryingTransactionHelper();
        
        solrDAO = (SOLRDAO)ctx.getBean("solrDAO");
        nodeDAO = (NodeDAO)ctx.getBean("nodeDAO");
        nodeService = (NodeService)ctx.getBean("NodeService");
        fileFolderService = (FileFolderService)ctx.getBean("FileFolderService");
        authenticationComponent = (AuthenticationComponent)ctx.getBean("authenticationComponent");
        
        authenticationComponent.setSystemUserAsCurrentUser();
        
        storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, getName() + System.currentTimeMillis());
        rootNodeRef = nodeService.getRootNode(storeRef);
    }
    
    public void testQueryTransactions1()
    {
        long startTime = System.currentTimeMillis();

        SOLRTest st = new SOLRTest1(txnHelper, fileFolderService, nodeService, rootNodeRef, "testQueryTransactions1", true, true);
        st.buildTransactions();
        
        List<Transaction> txns = solrDAO.getTransactions(null, startTime, 0);

    	int[] updates = new int[] {1, 1};
    	int[] deletes = new int[] {0, 1};
        List<Long> txnIds = checkTransactions(txns, 2, updates, deletes);
    	
    	NodeParameters nodeParameters = new NodeParameters();
    	nodeParameters.setTransactionIds(txnIds);
        getNodes(nodeParameters, st);
    	
    	//assertEquals("Unexpected nodes", 3, bt.getSuccessCount());
    }
    
    public void testQueryTransactions2()
    {
        long startTime = System.currentTimeMillis();

        SOLRTest st = new SOLRTest2(txnHelper, fileFolderService, nodeService, rootNodeRef, "testQueryTransactions2", true, true);
        st.buildTransactions();
        
        List<Transaction> txns = solrDAO.getTransactions(null, startTime, 0);

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
    	
    	//TestNodeQueryCallback nodeQueryCallback = new TestNodeQueryCallback(bt.getNodes());
        NodeParameters nodeParameters = new NodeParameters();
        nodeParameters.setTransactionIds(txnIds);
        getNodes(nodeParameters, st);
    	
    	//assertEquals("Unexpected nodes", 3, nodeQueryCallback.getSuccessCount());
    }
    
    public void testGetNodeMetaData()
    {
        long startTime = System.currentTimeMillis();

        SOLRTest st = new SOLRTest3(txnHelper, fileFolderService, nodeService, rootNodeRef, "testGetNodeMetaData", true, true);
        st.buildTransactions();
        
        List<Transaction> txns = solrDAO.getTransactions(null, startTime, 0);

        int[] updates = new int[] {1, 1};
        int[] deletes = new int[] {0, 1};
        List<Long> txnIds = checkTransactions(txns, 2, updates, deletes);

        NodeParameters nodeParameters = new NodeParameters();
        nodeParameters.setTransactionIds(txnIds);
        getNodes(nodeParameters, st);
        
//        assertEquals("Unxpected number of nodes", 3, nodeQueryCallback.getSuccessCount());

        NodeMetaDataParameters nodeMetaDataParams = new NodeMetaDataParameters();
        nodeMetaDataParams.setNodeIds(st.getNodeIds());
        getNodeMetaData(nodeMetaDataParams, null, st);
        
//        assertEquals("Unxpected number of nodes", 3, bt.getSuccessCount());
    }
    
    public void testGetNodeMetaData100Nodes()
    {
        long startTime = System.currentTimeMillis();

        SOLRTest st = new SOLRTest100Nodes(txnHelper, fileFolderService, nodeService, rootNodeRef, "testGetNodeMetaData", true, true);
        st.buildTransactions();
        
        List<Transaction> txns = solrDAO.getTransactions(null, startTime, 0);

        int[] updates = new int[] {100};
        int[] deletes = new int[] {0};
        List<Long> txnIds = checkTransactions(txns, 1, updates, deletes);

        NodeParameters nodeParameters = new NodeParameters();
        nodeParameters.setTransactionIds(txnIds);
        getNodes(nodeParameters, st);
        
//        assertEquals("Unxpected number of nodes", 3, nodeQueryCallback.getSuccessCount());

        NodeMetaDataParameters nodeMetaDataParams = new NodeMetaDataParameters();
        nodeMetaDataParams.setNodeIds(st.getNodeIds());
        getNodeMetaData(nodeMetaDataParams, null, st);

        nodeMetaDataParams.setMaxResults(20);
        getNodeMetaData(nodeMetaDataParams, null, st);
        
//        assertEquals("Unxpected number of nodes", 3, bt.getSuccessCount());
    }
    
    public void testNodeMetaDataManyNodes() throws Exception
    {
        long fromCommitTime = System.currentTimeMillis();

        SOLRTest st = new SOLRTest4(txnHelper, fileFolderService, nodeService, rootNodeRef, "testNodeMetaDataManyNodes", true, false);
        st.buildTransactions();

        List<Transaction> txns = solrDAO.getTransactions(null, fromCommitTime, 0);

        int[] updates = new int[] {2001};
        int[] deletes = new int[] {0};
        List<Long> txnIds = checkTransactions(txns, 1, updates, deletes);

        NodeParameters nodeParameters = new NodeParameters();
        nodeParameters.setTransactionIds(txnIds);
        getNodes(nodeParameters, st);
        
        // make sure caches are warm - time last call
        System.out.println("Cold cache");
        NodeMetaDataParameters nodeMetaDataParams = new NodeMetaDataParameters();
        nodeMetaDataParams.setNodeIds(st.getNodeIds());
        getNodeMetaData(nodeMetaDataParams, null, st);
        getNodeMetaData(nodeMetaDataParams, null, st);
        System.out.println("Warm cache");
        getNodeMetaData(nodeMetaDataParams, null, st);
        
        // clear out node caches
        nodeDAO.clear();

        System.out.println("Cold cache - explicit clear");
        nodeMetaDataParams.setMaxResults(800);
        getNodeMetaData(nodeMetaDataParams, null, st);
        getNodeMetaData(nodeMetaDataParams, null, st);
        System.out.println("Warm cache");        
        getNodeMetaData(nodeMetaDataParams, null, st);
        
        System.out.println("Cold cache - explicit clear");
        nodeMetaDataParams.setMaxResults(500);
        getNodeMetaData(nodeMetaDataParams, null, st);
        getNodeMetaData(nodeMetaDataParams, null, st);
        System.out.println("Warm cache");        
        getNodeMetaData(nodeMetaDataParams, null, st);
        
        System.out.println("Cold cache - explicit clear");
        nodeMetaDataParams.setMaxResults(200);
        getNodeMetaData(nodeMetaDataParams, null, st);
        getNodeMetaData(nodeMetaDataParams, null, st);
        System.out.println("Warm cache");        
        getNodeMetaData(nodeMetaDataParams, null, st);
        
        // clear out node caches
        nodeDAO.clear();
        
        System.out.println("Cold cache - explicit clear");
        getNodeMetaData(nodeMetaDataParams, null, st);
    }

    public void testFilters()
    {
        long startTime = System.currentTimeMillis();

        SOLRTest st = new SOLRTest1(txnHelper, fileFolderService, nodeService, rootNodeRef, "testFilters", true, true);
        st.buildTransactions();
        
        List<Transaction> txns = solrDAO.getTransactions(null, startTime, 0);

        int[] updates = new int[] {1, 1};
        int[] deletes = new int[] {0, 1};
        List<Long> txnIds = checkTransactions(txns, 2, updates, deletes);
        
        NodeParameters nodeParameters = new NodeParameters();
        nodeParameters.setTransactionIds(txnIds);
        getNodes(nodeParameters, st);

        NodeMetaDataParameters nodeMetaDataParams = new NodeMetaDataParameters();
        nodeMetaDataParams.setNodeIds(st.getNodeIds());
        getNodeMetaData(nodeMetaDataParams, null, st);
    }

    private static class NodeAssertions
    {
        private Set<QName> aspects;
        private Map<QName, Serializable> properties;
        private NodeStatus nodeStatus;
        private Boolean expectAspects = true;
        private Boolean expectProperties = true;
        private boolean expectType = true;
        private boolean expectOwner = true;
        private boolean expectAssociations = true;
        private boolean expectPaths = true;
        private boolean expectAclId = true;

        public NodeAssertions()
        {
            super();
        }
        
        public boolean isExpectType()
        {
            return expectType;
        }

        public void setExpectType(boolean expectType)
        {
            this.expectType = expectType;
        }


        public boolean isExpectOwner()
        {
            return expectOwner;
        }


        public void setExpectOwner(boolean expectOwner)
        {
            this.expectOwner = expectOwner;
        }


        public boolean isExpectAssociations()
        {
            return expectAssociations;
        }


        public void setExpectAssociations(boolean expectAssociations)
        {
            this.expectAssociations = expectAssociations;
        }


        public boolean isExpectPaths()
        {
            return expectPaths;
        }


        public void setExpectPaths(boolean expectPaths)
        {
            this.expectPaths = expectPaths;
        }


        public boolean isExpectAclId()
        {
            return expectAclId;
        }


        public void setExpectAclId(boolean expectAclId)
        {
            this.expectAclId = expectAclId;
        }


        public boolean isExpectAspects()
        {
            return expectAspects;
        }

        public void setExpectAspects(boolean expectAspects)
        {
            this.expectAspects = expectAspects;
        }

        public boolean isExpectProperties()
        {
            return expectProperties;
        }

        public void setExpectProperties(boolean expectProperties)
        {
            this.expectProperties = expectProperties;
        }

        public void setAspects(Set<QName> aspects)
        {
            this.aspects = aspects;
        }

        public void setProperties(Map<QName, Serializable> properties)
        {
            this.properties = properties;
        }

        public void setNodeStatus(NodeStatus nodeStatus)
        {
            this.nodeStatus = nodeStatus;
        }

        public NodeStatus getNodeStatus()
        {
            return nodeStatus;
        }

        public Set<QName> getAspects()
        {
            return aspects;
        }

        public Map<QName, Serializable> getProperties()
        {
            return properties;
        }
    }

    private List<Long> checkTransactions(List<Transaction> txns, int numTransactions, int[] updates, int[] deletes)
    {
        assertEquals("Number of transactions is incorrect", numTransactions, txns.size());

        List<Long> txnIds = new ArrayList<Long>(txns.size());
        int i = 0;
        for(Transaction txn : txns)
        {
            assertEquals("Number of deletes is incorrect", deletes[i], txn.getDeletes());
            assertEquals("Number of updates is incorrect", updates[i], txn.getUpdates());
            i++;

            txnIds.add(txn.getId());
        }
        
        return txnIds;
    }
    
    private void getNodes(NodeParameters nodeParameters, SOLRTest bt)
    {
        long startTime = System.currentTimeMillis();
        solrDAO.getNodes(nodeParameters, bt);        
        long endTime = System.currentTimeMillis();
        
        bt.runNodeChecks(nodeParameters.getMaxResults());
        
        System.out.println("Got " + bt.getActualNodeCount() + " nodes in " + (endTime - startTime) + " ms");
    }
    
    private void getNodeMetaData(NodeMetaDataParameters params, MetaDataResultsFilter filter, SOLRTest bt)
    {
        bt.clearNodesMetaData();

        long startTime = System.currentTimeMillis();
        solrDAO.getNodesMetadata(params, filter, bt);
        long endTime = System.currentTimeMillis();

        bt.runNodeMetaDataChecks(params.getMaxResults());
        
        System.out.println("Got " + bt.getActualNodeMetaDataCount() + " node metadatas in " + (endTime - startTime) + " ms");
    }
    
    private static abstract class SOLRTest implements NodeQueryCallback, NodeMetaDataQueryCallback
    {
        protected FileFolderService fileFolderService;
        protected RetryingTransactionHelper txnHelper;
        protected NodeService nodeService;
        protected NodeRef rootNodeRef;

        protected String containerName;
        protected Map<NodeRef, NodeAssertions> nodeAssertions;
        
        protected boolean doChecks;
        protected boolean doNodeChecks;
        protected boolean doMetaDataChecks;

        protected int successCount = 0;
        protected int failureCount = 0;

        protected List<Long> nodeIds;
        
        protected long expectedNumMetaDataNodes = 0;
        
        protected long actualNodeCount = 0;
        protected long actualNodeMetaDataCount = 0;

        SOLRTest(RetryingTransactionHelper txnHelper, FileFolderService fileFolderService, NodeService nodeService,
                NodeRef rootNodeRef, String containerName, boolean doNodeChecks, boolean doMetaDataChecks)
        {
            this.txnHelper = txnHelper;
            this.nodeService = nodeService;
            this.rootNodeRef = rootNodeRef;
            this.fileFolderService = fileFolderService;
            
            this.containerName = containerName;
            this.nodeAssertions = new HashMap<NodeRef, NodeAssertions>();
            this.nodeIds = new ArrayList<Long>(getExpectedNumNodes());
            
            this.doNodeChecks = doNodeChecks;
            this.doMetaDataChecks = doMetaDataChecks;
            this.doChecks = doNodeChecks || doMetaDataChecks;
        }

        void runNodeChecks(int maxResults)
        {
            if(doNodeChecks)
            {
                if(maxResults != 0 && maxResults != Integer.MAX_VALUE)
                {
                    assertEquals("Number of returned nodes is incorrect", maxResults, getActualNodeCount());
                }
                else
                {
                    assertEquals("Number of returned nodes is incorrect", getExpectedNumNodes(), getActualNodeCount());
                }
                assertEquals("Unexpected failures", 0, getFailureCount());
                assertEquals("Success count is incorrect", getActualNodeCount(), getSuccessCount());
            }
        }

        // TODO
        void runNodeMetaDataChecks(int maxResults)
        {
            if(maxResults != 0 && maxResults != Integer.MAX_VALUE)
            {
                assertEquals("Number of returned nodes is incorrect", maxResults, getActualNodeMetaDataCount());
            }
            else
            {
                assertEquals("Number of returned nodes is incorrect", getExpectedNumMetaDataNodes(), getActualNodeMetaDataCount());
            }
        }
        
        void clearNodes()
        {
            successCount = 0;
            failureCount = 0;
            actualNodeCount = 0;
            nodeIds.clear();
            nodeAssertions.clear();
        }

        void clearNodesMetaData()
        {
            successCount = 0;
            failureCount = 0;
            actualNodeMetaDataCount = 0;
            nodeAssertions.clear();
        }

        public long getActualNodeCount()
        {
            return actualNodeCount;
        }

        public long getActualNodeMetaDataCount()
        {
            return actualNodeMetaDataCount;
        }
        
        protected long getExpectedNumMetaDataNodes()
        {
            return expectedNumMetaDataNodes;
        }

        protected abstract int getExpectedNumNodes();
        protected abstract void buildTransactionsInternal();

        public NodeAssertions getNodeAssertions(NodeRef nodeRef)
        {
            NodeAssertions assertions = nodeAssertions.get(nodeRef);
            if(assertions == null)
            {
                assertions = new NodeAssertions();
                nodeAssertions.put(nodeRef, assertions);
            }
            return assertions;
        }
        
        protected void setExpectedNodeStatus(NodeRef nodeRef, NodeStatus nodeStatus)
        {
            if(nodeStatus == NodeStatus.UPDATED)
            {
                expectedNumMetaDataNodes++;
            }
            
            if(doChecks)
            {
                NodeAssertions nodeAssertions = getNodeAssertions(nodeRef);
                nodeAssertions.setNodeStatus(nodeStatus);
            }
        }
        
//        protected void addNode(NodeRef nodeRef, NodeAssertions nodeStatus)
//        {
//            if(nodeStatus.nodeStatus == NodeStatus.UPDATED)
//            {
//                expectedNumMetaDataNodes++;
//            }
//            
//            if(doNodeChecks || doMetaDataChecks)
//            {
//                nodeAssertions.put(nodeRef, nodeStatus);
//            }
//        }
        
        void buildTransactions()
        {
            buildTransactionsInternal();
        }
        
        @Override
        public boolean handleNode(Node node) {
            actualNodeCount++;

            if(doNodeChecks)
            {
                NodeRef nodeRef = node.getNodeRef();
                Boolean isDeleted = node.getDeleted();
                nodeIds.add(node.getId());
    
                NodeAssertions expectedStatus = getNodeAssertions(nodeRef);
                if(expectedStatus == null)
                {
                    throw new RuntimeException("Unexpected missing assertion for NodeRef " + nodeRef);
                }
                
                //System.out.println("Node: " + node.toString());
                
                if((expectedStatus.getNodeStatus() == NodeStatus.DELETED && isDeleted) ||
                        (expectedStatus.getNodeStatus() == NodeStatus.UPDATED && !isDeleted))
                {
                    successCount++;
                }
                else
                {
                    failureCount++;
                }
            }

            return true;
        }

        @Override
        public boolean handleNodeMetaData(NodeMetaData nodeMetaData) {
            actualNodeMetaDataCount++;

            if(doMetaDataChecks)
            {
                NodeRef nodeRef = nodeMetaData.getNodeRef();

                Set<QName> aspects = nodeMetaData.getAspects();
                Set<QName> actualAspects = nodeService.getAspects(nodeRef);
                assertEquals("Aspects are incorrect", aspects, actualAspects);

                Map<QName, Serializable> properties = nodeMetaData.getProperties();
                Map<QName, Serializable> actualProperties = nodeService.getProperties(nodeRef);
                assertEquals("Properties are incorrect", properties, actualProperties);

                NodeAssertions assertions = getNodeAssertions(nodeRef);
//                NodeAssertions assertions = nodes.get(nodeRef);

                Set<QName> expectedAspects = assertions.getAspects();
                if(expectedAspects != null)
                {
                    for(QName aspect : expectedAspects)
                    {
                        assertTrue("Expected aspect" + aspect, aspects.contains(aspect));
                    }
                }
                
                Map<QName, Serializable> expectedProperties = assertions.getProperties();
                if(expectedProperties != null)
                {
                    for(QName propName : expectedProperties.keySet())
                    {
                        Serializable expectedPropValue = expectedProperties.get(propName);
                        Serializable actualPropValue = properties.get(propName);
                        assertNotNull("Missing property " + propName, actualPropValue);
                        assertEquals("Incorrect property value", expectedPropValue, actualPropValue);
                    }
                }
                
                List<Path> actualPaths = nodeMetaData.getPaths();
                List<Path> expectedPaths = nodeService.getPaths(nodeRef, false);
                assertEquals("Paths are incorrect", expectedPaths, actualPaths);
                
                boolean expectAspects = assertions.isExpectAspects();
                if(expectAspects && nodeMetaData.getAspects() == null)
                {
                    fail("Expecting aspects but got no aspects");
                }
                else if(!expectAspects && nodeMetaData.getAspects() != null)
                {
                    fail("Not expecting aspects but got aspects");
                }
                
                boolean expectProperties = assertions.isExpectProperties();
                if(expectProperties && nodeMetaData.getProperties() == null)
                {
                    fail("Expecting properties but got no properties");
                }
                else if(!expectProperties && nodeMetaData.getProperties() != null)
                {
                    fail("Not expecting properties but got properties");
                }

                boolean expectType = assertions.isExpectType();
                if(expectType && nodeMetaData.getNodeType() == null)
                {
                    fail("Expecting type but got no type");
                }
                else if(!expectType && nodeMetaData.getNodeType() != null)
                {
                    fail("Not expecting type but got type");
                }
                
                boolean expectAclId = assertions.isExpectAclId();
                if(expectAclId && nodeMetaData.getAclId() == null)
                {
                    fail("Expecting acl id but got no acl id");
                }
                else if(!expectAclId && nodeMetaData.getAclId() != null)
                {
                    fail("Not expecting acl id but got acl id");
                }
                
                boolean expectPaths = assertions.isExpectPaths();
                if(expectPaths && nodeMetaData.getPaths() == null)
                {
                    fail("Expecting paths but got no paths");
                }
                else if(!expectPaths && nodeMetaData.getPaths() != null)
                {
                    fail("Not expecting paths but got paths");
                }

                boolean expectAssociations = assertions.isExpectAssociations();
                if(expectAssociations && nodeMetaData.getChildAssocs() == null)
                {
                    fail("Expecting associations but got no associations");
                }
                else if(!expectAssociations && nodeMetaData.getChildAssocs() != null)
                {
                    fail("Not expecting associations but got associations");
                }
                
                boolean expectOwner = assertions.isExpectOwner();
                if(expectOwner && nodeMetaData.getOwner() == null)
                {
                    fail("Expecting owner but got no owner");
                }
                else if(!expectOwner && nodeMetaData.getOwner() != null)
                {
                    fail("Not expecting owner but got owner");
                }
            }
            
            successCount++;

            return true;
        }
        
        public int getSuccessCount()
        {
            return successCount;
        }

        public int getFailureCount()
        {
            return failureCount;
        }

        public List<Long> getNodeIds()
        {
            return nodeIds;
        }
    }

    private static class SOLRTest1 extends SOLRTest
    {
        private NodeRef container;
        private NodeRef content1;
        private NodeRef content2;
        
        SOLRTest1(RetryingTransactionHelper txnHelper, FileFolderService fileFolderService, NodeService nodeService,
                NodeRef rootNodeRef, String containerName, boolean doNodeChecks, boolean doMetaDataChecks)
        {
            super(txnHelper, fileFolderService, nodeService, rootNodeRef, containerName, doNodeChecks, doMetaDataChecks);
        }
        
        public int getExpectedNumNodes()
        {
            return 3;
        }
        
        protected void buildTransactionsInternal()
        {
            txnHelper.doInTransaction(new RetryingTransactionCallback<Void>()
            {
                public Void execute() throws Throwable
                {
                    PropertyMap props = new PropertyMap();
                    props.put(ContentModel.PROP_NAME, "Container1");
                    container = nodeService.createNode(
                            rootNodeRef,
                            ContentModel.ASSOC_CHILDREN,
                            ContentModel.ASSOC_CHILDREN,
                            ContentModel.TYPE_FOLDER,
                            props).getChildRef();

                    FileInfo contentInfo = fileFolderService.create(container, "Content1", ContentModel.TYPE_CONTENT);
                    content1 = contentInfo.getNodeRef();

                    return null;
                }
            });

            txnHelper.doInTransaction(new RetryingTransactionCallback<Void>()
            {
                public Void execute() throws Throwable
                {
                    FileInfo contentInfo = fileFolderService.create(container, "Content2", ContentModel.TYPE_CONTENT);
                    content2 = contentInfo.getNodeRef();

                    fileFolderService.delete(content1);

                    return null;
                }
            });
            
            setExpectedNodeStatus(container, NodeStatus.UPDATED);
            setExpectedNodeStatus(content1, NodeStatus.DELETED);
            setExpectedNodeStatus(content2, NodeStatus.UPDATED);
        }
    }
    
    private static class SOLRTest2 extends SOLRTest
    {
        private NodeRef container;
        private NodeRef content1;
        private NodeRef content2;

        SOLRTest2(RetryingTransactionHelper txnHelper, FileFolderService fileFolderService, NodeService nodeService,
                NodeRef rootNodeRef, String containerName, boolean doNodeChecks, boolean doMetaDataChecks)
        {
            super(txnHelper, fileFolderService, nodeService, rootNodeRef, containerName, doNodeChecks, doMetaDataChecks);
        }
        
        public int getExpectedNumNodes()
        {
            return 3;
        }
        
        protected void buildTransactionsInternal()
        {
            txnHelper.doInTransaction(new RetryingTransactionCallback<Void>()
            {
                public Void execute() throws Throwable
                {
                    PropertyMap props = new PropertyMap();
                    props.put(ContentModel.PROP_NAME, "Container1");
                    container = nodeService.createNode(
                            rootNodeRef,
                            ContentModel.ASSOC_CHILDREN,
                            ContentModel.ASSOC_CHILDREN,
                            ContentModel.TYPE_FOLDER,
                            props).getChildRef();

                    FileInfo contentInfo = fileFolderService.create(container, "Content1", ContentModel.TYPE_CONTENT);
                    content1 = contentInfo.getNodeRef();

                    return null;
                }
            });

            txnHelper.doInTransaction(new RetryingTransactionCallback<Void>()
                    {
                public Void execute() throws Throwable
                {
                    FileInfo contentInfo = fileFolderService.create(container, "Content2", ContentModel.TYPE_CONTENT);
                    content2 = contentInfo.getNodeRef();

                    fileFolderService.delete(content1);

                    return null;
                }
            });

            setExpectedNodeStatus(container, NodeStatus.UPDATED);
            setExpectedNodeStatus(content1, NodeStatus.DELETED);
            setExpectedNodeStatus(content2, NodeStatus.UPDATED);
        }
    }
    
    private static class SOLRTest3 extends SOLRTest
    {
        private NodeRef container;
        private NodeRef content1;
        private NodeRef content2;

        SOLRTest3(RetryingTransactionHelper txnHelper, FileFolderService fileFolderService, NodeService nodeService,
                NodeRef rootNodeRef, String containerName, boolean doNodeChecks, boolean doMetaDataChecks)
        {
            super(txnHelper, fileFolderService, nodeService, rootNodeRef, containerName, doNodeChecks, doMetaDataChecks);
        }
        
        public int getExpectedNumNodes()
        {
            return 3;
        }

        protected void buildTransactionsInternal()
        {
            txnHelper.doInTransaction(new RetryingTransactionCallback<Void>()
            {
                public Void execute() throws Throwable
                {
                    PropertyMap props = new PropertyMap();
                    props.put(ContentModel.PROP_NAME, "Container1");
                    container = nodeService.createNode(
                            rootNodeRef,
                            ContentModel.ASSOC_CHILDREN,
                            ContentModel.ASSOC_CHILDREN,
                            ContentModel.TYPE_FOLDER,
                            props).getChildRef();
                    
                    FileInfo contentInfo = fileFolderService.create(container, "Content1", ContentModel.TYPE_CONTENT);
                    content1 = contentInfo.getNodeRef();
                    
                    Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>();
                    aspectProperties.put(ContentModel.PROP_AUTHOR, "steve");
                    nodeService.addAspect(content1, ContentModel.ASPECT_AUTHOR, aspectProperties);
                    
                    return null;
                }
            });
            
            txnHelper.doInTransaction(new RetryingTransactionCallback<Void>()
            {
                public Void execute() throws Throwable
                {
                    FileInfo contentInfo = fileFolderService.create(container, "Content2", ContentModel.TYPE_CONTENT);
                    content2 = contentInfo.getNodeRef();
                    
                    nodeService.addAspect(content2, ContentModel.ASPECT_TEMPORARY, null);
                    fileFolderService.delete(content1);

                    return null;
                }
            });

            setExpectedNodeStatus(container, NodeStatus.UPDATED);
            setExpectedNodeStatus(content1, NodeStatus.DELETED);
            setExpectedNodeStatus(content2, NodeStatus.UPDATED);
        }
    }
    
    private static class SOLRTest100Nodes extends SOLRTest
    {
        SOLRTest100Nodes(RetryingTransactionHelper txnHelper, FileFolderService fileFolderService, NodeService nodeService,
                NodeRef rootNodeRef, String containerName, boolean doNodeChecks, boolean doMetaDataChecks)
        {
            super(txnHelper, fileFolderService, nodeService, rootNodeRef, containerName, doNodeChecks, doMetaDataChecks);
        }
        
        public int getExpectedNumNodes()
        {
            return 100;
        }
        
        protected void buildTransactionsInternal()
        {
            txnHelper.doInTransaction(new RetryingTransactionCallback<Void>()
            {
                public Void execute() throws Throwable
                {
                    PropertyMap props = new PropertyMap();
                    props.put(ContentModel.PROP_NAME, "Container100Nodes");
                    NodeRef container = nodeService.createNode(
                            rootNodeRef,
                            ContentModel.ASSOC_CHILDREN,
                            ContentModel.ASSOC_CHILDREN,
                            ContentModel.TYPE_FOLDER,
                            props).getChildRef();
                    setExpectedNodeStatus(container, NodeStatus.UPDATED);

                    for(int i = 0; i < 99; i++)
                    {
                        FileInfo contentInfo = fileFolderService.create(container, "Content" + i, ContentModel.TYPE_CONTENT);
                        NodeRef nodeRef = contentInfo.getNodeRef();

                        setExpectedNodeStatus(nodeRef, NodeStatus.UPDATED);
                    }
                    
                    return null;
                }
            });
        }
    }

    private static class SOLRTest4 extends SOLRTest
    {
        private int numContentNodes = 2000;
        
        SOLRTest4(RetryingTransactionHelper txnHelper, FileFolderService fileFolderService, NodeService nodeService,
                NodeRef rootNodeRef, String containerName, boolean doNodeChecks, boolean doMetaDataChecks)
        {
            super(txnHelper, fileFolderService, nodeService, rootNodeRef, containerName, doNodeChecks, doMetaDataChecks);
        }
        
        public int getExpectedNumNodes()
        {
            return numContentNodes + 1;
        }

        public void buildTransactionsInternal()
        {
            txnHelper.doInTransaction(new RetryingTransactionCallback<Void>()
            {
                public Void execute() throws Throwable
                {
                    PropertyMap props = new PropertyMap();
                    props.put(ContentModel.PROP_NAME, containerName);
                    NodeRef container = nodeService.createNode(
                            rootNodeRef,
                            ContentModel.ASSOC_CHILDREN,
                            ContentModel.ASSOC_CHILDREN,
                            ContentModel.TYPE_FOLDER,
                            props).getChildRef();
                    setExpectedNodeStatus(container, NodeStatus.UPDATED);

                    for(int i = 0; i < numContentNodes; i++)
                    {
                        FileInfo contentInfo = fileFolderService.create(container, "Content" + i, ContentModel.TYPE_CONTENT);
                        NodeRef nodeRef = contentInfo.getNodeRef();

                        if(i % 2 == 1)
                        {
                            nodeService.addAspect(nodeRef, ContentModel.ASPECT_TEMPORARY, null);
                        }

                        setExpectedNodeStatus(nodeRef, NodeStatus.UPDATED);
                    }
                    
                    return null;
                }
            });
        }
    }

}
