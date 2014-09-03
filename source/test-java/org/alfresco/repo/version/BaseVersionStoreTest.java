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
package org.alfresco.repo.version;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryBootstrap;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.node.StoreArchiveMap;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.version.common.versionlabel.SerialVersionLabelPolicy;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.BaseSpringTest;

public abstract class BaseVersionStoreTest extends BaseSpringTest
{
    /*
     * Services used by the tests
     */
    protected NodeService dbNodeService;
    protected VersionService versionService;
    protected ContentService contentService;
    protected DictionaryDAO dictionaryDAO;
    protected MutableAuthenticationService authenticationService;
    protected TransactionService transactionService;
    protected RetryingTransactionHelper txnHelper;
    protected MutableAuthenticationDao authenticationDAO;
    protected NodeArchiveService nodeArchiveService;
    protected NodeService nodeService;
    protected PermissionService permissionService;
    protected CheckOutCheckInService checkOutCheckInService;
    protected VersionMigrator versionMigrator;
    protected SearchService versionSearchService;
    protected DictionaryService dictionaryService;
    protected PolicyComponent policyComponent;
    protected BehaviourFilter policyBehaviourFilter;

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
     * Property names and values
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
    protected static final QName TEST_ATS_PARENT_TYPE_QNAME = QName.createQName(TEST_NAMESPACE, "atsParent");
    protected static final QName TEST_ATS_CHILD_TYPE_QNAME = QName.createQName(TEST_NAMESPACE, "atsChild");
    protected static final QName TEST_ATS_RELATED_CHILDREN_QNAME = QName.createQName(TEST_NAMESPACE, "atsRelatedChildren");
    protected static final QName PROP_ATS_PARENT_ID = QName.createQName(TEST_NAMESPACE, "atsParentID");
    protected static final QName PROP_ATS_CHILD_ID = QName.createQName(TEST_NAMESPACE, "atsChildID");
    
    protected Collection<String> multiValue = null;
    protected static final String MULTI_VALUE_1 = "multi1";
    protected static final String MULTI_VALUE_2 = "multi2";
    
    protected MLText mlText;
    protected static final QName MLTEXT_PROP = QName.createQName(TEST_NAMESPACE, "propMl");
    
    /**
     * Test content
     */
    protected static final String TEST_CONTENT = "This is the versioned test content.";
    
    /**
     * Test user details
     */
    private static final String PWD = "admin";
    
	/**
	 * Sets the meta model dao
	 * 
	 * @param dictionaryDAO  the meta model dao
	 */
	public void setDictionaryDAO(DictionaryDAO dictionaryDAO) 
	{
		this.dictionaryDAO = dictionaryDAO;
	}
	
	public void setVersionService(VersionService versionService) 
	{
	    this.versionService = versionService;
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
        this.contentService = (ContentService)applicationContext.getBean("contentService");
        this.authenticationService = (MutableAuthenticationService)applicationContext.getBean("authenticationService");
        this.transactionService = (TransactionService)this.applicationContext.getBean("transactionComponent");
        this.txnHelper = (RetryingTransactionHelper) applicationContext.getBean("retryingTransactionHelper");
        this.authenticationDAO = (MutableAuthenticationDao) applicationContext.getBean("authenticationDao");
        this.nodeArchiveService = (NodeArchiveService) applicationContext.getBean("nodeArchiveService");
        this.nodeService = (NodeService)applicationContext.getBean("nodeService");
        this.permissionService = (PermissionService)this.applicationContext.getBean("permissionService");
        this.checkOutCheckInService = (CheckOutCheckInService) applicationContext.getBean("checkOutCheckInService");
        this.versionSearchService = (SearchService)this.applicationContext.getBean("versionSearchService");
        this.versionMigrator = (VersionMigrator)this.applicationContext.getBean("versionMigrator");
        this.dictionaryService = (DictionaryService)this.applicationContext.getBean("dictionaryService");
        this.policyComponent = (PolicyComponent)this.applicationContext.getBean("policyComponent");
        this.policyBehaviourFilter = (BehaviourFilter)this.applicationContext.getBean("policyBehaviourFilter");
        
        setVersionService((VersionService)applicationContext.getBean("versionService"));
        
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
        
        // Add mlText property
        this.mlText = new MLText(Locale.UK, "UK value");
        this.mlText.addValue(Locale.US, "Y'all US value");
        this.nodeProperties.put(MLTEXT_PROP, this.mlText);
        
        // Create a workspace that contains the 'live' nodes
        this.testStoreRef = this.dbNodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        
        StoreRef archiveStoreRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "archive" + getName() + System.currentTimeMillis());
        
        // Map the work store to the archive store.  This will already be wired into the NodeService.
        StoreArchiveMap archiveMap = (StoreArchiveMap) applicationContext.getBean("storeArchiveMap");
        archiveMap.put(testStoreRef, archiveStoreRef);        
        
        // Get a reference to the root node
        this.rootNodeRef = this.dbNodeService.getRootNode(this.testStoreRef);
        
        // Create and authenticate the user
        
        if(!authenticationDAO.userExists(AuthenticationUtil.getAdminUserName()))
        {
            authenticationService.createAuthentication(AuthenticationUtil.getAdminUserName(), PWD.toCharArray());
        }
        
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
    }
    
    /**
     * Creates the test model used by the tests
     */
    private void createTestModel()
    {
        // register the test model
        List<String> bootstrapModels = new ArrayList<String>();
        bootstrapModels.add("org/alfresco/repo/version/VersionStoreBaseTest_model.xml");
        
        DictionaryBootstrap bootstrap = new DictionaryBootstrap();
        bootstrap.setModels(bootstrapModels);
        bootstrap.setDictionaryDAO(dictionaryDAO);
        
        bootstrap.bootstrap();
    }
    
    /**
     * Creates a new versionable node
     * 
     * @return  the node reference
     */
    protected NodeRef createNewVersionableNode()
    {
    	return createNode(true, TEST_TYPE_QNAME);
    }
    
    protected NodeRef createNewNode()
    {
    	return createNode(false, TEST_TYPE_QNAME);
    }
    
    protected NodeRef createNode(boolean versionable, QName nodeType)
    {
        // Use this map to retrive the versionable nodes in later tests
        this.versionableNodes = new HashMap<String, NodeRef>();
        
        // Create node (this node has some content)
        NodeRef nodeRef = this.dbNodeService.createNode(
                rootNodeRef, 
                ContentModel.ASSOC_CHILDREN, 
                QName.createQName("{test}MyVersionableNode"),
                nodeType,
                this.nodeProperties).getChildRef();
        if (versionable)
        {
            this.dbNodeService.addAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE, new HashMap<QName, Serializable>());
        }
        
        assertNotNull(nodeRef);
        this.versionableNodes.put(nodeRef.getId(), nodeRef);
        
        // Add the content to the node
        ContentWriter contentWriter = this.contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        contentWriter.putContent(TEST_CONTENT);
        
        // Set author
        Map<QName, Serializable> authorProps = new HashMap<QName, Serializable>(1, 1.0f);
        authorProps.put(ContentModel.PROP_AUTHOR, "Charles Dickens");
        this.dbNodeService.addAspect(nodeRef, ContentModel.ASPECT_AUTHOR, authorProps);
        
        // Add some children to the node
        NodeRef child1 = this.dbNodeService.createNode(
                nodeRef,
                TEST_CHILD_ASSOC_1,
                TEST_CHILD_ASSOC_1,
                nodeType,
                this.nodeProperties).getChildRef();
        
        if (versionable)
        {
            this.dbNodeService.addAspect(child1, ContentModel.ASPECT_VERSIONABLE, new HashMap<QName, Serializable>());
        }
        
        assertNotNull(child1);
        this.versionableNodes.put(child1.getId(), child1);
        NodeRef child2 = this.dbNodeService.createNode(
                nodeRef,
                TEST_CHILD_ASSOC_2,
                TEST_CHILD_ASSOC_2,
                nodeType,
                this.nodeProperties).getChildRef();
        
        if (versionable)
        {
        	this.dbNodeService.addAspect(child2, ContentModel.ASPECT_VERSIONABLE, new HashMap<QName, Serializable>());
        }
        
        assertNotNull(child2);
        this.versionableNodes.put(child2.getId(), child2);
        
        // Create a node that can be associated with the root node
        NodeRef assocNode = this.dbNodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}MyAssocNode"),
                nodeType,
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
        // Get the next version label
        String nextVersionLabel = peekNextVersionLabel(versionableNode, versionProperties);
        
        // Snap-shot the node created date-time
        long beforeVersionTime = ((Date)nodeService.getProperty(versionableNode, ContentModel.PROP_CREATED)).getTime();
        
        // Now lets create a new version for this node
        Version newVersion = versionService.createVersion(versionableNode, this.versionProperties);
        checkNewVersion(beforeVersionTime, nextVersionLabel, newVersion, versionableNode);
        
        // Return the new version
        return newVersion;
    }
    
    protected Collection<Version> createVersion(NodeRef versionableNode, Map<String, Serializable> versionProperties, boolean versionChildren)
    {
        // Get the next version label
        String nextVersionLabel = peekNextVersionLabel(versionableNode, versionProperties);
        
        // Snap-shot the node created date-time
        long beforeVersionTime = ((Date)nodeService.getProperty(versionableNode, ContentModel.PROP_CREATED)).getTime();
        
        // Now lets create new version for this node (optionally with children)
        Collection<Version> versions = versionService.createVersion(versionableNode, this.versionProperties, versionChildren);
        
        // Check the returned versions are correct
        checkVersionCollection(nextVersionLabel, beforeVersionTime, versions);
        
        // Return the new versions
        return versions;
    }
    
    /**
     * Gets the next version label
     */
    protected String peekNextVersionLabel(NodeRef nodeRef, Map<String, Serializable> versionProperties)
    {
        Version version = this.versionService.getCurrentVersion(nodeRef);
        SerialVersionLabelPolicy policy = new SerialVersionLabelPolicy();
        return policy.calculateVersionLabel(ContentModel.TYPE_CMOBJECT, version, versionProperties);
    }
    
    /**
     * Checkd the validity of a new version
     * 
     * @param beforeVersionTime     the time snap shot before the version was created
     * @param newVersion            the new version
     * @param versionableNode       the versioned node
     */
    protected void checkVersion(long beforeVersionTime, String expectedVersionLabel, Version newVersion, NodeRef versionableNode)
    {
        assertNotNull(newVersion);
        
        // Check the version label
        assertEquals(
                "The expected version label was not used.",
                expectedVersionLabel,
                newVersion.getVersionLabel());
        
        // Check the created date
        long afterVersionTime = System.currentTimeMillis();
        long createdDate = newVersion.getFrozenModifiedDate().getTime();
        if (createdDate < beforeVersionTime || createdDate > afterVersionTime)
        {
            fail("The created date of the version is incorrect.");
        }
        
        // Check the creator
        assertEquals(AuthenticationUtil.getAdminUserName(), newVersion.getFrozenModifier());
        
        // Check the metadata properties of the version
        Map<String, Serializable> props = newVersion.getVersionProperties();
        assertNotNull("The version properties collection should not be null.", props);
        if (versionProperties != null)
        {
            // TODO sort this out - need to check for the reserved properties too
            //assertEquals(versionProperties.size(), props.size());
            for (String key : versionProperties.keySet())
            {
                assertEquals(
                        versionProperties.get(key),
                        newVersion.getVersionProperty(key));
            }
        }
        
        // Check that the node reference is correct
        NodeRef nodeRef = newVersion.getFrozenStateNodeRef();
        assertNotNull(nodeRef);
        
        // Switch VersionStore depending on configured impl
        if (versionService.getVersionStoreReference().getIdentifier().equals(Version2Model.STORE_ID))
        {
        	// V2 version store (eg. workspace://version2Store)
            assertEquals(
                    Version2Model.STORE_ID,
                    nodeRef.getStoreRef().getIdentifier());
            assertEquals(
                    Version2Model.STORE_PROTOCOL,
                    nodeRef.getStoreRef().getProtocol());
            assertNotNull(nodeRef.getId());
        } 
        else if (versionService.getVersionStoreReference().getIdentifier().equals(VersionModel.STORE_ID))
        {
            // Deprecated V1 version store (eg. workspace://lightWeightVersionStore)
            assertEquals(
                    VersionModel.STORE_ID,
                    nodeRef.getStoreRef().getIdentifier());
            assertEquals(
                    VersionModel.STORE_PROTOCOL,
                    nodeRef.getStoreRef().getProtocol());
            assertNotNull(nodeRef.getId());
        }
    }
    
    protected void checkNewVersion(long beforeVersionTime, String expectedVersionLabel, Version newVersion, NodeRef versionableNode)
    {
        checkVersion(beforeVersionTime, expectedVersionLabel, newVersion, versionableNode);
        
        // TODO: How do we check the frozen attributes ??
        
        // Check the node ref for the current version
        String currentVersionLabel = (String)this.dbNodeService.getProperty(
                versionableNode,
                ContentModel.PROP_VERSION_LABEL);
        assertEquals(newVersion.getVersionLabel(), currentVersionLabel);
    }
    
    /**
     * Helper method to check the validity of the list of newly created versions.
     * 
     * @param beforeVersionTime      the time before the versions where created
     * @param versions               the collection of version objects
     */
    private void checkVersionCollection(String expectedVersionLabel, long beforeVersionTime, Collection<Version> versions)
    {
        for (Version version : versions)
        {
            // Get the frozen id from the version
            String frozenNodeId = null;
            
            // Switch VersionStore depending on configured impl
            if (versionService.getVersionStoreReference().getIdentifier().equals(Version2Model.STORE_ID))
            {
                // V2 version store (eg. workspace://version2Store)
                frozenNodeId = ((NodeRef)version.getVersionProperty(Version2Model.PROP_FROZEN_NODE_REF)).getId();
            } 
            else if (versionService.getVersionStoreReference().getIdentifier().equals(VersionModel.STORE_ID))
            {
                // Deprecated V1 version store (eg. workspace://lightWeightVersionStore)
                frozenNodeId = (String)version.getVersionProperty(VersionModel.PROP_FROZEN_NODE_ID);
            }
            
            assertNotNull("Unable to retrieve the frozen node id from the created version.", frozenNodeId);
            
            // Get the original node ref (based on the forzen node)
            NodeRef origionaNodeRef = this.versionableNodes.get(frozenNodeId);
            assertNotNull("The versionable node ref that relates to the frozen node id can not be found.", origionaNodeRef);
            
            // Check the new version
            checkNewVersion(beforeVersionTime, expectedVersionLabel, version, origionaNodeRef);
        }
    }
}
