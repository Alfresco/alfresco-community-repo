/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.repo.version;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.version.common.counter.VersionCounterService;
import org.alfresco.repo.version.common.versionlabel.SerialVersionLabelPolicy;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.TestWithUserUtils;

public abstract class BaseVersionStoreTest extends BaseSpringTest 
{
	/*
     * Services used by the tests
     */
	protected NodeService dbNodeService;
    protected VersionService versionService;
    protected VersionCounterService versionCounterDaoService;
    protected ContentService contentService;
	protected DictionaryDAO dictionaryDAO;
    protected AuthenticationService authenticationService;
    protected TransactionService transactionService;
    protected MutableAuthenticationDao authenticationDAO;
    protected NodeArchiveService nodeArchiveService;
    protected NodeService nodeService;
	
    /*
     * Data used by tests
     */
    protected StoreRef testStoreRef;
    protected NodeRef rootNodeRef;
    protected Map<String, Serializable> versionProperties;
    protected HashMap<QName, Serializable> nodeProperties;
    
    /**
     * The most recent set of versionable nodes created by createVersionableNode
     */
    protected HashMap<String, NodeRef> versionableNodes;
    
    /*
     * Proprety names and values
     */
    protected static final String TEST_NAMESPACE = "http://www.alfresco.org/test/versionstorebasetest/1.0";
	protected static final QName TEST_TYPE_QNAME = QName.createQName(TEST_NAMESPACE, "testtype");
    protected static final QName TEST_ASPECT_QNAME = QName.createQName(TEST_NAMESPACE, "testaspect");
	protected static final QName PROP_1 = QName.createQName(TEST_NAMESPACE, "prop1");
	protected static final QName PROP_2 = QName.createQName(TEST_NAMESPACE, "prop2");
	protected static final QName PROP_3 = QName.createQName(TEST_NAMESPACE, "prop3");
    protected static final QName MULTI_PROP = QName.createQName(TEST_NAMESPACE, "multiProp");
	protected static final String VERSION_PROP_1 = "versionProp1";
	protected static final String VERSION_PROP_2 = "versionProp2";
	protected static final String VERSION_PROP_3 = "versionProp3";
	protected static final String VALUE_1 = "value1";
	protected static final String VALUE_2 = "value2";
	protected static final String VALUE_3 = "value3";
	protected static final QName TEST_CHILD_ASSOC_1 = QName.createQName(TEST_NAMESPACE, "childassoc1");
	protected static final QName TEST_CHILD_ASSOC_2 = QName.createQName(TEST_NAMESPACE, "childassoc2");
	protected static final QName TEST_ASSOC = QName.createQName(TEST_NAMESPACE, "assoc1");	
    
    protected Collection<String> multiValue = null;
    protected static final String MULTI_VALUE_1 = "multi1";
    protected static final String MULTI_VALUE_2 = "multi2";
    
    /**
     * Test content
     */
    protected static final String TEST_CONTENT = "This is the versioned test content.";
    
    /**
     * Test user details
     */
    private static final String PWD = "admin";
    private static final String USER_NAME = "admin";	
    
	/**
	 * Sets the meta model dao
	 * 
	 * @param dictionaryDAO  the meta model dao
	 */
	public void setDictionaryDAO(DictionaryDAO dictionaryDAO) 
	{
		this.dictionaryDAO = dictionaryDAO;
	}
	
    /**
     * Called during the transaction setup
     */
    protected void onSetUpInTransaction() throws Exception
    {
        // Set the multi value if required
        if (this.multiValue == null)
        {
            this.multiValue = new ArrayList<String>();
            this.multiValue.add(MULTI_VALUE_1);
            this.multiValue.add(MULTI_VALUE_2);
        }
        
        // Get the services by name from the application context
        this.dbNodeService = (NodeService)applicationContext.getBean("dbNodeService");
        this.versionService = (VersionService)applicationContext.getBean("versionService");
        this.versionCounterDaoService = (VersionCounterService)applicationContext.getBean("versionCounterService");
        this.contentService = (ContentService)applicationContext.getBean("contentService");
        this.authenticationService = (AuthenticationService)applicationContext.getBean("authenticationService");
        this.transactionService = (TransactionService)this.applicationContext.getBean("transactionComponent");
        this.authenticationDAO = (MutableAuthenticationDao) applicationContext.getBean("alfDaoImpl");
        this.nodeArchiveService = (NodeArchiveService) applicationContext.getBean("nodeArchiveService");
        this.nodeService = (NodeService)applicationContext.getBean("nodeService");
        
        authenticationService.clearCurrentSecurityContext();
        
		// Create the test model
		createTestModel();
		
        // Create a bag of properties for later use
        this.versionProperties = new HashMap<String, Serializable>();
        versionProperties.put(VERSION_PROP_1, VALUE_1);
        versionProperties.put(VERSION_PROP_2, VALUE_2);
        versionProperties.put(VERSION_PROP_3, VALUE_3);
        
        // Create the node properties
        this.nodeProperties = new HashMap<QName, Serializable>();
        this.nodeProperties.put(PROP_1, VALUE_1);
        this.nodeProperties.put(PROP_2, VALUE_2);
        this.nodeProperties.put(PROP_3, VALUE_3);
        this.nodeProperties.put(MULTI_PROP, (Serializable)multiValue);
        this.nodeProperties.put(ContentModel.PROP_CONTENT, new ContentData(null, "text/plain", 0L, "UTF-8"));
        
        // Create a workspace that contains the 'live' nodes
        this.testStoreRef = this.dbNodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        
        // Get a reference to the root node
        this.rootNodeRef = this.dbNodeService.getRootNode(this.testStoreRef);
        
        // Create an authenticate the user
        
        if(!authenticationDAO.userExists(USER_NAME))
        {
            authenticationService.createAuthentication(USER_NAME, PWD.toCharArray());
        }
        
        TestWithUserUtils.authenticateUser(USER_NAME, PWD, this.rootNodeRef, this.authenticationService);
    }
	
	/**
	 * Creates the test model used by the tests
	 */
	private void createTestModel()
	{
        InputStream is = getClass().getClassLoader().getResourceAsStream("org/alfresco/repo/version/VersionStoreBaseTest_model.xml");
        M2Model model = M2Model.createModel(is);
        dictionaryDAO.putModel(model);
	}
    
    /**
     * Creates a new versionable node
     * 
     * @return  the node reference
     */
    protected NodeRef createNewVersionableNode()
    {
        // Use this map to retrive the versionable nodes in later tests
        this.versionableNodes = new HashMap<String, NodeRef>();
        
        // Create node (this node has some content)
        NodeRef nodeRef = this.dbNodeService.createNode(
                rootNodeRef, 
                ContentModel.ASSOC_CHILDREN, 
                QName.createQName("{test}MyVersionableNode"),
                TEST_TYPE_QNAME,
                this.nodeProperties).getChildRef();
        this.dbNodeService.addAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE, new HashMap<QName, Serializable>());
        
        assertNotNull(nodeRef);
        this.versionableNodes.put(nodeRef.getId(), nodeRef);
        
        // Add the content to the node
        ContentWriter contentWriter = this.contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        contentWriter.putContent(TEST_CONTENT);
        
        // Add some children to the node
        NodeRef child1 = this.dbNodeService.createNode(
                nodeRef,
				TEST_CHILD_ASSOC_1,
                TEST_CHILD_ASSOC_1,
				TEST_TYPE_QNAME,
                this.nodeProperties).getChildRef();
        this.dbNodeService.addAspect(child1, ContentModel.ASPECT_VERSIONABLE, new HashMap<QName, Serializable>());
        assertNotNull(child1);
        this.versionableNodes.put(child1.getId(), child1);
        NodeRef child2 = this.dbNodeService.createNode(
                nodeRef,
				TEST_CHILD_ASSOC_2,
                TEST_CHILD_ASSOC_2,
				TEST_TYPE_QNAME,
                this.nodeProperties).getChildRef();
        this.dbNodeService.addAspect(child2, ContentModel.ASPECT_VERSIONABLE, new HashMap<QName, Serializable>());
        assertNotNull(child2);
        this.versionableNodes.put(child2.getId(), child2);
        
        // Create a node that can be associated with the root node
        NodeRef assocNode = this.dbNodeService.createNode(
                rootNodeRef,
				ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}MyAssocNode"),
				TEST_TYPE_QNAME,
                this.nodeProperties).getChildRef();
        assertNotNull(assocNode);
        this.dbNodeService.createAssociation(nodeRef, assocNode, TEST_ASSOC);
        
        return nodeRef;
    }
    
    /**
     * Creates a new version, checking the properties of the version.
     * <p>
     * The default test propreties are assigned to the version.
     * 
     * @param versionableNode    the versionable node
     * @return                   the created (and checked) new version
     */
    protected Version createVersion(NodeRef versionableNode)
    {
        return createVersion(versionableNode, this.versionProperties);
    }
    
    /**
     * Creates a new version, checking the properties of the version.
     * 
     * @param versionableNode    the versionable node
     * @param versionProperties  the version properties
     * @return                   the created (and checked) new version
     */
    protected Version createVersion(NodeRef versionableNode, Map<String, Serializable> versionProperties)
    {
        // Get the next version number
        int nextVersion = peekNextVersionNumber(); 
        String nextVersionLabel = peekNextVersionLabel(versionableNode, nextVersion, versionProperties);
		
        // Snap-shot the date-time
        long beforeVersionTime = System.currentTimeMillis();
        
        // Now lets create a new version for this node
        Version newVersion = versionService.createVersion(versionableNode, this.versionProperties);
        checkNewVersion(beforeVersionTime, nextVersion, nextVersionLabel, newVersion, versionableNode);
        
        // Return the new version
        return newVersion;
    }
	
	/**
	 * Gets the next version label
	 */
	protected String peekNextVersionLabel(NodeRef nodeRef, int versionNumber, Map<String, Serializable> versionProperties)
	{
		Version version = this.versionService.getCurrentVersion(nodeRef);		
		SerialVersionLabelPolicy policy = new SerialVersionLabelPolicy();
		return policy.calculateVersionLabel(ContentModel.TYPE_CMOBJECT, version, versionNumber, versionProperties);
	}
    
    /**
     * Checkd the validity of a new version
     * 
     * @param beforeVersionTime     the time snap shot before the version was created
     * @param expectedVersionNumber the expected version number
     * @param newVersion            the new version
     * @param versionableNode       the versioned node
     */
    protected void checkNewVersion(long beforeVersionTime, int expectedVersionNumber, String expectedVersionLabel, Version newVersion, NodeRef versionableNode)
    {
        assertNotNull(newVersion);
        
        // Check the version label and version number
        assertEquals(
                "The expected version number was not used.",
                Integer.toString(expectedVersionNumber), 
                newVersion.getVersionProperty(VersionModel.PROP_VERSION_NUMBER).toString());
		assertEquals(
				"The expected version label was not used.",
				expectedVersionLabel,
				newVersion.getVersionLabel());
        
        // Check the created date
        long afterVersionTime = System.currentTimeMillis();
        long createdDate = newVersion.getCreatedDate().getTime();
        if (createdDate < beforeVersionTime || createdDate > afterVersionTime)
        {
            fail("The created date of the version is incorrect.");
        }
        
        // Check the creator 
        assertEquals(USER_NAME, newVersion.getCreator());
        
        // Check the properties of the verison
        Map<String, Serializable> props = newVersion.getVersionProperties();
        assertNotNull("The version properties collection should not be null.", props);
        // TODO sort this out - need to check for the reserved properties too
        //assertEquals(versionProperties.size(), props.size());
        for (String key : versionProperties.keySet())
        {
            assertEquals(
                    versionProperties.get(key), 
                    newVersion.getVersionProperty(key));
        }
        
        // Check that the node reference is correct
        NodeRef nodeRef = newVersion.getFrozenStateNodeRef();
        assertNotNull(nodeRef);
        assertEquals(
                VersionModel.STORE_ID, 
                nodeRef.getStoreRef().getIdentifier());
        assertEquals(
                VersionModel.STORE_PROTOCOL, 
                nodeRef.getStoreRef().getProtocol());
        assertNotNull(nodeRef.getId());        
        
        // TODO: How do we check the frozen attributes ??
        
        // Check the node ref for the current version
        String currentVersionLabel = (String)this.dbNodeService.getProperty(
                versionableNode,
                ContentModel.PROP_VERSION_LABEL);
        assertEquals(newVersion.getVersionLabel(), currentVersionLabel);
    }
    
    /**
     * Returns the next version number without affecting the version counter.
     * 
     * @return  the next version number to be allocated
     */
    protected int peekNextVersionNumber()
    {
        StoreRef lwVersionStoreRef = this.versionService.getVersionStoreReference();
        return this.versionCounterDaoService.currentVersionNumber(lwVersionStoreRef) + 1; 
    }

}
