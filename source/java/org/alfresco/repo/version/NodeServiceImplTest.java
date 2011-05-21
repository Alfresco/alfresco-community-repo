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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.TransactionalCache;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.debug.NodeStoreInspector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Roy Wetherall
 */
public class NodeServiceImplTest extends BaseVersionStoreTest 
{
    private static Log logger = LogFactory.getLog(NodeServiceImplTest.class);
    
    /**
     * version store node service
     */
    protected NodeService versionStoreNodeService = null;
    
    /**
     * Error message
     */
    private final static String MSG_ERR = 
        "This operation is not supported by a version store implementation of the node service.";
    
    /**
     * Dummy data used in failure tests
     */
    private NodeRef dummyNodeRef = null;
    private QName dummyQName = null;
    
    /**
     * Called during the transaction setup
     */
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();
        
        // Get the node service by name
        this.versionStoreNodeService = (NodeService)this.applicationContext.getBean("versionNodeService");
        
        // Create some dummy data used during the tests
        this.dummyNodeRef = new NodeRef(
                this.versionService.getVersionStoreReference(),
                "dummy");
        this.dummyQName = QName.createQName("{dummy}dummy");
    }
    
    /**
     * Test getType
     */
    public void testGetType()
    {
        // Create a new versionable node
        NodeRef versionableNode = createNewVersionableNode();
        
        // Create a new version
        Version version = createVersion(versionableNode, this.versionProperties);
        
        // Get the type from the versioned state
        QName versionedType = this.versionStoreNodeService.getType(version.getFrozenStateNodeRef());
        assertNotNull(versionedType);
        assertEquals(this.dbNodeService.getType(versionableNode), versionedType);
    }
    
    /**
     * Test getProperties
     */
    public void testGetProperties()
    {
        // Create a new versionable node
        NodeRef versionableNode = createNewVersionableNode();
        
        // Get a list of the nodes properties
        Map<QName, Serializable> origProps = this.dbNodeService.getProperties(versionableNode);
        
        // Create a new version
        Version version = createVersion(versionableNode, this.versionProperties);
        
        // Get the properties of the versioned state 
        Map<QName, Serializable> versionedProperties = this.versionStoreNodeService.getProperties(version.getFrozenStateNodeRef());
        
        if (logger.isDebugEnabled())
        {
            logger.debug("original ("+origProps.size()+"):  " + origProps.keySet());
            logger.debug("versioned ("+versionedProperties.size()+"): " + versionedProperties.keySet());
        }
        
        for (QName key : origProps.keySet())
        {
            assertTrue(versionedProperties.containsKey(key));
            assertEquals(""+key, origProps.get(key), versionedProperties.get(key));
        }
        
        // NOTE: cm:versionLabel is an expected additional property
        //assertEquals(origProps.size(), versionedProperties.size());
        
        // check version label
        assertEquals("first version label", "0.1", versionedProperties.get(ContentModel.PROP_VERSION_LABEL));
        
        // TODO do futher versioning and check by changing values
    }
    
    /**
     * Test getProperty
     */
    public void testGetProperty()
    {
        // Create a new versionable node
        NodeRef versionableNode = createNewVersionableNode();
        
        // Create a new version
        Version version = createVersion(versionableNode, this.versionProperties);
        
        // Check the property values can be retrieved
        Serializable value1 = this.versionStoreNodeService.getProperty(
                version.getFrozenStateNodeRef(),
                PROP_1);
        assertEquals(VALUE_1, value1);
        
        // Check the mlText property
        // TODO
        
        // Check the multi values property specifically
        Collection<String> multiValue = (Collection<String>)this.versionStoreNodeService.getProperty(version.getFrozenStateNodeRef(), MULTI_PROP);
        assertNotNull(multiValue);
        assertEquals(2, multiValue.size());
        String[] array = multiValue.toArray(new String[multiValue.size()]);
        assertEquals(MULTI_VALUE_1, array[0]);
        assertEquals(MULTI_VALUE_2, array[1]);
    }
    
    /**
     * Test getChildAssocs
     */
    public void testGetChildAssocs()
    {
        if (logger.isTraceEnabled())
        {
            // Let's have a look at the version store ..
            logger.trace(NodeStoreInspector.dumpNodeStore(
                    this.dbNodeService, 
                    this.versionService.getVersionStoreReference()) + "\n\n");
            logger.trace("");
        }
        
        // Create a new versionable node
        NodeRef versionableNode = createNewVersionableNode();
        Collection<ChildAssociationRef> originalChildren = this.dbNodeService.getChildAssocs(versionableNode);
        assertNotNull(originalChildren);
        
        // Store the original children in a map for easy navigation later
        HashMap<String, ChildAssociationRef> originalChildAssocRefs = new HashMap<String, ChildAssociationRef>();
        for (ChildAssociationRef ref : originalChildren)
        {
            originalChildAssocRefs.put(ref.getChildRef().getId(), ref);
        }
        
        // Create a new version
        Version version = createVersion(versionableNode, this.versionProperties);
        
        if (logger.isTraceEnabled())
        {
            // Let's have a look at the version store ..
            logger.trace(NodeStoreInspector.dumpNodeStore(
                    this.dbNodeService,
                    this.versionService.getVersionStoreReference()));
            logger.trace("");
        }
        
        // Get the children of the versioned node
        Collection<ChildAssociationRef> versionedChildren = this.versionStoreNodeService.getChildAssocs(version.getFrozenStateNodeRef());
        assertNotNull(versionedChildren);
        assertEquals(originalChildren.size(), versionedChildren.size());
        
        for (ChildAssociationRef versionedChildRef : versionedChildren)
        {
            ChildAssociationRef origChildAssocRef = originalChildAssocRefs.get(versionedChildRef.getChildRef().getId());
            assertNotNull(origChildAssocRef);
                        
            assertEquals(
                    origChildAssocRef.getChildRef(),
                    versionedChildRef.getChildRef());
            assertEquals(
                    origChildAssocRef.isPrimary(),
                    versionedChildRef.isPrimary());
            assertEquals(
                    origChildAssocRef.getNthSibling(),
                    versionedChildRef.getNthSibling());
        }
    }
    
    /**
     * Test getAssociationTargets
     */
    public void testGetAssociationTargets()
    {
        // Create a new versionable node
        NodeRef versionableNode = createNewVersionableNode();
        
        // Store the current details of the target associations
        List<AssociationRef> origAssocs = this.dbNodeService.getTargetAssocs(
                versionableNode,
                RegexQNamePattern.MATCH_ALL);
        
        // Create a new version
        Version version = createVersion(versionableNode, this.versionProperties);
        
        List<AssociationRef> assocs = this.versionStoreNodeService.getTargetAssocs(
                version.getFrozenStateNodeRef(), 
                RegexQNamePattern.MATCH_ALL);
        assertNotNull(assocs);
        assertEquals(origAssocs.size(), assocs.size());
    }
    
    /**
     * Test hasAspect
     */
    public void testHasAspect()
    {
        // Create a new versionable node
        NodeRef versionableNode = createNewVersionableNode();
        
        // Create a new version
        Version version = createVersion(versionableNode, this.versionProperties);
        
        boolean test1 = this.versionStoreNodeService.hasAspect(
                version.getFrozenStateNodeRef(), 
                ApplicationModel.ASPECT_UIFACETS);
        assertFalse(test1);
        
        boolean test2 = this.versionStoreNodeService.hasAspect(
                version.getFrozenStateNodeRef(),
                ContentModel.ASPECT_VERSIONABLE);
        assertTrue(test2);
    }

    /**
     * Test getAspects
     */
    public void testGetAspects() 
    {
        // Create a new versionable node
        NodeRef versionableNode = createNewVersionableNode();
        Set<QName> origAspects = this.dbNodeService.getAspects(versionableNode);
        
        // Create a new version
        Version version = createVersion(versionableNode, this.versionProperties);
        
        Set<QName> aspects = this.versionStoreNodeService.getAspects(version.getFrozenStateNodeRef());
        assertEquals(origAspects.size(), aspects.size());
        
        for (QName origAspect : origAspects)
        { 
            assertTrue(origAspect+"",aspects.contains(origAspect));
        }
    }
	
    /**
     * Test getParentAssocs
     */
    public void testGetParentAssocs()
    {
        // Create a new versionable node
        NodeRef versionableNode = createNewVersionableNode();
        
        // Create a new version
        Version version = createVersion(versionableNode, this.versionProperties);
        NodeRef nodeRef = version.getFrozenStateNodeRef();
        
        List<ChildAssociationRef> results = this.versionStoreNodeService.getParentAssocs(nodeRef);
        assertNotNull(results);
        assertEquals(1, results.size());
        ChildAssociationRef childAssoc = results.get(0);
        assertEquals(nodeRef, childAssoc.getChildRef());
        NodeRef versionStoreRoot = this.dbNodeService.getRootNode(this.versionService.getVersionStoreReference());
        assertEquals(versionStoreRoot, childAssoc.getParentRef());
    }
    
    /**
     * Test getPrimaryParent
     */
    public void testGetPrimaryParent()
    {
        // Create a new versionable node
        NodeRef versionableNode = createNewVersionableNode();
        
        // Create a new version
        Version version = createVersion(versionableNode, this.versionProperties);
        NodeRef nodeRef = version.getFrozenStateNodeRef();
        
        ChildAssociationRef childAssoc = this.versionStoreNodeService.getPrimaryParent(nodeRef);
        assertNotNull(childAssoc);
        assertEquals(nodeRef, childAssoc.getChildRef());
        NodeRef versionStoreRoot = this.dbNodeService.getRootNode(this.versionService.getVersionStoreReference());
        assertEquals(versionStoreRoot, childAssoc.getParentRef());        
    }
    
	/** ================================================
	 *  These test ensure that the following operations
	 *  are not supported as expected.
	 */
	
	/**
	 * Test createNode
	 */
	public void testCreateNode()
    {
		try
		{
			this.versionStoreNodeService.createNode(
					dummyNodeRef,
					null,
					dummyQName,
                    ContentModel.TYPE_CONTENT);
			fail("This operation is not supported.");
		}
		catch (UnsupportedOperationException exception)
		{
			if (exception.getMessage() != MSG_ERR)
			{
				fail("Unexpected exception raised during method excution: " + exception.getMessage());
			}
		}
    }
    
    /**
     * Test addAspect
     */
    public void testAddAspect()
    {
        try
        {
            this.versionStoreNodeService.addAspect(
                    dummyNodeRef,
                    TEST_ASPECT_QNAME,
                    null);
            fail("This operation is not supported.");
        }
        catch (UnsupportedOperationException exception)
        {
            if (exception.getMessage() != MSG_ERR)
            {
                fail("Unexpected exception raised during method excution: " + exception.getMessage());
            }
        }
    }
    
    /**
     * Test removeAspect
     */
    public void testRemoveAspect() 
    {
        try
        {
            this.versionStoreNodeService.removeAspect(
                    dummyNodeRef,
                    TEST_ASPECT_QNAME);
            fail("This operation is not supported.");
        }
        catch (UnsupportedOperationException exception)
        {
            if (exception.getMessage() != MSG_ERR)
            {
                fail("Unexpected exception raised during method excution: " + exception.getMessage());
            }
        }
    }
    
	/**
	 * Test delete node
	 */
    public void testDeleteNode()
    {
		try
		{
			this.versionStoreNodeService.deleteNode(this.dummyNodeRef);
			fail("This operation is not supported.");
		}
		catch (UnsupportedOperationException exception)
		{
			if (exception.getMessage() != MSG_ERR)
			{
				fail("Unexpected exception raised during method excution: " + exception.getMessage());
			}
		}
    }
    
	/**
	 * Test addChild
	 */
    public void testAddChild()
    {
		try
		{
			this.versionStoreNodeService.addChild(
					this.dummyNodeRef,
					this.dummyNodeRef,
                    this.dummyQName,
					this.dummyQName);
			fail("This operation is not supported.");
		}
		catch (UnsupportedOperationException exception)
		{
			if (exception.getMessage() != MSG_ERR)
			{
				fail("Unexpected exception raised during method excution: " + exception.getMessage());
			}
		}
    }
    
	/**
	 * Test removeChild
	 */
    public void testRemoveChild()
    {
		try
		{
			this.versionStoreNodeService.removeChild(
					this.dummyNodeRef, 
					this.dummyNodeRef);
			fail("This operation is not supported.");
		}
		catch (UnsupportedOperationException exception)
		{
			if (exception.getMessage() != MSG_ERR)
			{
				fail("Unexpected exception raised during method excution: " + exception.getMessage());
			}
		}	
    }
    
    /**
     * Test setProperties
     */
    public void testSetProperties()
    {
        try
        {
            this.versionStoreNodeService.setProperties(
                    this.dummyNodeRef,
                    new HashMap<QName, Serializable>());
            fail("This operation is not supported.");
        }
        catch (UnsupportedOperationException exception)
        {
            if (exception.getMessage() != MSG_ERR)
            {
                fail("Unexpected exception raised during method excution: " + exception.getMessage());
            }
        }
    }
    
    /**
     * Test setProperty
     */
    public void testSetProperty()
	{
        try
        {
            this.versionStoreNodeService.setProperty(
                    this.dummyNodeRef,
                    this.dummyQName,
                    "dummy");
            fail("This operation is not supported.");
        }
        catch (UnsupportedOperationException exception)
        {
            if (exception.getMessage() != MSG_ERR)
            {
                fail("Unexpected exception raised during method excution: " + exception.getMessage());
            }
        }
    }   
    
    /**
     * Test createAssociation
     */
    public void testCreateAssociation()
    {
        try
        {
            this.versionStoreNodeService.createAssociation(
                    this.dummyNodeRef,
                    this.dummyNodeRef,
                    this.dummyQName);
            fail("This operation is not supported.");
        }
        catch (UnsupportedOperationException exception)
        {
            if (exception.getMessage() != MSG_ERR)
            {
                fail("Unexpected exception raised during method excution: " + exception.getMessage());
            }
        }
    }
    
    /**
     * Test removeAssociation
     */
    public void testRemoveAssociation()
    {
        try
        {
            this.versionStoreNodeService.removeAssociation(
                    this.dummyNodeRef,
                    this.dummyNodeRef,
                    this.dummyQName);
            fail("This operation is not supported.");
        }
        catch (UnsupportedOperationException exception)
        {
            if (exception.getMessage() != MSG_ERR)
            {
                fail("Unexpected exception raised during method excution: " + exception.getMessage());
            }
        }
    }       
    
    /**
     * Test getAssociationSources
     */
    public void testGetAssociationSources()
    {
        try
        {
            this.versionStoreNodeService.getSourceAssocs(
                    this.dummyNodeRef,
                    this.dummyQName);
            fail("This operation is not supported.");
        }
        catch (UnsupportedOperationException exception)
        {
            if (exception.getMessage() != MSG_ERR)
            {
                fail("Unexpected exception raised during method excution: " + exception.getMessage());
            }
        }
    }
    
    /**
     * Test getPath
     */
    public void testGetPath()
    {
        Path path = this.versionStoreNodeService.getPath(this.dummyNodeRef);
    }
    
    /**
     * Test getPaths
     */
    public void testGetPaths()
    {
        List<Path> paths = this.versionStoreNodeService.getPaths(this.dummyNodeRef, false);
    }
    
    /**
     * Tests that we can store and retrieve unicode properties
     *  and association names.
     * If there's something wrong with how we're setting up the
     *  database or database connection WRT unicode, this is a
     *  test that'll hopefully break in testing and alert us!
     */
    public void testUnicodeNamesAndProperties()
    {
        // Get our cache objects
        List<TransactionalCache> cachesToClear = new ArrayList<TransactionalCache>(); 
        cachesToClear.add( (TransactionalCache)this.applicationContext.getBean("propertyValueCache") );
        cachesToClear.add( (TransactionalCache)this.applicationContext.getBean("node.nodesCache") );
        cachesToClear.add( (TransactionalCache)this.applicationContext.getBean("node.propertiesCache") );
        
        
        // First up, try with a simple English name+properties
        String engProp = "This is a property in English";
        QName engQName = QName.createQName("NameSpace", "In English");
        NodeRef engNode = nodeService.createNode(
                this.rootNodeRef, ContentModel.ASSOC_CONTAINS,
                engQName, ContentModel.TYPE_CONTENT
        ).getChildRef();
        nodeService.setProperty(engNode, ContentModel.PROP_NAME, engProp);
        
        // Check they exist and are correct
        assertEquals(engProp, nodeService.getProperty(engNode, ContentModel.PROP_NAME));
        assertEquals(1, nodeService.getChildAssocs(this.rootNodeRef, ContentModel.ASSOC_CONTAINS, engQName).size());
        assertEquals(engNode, nodeService.getChildByName(rootNodeRef, ContentModel.ASSOC_CONTAINS, engProp));
        
        
        // Now French
        String frProp = "C'est une propri\u00e9t\u00e9 en fran\u00e7ais"; // C'est une propriÃ©tÃ© en franÃ§ais
        QName frQName = QName.createQName("NameSpace", "En Fran\u00e7ais"); // En FranÃ§ais
        NodeRef frNode = nodeService.createNode(
                this.rootNodeRef, ContentModel.ASSOC_CONTAINS,
                frQName, ContentModel.TYPE_CONTENT
        ).getChildRef();
        nodeService.setProperty(frNode, ContentModel.PROP_NAME, frProp);
        
        assertEquals(frProp, nodeService.getProperty(frNode, ContentModel.PROP_NAME));
        assertEquals(1, nodeService.getChildAssocs(this.rootNodeRef, ContentModel.ASSOC_CONTAINS, frQName).size());
        assertEquals(frNode, nodeService.getChildByName(rootNodeRef, ContentModel.ASSOC_CONTAINS, frProp));
        
        
        // Zap the cache and re-check
        // (If the DB is broken but the cache works, then the above
        //  tests could pass even in the face of a problem)
        for(TransactionalCache tc : cachesToClear) tc.clear();
        assertEquals(frProp, nodeService.getProperty(frNode, ContentModel.PROP_NAME));
        assertEquals(1, nodeService.getChildAssocs(this.rootNodeRef, ContentModel.ASSOC_CONTAINS, frQName).size());
        assertEquals(frNode, nodeService.getChildByName(rootNodeRef, ContentModel.ASSOC_CONTAINS, frProp));
        
        
        // Next Spanish
        String esProp = "Esta es una propiedad en Espa\u00f1ol"; // Esta es una propiedad en EspaÃ±ol
        QName esQName = QName.createQName("NameSpace", "En Espa\u00f1ol"); // En EspaÃ±ol
        NodeRef esNode = nodeService.createNode(
                this.rootNodeRef, ContentModel.ASSOC_CONTAINS,
                esQName, ContentModel.TYPE_CONTENT
        ).getChildRef();
        nodeService.setProperty(esNode, ContentModel.PROP_NAME, esProp);
        
        assertEquals(esProp, nodeService.getProperty(esNode, ContentModel.PROP_NAME));
        assertEquals(1, nodeService.getChildAssocs(this.rootNodeRef, ContentModel.ASSOC_CONTAINS, esQName).size());
        assertEquals(esNode, nodeService.getChildByName(rootNodeRef, ContentModel.ASSOC_CONTAINS, esProp));
        
        
        // Zap cache and re-test the Spanish
        for(TransactionalCache tc : cachesToClear) tc.clear();
        assertEquals(esProp, nodeService.getProperty(esNode, ContentModel.PROP_NAME));
        assertEquals(1, nodeService.getChildAssocs(this.rootNodeRef, ContentModel.ASSOC_CONTAINS, esQName).size());
        assertEquals(esNode, nodeService.getChildByName(rootNodeRef, ContentModel.ASSOC_CONTAINS, esProp));

        
        // Finally Japanese
        String jpProp = "\u3092\u30af\u30ea\u30c3\u30af\u3057\u3066\u304f\u3060\u3055\u3044\u3002"; //  ã‚’ã‚¯ãƒªãƒƒã‚¯ã�—ã�¦ã��ã� ã�•ã�„ã€‚
        QName jpQName = QName.createQName("NameSpace", "\u3092\u30af\u30ea\u30c3\u30af\u3057\u3066\u304f"); //  ã‚’ã‚¯ãƒªãƒƒã‚¯ã�—ã�¦ã��
        NodeRef jpNode = nodeService.createNode(
                this.rootNodeRef, ContentModel.ASSOC_CONTAINS,
                jpQName, ContentModel.TYPE_CONTENT
        ).getChildRef();
        nodeService.setProperty(jpNode, ContentModel.PROP_NAME, jpProp);
        
        assertEquals(jpProp, nodeService.getProperty(jpNode, ContentModel.PROP_NAME));
        assertEquals(1, nodeService.getChildAssocs(this.rootNodeRef, ContentModel.ASSOC_CONTAINS, jpQName).size());
        assertEquals(jpNode, nodeService.getChildByName(rootNodeRef, ContentModel.ASSOC_CONTAINS, jpProp));
        
        // Zap the cache and check the Japanese
        for(TransactionalCache tc : cachesToClear) tc.clear();
        assertEquals(jpProp, nodeService.getProperty(jpNode, ContentModel.PROP_NAME));
        assertEquals(1, nodeService.getChildAssocs(this.rootNodeRef, ContentModel.ASSOC_CONTAINS, jpQName).size());
        assertEquals(jpNode, nodeService.getChildByName(rootNodeRef, ContentModel.ASSOC_CONTAINS, jpProp));
    }
}
