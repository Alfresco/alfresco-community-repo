/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;

import javax.transaction.UserTransaction;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.Transaction;
import org.alfresco.repo.domain.node.ibatis.NodeDAOImpl;
import org.alfresco.repo.node.db.DeletedNodeCleanupWorker;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.testing.category.DBTests;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.GUID;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@Category({ OwnJVMTestsCategory.class, DBTests.class })
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class DeletedNodeBatchCleanupTest extends BaseSpringTest
{

    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private NodeDAO nodeDAO;
    @Autowired
    @Qualifier("node.nodesSharedCache")
    private SimpleCache<Serializable, Serializable> nodesCache;
    @Autowired
    private DeletedNodeCleanupWorker worker;
    @Autowired
    private NamespaceService namespaceService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private SearchService searchService;
    private RetryingTransactionHelper helper;
    private List<NodeRef> testNodes;

    @Before
    public void before()
    {
        helper = transactionService.getRetryingTransactionHelper();
        authenticationService.authenticate("admin", "admin".toCharArray());

        resetWorkerConfig();

        // create 5 test nodes
        final NodeRef companyHome = getCompanyHome();
        testNodes = IntStream.range(0, 5)
            .mapToObj(i -> helper.doInTransaction(createNodeCallback(companyHome), false, true))
            .collect(toList());

        // clean up pre-existing data
        helper.doInTransaction(() -> worker.doClean(), false, true);
    }

    private void resetWorkerConfig()
    {
        worker.setMinPurgeAgeDays(0);
        worker.setAlgorithm("V2");
        worker.setDeleteBatchSize(20);
    }

    private NodeRef getCompanyHome()
    {
        StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
        NodeRef storeRoot = nodeService.getRootNode(storeRef);
        List<NodeRef> nodeRefs = searchService.selectNodes(storeRoot, "/app:company_home", null, namespaceService,
            false);

        return nodeRefs.get(0);
    }

    private RetryingTransactionCallback<NodeRef> createNodeCallback(NodeRef companyHome)
    {
        return () -> nodeService.createNode(
            companyHome, ContentModel.ASSOC_CONTAINS, QName.createQName("test", GUID.generate()),
            ContentModel.TYPE_CONTENT).getChildRef();
    }

    private void deleteNodes(NodeRef nodeRef, NodeRef... additionalNodeRefs)
    {
        Stream.concat(of(nodeRef), of(additionalNodeRefs))
            .forEach(this::deleteNode);
    }

    private void deleteNode(NodeRef nodeRef)
    {
        helper.doInTransaction(new DeleteNode(nodeRef), false, true);
    }

    @Test
    public void testPurgeNodesDeleted()
    {
        final NodeRef nodeRef4 = getNode(4);
        final NodeRef nodeRef5 = getNode(5);

        // delete nodes 4 and 5
        deleteNodes(nodeRef4, nodeRef5);

        // double-check that node 4 and 5 are present in deleted form
        nodesCache.clear();
        assertTrue("Node 4 is deleted but not purged", nodeDAO.getNodeRefStatus(nodeRef4).isDeleted());
        assertTrue("Node 5 is deleted but not purged", nodeDAO.getNodeRefStatus(nodeRef5).isDeleted());

        worker.doClean();

        // verify that node 4 and 5 were purged
        nodesCache.clear();
        assertNull("Node 4 was not cleaned up", nodeDAO.getNodeRefStatus(nodeRef4));
        assertNull("Node 5 was not cleaned up", nodeDAO.getNodeRefStatus(nodeRef5));
    }

    @Test
    public void testNodesDeletedNotPurgedWhenNotAfterPurgeAge()
    {
        final NodeRef nodeRef1 = getNode(1);
        final NodeRef nodeRef2 = getNode(2);

        // delete nodes 1 and 2
        deleteNodes(nodeRef1, nodeRef2);

        // double-check that node 1 and 2 are present in deleted form
        nodesCache.clear();
        assertTrue("Node 1 is deleted but not purged", nodeDAO.getNodeRefStatus(nodeRef1).isDeleted());
        assertTrue("Node 2 is deleted but not purged", nodeDAO.getNodeRefStatus(nodeRef2).isDeleted());

        // run the worker
        worker.setMinPurgeAgeDays(1);
        worker.doClean();

        // verify that node 1 and 2 were not purged
        nodesCache.clear();
        assertNotNull("Node 1 was cleaned up", nodeDAO.getNodeRefStatus(nodeRef1));
        assertNotNull("Node 2 was cleaned up", nodeDAO.getNodeRefStatus(nodeRef2));
    }

    @Test
    public void testPurgeUnusedTransactions() throws Exception
    {
        // Execute transactions that update a number of nodes. For nodeRef1, all but the last txn will be unused.
        final long start = System.currentTimeMillis();
        final Long minTxnId = nodeDAO.getMinTxnId();

        final Map<NodeRef, List<String>> txnIds = createTransactions();
        final List<String> txnIds1 = txnIds.get(getNode(1));
        final List<String> txnIds2 = txnIds.get(getNode(2));
        final List<String> txnIds3 = txnIds.get(getNode(3));

        // Double-check that n4 and n5 are present in deleted form
        nodesCache.clear();
        UserTransaction txn = transactionService.getUserTransaction(true);
        txn.begin();
        try
        {
            assertTrue("Node 4 is deleted but not purged", nodeDAO.getNodeRefStatus(getNode(4)).isDeleted());
            assertTrue("Node 5 is deleted but not purged", nodeDAO.getNodeRefStatus(getNode(5)).isDeleted());
        }
        finally
        {
            txn.rollback();
        }

        // run the transaction cleaner
        worker.doClean();

        // Get transactions committed after the test started
        RetryingTransactionHelper.RetryingTransactionCallback<List<Transaction>> getTxnsCallback = () -> ((NodeDAOImpl) nodeDAO).selectTxns(
            start, Long.MAX_VALUE, Integer.MAX_VALUE, null, null, true);
        List<Transaction> txns = transactionService.getRetryingTransactionHelper()
            .doInTransaction(getTxnsCallback, true, false);

        List<String> expectedUnusedTxnIds = new ArrayList<>(10);
        expectedUnusedTxnIds.addAll(txnIds1.subList(0, txnIds1.size() - 1));

        List<String> expectedUsedTxnIds = new ArrayList<>(5);
        expectedUsedTxnIds.add(txnIds1.get(txnIds1.size() - 1));
        expectedUsedTxnIds.addAll(txnIds2);
        expectedUsedTxnIds.addAll(txnIds3);
        // 4 and 5 should not be in the list because they are deletes

        // check that the correct transactions have been purged i.e. all except the last one to update the node
        // i.e. in this case, all but the last one in txnIds1
        List<String> unusedTxnsNotPurged = expectedUnusedTxnIds.stream()
            .filter(txnId -> containsTransaction(txns, txnId))
            .collect(toList());
        if (!unusedTxnsNotPurged.isEmpty())
        {
            fail("Unused transaction(s) were not purged: " + unusedTxnsNotPurged);
        }

        long numFoundUnusedTxnIds = expectedUnusedTxnIds.stream()
            .filter(txnId -> !containsTransaction(txns, txnId))
            .count();
        assertEquals(9, numFoundUnusedTxnIds);

        // check that the correct transactions remain i.e. all those in txnIds2, txnIds3, txnIds4 and txnIds5
        long numFoundUsedTxnIds = expectedUsedTxnIds.stream()
            .filter(txnId -> containsTransaction(txns, txnId))
            .count();
        assertEquals(3, numFoundUsedTxnIds);

        // Get transactions committed after the test started
        RetryingTransactionHelper.RetryingTransactionCallback<List<Long>> getTxnsUnusedCallback = () -> nodeDAO.getTxnsUnused(
            minTxnId, Long.MAX_VALUE, Integer.MAX_VALUE);
        List<Long> txnsUnused = transactionService.getRetryingTransactionHelper()
            .doInTransaction(getTxnsUnusedCallback, true, false);
        assertEquals(0, txnsUnused.size());

        // Double-check that n4 and n5 were removed as well
        nodesCache.clear();
        assertNull("Node 4 was not cleaned up", nodeDAO.getNodeRefStatus(getNode(4)));
        assertNull("Node 5 was not cleaned up", nodeDAO.getNodeRefStatus(getNode(5)));
    }

    private boolean containsTransaction(List<Transaction> txns, String txnId)
    {
        return txns.stream()
            .map(Transaction::getChangeTxnId)
            .filter(changeTxnId -> changeTxnId.equals(txnId))
            .map(match -> true)
            .findFirst()
            .orElse(false);
    }

    private Map<NodeRef, List<String>> createTransactions()
    {
        Map<NodeRef, List<String>> txnIds = new HashMap<>();

        UpdateNode updateNode1 = new UpdateNode(getNode(1));
        UpdateNode updateNode2 = new UpdateNode(getNode(2));
        UpdateNode updateNode3 = new UpdateNode(getNode(3));
        DeleteNode deleteNode4 = new DeleteNode(getNode(4));
        DeleteNode deleteNode5 = new DeleteNode(getNode(5));

        List<String> txnIds1 = new ArrayList<>();
        List<String> txnIds2 = new ArrayList<>();
        List<String> txnIds3 = new ArrayList<>();
        List<String> txnIds4 = new ArrayList<>();
        List<String> txnIds5 = new ArrayList<>();
        txnIds.put(getNode(1), txnIds1);
        txnIds.put(getNode(2), txnIds2);
        txnIds.put(getNode(3), txnIds3);
        txnIds.put(getNode(4), txnIds4);
        txnIds.put(getNode(5), txnIds5);

        for (int i = 0; i < 10; i++)
        {
            String txnId1 = helper.doInTransaction(updateNode1, false, true);
            txnIds1.add(txnId1);
            if (i == 0)
            {
                String txnId2 = helper.doInTransaction(updateNode2, false, true);
                txnIds2.add(txnId2);
            }
            if (i == 1)
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

    private class UpdateNode implements RetryingTransactionHelper.RetryingTransactionCallback<String>
    {
        private final NodeRef nodeRef;

        UpdateNode(NodeRef nodeRef)
        {
            this.nodeRef = nodeRef;
        }

        @Override
        public String execute() throws Throwable
        {
            nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, GUID.generate());
            return AlfrescoTransactionSupport.getTransactionId();
        }
    }

    private class DeleteNode implements RetryingTransactionHelper.RetryingTransactionCallback<String>
    {
        private final NodeRef nodeRef;

        DeleteNode(NodeRef nodeRef)
        {
            this.nodeRef = nodeRef;
        }

        @Override
        public String execute() throws Throwable
        {
            nodeService.addAspect(nodeRef, ContentModel.ASPECT_TEMPORARY, null);
            nodeService.deleteNode(nodeRef);
            return AlfrescoTransactionSupport.getTransactionId();
        }
    }

    private NodeRef getNode(int i)
    {
        return testNodes.get(i - 1);
    }

}