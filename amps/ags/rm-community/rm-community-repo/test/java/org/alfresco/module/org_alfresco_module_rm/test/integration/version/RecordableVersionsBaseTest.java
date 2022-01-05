/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.test.integration.version;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.relationship.Relationship;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionModel;
import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionPolicy;
import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionService;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;

/**
 * Recordable versions base integration test
 * 
 * @author Roy Wetherall
 * @since 2.3
 */
public abstract class RecordableVersionsBaseTest extends BaseRMTestCase implements RecordableVersionModel
{
    protected static final QName QNAME_PUBLISHER = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "publisher");
    protected static final QName QNAME_SUBJECT = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "subject");
    
    protected static final String DESCRIPTION = "description";
    protected static final String PUBLISHER = "publisher";
    protected static final String SUBJECT = "subject";
    protected static final String OWNER = "GracieWetherall";
    
    protected static final String CONTENT = 
              "Simple + Smart.  A smarter way to build, a smarter way to deploy.  Its simple because we focus on the end "
              + "user and smart because we support more open standards than any other ECM platform, while delivering all "
              + "the value a traditional platform provides.";    
   
    protected RecordableVersionService recordableVersionService;
    
    @Override
    protected void initServices()
    {
        super.initServices();        
        recordableVersionService = (RecordableVersionService)applicationContext.getBean("RecordableVersionService");
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#isCollaborationSiteTest()
     */
    @Override
    protected boolean isCollaborationSiteTest()
    {
        return true;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#setupCollaborationSiteTestDataImpl()
     */
    @Override
    protected void setupCollaborationSiteTestDataImpl()
    {
        super.setupCollaborationSiteTestDataImpl();
        
        // create authentication for owner
        createPerson(OWNER);
        
        // prepare content
        prepareContent(dmDocument);
    }
    
    protected void prepareContent(NodeRef content)
    {
        // add titled aspect
        PropertyMap titledProperties = new PropertyMap(2);
        titledProperties.put(ContentModel.PROP_TITLE, "document title");
        titledProperties.put(ContentModel.PROP_DESCRIPTION, "document description");
        nodeService.addAspect(content, ContentModel.ASPECT_TITLED, titledProperties);
        
        // add ownable aspect
        PropertyMap ownableProperties = new PropertyMap(1);
        ownableProperties.put(ContentModel.PROP_OWNER, OWNER);
        nodeService.addAspect(content, ContentModel.ASPECT_OWNABLE, ownableProperties);
        
        // add Dublin core aspect
        PropertyMap dublinCoreProperties = new PropertyMap(2);
        dublinCoreProperties.put(QNAME_PUBLISHER, PUBLISHER);
        dublinCoreProperties.put(QNAME_SUBJECT, SUBJECT);
        nodeService.addAspect(content, ContentModel.ASPECT_DUBLINCORE, dublinCoreProperties);
        
        // add content
        ContentWriter writer = contentService.getWriter(content, ContentModel.PROP_CONTENT, true);
        writer.setEncoding("UTF-8");
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.putContent(CONTENT);
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#tearDownImpl()
     */
    @Override
    protected void tearDownImpl()
    {
        super.tearDownImpl();
        
        // remove owner 
        personService.deletePerson(OWNER);
    }
    
    /**
     * Helper to check that the current version is recorded
     */
    protected void checkRecordedVersion(NodeRef document, String description, String versionLabel)
    {
        // double check that the document is not a record
        assertFalse(recordService.isRecord(document));
        
        // store document state
        Map<QName, Serializable> beforeProperties = nodeService.getProperties(document);
        Set<QName> beforeAspects = nodeService.getAspects(document);
        
        // get the current version
        Version version = versionService.getCurrentVersion(document);
        
        // version has been created
        assertNotNull(version);
        
        // check the version properties
        assertEquals(description, version.getDescription());
        assertEquals(versionLabel, version.getVersionLabel());
        
        // get the frozen state
        NodeRef frozen = version.getFrozenStateNodeRef();

        // check the properties
        checkProperties(frozen, beforeProperties);
        
        // compare aspects
        checkAspects(frozen, beforeAspects);
        
        // record version node reference is available on version
        NodeRef record = recordableVersionService.getVersionRecord(version);
        assertNotNull(record);
        
        // check that the version record information has been added
        assertTrue(nodeService.hasAspect(record, ASPECT_VERSION_RECORD));
        assertEquals(versionLabel, nodeService.getProperty(record, RecordableVersionModel.PROP_VERSION_LABEL));
        assertEquals(description, nodeService.getProperty(record, RecordableVersionModel.PROP_VERSION_DESCRIPTION));
        
        // record version is an unfiled record
        assertTrue(recordService.isRecord(record));
        assertFalse(recordService.isFiled(record));

        // check that the created record does not have either of the versionable aspects
        assertFalse(nodeService.hasAspect(record, RecordableVersionModel.ASPECT_VERSIONABLE));
        
        // check the version history
        VersionHistory versionHistory = versionService.getVersionHistory(document);
        assertNotNull(versionHistory);
        Version headVersion = versionHistory.getHeadVersion();
        assertNotNull(headVersion);
    }
    
    /**
     * Helper method to check that the current version is not recorded
     */
    protected void checkNotRecordedAspect(NodeRef document, String description, String versionLabel)
    {
        // double check that the document is not a record
        assertFalse(recordService.isRecord(document));
        
        // get the current version
        Version version = versionService.getCurrentVersion(document);
        
        // version has been created
        assertNotNull(version);
        
        // check the version properties
        assertEquals(description, version.getDescription());
        assertEquals(versionLabel, version.getVersionLabel());
                        
        // record version node reference is available on version
        NodeRef record = recordableVersionService.getVersionRecord(version);
        assertNull(record);
        
        // check the version history
        VersionHistory versionHistory = versionService.getVersionHistory(document);
        assertNotNull(versionHistory);
        Version headVersion = versionHistory.getHeadVersion();
        assertNotNull(headVersion);
    }
    
    /**
     * Helper to check the properties of a recorded version
     */
    protected void checkProperties(NodeRef frozen, Map<QName, Serializable> beforeProperies)
    {
        Map<QName, Serializable> frozenProperties = nodeService.getProperties(frozen);
        Map<QName, Serializable> cloneFrozenProperties = new HashMap<>(frozenProperties);
        for (Map.Entry<QName, Serializable> entry : beforeProperies.entrySet())
        {
            QName beforePropertyName = entry.getKey();
            if (frozenProperties.containsKey(beforePropertyName))
            {
                Serializable frozenValue = frozenProperties.get(beforePropertyName);
                if(beforePropertyName.equals(ContentModel.PROP_CONTENT))
                {
                    assertTrue("Content property value should be different.",
                            entry.getValue() != frozenValue);
                }
                else
                {
                    assertEquals("Frozen property " + beforePropertyName.getLocalName() + " value is incorrect.",
                            entry.getValue(), frozenValue);
                }
                cloneFrozenProperties.remove(beforePropertyName);               
            }
            else if (!PROP_FILE_PLAN.equals(beforePropertyName) &&
                     !PROP_RECORDABLE_VERSION_POLICY.equals(beforePropertyName) &&
                     !ContentModel.PROP_AUTO_VERSION_PROPS.equals(beforePropertyName) &&
                     !ContentModel.PROP_AUTO_VERSION.equals(beforePropertyName) &&
                     !ContentModel.PROP_INITIAL_VERSION.equals(beforePropertyName) &&
                     !ContentModel.PROP_VERSION_TYPE.equals(beforePropertyName) &&
                     !ContentModel.PROP_VERSION_LABEL.equals(beforePropertyName))
            {
                fail("Property missing from frozen state .. " + beforePropertyName);
            }
        }
        
        // filter out missing properties with null values
        for (Map.Entry<QName, Serializable> entry : frozenProperties.entrySet())
        {
            if (entry.getValue() == null)
            {
                cloneFrozenProperties.remove(entry.getKey());
            }
        }
        
        // remove "owner" from cloneFrozenProperties
        cloneFrozenProperties.remove(ContentModel.PROP_OWNER);
        
        
        // frozen properties should be empty
        assertTrue("Properties in frozen state, but not in origional. " + cloneFrozenProperties.keySet(), cloneFrozenProperties.isEmpty());
    }
    
    /**
     * Helper to check the aspects of a recorded version
     */
    protected void checkAspects(NodeRef frozen, Set<QName> beforeAspects)
    {
        Set<QName> cloneBeforeAspects = new HashSet<>(beforeAspects);
        
        // compare origional and frozen aspects
        Set<QName> frozenAspects = nodeService.getAspects(frozen);
        cloneBeforeAspects.removeAll(frozenAspects);
        cloneBeforeAspects.remove(RecordableVersionModel.ASPECT_VERSIONABLE);
        cloneBeforeAspects.remove(ContentModel.ASPECT_VERSIONABLE);
        if (!cloneBeforeAspects.isEmpty())
        {
            fail("Aspects not present in frozen state. " + cloneBeforeAspects.toString());
        }
        
        frozenAspects.removeAll(beforeAspects);
        if (!frozenAspects.isEmpty())
        {
            fail("Aspects in the frozen state, but not in origional.  " + frozenAspects.toString());
        }         
    }
    
    /**
     * Creates a document with three versions (1.0, 1.1, 1.2) all of which
     * are recorded.
     * 
     * @return  NodeRef node reference
     */
    protected NodeRef createDocumentWithRecordVersions()
    {
        // create document and initial version (1.0)
        final NodeRef myDocument = doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run() throws Exception
            {
                // create a document
                NodeRef testDoc = fileFolderService.create(dmFolder, GUID.generate(), ContentModel.TYPE_CONTENT).getNodeRef();
                ContentWriter writer = fileFolderService.getWriter(testDoc);
                writer.setEncoding("UTF-8");
                writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                writer.putContent(GUID.generate());
                
                // make versionable
                Map<QName, Serializable> props = new HashMap<>(2);
                props.put(RecordableVersionModel.PROP_RECORDABLE_VERSION_POLICY, RecordableVersionPolicy.ALL);
                props.put(RecordableVersionModel.PROP_FILE_PLAN, filePlan);
                nodeService.addAspect(testDoc, RecordableVersionModel.ASPECT_VERSIONABLE, props);
                nodeService.addAspect(testDoc, ContentModel.ASPECT_VERSIONABLE, null);      
                
                return testDoc;
            }
        });
        
        // create 1.1
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                // update content
                ContentWriter writer = fileFolderService.getWriter(myDocument);
                writer.putContent(GUID.generate());
                
                return null;
            }
        });
        
        // create 1.2
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                // update content
                ContentWriter writer = fileFolderService.getWriter(myDocument);
                writer.putContent(GUID.generate());
                
                return null;
            }
        });
        
        // we do these checks to ensure that the test data is in the correct state before we
        // start to manipulate the versions and execute tests
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                // verify that the version history looks as expected
                VersionHistory versionHistory = versionService.getVersionHistory(myDocument);
                assertNotNull(versionHistory);                
                Collection<Version> versions = versionHistory.getAllVersions();
                assertEquals(3, versions.size());
                
                // verify 1.2 setup as expected
                Version version12 = versionHistory.getHeadVersion();
                assertEquals("1.2", version12.getVersionLabel());
                NodeRef recordVersion12 = recordableVersionService.getVersionRecord(version12);
                assertNotNull(recordVersion12);
                
                assertTrue(relationshipService.getRelationshipsTo(recordVersion12, "versions").isEmpty());
                
                Set<Relationship> from12 = relationshipService.getRelationshipsFrom(recordVersion12, "versions");
                assertEquals(1, from12.size());
                
                // verify 1.1 setup as expected
                Version version11 = versionHistory.getPredecessor(version12);
                assertEquals("1.1", version11.getVersionLabel());
                NodeRef recordVersion11 = recordableVersionService.getVersionRecord(version11);
                assertNotNull(recordVersion11);
                
                Set<Relationship> to11 = relationshipService.getRelationshipsTo(recordVersion11, "versions");
                assertEquals(1, to11.size());
                assertEquals(recordVersion12, to11.iterator().next().getSource());
                
                Set<Relationship> from11 = relationshipService.getRelationshipsFrom(recordVersion11, "versions");
                assertEquals(1, from11.size());
                
                // verify 1.0 setup as expected
                Version version10 = versionHistory.getPredecessor(version11);
                assertEquals("1.0", version10.getVersionLabel());
                NodeRef recordVersion10 = recordableVersionService.getVersionRecord(version10);
                assertNotNull(recordVersion10);
                
                Set<Relationship> to10 = relationshipService.getRelationshipsTo(recordVersion10, "versions");
                assertEquals(1, to10.size());
                assertEquals(recordVersion11, to10.iterator().next().getSource());
                
                assertTrue(relationshipService.getRelationshipsFrom(recordVersion10, "versions").isEmpty());
                
                return null;
            }
        });
                
        return myDocument;
    }

}
