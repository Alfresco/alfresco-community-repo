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
import org.alfresco.repo.transfer.manifest.TransferManifestDeletedNode;
import org.alfresco.repo.transfer.manifest.TransferManifestHeader;
import org.alfresco.repo.transfer.manifest.TransferManifestNode;
import org.alfresco.repo.transfer.manifest.TransferManifestNormalNode;
import org.alfresco.repo.transfer.manifest.XMLTransferManifestWriter;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.transfer.TransferException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.filters.StringInputStream;

/**
 * Unit test for RepoTransferReceiverImpl
 * 
 * @author Brian Remmington
 */
public class RepoTransferReceiverImplTest extends BaseAlfrescoSpringTest
{
    private static int fileCount = 0;
    private static final Log log = LogFactory.getLog(RepoTransferReceiverImplTest.class);
    
    private RepoTransferReceiverImpl receiver;
    private String dummyContent;
    private byte[] dummyContentBytes;

    /**
     * Called during the transaction setup
     */
    @SuppressWarnings("deprecation")
    protected void onSetUpInTransaction() throws Exception
    {
        System.out.println("java.io.tmpdir == " + System.getProperty("java.io.tmpdir"));
        super.onSetUpInTransaction();

        // Get the required services
        this.receiver = (RepoTransferReceiverImpl) this.getApplicationContext().getBean("transferReceiver");
        this.dummyContent = "This is some dummy content.";
        this.dummyContentBytes = dummyContent.getBytes("UTF-8");
    }

    public void testStartAndEnd() throws Exception
    {
        String transferId = receiver.start();
        System.out.println("TransferId == " + transferId);

        File stagingFolder = receiver.getStagingFolder(transferId);
        assertTrue(receiver.getStagingFolder(transferId).exists());

        try
        {
            receiver.start();
            fail("Successfully started twice!");
        } catch (TransferException ex)
        {
            // Expected
        }
        try
        {
            receiver.end(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, GUID.generate()).toString());
            fail("Successfully ended with transfer id that doesn't own lock.");
        } catch (TransferException ex)
        {
            // Expected
        }
        receiver.end(transferId);
        assertFalse(stagingFolder.exists());

        receiver.end(receiver.start());
    }

    public void testSaveContent() throws Exception
    {
        String transferId = receiver.start();
        try
        {
            String contentId = "mytestcontent";
            receiver.saveContent(transferId, contentId, new ByteArrayInputStream(dummyContentBytes));
            File contentFile = new File(receiver.getStagingFolder(transferId), contentId);
            assertTrue(contentFile.exists());
            assertEquals(dummyContentBytes.length, contentFile.length());
        } finally
        {
            receiver.end(transferId);
        }
    }

    public void testSaveSnapshot() throws Exception
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
        } finally
        {
            receiver.end(transferId);
            if (snapshotFile != null) {
                assertFalse(snapshotFile.exists());
            }
        }
    }
    
    public void testBasicCommit() throws Exception {
        String transferId = receiver.start();
        try
        {
            TransferManifestNode node = createContentNode(transferId);
            List<TransferManifestNode> nodes = new ArrayList<TransferManifestNode>();
            nodes.add(node);
            String snapshot = createSnapshot(nodes);

            receiver.saveSnapshot(transferId, new StringInputStream(snapshot, "UTF-8"));
            receiver.saveContent(transferId, node.getUuid(), new ByteArrayInputStream(dummyContentBytes));
            receiver.commit(transferId);

            assertTrue(nodeService.exists(node.getNodeRef()));
            nodeService.deleteNode(node.getNodeRef());
        } catch (Exception ex)
        {
            receiver.end(transferId);
            throw ex;
        }
        
    }
    
    public void testMoreComplexCommit() throws Exception {
        String transferId = receiver.start();
        try
        {
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
            
            String snapshot = createSnapshot(nodes);

            receiver.saveSnapshot(transferId, new StringInputStream(snapshot, "UTF-8"));
            
            for (TransferManifestNode node : nodes) {
                receiver.saveContent(transferId, node.getUuid(), new ByteArrayInputStream(dummyContentBytes));
            }
            receiver.commit(transferId);

            assertTrue(nodeService.getAspects(node1.getNodeRef()).contains(ContentModel.ASPECT_ATTACHABLE));
            assertFalse(nodeService.getSourceAssocs(node2.getNodeRef(), ContentModel.ASSOC_ATTACHMENTS).isEmpty());
            for (TransferManifestNode node : nodes) {
                assertTrue(nodeService.exists(node.getNodeRef()));
            }
        } catch (Exception ex)
        {
            receiver.end(transferId);
            throw ex;
        }
        
    }
    
    public void testNodeDeleteAndRestore() throws Exception {
        String transferId = receiver.start();
        try
        {
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
            
            String snapshot = createSnapshot(nodes);
            log.debug(snapshot);
            receiver.saveSnapshot(transferId, new StringInputStream(snapshot, "UTF-8"));
            
            for (TransferManifestNode node : nodes) {
                receiver.saveContent(transferId, node.getUuid(), new ByteArrayInputStream(dummyContentBytes));
            }
            receiver.commit(transferId);

            assertTrue(nodeService.getAspects(node1.getNodeRef()).contains(ContentModel.ASPECT_ATTACHABLE));
            assertFalse(nodeService.getSourceAssocs(node2.getNodeRef(), ContentModel.ASSOC_ATTACHMENTS).isEmpty());
            for (TransferManifestNode node : nodes) {
                assertTrue(nodeService.exists(node.getNodeRef()));
            }
            
            //Now delete nodes 8, 2, and 11 (2 and 11 are parent/child)
            TransferManifestDeletedNode deletedNode8 = createDeletedNode(node8);
            TransferManifestDeletedNode deletedNode2 = createDeletedNode(node2);
            TransferManifestDeletedNode deletedNode11 = createDeletedNode(node11);
            transferId = receiver.start();
            snapshot = createSnapshot(Arrays.asList(new TransferManifestNode[] {deletedNode8, deletedNode2, deletedNode11}));
            receiver.saveSnapshot(transferId, new StringInputStream(snapshot, "UTF-8"));
            receiver.commit(transferId);
            assertTrue(nodeService.exists(deletedNode8.getNodeRef()));
            assertTrue(nodeService.hasAspect(deletedNode8.getNodeRef(), ContentModel.ASPECT_ARCHIVED));
            assertTrue(nodeService.exists(deletedNode2.getNodeRef()));
            assertTrue(nodeService.hasAspect(deletedNode2.getNodeRef(), ContentModel.ASPECT_ARCHIVED));
            assertTrue(nodeService.exists(deletedNode11.getNodeRef()));
            assertTrue(nodeService.hasAspect(deletedNode11.getNodeRef(), ContentModel.ASPECT_ARCHIVED));
            
            //try to restore node 2. Expect an "orphan" failure, since its parent (node11) is deleted
            transferId = receiver.start();
            snapshot = createSnapshot(Arrays.asList(new TransferManifestNode[] {node2}));
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
                assertTrue(ex.getMsgId(), ex.getMsgId().contains("orphan"));
            }
            
        } catch (Exception ex)
        {
            receiver.end(transferId);
            throw ex;
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
        
        for (ChildAssociationRef parent : currentParents) {
            if (!parent.isPrimary()) {
                newParents.add(parent);
            } else {
                ChildAssociationRef newPrimaryAssoc = new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, 
                        newParent.getNodeRef(), parent.getQName(), parent.getChildRef(), true, -1); 
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

    private void associatePeers(TransferManifestNormalNode source, TransferManifestNormalNode target) {
        List<AssociationRef> currentReferencedPeers = source.getTargetAssocs();
        if (currentReferencedPeers == null) {
            currentReferencedPeers = new ArrayList<AssociationRef>();
            source.setTargetAssocs(currentReferencedPeers);
        }
        
        List<AssociationRef> currentRefereePeers = target.getSourceAssocs();
        if (currentRefereePeers == null) {
            currentRefereePeers = new ArrayList<AssociationRef>();
            target.setSourceAssocs(currentRefereePeers);
        }
    
        Set<QName> aspects = source.getAspects();
        if (aspects == null ) {
            aspects = new HashSet<QName>();
            source.setAspects(aspects);
        }
        aspects.add(ContentModel.ASPECT_ATTACHABLE);
        
        AssociationRef newAssoc = new AssociationRef(source.getNodeRef(), ContentModel.ASSOC_ATTACHMENTS, target.getNodeRef());
        currentRefereePeers.add(newAssoc);
        currentReferencedPeers.add(newAssoc);
    }
    
    private String createSnapshot(List<TransferManifestNode> nodes) throws Exception {
        XMLTransferManifestWriter manifestWriter = new XMLTransferManifestWriter();
        StringWriter output = new StringWriter();
        manifestWriter.startTransferManifest(output);
        TransferManifestHeader header = new TransferManifestHeader();
        header.setCreatedDate(new Date());
        manifestWriter.writeTransferManifestHeader(header);
        for (TransferManifestNode node : nodes) {
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
        String nodeName = transferId + ".testnode" + getNameSuffix();

        List<ChildAssociationRef> parents = new ArrayList<ChildAssociationRef>();
        ChildAssociationRef primaryAssoc = new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, parentFolder, QName.createQName(
                NamespaceService.CONTENT_MODEL_1_0_URI, nodeName), node.getNodeRef(), true, -1);
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
        String nodeName = transferId + ".folder" + getNameSuffix();

        List<ChildAssociationRef> parents = new ArrayList<ChildAssociationRef>();
        ChildAssociationRef primaryAssoc = new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, parentFolder, QName.createQName(
                NamespaceService.CONTENT_MODEL_1_0_URI, nodeName), node.getNodeRef(), true, -1);
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

    private String getNameSuffix() {
        return "" + fileCount++;
    }
}
