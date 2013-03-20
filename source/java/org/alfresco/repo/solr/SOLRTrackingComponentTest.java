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
package org.alfresco.repo.solr;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.dictionary.M2Property;
import org.alfresco.repo.dictionary.M2Type;
import org.alfresco.repo.domain.node.Node;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.node.db.DbNodeServiceImpl;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.solr.SOLRTrackingComponent.NodeMetaDataQueryCallback;
import org.alfresco.repo.solr.SOLRTrackingComponent.NodeQueryCallback;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Tests tracking component
 *
 * @since 4.0
 */
public class SOLRTrackingComponentTest extends TestCase
{
    private static final Log logger = LogFactory.getLog(SOLRTrackingComponentTest.class);

    private ConfigurableApplicationContext ctx = (ConfigurableApplicationContext) ApplicationContextHelper.getApplicationContext();
    private static enum NodeStatus
    {
        UPDATED, DELETED;
    }

    private AuthenticationComponent authenticationComponent;
    private TransactionService transactionService;
    private DictionaryService dictionaryService;
    private NamespaceService namespaceService;
    private RetryingTransactionHelper txnHelper;
    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private NodeDAO nodeDAO;
    private QNameDAO qnameDAO;
    private DictionaryDAO dictionaryDAO;
    private SOLRTrackingComponent solrTrackingComponent;
    private DbNodeServiceImpl dbNodeService;

    private StoreRef storeRef;
    private NodeRef rootNodeRef;

    @Override
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        transactionService = serviceRegistry.getTransactionService();
        txnHelper = transactionService.getRetryingTransactionHelper();

        solrTrackingComponent = (SOLRTrackingComponent) ctx.getBean("solrTrackingComponent");
        nodeDAO = (NodeDAO)ctx.getBean("nodeDAO");
        qnameDAO = (QNameDAO) ctx.getBean("qnameDAO");
        dictionaryDAO =  (DictionaryDAO)ctx.getBean("dictionaryDAO");
        nodeService = (NodeService)ctx.getBean("NodeService");
        fileFolderService = (FileFolderService)ctx.getBean("FileFolderService");
        dictionaryService = serviceRegistry.getDictionaryService();
        namespaceService = serviceRegistry.getNamespaceService();
        authenticationComponent = (AuthenticationComponent)ctx.getBean("authenticationComponent");

        dbNodeService = (DbNodeServiceImpl)ctx.getBean("dbNodeService");
        dbNodeService.setEnableTimestampPropagation(false);

        authenticationComponent.setSystemUserAsCurrentUser();

        storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, getName() + System.currentTimeMillis());
        rootNodeRef = nodeService.getRootNode(storeRef);
    }

    public void testAclChnageSetLimits()
    {
        List<AclChangeSet> aclChangeSets = solrTrackingComponent.getAclChangeSets(null, null, null, null, 50);

        // First
        Long first = aclChangeSets.get(0).getId();
        Long firstTime = aclChangeSets.get(1).getId();
        List<AclChangeSet> testSets = solrTrackingComponent.getAclChangeSets(first, null, first, null, 50);
        assertEquals(0, testSets.size());
        testSets = solrTrackingComponent.getAclChangeSets(first, firstTime, first+1, firstTime, 50);
        assertEquals(0, testSets.size());
        testSets = solrTrackingComponent.getAclChangeSets(first, firstTime, first+1, firstTime+1, 50);
        assertEquals(0, testSets.size());

    }

    public void testGetAcls_Simple()
    {
        List<AclChangeSet> cs = solrTrackingComponent.getAclChangeSets(null, null, null, null, 50);
        assertTrue("Expected results to be limited in number", cs.size() <= 50);
        List<Long> aclChangeSetIds = new ArrayList<Long>(50);
        int totalAcls = 0;
        for (AclChangeSet aclChangeSet : cs)
        {
            aclChangeSetIds.add(aclChangeSet.getId());
            totalAcls += aclChangeSet.getAclCount();
        }
        int totalAclsCheck = 0;
        Long fromAclId = null;
        while (true)
        {
            List<Acl> acls = solrTrackingComponent.getAcls(aclChangeSetIds, fromAclId, 2);
            if (acls.size() == 0)
            {
                break;
            }
            totalAclsCheck += acls.size();
            fromAclId = acls.get(acls.size() - 1).getId() + 1;
        }
        // Double check number of ACLs
        assertEquals("ACL count should have matched", totalAcls, totalAclsCheck);
    }

    public void testGetNodeMetaData()
    {
        long startTime = System.currentTimeMillis();

        SOLRTest st = new SOLRTest3(txnHelper, fileFolderService, nodeDAO, qnameDAO, nodeService, dictionaryService, rootNodeRef, "testGetNodeMetaData", true, true);
        List<Long> createdTransactions = st.buildTransactions();

        List<Transaction> txns = solrTrackingComponent.getTransactions(null, startTime-1000, null, null, 100);

        int[] updates = new int[] {1, 1};
        int[] deletes = new int[] {0, 1};
        List<Transaction> checkedTransactions = checkTransactions(txns, createdTransactions, updates, deletes);

        NodeParameters nodeParameters = new NodeParameters();
        nodeParameters.setTransactionIds(getTransactionIds(checkedTransactions));
        getNodes(nodeParameters, st);

        NodeMetaDataParameters nodeMetaDataParams = new NodeMetaDataParameters();
        nodeMetaDataParams.setNodeIds(st.getNodeIds());
        getNodeMetaData(nodeMetaDataParams, null, st);
    }

    /**
     * @param checkedTransactions
     * @return
     */
    private List<Long> getTransactionIds(List<Transaction> checkedTransactions)
    {
        ArrayList<Long> txIds = new ArrayList<Long>(checkedTransactions.size());
        for(Transaction txn : checkedTransactions)
        {
            txIds.add(txn.getId());
        }
        return txIds;
    }

    public void testGetTransactionLimits()
    {
        long startTime = System.currentTimeMillis();

        SOLRTest st = new SOLRTest3(txnHelper, fileFolderService, nodeDAO, qnameDAO, nodeService, dictionaryService, rootNodeRef, "testGetNodeMetaData", true, true);
        List<Long> createdTransactions = st.buildTransactions();

        // All

        List<Transaction> txns = solrTrackingComponent.getTransactions(null, startTime-1000, null, null, 100);

        int[] updates = new int[] {1, 1};
        int[] deletes = new int[] {0, 1};
        List<Transaction> checkedTransactions  = checkTransactions(txns, createdTransactions, updates, deletes);

        Long first = checkedTransactions.get(0).getId();
        Long firstTime = checkedTransactions.get(0).getCommitTimeMs();
        Long last = checkedTransactions.get(1).getId();
        Long lastTime = checkedTransactions.get(1).getCommitTimeMs();

        // First

        txns = solrTrackingComponent.getTransactions(first, null, first, null, 50);
        assertEquals(0, txns.size());

        txns = solrTrackingComponent.getTransactions(first, null, first+1, null, 50);
        updates = new int[] {1};
        deletes = new int[] {0};
        List<Long> createdTransactionFirst = new ArrayList<Long>(1);
        createdTransactionFirst.add(createdTransactions.get(0));
        checkTransactions(txns, createdTransactionFirst, updates, deletes);

        txns = solrTrackingComponent.getTransactions(first, firstTime, first+1, firstTime+1, 50);
        checkTransactions(txns, createdTransactionFirst, updates, deletes);

        txns = solrTrackingComponent.getTransactions(first, firstTime-1, first+1, firstTime, 50);
        assertEquals(0, txns.size());

        // Last

        txns = solrTrackingComponent.getTransactions(last, null, last, null, 50);
        assertEquals(0, txns.size());

        txns = solrTrackingComponent.getTransactions(last, null, last+1, null, 50);
        updates = new int[] {1};
        deletes = new int[] {1};
        List<Long> createdTransactionLast = new ArrayList<Long>(1);
        createdTransactionLast.add(createdTransactions.get(1));
        checkTransactions(txns, createdTransactionLast, updates, deletes);


        txns = solrTrackingComponent.getTransactions(last, lastTime, last+1, lastTime+1, 50);
        checkTransactions(txns, createdTransactionLast, updates, deletes);

        txns = solrTrackingComponent.getTransactions(last, lastTime-1, last+1, lastTime, 50);
        assertEquals(0, txns.size());
    }

    public void testGetNodeMetaDataExludesResidualProperties()
    {
        long startTime = System.currentTimeMillis();

        SOLRTest st = new SOLRTestResidualProperties(txnHelper, fileFolderService, nodeDAO, qnameDAO, nodeService, dictionaryService, rootNodeRef, "testNodeMetaDataNullPropertyValue", true, true);
        List<Long> createdTransactions = st.buildTransactions();

        List<Transaction> txns = solrTrackingComponent.getTransactions(null, startTime-1000, null, null, 100);

        int[] updates = new int[] {2};
        int[] deletes = new int[] {0};
        List<Transaction> checkedTransactions = checkTransactions(txns, createdTransactions, updates, deletes);

        NodeParameters nodeParameters = new NodeParameters();
        nodeParameters.setTransactionIds(getTransactionIds(checkedTransactions));
        getNodes(nodeParameters, st);


        NodeMetaDataParameters nodeMetaDataParams = new NodeMetaDataParameters();
        nodeMetaDataParams.setNodeIds(st.getNodeIds());
        getNodeMetaData(nodeMetaDataParams, null, st);

    }

    public void testGetNodeMetaData100Nodes()
    {
        long startTime = System.currentTimeMillis();

        SOLRTest st = new SOLRTest100Nodes(txnHelper, fileFolderService, nodeDAO, qnameDAO, nodeService, dictionaryService, rootNodeRef, "testGetNodeMetaData", true, true);
        List<Long> createdTransactions = st.buildTransactions();

        List<Transaction> txns = solrTrackingComponent.getTransactions(null, startTime-1000, null, null, 100);

        int[] updates = new int[] {100};
        int[] deletes = new int[] {0};
        List<Transaction> checkedTransactions = checkTransactions(txns, createdTransactions, updates, deletes);

        NodeParameters nodeParameters = new NodeParameters();
        nodeParameters.setTransactionIds(getTransactionIds(checkedTransactions));
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

        SOLRTest st = new SOLRTest4(txnHelper, fileFolderService, nodeDAO, qnameDAO, nodeService, dictionaryService, rootNodeRef, "testNodeMetaDataManyNodes", true, false);
        List<Long> createdTransactions = st.buildTransactions();

        List<Transaction> txns = solrTrackingComponent.getTransactions(null, fromCommitTime-1000, null, null, 100);

        int[] updates = new int[] {2001};
        int[] deletes = new int[] {0};
        List<Transaction> checkedTransactions = checkTransactions(txns, createdTransactions, updates, deletes);

        NodeParameters nodeParameters = new NodeParameters();
        nodeParameters.setTransactionIds(getTransactionIds(checkedTransactions));
        getNodes(nodeParameters, st);

        // make sure caches are warm - time last call
        logger.debug("Cold cache");
        NodeMetaDataParameters nodeMetaDataParams = new NodeMetaDataParameters();
        nodeMetaDataParams.setNodeIds(st.getNodeIds());
        getNodeMetaData(nodeMetaDataParams, null, st);
        getNodeMetaData(nodeMetaDataParams, null, st);
        logger.debug("Warm cache");
        getNodeMetaData(nodeMetaDataParams, null, st);

        // clear out node caches
        nodeDAO.clear();

        logger.debug("Cold cache - explicit clear");
        nodeMetaDataParams.setMaxResults(800);
        getNodeMetaData(nodeMetaDataParams, null, st);
        getNodeMetaData(nodeMetaDataParams, null, st);
        logger.debug("Warm cache");        
        getNodeMetaData(nodeMetaDataParams, null, st);

        logger.debug("Cold cache - explicit clear");
        nodeMetaDataParams.setMaxResults(500);
        getNodeMetaData(nodeMetaDataParams, null, st);
        getNodeMetaData(nodeMetaDataParams, null, st);
        logger.debug("Warm cache");        
        getNodeMetaData(nodeMetaDataParams, null, st);

        logger.debug("Cold cache - explicit clear");
        nodeMetaDataParams.setMaxResults(200);
        getNodeMetaData(nodeMetaDataParams, null, st);
        getNodeMetaData(nodeMetaDataParams, null, st);
        logger.debug("Warm cache");        
        getNodeMetaData(nodeMetaDataParams, null, st);

        // clear out node caches
        nodeDAO.clear();

        logger.debug("Cold cache - explicit clear");
        getNodeMetaData(nodeMetaDataParams, null, st);
    }

    public void testNodeMetaDataCache() throws Exception
    {
        long fromCommitTime = System.currentTimeMillis();

        SOLRTest st = new SOLRTest4(txnHelper, fileFolderService, nodeDAO, qnameDAO, nodeService, dictionaryService, rootNodeRef, "testNodeMetaDataManyNodes", true, false);
        List<Long> createdTransactions = st.buildTransactions();

        List<Transaction> txns = solrTrackingComponent.getTransactions(null, fromCommitTime-1000, null, null, 100);

        int[] updates = new int[] {2001};
        int[] deletes = new int[] {0};
        List<Transaction> checkedTransactions = checkTransactions(txns, createdTransactions, updates, deletes);

        NodeParameters nodeParameters = new NodeParameters();
        nodeParameters.setTransactionIds(getTransactionIds(checkedTransactions));
        getNodes(nodeParameters, st);

        // clear out node caches
        nodeDAO.clear();

        logger.debug("Cold cache - explicit clear");
        NodeMetaDataParameters nodeMetaDataParams = new NodeMetaDataParameters();
        nodeMetaDataParams.setNodeIds(st.getNodeIds());
        MetaDataResultsFilter filter = new MetaDataResultsFilter();
        filter.setIncludeParentAssociations(false);
        //filter.setIncludePaths(false);
        filter.setIncludeChildAssociations(false);
        getNodeMetaData(nodeMetaDataParams, filter, st);
    }

    public void testNodeMetaDataNullPropertyValue() throws Exception
    {
        long fromCommitTime = System.currentTimeMillis();

        SOLRTest st = new SOLRTest5(txnHelper, fileFolderService, nodeDAO, qnameDAO, nodeService, dictionaryService, rootNodeRef, "testNodeMetaDataNullPropertyValue", true, true);
        List<Long> createdTransactions = st.buildTransactions();

        List<Transaction> txns = solrTrackingComponent.getTransactions(null, fromCommitTime-1000, null, null, 100);

        int[] updates = new int[] {11};
        int[] deletes = new int[] {0};
        List<Transaction> checkedTransactions = checkTransactions(txns, createdTransactions, updates, deletes);

        NodeParameters nodeParameters = new NodeParameters();
        nodeParameters.setTransactionIds(getTransactionIds(checkedTransactions));
        getNodes(nodeParameters, st);

        NodeMetaDataParameters nodeMetaDataParams = new NodeMetaDataParameters();
        nodeMetaDataParams.setNodeIds(st.getNodeIds());
        getNodeMetaData(nodeMetaDataParams, null, st);
    }

    public void testFilters()
    {
        long startTime = System.currentTimeMillis();

        SOLRTest st = new SOLRTest1(txnHelper, fileFolderService, nodeDAO, qnameDAO, nodeService, dictionaryService, rootNodeRef, "testFilters", true, true);
        List<Long> createdTransactions = st.buildTransactions();

        List<Transaction> txns = solrTrackingComponent.getTransactions(null, startTime-1000, null, null, 100);

        int[] updates = new int[] {1, 1};
        int[] deletes = new int[] {0, 1};
        List<Transaction> checkedTransactions = checkTransactions(txns, createdTransactions, updates, deletes);

        NodeParameters nodeParameters = new NodeParameters();
        nodeParameters.setTransactionIds(getTransactionIds(checkedTransactions));
        getNodes(nodeParameters, st);

        NodeMetaDataParameters nodeMetaDataParams = new NodeMetaDataParameters();
        nodeMetaDataParams.setNodeIds(st.getNodeIds());
        getNodeMetaData(nodeMetaDataParams, null, st);
    }

    public void testModelDiffs()
    {
        Collection<QName> allModels = dictionaryService.getAllModels();

        ModelDiffsTracker tracker = new ModelDiffsTracker();
        ModelDiffResults diffResults = tracker.diff();

        // number of diffs should equal the number of models in the repository
        assertEquals("Unexpected number of new models", allModels.size(), diffResults.getNewModels().size());
        assertEquals("Expected no removed models", 0, diffResults.getRemovedModels().size());
        assertEquals("Expected no changed models", 0, diffResults.getChangedModels().size());

        // create a new model
        InputStream modelStream = getClass().getClassLoader().getResourceAsStream("org/alfresco/repo/solr/testModel.xml");
        M2Model testModel = M2Model.createModel(modelStream);
        dictionaryDAO.putModel(testModel);

        // call model diffs - should detect new model
        ModelDiffResults diffResults1 = tracker.diff();
        assertEquals("Expected 1 new model", 1, diffResults1.getNewModels().size());
        assertEquals("Unexpected number of changed models", 0, diffResults1.getChangedModels().size());
        assertEquals("Unexpected number of removed models", 0, diffResults1.getRemovedModels().size());
        AlfrescoModelDiff diff = diffResults1.getNewModels().get(0);
        assertEquals("Unexpected model name change", QName.createQName(testModel.getName(), namespaceService).toString(), diff.getModelName());

        // get current checksum for the test model
        Long testModelChecksum = tracker.getChecksum(QName.createQName(testModel.getName(), namespaceService));
        assertNotNull("", testModelChecksum);

        // create a new type and add it to the new test model
        M2Type anotherType = testModel.createType("anothertype");
        M2Property prop1 = anotherType.createProperty("prop1");
        prop1.setType("d:text");

        // call model diffs - should detect test model changes
        ModelDiffResults diffResults2 = tracker.diff();
        List<AlfrescoModelDiff> changedModels = diffResults2.getChangedModels();
        assertEquals("Expected no new models", 0, diffResults2.getNewModels().size());
        assertEquals("Expected no removed models", 0, diffResults2.getRemovedModels().size());
        assertEquals("Expected detection of changed testmodel", 1, changedModels.size());

        AlfrescoModelDiff changedModel = changedModels.get(0);
        assertEquals("Unexpected changed model name", QName.createQName(testModel.getName(), namespaceService).toString(),
                changedModel.getModelName());
        assertNotNull("", changedModel.getOldChecksum().longValue());
        assertEquals("Old checksum value is incorrect", testModelChecksum.longValue(), changedModel.getOldChecksum().longValue());
        assertNotSame("Expected checksums to be different", changedModel.getOldChecksum(), changedModel.getNewChecksum());

        // remove the model        
        dictionaryDAO.removeModel(QName.createQName(testModel.getName(), namespaceService));

        // call model diffs - check that the model has been removed
        ModelDiffResults diffResults3 = tracker.diff();
        List<AlfrescoModelDiff> removedModels = diffResults3.getRemovedModels();
        assertEquals("Expected 1 removed model", 1, removedModels.size());
        QName removedModelName = QName.createQName(removedModels.get(0).getModelName());
        String removedModelNamespace = removedModelName.getNamespaceURI();
        String removedModelLocalName = removedModelName.getLocalName();
        assertEquals("Removed model namespace is incorrect", "http://www.alfresco.org/model/solrtest/1.0", removedModelNamespace);
        assertEquals("Removed model name is incorrect", "contentmodel", removedModelLocalName);
        assertEquals("Expected no new models", 0, diffResults3.getNewModels().size());
        assertEquals("Expected no changed modeks", 0, diffResults3.getChangedModels().size());
    }

    private static class ModelDiffResults
    {
        private List<AlfrescoModelDiff> newModels;
        private List<AlfrescoModelDiff> changedModels;
        private List<AlfrescoModelDiff> removedModels;

        public ModelDiffResults(List<AlfrescoModelDiff> newModels, List<AlfrescoModelDiff> changedModels, List<AlfrescoModelDiff> removedModels)
        {
            super();
            this.newModels = newModels;
            this.changedModels = changedModels;
            this.removedModels = removedModels;
        }

        public List<AlfrescoModelDiff> getNewModels()
        {
            return newModels;
        }

        public List<AlfrescoModelDiff> getChangedModels()
        {
            return changedModels;
        }

        public List<AlfrescoModelDiff> getRemovedModels()
        {
            return removedModels;
        }
    }

    private class ModelDiffsTracker
    {
        private Map<QName, Long> trackedModels = new HashMap<QName, Long>();

        public ModelDiffResults diff()
        {
            List<AlfrescoModelDiff> modelDiffs = solrTrackingComponent.getModelDiffs(trackedModels);
            List<AlfrescoModelDiff> newModels = new ArrayList<AlfrescoModelDiff>();
            List<AlfrescoModelDiff> changedModels = new ArrayList<AlfrescoModelDiff>();
            List<AlfrescoModelDiff> removedModels = new ArrayList<AlfrescoModelDiff>();

            for(AlfrescoModelDiff diff : modelDiffs)
            {
                if(diff.getType().equals(AlfrescoModelDiff.TYPE.NEW))
                {
                    newModels.add(diff);
                    trackedModels.put(QName.createQName(diff.getModelName()), diff.getNewChecksum());
                }
                else if(diff.getType().equals(AlfrescoModelDiff.TYPE.CHANGED))
                {
                    changedModels.add(diff);
                }
                else if(diff.getType().equals(AlfrescoModelDiff.TYPE.REMOVED))
                {
                    removedModels.add(diff);
                }
            }

            return new ModelDiffResults(newModels, changedModels, removedModels);
        }

        public Long getChecksum(QName modelName)
        {
            return trackedModels.get(modelName);
        }
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

        public boolean isExpectOwner()
        {
            return expectOwner;
        }

        public boolean isExpectAssociations()
        {
            return expectAssociations;
        }

        public boolean isExpectPaths()
        {
            return expectPaths;
        }

        public boolean isExpectAclId()
        {
            return expectAclId;
        }

        public boolean isExpectAspects()
        {
            return expectAspects;
        }

        public boolean isExpectProperties()
        {
            return expectProperties;
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

    private List<Transaction> checkTransactions(List<Transaction> txns, List<Long> createdTransaction, int[] updates, int[] deletes)
    {
        ArrayList<Transaction> matchedTransactions = new ArrayList<Transaction>();
        HashSet<Long> toMatch = new HashSet<Long>();
        toMatch.addAll(createdTransaction);
        for(Transaction found : txns)
        {
            if(found != null)
            {
                if(toMatch.contains(found.getId()))
                {
                    matchedTransactions.add(found);
                }
            }
        }

        assertEquals("Number of transactions is incorrect", createdTransaction.size(), matchedTransactions.size());

        int i = 0;
        for(Transaction txn : matchedTransactions)
        {
            assertEquals("Number of deletes is incorrect", deletes[i], txn.getDeletes());
            assertEquals("Number of updates is incorrect", updates[i], txn.getUpdates());
            i++;
        }

        return matchedTransactions;
    }

    private void getNodes(NodeParameters nodeParameters, SOLRTest bt)
    {
        long startTime = System.currentTimeMillis();
        solrTrackingComponent.getNodes(nodeParameters, bt);        
        long endTime = System.currentTimeMillis();

        bt.runNodeChecks(nodeParameters.getMaxResults());

        logger.debug("Got " + bt.getActualNodeCount() + " nodes in " + (endTime - startTime) + " ms");
    }

    private void getNodeMetaData(final NodeMetaDataParameters params, final MetaDataResultsFilter filter, final SOLRTest bt)
    {
        bt.clearNodesMetaData();

        long startTime = System.currentTimeMillis();
        txnHelper.doInTransaction(new RetryingTransactionCallback<Void>()
                {
            @Override
            public Void execute() throws Throwable
            {
                solrTrackingComponent.getNodesMetadata(params, filter, bt);
                return null;
            }
                }, true, true);
        long endTime = System.currentTimeMillis();

        bt.runNodeMetaDataChecks(params.getMaxResults());

        logger.debug("Got " + bt.getActualNodeMetaDataCount() + " node metadatas in " + (endTime - startTime) + " ms");
    }

    private static abstract class SOLRTest implements NodeQueryCallback, NodeMetaDataQueryCallback
    {
        protected FileFolderService fileFolderService;
        protected RetryingTransactionHelper txnHelper;
        protected NodeService nodeService;
        protected NodeRef rootNodeRef;
        protected NodeDAO nodeDAO;
        protected QNameDAO qnameDAO;
        protected DictionaryService dictionaryService;

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

        SOLRTest(
                RetryingTransactionHelper txnHelper, FileFolderService fileFolderService,
                NodeDAO nodeDAO, QNameDAO qnameDAO, NodeService nodeService, DictionaryService dictionaryService,
                NodeRef rootNodeRef, String containerName, boolean doNodeChecks, boolean doMetaDataChecks)
                {
            this.txnHelper = txnHelper;
            this.nodeService = nodeService;
            this.rootNodeRef = rootNodeRef;
            this.fileFolderService = fileFolderService;
            this.nodeDAO = nodeDAO;
            this.qnameDAO = qnameDAO;
            this.dictionaryService = dictionaryService;

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
        protected abstract List<Long> buildTransactionsInternal();

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
            
            /*
            if(nodeStatus == NodeStatus.DELETED)
            {
                expectedNumMetaDataNodes++;
            }
            */
            
            if(doChecks)
            {
                NodeAssertions nodeAssertions = getNodeAssertions(nodeRef);
                nodeAssertions.setNodeStatus(nodeStatus);
            }
        }

        List<Long> buildTransactions()
        {
            return buildTransactionsInternal();
        }

        @Override
        public boolean handleNode(Node node)
        {
            actualNodeCount++;

            if(doNodeChecks)
            {
                NodeRef nodeRef = node.getNodeRef();
                Boolean isDeleted = node.getDeleted(qnameDAO);
                nodeIds.add(node.getId());

                NodeAssertions expectedStatus = getNodeAssertions(nodeRef);
                if(expectedStatus == null)
                {
                    throw new RuntimeException("Unexpected missing assertion for NodeRef " + nodeRef);
                }

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

        private Map<QName, Serializable> filterResudualProperties(Map<QName, Serializable>  sourceProps)
        {
            Map<QName, Serializable>  props = new HashMap<QName, Serializable>((int)(sourceProps.size() * 1.3));
            for(QName propertyQName : sourceProps.keySet())
            {
                PropertyDefinition propDef = dictionaryService.getProperty(propertyQName);
                if(propDef != null)
                {
                    props.put(propertyQName, sourceProps.get(propertyQName));
                }
            }
            return props;
        }

        @Override
        public boolean handleNodeMetaData(NodeMetaData nodeMetaData) {
            actualNodeMetaDataCount++;

            if(doMetaDataChecks)
            {
                Long nodeId = nodeMetaData.getNodeId();
                NodeRef nodeRef = nodeMetaData.getNodeRef();

                if(nodeService.exists(nodeRef))
                {

                    Set<QName> aspects = nodeMetaData.getAspects();
                    Set<QName> actualAspects = nodeService.getAspects(nodeRef);
                    assertEquals("Aspects are incorrect", actualAspects, aspects);

                    Map<QName, Serializable> properties = nodeMetaData.getProperties();
                    // NodeService converts properties so use nodeDAO to get unadulterated property value
                    Map<QName, Serializable> actualProperties = filterResudualProperties(nodeDAO.getNodeProperties(nodeId));
                    //assertTrue("Properties are incorrect", compareProperties(actualProperties, properties));
                    assertEquals("Properties are incorrect", actualProperties, properties);

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

                    // TODO complete path tests
                    //                List<Path> actualPaths = nodeMetaData.getPaths();
                    //                List<Path> expectedPaths = nodeService.getPaths(nodeRef, false);
                    //                assertEquals("Paths are incorrect", expectedPaths, actualPaths);

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

        SOLRTest1(
                RetryingTransactionHelper txnHelper, FileFolderService fileFolderService,
                NodeDAO nodeDAO, QNameDAO qnameDAO, NodeService nodeService, DictionaryService dictionaryService,
                NodeRef rootNodeRef, String containerName, boolean doNodeChecks, boolean doMetaDataChecks)
                {
            super(txnHelper, fileFolderService, nodeDAO, qnameDAO, nodeService, dictionaryService, rootNodeRef, containerName, doNodeChecks, doMetaDataChecks);
                }

        public int getExpectedNumNodes()
        {
            return 3;
        }

        protected List<Long> buildTransactionsInternal()
        {
            ArrayList<Long> txs = new ArrayList<Long>(2);

            txs.add(txnHelper.doInTransaction(new RetryingTransactionCallback<Long>()
                    {
                public Long execute() throws Throwable
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

                    return nodeDAO.getNodeRefStatus(content1).getDbTxnId();
                }
                    }));

            txs.add(txnHelper.doInTransaction(new RetryingTransactionCallback<Long>()
                    {
                public Long execute() throws Throwable
                {
                    FileInfo contentInfo = fileFolderService.create(container, "Content2", ContentModel.TYPE_CONTENT);
                    content2 = contentInfo.getNodeRef();

                    fileFolderService.delete(content1);

                    return nodeDAO.getNodeRefStatus(content1).getDbTxnId();
                }
                    }));

            setExpectedNodeStatus(container, NodeStatus.UPDATED);
            setExpectedNodeStatus(content1, NodeStatus.DELETED);
            setExpectedNodeStatus(content2, NodeStatus.UPDATED);
            return txs;
        }
    }

    private static class SOLRTest3 extends SOLRTest
    {
        private NodeRef container;
        private NodeRef content1;
        private NodeRef content2;

        SOLRTest3(
                RetryingTransactionHelper txnHelper, FileFolderService fileFolderService,
                NodeDAO nodeDAO, QNameDAO qnameDAO, NodeService nodeService, DictionaryService dictionaryService, 
                NodeRef rootNodeRef, String containerName, boolean doNodeChecks, boolean doMetaDataChecks)
                {
            super(txnHelper, fileFolderService, nodeDAO, qnameDAO, nodeService, dictionaryService, rootNodeRef, containerName, doNodeChecks, doMetaDataChecks);
                }

        public int getExpectedNumNodes()
        {
            return 3;
        }

        protected List<Long> buildTransactionsInternal()
        {
            ArrayList<Long> txs = new ArrayList<Long>(2);

            txs.add(txnHelper.doInTransaction(new RetryingTransactionCallback<Long>()
                    {
                public Long execute() throws Throwable
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

                    return nodeDAO.getNodeRefStatus(content1).getDbTxnId();
                }
                    }));

            txs.add(txnHelper.doInTransaction(new RetryingTransactionCallback<Long>()
                    {
                public Long execute() throws Throwable
                {
                    FileInfo contentInfo = fileFolderService.create(container, "Content2", ContentModel.TYPE_CONTENT);
                    content2 = contentInfo.getNodeRef();

                    nodeService.addAspect(content2, ContentModel.ASPECT_TEMPORARY, null);
                    fileFolderService.delete(content1);

                    return nodeDAO.getNodeRefStatus(content1).getDbTxnId();
                }
                    }));

            setExpectedNodeStatus(container, NodeStatus.UPDATED);
            setExpectedNodeStatus(content1, NodeStatus.DELETED);
            setExpectedNodeStatus(content2, NodeStatus.UPDATED);

            return txs;
        }
    }

    private static class SOLRTest100Nodes extends SOLRTest
    {
        SOLRTest100Nodes(
                RetryingTransactionHelper txnHelper, FileFolderService fileFolderService,
                NodeDAO nodeDAO, QNameDAO qnameDAO, NodeService nodeService, DictionaryService dictionaryService,
                NodeRef rootNodeRef, String containerName, boolean doNodeChecks, boolean doMetaDataChecks)
                {
            super(txnHelper, fileFolderService, nodeDAO, qnameDAO, nodeService, dictionaryService, rootNodeRef, containerName, doNodeChecks, doMetaDataChecks);
                }

        public int getExpectedNumNodes()
        {
            return 100;
        }

        protected List<Long> buildTransactionsInternal()
        {
            ArrayList<Long> txs = new ArrayList<Long>(2);

            txs.add(txnHelper.doInTransaction(new RetryingTransactionCallback<Long>()
                    {
                public Long execute() throws Throwable
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

                    return nodeDAO.getNodeRefStatus(container).getDbTxnId();
                }
                    }));
            return txs;
        }
    }

    private static class SOLRTest4 extends SOLRTest
    {
        private int numContentNodes = 2000;

        SOLRTest4(
                RetryingTransactionHelper txnHelper, FileFolderService fileFolderService,
                NodeDAO nodeDAO, QNameDAO qnameDAO, NodeService nodeService, DictionaryService dictionaryService,
                NodeRef rootNodeRef, String containerName, boolean doNodeChecks, boolean doMetaDataChecks)
                {
            super(txnHelper, fileFolderService, nodeDAO, qnameDAO, nodeService, dictionaryService, rootNodeRef, containerName, doNodeChecks, doMetaDataChecks);
                }

        public int getExpectedNumNodes()
        {
            return numContentNodes + 1;
        }

        public List<Long> buildTransactionsInternal()
        {
            ArrayList<Long> txs = new ArrayList<Long>(2);

            txs.add(txnHelper.doInTransaction(new RetryingTransactionCallback<Long>()
                    {
                public Long execute() throws Throwable
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
                        nodeService.setProperty(nodeRef, ContentModel.PROP_AUTHOR, null);

                        setExpectedNodeStatus(nodeRef, NodeStatus.UPDATED);
                    }

                    return nodeDAO.getNodeRefStatus(container).getDbTxnId();
                }
                    }));
            return txs;
        }

    }

    private static class SOLRTest5 extends SOLRTest
    {
        private int numContentNodes = 10;

        SOLRTest5(
                RetryingTransactionHelper txnHelper, FileFolderService fileFolderService,
                NodeDAO nodeDAO, QNameDAO qnameDAO, NodeService nodeService, DictionaryService dictionaryService,
                NodeRef rootNodeRef, String containerName, boolean doNodeChecks, boolean doMetaDataChecks)
                {
            super(txnHelper, fileFolderService, nodeDAO, qnameDAO, nodeService, dictionaryService, rootNodeRef, containerName, doNodeChecks, doMetaDataChecks);
                }

        public int getExpectedNumNodes()
        {
            return numContentNodes + 1;
        }

        public  List<Long> buildTransactionsInternal()
        {
            ArrayList<Long> txs = new ArrayList<Long>(2);

            final String titles[] = 
            {
                    "caf\u00E9", "\u00E7edilla", "\u00E0\u00E1\u00E2\u00E3", "\u00EC\u00ED\u00EE\u00EF", "\u00F0\u00F1\u00F2\u00F3\u00F4\u00F5\u00F6",
                    "caf\u00E9", "\u00E7edilla", "\u00E0\u00E1\u00E2\u00E3", "\u00EC\u00ED\u00EE\u00EF", "\u00F0\u00F1\u00F2\u00F3\u00F4\u00F5\u00F6"
            };
            txs.add(txnHelper.doInTransaction(new RetryingTransactionCallback<Long>()
                    {
                public Long execute() throws Throwable
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

                        nodeService.addAspect(nodeRef, ContentModel.ASPECT_AUTHOR, null);
                        if(i % 5 == 1)
                        {
                            nodeService.setProperty(nodeRef, ContentModel.PROP_AUTHOR, null);
                        }
                        else
                        {
                            nodeService.setProperty(nodeRef, ContentModel.PROP_AUTHOR, "author" + i);
                        }

                        nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, titles[i]);

                        setExpectedNodeStatus(nodeRef, NodeStatus.UPDATED);
                    }

                    return nodeDAO.getNodeRefStatus(container).getDbTxnId();
                }
                    }));

            return txs;
        }
    }

    private static class SOLRTestResidualProperties extends SOLRTest
    {
        private NodeRef container;
        private NodeRef content;

        SOLRTestResidualProperties(
                RetryingTransactionHelper txnHelper, FileFolderService fileFolderService,
                NodeDAO nodeDAO, QNameDAO qnameDAO, NodeService nodeService, DictionaryService dictionaryService,
                NodeRef rootNodeRef, String containerName, boolean doNodeChecks, boolean doMetaDataChecks)
                {
            super(txnHelper, fileFolderService, nodeDAO, qnameDAO, nodeService, dictionaryService,rootNodeRef, containerName, doNodeChecks, doMetaDataChecks);
                }

        public int getExpectedNumNodes()
        {
            return 2;
        }

        protected List<Long> buildTransactionsInternal()
        {
            ArrayList<Long> txs = new ArrayList<Long>(2);

            txs.add(txnHelper.doInTransaction(new RetryingTransactionCallback<Long>()
                    {
                public Long execute() throws Throwable
                {
                    PropertyMap props = new PropertyMap();
                    props.put(ContentModel.PROP_NAME, "ContainerResidual");
                    container = nodeService.createNode(
                            rootNodeRef,
                            ContentModel.ASSOC_CHILDREN,
                            ContentModel.ASSOC_CHILDREN,
                            ContentModel.TYPE_FOLDER,
                            props).getChildRef();

                    FileInfo contentInfo = fileFolderService.create(container, "ContentResidual", ContentModel.TYPE_CONTENT);
                    content = contentInfo.getNodeRef();

                    nodeService.setProperty(content, QName.createQName("{rubbish}rubbish"), "Rubbish");

                    return nodeDAO.getNodeRefStatus(container).getDbTxnId();
                }
                    }));

            setExpectedNodeStatus(container, NodeStatus.UPDATED);
            setExpectedNodeStatus(content, NodeStatus.UPDATED);

            return txs;
        }
    }
}
