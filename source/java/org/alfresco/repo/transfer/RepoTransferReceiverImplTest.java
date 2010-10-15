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
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.filters.StringInputStream;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Unit test for RepoTransferReceiverImpl
 * 
 * @author Brian Remmington
 */
@SuppressWarnings("deprecation")
// It's a test
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
        setDefaultRollback(false);
        String uuid = GUID.generate();
        ChildAssociationRef childAssoc;
        startNewTransaction();
        try
        {
            ResultSet rs = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH, 
                    "/app:company_home");
            assertEquals(1, rs.length());
            NodeRef companyHome = rs.getNodeRef(0);
            Map<QName, Serializable> props = new HashMap<QName, Serializable>();
            props.put(ContentModel.PROP_NAME, uuid);
            childAssoc = nodeService.createNode(companyHome, ContentModel.ASSOC_CONTAINS, 
                    QName.createQName(NamespaceService.APP_MODEL_1_0_URI, uuid), ContentModel.TYPE_CONTENT, props);
        }
        finally
        {
            endTransaction();
        }
        
        startNewTransaction();
        try
        {
            nodeService.deleteNode(childAssoc.getChildRef());
        }
        finally
        {
            endTransaction();
        }
        
        startNewTransaction();
        try
        {
            log.debug("Test that original node no longer exists...");
            assertFalse(nodeService.exists(childAssoc.getChildRef()));
            log.debug("PASS - Original node no longer exists.");
            NodeRef archiveNodeRef = new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, childAssoc.getChildRef().getId());
            log.debug("Test that archive node exists...");
            assertTrue(nodeService.exists(archiveNodeRef));
            log.debug("PASS - Archive node exists.");
        }
        finally
        {
            endTransaction();
        }
        
    }
    
    /**
     * Tests start and end with regard to locking.
     * @throws Exception
     */
    public void testStartAndEnd() throws Exception
    {
        log.info("testStartAndEnd");
        
        RetryingTransactionHelper trx = transactionService.getRetryingTransactionHelper();
       
        RetryingTransactionCallback<Void> cb = new RetryingTransactionCallback<Void>()
        {
            
            @Override
            public Void execute() throws Throwable
            {
                log.debug("about to call start");
                String transferId = receiver.start();
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
                        receiver.start();
                        fail("Successfully started twice!");
                    }
                    catch (TransferException ex)
                    {
                        // Expected
                    }
                
                    Thread.sleep(300);
                    try
                    {
                        receiver.start();
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
    public void testLockTimeout() throws Exception
    {
        log.info("testStartAndEnd");
        
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
                String transferId = receiver.start();
                return null;
            }
        };
        
        RetryingTransactionCallback<Void> slowTransfer = new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                log.debug("about to call start");
                String transferId = receiver.start();
                Thread.sleep(1000);
                try
                {
                    receiver.saveSnapshot(transferId, null);
                    fail("did not timeout");
                }
                catch (TransferException te)
                {
                    logger.debug("expected to timeout", te);
                    // expect to go here with a timeout
                }
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
            trx.doInTransaction(slowTransfer, false, true);
        } 
        finally
        {
            receiver.setLockRefreshTime(lockRefreshTime);
            receiver.setLockTimeOut(lockTimeOut);
        }
    }

    public void testSaveContent() throws Exception
    {
        log.info("testSaveContent");
        startNewTransaction();
        try
        {
            String transferId = receiver.start();
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
        log.info("testSaveSnapshot");
        startNewTransaction();
        try
        {
            String transferId = receiver.start();
            File snapshotFile = null;
            try
            {
                TransferManifestNode node = createContentNode(transferId);
                List<TransferManifestNode> nodes = new ArrayList<TransferManifestNode>();
                nodes.add(node);
                String snapshot = createSnapshot(nodes);

                receiver.saveSnapshot(transferId, new StringInputStream(snapshot, "UTF-8"));

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
        log.info("testBasicCommit");
        startNewTransaction();
        TransferManifestNode node = null;

        try
        {
            String transferId = receiver.start();
            try
            {
                node = createContentNode(transferId);
                List<TransferManifestNode> nodes = new ArrayList<TransferManifestNode>();
                nodes.add(node);
                String snapshot = createSnapshot(nodes);

                receiver.saveSnapshot(transferId, new StringInputStream(snapshot, "UTF-8"));
                receiver.saveContent(transferId, node.getUuid(), new ByteArrayInputStream(dummyContentBytes));
                receiver.commit(transferId);

            }
            catch (Exception ex)
            {
                receiver.end(transferId);
                throw ex;
            }
        }
        finally
        {
            endTransaction();
        }

        startNewTransaction();
        try
        {
            assertTrue(nodeService.exists(node.getNodeRef()));
            nodeService.deleteNode(node.getNodeRef());
        }
        finally
        {
            endTransaction();
        }
    }

    public void testMoreComplexCommit() throws Exception
    {
        log.info("testMoreComplexCommit");
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

        startNewTransaction();
        try
        {
            transferId = receiver.start();
            node1 = createContentNode(transferId);
            nodes.add(node1);
            node2 = createContentNode(transferId);
            nodes.add(node2);
            node3 = createContentNode(transferId);
            nodes.add(node3);
            node4 = createContentNode(transferId);
            nodes.add(node4);
            node5 = createContentNode(transferId);
            nodes.add(node5);
            node6 = createContentNode(transferId);
            nodes.add(node6);
            node7 = createContentNode(transferId);
            nodes.add(node7);
            node8 = createFolderNode(transferId);
            nodes.add(node8);
            node9 = createFolderNode(transferId);
            nodes.add(node9);
            node10 = createFolderNode(transferId);
            nodes.add(node10);
            node11 = createFolderNode(transferId);
            nodes.add(node11);
            node12 = createFolderNode(transferId);
            nodes.add(node12);

            associatePeers(node1, node2);
            moveNode(node2, node11);

            String snapshot = createSnapshot(nodes);

            receiver.saveSnapshot(transferId, new StringInputStream(snapshot, "UTF-8"));

            for (TransferManifestNode node : nodes)
            {
                receiver.saveContent(transferId, node.getUuid(), new ByteArrayInputStream(dummyContentBytes));
            }
            receiver.commit(transferId);

        }
        finally
        {
            receiver.end(transferId);
            endTransaction();
        }

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
    
    @SuppressWarnings("unchecked")
    public void testNodeDeleteAndRestore() throws Exception
    {
        TransferServicePolicies.OnEndInboundTransferPolicy mockedPolicyHandler = 
            mock(TransferServicePolicies.OnEndInboundTransferPolicy.class);
        
        policyComponent.bindClassBehaviour(
                TransferServicePolicies.OnEndInboundTransferPolicy.QNAME,
                TransferModel.TYPE_TRANSFER_RECORD, 
                new JavaBehaviour(mockedPolicyHandler, "onEndInboundTransfer", NotificationFrequency.EVERY_EVENT));
        
        log.info("testNodeDeleteAndRestore");

        setDefaultRollback(true);
        startNewTransaction();
        String transferId = receiver.start();

        List<TransferManifestNode> nodes = new ArrayList<TransferManifestNode>();
        TransferManifestNormalNode node1 = createContentNode(transferId);
        nodes.add(node1);
        TransferManifestNormalNode node2 = createContentNode(transferId);
        nodes.add(node2);
        TransferManifestNode node3 = createContentNode(transferId);
        nodes.add(node3);
        TransferManifestNode node4 = createContentNode(transferId);
        nodes.add(node4);
        TransferManifestNode node5 = createContentNode(transferId);
        nodes.add(node5);
        TransferManifestNode node6 = createContentNode(transferId);
        nodes.add(node6);
        TransferManifestNode node7 = createContentNode(transferId);
        nodes.add(node7);
        TransferManifestNode node8 = createFolderNode(transferId);
        nodes.add(node8);
        TransferManifestNode node9 = createFolderNode(transferId);
        nodes.add(node9);
        TransferManifestNode node10 = createFolderNode(transferId);
        nodes.add(node10);
        TransferManifestNormalNode node11 = createFolderNode(transferId);
        nodes.add(node11);
        TransferManifestNode node12 = createFolderNode(transferId);
        nodes.add(node12);

        associatePeers(node1, node2);
        moveNode(node2, node11);

        TransferManifestDeletedNode deletedNode8 = createDeletedNode(node8);
        TransferManifestDeletedNode deletedNode2 = createDeletedNode(node2);
        TransferManifestDeletedNode deletedNode11 = createDeletedNode(node11);

        endTransaction();

        this.setDefaultRollback(false);
        startNewTransaction();
        try
        {
            String snapshot = createSnapshot(nodes);
            log.debug(snapshot);
            receiver.saveSnapshot(transferId, new StringInputStream(snapshot, "UTF-8"));

            for (TransferManifestNode node : nodes)
            {
                receiver.saveContent(transferId, node.getUuid(), new ByteArrayInputStream(dummyContentBytes));
            }
            receiver.commit(transferId);

            assertTrue(nodeService.getAspects(node1.getNodeRef()).contains(ContentModel.ASPECT_ATTACHABLE));
            assertFalse(nodeService.getSourceAssocs(node2.getNodeRef(), ContentModel.ASSOC_ATTACHMENTS).isEmpty());

            ArgumentCaptor<String> transferIdCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Set> createdNodesCaptor = ArgumentCaptor.forClass(Set.class);
            ArgumentCaptor<Set> updatedNodesCaptor = ArgumentCaptor.forClass(Set.class);
            ArgumentCaptor<Set> deletedNodesCaptor = ArgumentCaptor.forClass(Set.class);
            verify(mockedPolicyHandler, times(1)).onEndInboundTransfer(transferIdCaptor.capture(), 
                    createdNodesCaptor.capture(), updatedNodesCaptor.capture(), deletedNodesCaptor.capture());
            assertEquals(transferId, transferIdCaptor.getValue());
            Set capturedCreatedNodes = createdNodesCaptor.getValue();
            assertEquals(nodes.size(), capturedCreatedNodes.size());

            for (TransferManifestNode node : nodes)
            {
                assertTrue(nodeService.exists(node.getNodeRef()));
                assertTrue(capturedCreatedNodes.contains(node.getNodeRef()));
            }
        }
        finally
        {
            endTransaction();
        }

        reset(mockedPolicyHandler);
        
        startNewTransaction();
        try
        {
            // Now delete nodes 8, 2, and 11 (11 and 2 are parent/child)
            transferId = receiver.start();
            String snapshot = createSnapshot(Arrays.asList(new TransferManifestNode[] { deletedNode8, deletedNode2,
                    deletedNode11 }));
            log.debug(snapshot);
            receiver.saveSnapshot(transferId, new StringInputStream(snapshot, "UTF-8"));
            receiver.commit(transferId);

            ArgumentCaptor<String> transferIdCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Set> createdNodesCaptor = ArgumentCaptor.forClass(Set.class);
            ArgumentCaptor<Set> updatedNodesCaptor = ArgumentCaptor.forClass(Set.class);
            ArgumentCaptor<Set> deletedNodesCaptor = ArgumentCaptor.forClass(Set.class);
            verify(mockedPolicyHandler, times(1)).onEndInboundTransfer(transferIdCaptor.capture(), 
                    createdNodesCaptor.capture(), updatedNodesCaptor.capture(), deletedNodesCaptor.capture());
            assertEquals(transferId, transferIdCaptor.getValue());
            Set capturedDeletedNodes = deletedNodesCaptor.getValue();
            assertEquals(3, capturedDeletedNodes.size());
            assertTrue(capturedDeletedNodes.contains(deletedNode8.getNodeRef()));
            assertTrue(capturedDeletedNodes.contains(deletedNode2.getNodeRef()));
            assertTrue(capturedDeletedNodes.contains(deletedNode11.getNodeRef()));
        }
        finally
        {
            endTransaction();
        }

        startNewTransaction();
        try
        {
            log.debug("Test success of transfer...");
            TransferProgress progress = receiver.getProgressMonitor().getProgress(transferId);
            assertEquals(TransferProgress.Status.COMPLETE, progress.getStatus());

            NodeRef archiveNode8 = new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, node8.getNodeRef().getId()); 
            NodeRef archiveNode2 = new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, node2.getNodeRef().getId()); 
            NodeRef archiveNode11 = new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, node11.getNodeRef().getId()); 

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
            
            log.debug("Testing existence of original node: " + node8.getNodeRef());
            assertFalse(nodeService.exists(node8.getNodeRef()));

            log.debug("Testing existence of original node: " + node2.getNodeRef());
            assertFalse(nodeService.exists(node2.getNodeRef()));

            log.debug("Testing existence of original node: " + node11.getNodeRef());
            assertFalse(nodeService.exists(node11.getNodeRef()));
            
            log.debug("Successfully tested non-existence of all original nodes");
            
            log.debug("Progress indication: " + progress.getCurrentPosition() + "/" + progress.getEndPosition());
        }
        finally
        {
            endTransaction();
        }
        System.out.println("Now try to restore orphan node 2.");

        reset(mockedPolicyHandler);

        String errorMsgId = null;
        startNewTransaction();
        try
        {
            // try to restore node 2. Expect an "orphan" failure, since its parent (node11) is deleted
            transferId = receiver.start();
            String snapshot = createSnapshot(Arrays.asList(new TransferManifestNode[] { node2 }));
            log.debug(snapshot);
            receiver.saveSnapshot(transferId, new StringInputStream(snapshot, "UTF-8"));
            receiver.saveContent(transferId, node2.getUuid(), new ByteArrayInputStream(dummyContentBytes));
            try
            {
                receiver.commit(transferId);
                fail("Expected an exception");
            }
            catch (TransferException ex)
            {
                // Expected
                errorMsgId = ex.getMsgId();

                ArgumentCaptor<String> transferIdCaptor = ArgumentCaptor.forClass(String.class);
                ArgumentCaptor<Set> createdNodesCaptor = ArgumentCaptor.forClass(Set.class);
                ArgumentCaptor<Set> updatedNodesCaptor = ArgumentCaptor.forClass(Set.class);
                ArgumentCaptor<Set> deletedNodesCaptor = ArgumentCaptor.forClass(Set.class);
                
                verify(mockedPolicyHandler, times(1)).onEndInboundTransfer(transferIdCaptor.capture(), 
                        createdNodesCaptor.capture(), updatedNodesCaptor.capture(), deletedNodesCaptor.capture());
                
                assertEquals(transferId, transferIdCaptor.getValue());
                assertTrue(createdNodesCaptor.getValue().isEmpty());
                assertTrue(updatedNodesCaptor.getValue().isEmpty());
                assertTrue(deletedNodesCaptor.getValue().isEmpty());
            }
        }
        catch (Exception ex)
        {
            receiver.end(transferId);
            throw ex;
        }
        finally
        {
            endTransaction();
        }

        startNewTransaction();
        try
        {
            TransferProgress progress = receiver.getProgressMonitor().getProgress(transferId);
            assertEquals(TransferProgress.Status.ERROR, progress.getStatus());
            log.debug("Progress indication: " + progress.getCurrentPosition() + "/" + progress.getEndPosition());
            assertNotNull("Progress error", progress.getError());
            assertTrue(progress.getError() instanceof Exception);
            assertTrue(errorMsgId, errorMsgId.contains("orphan"));
        }
        finally
        {
            endTransaction();
        }
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
        setDefaultRollback(true);
        startNewTransaction();
        String transferId = receiver.start();

        TransferManifestNormalNode node1 = createContentNode(transferId);
        TransferManifestNormalNode node2 = createContentNode(transferId);
        TransferManifestNormalNode node11 = createFolderNode(transferId);

        associatePeers(node1, node2);
        moveNode(node2, node11);

        TransferManifestDeletedNode deletedNode11 = createDeletedNode(node11);

        endTransaction();

        List<TransferManifestNode> nodes = new ArrayList<TransferManifestNode>();
        
        
        //First we'll just send a folder node
        nodes.add(node11);
        
        this.setDefaultRollback(false);
        startNewTransaction();
        try
        {
            String snapshot = createSnapshot(nodes);
            log.debug(snapshot);
            receiver.saveSnapshot(transferId, new StringInputStream(snapshot, "UTF-8"));

            for (TransferManifestNode node : nodes)
            {
                receiver.saveContent(transferId, node.getUuid(), new ByteArrayInputStream(dummyContentBytes));
            }
            receiver.commit(transferId);

            for (TransferManifestNode node : nodes)
            {
                assertTrue(nodeService.exists(node.getNodeRef()));
            }
        }
        finally
        {
            receiver.end(transferId);
            endTransaction();
        }


        //Now we delete the folder
        startNewTransaction();
        try
        {
            transferId = receiver.start();
            String snapshot = createSnapshot(Arrays.asList(new TransferManifestNode[] { deletedNode11 }));
            log.debug(snapshot);
            receiver.saveSnapshot(transferId, new StringInputStream(snapshot, "UTF-8"));
            receiver.commit(transferId);
        }
        finally
        {
            receiver.end(transferId);
            endTransaction();
        }

        startNewTransaction();
        try
        {
            log.debug("Test success of transfer...");
            TransferProgress progress = receiver.getProgressMonitor().getProgress(transferId);
            assertEquals(TransferProgress.Status.COMPLETE, progress.getStatus());

            NodeRef archivedNodeRef = new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, deletedNode11.getNodeRef().getId());
            assertTrue(nodeService.exists(archivedNodeRef));
            assertTrue(nodeService.hasAspect(archivedNodeRef, ContentModel.ASPECT_ARCHIVED));
            log.debug("Successfully tested existence of archive node: " + deletedNode11.getNodeRef());
            
            log.debug("Successfully tested existence of all archive nodes");
            
            log.debug("Testing existence of original node: " + node11.getNodeRef());
            assertFalse(nodeService.exists(node11.getNodeRef()));

            log.debug("Successfully tested non-existence of all original nodes");
            
            log.debug("Progress indication: " + progress.getCurrentPosition() + "/" + progress.getEndPosition());
        }
        finally
        {
            endTransaction();
        }


        //Finally we transfer node2 and node11 (in that order)
        startNewTransaction();
        try
        {
            transferId = receiver.start();
            String snapshot = createSnapshot(Arrays.asList(new TransferManifestNode[] { node2, node11 }));
            log.debug(snapshot);
            receiver.saveSnapshot(transferId, new StringInputStream(snapshot, "UTF-8"));
            receiver.saveContent(transferId, node2.getUuid(), new ByteArrayInputStream(dummyContentBytes));
            receiver.commit(transferId);
        }
        catch (Exception ex)
        {
            fail("Test of ALF-2772 failed: " + ex.getMessage());
        }
        finally
        {
            receiver.end(transferId);
            endTransaction();
        }

    }
    
    
    public void testAsyncCommit() throws Exception
    {
        log.info("testAsyncCommit");

        this.setDefaultRollback(false);

        startNewTransaction();
        final String transferId = receiver.start();
        endTransaction();

        startNewTransaction();
        final List<TransferManifestNode> nodes = new ArrayList<TransferManifestNode>();
        final TransferManifestNormalNode node1 = createContentNode(transferId);
        nodes.add(node1);
        final TransferManifestNormalNode node2 = createContentNode(transferId);
        nodes.add(node2);
        TransferManifestNode node3 = createContentNode(transferId);
        nodes.add(node3);
        TransferManifestNode node4 = createContentNode(transferId);
        nodes.add(node4);
        TransferManifestNode node5 = createContentNode(transferId);
        nodes.add(node5);
        TransferManifestNode node6 = createContentNode(transferId);
        nodes.add(node6);
        TransferManifestNode node7 = createContentNode(transferId);
        nodes.add(node7);
        TransferManifestNode node8 = createFolderNode(transferId);
        nodes.add(node8);
        TransferManifestNode node9 = createFolderNode(transferId);
        nodes.add(node9);
        TransferManifestNode node10 = createFolderNode(transferId);
        nodes.add(node10);
        TransferManifestNormalNode node11 = createFolderNode(transferId);
        nodes.add(node11);
        TransferManifestNode node12 = createFolderNode(transferId);
        nodes.add(node12);

        associatePeers(node1, node2);
        moveNode(node2, node11);

        endTransaction();

        String snapshot = createSnapshot(nodes);

        startNewTransaction();
        receiver.saveSnapshot(transferId, new StringInputStream(snapshot, "UTF-8"));
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
        XMLTransferManifestWriter manifestWriter = new XMLTransferManifestWriter();
        StringWriter output = new StringWriter();
        manifestWriter.startTransferManifest(output);
        TransferManifestHeader header = new TransferManifestHeader();
        header.setCreatedDate(new Date());
        header.setNodeCount(nodes.size());
        header.setRepositoryId("repo 1");
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
    private TransferManifestNormalNode createContentNode(String transferId) throws Exception
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

    private TransferManifestNormalNode createFolderNode(String transferId) throws Exception
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
        
        String nodeName = uuid + ".folder" + getNameSuffix();

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
