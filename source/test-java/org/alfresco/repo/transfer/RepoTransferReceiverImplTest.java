/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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
package org.alfresco.repo.transfer;

import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transfer.manifest.TransferManifestDeletedNode;
import org.alfresco.repo.transfer.manifest.TransferManifestHeader;
import org.alfresco.repo.transfer.manifest.TransferManifestNode;
import org.alfresco.repo.transfer.manifest.TransferManifestNormalNode;
import org.alfresco.repo.transfer.manifest.XMLTransferManifestWriter;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.transfer.TransferException;
import org.alfresco.service.cmr.transfer.TransferProgress;
import org.alfresco.service.cmr.transfer.TransferServicePolicies;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.BaseSpringTestsCategory;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.experimental.categories.Category;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Unit test for RepoTransferReceiverImpl
 * 
 * @author Brian Remmington
 */
@SuppressWarnings("deprecation")
@Category(BaseSpringTestsCategory.class)
public class RepoTransferReceiverImplTest extends BaseAlfrescoSpringTest
{
    private static int fileCount = 0;
    private static final Log log = LogFactory.getLog(RepoTransferReceiverImplTest.class);

    private RepoTransferReceiverImpl receiver;
    private SearchService searchService;
    private String dummyContent;
    private byte[] dummyContentBytes;
    private NodeRef guestHome;
    private PolicyComponent policyComponent;
    
    @Override
    public void runBare() throws Throwable
    {
        preventTransaction();
        super.runBare();
    }

    /**
     * Called during the transaction setup
     */
    protected void onSetUp() throws Exception
    {
        super.onSetUp();
        System.out.println("java.io.tmpdir == " + System.getProperty("java.io.tmpdir"));

        // Get the required services
        this.nodeService = (NodeService) this.applicationContext.getBean("nodeService");
        this.contentService = (ContentService) this.applicationContext.getBean("contentService");
        this.authenticationService = (MutableAuthenticationService) this.applicationContext
                .getBean("authenticationService");
        this.actionService = (ActionService) this.applicationContext.getBean("actionService");
        this.transactionService = (TransactionService) this.applicationContext.getBean("transactionComponent");
        this.authenticationComponent = (AuthenticationComponent) this.applicationContext
                .getBean("authenticationComponent");
        this.receiver = (RepoTransferReceiverImpl) this.getApplicationContext().getBean("transferReceiver");
        this.policyComponent = (PolicyComponent) this.getApplicationContext().getBean("policyComponent");
        this.searchService = (SearchService) this.getApplicationContext().getBean("searchService");
        this.dummyContent = "This is some dummy content.";        
        this.dummyContentBytes = dummyContent.getBytes("UTF-8");
        setTransactionDefinition(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        authenticationComponent.setSystemUserAsCurrentUser();

        startNewTransaction();
        String guestHomeQuery = "/app:company_home/app:guest_home";
        ResultSet guestHomeResult = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH, guestHomeQuery);
        assertEquals("", 1, guestHomeResult.length());
        guestHome = guestHomeResult.getNodeRef(0);
        endTransaction();
    }

    public void testDelete()
    {
        log.debug("start testDelete");
        
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
        final String uuid = GUID.generate();
        class TestContext 
        {
            ChildAssociationRef childAssoc;
        };
        
        RetryingTransactionCallback<TestContext> setupCB = new RetryingTransactionCallback<TestContext>()
        {

            @Override
            public TestContext execute() throws Throwable
            {
                TestContext tc = new TestContext();
                ResultSet rs = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH, 
                        "/app:company_home");
                assertEquals(1, rs.length());
                NodeRef companyHome = rs.getNodeRef(0);
                Map<QName, Serializable> props = new HashMap<QName, Serializable>();
                props.put(ContentModel.PROP_NAME, uuid);
                tc.childAssoc = nodeService.createNode(companyHome, ContentModel.ASSOC_CONTAINS, 
                        QName.createQName(NamespaceService.APP_MODEL_1_0_URI, uuid), ContentModel.TYPE_CONTENT, props);
                return tc;
            }
        };
        
        final TestContext tc = tran.doInTransaction(setupCB, false, true);
        
        RetryingTransactionCallback<Void> deleteCB = new RetryingTransactionCallback<Void>()
        {

            @Override
            public Void execute() throws Throwable
            {
                nodeService.deleteNode(tc.childAssoc.getChildRef());
                return null;
            }
        };
        
        tran.doInTransaction(deleteCB, false, true);
        
        RetryingTransactionCallback<Void> validateCB = new RetryingTransactionCallback<Void>()
        {

            @Override
            public Void execute() throws Throwable
            {
                log.debug("Test that original node no longer exists...");
                assertFalse(nodeService.exists(tc.childAssoc.getChildRef()));
                log.debug("PASS - Original node no longer exists.");
                NodeRef archiveNodeRef = new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, tc.childAssoc.getChildRef().getId());
                log.debug("Test that archive node exists...");
                assertTrue(nodeService.exists(archiveNodeRef));
                log.debug("PASS - Archive node exists.");
                return null;
            }
        };
        
        tran.doInTransaction(validateCB, false, true);
        
    }
    
    /**
     * Tests start and end with regard to locking.
     * @throws Exception
     */
    public void DISABLED_testStartAndEnd() throws Exception
    {
        log.debug("start testStartAndEnd");
        
        RetryingTransactionHelper trx = transactionService.getRetryingTransactionHelper();
       
        RetryingTransactionCallback<Void> cb = new RetryingTransactionCallback<Void>()
        {
            
            @Override
            public Void execute() throws Throwable
            {
                log.debug("about to call start");
                String transferId = receiver.start("1234", true, receiver.getVersion());
                
                File stagingFolder = null;
                try
                {
                    System.out.println("TransferId == " + transferId);

                    stagingFolder = receiver.getStagingFolder(transferId);
                    assertTrue(receiver.getStagingFolder(transferId).exists());
                    NodeRef tempFolder = receiver.getTempFolder(transferId);
                    assertNotNull("tempFolder is null", tempFolder);

                    Thread.sleep(1000);
                    try
                    {
                        receiver.start("1234", true, receiver.getVersion());
                        fail("Successfully started twice!");
                    }
                    catch (TransferException ex)
                    {
                        // Expected
                    }
                
                    Thread.sleep(300);
                    try
                    {
                        receiver.start("1234", true, receiver.getVersion());
                        fail("Successfully started twice!");
                    }
                    catch (TransferException ex)
                    {
                        // Expected
                    }
                
                    try
                    {
                        receiver.end(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, GUID.generate()).toString());
//                      fail("Successfully ended with transfer id that doesn't own lock.");
                    }
                    catch (TransferException ex)
                    {
                        // Expected
                    }
                } 
                finally
                {
                    log.debug("about to call end");
                    receiver.end(transferId);
                    
                    /**
                     * Check clean-up
                     */
                    if(stagingFolder != null)
                    {
                        assertFalse(stagingFolder.exists());
                    }
                }
       
                return null;
            }
        };
        
        long oldRefreshTime = receiver.getLockRefreshTime();  
        try
        {
            receiver.setLockRefreshTime(500);
        
            for (int i = 0; i < 5; i++)
            {
                log.info("test iteration:" + i);
                
                trx.doInTransaction(cb, false, true);
            }
        }
        finally
        {
            receiver.setLockRefreshTime(oldRefreshTime);
        }
    }
    
    /**
     * Tests start and end with regard to locking.
     * 
     * Going to cut down the timeout to a very short period, the lock should expire
     * @throws Exception
     */
    public void DISABLED_testLockTimeout() throws Exception
    {
        log.info("start testLockTimeout");
        
        RetryingTransactionHelper trx = transactionService.getRetryingTransactionHelper();
        
        /**
         * Simulates a client starting a transfer and then "going away";
         */
        RetryingTransactionCallback<Void> startWithoutAnythingElse = new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                log.debug("about to call start");
                String transferId = receiver.start("1234", true, receiver.getVersion());
                return null;
            }
        };
        
        RetryingTransactionCallback<Void> slowTransfer = new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                log.debug("about to call start");
                String transferId = receiver.start("1234", true, receiver.getVersion());
                Thread.sleep(1000);
                receiver.saveSnapshot(transferId, null);
                fail("did not timeout");
                return null;
            }
        };
        
        
        long lockRefreshTime = receiver.getLockRefreshTime();
        long lockTimeOut = receiver.getLockTimeOut();
        
        try
        {
            receiver.setLockRefreshTime(500);
            receiver.setLockTimeOut(200);
        
            /**
             * This test simulates a client that starts a transfer and then "goes away".  
             * We kludge the timeouts to far shorter than normal to make the test run in a reasonable time.
             */
            for (int i = 0; i < 3; i++)
            {
                log.info("test iteration:" + i);
                trx.doInTransaction(startWithoutAnythingElse, false, true);
                Thread.sleep(1000);
            }
            try
            {
                trx.doInTransaction(slowTransfer, false, true);
            }
            catch (Exception e)
            {
                // Expect to go here.
            }
        } 
        finally
        {
            receiver.setLockRefreshTime(lockRefreshTime);
            receiver.setLockTimeOut(lockTimeOut);
        }
        
        log.info("end testLockTimeout");
    }

    public void testSaveContent() throws Exception
    {
        log.info("start testSaveContent");
        
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
        
        startNewTransaction();
        try
        {
            String transferId = receiver.start("1234", true, receiver.getVersion());
            try
            {
                String contentId = "mytestcontent";
                receiver.saveContent(transferId, contentId, new ByteArrayInputStream(dummyContentBytes));
                File contentFile = new File(receiver.getStagingFolder(transferId), contentId);
                assertTrue(contentFile.exists());
                assertEquals(dummyContentBytes.length, contentFile.length());
            }
            finally
            {
                receiver.end(transferId);
            }
        }
        finally
        {
            endTransaction();
        }
    }

    public void testSaveSnapshot() throws Exception
    {
        log.info("start testSaveSnapshot");
        
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
        
        startNewTransaction();
        try
        {
            String transferId = receiver.start("1234", true, receiver.getVersion());
            File snapshotFile = null;
            try
            {
                TransferManifestNode node = createContentNode();
                List<TransferManifestNode> nodes = new ArrayList<TransferManifestNode>();
                nodes.add(node);
                String snapshot = createSnapshot(nodes);

                receiver.saveSnapshot(transferId, new ByteArrayInputStream(snapshot.getBytes("UTF-8")));

                File stagingFolder = receiver.getStagingFolder(transferId);
                snapshotFile = new File(stagingFolder, "snapshot.xml");
                assertTrue(snapshotFile.exists());
                assertEquals(snapshot.getBytes("UTF-8").length, snapshotFile.length());
            }
            finally
            {
                receiver.end(transferId);
                if (snapshotFile != null)
                {
                    assertFalse(snapshotFile.exists());
                }
            }
        }
        finally
        {
            endTransaction();
        }
    }

    public void testBasicCommit() throws Exception
    {
        log.info("start testBasicCommit");
        
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
        
        class TestContext
        {
            TransferManifestNode node = null;
            String transferId = null;
        }
        
        RetryingTransactionCallback<TestContext> setupCB = new RetryingTransactionCallback<TestContext>()
        {
            @Override
            public TestContext execute() throws Throwable
            {
                TestContext tc = new TestContext();
                tc.node = createContentNode();
                return tc;
            }
        };
        
        final TestContext tc = tran.doInTransaction(setupCB, false, true);
        
        RetryingTransactionCallback<Void> doPrepareCB = new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                tc.transferId = receiver.start("1234", true, receiver.getVersion());
                
                List<TransferManifestNode> nodes = new ArrayList<TransferManifestNode>();
                nodes.add(tc.node);
                String snapshot = createSnapshot(nodes);

                receiver.saveSnapshot(tc.transferId, new ByteArrayInputStream(snapshot.getBytes("UTF-8")));
                receiver.saveContent(tc.transferId, tc.node.getUuid(), new ByteArrayInputStream(dummyContentBytes));
            
                return null;
            }
        };
  
        RetryingTransactionCallback<Void> doCommitCB = new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                receiver.commit(tc.transferId);

                return null;
            }
        };
        
        RetryingTransactionCallback<Void> doEndCB = new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                receiver.end(tc.transferId);
                return null;
            }
        };
        
        try
        {
            tran.doInTransaction(doPrepareCB, false, true);
            tran.doInTransaction(doCommitCB, false, true);
        }
        finally
        {
            if(tc.transferId != null)
            {
                tran.doInTransaction(doEndCB, false, true);
            }
        }

        RetryingTransactionCallback<Void> doValidateCB = new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                assertTrue(nodeService.exists(tc.node.getNodeRef()));
                nodeService.deleteNode(tc.node.getNodeRef());

                return null;
            }
        };
        
        tran.doInTransaction(doValidateCB, false, true);
    }

    /**
     * Test More Complex Commit
     * 
     * @throws Exception
     */
    public void testMoreComplexCommit() throws Exception
    {
        log.info("start testMoreComplexCommit");
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
        
        class TestContext
        {
            List<TransferManifestNode> nodes = new ArrayList<TransferManifestNode>();
            TransferManifestNormalNode node1 = null;
            TransferManifestNormalNode node2 = null;
            TransferManifestNode node3 = null;
            TransferManifestNode node4 = null;
            TransferManifestNode node5 = null;
            TransferManifestNode node6 = null;
            TransferManifestNode node7 = null;
            TransferManifestNode node8 = null;
            TransferManifestNode node9 = null;
            TransferManifestNode node10 = null;
            TransferManifestNormalNode node11 = null;
            TransferManifestNode node12 = null;
            String transferId = null;
        };

        RetryingTransactionCallback<TestContext> setupCB = new RetryingTransactionCallback<TestContext>()
        {
            @Override
            public TestContext execute() throws Throwable
            {
                TestContext tc = new TestContext();
           
                tc.node1 = createContentNode();
                tc.nodes.add(tc.node1);
                tc.node2 = createContentNode();
                tc.nodes.add(tc.node2);
                tc.node3 = createContentNode();
                tc.nodes.add(tc.node3);
                tc.node4 = createContentNode();
                tc.nodes.add(tc.node4);
                tc.node5 = createContentNode();
                tc.nodes.add(tc.node5);
                tc.node6 = createContentNode();
                tc.nodes.add(tc.node6);
                tc.node7 = createContentNode();
                tc.nodes.add(tc.node7);
                tc.node8 = createFolderNode();
                tc.nodes.add(tc.node8);
                tc.node9 = createFolderNode();
                tc.nodes.add(tc.node9);
                tc.node10 = createFolderNode();
                tc.nodes.add(tc.node10);
                tc.node11 = createFolderNode();
                tc.nodes.add(tc.node11);
                tc.node12 = createFolderNode();
                tc.nodes.add(tc.node12);

                associatePeers(tc.node1, tc.node2);
                moveNode(tc.node2, tc.node11);
                
                return tc;
            }
        };
        
        final TestContext tc = tran.doInTransaction(setupCB, false, true);
  
        RetryingTransactionCallback<Void> doPrepareCB = new RetryingTransactionCallback<Void>()
        {

            @Override
            public Void execute() throws Throwable
            {
                tc.transferId = receiver.start("1234", true, receiver.getVersion());
                String snapshot = createSnapshot(tc.nodes);

                receiver.saveSnapshot(tc.transferId, new ByteArrayInputStream(snapshot.getBytes("UTF-8")));

                for (TransferManifestNode node : tc.nodes)
                {
                     receiver.saveContent(tc.transferId, node.getUuid(), new ByteArrayInputStream(dummyContentBytes));
                }
                return null;
            }
        };

        RetryingTransactionCallback<Void> doCommitCB = new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                log.info("testMoreComplexCommit - commit");
                receiver.commit(tc.transferId);
                log.info("testMoreComplexCommit - commited");
                return null;
            }
        };
        RetryingTransactionCallback<Void> doEndCB = new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Needs to move elsewhere to allow other tests to pass.
                receiver.end(tc.transferId);
                return null;
            }
        };
        
        try
        {
            tran.doInTransaction(doPrepareCB, false, true);
            tran.doInTransaction(doCommitCB, false, true);
        }
        finally
        {
            if(tc.transferId != null)
            {
                tran.doInTransaction(doEndCB, false, true);
            }
        }
        
        RetryingTransactionCallback<Void> validateCB = new RetryingTransactionCallback<Void>()
        {

            @Override
            public Void execute() throws Throwable
            {
                log.info("testMoreComplexCommit - validate nodes");
                assertTrue(nodeService.getAspects(tc.node1.getNodeRef()).contains(ContentModel.ASPECT_ATTACHABLE));
                assertFalse(nodeService.getSourceAssocs(tc.node2.getNodeRef(), ContentModel.ASSOC_ATTACHMENTS).isEmpty());
                for (TransferManifestNode node : tc.nodes)
                {
                    assertTrue(nodeService.exists(node.getNodeRef()));
                }
               
                return null;
            }
        };
        tran.doInTransaction(validateCB, false, true);
    }
    
    /**
     * Test Node Delete And Restore
     * 
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public void testNodeDeleteAndRestore() throws Exception
    {
        log.info("start testNodeDeleteAndRestore");
        
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
        
        final TransferServicePolicies.OnEndInboundTransferPolicy mockedPolicyHandler = 
            mock(TransferServicePolicies.OnEndInboundTransferPolicy.class);
        
        policyComponent.bindClassBehaviour(
                TransferServicePolicies.OnEndInboundTransferPolicy.QNAME,
                TransferModel.TYPE_TRANSFER_RECORD, 
                new JavaBehaviour(mockedPolicyHandler, "onEndInboundTransfer", NotificationFrequency.EVERY_EVENT));
        
        class TestContext 
        {
            String transferId;
            TransferManifestNormalNode node1;
            TransferManifestNormalNode node2;
            TransferManifestNode node3;
            TransferManifestNode node4;
            TransferManifestNode node5;
            TransferManifestNode node6;
            TransferManifestNode node7;
            TransferManifestNode node8;
            TransferManifestNode node9;
            TransferManifestNode node10;
            TransferManifestNormalNode node11;
            TransferManifestNode node12;
            TransferManifestDeletedNode deletedNode8;
            TransferManifestDeletedNode deletedNode2;
            TransferManifestDeletedNode deletedNode11;
            List<TransferManifestNode> nodes;
            String errorMsgId;
        };
        
        RetryingTransactionCallback<TestContext> setupCB = new RetryingTransactionCallback<TestContext>()
        {
            @Override
            public TestContext execute() throws Throwable
            {
                TestContext tc = new TestContext();
                
                tc.nodes = new ArrayList<TransferManifestNode>();
                tc.node1 = createContentNode();
                tc.nodes.add(tc.node1);
                tc.node2 = createContentNode();
                tc.nodes.add(tc.node2);
                tc.node3 = createContentNode();
                tc.nodes.add(tc.node3);
                tc.node4 = createContentNode();
                tc.nodes.add(tc.node4);
                tc.node5 = createContentNode();
                tc.nodes.add(tc.node5);
                tc.node6 = createContentNode();
                tc.nodes.add(tc.node6);
                tc.node7 = createContentNode();
                tc.nodes.add(tc.node7);
                tc.node8 = createFolderNode();
                tc.nodes.add(tc.node8);
                tc.node9 = createFolderNode();
                tc.nodes.add(tc.node9);
                tc.node10 = createFolderNode();
                tc.nodes.add(tc.node10);
                tc.node11 = createFolderNode();
                tc.nodes.add(tc.node11);
                tc.node12 = createFolderNode();
                tc.nodes.add(tc.node12);

                associatePeers(tc.node1, tc.node2);
                moveNode(tc.node2, tc.node11);

                tc.deletedNode8 = createDeletedNode(tc.node8);
                tc.deletedNode2 = createDeletedNode(tc.node2);
                tc.deletedNode11 = createDeletedNode(tc.node11);
           
                return tc;
            }
        };
        
        final TestContext tc = tran.doInTransaction(setupCB, false, true);
        
        RetryingTransactionCallback<Void> doFirstPrepareCB = new RetryingTransactionCallback<Void>()
        {

            @Override
            public Void execute() throws Throwable
            {
                tc.transferId = receiver.start("1234", true, receiver.getVersion());
                String snapshot = createSnapshot(tc.nodes);
                log.debug(snapshot);
                receiver.saveSnapshot(tc.transferId, new ByteArrayInputStream(snapshot.getBytes("UTF-8")));

                for (TransferManifestNode node : tc.nodes)
                {
                    receiver.saveContent(tc.transferId, node.getUuid(), new ByteArrayInputStream(dummyContentBytes));
                }
             
                return null;
            }
        };
        
        RetryingTransactionCallback<Void> doCommitCB = new RetryingTransactionCallback<Void>()
        {

            @Override
            public Void execute() throws Throwable
            {
                receiver.commit(tc.transferId);
                
                return null;
            }
        };
        RetryingTransactionCallback<Void> doEndCB = new RetryingTransactionCallback<Void>()
        {

            @Override
            public Void execute() throws Throwable
            {
                // Needs to move elsewhere to allow other tests to pass.
                receiver.end(tc.transferId);
                return null;
            }
        };
        
        RetryingTransactionCallback<Void> validateFirstCB = new RetryingTransactionCallback<Void>()
        {

            @Override
            public Void execute() throws Throwable
            {
                assertTrue(nodeService.getAspects(tc.node1.getNodeRef()).contains(ContentModel.ASPECT_ATTACHABLE));
                assertFalse(nodeService.getSourceAssocs(tc.node2.getNodeRef(), ContentModel.ASSOC_ATTACHMENTS).isEmpty());

                ArgumentCaptor<String> transferIdCaptor = ArgumentCaptor.forClass(String.class);
                ArgumentCaptor<Set> createdNodesCaptor = ArgumentCaptor.forClass(Set.class);
                ArgumentCaptor<Set> updatedNodesCaptor = ArgumentCaptor.forClass(Set.class);
                ArgumentCaptor<Set> deletedNodesCaptor = ArgumentCaptor.forClass(Set.class);
                verify(mockedPolicyHandler, times(1)).onEndInboundTransfer(transferIdCaptor.capture(), 
                        createdNodesCaptor.capture(), 
                        updatedNodesCaptor.capture(), 
                        deletedNodesCaptor.capture());
                assertEquals(tc.transferId, transferIdCaptor.getValue());
                Set capturedCreatedNodes = createdNodesCaptor.getValue();
                assertEquals(tc.nodes.size(), capturedCreatedNodes.size());

                for (TransferManifestNode node : tc.nodes)
                {
                    assertTrue(nodeService.exists(node.getNodeRef()));
                    assertTrue(capturedCreatedNodes.contains(node.getNodeRef()));
                }
                return null;
            }
        };
  
        
        /**
         * First transfer test here
         */
        reset(mockedPolicyHandler);
        try
        {
            tran.doInTransaction(doFirstPrepareCB, false, true);
            tran.doInTransaction(doCommitCB, false, true);
            tran.doInTransaction(validateFirstCB, false, true);
        }
        finally
        {
            if(tc.transferId != null)
            {
                tran.doInTransaction(doEndCB, false, true);
            }
        }
        
        /**
         * Second transfer this time with some deleted nodes,
         * 
         * nodes 8, 2, and 11 (11 and 2 are parent/child)
         */
        reset(mockedPolicyHandler);
        
        logger.debug("part 2 - transfer some deleted nodes");
        
        RetryingTransactionCallback<Void> doSecondPrepareCB = new RetryingTransactionCallback<Void>()
        {

            @Override
            public Void execute() throws Throwable
            {
                // Now delete nodes 8, 2, and 11 (11 and 2 are parent/child)
                tc.transferId = receiver.start("1234", true, receiver.getVersion());
                String snapshot = createSnapshot(Arrays.asList(new TransferManifestNode[] { tc.deletedNode8, 
                        tc.deletedNode2,
                        tc.deletedNode11 }));
                log.debug(snapshot);
                receiver.saveSnapshot(tc.transferId, new ByteArrayInputStream(snapshot.getBytes("UTF-8")));
             
                return null;
            }
        };
        
        RetryingTransactionCallback<Void> validateSecondCB = new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                ArgumentCaptor<String> transferIdCaptor = ArgumentCaptor.forClass(String.class);
                ArgumentCaptor<Set> createdNodesCaptor = ArgumentCaptor.forClass(Set.class);
                ArgumentCaptor<Set> updatedNodesCaptor = ArgumentCaptor.forClass(Set.class);
                ArgumentCaptor<Set> deletedNodesCaptor = ArgumentCaptor.forClass(Set.class);
                verify(mockedPolicyHandler, times(1)).onEndInboundTransfer(transferIdCaptor.capture(), 
                        createdNodesCaptor.capture(), 
                        updatedNodesCaptor.capture(), 
                        deletedNodesCaptor.capture());
                assertEquals(tc.transferId, transferIdCaptor.getValue());
                Set capturedDeletedNodes = deletedNodesCaptor.getValue();
                assertEquals(3, capturedDeletedNodes.size());
                assertTrue(capturedDeletedNodes.contains(tc.deletedNode8.getNodeRef()));
                assertTrue(capturedDeletedNodes.contains(tc.deletedNode2.getNodeRef()));
                assertTrue(capturedDeletedNodes.contains(tc.deletedNode11.getNodeRef()));
                
                log.debug("Test success of transfer...");
                TransferProgress progress = receiver.getProgressMonitor().getProgress(tc.transferId);
                assertEquals(TransferProgress.Status.COMPLETE, progress.getStatus());

                NodeRef archiveNode8 = new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, tc.node8.getNodeRef().getId()); 
                NodeRef archiveNode2 = new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, tc.node2.getNodeRef().getId()); 
                NodeRef archiveNode11 = new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, tc.node11.getNodeRef().getId()); 

                assertTrue(nodeService.exists(archiveNode8));
                assertTrue(nodeService.hasAspect(archiveNode8, ContentModel.ASPECT_ARCHIVED));
                log.debug("Successfully tested existence of archive node: " + archiveNode8);
                
                assertTrue(nodeService.exists(archiveNode2));
                assertTrue(nodeService.hasAspect(archiveNode2, ContentModel.ASPECT_ARCHIVED));
                log.debug("Successfully tested existence of archive node: " + archiveNode2);
                
                assertTrue(nodeService.exists(archiveNode11));
                assertTrue(nodeService.hasAspect(archiveNode11, ContentModel.ASPECT_ARCHIVED));
                log.debug("Successfully tested existence of archive node: " + archiveNode11);
                
                log.debug("Successfully tested existence of all archive nodes");
                
                log.debug("Testing existence of original node: " + tc.node8.getNodeRef());
                assertFalse(nodeService.exists(tc.node8.getNodeRef()));

                log.debug("Testing existence of original node: " + tc.node2.getNodeRef());
                assertFalse(nodeService.exists(tc.node2.getNodeRef()));

                log.debug("Testing existence of original node: " + tc.node11.getNodeRef());
                assertFalse(nodeService.exists(tc.node11.getNodeRef()));
                
                log.debug("Successfully tested non-existence of all original nodes");
                
                log.debug("Progress indication: " + progress.getCurrentPosition() + "/" + progress.getEndPosition());
                return null;
            }
        };
        
        try
        {
            tran.doInTransaction(doSecondPrepareCB, false, true);
            tran.doInTransaction(doCommitCB, false, true);
            tran.doInTransaction(validateSecondCB, false, true);
        }
        finally
        {
            if(tc.transferId != null)
            {
                tran.doInTransaction(doEndCB, false, true);
            }
        }
        
        logger.debug("part 3 - restore orphan node which should fail");
        System.out.println("Now try to restore orphan node 2.");
        /**
         * A third transfer.  Expect an "orphan" failure, since its parent (node11) is deleted
         */
        reset(mockedPolicyHandler);

        String errorMsgId = null;
        
        RetryingTransactionCallback<Void> doThirdPrepareCB = new RetryingTransactionCallback<Void>()
        {

            @Override
            public Void execute() throws Throwable
            {
                tc.transferId = receiver.start("1234", true, receiver.getVersion());
                String snapshot = createSnapshot(Arrays.asList(new TransferManifestNode[] { tc.node2 }));
                log.debug(snapshot);
                receiver.saveSnapshot(tc.transferId, new ByteArrayInputStream(snapshot.getBytes("UTF-8")));
                receiver.saveContent(tc.transferId, tc.node2.getUuid(), new ByteArrayInputStream(dummyContentBytes));
             
                return null;
            }
        };
        

        RetryingTransactionCallback<Void> doCommitExpectingFailCB = new RetryingTransactionCallback<Void>()
        {

            @Override
            public Void execute() throws Throwable
            {
                try
                {
                    receiver.commit(tc.transferId);
                    fail("Expected an exception");
                }
                catch (TransferException ex)
                {
                    // Expected
                    tc.errorMsgId = ex.getMsgId();

                    ArgumentCaptor<String> transferIdCaptor = ArgumentCaptor.forClass(String.class);
                    ArgumentCaptor<Set> createdNodesCaptor = ArgumentCaptor.forClass(Set.class);
                    ArgumentCaptor<Set> updatedNodesCaptor = ArgumentCaptor.forClass(Set.class);
                    ArgumentCaptor<Set> deletedNodesCaptor = ArgumentCaptor.forClass(Set.class);
                    
                    verify(mockedPolicyHandler, times(1)).onEndInboundTransfer(transferIdCaptor.capture(), 
                            createdNodesCaptor.capture(), updatedNodesCaptor.capture(), deletedNodesCaptor.capture());
                    
                    assertEquals(tc.transferId, transferIdCaptor.getValue());
                    assertTrue(createdNodesCaptor.getValue().isEmpty());
                    assertTrue(updatedNodesCaptor.getValue().isEmpty());
                    assertTrue(deletedNodesCaptor.getValue().isEmpty());
                }
       
                
                return null;
            }
        };
        
        RetryingTransactionCallback<Void> validateThirdCB = new RetryingTransactionCallback<Void>()
        {

            @Override
            public Void execute() throws Throwable
            {
                TransferProgress progress = receiver.getProgressMonitor().getProgress(tc.transferId);
                assertEquals(TransferProgress.Status.ERROR, progress.getStatus());
                log.debug("Progress indication: " + progress.getCurrentPosition() + "/" + progress.getEndPosition());
                assertNotNull("Progress error", progress.getError());
                assertTrue(progress.getError() instanceof Exception);
                assertTrue(tc.errorMsgId, tc.errorMsgId.contains("orphan"));
                return null;
            }
        };
        
        try
        {
            tran.doInTransaction(doThirdPrepareCB, false, true);
            tran.doInTransaction(doCommitExpectingFailCB, false, true);
        }
        finally
        {
            if(tc.transferId != null)
            {
                tran.doInTransaction(doEndCB, false, true);
            }
        }
        
        tran.doInTransaction(validateThirdCB, false, true);
        
        log.debug("start testNodeDeleteAndRestore");
    
    }

    /**
     * Test for fault raised as ALF-2772
     * (https://issues.alfresco.com/jira/browse/ALF-2772) When transferring
     * nodes it shouldn't matter the order in which they appear in the snapshot.
     * That is to say, it shouldn't matter if a child node is listed before its
     * parent node.
     * 
     * Typically this is true, but in the special case where the parent node is
     * being restored from the target's archive store then there is a fault. The
     * process should be:
     * 
     * 1. Process child node 
     * 2. Fail to find parent node 
     * 3. Place child node in temporary location and mark as an orphan 
     * 4. Process parent node 
     * 5. Create node to correspond to parent node 
     * 6. "Re-parent" orphaned child node with parent node
     * 
     * However, in the case where the parent node is found in the target's
     * archive store, the process is actually:
     * 
     * 1. Process child node 
     * 2. Fail to find parent node 
     * 3. Place child node in temporary location and mark as an orphan 
     * 4. Process parent node 
     * 5. Find corresponding parent node in archive store and restore it 
     * 6. Update corresponding parent node
     * 
     * Note that, in this case, we are not re-parenting the orphan as we should be.
     * 
     * @throws Exception
     */
    public void testJira_ALF_2772() throws Exception
    {
        log.debug("start testJira_ALF_2772");
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
        
        class TestContext
        {
            TransferManifestNormalNode node1 = null;
            TransferManifestNormalNode node2 = null;
            TransferManifestNormalNode node11 = null;
            TransferManifestDeletedNode deletedNode11 = null;
            String transferId = null;
        };
        
        RetryingTransactionCallback<TestContext> setupCB = new RetryingTransactionCallback<TestContext>()
        {
            @Override
            public TestContext execute() throws Throwable
            {
                TestContext tc = new TestContext();
                
                tc.node1 = createContentNode();
                tc.node2 = createContentNode();
                tc.node11 = createFolderNode();
                
                associatePeers(tc.node1, tc.node2);
                moveNode(tc.node2, tc.node11);
                
                tc.deletedNode11 = createDeletedNode(tc.node11);
                
                return tc;
            }
        };
        
        final TestContext tc = tran.doInTransaction(setupCB, false, true);
        
        RetryingTransactionCallback<Void> doEndCB = new RetryingTransactionCallback<Void>()
        {

            @Override
            public Void execute() throws Throwable
            {
                // Needs to move elsewhere to allow other tests to pass.
                receiver.end(tc.transferId);
                return null;
            }
        };


        RetryingTransactionCallback<Void> doFirstCB = new RetryingTransactionCallback<Void>()
        {

            @Override
            public Void execute() throws Throwable
            {
                tc.transferId = receiver.start("1234", true, receiver.getVersion());

                List<TransferManifestNode> nodes = new ArrayList<TransferManifestNode>();
                
                //First we'll just send a folder node
                nodes.add(tc.node11);
                
                String snapshot = createSnapshot(nodes);
                log.debug(snapshot);
                receiver.saveSnapshot(tc.transferId, new ByteArrayInputStream(snapshot.getBytes("UTF-8")));

                for (TransferManifestNode node : nodes)
                {
                    receiver.saveContent(tc.transferId, node.getUuid(), new ByteArrayInputStream(dummyContentBytes));
                }
                receiver.commit(tc.transferId);

                for (TransferManifestNode node : nodes)
                {
                    assertTrue(nodeService.exists(node.getNodeRef()));
                }
                
                return null;
            }
        };
        
        /**
         * First we'll just send a folder node
         */
        try
        {
            tran.doInTransaction(doFirstCB, false, true);
        }
        finally
        {
            if(tc.transferId != null)
            {
                tran.doInTransaction(doEndCB, false, true);
            }
        }
        
        
                
        RetryingTransactionCallback<Void> doSecondCB = new RetryingTransactionCallback<Void>()
        {

            @Override
            public Void execute() throws Throwable
            {
                tc.transferId = receiver.start("1234", true, receiver.getVersion());
                String snapshot = createSnapshot(Arrays.asList(new TransferManifestNode[] { tc.deletedNode11 }));
                log.debug(snapshot);
                receiver.saveSnapshot(tc.transferId, new ByteArrayInputStream(snapshot.getBytes("UTF-8")));
                receiver.commit(tc.transferId);
               
                return null;
            }
        };
        
        RetryingTransactionCallback<Void> doValidateSecondCB = new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {     
                TransferProgress progress = receiver.getProgressMonitor().getProgress(tc.transferId);
                assertEquals(TransferProgress.Status.COMPLETE, progress.getStatus());

                NodeRef archivedNodeRef = new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, tc.deletedNode11.getNodeRef().getId());
                assertTrue(nodeService.exists(archivedNodeRef));
                assertTrue(nodeService.hasAspect(archivedNodeRef, ContentModel.ASPECT_ARCHIVED));
                log.debug("Successfully tested existence of archive node: " + tc.deletedNode11.getNodeRef());
                
                log.debug("Successfully tested existence of all archive nodes");
                
                log.debug("Testing existence of original node: " + tc.node11.getNodeRef());
                assertFalse(nodeService.exists(tc.node11.getNodeRef()));

                log.debug("Successfully tested non-existence of all original nodes");
                
                log.debug("Progress indication: " + progress.getCurrentPosition() + "/" + progress.getEndPosition());
                
                return null;
            }
        };
        
        /**
         * Then delete a folder node
         */
        try
        {
            tran.doInTransaction(doSecondCB, false, true);
            tran.doInTransaction(doValidateSecondCB, false, true);
        }
        finally
        {
            if(tc.transferId != null)
            {
                tran.doInTransaction(doEndCB, false, true);
            }
        }
        

        /**
         * Finally we transfer node2 and node11 (in that order)
         */
        RetryingTransactionCallback<Void> doThirdCB = new RetryingTransactionCallback<Void>()
        {

            @Override
            public Void execute() throws Throwable
            {
              
                tc.transferId = receiver.start("1234", true, receiver.getVersion());
                String snapshot = createSnapshot(Arrays.asList(new TransferManifestNode[] { tc.node2, tc.node11 }));
                log.debug(snapshot);
                receiver.saveSnapshot(tc.transferId, new ByteArrayInputStream(snapshot.getBytes("UTF-8")));
                receiver.saveContent(tc.transferId, tc.node2.getUuid(), new ByteArrayInputStream(dummyContentBytes));
                receiver.commit(tc.transferId);
                
                return null;
            }
        };
        
        RetryingTransactionCallback<Void> doValidateThirdCB = new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {     
                
                return null;
            }
        };
        
        /**
         * Then delete a folder node
         */
        try
        {
            tran.doInTransaction(doThirdCB, false, true);
            tran.doInTransaction(doValidateThirdCB, false, true);
        }
        finally
        {
            if(tc.transferId != null)
            {
                tran.doInTransaction(doEndCB, false, true);
            }
        }

    }

    /**
     * Test for fault raised as MNT-11057
     * (https://issues.alfresco.com/jira/browse/MNT-11057) Bug in replication process on Aliens.
     *
     * @throws Exception
     */

    public void testMNT11057() throws Exception
    {
        String folder1Name = "H1";
        String folder2Name = "H2";
        String folder3Name = "H3";

        //Step 1 transfer from repo A (H1 -> H2)
        setDefaultRollback(true);
        startNewTransaction();

        String transferIdA1 = receiver.start("transferFromRepoA1", true, receiver.getVersion());

        TransferManifestNormalNode folder1A1 = createFolderNode(folder1Name);
        TransferManifestNormalNode folder2A1 = createFolderNode(folder2Name);
        TransferManifestNormalNode folder3A1 = createFolderNode(folder3Name);
        moveNode(folder2A1, folder1A1);

        List<TransferManifestNode> nodesA1 = new ArrayList<TransferManifestNode>();

        nodesA1.add(folder1A1);
        nodesA1.add(folder2A1);

        endTransaction();

        this.setDefaultRollback(false);
        startNewTransaction();
        try
        {
            String snapshot = createSnapshot(nodesA1, "repo A");
            log.debug(snapshot);
            receiver.saveSnapshot(transferIdA1, new StringInputStream(snapshot, "UTF-8"));

            for (TransferManifestNode node : nodesA1)
            {
                receiver.saveContent(transferIdA1, node.getUuid(), new ByteArrayInputStream(dummyContentBytes));
            }
            receiver.commit(transferIdA1);

            for (TransferManifestNode node : nodesA1)
            {
                assertTrue(nodeService.exists(node.getNodeRef()));
            }
        }
        finally
        {
            receiver.end(transferIdA1);
            endTransaction();
        }

        //Step 2 trasfer from repo B (H1 -> H3)
        setDefaultRollback(true);
        startNewTransaction();

        String transferIdB1 = receiver.start("transferFromRepoB1", true, receiver.getVersion());

        TransferManifestNormalNode folder1B1 = createFolderNode(folder1Name);
        TransferManifestNormalNode folder3B1 = createFolderNode(folder3Name);
        moveNode(folder3B1, folder1B1);

        List<TransferManifestNode> nodesB1 = new ArrayList<TransferManifestNode>();

        nodesB1.add(folder1B1);
        nodesB1.add(folder3B1);

        endTransaction();

        this.setDefaultRollback(false);
        startNewTransaction();
        try
        {
            String snapshot = createSnapshot(nodesB1, "repo B");
            log.debug(snapshot);
            receiver.saveSnapshot(transferIdB1, new StringInputStream(snapshot, "UTF-8"));

            for (TransferManifestNode node : nodesB1)
            {
                receiver.saveContent(transferIdB1, node.getUuid(), new ByteArrayInputStream(dummyContentBytes));
            }
            receiver.commit(transferIdB1);
        }
        finally
        {
            receiver.end(transferIdB1);
            endTransaction();
        }

        assertTrue(nodeService.exists(folder1A1.getNodeRef()));
        log.info("has Alien");
        log.info(nodeService.hasAspect(folder1A1.getNodeRef(), TransferModel.ASPECT_ALIEN));

        assertTrue(nodeService.exists(folder2A1.getNodeRef()));
        log.info("has Alien");
        assertFalse(nodeService.hasAspect(folder2A1.getNodeRef(), TransferModel.ASPECT_ALIEN));

        assertFalse(nodeService.exists(folder1B1.getNodeRef()));

        assertTrue(nodeService.exists(folder3B1.getNodeRef()));
        log.info("has Alien");
        assertTrue(nodeService.hasAspect(folder3B1.getNodeRef(), TransferModel.ASPECT_ALIEN));

        startNewTransaction();

        moveNode(folder3A1, folder1A1);
        moveNode(folder2A1, folder3A1);

        nodesA1 = new ArrayList<TransferManifestNode>();

        nodesA1.add(folder1A1);
        nodesA1.add(folder3A1);
        nodesA1.add(folder2A1);

        endTransaction();


        //Step 3 transfer from repo A again (H2 is moved to newly created H3 on A: H1 -> H3 -> H2)
        startNewTransaction();
        try
        {
            String transferId = receiver.start("transferFromRepoA1Again", true, receiver.getVersion());
            String snapshot = createSnapshot(nodesA1, "repo A");
            log.debug(snapshot);
            receiver.saveSnapshot(transferId, new StringInputStream(snapshot, "UTF-8"));
            receiver.commit(transferId);
        }
        catch(Exception ex)
        {
            if(ex instanceof NullPointerException)
            {
                fail("Test of MNT-11057 failed: " + ex.getMessage());
            }
        }
        finally
        {
            endTransaction();
        }
    }
    
    
    public void testAsyncCommit() throws Exception
    {
        log.info("start testAsyncCommit");

        this.setDefaultRollback(false);
        final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();

        startNewTransaction();
        final String transferId = receiver.start("1234", true, receiver.getVersion());
        endTransaction();

        startNewTransaction();
        final List<TransferManifestNode> nodes = new ArrayList<TransferManifestNode>();
        final TransferManifestNormalNode node1 = createContentNode();
        nodes.add(node1);
        final TransferManifestNormalNode node2 = createContentNode();
        nodes.add(node2);
        TransferManifestNode node3 = createContentNode();
        nodes.add(node3);
        TransferManifestNode node4 = createContentNode();
        nodes.add(node4);
        TransferManifestNode node5 = createContentNode();
        nodes.add(node5);
        TransferManifestNode node6 = createContentNode();
        nodes.add(node6);
        TransferManifestNode node7 = createContentNode();
        nodes.add(node7);
        TransferManifestNode node8 = createFolderNode();
        nodes.add(node8);
        TransferManifestNode node9 = createFolderNode();
        nodes.add(node9);
        TransferManifestNode node10 = createFolderNode();
        nodes.add(node10);
        TransferManifestNormalNode node11 = createFolderNode();
        nodes.add(node11);
        TransferManifestNode node12 = createFolderNode();
        nodes.add(node12);

        associatePeers(node1, node2);
        moveNode(node2, node11);

        endTransaction();

        String snapshot = createSnapshot(nodes);

        startNewTransaction();
        receiver.saveSnapshot(transferId, new ByteArrayInputStream(snapshot.getBytes("UTF-8")));
        endTransaction();

        for (TransferManifestNode node : nodes)
        {
            startNewTransaction();
            receiver.saveContent(transferId, node.getUuid(), new ByteArrayInputStream(dummyContentBytes));
            endTransaction();
        }

        startNewTransaction();
        receiver.commitAsync(transferId);
        endTransaction();

        log.debug("Posted request for commit");

        TransferProgressMonitor progressMonitor = receiver.getProgressMonitor();
        TransferProgress progress = null;
        while (progress == null || !TransferProgress.getTerminalStatuses().contains(progress.getStatus()))
        {
            Thread.sleep(500);
            startNewTransaction();
            progress = progressMonitor.getProgress(transferId);
            endTransaction();
            log.debug("Progress indication: " + progress.getStatus() + ": " + progress.getCurrentPosition() + "/"
                    + progress.getEndPosition());
        }
        assertEquals(TransferProgress.Status.COMPLETE, progress.getStatus());

        startNewTransaction();
        try
        {
            assertTrue(nodeService.getAspects(node1.getNodeRef()).contains(ContentModel.ASPECT_ATTACHABLE));
            assertFalse(nodeService.getSourceAssocs(node2.getNodeRef(), ContentModel.ASSOC_ATTACHMENTS).isEmpty());
            for (TransferManifestNode node : nodes)
            {
                assertTrue(nodeService.exists(node.getNodeRef()));
            }
        }
        finally
        {
            endTransaction();
        }
    }

    /**
     * @param nodeToDelete
     * @return
     */
    private TransferManifestDeletedNode createDeletedNode(TransferManifestNode nodeToDelete)
    {
        TransferManifestDeletedNode deletedNode = new TransferManifestDeletedNode();
        deletedNode.setNodeRef(nodeToDelete.getNodeRef());
        deletedNode.setParentPath(nodeToDelete.getParentPath());
        deletedNode.setPrimaryParentAssoc(nodeToDelete.getPrimaryParentAssoc());
        deletedNode.setUuid(nodeToDelete.getUuid());
        return deletedNode;
    }

    /**
     * move transfer node to new parent.
     * @param childNode
     * @param newParent
     */
    private void moveNode(TransferManifestNormalNode childNode, TransferManifestNormalNode newParent)
    {
        List<ChildAssociationRef> currentParents = childNode.getParentAssocs();
        List<ChildAssociationRef> newParents = new ArrayList<ChildAssociationRef>();

        for (ChildAssociationRef parent : currentParents)
        {
            if (!parent.isPrimary())
            {
                newParents.add(parent);
            }
            else
            {
                ChildAssociationRef newPrimaryAssoc = new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, newParent
                        .getNodeRef(), parent.getQName(), parent.getChildRef(), true, -1);
                newParents.add(newPrimaryAssoc);
                childNode.setPrimaryParentAssoc(newPrimaryAssoc);
                Path newParentPath = new Path();
                newParentPath.append(newParent.getParentPath());
                newParentPath.append(new Path.ChildAssocElement(newParent.getPrimaryParentAssoc()));
                childNode.setParentPath(newParentPath);
            }
        }
        childNode.setParentAssocs(newParents);
    }

    private void associatePeers(TransferManifestNormalNode source, TransferManifestNormalNode target)
    {
        List<AssociationRef> currentReferencedPeers = source.getTargetAssocs();
        if (currentReferencedPeers == null)
        {
            currentReferencedPeers = new ArrayList<AssociationRef>();
            source.setTargetAssocs(currentReferencedPeers);
        }

        List<AssociationRef> currentRefereePeers = target.getSourceAssocs();
        if (currentRefereePeers == null)
        {
            currentRefereePeers = new ArrayList<AssociationRef>();
            target.setSourceAssocs(currentRefereePeers);
        }

        Set<QName> aspects = source.getAspects();
        if (aspects == null)
        {
            aspects = new HashSet<QName>();
            source.setAspects(aspects);
        }
        aspects.add(ContentModel.ASPECT_ATTACHABLE);

        AssociationRef newAssoc = new AssociationRef(null, source.getNodeRef(), ContentModel.ASSOC_ATTACHMENTS, target
                .getNodeRef());
        currentRefereePeers.add(newAssoc);
        currentReferencedPeers.add(newAssoc);
    }

    private String createSnapshot(List<TransferManifestNode> nodes) throws Exception
    {
        return createSnapshot(nodes, "repo 1");
    }

    private String createSnapshot(List<TransferManifestNode> nodes, String repoID) throws Exception
    {
        XMLTransferManifestWriter manifestWriter = new XMLTransferManifestWriter();
        StringWriter output = new StringWriter();
        manifestWriter.startTransferManifest(output);
        TransferManifestHeader header = new TransferManifestHeader();
        header.setCreatedDate(new Date());
        header.setNodeCount(nodes.size());
        header.setRepositoryId(repoID);
        manifestWriter.writeTransferManifestHeader(header);
        for (TransferManifestNode node : nodes)
        {
            manifestWriter.writeTransferManifestNode(node);
        }
        manifestWriter.endTransferManifest();

        return output.toString();

    }

    /**
     * @return
     */
    private TransferManifestNormalNode createContentNode(/*String transferId*/) throws Exception
    {
        TransferManifestNormalNode node = new TransferManifestNormalNode();
        String uuid = GUID.generate();
        NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, uuid);
        node.setNodeRef(nodeRef);
        node.setUuid(uuid);
        byte[] dummyContent = "This is some dummy content.".getBytes("UTF-8");

        node.setType(ContentModel.TYPE_CONTENT);
        
        /**
         * Get guest home
         */
        NodeRef parentFolder = guestHome;

        String nodeName = uuid + ".testnode" + getNameSuffix();

        List<ChildAssociationRef> parents = new ArrayList<ChildAssociationRef>();
        ChildAssociationRef primaryAssoc = new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, parentFolder, QName
                .createQName(NamespaceService.CONTENT_MODEL_1_0_URI, nodeName), node.getNodeRef(), true, -1);
        parents.add(primaryAssoc);
        node.setParentAssocs(parents);
        node.setParentPath(nodeService.getPath(parentFolder));
        node.setPrimaryParentAssoc(primaryAssoc);

        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_NODE_UUID, uuid);
        props.put(ContentModel.PROP_NAME, nodeName);
        ContentData contentData = new ContentData("/" + uuid, "text/plain", dummyContent.length, "UTF-8");
        props.put(ContentModel.PROP_CONTENT, contentData);
        node.setProperties(props);

        return node;
    }

    private TransferManifestNormalNode createFolderNode() throws Exception
    {
        return createFolderNode(null);
    }

    private TransferManifestNormalNode createFolderNode(String folderName) throws Exception
    {
        TransferManifestNormalNode node = new TransferManifestNormalNode();
        String uuid = GUID.generate();
        NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, uuid);
        node.setNodeRef(nodeRef);
        node.setUuid(uuid);

        node.setType(ContentModel.TYPE_FOLDER);
        
        /**
         * Get guest home
         */
        String guestHomeQuery = "/app:company_home/app:guest_home";
        ResultSet guestHomeResult = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH, guestHomeQuery);
        assertEquals("", 1, guestHomeResult.length());
        NodeRef guestHome = guestHomeResult.getNodeRef(0); 
        NodeRef parentFolder = guestHome;
        
        String nodeName = folderName == null ? uuid + ".folder" + getNameSuffix() : folderName;

        List<ChildAssociationRef> parents = new ArrayList<ChildAssociationRef>();
        ChildAssociationRef primaryAssoc = new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, parentFolder, QName
                .createQName(NamespaceService.CONTENT_MODEL_1_0_URI, nodeName), node.getNodeRef(), true, -1);
        parents.add(primaryAssoc);
        node.setParentAssocs(parents);
        node.setParentPath(nodeService.getPath(parentFolder));
        node.setPrimaryParentAssoc(primaryAssoc);

        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_NODE_UUID, uuid);
        props.put(ContentModel.PROP_NAME, nodeName);
        node.setProperties(props);

        return node;
    }

    private String getNameSuffix()
    {
        return "" + fileCount++;
    }
}
