/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.lock;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.TestWithUserUtils;

/**
 * LockBehaviourImpl Unit Test.
 * 
 * @author Roy Wetherall
 */
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
     * The authentication service
     */
    private AuthenticationService authenticationService;    
    
    private PermissionService permissionService;
    
    /**
     * Node references used in the tests
     */
    private NodeRef nodeRef;
    private NodeRef noAspectNode;
    
    /**
     * Store reference
     */
    private StoreRef storeRef;
    
    /**
     * User details
     */
    private static final String PWD = "password";
    private static final String GOOD_USER_NAME = "goodUser";
    private static final String BAD_USER_NAME = "badUser";
    
    NodeRef rootNodeRef;
   
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        this.nodeService = (NodeService)applicationContext.getBean("dbNodeService");
        this.lockService = (LockService)applicationContext.getBean("lockService");
		this.versionService = (VersionService)applicationContext.getBean("versionService");
        this.authenticationService = (AuthenticationService)applicationContext.getBean("authenticationService");
        this.permissionService = (PermissionService)applicationContext.getBean("permissionService");
        
        // Set the authentication
        AuthenticationComponent authComponent = (AuthenticationComponent)this.applicationContext.getBean("authenticationComponent");
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
        
        permissionService.setPermission(rootNodeRef, GOOD_USER_NAME, PermissionService.ALL_PERMISSIONS, true);
        permissionService.setPermission(rootNodeRef, BAD_USER_NAME, PermissionService.READ, true);
    }   
    
    /**
     * Test checkForLock (no user specified)
     */
    public void testCheckForLockNoUser()
    {
        TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);     	
		
        this.lockService.checkForLock(this.nodeRef);
        this.lockService.checkForLock(this.noAspectNode);
        
        // Give the node a write lock (as the good user)
        this.lockService.lock(this.nodeRef, LockType.WRITE_LOCK);    
        this.lockService.checkForLock(this.nodeRef);
        
        // Give the node a read only lock (as the good user)
        this.lockService.unlock(this.nodeRef);
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
        
        // Give the node a write lock (as the bad user)
        this.lockService.unlock(this.nodeRef);
        
        TestWithUserUtils.authenticateUser(BAD_USER_NAME, PWD, rootNodeRef, this.authenticationService); 
        this.lockService.lock(this.nodeRef, LockType.WRITE_LOCK);        
        try
        {
            TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService); 
            this.lockService.checkForLock(this.nodeRef);
            fail("The node locked exception should have been raised");
        }
        catch (NodeLockedException exception)
        {
            // Correct behaviour
        }
        
        // Give the node a read only lock (as the bad user)
        TestWithUserUtils.authenticateUser(BAD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
        this.lockService.unlock(this.nodeRef);
        this.lockService.lock(this.nodeRef, LockType.READ_ONLY_LOCK);        
        try
        {
            TestWithUserUtils.authenticateUser(GOOD_USER_NAME, PWD, rootNodeRef, this.authenticationService);
            this.lockService.checkForLock(this.nodeRef);
            fail("The node locked exception should have been raised");
        }
        catch (NodeLockedException exception)
        {
            // Correct behaviour
        }
    }

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
        
        try {Thread.sleep(2*1000); } catch (Exception e) {};
        
        // Should now have expired so the node should no longer appear to be locked
        this.lockService.checkForLock(this.nodeRef);
    }
	
    /**
     * Test version service lock checking
     */
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
            fail("Should have failed since this node has been locked with a read only lock.");
        }
        catch (NodeLockedException exception)
        {
        }
        this.lockService.unlock(this.nodeRef);
    }
    
    /**
     * Test version service lock checking
     */
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
        catch (NodeLockedException exception)
        {
        }
    }
    
	/**
	 * Test that the node service lock behaviour is as we expect
	 *
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
		catch(NodeLockedException exception)
		{
		}
		
		// TODO various other tests along these lines ...
		
		// TODO check that delete is also working
	}
    
}
