/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.test.integration.version;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionService;
import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionServiceImpl;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;

/**
 * Declare as record version integration tests
 * 
 * @author Roy Wetherall
 * @since 2.3
 */
public class DeclareAsRecordVersionTest extends RecordableVersionsBaseTest
{
    /** recordable version service */
    private RecordableVersionService recordableVersionService;

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#initServices()
     */
    @Override
    protected void initServices()
    {
        super.initServices();
        recordableVersionService = (RecordableVersionService) applicationContext.getBean("RecordableVersionService");
    }

    /**
     * Given versionable content with a non-recorded latest version When I declare a version record Then the latest
     * version is recorded and a record is created
     */
    public void testDeclareLatestVersionAsRecord()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(dmCollaborator)
        {
            private NodeRef versionRecord;
            private Map<String, Serializable> versionProperties;

            public void given() throws Exception
            {
                // setup version properties
                versionProperties = new HashMap<String, Serializable>(4);
                versionProperties.put(Version.PROP_DESCRIPTION, DESCRIPTION);
                versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);

                // create version
                versionService.createVersion(dmDocument, versionProperties);

                // assert that the latest version is not recorded
                assertFalse(recordableVersionService.isCurrentVersionRecorded(dmDocument));
            }

            public void when()
            {
                // create version record from latest version
                versionRecord = recordableVersionService.createRecordFromLatestVersion(filePlan, dmDocument);
            }

            public void then()
            {
                // check the created record
                assertNotNull(versionRecord);
                assertTrue(recordService.isRecord(versionRecord));

                // assert the current version is recorded
                assertTrue(recordableVersionService.isCurrentVersionRecorded(dmDocument));

                // check the recorded version
                checkRecordedVersion(dmDocument, DESCRIPTION, "0.1");
            }
        });
    }

    /**
     * Given versionable content with a recorded latest version 
     * When I declare a version record
     * Then nothing happens since the latest version is already recorded And a warning is logged
     * 
     */
    public void testDeclareLatestVersionAsRecordButAlreadyRecorded()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(dmCollaborator)
        {
            private NodeRef versionRecord;
            private Map<String, Serializable> versionProperties;

            public void given() throws Exception
            {
                // setup version properties
                versionProperties = new HashMap<String, Serializable>(4);
                versionProperties.put(Version.PROP_DESCRIPTION, DESCRIPTION);
                versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);
                versionProperties.put(RecordableVersionServiceImpl.KEY_RECORDABLE_VERSION, true);
                versionProperties.put(RecordableVersionServiceImpl.KEY_FILE_PLAN, filePlan);

                // create version
                versionService.createVersion(dmDocument, versionProperties);

                // assert that the latest version is not recorded
                assertTrue(recordableVersionService.isCurrentVersionRecorded(dmDocument));
            }

            public void when()
            {
                // create version record from latest version
                versionRecord = recordableVersionService.createRecordFromLatestVersion(filePlan, dmDocument);
            }

            public void then()
            {
                // check that a record was not created
                assertNull(versionRecord);

                // assert the current version is recorded
                assertTrue(recordableVersionService.isCurrentVersionRecorded(dmDocument));

                // check the recorded version
                checkRecordedVersion(dmDocument, DESCRIPTION, "0.1");
            }
        });
    }

    /**
     * Given that a document is a specialized type 
     * When version is declared as a record 
     * Then the record is the same type as the source document
     * 
     * @see https://issues.alfresco.com/jira/browse/RM-2194
     */
    public void testSpecializedContentType()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(dmCollaborator)
        {
            private NodeRef customDocument;
            private NodeRef versionRecord;
            private Map<String, Serializable> versionProperties;

            public void given() throws Exception
            {
                // create content
                customDocument = fileFolderService.create(dmFolder, GUID.generate(), TYPE_CUSTOM_TYPE).getNodeRef();
                prepareContent(customDocument);

                // setup version properties
                versionProperties = new HashMap<String, Serializable>(2);
                versionProperties.put(Version.PROP_DESCRIPTION, DESCRIPTION);
                versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);

                // create version
                versionService.createVersion(customDocument, versionProperties);

                // assert that the latest version is not recorded
                assertFalse(recordableVersionService.isCurrentVersionRecorded(customDocument));
            }

            public void when()
            {
                // create version record from latest version
                versionRecord = recordableVersionService.createRecordFromLatestVersion(filePlan, customDocument);
            }

            public void then()
            {
                // check the created record
                assertNotNull(versionRecord);
                assertTrue(recordService.isRecord(versionRecord));

                // check the record type is correct
                assertEquals(TYPE_CUSTOM_TYPE, nodeService.getType(versionRecord));

                // assert the current version is recorded
                assertTrue(recordableVersionService.isCurrentVersionRecorded(customDocument));

                // check the recorded version
                checkRecordedVersion(customDocument, DESCRIPTION, "0.1");
            }
        });

    }

    /**
     * Given versionable content with a non recorded latest version, with auto-version flag true , and properties are changed for current version 
     * When I declare this version as a record
     * Then a new minor version is created
     * @see https://issues.alfresco.com/jira/browse/RM-2368
     */
    public void testCreateRecordFromLatestVersion()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(dmCollaborator)
        {
            private NodeRef myDocument;
            private NodeRef versionedRecord;
            private Map<String, Serializable> versionProperties;
            private Date createdDate;
            private Date frozenModifDate;
            private Date modificationDate;
            private String record_name = "initial_name";
            private String AUTO_VERSION_DESCRIPTION = "Auto Version on Record Creation";
            private boolean autoVersion = false;

            public void given() throws Exception
            {
                // create a document
                myDocument = fileFolderService.create(dmFolder, GUID.generate(), ContentModel.TYPE_CONTENT).getNodeRef();
                createdDate = (Date) nodeService.getProperty(myDocument, ContentModel.PROP_CREATED);
                modificationDate = (Date) nodeService.getProperty(myDocument, ContentModel.PROP_MODIFIED);
                assertTrue("Modified date must be after or on creation date", createdDate.getTime() == modificationDate.getTime());

                // Set initial set of properties
                Map<QName, Serializable> properties = new HashMap<QName, Serializable>(3);
                // Ensure default behaviour autoversion on change properties is set to false
                properties.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
                // Set initial name
                properties.put(ContentModel.PROP_NAME, "initial_name");
                nodeService.setProperties(myDocument, properties);
                nodeService.setProperty(myDocument, ContentModel.PROP_DESCRIPTION, DESCRIPTION);
                nodeService.addAspect(myDocument, ContentModel.ASPECT_OWNABLE, null);
                // make sure document is versionable
                nodeService.addAspect(myDocument, ContentModel.ASPECT_VERSIONABLE, null);
                // Change Type to a custom document
                nodeService.setType(myDocument, TYPE_CUSTOM_TYPE);

                Date modificationDate1 = (Date) nodeService.getProperty(myDocument, ContentModel.PROP_MODIFIED);
                assertTrue("Frozen modification date", modificationDate.getTime() == modificationDate1.getTime());
                // setup version properties
                versionProperties = new HashMap<String, Serializable>(2);
                versionProperties.put(Version.PROP_DESCRIPTION, DESCRIPTION);
                versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);

                // create initial version
                versionService.createVersion(myDocument, versionProperties);
                Version version = versionService.getCurrentVersion(myDocument);
                frozenModifDate = version.getFrozenModifiedDate();

                // get autoversion flag from cofiguratie
                autoVersion = recordableVersionService.isEnableAutoVersionOnRecordCreation();
            }

            public void when()
            {
                // check1
                assertTrue("Frozen modification date is the same with initial document ", modificationDate.getTime() == frozenModifDate.getTime());
                // current node is not dirty
                assertFalse(isCurrentVersionDirty(myDocument));

                if (autoVersion)
                {
                    // Apply a custom aspect
                    nodeService.addAspect(myDocument, ContentModel.ASPECT_TITLED, null);
                    // Update properties
                    nodeService.setProperty(myDocument, ContentModel.PROP_NAME, "updated_name");
                    nodeService.setProperty(myDocument, ContentModel.PROP_DESCRIPTION, DESCRIPTION);
                    // node should be modified
                    assertTrue(isCurrentVersionDirty(myDocument));
                }
                else
                {
                    assertFalse(isCurrentVersionDirty(myDocument));
                }

                assertFalse(recordableVersionService.isCurrentVersionRecorded(myDocument));
                // test RM-2368
                versionedRecord = recordableVersionService.createRecordFromLatestVersion(filePlan, myDocument);

            }

            public void then()
            {
                // Properties updated / flag as modified
                // check the created record
                assertNotNull(versionedRecord);
                assertTrue(recordService.isRecord(versionedRecord));

                // check the record type is correct
                assertEquals(TYPE_CUSTOM_TYPE, nodeService.getType(versionedRecord));

                // assert the current version is recorded
                assertTrue(recordableVersionService.isCurrentVersionRecorded(myDocument));

                // get name of record
                record_name = (String) nodeService.getProperty(versionedRecord, ContentModel.PROP_NAME);

                if (autoVersion)
                {
                    // new version is create, current node was modified
                    assertTrue("Name was updated:", record_name.contains("updated_name"));
                    // check record
                    checkRecordedVersion(myDocument, AUTO_VERSION_DESCRIPTION, "1.1");
                }
                else
                {
                    // record is created based on existing frozen, which does not contain any modification of node
                    assertTrue("Name is not modified: ", record_name.contains("initial_name"));
                    checkRecordedVersion(myDocument, DESCRIPTION, "1.0");
                }

            }

            public boolean isCurrentVersionDirty(NodeRef nodeRef)
            {
                if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE) == true)
                {
                    // get the latest version
                    Version currentVersion = versionService.getCurrentVersion(nodeRef);
                    Date modificationDate = (Date) nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED);
                    if (currentVersion != null)
                    {
                        // grab the frozen state
                        NodeRef currentFrozenState = currentVersion.getFrozenStateNodeRef();
                        Date frozenModificationDate = (Date) nodeService.getProperty(currentFrozenState, ContentModel.PROP_MODIFIED);
                        if (modificationDate.getTime() > frozenModificationDate.getTime()) { return true; }
                    }
                    else
                    {
                        return true;
                    }
                }
                return false;
            }

        });

    }

}
