/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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

import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.lock.mem.Lifetime;
import org.alfresco.repo.lock.mem.LockState;
import org.alfresco.repo.lock.mem.LockStore;
import org.alfresco.repo.policy.BehaviourDefinition;
import org.alfresco.repo.policy.ClassBehaviourBinding;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.search.IndexerAndSearcher;
import org.alfresco.repo.search.SearcherComponent;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.lock.UnableToAquireLockException;
import org.alfresco.service.cmr.lock.UnableToReleaseLockException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.test_category.BaseSpringTestsCategory;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.TestWithUserUtils;
import org.alfresco.util.testing.category.RedundantTests;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

/**
 * Simple lock service test
 * 
 * @author Roy Wetherall
 */
@Category({BaseSpringTestsCategory.class})
@Transactional
public class LockServiceImplTest extends BaseSpringTest
{
    /**
     * Services used in tests
     */
    private NodeService nodeService;
    private LockService lockService;
    private MutableAuthenticationService authenticationService;
    private CheckOutCheckInService cociService;
    
    private PermissionService permissionService;
    private LockService securedLockService;
    /**
     * Data used in tests
     */
    private NodeRef parentNode;
    private NodeRef childNode1;
    private NodeRef childNode2;    
    private NodeRef noAspectNode;
    private NodeRef checkedOutNode;
    
    private static final String GOOD_USER_NAME = "goodUser";
    private static final String BAD_USER_NAME = "badUser";
    private static final String PWD = "password";
    
    NodeRef rootNodeRef;
    private StoreRef storeRef;

    private PolicyComponent policyComponent;


    private class LockServicePoliciesImpl implements LockServicePolicies.BeforeLock,
            LockServicePolicies.BeforeUnlock
    {
        @Override
        public void beforeLock(NodeRef nodeRef, LockType lockType)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Invoked beforeLock() for nodeRef: " + nodeRef +
                        lockType != null ? (" and lockType: " + lockType) : "");
            }
        }

        @Override
        public void beforeUnlock(NodeRef nodeRef)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Invoked beforeUnlock() for nodeRef: " + nodeRef);
            }
        }
    }


    @Before
    public void before() throws Exception
    {
        this.nodeService = (NodeService)applicationContext.getBean("dbNodeService");
        this.lockService = (LockService)applicationContext.getBean("lockService");
        
        this.securedLockService = (LockService)applicationContext.getBean("LockService");
        this.permissionService = (PermissionService)applicationContext.getBean("PermissionService");
        
        this.authenticationService = (MutableAuthenticationService)applicationContext.getBean("authenticationService");
        this.cociService = (CheckOutCheckInService) applicationContext.getBean("checkOutCheckInService");

        this.policyComponent = (PolicyComponent)applicationContext.getBean("policyComponent");
        
        // Set the authentication
        AuthenticationComponent authComponent = (AuthenticationComponent)this.applicationContext.getBean("authenticationComponent");
        authComponent.setSystemUserAsCurrentUser();        
        
        // Create the node properties
        HashMap<QName, Serializable> nodeProperties = new HashMap<QName, Serializable>();
        nodeProperties.put(QName.createQName("{test}property1"), "value1");
        
        // Create a workspace that contains the 'live' nodes
        storeRef = this.nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        
        // Get a reference to the root node
        rootNodeRef = this.nodeService.getRootNode(storeRef);
        
        // Create node 
        this.parentNode = this.nodeService.createNode(
                rootNodeRef, 
				ContentModel.ASSOC_CHILDREN, 
                QName.createQName("{}ParentNode"),
                ContentModel.TYPE_CONTAINER,
                nodeProperties).getChildRef();
        this.nodeService.addAspect(this.parentNode, ContentModel.ASPECT_LOCKABLE, new HashMap<QName, Serializable>());
        HashMap<QName, Serializable> audProps = new HashMap<QName, Serializable>();
        audProps.put(ContentModel.PROP_CREATOR, "Monkey");
        this.nodeService.addAspect(this.parentNode, ContentModel.ASPECT_AUDITABLE, audProps);
        assertNotNull(this.parentNode);
        
        // Add some children to the node
        this.childNode1 = this.nodeService.createNode(
                this.parentNode,
				ContentModel.ASSOC_CHILDREN,
                QName.createQName("{}ChildNode1"),
                ContentModel.TYPE_CONTAINER,
                nodeProperties).getChildRef();
        this.nodeService.addAspect(this.childNode1, ContentModel.ASPECT_LOCKABLE, new HashMap<QName, Serializable>());
        assertNotNull(this.childNode1);
        this.childNode2 = this.nodeService.createNode(
                this.parentNode,
				ContentModel.ASSOC_CHILDREN,
                QName.createQName("{}ChildNode2"),
                ContentModel.TYPE_CONTAINER,
                nodeProperties).getChildRef();
        this.nodeService.addAspect(this.childNode2, ContentModel.ASPECT_LOCKABLE, new HashMap<QName, Serializable>());
        assertNotNull(this.childNode2);
        
        // Create a node with no lockAspect
        this.noAspectNode = this.nodeService.createNode(
                rootNodeRef, 
				ContentModel.ASSOC_CHILDREN, 
                QName.createQName("{}noAspectNode"),
                ContentModel.TYPE_CONTAINER,
                nodeProperties).getChildRef();
        assertNotNull(this.noAspectNode);
        
        // Create node with checkedOut
        this.checkedOutNode = this.nodeService.createNode(
                rootNodeRef, 
                ContentModel.ASSOC_CHILDREN, 
                QName.createQName("{}checkedOutNode"),
                ContentModel.TYPE_CONTAINER,
                nodeProperties).getChildRef();
        assertNotNull(this.checkedOutNode);
        
        // Check out test file
        NodeRef fileWorkingCopyNodeRef = cociService.checkout(checkedOutNode);
        assertNotNull(fileWorkingCopyNodeRef);
        assertTrue(nodeService.hasAspect(checkedOutNode, ContentModel.ASPECT_CHECKED_OUT));
        assertTrue(nodeService.hasAspect(checkedOutNode, ContentModel.ASPECT_LOCKABLE));
        
        
        // Create the  users
        TestWithUserUtils.createUser(GOOD_USER_NAME, PWD, rootNodeRef, this.nodeService, this.authenticationService);
        TestWithUserUtils.createUser(BAD_USER_NAME, PWD, rootNodeRef, this.nodeService, this.authenticationService);
        
        this.permissionService.setPermission(rootNodeRef, GOOD_USER_NAME, PermissionService.ALL_PERMISSIONS, true);
        this.permissionService.setPermission(rootNodeRef, BAD_USER_NAME, PermissionService.CHECK_OUT, true);
        this.permissionService.setPermission(rootNodeRef, BAD_USER_NAME, PermissionService.WRITE, true);
        this.permissionService.setPermission(rootNodeRef, BAD_USER_NAME, PermissionService.READ, true);
        
        // Stash the user node ref's for later use
        TestWithUserUtils.authenticateUser(BAD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
    }

    @Test
    public void testLockServicePolicies()
    {
        LockServicePoliciesImpl mockedLockServicePoliciesImpl = mock(LockServicePoliciesImpl.class);

        BehaviourDefinition<ClassBehaviourBinding> lockDef =
                this.policyComponent.bindClassBehaviour(LockServicePolicies.BeforeLock.QNAME, ContentModel.TYPE_BASE,
                        new JavaBehaviour(mockedLockServicePoliciesImpl, "beforeLock"));

        BehaviourDefinition<ClassBehaviourBinding> unlockDef =
                this.policyComponent.bindClassBehaviour(LockServicePolicies.BeforeUnlock.QNAME, ContentModel.TYPE_BASE,
                        new JavaBehaviour(mockedLockServicePoliciesImpl, "beforeUnlock"));

        this.lockService.lock(this.parentNode, LockType.WRITE_LOCK);

        verify(mockedLockServicePoliciesImpl, times(1)).beforeLock(this.parentNode, LockType.WRITE_LOCK);

        this.lockService.unlock(this.parentNode);

        verify(mockedLockServicePoliciesImpl, times(1)).beforeUnlock(this.parentNode);

        // cleanup:
        this.policyComponent.removeClassDefinition(lockDef);
        this.policyComponent.removeClassDefinition(unlockDef);
    }

    /**
     * Test lock
     */
    public void testLock()
    {
        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        
        // Check that the node is not currently locked
        assertEquals(
                LockStatus.NO_LOCK, 
                this.lockService.getLockStatus(this.parentNode));
        assertFalse(lockService.isLocked(parentNode));
        
        // Test valid lock
        this.lockService.lock(this.parentNode, LockType.WRITE_LOCK);
        assertEquals(
                LockStatus.LOCK_OWNER, 
                this.lockService.getLockStatus(this.parentNode));
        assertTrue(lockService.isLocked(parentNode));
        
        // Check that we can retrieve LockState
        LockState lockState = lockService.getLockState(parentNode);
        assertEquals(parentNode, lockState.getNodeRef());
        assertEquals(LockType.WRITE_LOCK, lockState.getLockType());
        assertEquals(GOOD_USER_NAME, lockState.getOwner());
        assertEquals(Lifetime.PERSISTENT, lockState.getLifetime());
        assertEquals(null, lockState.getExpires());
        assertEquals(null, lockState.getAdditionalInfo());
        
        // Check the correct properties have been set
        Map<QName, Serializable> props = nodeService.getProperties(parentNode);
        assertEquals(GOOD_USER_NAME, props.get(ContentModel.PROP_LOCK_OWNER));
        assertEquals(LockType.WRITE_LOCK.toString(), props.get(ContentModel.PROP_LOCK_TYPE));
        assertEquals(Lifetime.PERSISTENT.toString(), props.get(ContentModel.PROP_LOCK_LIFETIME));
        assertEquals(null, props.get(ContentModel.PROP_EXPIRY_DATE));
        
        TestWithUserUtils.authenticateUser(BAD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        
        assertEquals(
                LockStatus.LOCKED,
                this.lockService.getLockStatus(this.parentNode));
        assertTrue(lockService.isLocked(parentNode));
     
        // Test lock when already locked
        try
        {
            this.lockService.lock(this.parentNode, LockType.WRITE_LOCK);
            fail("The user should not be able to lock the node since it is already locked by another user.");
        }
        catch (UnableToAquireLockException exception)
        {
            System.out.println(exception.getMessage());
        }
        
        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        
        // Test already locked by this user
        try
        {
            this.lockService.lock(this.parentNode, LockType.WRITE_LOCK);
        }
        catch (Exception exception)
        {
            fail("No error should be thrown when a node is re-locked by the current lock owner.");
        }
        
        // Test with no apect node
        this.lockService.lock(this.noAspectNode, LockType.WRITE_LOCK);
        assertTrue(lockService.isLocked(noAspectNode));
    }
    
    public void testPersistentLockMayStoreAdditionalInfo()
    {
        lockService.lock(noAspectNode, LockType.NODE_LOCK, 0, Lifetime.PERSISTENT, "additional info");

        LockState lockState = lockService.getLockState(noAspectNode);
        assertEquals("additional info", lockState.getAdditionalInfo());
    }
    
    public void testEphemeralLock()
    {
        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        
        // Check that the node is not currently locked
        assertEquals(LockStatus.NO_LOCK, lockService.getLockStatus(noAspectNode));
        assertFalse(lockService.isLocked(noAspectNode));
        
        // Check that there really is no lockable aspect
        assertEquals(false, nodeService.hasAspect(noAspectNode, ContentModel.ASPECT_LOCKABLE));
        
        // Lock the node 
        lockService.lock(noAspectNode, LockType.WRITE_LOCK, 86400, Lifetime.EPHEMERAL, "some extra data");
        
        // Check additionalInfo has been stored
        assertEquals("some extra data", lockService.getAdditionalInfo(noAspectNode));
        
        // Check that we can retrieve LockState
        LockState lockState = lockService.getLockState(noAspectNode);
        assertEquals(noAspectNode, lockState.getNodeRef());
        assertEquals(LockType.WRITE_LOCK, lockState.getLockType());
        assertEquals(GOOD_USER_NAME, lockState.getOwner());
        assertEquals(Lifetime.EPHEMERAL, lockState.getLifetime());
        assertNotNull(lockState.getExpires());
        assertEquals("some extra data", lockState.getAdditionalInfo());
        
        // The node should be locked
        assertEquals(LockStatus.LOCK_OWNER, lockService.getLockStatus(noAspectNode));
        assertTrue(lockService.isLocked(noAspectNode));
        
        // The node must still not have the lockable aspect applied
        assertEquals(false, nodeService.hasAspect(noAspectNode, ContentModel.ASPECT_LOCKABLE));
        // ...though the full node service should report that it is present
        NodeService fullNodeService = (NodeService) applicationContext.getBean("nodeService");
        assertEquals(true, fullNodeService.hasAspect(noAspectNode, ContentModel.ASPECT_LOCKABLE));
        
        TestWithUserUtils.authenticateUser(BAD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        
        assertEquals(LockStatus.LOCKED, lockService.getLockStatus(noAspectNode));
        assertTrue(lockService.isLocked(noAspectNode));
        
        // Test lock when already locked
        try
        {
            lockService.lock(noAspectNode, LockType.WRITE_LOCK);
            fail("The user should not be able to lock the node since it is already locked by another user.");
        }
        catch (UnableToAquireLockException exception)
        {
            System.out.println(exception.getMessage());
        }
        
        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        
        assertEquals(LockStatus.LOCK_OWNER, lockService.getLockStatus(noAspectNode));
        assertTrue(lockService.isLocked(noAspectNode));
        
        // Test already locked by this user - relock
        try
        {
            lockService.lock(noAspectNode, LockType.WRITE_LOCK, 0, Lifetime.EPHEMERAL);
        }
        catch (Exception exception)
        {
            fail("No error should be thrown when a node is re-locked by the current lock owner.");
        } 
        
        // The node should be locked
        assertEquals(LockStatus.LOCK_OWNER, lockService.getLockStatus(noAspectNode));
        assertTrue(lockService.isLocked(noAspectNode));
        // If we remove the lock info directly from the memory store then the node should no longer
        // be reported as locked (as it is an ephemeral lock)
        LockStore lockStore = (LockStore) applicationContext.getBean("lockStore");
        lockStore.clear();
        // The node must no longer be reported as locked
        assertEquals(LockStatus.NO_LOCK, lockService.getLockStatus(noAspectNode));
        assertFalse(lockService.isLocked(noAspectNode));
        
        // Lock again, ready to test unlocking an ephemeral lock.
        try
        {
            lockService.lock(noAspectNode, LockType.WRITE_LOCK, 0, Lifetime.EPHEMERAL);
        }
        catch (Exception exception)
        {
            fail("No error should be thrown when a node is re-locked by the current lock owner.");
        }
        
        assertEquals(LockStatus.LOCK_OWNER, lockService.getLockStatus(noAspectNode));
        assertTrue(lockService.isLocked(noAspectNode));
        
        lockService.unlock(noAspectNode);
        
        assertEquals(LockStatus.NO_LOCK, lockService.getLockStatus(noAspectNode));
        assertFalse(lockService.isLocked(noAspectNode));
    }

    @Test
    @Category(RedundantTests.class)
    public void testEphemeralLockIndexing()
    {
        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, authenticationService);
        
        IndexerAndSearcher indexerAndSearcher = (IndexerAndSearcher)
                    applicationContext.getBean("indexerAndSearcherFactory");
        SearcherComponent searcher = new SearcherComponent();
        searcher.setIndexerAndSearcherFactory(indexerAndSearcher);

        // Create a lock (owned by the current user)
        lockService.lock(noAspectNode, LockType.WRITE_LOCK, 86400, Lifetime.EPHEMERAL);
        
        // Query for the user's locks
        final String query = String.format("+@cm\\:lockOwner:\"%s\" +@cm\\:lockType:\"WRITE_LOCK\"", GOOD_USER_NAME);
        ResultSet rs = searcher.query(storeRef, "lucene", query);
        assertTrue(rs.getNodeRefs().contains(noAspectNode));
        
        // Unlock the node
        lockService.unlock(noAspectNode);
        
        // Perform a new search, the index should reflect that it is not locked.
        rs = searcher.query(storeRef, "lucene", query);
        assertFalse(rs.getNodeRefs().contains(noAspectNode));
    }
    
    /* MNT-10477 related test */
    @Test
    public void testEphemeralLockModifyNode()
    {
        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        
        // Check that the node is not currently locked
        assertEquals(LockStatus.NO_LOCK, lockService.getLockStatus(noAspectNode));
        assertFalse(lockService.isLocked(noAspectNode));
        
        // Check that there really is no lockable aspect
        assertEquals(false, nodeService.hasAspect(noAspectNode, ContentModel.ASPECT_LOCKABLE));
        
        // Lock the node 
        lockService.lock(noAspectNode, LockType.WRITE_LOCK, 86400, Lifetime.EPHEMERAL, "some extra data");
        
        // get bad user
        TestWithUserUtils.authenticateUser(BAD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        
        assertEquals(LockStatus.LOCKED, lockService.getLockStatus(noAspectNode));
        assertTrue(lockService.isLocked(noAspectNode));
        
        NodeService fullNodeService = (NodeService) applicationContext.getBean("nodeService");
        
        /* addProperties test */
        try
        {
            Map<QName, Serializable> props = new HashMap<QName, Serializable>();
            props.put(ContentModel.PROP_DESCRIPTION, "descr" + System.currentTimeMillis());
            props.put(ContentModel.PROP_TITLE, "title" + System.currentTimeMillis());
            fullNodeService.addProperties(noAspectNode, props);
            
            fail();
        }
        catch(NodeLockedException e)
        {
            // it's ok - node supposed to be locked
        }
        
        /* setProperty test */
        try
        {
            fullNodeService.setProperty(noAspectNode, ContentModel.PROP_DESCRIPTION, "descr" + System.currentTimeMillis());
            
            fail();
        }
        catch(NodeLockedException e)
        {
            // it's ok - node supposed to be locked
        }
        
        /* setProperties test */
        try
        {
            Map<QName, Serializable> props = new HashMap<QName, Serializable>();
            props.put(ContentModel.PROP_DESCRIPTION, "descr" + System.currentTimeMillis());
            props.put(ContentModel.PROP_TITLE, "title" + System.currentTimeMillis());
            fullNodeService.setProperties(noAspectNode, props);
            
            fail();
        }
        catch(NodeLockedException e)
        {
            // it's ok - node supposed to be locked
        }
        
        /* removeProperty test */
        try
        {
            fullNodeService.removeProperty(noAspectNode, ContentModel.PROP_DESCRIPTION);
            
            fail();
        }
        catch(NodeLockedException e)
        {
            // it's ok - node supposed to be locked
        }
        
        /* addAspect test */
        try
        {
            fullNodeService.addAspect(noAspectNode, ContentModel.ASPECT_AUTHOR , null);
            
            fail();
        }
        catch(NodeLockedException e)
        {
            // it's ok - node supposed to be locked
        }
        
        /* removeAspect test */
        try
        {
            fullNodeService.removeAspect(noAspectNode, ContentModel.ASPECT_AUTHOR);
            
            fail();
        }
        catch(NodeLockedException e)
        {
            // it's ok - node supposed to be locked
        }
        
        /* setType test */
        try
        {
            fullNodeService.setType(noAspectNode, ContentModel.TYPE_CMOBJECT);
            
            fail();
        }
        catch(NodeLockedException e)
        {
            // it's ok - node supposed to be locked
        }
        
        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        
        lockService.unlock(noAspectNode);        
        assertEquals(LockStatus.NO_LOCK, lockService.getLockStatus(noAspectNode));
        assertFalse(lockService.isLocked(noAspectNode));
    }
    
    /**
     * Test that covers MNT-17612 - having an expired ephemeral lock and a persistent one on the same node.
     */
    public void testExpiredEphemeralLockAndPersitentLock()
    {
       TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
 
        // Check that the node is not currently locked
        assertEquals(LockStatus.NO_LOCK, securedLockService.getLockStatus(noAspectNode));
        assertFalse(securedLockService.isLocked(noAspectNode));

        // Check that there really is no lockable aspect
        assertEquals(false, nodeService.hasAspect(noAspectNode, ContentModel.ASPECT_LOCKABLE));

        // Lock the node 
        securedLockService.lock(noAspectNode, LockType.WRITE_LOCK, 1, Lifetime.EPHEMERAL);

        // Check that we can retrieve LockState
        LockState lockState = securedLockService.getLockState(noAspectNode);
        assertEquals(noAspectNode, lockState.getNodeRef());
        assertEquals(LockType.WRITE_LOCK, lockState.getLockType());
        assertEquals(GOOD_USER_NAME, lockState.getOwner());
        assertEquals(Lifetime.EPHEMERAL, lockState.getLifetime());
        assertNotNull(lockState.getExpires());

        // Wait for 2 seconds to give the ephemeral lock time to expire
        try {Thread.sleep(2*1000);} catch (Exception exception){};

        assertFalse(securedLockService.isLocked(noAspectNode));

        // Do a persistent lock with a different user (simulate an Edit Offline - MNT-17612)
        TestWithUserUtils.authenticateUser(BAD_USER_NAME, PWD, rootNodeRef, this.authenticationService);

        // Lock the node 
        securedLockService.lock(noAspectNode, LockType.READ_ONLY_LOCK, 1000, Lifetime.PERSISTENT);

        assertTrue(securedLockService.isLocked(noAspectNode));
        assertEquals(true, nodeService.hasAspect(noAspectNode, ContentModel.ASPECT_LOCKABLE));
        assertEquals(LockType.READ_ONLY_LOCK, securedLockService.getLockType(noAspectNode));

        // Check that we can retrieve LockState
        lockState = securedLockService.getLockState(noAspectNode);
        assertEquals(noAspectNode, lockState.getNodeRef());
        assertEquals(LockType.READ_ONLY_LOCK, lockState.getLockType());
        assertEquals(BAD_USER_NAME, lockState.getOwner());
        assertEquals(Lifetime.PERSISTENT, lockState.getLifetime());

        // Check unlock
        securedLockService.unlock(noAspectNode);

        assertFalse(securedLockService.isLocked(noAspectNode));        
    }

    public void testLockRevertedOnRollback() throws NotSupportedException, SystemException
    {
        // Preconditions of test
        assertEquals(LockStatus.NO_LOCK, lockService.getLockStatus(noAspectNode));
        assertFalse(lockService.isLocked(noAspectNode));
        assertEquals(LockStatus.NO_LOCK, lockService.getLockStatus(rootNodeRef));
        assertFalse(lockService.isLocked(rootNodeRef));
        
        // Lock noAspectNode
        lockService.lock(noAspectNode, LockType.WRITE_LOCK, 0, Lifetime.EPHEMERAL);
        
        // Lock rootNodeRef
        lockService.lock(rootNodeRef, LockType.NODE_LOCK, 0, Lifetime.EPHEMERAL);
        
        // Sometime later, a refresh occurs (so this should not be reverted to unlocked, but to this state)
        lockService.lock(rootNodeRef, LockType.NODE_LOCK, 3600, Lifetime.EPHEMERAL);
        
        // Rollback
        TestTransaction.end();
        
        // This lock should not be present.
        assertEquals(LockStatus.NO_LOCK, lockService.getLockStatus(noAspectNode));
        assertFalse(lockService.isLocked(noAspectNode));
        
        // This lock should still be present.
        assertEquals(LockStatus.LOCK_OWNER, lockService.getLockStatus(rootNodeRef));
        assertTrue(lockService.isLocked(rootNodeRef));
    }
    
    /**
     * Test lock with lockChildren == true
     */
    // TODO
    public void testLockChildren()
    {
    }
    
    /**
     * Test lock with collection
     */
    // TODO
    public void testLockMany()
    {
    }
    
    /**
     * Test unlock node
     */
    public void testUnlock()
    {
        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        
        // Lock the parent node
        testLock();
        
        TestWithUserUtils.authenticateUser(BAD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        
        // Try and unlock a locked node
        try
        {
            this.lockService.unlock(this.parentNode);
            // This will pass in the open workd
            //fail("A user cannot unlock a node that is currently lock by another user.");
        }
        catch (UnableToReleaseLockException exception)
        {
            System.out.println(exception.getMessage());
        }
        
        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        
        // Unlock the node
        this.lockService.unlock(this.parentNode);
        assertEquals(
                LockStatus.NO_LOCK,
                this.lockService.getLockStatus(this.parentNode));
        assertFalse(lockService.isLocked(parentNode));
        
        TestWithUserUtils.authenticateUser(BAD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        
        assertEquals(
                LockStatus.NO_LOCK,
                this.lockService.getLockStatus(this.parentNode));
        assertFalse(lockService.isLocked(parentNode));
        
        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        
        // Try and unlock node with no lock
        try
        {
            this.lockService.unlock(this.parentNode);
        }
        catch (Exception exception)
        {
            fail("Unlocking an unlocked node should not result in an exception being raised.");
        }
        
        // Test with no apect node
        this.lockService.unlock(this.noAspectNode);
    }
    
    // TODO
    public void testUnlockChildren()
    {
    }
    
    // TODO
    public void testUnlockMany()
    {
    }
    
    /**
     * Test getLockStatus
     */
    public void testGetLockStatus()
    {
        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        
        // Check an unlocked node
        LockStatus lockStatus1 = this.lockService.getLockStatus(this.parentNode);
        assertEquals(LockStatus.NO_LOCK, lockStatus1);
        
        this.lockService.lock(this.parentNode, LockType.WRITE_LOCK);
        
        TestWithUserUtils.authenticateUser(BAD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        
        // Check for locked status
        LockStatus lockStatus2 = this.lockService.getLockStatus(this.parentNode);
        assertEquals(LockStatus.LOCKED, lockStatus2);
        
        // Check lockstore is not used for persistent locks
        // Previously LockStore was doubling up as a cache - the change in requirements means a test
        // is necessary to ensure the work has been implemented correctly (despite being an odd test)
        LockStore lockStore = (LockStore) applicationContext.getBean("lockStore");
        lockStore.clear();
        LockState lockState = lockStore.get(parentNode);
        // Nothing stored against node ref
        assertNull(lockState);
        lockService.getLockStatus(parentNode);
        // In-memory store still empty - only used for ephemeral locks
        lockState = lockStore.get(parentNode);
        assertNull(lockState);
        
        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        
        // Check for lock owner status
        LockStatus lockStatus3 = this.lockService.getLockStatus(this.parentNode);
        assertEquals(LockStatus.LOCK_OWNER, lockStatus3);
                
        // Test with no apect node
        this.lockService.getLockStatus(this.noAspectNode);
        
        // Test method overload
        LockStatus lockStatus4 = this.lockService.getLockStatus(this.parentNode); 
        assertEquals(LockStatus.LOCK_OWNER, lockStatus4);
    }

    @Category(RedundantTests.class)
    public void testGetLocks()
    {
        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        
        LockServiceImpl lockService = (LockServiceImpl) this.lockService;
        List<NodeRef> locked1 = lockService.getLocks(this.storeRef);
        assertNotNull(locked1);
        assertEquals(0, locked1.size());

        assertFalse(lockService.isLocked(parentNode));
        assertFalse(lockService.isLockedAndReadOnly(parentNode));
        this.lockService.lock(this.parentNode, LockType.WRITE_LOCK);
        assertTrue(lockService.isLocked(parentNode));
        assertFalse(lockService.isLockedAndReadOnly(parentNode));

        assertFalse(lockService.isLocked(childNode1));
        assertFalse(lockService.isLockedAndReadOnly(childNode1));
        this.lockService.lock(this.childNode1, LockType.WRITE_LOCK);
        assertTrue(lockService.isLocked(childNode1));
        assertFalse(lockService.isLockedAndReadOnly(childNode1));

        assertFalse(lockService.isLocked(childNode2));
        assertFalse(lockService.isLockedAndReadOnly(childNode2));
        this.lockService.lock(this.childNode2, LockType.READ_ONLY_LOCK);
        assertTrue(lockService.isLocked(childNode2));
        assertTrue(lockService.isLockedAndReadOnly(childNode2));
        
        List<NodeRef> locked2 = lockService.getLocks(this.storeRef);
        assertNotNull(locked2);
        assertEquals(3, locked2.size());
        
        List<NodeRef> locked3 = lockService.getLocks(this.storeRef, LockType.WRITE_LOCK);
        assertNotNull(locked3);
        assertEquals(2, locked3.size());
        
        List<NodeRef> locked4 = lockService.getLocks(this.storeRef, LockType.READ_ONLY_LOCK);
        assertNotNull(locked4);
        assertEquals(1, locked4.size());
        
        TestWithUserUtils.authenticateUser(BAD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        
        List<NodeRef> locked5 = lockService.getLocks(this.storeRef);
        assertNotNull(locked5);
        assertEquals(0, locked5.size());
    }
    
    /**
     * Test getLockType (and isLocked/isLockedReadOnly)
     */
    public void testGetLockType()
    {
        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        
        // Get the lock type (should be null since the object is not locked)
        LockType lockType1 = this.lockService.getLockType(this.parentNode);
        assertNull(lockType1);
        
        // Lock the object for writing
        this.lockService.lock(this.parentNode, LockType.WRITE_LOCK);
        LockType lockType2 = this.lockService.getLockType(this.parentNode);
        assertNotNull(lockType2);               
        assertEquals(LockType.WRITE_LOCK, lockType2);
        assertTrue(lockService.isLocked(parentNode));
        assertFalse(lockService.isLockedAndReadOnly(parentNode));
        
        // Unlock the node
        this.lockService.unlock(this.parentNode);
        LockType lockType3 = this.lockService.getLockType(this.parentNode);
        assertNull(lockType3);
        assertFalse(lockService.isLocked(parentNode));
        assertFalse(lockService.isLockedAndReadOnly(parentNode));
        
        // Lock the object for read only
        this.lockService.lock(this.parentNode, LockType.READ_ONLY_LOCK);
        LockType lockType4 = this.lockService.getLockType(this.parentNode);
        assertNotNull(lockType4);
        assertEquals(LockType.READ_ONLY_LOCK, lockType4);
        assertTrue(lockService.isLocked(parentNode));
        assertTrue(lockService.isLockedAndReadOnly(parentNode));
        
        // Lock the object for node lock
        this.lockService.lock(this.parentNode, LockType.NODE_LOCK);
        LockType lockType5 = this.lockService.getLockType(this.parentNode);
        assertNotNull(lockType5);               
        assertEquals(LockType.NODE_LOCK, lockType5);
        assertTrue(lockService.isLocked(parentNode));
        assertTrue(lockService.isLockedAndReadOnly(parentNode));
        
        // Unlock the node
        this.lockService.unlock(this.parentNode);
        LockType lockType6 = this.lockService.getLockType(this.parentNode);
        assertNull(lockType6);
        assertFalse(lockService.isLocked(parentNode));
        assertFalse(lockService.isLockedAndReadOnly(parentNode));
       
        // Test with no apect node
        LockType lockType7 = this.lockService.getLockType(this.noAspectNode);
        assertTrue("lock type is not null", lockType7 == null);
    }
    
    public void testGetLockTypeEphemeral()
    {
        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        
        // Get the lock type (should be null since the object is not locked)
        LockType lockType1 = this.lockService.getLockType(this.parentNode);
        assertNull(lockType1);
        
        // Lock the object for writing
        this.lockService.lock(this.parentNode, LockType.WRITE_LOCK, 0, Lifetime.EPHEMERAL);
        LockType lockType2 = this.lockService.getLockType(this.parentNode);
        assertNotNull(lockType2);               
        assertEquals(LockType.WRITE_LOCK, lockType2);
        assertTrue(lockService.isLocked(parentNode));
        assertFalse(lockService.isLockedAndReadOnly(parentNode));
        
        // Unlock the node
        this.lockService.unlock(this.parentNode);
        LockType lockType3 = this.lockService.getLockType(this.parentNode);
        assertNull(lockType3);
        assertFalse(lockService.isLocked(parentNode));
        assertFalse(lockService.isLockedAndReadOnly(parentNode));
        
        // Lock the object for read only
        this.lockService.lock(this.parentNode, LockType.READ_ONLY_LOCK, 0, Lifetime.EPHEMERAL);
        LockType lockType4 = this.lockService.getLockType(this.parentNode);
        assertNotNull(lockType4);
        assertEquals(LockType.READ_ONLY_LOCK, lockType4);
        assertTrue(lockService.isLocked(parentNode));
        assertTrue(lockService.isLockedAndReadOnly(parentNode));
        
        // Lock the object for node lock
        this.lockService.lock(this.parentNode, LockType.NODE_LOCK, 0, Lifetime.EPHEMERAL);
        LockType lockType5 = this.lockService.getLockType(this.parentNode);
        assertNotNull(lockType5);               
        assertEquals(LockType.NODE_LOCK, lockType5);
        assertTrue(lockService.isLocked(parentNode));
        assertTrue(lockService.isLockedAndReadOnly(parentNode));
        
        // Unlock the node
        this.lockService.unlock(this.parentNode);
        LockType lockType6 = this.lockService.getLockType(this.parentNode);
        assertNull(lockType6);
        assertFalse(lockService.isLocked(parentNode));
        assertFalse(lockService.isLockedAndReadOnly(parentNode));
        
        // Test with no apect node
        LockType lockType7 = this.lockService.getLockType(this.noAspectNode);
        assertTrue("lock type is not null", lockType7 == null);
    }
    
    public void testTimeToExpire()
    {
        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        
        this.lockService.lock(this.parentNode, LockType.WRITE_LOCK, 1);
        assertEquals(LockStatus.LOCK_OWNER, this.lockService.getLockStatus(this.parentNode));
        assertTrue(lockService.isLocked(parentNode));
        
        TestWithUserUtils.authenticateUser(BAD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        
        assertEquals(LockStatus.LOCKED, this.lockService.getLockStatus(this.parentNode));
        assertTrue(lockService.isLocked(parentNode));
        
        // Wait for 2 second before re-testing the status
        try {Thread.sleep(2*1000);} catch (Exception exception){};
        
        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        assertEquals(LockStatus.LOCK_EXPIRED, this.lockService.getLockStatus(this.parentNode));
        assertFalse(lockService.isLocked(parentNode));
        
        TestWithUserUtils.authenticateUser(BAD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        assertEquals(LockStatus.LOCK_EXPIRED, this.lockService.getLockStatus(this.parentNode));
        assertFalse(lockService.isLocked(parentNode));
        
        // Re-lock and then update the time to expire before lock expires
        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        this.lockService.lock(this.parentNode, LockType.WRITE_LOCK, 0);
        try
        {
            TestWithUserUtils.authenticateUser(BAD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
            this.lockService.lock(this.parentNode, LockType.WRITE_LOCK, 1);
            fail("Can not update lock info if not lock owner");
        }
        catch (UnableToAquireLockException exception)
        {
            // Expected
        }
        
        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        this.lockService.lock(this.parentNode, LockType.WRITE_LOCK, 1);
        assertEquals(LockStatus.LOCK_OWNER, this.lockService.getLockStatus(this.parentNode));
        assertTrue(lockService.isLocked(parentNode));
        
        TestWithUserUtils.authenticateUser(BAD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        assertEquals(LockStatus.LOCKED, this.lockService.getLockStatus(this.parentNode));
        assertTrue(lockService.isLocked(parentNode));
        
        // Wait for 2 second before re-testing the status
        try {Thread.sleep(2*1000);} catch (Exception exception){};
        
        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        assertEquals(LockStatus.LOCK_EXPIRED, this.lockService.getLockStatus(this.parentNode));
        assertFalse(lockService.isLocked(parentNode));
        
        TestWithUserUtils.authenticateUser(BAD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        assertEquals(LockStatus.LOCK_EXPIRED, this.lockService.getLockStatus(this.parentNode));
        assertFalse(lockService.isLocked(parentNode));
    }
    
    public void testEphemeralExpiryThreshold()
    {
        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        final int origThresh = ((LockServiceImpl)lockService).getEphemeralExpiryThreshold();
        // Check the default situation is that the threshold does not apply.
        assertEquals(LockServiceImpl.MAX_EPHEMERAL_LOCK_SECONDS, origThresh);
        try
        {
            // Set the ephemeral expiry threshold to a much smaller value than the default
            // so that it takes effect.
            lockService.setEphemeralExpiryThreshold(300);
            
            // Check for an expiry time that should be unaffected by the threshold.
            checkLifetimeForExpiry(Lifetime.EPHEMERAL, 0, Lifetime.EPHEMERAL);
            checkLifetimeForExpiry(Lifetime.EPHEMERAL, 150, Lifetime.EPHEMERAL);
            
            // Check the largest allowed ephemeral expiry time.
            checkLifetimeForExpiry(Lifetime.EPHEMERAL, 300, Lifetime.EPHEMERAL);
            
            // When the expiry is greater than the threshold, then the lock should be
            // applied as a persistent lock.
            checkLifetimeForExpiry(Lifetime.PERSISTENT, 301, Lifetime.EPHEMERAL);
            
            // Switch off ephemeral locks entirely
            lockService.setEphemeralExpiryThreshold(-1);
            // Always persistent...
            checkLifetimeForExpiry(Lifetime.PERSISTENT, 0, Lifetime.EPHEMERAL);
            checkLifetimeForExpiry(Lifetime.PERSISTENT, 150, Lifetime.EPHEMERAL);
            checkLifetimeForExpiry(Lifetime.PERSISTENT, 300, Lifetime.EPHEMERAL);
            checkLifetimeForExpiry(Lifetime.PERSISTENT, 301, Lifetime.EPHEMERAL);
        }
        finally
        {
            lockService.setEphemeralExpiryThreshold(origThresh);
        }
    }
    
    private void checkLifetimeForExpiry(Lifetime expectedLifetime, int expirySecs, Lifetime requestedLifetime)
    {
        lockService.unlock(parentNode);
        assertNotEquals(LockStatus.LOCKED ,lockService.getLockStatus(parentNode));
        lockService.lock(parentNode, LockType.WRITE_LOCK, expirySecs, requestedLifetime);
        LockState lock = lockService.getLockState(parentNode);
        assertEquals(expectedLifetime, lock.getLifetime());
        
        // Check that for any timeouts we test, a request for a persistent lock always yields a persistent lock.
        lockService.unlock(parentNode);
        assertNotEquals(LockStatus.LOCKED ,lockService.getLockStatus(parentNode));
        lockService.lock(parentNode, LockType.WRITE_LOCK, expirySecs, Lifetime.PERSISTENT);
        lock = lockService.getLockState(parentNode);
        assertEquals(Lifetime.PERSISTENT, lock.getLifetime());
    }
    
    /**
     * Unit test to validate the behaviour of creating children of locked nodes.
     * No lock - can create children
     * READ_ONLY_LOCK - can't create children
     * WRITE_LOCK - owner can create children
     *      non owner can't create children
     * NODE_LOCK non owner can create children
     *     owner can create children     
     */
    public void testCreateChildrenOfLockedNodes() throws Exception
    {
      
      /**
       * Check we can create a child of an unlocked node.  
       */
      assertEquals(
        LockStatus.NO_LOCK, 
        this.lockService.getLockStatus(this.parentNode));
      assertFalse(lockService.isLocked(parentNode));
      
      ChildAssociationRef child = nodeService.createNode(parentNode, ContentModel.ASSOC_CONTAINS, QName.createQName("ChildA"), ContentModel.TYPE_FOLDER);
        
      TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
      
      this.lockService.lock(this.parentNode, LockType.WRITE_LOCK);
      
      // Owner can create children
      child = nodeService.createNode(parentNode, ContentModel.ASSOC_CONTAINS, QName.createQName("ChildB"), ContentModel.TYPE_FOLDER);
      
      TestWithUserUtils.authenticateUser(BAD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
      
      try
      {
          // Non owner can't create children with a write lock in place
          child = nodeService.createNode(parentNode, ContentModel.ASSOC_CONTAINS, QName.createQName("ChildB"), ContentModel.TYPE_FOLDER);
          fail("could create a child with a read only lock");
      } 
      catch (NodeLockedException e)
      {
          logger.debug("exception while trying to create a child of a read only lock", e);
      }  
      
      TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
      
      this.lockService.lock(this.parentNode, LockType.NODE_LOCK);
      
      // owner can create children with a node lock
      child = nodeService.createNode(parentNode, ContentModel.ASSOC_CONTAINS, QName.createQName("ChildD"), ContentModel.TYPE_FOLDER);
 
      TestWithUserUtils.authenticateUser(BAD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
      
      // Non owner can create children with a node lock
      child = nodeService.createNode(parentNode, ContentModel.ASSOC_CONTAINS, QName.createQName("ChildC"), ContentModel.TYPE_FOLDER);
      
      TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
           
      this.lockService.lock(this.parentNode, LockType.READ_ONLY_LOCK);
      
      // owner should not be able to create children with a READ_ONLY_LOCK
      try
      {
          child = nodeService.createNode(parentNode, ContentModel.ASSOC_CONTAINS, QName.createQName("ChildD"), ContentModel.TYPE_FOLDER);
          fail("could create a child with a read only lock");
      } 
      catch (NodeLockedException e)
      {
          logger.debug("exception while trying to create a child of a read only lock", e);
      }
      
      TestWithUserUtils.authenticateUser(BAD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
      
      // Non owner should not be able to create children with READ_ONLY_LOCK
      try
      {
          child = nodeService.createNode(parentNode, ContentModel.ASSOC_CONTAINS, QName.createQName("ChildE"), ContentModel.TYPE_FOLDER);
          fail("could create a child with a read only lock");
      } 
      catch (NodeLockedException e)
      {
          logger.debug("exception while trying to create a child of a read only lock", e);
      }      
    }
    
    /**
     * Test that it is impossible to unlock a checked out node
     */
    public void testUnlockCheckedOut() throws Exception
    {
        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);

        try
        {
            this.lockService.unlock(checkedOutNode);
            fail("could unlock a checked out node");
        }
        catch (UnableToReleaseLockException e)
        {
            logger.debug("exception while trying to unlock a checked out node", e);
        }

        assertTrue(lockService.isLocked(checkedOutNode));
        assertTrue(lockService.isLockedAndReadOnly(checkedOutNode));
    }
    
    @SuppressWarnings("deprecation")
    public void testUnlockNodeWithAdminUserAndAllPermissionsUser()
    {
        for (Lifetime lt : new Lifetime[]{Lifetime.EPHEMERAL, Lifetime.PERSISTENT})
        {
            TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        
            /* create node */
            final NodeRef testNode = 
                this.nodeService.createNode(parentNode, ContentModel.ASSOC_CONTAINS, QName.createQName("{}testNode"), ContentModel.TYPE_CONTAINER).getChildRef();
        
            // lock it as GOOD user
            this.securedLockService.lock(testNode, LockType.WRITE_LOCK, 2 * 86400, lt, null);
            
            // check lock state and status as GOOD user
            assertNotNull(this.securedLockService.getLockState(testNode));
            assertNotNull(this.securedLockService.getLockStatus(testNode));
            assertTrue(this.securedLockService.isLocked(testNode));
            assertFalse(this.securedLockService.isLockedAndReadOnly(testNode));
        
            TestWithUserUtils.authenticateUser(BAD_USER_NAME, PWD, rootNodeRef, this.authenticationService);

            // check lock state and status as BAD user
            assertNotNull(this.securedLockService.getLockState(testNode));
            assertNotNull(this.securedLockService.getLockStatus(testNode));
            assertTrue(this.securedLockService.isLocked(testNode));
            assertTrue(this.securedLockService.isLockedAndReadOnly(testNode));
        
            try
            {
                // try to unlock as bad user
                this.securedLockService.unlock(testNode);
                fail("BAD user shouldn't be able to unlock " + lt + " lock");
            }
            catch(AccessDeniedException e)
            {
                // expected expetion
            }
        
            TestWithUserUtils.authenticateUser(AuthenticationUtil.getAdminUserName(), "admin", rootNodeRef, this.authenticationService);

            // check lock state and status as ADMIN user
            assertNotNull(this.securedLockService.getLockState(testNode));
            assertNotNull(this.securedLockService.getLockStatus(testNode));
            assertTrue(this.securedLockService.isLocked(testNode));
            assertTrue(this.securedLockService.isLockedAndReadOnly(testNode));
        
            // try to unlock as ADMIN user
            this.securedLockService.unlock(testNode);
            
            // test that bad use able to lock/unlock node
            TestWithUserUtils.authenticateUser(BAD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
            this.securedLockService.lock(testNode, LockType.WRITE_LOCK, 2 * 86400, lt, null);
            this.securedLockService.unlock(testNode);
        
            this.securedLockService.lock(testNode, LockType.WRITE_LOCK, 2 * 86400, lt, null);
            
            // user who has ALL PERMISSIONS is able to unlock another's user lock
            TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
            this.securedLockService.unlock(testNode);
            
            this.nodeService.deleteNode(testNode);
        }
    }
}
