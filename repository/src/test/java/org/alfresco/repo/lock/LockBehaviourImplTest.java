/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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
package org.alfresco.repo.lock;

import static org.apache.commons.lang3.RandomStringUtils.secure;
import static org.awaitility.Awaitility.await;

import java.io.Serializable;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.transaction.annotation.Transactional;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.TestWithUserUtils;

/**
 * LockBehaviourImpl Unit Test.
 *
 * @author Roy Wetherall
 */
@Category(OwnJVMTestsCategory.class)
@Transactional
public class LockBehaviourImplTest extends BaseSpringTest
{
    /**
     * The lock service
     */
    private LockService lockService;

    /**
     * The version service
     */
    private VersionService versionService;

    /**
     * The node service
     */
    private NodeService nodeService;

    /**
     * The Copy service
     */
    private CopyService copyService;

    /**
     * The NodeArchiveService service
     */
    private NodeArchiveService nodeArchiveService;

    /**
     * The authentication service
     */
    private MutableAuthenticationService authenticationService;

    private PermissionService permissionService;

    /**
     * Node references used in the tests
     */
    private NodeRef nodeRef;
    private NodeRef noAspectNode;
    private NodeRef inSpaceStoreNode;

    /**
     * Store reference
     */
    private StoreRef storeRef;

    /**
     * User details
     */
    private static final String PWD = secure().nextAlphabetic(10);
    private static final String GOOD_USER_NAME = "goodUser";
    private static final String BAD_USER_NAME = "badUser";
    private static final String BAD_USER_WITH_ALL_PERMS_NAME = "badUserOwns";

    NodeRef rootNodeRef;

    @Before
    public void before() throws Exception
    {
        this.nodeService = (NodeService) applicationContext.getBean("dbNodeService");
        this.lockService = (LockService) applicationContext.getBean("lockService");
        this.versionService = (VersionService) applicationContext.getBean("versionService");
        this.authenticationService = (MutableAuthenticationService) applicationContext.getBean("authenticationService");
        this.permissionService = (PermissionService) applicationContext.getBean("permissionService");
        this.copyService = (CopyService) applicationContext.getBean("copyService");
        this.nodeArchiveService = (NodeArchiveService) applicationContext.getBean("nodeArchiveService");

        // Set the authentication
        AuthenticationComponent authComponent = (AuthenticationComponent) this.applicationContext.getBean("authenticationComponent");
        authComponent.setSystemUserAsCurrentUser();

        // Create the node properties
        HashMap<QName, Serializable> nodeProperties = new HashMap<QName, Serializable>();
        nodeProperties.put(QName.createQName("{test}property1"), "value1");

        // Create a workspace that contains the 'live' nodes
        this.storeRef = this.nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());

        // Get a reference to the root node
        rootNodeRef = this.nodeService.getRootNode(this.storeRef);

        // Create node
        this.nodeRef = this.nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{}ParentNode"),
                ContentModel.TYPE_FOLDER,
                nodeProperties).getChildRef();
        this.nodeService.addAspect(this.nodeRef, ContentModel.ASPECT_LOCKABLE, new HashMap<QName, Serializable>());
        assertNotNull(this.nodeRef);

        this.inSpaceStoreNode = this.nodeService.createNode(
                nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE),
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{}ParentNode"),
                ContentModel.TYPE_FOLDER,
                nodeProperties).getChildRef();
        this.nodeService.addAspect(this.inSpaceStoreNode, ContentModel.ASPECT_LOCKABLE, new HashMap<QName, Serializable>());
        assertNotNull(this.inSpaceStoreNode);

        // Create a node with no lockAspect
        this.noAspectNode = this.nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{}noAspectNode"),
                ContentModel.TYPE_CONTAINER,
                nodeProperties).getChildRef();
        assertNotNull(this.noAspectNode);

        // Create the users
        TestWithUserUtils.createUser(GOOD_USER_NAME, PWD, rootNodeRef, this.nodeService, this.authenticationService);
        TestWithUserUtils.createUser(BAD_USER_NAME, PWD, rootNodeRef, this.nodeService, this.authenticationService);
        TestWithUserUtils.createUser(BAD_USER_WITH_ALL_PERMS_NAME, PWD, rootNodeRef, this.nodeService, this.authenticationService);

        // Stash the user node ref's for later use
        TestWithUserUtils.authenticateUser(BAD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        TestWithUserUtils.authenticateUser(BAD_USER_WITH_ALL_PERMS_NAME, PWD, rootNodeRef, this.authenticationService);

        permissionService.setPermission(rootNodeRef, GOOD_USER_NAME, PermissionService.ALL_PERMISSIONS, true);
        permissionService.setPermission(rootNodeRef, BAD_USER_NAME, PermissionService.READ, true);
        permissionService.setPermission(rootNodeRef, BAD_USER_WITH_ALL_PERMS_NAME, PermissionService.ALL_PERMISSIONS, true);

        permissionService.setPermission(inSpaceStoreNode, GOOD_USER_NAME, PermissionService.ALL_PERMISSIONS, true);
        permissionService.setPermission(inSpaceStoreNode, BAD_USER_WITH_ALL_PERMS_NAME, PermissionService.ALL_PERMISSIONS, true);
    }

    /**
     * Test checkForLock (no user specified)
     */
    @Test
    public void testCheckForLockNoUser()
    {
        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);

        this.lockService.checkForLock(this.nodeRef);
        assertFalse(lockService.isLocked(nodeRef));
        this.lockService.checkForLock(this.noAspectNode);
        assertFalse(lockService.isLocked(noAspectNode));

        // Give the node a write lock (as the good user)
        this.lockService.lock(this.nodeRef, LockType.WRITE_LOCK);

        this.lockService.checkForLock(this.nodeRef);
        assertTrue(lockService.isLocked(nodeRef));
        assertFalse(lockService.isLockedAndReadOnly(nodeRef));

        // Unlock
        this.lockService.unlock(this.nodeRef);

        assertFalse(lockService.isLocked(nodeRef));
        assertFalse(lockService.isLockedAndReadOnly(nodeRef));

        // Give the node a read only lock (as the good user)
        this.lockService.lock(this.nodeRef, LockType.READ_ONLY_LOCK);
        try
        {
            this.lockService.checkForLock(this.nodeRef);
            fail("The node locked exception should have been raised");
        }
        catch (NodeLockedException exception)
        {
            // Correct behaviour
        }
        assertTrue(lockService.isLocked(nodeRef));
        assertTrue(lockService.isLockedAndReadOnly(nodeRef));

        // Unlock
        this.lockService.unlock(this.nodeRef);
        assertFalse(lockService.isLocked(nodeRef));
        assertFalse(lockService.isLockedAndReadOnly(nodeRef));

        // Give the node a write lock (as the bad user)
        TestWithUserUtils.authenticateUser(BAD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        this.lockService.lock(this.nodeRef, LockType.WRITE_LOCK);

        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        try
        {
            this.lockService.checkForLock(this.nodeRef);
            fail("The node locked exception should have been raised");
        }
        catch (NodeLockedException exception)
        {
            // Correct behaviour
        }

        assertTrue(lockService.isLocked(nodeRef));
        assertTrue(lockService.isLockedAndReadOnly(nodeRef));

        // Give the node a read only lock (as the bad user)
        TestWithUserUtils.authenticateUser(BAD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        this.lockService.unlock(this.nodeRef);
        assertFalse(lockService.isLocked(nodeRef));
        assertFalse(lockService.isLockedAndReadOnly(nodeRef));

        this.lockService.lock(this.nodeRef, LockType.READ_ONLY_LOCK);

        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        try
        {
            this.lockService.checkForLock(this.nodeRef);
            fail("The node locked exception should have been raised");
        }
        catch (NodeLockedException exception)
        {
            // Correct behaviour
        }

        assertTrue(lockService.isLocked(nodeRef));
        assertTrue(lockService.isLockedAndReadOnly(nodeRef));
    }

    @Test
    public void testCheckForLockWhenExpired()
    {
        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);

        this.lockService.lock(this.nodeRef, LockType.READ_ONLY_LOCK, 1);
        try
        {
            this.lockService.checkForLock(this.nodeRef);
            fail("Should be locked.");
        }
        catch (NodeLockedException e)
        {
            // Expected
        }

        assertTrue(lockService.isLocked(nodeRef));
        assertTrue(lockService.isLockedAndReadOnly(nodeRef));

        await().pollInSameThread()
                .pollDelay(Duration.ofMillis(500))
                .atMost(MAX_ASYNC_TIMEOUT)
                .until(() -> !lockService.isLocked(nodeRef));

        // Should now have expired so the node should no longer appear to be locked
        this.lockService.checkForLock(this.nodeRef);

        assertFalse(lockService.isLocked(nodeRef));
        assertFalse(lockService.isLockedAndReadOnly(nodeRef));
    }

    /**
     * Test version service lock checking
     */
    @Test
    public void testVersionServiceLockBehaviour01()
    {
        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);

        // Add the version aspect to the node
        this.nodeService.addAspect(this.nodeRef, ContentModel.ASPECT_VERSIONABLE, null);

        try
        {
            this.versionService.createVersion(this.nodeRef, new HashMap<String, Serializable>());
        }
        catch (NodeLockedException exception)
        {
            fail("There is no lock so this should have worked.");
        }

        // Lock the node as the good user with a write lock
        this.lockService.lock(this.nodeRef, LockType.WRITE_LOCK);
        try
        {
            this.versionService.createVersion(this.nodeRef, new HashMap<String, Serializable>());
        }
        catch (NodeLockedException exception)
        {
            fail("Tried to version as the lock owner so should work.");
        }
        this.lockService.unlock(this.nodeRef);

        // Lock the node as the good user with a read only lock
        this.lockService.lock(this.nodeRef, LockType.READ_ONLY_LOCK);
        try
        {
            this.versionService.createVersion(this.nodeRef, new HashMap<String, Serializable>());
        }
        catch (NodeLockedException exception)
        {
            fail("Should have passed, as we should be able to create a version. See ALF-16540");
        }
        this.lockService.unlock(this.nodeRef);
    }

    /**
     * Test version service lock checking
     */
    @Test
    public void testVersionServiceLockBehaviour02()
    {
        // Add the version aspect to the node
        this.nodeService.addAspect(this.nodeRef, ContentModel.ASPECT_VERSIONABLE, null);

        // Lock the node as the bad user with a write lock
        this.lockService.lock(this.nodeRef, LockType.WRITE_LOCK);
        try
        {
            TestWithUserUtils.authenticateUser(BAD_USER_NAME, PWD, rootNodeRef, this.authenticationService);

            this.versionService.createVersion(this.nodeRef, new HashMap<String, Serializable>());
            fail("Should have failed since this node has been locked by another user with a write lock.");
        }
        catch (AccessDeniedException exception)
        {
            // Exception occurs when the properties are updated for a node
        }
    }

    /**
     * Test that the node service lock behaviour is as we expect
     */
    @SuppressWarnings("unused")
    public void testNodeServiceLockBehaviour()
    {
        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);

        // Check that we can create a new node and set of it properties when no lock is present
        ChildAssociationRef childAssocRef = this.nodeService.createNode(
                this.nodeRef,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName("{test}nodeServiceLockTest"),
                ContentModel.TYPE_CONTAINER);
        NodeRef nodeRef = childAssocRef.getChildRef();

        // Lets lock the parent node and check that whether we can still create a new node
        this.lockService.lock(this.nodeRef, LockType.WRITE_LOCK);
        ChildAssociationRef childAssocRef2 = this.nodeService.createNode(
                this.nodeRef,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName("{test}nodeServiceLockTest"),
                ContentModel.TYPE_CONTAINER);
        NodeRef nodeRef2 = childAssocRef.getChildRef();

        // Lets check that we can do other stuff with the node since we have it locked
        this.nodeService.setProperty(this.nodeRef, QName.createQName("{test}prop1"), "value1");
        Map<QName, Serializable> propMap = new HashMap<QName, Serializable>();
        propMap.put(QName.createQName("{test}prop2"), "value2");
        this.nodeService.setProperties(this.nodeRef, propMap);
        this.nodeService.removeAspect(this.nodeRef, ContentModel.ASPECT_VERSIONABLE);
        // TODO there are various other calls that could be more vigirously checked

        // Lock the node as the 'bad' user
        this.lockService.unlock(this.nodeRef);

        TestWithUserUtils.authenticateUser(BAD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        this.lockService.lock(this.nodeRef, LockType.WRITE_LOCK);

        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);

        // Lets check that we can't create a new child
        try
        {
            this.nodeService.createNode(
                    this.nodeRef,
                    ContentModel.ASSOC_CONTAINS,
                    QName.createQName("{test}nodeServiceLockTest"),
                    ContentModel.TYPE_CONTAINER);
            fail("The parent is locked so a new child should not have been created.");
        }
        catch (NodeLockedException exception)
        {}

        // TODO various other tests along these lines ...

        // TODO check that delete is also working
    }

    /**
     * ALF-5680: It is possible to cut/paste a locked file
     */
    @Test
    public void testCannotMoveNodeWhenLocked()
    {
        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);

        // Create the node that we'll try to move
        NodeRef parentNode = this.nodeRef;
        ChildAssociationRef childAssocRef = nodeService.createNode(
                parentNode,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName("{test}nodeServiceLockTest"),
                ContentModel.TYPE_CONTENT);

        NodeRef nodeRef = childAssocRef.getChildRef();
        // Lock it - so that it can't be moved.
        this.lockService.lock(nodeRef, LockType.WRITE_LOCK);

        // Create the new container that we'll move the node to.
        NodeRef newParentRef = nodeService.createNode(
                parentNode,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName("{test}nodeServiceLockTest"),
                ContentModel.TYPE_CONTAINER).getChildRef();

        // Now the bad user will try to move the node.
        TestWithUserUtils.authenticateUser(BAD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        try
        {
            nodeService.moveNode(
                    nodeRef,
                    newParentRef,
                    ContentModel.ASSOC_CONTAINS,
                    QName.createQName("{test}nodeServiceLockTest"));
            fail("Shouldn't have been able to move locked node.");
        }
        catch (NodeLockedException e)
        {
            // Good, we can't move it - as expected.
        }
    }

    /**
     * MNT-9475: Moving locked content breaks edit online
     */
    @Test
    public void testCanMoveCopyDeleteWithLockOwner()
    {
        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);

        // Create the node that we'll try to move, copy & delete
        NodeRef parentNode = this.nodeRef;
        ChildAssociationRef childAssocRef = nodeService.createNode(
                parentNode,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName("{test}nodeServiceLockTest"),
                ContentModel.TYPE_CONTENT);

        NodeRef nodeRef = childAssocRef.getChildRef();
        // Lock it
        this.lockService.lock(nodeRef, LockType.WRITE_LOCK);

        // Create the node that we'll try to archive and restore
        NodeRef archivingBehaviorNodeRef = nodeService.createNode(
                this.inSpaceStoreNode,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName("{test}nodeServiceLockTest"),
                ContentModel.TYPE_CONTENT).getChildRef();
        // Lock it
        this.lockService.lock(archivingBehaviorNodeRef, LockType.WRITE_LOCK);

        // Create the new container that we'll move the node to.
        NodeRef newParentRefToMove = nodeService.createNode(
                parentNode,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName("{test}nodeServiceLockTest"),
                ContentModel.TYPE_CONTAINER).getChildRef();

        // Create the new container that we'll copy the node to.
        NodeRef newParentRefToCopy = nodeService.createNode(
                parentNode,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName("{test}nodeServiceLockTest"),
                ContentModel.TYPE_CONTAINER).getChildRef();

        try
        {
            // user should be able to move node
            nodeService.moveNode(nodeRef, newParentRefToMove, ContentModel.ASSOC_CONTAINS, QName.createQName("{test}nodeServiceLockTest"));

            // copy it
            copyService.copy(nodeRef, newParentRefToCopy, ContentModel.ASSOC_CONTAINS, QName.createQName("{test}nodeServiceLockTest"));

            // and delete node
            nodeService.deleteNode(nodeRef);
        }
        catch (NodeLockedException e)
        {
            fail("Should be moved, copied an deleted.");
        }

        childAssocRef = nodeService.createNode(
                parentNode,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName("{test}nodeServiceLockTest"),
                ContentModel.TYPE_CONTENT);

        nodeRef = childAssocRef.getChildRef();

        // Create the new container that we'll copy the node to.
        newParentRefToCopy = nodeService.createNode(
                parentNode,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName("{test}nodeServiceLockTest"),
                ContentModel.TYPE_CONTAINER).getChildRef();

        this.lockService.lock(nodeRef, LockType.WRITE_LOCK);

        TestWithUserUtils.authenticateUser(BAD_USER_WITH_ALL_PERMS_NAME, PWD, rootNodeRef, this.authenticationService);

        try
        {
            // Node Can be Copied by Not LockOwner
            copyService.copy(nodeRef, newParentRefToCopy, ContentModel.ASSOC_CONTAINS, QName.createQName("{test}nodeServiceLockTest"));
        }
        catch (NodeLockedException e)
        {
            fail("Should be copied.");
        }

        try
        {
            nodeService.deleteNode(newParentRefToCopy);
        }
        catch (NodeLockedException e)
        {
            fail("Should not have any locks.");
        }
        try
        {
            nodeService.deleteNode(nodeRef);
            fail("Should not be deleted.");
        }
        catch (NodeLockedException e)
        {
            // Only LockOwner can Delete Node
        }

        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);

        nodeService.deleteNode(archivingBehaviorNodeRef);
        NodeRef archivedNode = nodeArchiveService.getArchivedNode(archivingBehaviorNodeRef);

        // check for lock for archived node
        checkForLockForBadAndGoodUsers(archivedNode);

        TestWithUserUtils.authenticateUser(BAD_USER_WITH_ALL_PERMS_NAME, PWD, rootNodeRef, this.authenticationService);
        try
        {
            // Try to restore archived node by Not Lock Owner
            archivingBehaviorNodeRef = nodeService.restoreNode(archivedNode,
                    this.inSpaceStoreNode, ContentModel.ASSOC_CONTAINS, QName.createQName("{test}nodeServiceLockTest"));
        }
        catch (Exception e)
        {
            fail("Should not be any Exceptons.");
        }

        // check for lock for restored node by bad user
        checkForLockForBadAndGoodUsers(archivingBehaviorNodeRef);

        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);

        nodeService.deleteNode(archivingBehaviorNodeRef);
        try
        {
            archivingBehaviorNodeRef = nodeService.restoreNode(archivedNode,
                    this.inSpaceStoreNode, ContentModel.ASSOC_CONTAINS, QName.createQName("{test}nodeServiceLockTest"));
        }
        catch (Exception e)
        {
            fail("Should not be any Exceptons.");
        }

        // check for lock for restored node by good user
        checkForLockForBadAndGoodUsers(archivingBehaviorNodeRef);
    }

    private void checkForLockForBadAndGoodUsers(NodeRef nodeToCheck)
    {
        String currentUserName = TestWithUserUtils.getCurrentUser(this.authenticationService);

        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        try
        {
            lockService.checkForLock(nodeToCheck);
        }
        catch (NodeLockedException e)
        {
            fail("Should not be locked for GoodUser : " + nodeToCheck);
        }
        assertTrue(lockService.isLocked(nodeToCheck));
        assertFalse(lockService.isLockedAndReadOnly(nodeToCheck));

        TestWithUserUtils.authenticateUser(BAD_USER_WITH_ALL_PERMS_NAME, PWD, rootNodeRef, this.authenticationService);
        try
        {
            lockService.checkForLock(nodeToCheck);
            fail("Should be locked for BadUser : " + nodeToCheck);
        }
        catch (NodeLockedException e)
        {
            // It's Ok
        }
        assertTrue(lockService.isLocked(nodeToCheck));
        assertTrue(lockService.isLockedAndReadOnly(nodeToCheck));

        TestWithUserUtils.authenticateUser(currentUserName, PWD, rootNodeRef, this.authenticationService);
    }
}
