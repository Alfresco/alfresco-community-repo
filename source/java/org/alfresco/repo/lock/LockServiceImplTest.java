/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.lock;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.lock.UnableToAquireLockException;
import org.alfresco.service.cmr.lock.UnableToReleaseLockException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.TestWithUserUtils;

/**
 * Simple lock service test
 * 
 * @author Roy Wetherall
 */
public class LockServiceImplTest extends BaseSpringTest
{
    /**
     * Services used in tests
     */
    private NodeService nodeService;
    private LockService lockService;
    private AuthenticationService authenticationService;
    
    /**
     * Data used in tests
     */
    private NodeRef parentNode;
    private NodeRef childNode1;
    private NodeRef childNode2;    
    private NodeRef noAspectNode;
    
    private static final String GOOD_USER_NAME = "goodUser";
    private static final String BAD_USER_NAME = "badUser";
    private static final String PWD = "password";
    
    NodeRef rootNodeRef;
    private StoreRef storeRef;

    /**
     * Called during the transaction setup
     */
    protected void onSetUpInTransaction() throws Exception
    {
        this.nodeService = (NodeService)applicationContext.getBean("dbNodeService");
        this.lockService = (LockService)applicationContext.getBean("lockService");
        this.authenticationService = (AuthenticationService)applicationContext.getBean("authenticationService");
        authenticationService.clearCurrentSecurityContext();
        
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
        
        // Create the  users
        TestWithUserUtils.createUser(GOOD_USER_NAME, PWD, rootNodeRef, this.nodeService, this.authenticationService);
        TestWithUserUtils.createUser(BAD_USER_NAME, PWD, rootNodeRef, this.nodeService, this.authenticationService);
        
        // Stash the user node ref's for later use
        TestWithUserUtils.authenticateUser(BAD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
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
 
        
        // Test valid lock
        this.lockService.lock(this.parentNode, LockType.WRITE_LOCK);
        assertEquals(
                LockStatus.LOCK_OWNER, 
                this.lockService.getLockStatus(this.parentNode));
        
        TestWithUserUtils.authenticateUser(BAD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        
        assertEquals(
                LockStatus.LOCKED,
                this.lockService.getLockStatus(this.parentNode));
     
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
        
        TestWithUserUtils.authenticateUser(BAD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        
        assertEquals(
                LockStatus.NO_LOCK,
                this.lockService.getLockStatus(this.parentNode));
        
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
    
    public void testGetLocks()
    {
        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        
        List<NodeRef> locked1 = this.lockService.getLocks(this.storeRef);
        assertNotNull(locked1);
        assertEquals(0, locked1.size());
        
        this.lockService.lock(this.parentNode, LockType.WRITE_LOCK);
        this.lockService.lock(this.childNode1, LockType.WRITE_LOCK);
        this.lockService.lock(this.childNode2, LockType.READ_ONLY_LOCK);
        
        List<NodeRef> locked2 = this.lockService.getLocks(this.storeRef);
        assertNotNull(locked2);
        assertEquals(3, locked2.size());
        
        List<NodeRef> locked3 = this.lockService.getLocks(this.storeRef, LockType.WRITE_LOCK);
        assertNotNull(locked3);
        assertEquals(2, locked3.size());
        
        List<NodeRef> locked4 = this.lockService.getLocks(this.storeRef, LockType.READ_ONLY_LOCK);
        assertNotNull(locked4);
        assertEquals(1, locked4.size());
        
        TestWithUserUtils.authenticateUser(BAD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        
        List<NodeRef> locked5 = this.lockService.getLocks(this.storeRef);
        assertNotNull(locked5);
        assertEquals(0, locked5.size());
    }
    
    /**
     * Test getLockType
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
        
        // Unlock the node
        this.lockService.unlock(this.parentNode);
        LockType lockType3 = this.lockService.getLockType(this.parentNode);
        assertNull(lockType3);
        
        // Lock the object for read only
        this.lockService.lock(this.parentNode, LockType.READ_ONLY_LOCK);
        LockType lockType4 = this.lockService.getLockType(this.parentNode);
        assertNotNull(lockType4);
        assertEquals(LockType.READ_ONLY_LOCK, lockType4);
        
        // Test with no apect node
        this.lockService.getLockType(this.noAspectNode);
    }
    
    public void testTimeToExpire()
    {
        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        
        this.lockService.lock(this.parentNode, LockType.WRITE_LOCK, 1);
        assertEquals(LockStatus.LOCK_OWNER, this.lockService.getLockStatus(this.parentNode));
        
        TestWithUserUtils.authenticateUser(BAD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        
        assertEquals(LockStatus.LOCKED, this.lockService.getLockStatus(this.parentNode));
        
        // Wait for 2 second before re-testing the status
        try {Thread.sleep(2*1000);} catch (Exception exception){};
        
        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        assertEquals(LockStatus.LOCK_EXPIRED, this.lockService.getLockStatus(this.parentNode));
        
        TestWithUserUtils.authenticateUser(BAD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        assertEquals(LockStatus.LOCK_EXPIRED, this.lockService.getLockStatus(this.parentNode));
        
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
        
        TestWithUserUtils.authenticateUser(BAD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        assertEquals(LockStatus.LOCKED, this.lockService.getLockStatus(this.parentNode));
        
        // Wait for 2 second before re-testing the status
        try {Thread.sleep(2*1000);} catch (Exception exception){};
        
        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        assertEquals(LockStatus.LOCK_EXPIRED, this.lockService.getLockStatus(this.parentNode));
        
        TestWithUserUtils.authenticateUser(BAD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        assertEquals(LockStatus.LOCK_EXPIRED, this.lockService.getLockStatus(this.parentNode));
    }
}
