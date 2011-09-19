/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.node.archive;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.StoreArchiveMap;
import org.alfresco.repo.node.archive.RestoreNodeReport.RestoreStatus;
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.TestWithUserUtils;
import org.springframework.context.ApplicationContext;

/**
 * Test the archive and restore functionality provided by the low-level
 * node service.
 * 
 * @author Derek Hulley
 */
public class ArchiveAndRestoreTest extends TestCase
{
    private static final String USER_A = "aaaaa";
    private static final String USER_B = "bbbbb";
    private static final QName ASPECT_ATTACHABLE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "attachable");
    private static final QName ASSOC_ATTACHMENTS = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "attachments");
    private static final QName QNAME_A = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "a");
    private static final QName QNAME_B = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "b");
    private static final QName QNAME_AA = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "aa");
    private static final QName QNAME_BB = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "bb");
    
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private NodeArchiveService nodeArchiveService;
    private NodeService nodeService;
    private PermissionService permissionService;
    private AuthenticationComponent authenticationComponent;
    private MutableAuthenticationService authenticationService;
    private OwnableService ownableService;
    private TransactionService transactionService;
    
    private UserTransaction txn;
    private StoreRef workStoreRef;
    private NodeRef workStoreRootNodeRef;
    private StoreRef archiveStoreRef;
    private NodeRef archiveStoreRootNodeRef;

    private NodeRef a;
    private NodeRef b;
    private NodeRef aa;
    private NodeRef bb;
    AssociationRef assocAtoB;
    AssociationRef assocAAtoBB;
    ChildAssociationRef childAssocAtoAA;
    ChildAssociationRef childAssocBtoBB;
    ChildAssociationRef childAssocBtoAA;
    ChildAssociationRef childAssocAtoBB;
    private NodeRef a_;
    private NodeRef b_;
    private NodeRef aa_;
    private NodeRef bb_;
    ChildAssociationRef childAssocAtoAA_;
    ChildAssociationRef childAssocBtoBB_;
    
    @Override
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean("ServiceRegistry");
        nodeArchiveService = (NodeArchiveService) ctx.getBean("nodeArchiveService");
        nodeService = serviceRegistry.getNodeService();
        permissionService = serviceRegistry.getPermissionService();
        authenticationService = serviceRegistry.getAuthenticationService();
        authenticationComponent = (AuthenticationComponent) ctx.getBean("authenticationComponent");
        ownableService = (OwnableService) ctx.getBean("ownableService");
        transactionService = serviceRegistry.getTransactionService();
        
        // Start a transaction
        txn = transactionService.getUserTransaction();
        txn.begin();
        
        // downgrade integrity checks
        IntegrityChecker.setWarnInTransaction();
        
        try
        {
            authenticationComponent.setSystemUserAsCurrentUser();
            // Create the work store
            workStoreRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, getName() + System.currentTimeMillis());
            workStoreRootNodeRef = nodeService.getRootNode(workStoreRef);
            archiveStoreRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "archive" + getName() + System.currentTimeMillis());
            archiveStoreRootNodeRef = nodeService.getRootNode(archiveStoreRef);
            
            // Map the work store to the archive store.  This will already be wired into the NodeService.
            StoreArchiveMap archiveMap = (StoreArchiveMap) ctx.getBean("storeArchiveMap");
            archiveMap.put(workStoreRef, archiveStoreRef);
            
            TestWithUserUtils.createUser(USER_A, USER_A, workStoreRootNodeRef, nodeService, authenticationService);
            TestWithUserUtils.createUser(USER_B, USER_B, workStoreRootNodeRef, nodeService, authenticationService);

            // grant A and B rights to the work store
            permissionService.setPermission(
                    workStoreRootNodeRef,
                    USER_A,
                    PermissionService.ALL_PERMISSIONS,
                    true);
            permissionService.setPermission(
                    workStoreRootNodeRef,
                    USER_B,
                    PermissionService.ALL_PERMISSIONS,
                    true);
            
        }
        finally
        {
            authenticationComponent.clearCurrentSecurityContext();
        }
        // authenticate as normal user
        authenticationService.authenticate(USER_A, USER_A.toCharArray());
        createNodeStructure();
    }
    
    @Override
    public void tearDown() throws Exception
    {
        try
        {
            txn.rollback();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
        AuthenticationUtil.clearCurrentSecurityContext();
    }
    
    /**
     * Create the following: 
     * <pre>
     *        root
     *       /  |
     *      /   |
     *     /    |
     *    /     |
     *   A  <-> B
     *   |\    /|
     *   | \  / |
     *   |  \/  |
     *   |  /\  |
     *   | /  \ |
     *   |/    \|
     *   AA <-> BB
     * </pre>
     * Explicit UUIDs are used for debugging purposes.  Live nodes are <b>cm:countable</b> with the
     * <b>cm:counter</b> property.
     * <p>
     * <b>A</b>, <b>B</b>, <b>AA</b> and <b>BB</b> are set up to archive automatically
     * on deletion.
     */
    private void createNodeStructure() throws Exception
    {
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(5);

        properties.put(ContentModel.PROP_COUNTER, 50);
        
        properties.put(ContentModel.PROP_NODE_UUID, "a");
        a = nodeService.createNode(
                workStoreRootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QNAME_A,
                ContentModel.TYPE_FOLDER,
                properties).getChildRef();
        nodeService.addAspect(a, ASPECT_ATTACHABLE, null);
        properties.put(ContentModel.PROP_NODE_UUID, "aa");
        childAssocAtoAA = nodeService.createNode(
                a,
                ContentModel.ASSOC_CONTAINS,
                QNAME_AA,
                ContentModel.TYPE_CONTENT,
                properties);
        aa = childAssocAtoAA.getChildRef();
        nodeService.addAspect(aa, ASPECT_ATTACHABLE, null);
        properties.put(ContentModel.PROP_NODE_UUID, "b");
        b = nodeService.createNode(
                workStoreRootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QNAME_B,
                ContentModel.TYPE_FOLDER,
                properties).getChildRef();
        properties.put(ContentModel.PROP_NODE_UUID, "bb");
        childAssocBtoBB = nodeService.createNode(
                b,
                ContentModel.ASSOC_CONTAINS,
                QNAME_BB,
                ContentModel.TYPE_CONTENT,
                properties);
        bb = childAssocBtoBB.getChildRef();
        assocAtoB = nodeService.createAssociation(a, b, ASSOC_ATTACHMENTS);
        assocAAtoBB = nodeService.createAssociation(aa, bb, ASSOC_ATTACHMENTS);
        childAssocBtoAA = nodeService.addChild(
                b,
                aa,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "aa"));
        childAssocAtoBB = nodeService.addChild(
                a,
                bb,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "bb"));
        
        // deduce the references
        a_ = new NodeRef(archiveStoreRef, a.getId());
        b_ = new NodeRef(archiveStoreRef, b.getId());
        aa_ = new NodeRef(archiveStoreRef, aa.getId());
        bb_ = new NodeRef(archiveStoreRef, bb.getId());
        childAssocAtoAA_ = new ChildAssociationRef(
                childAssocAtoAA.getTypeQName(),
                a_,
                childAssocAtoAA.getQName(),
                aa_);
        childAssocBtoBB_ = new ChildAssociationRef(
                childAssocBtoBB.getTypeQName(),
                b_,
                childAssocBtoBB.getQName(),
                bb_);
    }
    
    private void verifyNodeExistence(NodeRef nodeRef, boolean exists)
    {
        assertEquals("Node should " + (exists ? "" : "not ") + "exist", exists, nodeService.exists(nodeRef));
    }
    
    private void verifyChildAssocExistence(ChildAssociationRef childAssocRef, boolean exists)
    {
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(
                childAssocRef.getParentRef(),
                childAssocRef.getTypeQName(),
                childAssocRef.getQName());
        if (exists)
        {
            assertEquals("Expected exactly one match for child association: " + childAssocRef, 1, childAssocs.size());
        }
        else
        {
            assertEquals("Expected zero matches for child association: " + childAssocRef, 0, childAssocs.size());
        }
    }
    
    private void verifyTargetAssocExistence(AssociationRef assocRef, boolean exists)
    {
        List<AssociationRef> assocs = nodeService.getTargetAssocs(
                assocRef.getSourceRef(),
                assocRef.getTypeQName());
        if (exists)
        {
            assertEquals("Expected exactly one match for target association: " + assocRef, 1, assocs.size());
        }
        else
        {
            assertEquals("Expected zero matches for target association: " + assocRef, 0, assocs.size());
        }
    }
    
    private void verifyPropertyExistence(NodeRef nodeRef, QName propertyQName, boolean exists)
    {
        assertEquals(
                "Property is not present " + nodeRef + " - " + propertyQName,
                exists, nodeService.getProperty(nodeRef, propertyQName) != null);
    }
    
    private void verifyAspectExistence(NodeRef nodeRef, QName aspectQName, boolean exists)
    {
        assertEquals(
                "Aspect is not present " + nodeRef + " - " + aspectQName,
                exists, nodeService.hasAspect(nodeRef, aspectQName));
    }
    
    public void verifyAll()
    {
        // work store references
        verifyNodeExistence(a, true);
        verifyNodeExistence(b, true);
        verifyNodeExistence(aa, true);
        verifyNodeExistence(bb, true);
        verifyChildAssocExistence(childAssocAtoAA, true);
        verifyChildAssocExistence(childAssocBtoBB, true);
        verifyChildAssocExistence(childAssocAtoBB, true);
        verifyChildAssocExistence(childAssocBtoAA, true);
        verifyTargetAssocExistence(assocAtoB, true);
        verifyTargetAssocExistence(assocAAtoBB, true);
        verifyPropertyExistence(a, ContentModel.PROP_COUNTER, true);
        verifyAspectExistence(a, ContentModel.ASPECT_COUNTABLE, true);
        verifyPropertyExistence(b, ContentModel.PROP_COUNTER, true);
        verifyAspectExistence(b, ContentModel.ASPECT_COUNTABLE, true);
        verifyPropertyExistence(aa, ContentModel.PROP_COUNTER, true);
        verifyAspectExistence(aa, ContentModel.ASPECT_COUNTABLE, true);
        verifyPropertyExistence(bb, ContentModel.PROP_COUNTER, true);
        verifyAspectExistence(bb, ContentModel.ASPECT_COUNTABLE, true);
        // archive store references
        verifyNodeExistence(a_, false);
        verifyNodeExistence(b_, false);
        verifyNodeExistence(aa_, false);
        verifyNodeExistence(bb_, false);
    }
    
    public void testSetUp() throws Exception
    {
        verifyAll();
    }
    
    public void testGetStoreArchiveNode() throws Exception
    {
        NodeRef archiveNodeRef = nodeService.getStoreArchiveNode(workStoreRef);
        assertEquals("Mapping of archived store is not correct", archiveStoreRootNodeRef, archiveNodeRef);
    }
    
    public void testArchivedAspect() throws Exception
    {
        // delete 'a'
        nodeService.deleteNode(a);
        // check that it has the aspect and that the properties are correct
        assertTrue("Archived aspect not present", nodeService.hasAspect(a_, ContentModel.ASPECT_ARCHIVED));
        Map<QName, Serializable> properties = nodeService.getProperties(a_);
        assertNotNull("Original owner property not present", properties.get(ContentModel.PROP_ARCHIVED_ORIGINAL_OWNER));
        assertEquals("Original owner property is incorrect", USER_A, properties.get(ContentModel.PROP_ARCHIVED_ORIGINAL_OWNER));
    }
    
    public void testArchiveAndRestoreNodeBB() throws Exception
    {
        // delete a child
        nodeService.deleteNode(bb);
        // check
        verifyNodeExistence(b, true);
        verifyNodeExistence(bb, false);
//        verifyChildAssocExistence(childAssocAtoBB, false);
//        verifyChildAssocExistence(childAssocBtoBB, false);
        verifyNodeExistence(b_, false);
        verifyNodeExistence(bb_, true);
        
        // flush
        //AlfrescoTransactionSupport.flush();
        
        // check that the required properties are present and correct
        Map<QName, Serializable> bb_Properties = nodeService.getProperties(bb_);
        ChildAssociationRef bb_originalParent = (ChildAssociationRef) bb_Properties.get(ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC);
        assertNotNull("Original parent not stored", bb_originalParent);
        
        // restore the node
        nodeService.restoreNode(bb_, null, null, null);
        // check
        verifyAll();
    }
    
    public void testArchiveAndRestoreNodeB() throws Exception
    {
        // delete a child
        nodeService.deleteNode(b);
        // check
        verifyNodeExistence(b, false);
        verifyNodeExistence(bb, false);
//        verifyChildAssocExistence(childAssocAtoBB, false);
//        verifyTargetAssocExistence(assocAtoB, false);
//        verifyTargetAssocExistence(assocAAtoBB, false);
        verifyNodeExistence(b_, true);
        verifyNodeExistence(bb_, true);
//        verifyChildAssocExistence(childAssocBtoBB_, true);
        
        // flush
        //AlfrescoTransactionSupport.flush();
        
        // restore the node
        nodeService.restoreNode(b_, null, null, null);
        // check
        verifyAll();
    }
    
    public void testArchiveAndRestoreAll_B_A() throws Exception
    {
        // delete both trees in order 'b', 'a'
        nodeService.deleteNode(b);
        nodeService.deleteNode(a);

        // flush
        //AlfrescoTransactionSupport.flush();
        
        // restore in reverse order
        nodeService.restoreNode(a_, null, null, null);
        nodeService.restoreNode(b_, null, null, null);
        // check
        verifyAll();
    }
    
    public void testArchiveAndRestoreAll_A_B() throws Exception
    {
        // delete both trees in order 'b', 'a'
        nodeService.deleteNode(a);
        nodeService.deleteNode(b);

        // flush
        //AlfrescoTransactionSupport.flush();
        
        // restore in reverse order
        nodeService.restoreNode(b_, null, null, null);
        nodeService.restoreNode(a_, null, null, null);
        // check
        verifyAll();
    }
    
    public void testArchiveAndRestoreWithMissingAssocTargets() throws Exception
    {
        // delete a then b
        nodeService.deleteNode(a);
        nodeService.deleteNode(b);

        // flush
        //AlfrescoTransactionSupport.flush();
        
        // in restoring 'a' first, there will be some associations that won't be recreated
        nodeService.restoreNode(a_, null, null, null);
        nodeService.restoreNode(b_, null, null, null);
        
        // check
        verifyNodeExistence(a, true);
        verifyNodeExistence(b, true);
        verifyNodeExistence(aa, true);
        verifyNodeExistence(bb, true);
        verifyChildAssocExistence(childAssocAtoAA, true);
        verifyChildAssocExistence(childAssocBtoBB, true);
//        verifyChildAssocExistence(childAssocAtoBB, false);
//        verifyChildAssocExistence(childAssocBtoAA, false);
//        verifyTargetAssocExistence(assocAtoB, false);
//        verifyTargetAssocExistence(assocAAtoBB, false);
        verifyNodeExistence(a_, false);
        verifyNodeExistence(b_, false);
        verifyNodeExistence(aa_, false);
        verifyNodeExistence(bb_, false);
    }
    
    /**
     * Ensures that the archival is performed based on the node type.
     */
    public void testTypeDetection()
    {
        // change the type of 'a'
        nodeService.setType(a, ContentModel.TYPE_CONTAINER);
        // delete it
        nodeService.deleteNode(a);
        // it must be gone
        verifyNodeExistence(a, false);
        verifyNodeExistence(a_, false);
    }
    
    /**
     * Attempt to measure how much archiving affects the deletion performance.
     */
    public void testArchiveVsDeletePerformance() throws Exception
    {
        // Start by deleting the node structure and then recreating it.
        // Only measure the delete speed
        int iterations = 100;
        long cumulatedArchiveTimeNs = 0;
        long cumulatedRestoreTimeNs = 0;
        for (int i = 0; i < iterations; i++)
        {
            // timed delete
            long start = System.nanoTime();
            nodeService.deleteNode(b);
            long end = System.nanoTime();
            cumulatedArchiveTimeNs += (end - start);

            // flush
            //AlfrescoTransactionSupport.flush();
            
            // now restore
            start = System.nanoTime();
            nodeService.restoreNode(b_, null, null, null);
            end = System.nanoTime();
            cumulatedRestoreTimeNs += (end - start);
        }
        double averageArchiveTimeMs = (double)cumulatedArchiveTimeNs / 1E6 / (double)iterations;
        double averageRestoreTimeMs = (double)cumulatedRestoreTimeNs / 1E6 / (double)iterations;
        System.out.println("Average archive time: " + averageArchiveTimeMs + " ms");
        System.out.println("Average restore time: " + averageRestoreTimeMs + " ms");
        
        // Now force full deletions and creations
        StoreArchiveMap archiveMap = (StoreArchiveMap) ctx.getBean("storeArchiveMap");
        archiveMap.clear();
        long cumulatedDeleteTimeNs = 0;
        long cumulatedCreateTimeNs = 0;
        for (int i = 0; i < iterations; i++)
        {
            // timed delete
            long start = System.nanoTime();
            nodeService.deleteNode(b);
            long end = System.nanoTime();
            cumulatedDeleteTimeNs += (end - start);
            // delete 'a' as well
            nodeService.deleteNode(a);
            // now rebuild
            start = System.nanoTime();
            createNodeStructure();
            end = System.nanoTime();
            cumulatedCreateTimeNs += (end - start);
        }
        double averageDeleteTimeMs = (double)cumulatedDeleteTimeNs / 1E6 / (double)iterations;
        double averageCreateTimeMs = (double)cumulatedCreateTimeNs / 1E6 / (double)iterations;
        System.out.println("Average delete time: " + averageDeleteTimeMs + " ms");
        System.out.println("Average create time: " + averageCreateTimeMs + " ms");
    }
    
    public void testInTransactionRestore() throws Exception
    {
        RestoreNodeReport report = nodeArchiveService.restoreArchivedNode(a_);
        // expect a failure due to missing archive node
        assertEquals("Expected failure", RestoreStatus.FAILURE_INVALID_ARCHIVE_NODE, report.getStatus());
        // check that our transaction was not affected
        assertEquals("Transaction should still be valid", Status.STATUS_ACTIVE, txn.getStatus());
    }
    
    public void testInTransactionPurge() throws Exception
    {
        nodeArchiveService.purgeArchivedNode(a_);
        // the node should still be there (it was not available to the purge transaction)
        assertEquals("Transaction should still be valid", Status.STATUS_ACTIVE, txn.getStatus());
    }
    
    private void commitAndBeginNewTransaction() throws Exception
    {
        txn.commit();
        txn = transactionService.getUserTransaction();
        txn.begin();
    }
    
    public void testSimple_Create_Commit_Delete_Commit() throws Exception
    {
        commitAndBeginNewTransaction();
        nodeService.deleteNode(a);
        commitAndBeginNewTransaction();
    }
    
    public void testSimple_Create_Delete_Commit() throws Exception
    {
        nodeService.deleteNode(a);
        commitAndBeginNewTransaction();
    }
    
    public void testRestoreToMissingParent() throws Exception
    {
        nodeService.deleteNode(a);
        nodeService.deleteNode(b);
        commitAndBeginNewTransaction();
        
        // attempt to restore b_ to a
        RestoreNodeReport report = nodeArchiveService.restoreArchivedNode(b_, a, null, null);
        assertEquals("Incorrect report status", RestoreStatus.FAILURE_INVALID_PARENT, report.getStatus());
    }
    
    public void testMassRestore() throws Exception
    {
        nodeService.deleteNode(a);
        nodeService.deleteNode(b);
        commitAndBeginNewTransaction();
        
        List<NodeRef> nodesToRestore = new ArrayList<NodeRef>();
        nodesToRestore.add(a_);
        nodesToRestore.add(b_);

        List<RestoreNodeReport> reports = nodeArchiveService.restoreArchivedNodes(nodesToRestore);
        // check that both a and b were restored
        assertEquals("Incorrect number of node reports", 2, reports.size());
        commitAndBeginNewTransaction();
        // all nodes must be restored, but some of the inter a-b assocs might not be
        verifyNodeExistence(a, true);
        verifyNodeExistence(b, true);
        verifyNodeExistence(aa, true);
        verifyNodeExistence(bb, true);
        verifyNodeExistence(a_, false);
        verifyNodeExistence(b_, false);
        verifyNodeExistence(aa_, false);
        verifyNodeExistence(bb_, false);
    }
    
    public void testMassPurge() throws Exception
    {
        nodeService.deleteNode(a);
        nodeService.deleteNode(b);
        commitAndBeginNewTransaction();
        
        // check that archived nodes are visible
        verifyNodeExistence(a_, true);
        verifyNodeExistence(b_, true);
        
        nodeArchiveService.purgeAllArchivedNodes(workStoreRef);

        commitAndBeginNewTransaction();
        // all nodes must be gone
        verifyNodeExistence(a, false);
        verifyNodeExistence(b, false);
        verifyNodeExistence(aa, false);
        verifyNodeExistence(bb, false);
        verifyNodeExistence(a_, false);
        verifyNodeExistence(b_, false);
        verifyNodeExistence(aa_, false);
        verifyNodeExistence(bb_, false);
    }
    
    public void testDeletedOwnership() throws Exception
    {
        // check that A is the current owner of 'b'
        String bOwner = ownableService.getOwner(b);
        assertEquals("User A must own 'b'", USER_A, bOwner);
        // user B deletes 'b'
        authenticationService.authenticate(USER_B, USER_B.toCharArray());
        nodeService.deleteNode(b);
        // check that B is the owner of 'b_'
        String b_Owner = ownableService.getOwner(b_);
        assertEquals("User B must own 'b_'", USER_B, b_Owner);
    }
    
    /**
     * Check that node ownership changes correctly
     */
    public void testPermissionsForRestore() throws Exception
    {
        // user A deletes 'a'
        authenticationService.authenticate(USER_A, USER_A.toCharArray());
        nodeService.deleteNode(a);
        // user B deletes 'b'
        authenticationService.authenticate(USER_B, USER_B.toCharArray());
        nodeService.deleteNode(b);

        commitAndBeginNewTransaction();

        List<NodeRef> nodesToRestore = new ArrayList<NodeRef>();
        nodesToRestore.add(a_);
        nodesToRestore.add(b_);

        // user B can't see archived 'a'
        List<RestoreNodeReport> restoredByB = nodeArchiveService.restoreArchivedNodes(nodesToRestore);
        for (RestoreNodeReport restoreNodeReport : restoredByB)
        {
            if (restoreNodeReport.getArchivedNodeRef().equals(a_))
            {
                assertEquals(
                        "A user's node should not be restorable by B",
                        RestoreStatus.FAILURE_PERMISSION, restoreNodeReport.getStatus());
            }
            else if (restoreNodeReport.getArchivedNodeRef().equals(b_))
            {
                assertEquals(
                        "B user's node should have been restored by B",
                        RestoreStatus.SUCCESS, restoreNodeReport.getStatus());
            }
        }
    }
    
    /**
     * Deny the current user the rights to write to the destination location
     * and ensure that the use-case is handled properly.
     */
    public void testPermissionsLackingOnDestination() throws Exception
    {
        // remove 'b', deny permissions to workspace root and attempt a restore
        nodeService.deleteNode(b);
        permissionService.setPermission(workStoreRootNodeRef, USER_B, PermissionService.ADD_CHILDREN, false);
        commitAndBeginNewTransaction();
        
        // the restore of b should fail for user B
        authenticationService.authenticate(USER_B, USER_B.toCharArray());
        RestoreNodeReport report = nodeArchiveService.restoreArchivedNode(b_);
        assertEquals("Expected permission denied status", RestoreStatus.FAILURE_PERMISSION, report.getStatus());
    }
    
    /**
     * Check that the existence of the node in the archive store doesn't prevent archival.
     * It is possible to restore a node to the SpacesStore from some other source.  When
     * that node is archived, the currently archived node must be overwritten.
     */
    public void testAR1519ArchiveCleansDuplicateUuid() throws Exception
    {
        // Delete the child node
        nodeService.deleteNode(b);
        verifyNodeExistence(b_, true);
        // Delete the original parent node
        nodeService.deleteNode(a);
        verifyNodeExistence(a_, true);
        // Now recreate a and b (they have been separated in the archive store)
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
        props.put(ContentModel.PROP_NODE_UUID, a.getId());
        NodeRef aRecreated = nodeService.createNode(
                workStoreRootNodeRef, 
                ContentModel.ASSOC_CHILDREN, 
                ContentModel.ASSOC_CHILDREN, 
                ContentModel.TYPE_CONTENT, 
                props).getChildRef();
        assertEquals("NodeRef for recreated node should be the same as the original", a, aRecreated);
        props.put(ContentModel.PROP_NODE_UUID, b.getId());
        NodeRef bRecreated = nodeService.createNode(
                a, 
                ContentModel.ASSOC_CHILDREN, 
                ContentModel.ASSOC_CHILDREN, 
                ContentModel.TYPE_CONTENT, 
                props).getChildRef();
        assertEquals("NodeRef for recreated node should be the same as the original", b, bRecreated);
        
        // Check existence
        verifyNodeExistence(a, true);
        verifyNodeExistence(b, true);
        verifyNodeExistence(a_, true);
        verifyNodeExistence(b_, true);
        
        // Now check that the parent a can be deleted and the conflict is handled
        nodeService.deleteNode(a);
        
        // Check existence
        verifyNodeExistence(a, false);
        verifyNodeExistence(b, false);
        verifyNodeExistence(a_, true);
        verifyNodeExistence(b_, true);
    }
    
    /**
     * <a href="https://issues.alfresco.com/jira/browse/ALF-7889">ALF-7889</a>
     */
    public synchronized void testAR7889ArchiveAndRestoreMustNotModifyAuditable() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(USER_A);
        nodeService.addAspect(b, ContentModel.ASPECT_AUDITABLE, null);
        
        // Do a little wait to ensure that the cm:auditable modified date is at least 1s old
        wait(2000L);
        
        // Get the cm:auditable modified time
        String modifierOriginal = (String) nodeService.getProperty(b, ContentModel.PROP_MODIFIER);
        Date modifiedOriginal = (Date) nodeService.getProperty(b, ContentModel.PROP_MODIFIED);
        
        nodeService.deleteNode(b);
        verifyNodeExistence(b_, true);
        
        // Check that the cm:auditable modified did not change
        String modifierArchived = (String) nodeService.getProperty(b_, ContentModel.PROP_MODIFIER);
        Date modifiedArchived = (Date) nodeService.getProperty(b_, ContentModel.PROP_MODIFIED);
        assertEquals("cm:modifier should not have changed", modifierOriginal, modifierArchived);
        assertEquals("cm:modified should not have changed", modifiedOriginal, modifiedArchived);

        // Restore is done using clean txn
        commitAndBeginNewTransaction();
        
        // Restore and check cm:auditable
        RestoreNodeReport report = nodeArchiveService.restoreArchivedNode(b_);
        assertEquals("Restore failed", RestoreStatus.SUCCESS, report.getStatus());
        
        // Check that the cm:auditable modified did not change
        String modifierRestored = (String) nodeService.getProperty(b, ContentModel.PROP_MODIFIER);
        Date modifiedRestored = (Date) nodeService.getProperty(b, ContentModel.PROP_MODIFIED);
        assertEquals("cm:modifier should not have changed", modifierOriginal, modifierRestored);
        assertEquals("cm:modified should not have changed", modifiedOriginal, modifiedRestored);
    }
}
