/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.transfer;

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
import org.alfresco.repo.security.authentication.AuthenticationComponent;
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
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.transfer.TransferException;
import org.alfresco.service.cmr.transfer.TransferProgress;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.filters.StringInputStream;
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
    private String dummyContent;
    private byte[] dummyContentBytes;

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
        this.dummyContent = "This is some dummy content.";
        this.dummyContentBytes = dummyContent.getBytes("UTF-8");
        setTransactionDefinition(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        authenticationComponent.setSystemUserAsCurrentUser();
    }

    public void testStartAndEnd() throws Exception
    {
        log.info("testStartAndEnd");
        startNewTransaction();
        try
        {
            String transferId = receiver.start();
            System.out.println("TransferId == " + transferId);

            File stagingFolder = receiver.getStagingFolder(transferId);
            assertTrue(receiver.getStagingFolder(transferId).exists());

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
                fail("Successfully ended with transfer id that doesn't own lock.");
            }
            catch (TransferException ex)
            {
                // Expected
            }
            receiver.end(transferId);
            assertFalse(stagingFolder.exists());

            receiver.end(receiver.start());
        }
        finally
        {
            endTransaction();
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

    public void testNodeDeleteAndRestore() throws Exception
    {
        log.info("testNodeDeleteAndRestore");

        this.setDefaultRollback(false);
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
            for (TransferManifestNode node : nodes)
            {
                assertTrue(nodeService.exists(node.getNodeRef()));
            }
        }
        finally
        {
            endTransaction();
        }

        startNewTransaction();
        try
        {
            // Now delete nodes 8, 2, and 11 (2 and 11 are parent/child)
            transferId = receiver.start();
            String snapshot = createSnapshot(Arrays.asList(new TransferManifestNode[] { deletedNode8, deletedNode2,
                    deletedNode11 }));
            receiver.saveSnapshot(transferId, new StringInputStream(snapshot, "UTF-8"));
            receiver.commit(transferId);
        }
        finally
        {
            endTransaction();
        }

        startNewTransaction();
        try
        {
            assertTrue(nodeService.exists(deletedNode8.getNodeRef()));
            assertTrue(nodeService.hasAspect(deletedNode8.getNodeRef(), ContentModel.ASPECT_ARCHIVED));
            assertTrue(nodeService.exists(deletedNode2.getNodeRef()));
            assertTrue(nodeService.hasAspect(deletedNode2.getNodeRef(), ContentModel.ASPECT_ARCHIVED));
            assertTrue(nodeService.exists(deletedNode11.getNodeRef()));
            assertTrue(nodeService.hasAspect(deletedNode11.getNodeRef(), ContentModel.ASPECT_ARCHIVED));
            TransferProgress progress = receiver.getProgressMonitor().getProgress(transferId);
            assertEquals(TransferProgress.Status.COMPLETE, progress.getStatus());
            log.debug("Progress indication: " + progress.getCurrentPosition() + "/" + progress.getEndPosition());
        }
        finally
        {
            endTransaction();
        }

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
                fail("Expected an exception!");
            }
            catch (TransferException ex)
            {
                // Expected
                errorMsgId = ex.getMsgId();
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
        deletedNode.setNodeRef(new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, nodeToDelete.getNodeRef().getId()));
        deletedNode.setParentPath(nodeToDelete.getParentPath());
        deletedNode.setPrimaryParentAssoc(nodeToDelete.getPrimaryParentAssoc());
        deletedNode.setUuid(nodeToDelete.getUuid());
        return deletedNode;
    }

    /**
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

        AssociationRef newAssoc = new AssociationRef(source.getNodeRef(), ContentModel.ASSOC_ATTACHMENTS, target
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

        NodeRef parentFolder = receiver.getTempFolder(transferId);
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

        NodeRef parentFolder = receiver.getTempFolder(transferId);
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
