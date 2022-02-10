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
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.impl.CompleteEventAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.CutOffAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.DestroyAction;
import org.alfresco.module.org_alfresco_module_rm.relationship.Relationship;
import org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipService;
import org.alfresco.module.org_alfresco_module_rm.test.util.CommonRMTestUtils;
import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionModel;
import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionPolicy;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;

/**
 * Recordable version history integration tests.
 *
 * @author Roy Wetherall
 * @since 2.3.1
 */
public class DeleteRecordVersionTest extends RecordableVersionsBaseTest
{
    /**
     *  Given that a document is created
     *  And the initial version is record
     *  When I delete the version record
     *  Then the version is deleted
     *  And the version history is not deleted
     *
     *  @see https://issues.alfresco.com/jira/browse/RM-2562
     */
    public void testDeleteFirstRecordedVersion()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef myDocument;

            public void given() throws Exception
            {
                // create a document
                myDocument = fileFolderService.create(dmFolder, GUID.generate(), ContentModel.TYPE_CONTENT).getNodeRef();

                // make versionable
                Map<QName, Serializable> props = new HashMap<>(2);
                props.put(RecordableVersionModel.PROP_RECORDABLE_VERSION_POLICY, RecordableVersionPolicy.ALL);
                props.put(RecordableVersionModel.PROP_FILE_PLAN, filePlan);
                nodeService.addAspect(myDocument, RecordableVersionModel.ASPECT_VERSIONABLE, props);
                nodeService.addAspect(myDocument, ContentModel.ASPECT_VERSIONABLE, null);
            }

            public void when()
            {
                // check the initial version label
                assertEquals("1.0", nodeService.getProperty(myDocument, ContentModel.PROP_VERSION_LABEL));

                // check that the version history contains a single version that is recorded
                VersionHistory versionHistory = versionService.getVersionHistory(myDocument);
                assertNotNull(versionHistory);
                assertEquals(1, versionHistory.getAllVersions().size());

                // check the recorded version is not marked as destroyed
                Version head = versionHistory.getHeadVersion();
                assertNotNull(head);
                assertFalse(recordableVersionService.isRecordedVersionDestroyed(head));

                // check the version record
                NodeRef record = recordableVersionService.getVersionRecord(head);
                assertTrue(recordService.isRecord(record));

                // record should not have a version history because it is immutable
                assertFalse(nodeService.hasAspect(record, ContentModel.ASPECT_VERSIONABLE));
                VersionHistory recordVersionHistory = versionService.getVersionHistory(record);
                assertNull(recordVersionHistory);

                // destroy record
                nodeService.deleteNode(record);
            }

            public void then()
            {
                // document is still versionable
                assertTrue(nodeService.hasAspect(myDocument, ContentModel.ASPECT_VERSIONABLE));

                // check the initial version label
                assertEquals("1.0", nodeService.getProperty(myDocument, ContentModel.PROP_VERSION_LABEL));

                // still has a version history, but the version is marked as destroyed
                VersionHistory versionHistory = versionService.getVersionHistory(myDocument);
                assertNotNull(versionHistory);
                assertEquals(1, versionHistory.getAllVersions().size());

                // check the recorded version is marked as destroyed and the record version is not longer available
                Version version = versionHistory.getHeadVersion();
                assertNotNull(version);
                assertTrue(recordableVersionService.isRecordedVersionDestroyed(version));
                assertNull(recordableVersionService.getVersionRecord(version));
            }
        });
    }

    /**
     *  Given that a document is created
     *  And the initial version is record
     *  And the associated version record is deleted
     *  When a new version is created
     *  Then a new associated version record is created
     *  And the version is 1.1 (not 1.0 since this was deleted, but the version history maintained)
     *
     *  @see https://issues.alfresco.com/jira/browse/RM-2562
     */
    public void testDeleteFirstRecordedVersionAndCreateNewVersion()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef myDocument;

            public void given() throws Exception
            {
                // create a document
                myDocument = fileFolderService.create(dmFolder, GUID.generate(), ContentModel.TYPE_CONTENT).getNodeRef();
                ContentWriter writer = fileFolderService.getWriter(myDocument);
                writer.setEncoding("UTF-8");
                writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                writer.putContent(GUID.generate());

                // make versionable
                Map<QName, Serializable> props = new HashMap<>(2);
                props.put(RecordableVersionModel.PROP_RECORDABLE_VERSION_POLICY, RecordableVersionPolicy.ALL);
                props.put(RecordableVersionModel.PROP_FILE_PLAN, filePlan);
                nodeService.addAspect(myDocument, RecordableVersionModel.ASPECT_VERSIONABLE, props);
                nodeService.addAspect(myDocument, ContentModel.ASPECT_VERSIONABLE, null);
            }

            public void when()
            {
                // get the created version record
                VersionHistory versionHistory = versionService.getVersionHistory(myDocument);
                Version head = versionHistory.getHeadVersion();
                NodeRef record = recordableVersionService.getVersionRecord(head);

                // destroy record
                nodeService.deleteNode(record);

                // update the content to create a new version (and version record)
                ContentWriter writer = fileFolderService.getWriter(myDocument);
                writer.putContent(GUID.generate());
            }

            public void then()
            {
                // document is still versionable
                assertTrue(nodeService.hasAspect(myDocument, ContentModel.ASPECT_VERSIONABLE));

                // check the version number has been incremented
                assertEquals("1.1", nodeService.getProperty(myDocument, ContentModel.PROP_VERSION_LABEL));

                // still has a version history, with 2 enties
                VersionHistory versionHistory = versionService.getVersionHistory(myDocument);
                assertNotNull(versionHistory);
                assertEquals(2, versionHistory.getAllVersions().size());

                // latest version is current
                Version head = versionHistory.getHeadVersion();
                assertFalse(recordableVersionService.isRecordedVersionDestroyed(head));
                assertNotNull(recordableVersionService.getVersionRecord(head));

                // first version is destroyed
                Version destroyed = versionHistory.getPredecessor(head);
                assertTrue(recordableVersionService.isRecordedVersionDestroyed(destroyed));
                assertNull(recordableVersionService.getVersionRecord(destroyed));

                // get the version record for the current version
                NodeRef versionRecord = recordableVersionService.getVersionRecord(head);
                assertNotNull(versionRecord);
                assertTrue(nodeService.exists(versionRecord));

                Set<Relationship> from = relationshipService.getRelationshipsFrom(versionRecord);
                assertTrue(from.isEmpty());

                Set<Relationship> to = relationshipService.getRelationshipsTo(versionRecord);
                assertTrue(to.isEmpty());
            }
        });
    }

    /**
     * Given a chain of version records (1.0, 1.1, 1.2) which are all related
     * When I delete version record 1.0
     * Then 1.1 is the oldest version
     */
    public void testDeleteOldestVersion()
    {
        final NodeRef myDocument = createDocumentWithRecordVersions();

        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private VersionHistory versionHistory;

            public void given() throws Exception
            {
                // get version history
                versionHistory = versionService.getVersionHistory(myDocument);
            }

            public void when()
            {
                Version version10 = versionHistory.getVersion("1.0");
                NodeRef recordVersion10 = recordableVersionService.getVersionRecord(version10);

                // delete record version 1.0
                nodeService.deleteNode(recordVersion10);
            }

            public void then()
            {
                // check the deleted version
                Version version10 = versionHistory.getVersion("1.0");
                assertNotNull(version10);
                assertTrue(recordableVersionService.isRecordedVersionDestroyed(version10));
                NodeRef recordVersion10 = recordableVersionService.getVersionRecord(version10);
                assertNull(recordVersion10);

                // verify 1.2 setup as expected
                Version version12 = versionHistory.getHeadVersion();
                assertEquals("1.2", version12.getVersionLabel());
                NodeRef recordVersion12 = recordableVersionService.getVersionRecord(version12);
                assertNotNull(recordVersion12);

                assertTrue(relationshipService.getRelationshipsTo(recordVersion12, RelationshipService.RELATIONSHIP_VERSIONS).isEmpty());

                Set<Relationship> from12 = relationshipService.getRelationshipsFrom(recordVersion12, RelationshipService.RELATIONSHIP_VERSIONS);
                assertEquals(1, from12.size());

                // verify 1.1 setup as expected
                Version version11 = versionHistory.getPredecessor(version12);
                assertEquals("1.1", version11.getVersionLabel());
                NodeRef recordVersion11 = recordableVersionService.getVersionRecord(version11);
                assertNotNull(recordVersion11);

                Set<Relationship> to11 = relationshipService.getRelationshipsTo(recordVersion11, RelationshipService.RELATIONSHIP_VERSIONS);
                assertEquals(1, to11.size());
                assertEquals(recordVersion12, to11.iterator().next().getSource());

                assertTrue(relationshipService.getRelationshipsFrom(recordVersion11, RelationshipService.RELATIONSHIP_VERSIONS).isEmpty());
            }
        });
    }

    /**
     * Given a chain of version records (1.0, 1.1, 1.2) which are all related
     * When I delete version record 1.1
     * Then 1.2 now 'versions' 1.0
     */
    public void testDeleteMiddleVersion()
    {
        final NodeRef myDocument = createDocumentWithRecordVersions();

        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private VersionHistory versionHistory;

            public void given() throws Exception
            {
                // get version history
                versionHistory = versionService.getVersionHistory(myDocument);
            }

            public void when()
            {
                Version version11 = versionHistory.getVersion("1.1");
                NodeRef recordVersion11 = recordableVersionService.getVersionRecord(version11);

                // delete record version 1.1
                nodeService.deleteNode(recordVersion11);
            }

            public void then()
            {
                // check the deleted version
                Version version11 = versionHistory.getVersion("1.1");
                assertNotNull(version11);
                assertTrue(recordableVersionService.isRecordedVersionDestroyed(version11));
                NodeRef recordVersion11 = recordableVersionService.getVersionRecord(version11);
                assertNull(recordVersion11);

                // verify 1.2 setup as expected
                Version version12 = versionHistory.getHeadVersion();
                assertEquals("1.2", version12.getVersionLabel());
                NodeRef recordVersion12 = recordableVersionService.getVersionRecord(version12);
                assertNotNull(recordVersion12);

                assertTrue(relationshipService.getRelationshipsTo(recordVersion12, RelationshipService.RELATIONSHIP_VERSIONS).isEmpty());

                Set<Relationship> from12 = relationshipService.getRelationshipsFrom(recordVersion12, RelationshipService.RELATIONSHIP_VERSIONS);
                assertEquals(1, from12.size());

                // verify 1.0 setup as expected
                Version version10 = versionHistory.getVersion("1.0");
                assertEquals("1.0", version10.getVersionLabel());
                NodeRef recordVersion10 = recordableVersionService.getVersionRecord(version10);
                assertNotNull(recordVersion10);

                Set<Relationship> to10 = relationshipService.getRelationshipsTo(recordVersion10, RelationshipService.RELATIONSHIP_VERSIONS);
                assertEquals(1, to10.size());
                assertEquals(recordVersion12, to10.iterator().next().getSource());

                assertTrue(relationshipService.getRelationshipsFrom(recordVersion10, RelationshipService.RELATIONSHIP_VERSIONS).isEmpty());

            }
        });
    }

    /**
     * Given a chain of version records (1.0, 1.1, 1.2) which are all related
     * When I delete version record 1.2
     * Then 1.1 is the most recent version
     */
    public void testDeleteCurrentVersion()
    {
        final NodeRef myDocument = createDocumentWithRecordVersions();

        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private VersionHistory versionHistory;

            public void given() throws Exception
            {
                // get version history
                versionHistory = versionService.getVersionHistory(myDocument);
            }

            public void when()
            {
                Version version12 = versionHistory.getVersion("1.2");
                NodeRef recordVersion12 = recordableVersionService.getVersionRecord(version12);

                // delete record version 1.2
                nodeService.deleteNode(recordVersion12);
            }

            public void then()
            {
                // check 1.2
                Version version12 = versionHistory.getVersion("1.2");
                assertNotNull(version12);
                assertTrue(recordableVersionService.isRecordedVersionDestroyed(version12));
                assertNull(recordableVersionService.getVersionRecord(version12));

                // verify 1.1
                Version version11 = versionHistory.getVersion("1.1");
                assertNotNull(version11);
                NodeRef recordVersion11 = recordableVersionService.getVersionRecord(version11);
                assertNotNull(recordVersion11);

                assertTrue(relationshipService.getRelationshipsTo(recordVersion11, RelationshipService.RELATIONSHIP_VERSIONS).isEmpty());

                Set<Relationship> from11 = relationshipService.getRelationshipsFrom(recordVersion11, RelationshipService.RELATIONSHIP_VERSIONS);
                assertEquals(1, from11.size());

                // verify 1.0
                Version version10 = versionHistory.getVersion("1.0");
                assertNotNull(version10);
                NodeRef recordVersion10 = recordableVersionService.getVersionRecord(version10);
                assertNotNull(recordVersion10);

                Set<Relationship> to10 = relationshipService.getRelationshipsTo(recordVersion10, RelationshipService.RELATIONSHIP_VERSIONS);
                assertEquals(1, to10.size());
                assertEquals(recordVersion11, to10.iterator().next().getSource());

                assertTrue(relationshipService.getRelationshipsFrom(recordVersion10, RelationshipService.RELATIONSHIP_VERSIONS).isEmpty());
            }
        });
    }

    /**
     * Given that a version record
     * When the version record is destroyed whilst retaining the meta data
     * Then the version is marked as destroyed in the collab version history
     */
    public void testDestroyVersionRecordWithMetadata()
    {
        final NodeRef myDocument = createDocumentWithRecordVersions();

        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private VersionHistory versionHistory;
            private NodeRef recordVersion11;

            public void given() throws Exception
            {
                // create file plan structure
                NodeRef myCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());
                utils.createBasicDispositionSchedule(myCategory, GUID.generate(), GUID.generate(), true, true);

                NodeRef myRecordFolder = recordFolderService.createRecordFolder(myCategory, GUID.generate());

                // get version history
                versionHistory = versionService.getVersionHistory(myDocument);

                // file and complete all the version records into my record folder
                for (Version version : versionHistory.getAllVersions())
                {
                    NodeRef record = recordableVersionService.getVersionRecord(version);
                    fileFolderService.move(record, myRecordFolder, null);
                    utils.completeRecord(record);
                }
            }

            public void when()
            {
                Version version11 = versionHistory.getVersion("1.1");
                recordVersion11 = recordableVersionService.getVersionRecord(version11);

                Map<String, Serializable> params = new HashMap<>(1);
                params.put(CompleteEventAction.PARAM_EVENT_NAME, CommonRMTestUtils.DEFAULT_EVENT_NAME);
                rmActionService.executeRecordsManagementAction(recordVersion11, CompleteEventAction.NAME, params);

                rmActionService.executeRecordsManagementAction(recordVersion11, CutOffAction.NAME);

                rmActionService.executeRecordsManagementAction(recordVersion11, DestroyAction.NAME);
            }

            public void then()
            {
                // verify that the version history looks as expected
                VersionHistory versionHistory = versionService.getVersionHistory(myDocument);
                assertNotNull(versionHistory);
                Collection<Version> versions = versionHistory.getAllVersions();
                assertEquals(3, versions.size());

                // verify 1.2 setup as expected
                Version version12 = versionHistory.getHeadVersion();
                assertEquals("1.2", version12.getVersionLabel());
                assertFalse(recordableVersionService.isRecordedVersionDestroyed(version12));
                NodeRef recordVersion12 = recordableVersionService.getVersionRecord(version12);
                assertNotNull(recordVersion12);
                assertFalse(recordService.isMetadataStub(recordVersion12));

                assertTrue(relationshipService.getRelationshipsTo(recordVersion12, "versions").isEmpty());

                Set<Relationship> from12 = relationshipService.getRelationshipsFrom(recordVersion12, "versions");
                assertEquals(1, from12.size());

                // verify 1.1 setup as expected
                Version version11 = versionHistory.getPredecessor(version12);
                assertEquals("1.1", version11.getVersionLabel());
                assertTrue(recordableVersionService.isRecordedVersionDestroyed(version11));
                assertNotNull(recordVersion11);
                assertTrue(recordService.isMetadataStub(recordVersion11));

                Set<Relationship> to11 = relationshipService.getRelationshipsTo(recordVersion11, "versions");
                assertEquals(1, to11.size());
                assertEquals(recordVersion12, to11.iterator().next().getSource());

                Set<Relationship> from11 = relationshipService.getRelationshipsFrom(recordVersion11, "versions");
                assertEquals(1, from11.size());

                // verify 1.0 setup as expected
                Version version10 = versionHistory.getPredecessor(version11);
                assertEquals("1.0", version10.getVersionLabel());
                assertFalse(recordableVersionService.isRecordedVersionDestroyed(version10));
                NodeRef recordVersion10 = recordableVersionService.getVersionRecord(version10);
                assertNotNull(recordVersion10);
                assertFalse(recordService.isMetadataStub(recordVersion10));

                Set<Relationship> to10 = relationshipService.getRelationshipsTo(recordVersion10, "versions");
                assertEquals(1, to10.size());
                assertEquals(recordVersion11, to10.iterator().next().getSource());

                assertTrue(relationshipService.getRelationshipsFrom(recordVersion10, "versions").isEmpty());

            }
        });
    }
}
