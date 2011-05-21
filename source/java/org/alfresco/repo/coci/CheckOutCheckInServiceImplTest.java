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
package org.alfresco.repo.coci;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;
import org.alfresco.util.TestWithUserUtils;

/**
 * Version operations service implementation unit tests
 * 
 * @author Roy Wetherall
 */
public class CheckOutCheckInServiceImplTest extends BaseSpringTest 
{
	/**
	 * Services used by the tests
	 */
	private NodeService nodeService;
	private CheckOutCheckInService cociService;
	private ContentService contentService;
	private VersionService versionService;
    private MutableAuthenticationService authenticationService;
    private LockService lockService;
    private TransactionService transactionService;
    private PermissionService permissionService;
    private CopyService copyService;
    
    /**
	 * Data used by the tests
	 */
	private StoreRef storeRef;
	private NodeRef rootNodeRef;	
	private NodeRef nodeRef;
	private String userNodeRef;
	
	/**
	 * Types and properties used by the tests
	 */
	private static final String TEST_VALUE_NAME = "myDocument.doc";
	private static final String TEST_VALUE_2 = "testValue2";
	private static final String TEST_VALUE_3 = "testValue3";
	private static final QName PROP_NAME_QNAME = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "name");
	private static final QName PROP2_QNAME = ContentModel.PROP_DESCRIPTION;
	private static final String CONTENT_1 = "This is some content";
	private static final String CONTENT_2 = "This is the cotent modified.";
    
    /**
     * User details 
     */
    //private static final String USER_NAME = "cociTest" + GUID.generate();
    private String userName;
    private static final String PWD = "password";
	
	/**
	 * On setup in transaction implementation
	 */
	@Override
	protected void onSetUpInTransaction() 
		throws Exception 
	{
		// Set the services
		this.nodeService = (NodeService)this.applicationContext.getBean("nodeService");
		this.cociService = (CheckOutCheckInService)this.applicationContext.getBean("checkOutCheckInService");
		this.contentService = (ContentService)this.applicationContext.getBean("contentService");
		this.versionService = (VersionService)this.applicationContext.getBean("versionService");
        this.authenticationService = (MutableAuthenticationService)this.applicationContext.getBean("authenticationService");
        this.lockService = (LockService)this.applicationContext.getBean("lockService");
        this.transactionService = (TransactionService)this.applicationContext.getBean("transactionComponent");
        this.permissionService = (PermissionService)this.applicationContext.getBean("permissionService");
        this.copyService = (CopyService)this.applicationContext.getBean("copyService");
        
        // Authenticate as system to create initial test data set
        AuthenticationComponent authenticationComponent = (AuthenticationComponent)this.applicationContext.getBean("authenticationComponent");
        authenticationComponent.setSystemUserAsCurrentUser();
	
		// Create the store and get the root node reference
		this.storeRef = this.nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
		this.rootNodeRef = this.nodeService.getRootNode(storeRef);
		
		// Create the node used for tests
		ChildAssociationRef childAssocRef = this.nodeService.createNode(
				rootNodeRef,
				ContentModel.ASSOC_CHILDREN,
				QName.createQName("test"),
				ContentModel.TYPE_CONTENT);
		this.nodeRef = childAssocRef.getChildRef();
        this.nodeService.addAspect(this.nodeRef, ContentModel.ASPECT_TITLED, null);
        this.nodeService.setProperty(this.nodeRef, ContentModel.PROP_NAME, TEST_VALUE_NAME);
        this.nodeService.setProperty(this.nodeRef, PROP2_QNAME, TEST_VALUE_2);
        
		// Add the initial content to the node
		ContentWriter contentWriter = this.contentService.getWriter(this.nodeRef, ContentModel.PROP_CONTENT, true);
        contentWriter.setMimetype("text/plain");
        contentWriter.setEncoding("UTF-8");
		contentWriter.putContent(CONTENT_1);
		
		// Add the lock and version aspects to the created node
		this.nodeService.addAspect(this.nodeRef, ContentModel.ASPECT_VERSIONABLE, null);
		this.nodeService.addAspect(this.nodeRef, ContentModel.ASPECT_LOCKABLE, null);		
        
        // Create and authenticate the user
        this.userName = "cociTest" + GUID.generate();
        TestWithUserUtils.createUser(this.userName, PWD, this.rootNodeRef, this.nodeService, this.authenticationService);
        TestWithUserUtils.authenticateUser(this.userName, PWD, this.rootNodeRef, this.authenticationService);
        this.userNodeRef = TestWithUserUtils.getCurrentUser(this.authenticationService);
        
        permissionService.setPermission(this.rootNodeRef, this.userName, PermissionService.ALL_PERMISSIONS, true);
        permissionService.setPermission(this.nodeRef, this.userName, PermissionService.ALL_PERMISSIONS, true);
	}
	
	/**
	 * Helper method that creates a bag of properties for the test type
	 * 
	 * @return  bag of properties
	 */
	private Map<QName, Serializable> createTypePropertyBag()
	{
		Map<QName, Serializable> result = new HashMap<QName, Serializable>();
		result.put(PROP_NAME_QNAME, TEST_VALUE_NAME);
		return result;
	}
	
	/**
	 * Test checkout 
	 */
	public void testCheckOut()
	{
		checkout();
	}
	
	/**
	 * 
	 * @return
	 */
	private NodeRef checkout()
	{
		// Check out the node
		NodeRef workingCopy = this.cociService.checkout(
				this.nodeRef, 
				this.rootNodeRef, 
				ContentModel.ASSOC_CHILDREN, 
				QName.createQName("workingCopy"));
		assertNotNull(workingCopy);
		
        //System.out.println(NodeStoreInspector.dumpNodeStore(this.nodeService, this.storeRef));
        
		// Ensure that the working copy and copy aspect has been applied
		assertTrue(this.nodeService.hasAspect(workingCopy, ContentModel.ASPECT_WORKING_COPY));	
		assertTrue(this.nodeService.hasAspect(workingCopy, ContentModel.ASPECT_COPIEDFROM));
		
		// Check that the working copy owner has been set correctly
		assertEquals(this.userNodeRef, this.nodeService.getProperty(workingCopy, ContentModel.PROP_WORKING_COPY_OWNER));
		
		// Check that the working copy name has been set correctly
        String name = (String)this.nodeService.getProperty(this.nodeRef, PROP_NAME_QNAME);
        String workingCopyLabel = ((CheckOutCheckInServiceImpl)this.cociService).createWorkingCopyName(name);
        String workingCopyName = (String)this.nodeService.getProperty(workingCopy, PROP_NAME_QNAME);
        assertEquals(workingCopyLabel, workingCopyName);
		
		// Ensure that the content has been copied correctly
		ContentReader contentReader = this.contentService.getReader(this.nodeRef, ContentModel.PROP_CONTENT);
		assertNotNull(contentReader);
		ContentReader contentReader2 = this.contentService.getReader(workingCopy, ContentModel.PROP_CONTENT);
		assertNotNull(contentReader2);
		assertEquals(
				"The content string of the working copy should match the original immediatly after checkout.", 
				contentReader.getContentString(), 
				contentReader2.getContentString());
        
		return workingCopy;
	}
	
	/**
	 * Test checkIn
	 */
	public void testCheckIn()
	{
		NodeRef workingCopy = checkout();
		
		// Test standard check-in
		Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
		versionProperties.put(Version.PROP_DESCRIPTION, "This is a test version");		
		this.cociService.checkin(workingCopy, versionProperties);	
		
		// Test check-in with content
        NodeRef workingCopy3 = checkout();
        
		this.nodeService.setProperty(workingCopy3, PROP_NAME_QNAME, TEST_VALUE_2);
		this.nodeService.setProperty(workingCopy3, PROP2_QNAME, TEST_VALUE_3);
        ContentWriter tempWriter = this.contentService.getWriter(workingCopy3, ContentModel.PROP_CONTENT, false);
		assertNotNull(tempWriter);
		tempWriter.putContent(CONTENT_2);
		String contentUrl = tempWriter.getContentUrl();
		Map<String, Serializable> versionProperties3 = new HashMap<String, Serializable>();
		versionProperties3.put(Version.PROP_DESCRIPTION, "description");
		versionProperties3.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
		NodeRef origNodeRef = this.cociService.checkin(workingCopy3, versionProperties3, contentUrl, true);
		assertNotNull(origNodeRef);
		
		// Check the checked in content
		ContentReader contentReader = this.contentService.getReader(origNodeRef, ContentModel.PROP_CONTENT);
		assertNotNull(contentReader);
		assertEquals(CONTENT_2, contentReader.getContentString());
		
		// Check that the version history is correct
		Version version = this.versionService.getCurrentVersion(origNodeRef);
		assertNotNull(version);
		assertEquals("description", version.getDescription());
		assertEquals(VersionType.MAJOR, version.getVersionType());
		NodeRef versionNodeRef = version.getFrozenStateNodeRef();
		assertNotNull(versionNodeRef);
		
		// Check the verioned content
		ContentReader versionContentReader = this.contentService.getReader(versionNodeRef, ContentModel.PROP_CONTENT);
		assertNotNull(versionContentReader);	
        assertEquals(CONTENT_2, versionContentReader.getContentString());
		
		// Check that the name is not updated during the check-in
		assertEquals(TEST_VALUE_2, this.nodeService.getProperty(versionNodeRef, PROP_NAME_QNAME));
		assertEquals(TEST_VALUE_2, this.nodeService.getProperty(origNodeRef, PROP_NAME_QNAME));
		
		// Check that the other properties are updated during the check-in
		assertEquals(TEST_VALUE_3, this.nodeService.getProperty(versionNodeRef, PROP2_QNAME));
		assertEquals(TEST_VALUE_3, this.nodeService.getProperty(origNodeRef, PROP2_QNAME));
		
		// Cancel the check out after is has been left checked out
		this.cociService.cancelCheckout(workingCopy3);
		
		// Test keep checked out flag
		NodeRef workingCopy2 = checkout();		
		Map<String, Serializable> versionProperties2 = new HashMap<String, Serializable>();
		versionProperties2.put(Version.PROP_DESCRIPTION, "Another version test");		
		this.cociService.checkin(workingCopy2, versionProperties2, null, true);
		this.cociService.checkin(workingCopy2, new HashMap<String, Serializable>(), null, true);	
	}
	
	public void testCheckOutCheckInWithTranslatableAspect()
	{
		// Create a node to be used as the translation
		NodeRef translationNodeRef = this.nodeService.createNode(
				rootNodeRef,
				ContentModel.ASSOC_CHILDREN,
				QName.createQName("translation"),
				ContentModel.TYPE_CONTENT).getChildRef();
		
		this.nodeService.addAspect(this.nodeRef, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "translatable"), null);
		this.nodeService.createAssociation(this.nodeRef, translationNodeRef, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "translations"));
				
		// Check it out
		NodeRef workingCopy = this.cociService.checkout(
				this.nodeRef, 
				this.rootNodeRef, 
				ContentModel.ASSOC_CHILDREN, 
				QName.createQName("workingCopy"));
		
				
		// Check it back in again
		Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
		versionProperties.put(Version.PROP_DESCRIPTION, "This is a test version");
		this.cociService.checkin(workingCopy, versionProperties);
	}
	
	/**
	 * Test when the aspect is not set when check-in is performed
	 */
	public void testVersionAspectNotSetOnCheckIn()
	{
		// Create a bag of props
        Map<QName, Serializable> bagOfProps = createTypePropertyBag();
        bagOfProps.put(ContentModel.PROP_CONTENT, new ContentData(null, MimetypeMap.MIMETYPE_TEXT_PLAIN, 0L, "UTF-8"));

		// Create a new node 
		ChildAssociationRef childAssocRef = this.nodeService.createNode(
				rootNodeRef,
				ContentModel.ASSOC_CHILDREN,
				QName.createQName("test"),
				ContentModel.TYPE_CONTENT,
				bagOfProps);
		NodeRef noVersionNodeRef = childAssocRef.getChildRef();
		
		// Check out and check in
		NodeRef workingCopy = this.cociService.checkout(noVersionNodeRef);
		this.cociService.checkin(workingCopy, new HashMap<String, Serializable>());
		
		// Check that the origional node has no version history dispite sending verion props
		assertNull(this.versionService.getVersionHistory(noVersionNodeRef));		
	}
	
	/**
	 * Test cancel checkOut
	 */
	public void testCancelCheckOut()
	{
		NodeRef workingCopy = checkout();
		assertNotNull(workingCopy);
        
        try
        {
            this.lockService.checkForLock(this.nodeRef);
            fail("The origional should be locked now.");
        }
        catch (Throwable exception)
        {
            // Good the origional is locked
        }
		
		NodeRef origNodeRef = this.cociService.cancelCheckout(workingCopy);
		assertEquals(this.nodeRef, origNodeRef);
        
		// The origional should no longer be locked
        this.lockService.checkForLock(origNodeRef);
	}
    
    /**
     * Test the deleting a wokring copy node removed the lock on the origional node
     */
    public void testAutoCancelCheckOut()
    {
        NodeRef workingCopy = checkout();
        assertNotNull(workingCopy);
        
        try
        {
            this.lockService.checkForLock(this.nodeRef);
            fail("The origional should be locked now.");
        }
        catch (Throwable exception)
        {
            // Good the origional is locked
        }
        
        // Delete the working copy
        this.nodeService.deleteNode(workingCopy);
        
        // The origional should no longer be locked
        this.lockService.checkForLock(this.nodeRef);
        
    }
    
    /**
     * Test the getWorkingCopy method
     */
    public void testGetWorkingCopy()
    {
        NodeRef origNodeRef = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("test2"),
                ContentModel.TYPE_CONTENT).getChildRef();
        
        
        NodeRef wk1 = this.cociService.getWorkingCopy(origNodeRef);
        assertNull(wk1);

        // Check the document out
        final NodeRef workingCopy = this.cociService.checkout(origNodeRef);
        
        // Need to commit the transaction in order to get the indexer to run
        setComplete();
        endTransaction();
        
        final NodeRef finalNodeRef = origNodeRef;        
        
        this.transactionService.getRetryingTransactionHelper().doInTransaction(
                new RetryingTransactionCallback<Object>()
                {
                    public Object execute()
                    {
                        NodeRef wk2 = CheckOutCheckInServiceImplTest.this.cociService.getWorkingCopy(finalNodeRef);
                        assertNotNull(wk2);
                        assertEquals(workingCopy, wk2);
                        
                        CheckOutCheckInServiceImplTest.this.cociService.cancelCheckout(workingCopy);                        
                        return null;
                    }
                });
        
        NodeRef wk3 = this.cociService.getWorkingCopy(this.nodeRef);
        assertNull(wk3);           
    }
    
    /**
     * Test the getWorkingCopy method
     */
    public void testETWOTWO_733()
    {
        NodeRef origNodeRef = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("test2"),
                ContentModel.TYPE_CONTENT).getChildRef();
        
        // Make a copy of the node
        this.copyService.copyAndRename(
                origNodeRef,
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("test6"),
                false);        
        
        NodeRef wk1 = this.cociService.getWorkingCopy(origNodeRef);
        assertNull(wk1);

        // Check the document out
        final NodeRef workingCopy = this.cociService.checkout(origNodeRef);
        
        // Need to commit the transaction in order to get the indexer to run
        setComplete();
        endTransaction();
        
        final NodeRef finalNodeRef = origNodeRef;        
        
        this.transactionService.getRetryingTransactionHelper().doInTransaction(
                new RetryingTransactionCallback<Object>()
                {
                    public Object execute()
                    {
                        NodeRef wk2 = CheckOutCheckInServiceImplTest.this.cociService.getWorkingCopy(finalNodeRef);
                        assertNotNull(wk2);
                        assertEquals(workingCopy, wk2);
                        
                        CheckOutCheckInServiceImplTest.this.cociService.cancelCheckout(workingCopy);                        
                        return null;
                    }
                });
        
        NodeRef wk3 = this.cociService.getWorkingCopy(this.nodeRef);
        assertNull(wk3);           
    }
    
    public void testAR1056()
    {
    	// Check out the node
		NodeRef workingCopy = this.cociService.checkout(
				this.nodeRef, 
				this.rootNodeRef, 
				ContentModel.ASSOC_CHILDREN, 
				QName.createQName("workingCopy"));
		assertNotNull(workingCopy);
		
		// Try and check the same node out again
		try
		{
			this.cociService.checkout(
				this.nodeRef, 
				this.rootNodeRef, 
				ContentModel.ASSOC_CHILDREN, 
				QName.createQName("workingCopy2"));
			fail("This document has been checked out twice.");
		}
		catch (Exception exception)
		{
			// Good because we shouldnt be able to checkout a document twice
		}
    
    }
    
    public void testMultipleCheckoutsCheckInsWithPropChange()
    {
        // Note: this test assumes cm:autoVersionProps=true by default (refer to cm:versionableAspect in contentModel.xml)
        
        // Create a new node 
        ChildAssociationRef childAssocRef = this.nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("test"),
                ContentModel.TYPE_CONTENT,
                null);
        final NodeRef testNodeRef = childAssocRef.getChildRef();
        
        // Add the version aspect to the created node
        this.nodeService.addAspect(testNodeRef, ContentModel.ASPECT_VERSIONABLE, null);
        
        setComplete();
        endTransaction();
        
        // Checkout
        final NodeRef workingCopy1 = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Exception
            {
                return cociService.checkout(testNodeRef);
            }
        });
        
        // Change property and checkin
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
                nodeService.setProperty(workingCopy1, ContentModel.PROP_AUTHOR, "author1");
                
                Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
                versionProperties.put(Version.PROP_DESCRIPTION, "This is a test version 1");
                cociService.checkin(workingCopy1, versionProperties);
                
                return null;
            }
        });
        
        // Checkout
        final NodeRef workingCopy2 = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Exception
            {
                return cociService.checkout(testNodeRef);
            }
        });
        
        // Change property and checkin
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
                nodeService.setProperty(workingCopy2, ContentModel.PROP_AUTHOR, "author2");
                
                Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
                versionProperties.put(Version.PROP_DESCRIPTION, "This is a test version 2");
                cociService.checkin(workingCopy2, versionProperties);
                
                return null;
            }
        });
        
        // Checkout
        final NodeRef workingCopy3 = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Exception
            {
                return cociService.checkout(testNodeRef);
            }
        });
        
        // Change property and checkin
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
                nodeService.setProperty(workingCopy3, ContentModel.PROP_AUTHOR, "author3");
                
                Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
                versionProperties.put(Version.PROP_DESCRIPTION, "This is a test version 3");
                cociService.checkin(workingCopy3, versionProperties);
                
                return null;
            }
        });
    }
    
    public void testalfrescoCheckoutDoesntModifyNode()
    {
        String adminUser = AuthenticationUtil.getAdminUserName();
        AuthenticationUtil.setFullyAuthenticatedUser(adminUser);
        
        Serializable initModifieer = nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIER);
        Serializable initModifieed = nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED);
        assertFalse("The initial modifier should not be Admin!", adminUser.equals(initModifieer));
        
        NodeRef copy = cociService.checkout(
                nodeRef, 
                rootNodeRef, 
                ContentModel.ASSOC_CHILDREN, 
                QName.createQName("workingCopy"));

        Serializable modifieer = nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIER);
        assertEquals("Checkout should not cause the modifier to change!", initModifieer, modifieer);
        Serializable modifieed = nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED);
        assertEquals("Checkout should not cause the modified date to change!", initModifieed, modifieed);

        cociService.cancelCheckout(copy);
        modifieer = nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIER);
        assertEquals("Checkout should not cause the modifier to change!", initModifieer, modifieer);
        modifieed = nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED);
        assertEquals("Checkout should not cause the modified date to change!", initModifieed, modifieed);
        
        copy = cociService.checkout(
                nodeRef, 
                rootNodeRef, 
                ContentModel.ASSOC_CHILDREN, 
                QName.createQName("workingCopy"));
        Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
        versionProperties.put(Version.PROP_DESCRIPTION, "This is a test version");      
        cociService.checkin(copy, versionProperties);
        
        modifieer = nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIER);
        assertEquals("The modifier should change to Admin after checkin!", adminUser, modifieer);
    }
    
    public void testCheckOutPermissions_ALF7680_ALF535()
    {
        /*
         * Testing working copy creation in folder of source node. 
         * User has no permissions to create children in this folder. 
         * User has permissions to edit document.
         * Expected result: working copy should be created.
         */
        
        NodeRef folder1 = createFolderWithPermission(rootNodeRef, userName, PermissionService.CONSUMER);
        NodeRef node = createNodeWithPermission(folder1, userName, PermissionService.EDITOR);

        // Check out the node
        NodeRef workingCopy = this.cociService.checkout(
                node, 
                folder1, 
                ContentModel.ASSOC_CHILDREN, 
                QName.createQName("workingCopy"));

        // Ensure that the working copy was created and current user was set as owner
        assertNotNull(workingCopy);
        assertTrue(this.nodeService.hasAspect(workingCopy, ContentModel.ASPECT_WORKING_COPY));
        assertEquals(this.userNodeRef, this.nodeService.getProperty(workingCopy, ContentModel.PROP_WORKING_COPY_OWNER));

        this.cociService.cancelCheckout(workingCopy);

        /*
         * Testing working copy creation in a different folder. 
         * User has permissions to create children in this folder. 
         * User has permissions to edit document.
         * Expected result: working copy should be created.
         */
        
        NodeRef folder2 = createFolderWithPermission(rootNodeRef, userName, PermissionService.ALL_PERMISSIONS);

        // Check out the node
        workingCopy = this.cociService.checkout(
                node, 
                folder2, 
                ContentModel.ASSOC_CHILDREN, 
                QName.createQName("workingCopy"));

        // Ensure that the working copy was created and current user was set as owner
        assertNotNull(workingCopy);
        assertTrue(this.nodeService.hasAspect(workingCopy, ContentModel.ASPECT_WORKING_COPY));
        assertEquals(this.userNodeRef, this.nodeService.getProperty(workingCopy, ContentModel.PROP_WORKING_COPY_OWNER));

        this.cociService.cancelCheckout(workingCopy);

        /*
         * Testing working copy creation in a different folder. 
         * User has no permissions to create children in this folder. 
         * User has permissions to edit document.
         * Expected result: exception.
         */
        
        NodeRef folder3 = createFolderWithPermission(rootNodeRef, userName, PermissionService.CONSUMER);
        try
        {
            // Check out the node
            workingCopy = this.cociService.checkout(
                    node, 
                    folder3, 
                    ContentModel.ASSOC_CHILDREN, 
                    QName.createQName("workingCopy"));

            // Ensure that the working copy was not created and exception occurs
            fail("Node can't be checked out to folder where user has no permissions to create children");
        }
        catch (Exception e)
        {
            // Exception is expected
        }
        
        /*
         * Testing working copy creation in a different folder. 
         * User has permissions to create children in this folder. 
         * User has no permissions to edit document. 
         * Expected result: exception.
         */
        
        NodeRef node2 = createNodeWithPermission(folder3, userName, PermissionService.CONSUMER);
        try
        {
            // Check out the node
            workingCopy = this.cociService.checkout(
                    node2, 
                    folder3, 
                    ContentModel.ASSOC_CHILDREN, 
                    QName.createQName("workingCopy"));

            // Ensure that the working copy was not created and exception occurs
            fail("Node can't be checked out if user has no permissions to edit document");
        }
        catch (Exception e)
        {
            // Exception is expected
        }
    }

    private NodeRef createFolderWithPermission(NodeRef parent, String username, String permission)
    {
        // Authenticate as system user because the current user should not be node owner
        AuthenticationComponent authenticationComponent = (AuthenticationComponent) this.applicationContext.getBean("authenticationComponent");
        authenticationComponent.setSystemUserAsCurrentUser();

        // Create the folder
        NodeRef folder = this.nodeService.createNode(
                parent, 
                ContentModel.ASSOC_CHILDREN, 
                QName.createQName("TestFolder" + GUID.generate()), 
                ContentModel.TYPE_CONTENT).getChildRef();

        // Apply permissions to folder
        permissionService.deletePermissions(folder);
        permissionService.setInheritParentPermissions(folder, false);
        permissionService.setPermission(folder, userName, permission, true);

        // Authenticate test user
        TestWithUserUtils.authenticateUser(this.userName, PWD, this.rootNodeRef, this.authenticationService);

        return folder;
    }

    private NodeRef createNodeWithPermission(NodeRef parent, String username, String permission)
    {
        // Authenticate as system user because the current user should not be node owner
        AuthenticationComponent authenticationComponent = (AuthenticationComponent) this.applicationContext.getBean("authenticationComponent");
        authenticationComponent.setSystemUserAsCurrentUser();

        // Create the node as a copy of prepared
        NodeRef node = copyService.copy(nodeRef, parent, ContentModel.ASSOC_CHILDREN, ContentModel.TYPE_CONTENT);

        // Apply permissions to node
        permissionService.deletePermissions(node);
        permissionService.setInheritParentPermissions(node, false);
        permissionService.setPermission(node, userName, permission, true);

        // Authenticate test user
        TestWithUserUtils.authenticateUser(this.userName, PWD, this.rootNodeRef, this.authenticationService);

        return node;
    }
    
}
