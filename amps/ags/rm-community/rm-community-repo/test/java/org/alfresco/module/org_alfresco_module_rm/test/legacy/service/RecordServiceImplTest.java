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

package org.alfresco.module.org_alfresco_module_rm.test.legacy.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementPolicies;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementPolicies.BeforeRecordDeclaration;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementPolicies.BeforeRecordRejection;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementPolicies.OnRecordDeclaration;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementPolicies.OnRecordRejection;
import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.role.Role;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.BehaviourDefinition;
import org.alfresco.repo.policy.ClassBehaviourBinding;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;

/**
 * Records Service Implementation Test
 *
 * @author Roy Wetherall
 * @author Tuna Aksoy
 * @since 2.1
 */
public class RecordServiceImplTest extends BaseRMTestCase implements BeforeRecordDeclaration,
                                                                     OnRecordDeclaration,
                                                                     BeforeRecordRejection,
                                                                     OnRecordRejection
{
    /**
     * This is a user test
     *
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#isUserTest()
     */
    @Override
    protected boolean isUserTest()
    {
        return true;
    }

    /**
     * This is a record test
     *
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#isRecordTest()
     */
    @Override
    protected boolean isRecordTest()
    {
        return true;
    }

    /**
     * This is a collaboration site test
     */
    @Override
    protected boolean isCollaborationSiteTest()
    {
        return true;
    }

    /**
     * @see RecordService#getRecordMetaDataAspects()
     */
    public void testGetRecordMetaDataAspects() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                Set<QName> aspects = recordService.getRecordMetadataAspects(filePlan);
                assertNotNull(aspects);
                assertEquals(2, aspects.size());
                assertTrue(aspects.containsAll(getAspectList()));

                return null;
            }

            /**
             * Helper method for getting a list of record meta data aspects
             *
             * @return Record meta data aspects as list
             */
            private List<QName> getAspectList()
            {
                QName[] aspects = new QName[]
                {
                    ASPECT_RECORD_META_DATA
                };

                return Arrays.asList(aspects);
            }
        });
    }

    /**
     * @see RecordService#isRecord(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void testIsRecord() throws Exception
    {
        doTestInTransaction(new VoidTest()
        {
            @Override
            public void runImpl()
            {
                assertFalse(recordService.isRecord(filePlan));
                assertFalse(recordService.isRecord(rmContainer));
                assertFalse(recordService.isRecord(rmFolder));
                assertTrue(recordService.isRecord(recordOne));
                assertTrue(recordService.isRecord(recordDeclaredOne));
            }
        });
    }

    /**
     * @see RecordService#isDeclared(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void testIsDeclared() throws Exception
    {
        doTestInTransaction(new VoidTest()
        {
            @Override
            public void runImpl()
            {
                assertFalse(recordService.isRecord(filePlan));
                assertFalse(recordService.isRecord(rmContainer));
                assertFalse(recordService.isRecord(rmFolder));
                assertTrue(recordService.isRecord(recordOne));
                assertTrue(recordService.isRecord(recordDeclaredOne));
            }
        });
    }

    public void testUnfiled() throws Exception
    {
        doTestInTransaction(new VoidTest()
        {
            @Override
            public void runImpl()
            {
                assertFalse(recordService.isFiled(filePlan));
                assertFalse(recordService.isFiled(rmContainer));
                assertFalse(recordService.isFiled(rmFolder));
                assertTrue(recordService.isFiled(recordOne));
                assertTrue(recordService.isFiled(recordDeclaredOne));
            }
        });
    }

    public void testExtendedWriters() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertTrue(extendedSecurityService.getReaders(recordOne).isEmpty());
                assertTrue(extendedSecurityService.getWriters(recordOne).isEmpty());

                return null;
            }
        }, dmCollaborator);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(recordOne, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(recordOne, RMPermissionModel.FILING));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(filePlan, RMPermissionModel.VIEW_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(filePlan, RMPermissionModel.EDIT_NON_RECORD_METADATA));

                return null;
            }
        }, dmCollaborator);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                Set<String> writers = new HashSet<>(1);
                writers.add(dmCollaborator);
                extendedSecurityService.set(recordOne, null, writers);

                assertTrue(extendedSecurityService.getReaders(recordOne).isEmpty());
                assertFalse(extendedSecurityService.getWriters(recordOne).isEmpty());

                return null;
            }
        });

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(recordOne, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(recordOne, RMPermissionModel.FILING));

                // ALLOWED, becuase users have been added to the in-place roles
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(filePlan, RMPermissionModel.VIEW_RECORDS));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(filePlan, RMPermissionModel.EDIT_NON_RECORD_METADATA));

                return null;
            }
        }, dmCollaborator);

    }

    /**
     * @see RecordService#createRecord(org.alfresco.service.cmr.repository.NodeRef,
     *      org.alfresco.service.cmr.repository.NodeRef)
     */
    public void testCreateRecord() throws Exception
    {
        // show that users without WRITE can not create a record from a document
        doTestInTransaction(new FailureTest(
                "Can not create a record from a document if you do not have WRITE permissions.",
                AccessDeniedException.class)
        {
            public void run() throws Exception
            {
                recordService.createRecord(filePlan, dmDocument);
            }
        }, dmConsumer);

        // create record from document
        doTestInTransaction(new Test<Void>()
        {
            private NodeRef originalLocation;

            @Override
            public Void run()
            {
                originalLocation = nodeService.getPrimaryParent(dmDocument).getParentRef();

                assertFalse(recordService.isRecord(dmDocument));
                assertFalse(extendedSecurityService.hasExtendedSecurity(dmDocument));

                checkPermissions(READ_RECORDS, AccessStatus.DENIED, // file plan
                        AccessStatus.DENIED, // unfiled container
                        AccessStatus.DENIED, // record category
                        AccessStatus.DENIED, // record folder
                        AccessStatus.DENIED); // doc/record

                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(filePlan,
                        RMPermissionModel.VIEW_RECORDS));

                checkPermissions(FILING, AccessStatus.DENIED, // file plan
                        AccessStatus.DENIED, // unfiled container
                        AccessStatus.DENIED, // record category
                        AccessStatus.DENIED, // record folder
                        AccessStatus.DENIED); // doc/record

                recordService.createRecord(filePlan, dmDocument);

                return null;
            }

            public void test(Void result)
            {
                checkPermissions(READ_RECORDS, 
                        AccessStatus.DENIED, // file plan
                        AccessStatus.DENIED, // unfiled container
                        AccessStatus.DENIED, // record category
                        AccessStatus.DENIED, // record folder
                        AccessStatus.ALLOWED); // doc/record

                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(filePlan,
                        RMPermissionModel.VIEW_RECORDS));

                checkPermissions(FILING, 
                        AccessStatus.DENIED, // file plan
                        AccessStatus.DENIED, // unfiled container
                        AccessStatus.DENIED, // record category
                        AccessStatus.DENIED, // record folder
                        AccessStatus.ALLOWED); // doc/record

                assertTrue(recordService.isRecord(dmDocument));
                assertTrue(extendedSecurityService.hasExtendedSecurity(dmDocument));
                assertFalse(recordService.isFiled(dmDocument));

                // show that the record has meta-data about it's original
                // location
                assertTrue(nodeService.hasAspect(dmDocument, ASPECT_RECORD_ORIGINATING_DETAILS));
                assertEquals(originalLocation, nodeService.getProperty(dmDocument, PROP_RECORD_ORIGINATING_LOCATION));
                assertFalse(originalLocation == nodeService.getPrimaryParent(dmDocument).getParentRef());

                // show that the record is linked to it's original location
                assertEquals(2, nodeService.getParentAssocs(dmDocument).size());

                // ****
                // Capability Tests
                // ****

                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(filePlan,
                        RMPermissionModel.VIEW_RECORDS));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(filePlan,
                        RMPermissionModel.EDIT_NON_RECORD_METADATA));

                Capability filling = capabilityService.getCapability("FileRecords");
                assertEquals(AccessStatus.DENIED, filling.hasPermission(dmDocument));

                Capability editRecordMetadata = capabilityService.getCapability("EditNonRecordMetadata");
                assertEquals(AccessStatus.ALLOWED, editRecordMetadata.hasPermission(dmDocument));

                Capability updateProperties = capabilityService.getCapability("UpdateProperties");
                assertEquals(AccessStatus.ALLOWED, updateProperties.hasPermission(dmDocument));
            }
        }, dmCollaborator);

        // check the consumer's permissions are correct for the newly created
        // document
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                checkPermissions(READ_RECORDS, AccessStatus.DENIED, // file plan
                        AccessStatus.DENIED, // unfiled container
                        AccessStatus.DENIED, // record category
                        AccessStatus.DENIED, // record folder
                        AccessStatus.ALLOWED); // doc/record

                checkPermissions(FILING, AccessStatus.DENIED, // file plan
                        AccessStatus.DENIED, // unfiled container
                        AccessStatus.DENIED, // record category
                        AccessStatus.DENIED, // record folder
                        AccessStatus.DENIED); // doc/record

                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(filePlan,
                        RMPermissionModel.VIEW_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(filePlan,
                        RMPermissionModel.EDIT_NON_RECORD_METADATA));

                Capability filling = capabilityService.getCapability("FileRecords");
                assertEquals(AccessStatus.DENIED, filling.hasPermission(dmDocument));

                Capability editRecordMetadata = capabilityService.getCapability("EditNonRecordMetadata");
                assertEquals(AccessStatus.DENIED, editRecordMetadata.hasPermission(dmDocument));

                Capability updateProperties = capabilityService.getCapability("UpdateProperties");
                assertEquals(AccessStatus.DENIED, updateProperties.hasPermission(dmDocument));

                return null;
            }
        }, dmConsumer);
    }

    public void testCreateRecordNoLink() throws Exception
    {
        // show that users without WRITE can not create a record from a document
        doTestInTransaction(new FailureTest(
                "Can not create a record from a document if you do not have WRITE permissions.",
                AccessDeniedException.class)
        {
            public void run() throws Exception
            {
                recordService.createRecord(filePlan, dmDocument, false);
            }
        }, dmConsumer);

        // create record from document
        final NodeRef originalLocation = doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {
                NodeRef originalLocation = nodeService.getPrimaryParent(dmDocument).getParentRef();

                //assertFalse(recordService.isRecord(dmDocument));
                //assertFalse(extendedSecurityService.hasExtendedSecurity(dmDocument));

                checkPermissions(READ_RECORDS, AccessStatus.DENIED, // file plan
                        AccessStatus.DENIED, // unfiled container
                        AccessStatus.DENIED, // record category
                        AccessStatus.DENIED, // record folder
                        AccessStatus.DENIED); // doc/record

                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(filePlan,
                        RMPermissionModel.VIEW_RECORDS));

                checkPermissions(FILING, AccessStatus.DENIED, // file plan
                        AccessStatus.DENIED, // unfiled container
                        AccessStatus.DENIED, // record category
                        AccessStatus.DENIED, // record folder
                        AccessStatus.DENIED); // doc/record

                recordService.createRecord(filePlan, dmDocument, false);

                checkPermissions(READ_RECORDS, AccessStatus.DENIED, // file plan
                        AccessStatus.DENIED, // unfiled container
                        AccessStatus.DENIED, // record category
                        AccessStatus.DENIED, // record folder
                        AccessStatus.DENIED); // doc/record

                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(filePlan,
                        RMPermissionModel.VIEW_RECORDS));

                checkPermissions(FILING, AccessStatus.DENIED, // file plan
                        AccessStatus.DENIED, // unfiled container
                        AccessStatus.DENIED, // record category
                        AccessStatus.DENIED, // record folder
                        AccessStatus.DENIED); // doc/record

                return originalLocation;
            }
        }, dmCollaborator);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertTrue(recordService.isRecord(dmDocument));
                assertFalse(extendedSecurityService.hasExtendedSecurity(dmDocument));
                assertFalse(recordService.isFiled(dmDocument));

                // show that the record has meta-data about it's original
                // location
                assertTrue(nodeService.hasAspect(dmDocument, ASPECT_RECORD_ORIGINATING_DETAILS));
                assertEquals(originalLocation, nodeService.getProperty(dmDocument, PROP_RECORD_ORIGINATING_LOCATION));
                assertFalse(originalLocation == nodeService.getPrimaryParent(dmDocument).getParentRef());

                // show that the record is linked to it's original location
                assertEquals(1, nodeService.getParentAssocs(dmDocument).size());

                return null;
            }
        }, ADMIN_USER);
    }

    public void testFileNewContent() throws Exception
    {
        doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {
                NodeRef record = fileFolderService.create(rmFolder, "test101.txt", TYPE_CONTENT).getNodeRef();

                ContentWriter writer = contentService.getWriter(record, PROP_CONTENT, true);
                writer.setEncoding("UTF-8");
                writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                writer.putContent("hello world this is some test content");

                return record;
            }

            @Override
            public void test(NodeRef record) throws Exception
            {
                assertTrue(recordService.isRecord(record));
                assertTrue(recordService.isFiled(record));

                assertNotNull(nodeService.getProperty(record, PROP_DATE_FILED));
            }
        });
    }

    public void xtestFileUnfiledrecord() throws Exception
    {
        doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run() throws Exception
            {
                recordService.createRecord(filePlan, dmDocument);

                assertTrue(recordService.isRecord(dmDocument));
                assertFalse(recordService.isFiled(dmDocument));

                assertNull(nodeService.getProperty(dmDocument, PROP_DATE_FILED));

                fileFolderService.move(dmDocument, rmFolder, "record.txt");

                return dmDocument;
            }

            @Override
            public void test(NodeRef record) throws Exception
            {
                assertTrue(recordService.isRecord(record));
                assertTrue(recordService.isFiled(record));

                assertNotNull(nodeService.getProperty(record, PROP_DATE_FILED));
            }
        });
    }

    public void testFileDirectlyFromCollab() throws Exception
    {
        
        doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run() throws Exception
            {
                assertNull(nodeService.getProperty(dmDocument, PROP_DATE_FILED));

                fileFolderService.move(dmDocument, rmFolder, "record.txt");

                return dmDocument;
            }

            @Override
            public void test(NodeRef record) throws Exception
            {
                assertTrue(recordService.isRecord(record));
                assertTrue(recordService.isFiled(record));

                assertNotNull(nodeService.getProperty(record, PROP_DATE_FILED));
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    private void checkPermissions(String permission, AccessStatus filePlanExpected, AccessStatus unfiledExpected,
            AccessStatus recordCatExpected, AccessStatus recordFolderExpected, AccessStatus recordExpected)
    {
        assertEquals(filePlanExpected, permissionService.hasPermission(filePlan, permission));
        assertEquals(unfiledExpected, permissionService.hasPermission(unfiledContainer, permission));
        assertEquals(recordCatExpected, permissionService.hasPermission(rmContainer, permission));
        assertEquals(recordFolderExpected, permissionService.hasPermission(rmFolder, permission));
        assertEquals(recordExpected, permissionService.hasPermission(dmDocument, permission));
    }

    private String createUserWithCapabilties(final String... capabiltyNames)
    {
        return doTestInTransaction(new Test<String>()
        {
            @Override
            public String run() throws Exception
            {
                Role role = utils.createRole(filePlan, GUID.generate(), capabiltyNames);

                String userName = GUID.generate();
                createPerson(userName);
                filePlanRoleService.assignRoleToAuthority(filePlan, role.getName(), userName);

                return userName;
            }
        }, AuthenticationUtil.getSystemUserName());

    }

    /**
     * Test {@link RecordService#isPropertyEditable(NodeRef, QName)}
     */
    public void testIsPropertyEditable() throws Exception
    {
        final String nonRecordMetadata = createUserWithCapabilties(
                RMPermissionModel.VIEW_RECORDS,
                RMPermissionModel.EDIT_NON_RECORD_METADATA);
        final String recordMetadata = createUserWithCapabilties(
                RMPermissionModel.VIEW_RECORDS,
                RMPermissionModel.EDIT_RECORD_METADATA);
        final String declaredRecordMetadata = createUserWithCapabilties(
                RMPermissionModel.VIEW_RECORDS,
                RMPermissionModel.EDIT_DECLARED_RECORD_METADATA);

        doTestInTransaction(new VoidTest()
        {
            @Override
            public void runImpl() throws Exception
            {
                filePlanPermissionService.setPermission(rmFolder, rmUserName, RMPermissionModel.FILING);
                filePlanPermissionService.setPermission(rmFolder, nonRecordMetadata, RMPermissionModel.FILING);
                filePlanPermissionService.setPermission(rmFolder, recordMetadata, RMPermissionModel.FILING);
                filePlanPermissionService.setPermission(rmFolder, declaredRecordMetadata, RMPermissionModel.FILING);
            }
        });

        // test rmadmin
        doTestInTransaction(new VoidTest()
        {
            @Override
            public void runImpl() throws Exception
            {
                assertTrue(recordService.isPropertyEditable(recordOne, RecordsManagementModel.PROP_LOCATION));
                assertTrue(recordService.isPropertyEditable(recordOne, PROP_DESCRIPTION));
                assertTrue(recordService.isPropertyEditable(recordDeclaredOne, RecordsManagementModel.PROP_LOCATION));
                assertFalse(recordService.isPropertyEditable(recordDeclaredOne, PROP_DESCRIPTION));
            }
        });

        // test normal user
        doTestInTransaction(new VoidTest()
        {
            @Override
            public void runImpl() throws Exception
            {
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(recordOne, RMPermissionModel.READ_RECORDS));

                assertFalse(recordService.isPropertyEditable(recordOne, RecordsManagementModel.PROP_LOCATION));
                assertFalse(recordService.isPropertyEditable(recordOne, PROP_DESCRIPTION));
                assertFalse(recordService.isPropertyEditable(recordDeclaredOne, RecordsManagementModel.PROP_LOCATION));
                assertFalse(recordService.isPropertyEditable(recordDeclaredOne, PROP_DESCRIPTION));
            }
        }, rmUserName);

        // test undeclared record with edit non-record metadata capability
        // test declared record with edit non-record metadata capability
        doTestInTransaction(new VoidTest()
        {
            @Override
            public void runImpl() throws Exception
            {
                assertFalse(recordService.isPropertyEditable(recordOne, RecordsManagementModel.PROP_LOCATION));
                assertTrue(recordService.isPropertyEditable(recordOne, PROP_DESCRIPTION));
                assertFalse(recordService.isPropertyEditable(recordDeclaredOne, RecordsManagementModel.PROP_LOCATION));
                assertFalse(recordService.isPropertyEditable(recordDeclaredOne, PROP_DESCRIPTION));
            }
        }, nonRecordMetadata);

        // test undeclared record with edit record metadata capability
        // test declared record with edit record metadata capability
        doTestInTransaction(new VoidTest()
        {
            @Override
            public void runImpl() throws Exception
            {
                assertTrue(recordService.isPropertyEditable(recordOne, RecordsManagementModel.PROP_LOCATION));
                assertFalse(recordService.isPropertyEditable(recordOne, PROP_DESCRIPTION));
                assertFalse(recordService.isPropertyEditable(recordDeclaredOne, RecordsManagementModel.PROP_LOCATION));
                assertFalse(recordService.isPropertyEditable(recordDeclaredOne, PROP_DESCRIPTION));
            }
        }, recordMetadata);

        // test undeclared record with edit declared record metadata capability
        // test declared record with edit declared record metadata capability
        doTestInTransaction(new VoidTest()
        {
            @Override
            public void runImpl() throws Exception
            {
                assertFalse(recordService.isPropertyEditable(recordOne, RecordsManagementModel.PROP_LOCATION));
                assertFalse(recordService.isPropertyEditable(recordOne, PROP_DESCRIPTION));
                assertTrue(recordService.isPropertyEditable(recordDeclaredOne, RecordsManagementModel.PROP_LOCATION));
                assertFalse(recordService.isPropertyEditable(recordDeclaredOne, PROP_DESCRIPTION));
            }
        }, declaredRecordMetadata);
    }

    public void testRecordPropertiesUpdate() throws Exception
    {
        final String nonRecordMetadata = createUserWithCapabilties(
                RMPermissionModel.VIEW_RECORDS,
                RMPermissionModel.EDIT_NON_RECORD_METADATA);
        final String recordMetadata = createUserWithCapabilties(
                RMPermissionModel.VIEW_RECORDS,
                RMPermissionModel.EDIT_RECORD_METADATA);
        final String declaredRecordMetadata = createUserWithCapabilties(
                RMPermissionModel.VIEW_RECORDS,
                RMPermissionModel.EDIT_DECLARED_RECORD_METADATA);

        doTestInTransaction(new VoidTest()
        {
            @Override
            public void runImpl() throws Exception
            {
                filePlanPermissionService.setPermission(rmFolder, rmUserName, RMPermissionModel.FILING);
                filePlanPermissionService.setPermission(rmFolder, nonRecordMetadata, RMPermissionModel.FILING);
                filePlanPermissionService.setPermission(rmFolder, recordMetadata, RMPermissionModel.FILING);
                filePlanPermissionService.setPermission(rmFolder, declaredRecordMetadata, RMPermissionModel.FILING);
            }
        });

        // test rmadmin
        canEditProperty(recordOne, ContentModel.PROP_DESCRIPTION, ADMIN_USER);
        canEditProperty(recordOne, RecordsManagementModel.PROP_LOCATION, ADMIN_USER);
        cantEditProperty(recordDeclaredOne, ContentModel.PROP_DESCRIPTION, ADMIN_USER);
        canEditProperty(recordDeclaredOne, RecordsManagementModel.PROP_LOCATION, ADMIN_USER);

        // test normal user
        cantEditProperty(recordOne, ContentModel.PROP_DESCRIPTION, rmUserName);
        cantEditProperty(recordOne, RecordsManagementModel.PROP_LOCATION, rmUserName);
        cantEditProperty(recordDeclaredOne, ContentModel.PROP_DESCRIPTION, rmUserName);
        cantEditProperty(recordDeclaredOne, RecordsManagementModel.PROP_LOCATION, rmUserName);

        // test undeclared record with edit non-record metadata capability
        canEditProperty(recordOne, ContentModel.PROP_DESCRIPTION, nonRecordMetadata);
        cantEditProperty(recordOne, RecordsManagementModel.PROP_LOCATION, nonRecordMetadata);
        // test declared record with edit non-record metadata capability
        cantEditProperty(recordDeclaredOne, ContentModel.PROP_DESCRIPTION, nonRecordMetadata);
        cantEditProperty(recordDeclaredOne, RecordsManagementModel.PROP_LOCATION, nonRecordMetadata);

        // test undeclared record with edit record metadata capability
        cantEditProperty(recordOne, ContentModel.PROP_DESCRIPTION, recordMetadata);
        canEditProperty(recordOne, RecordsManagementModel.PROP_LOCATION, recordMetadata);
        // test declared record with edit record metadata capability
        cantEditProperty(recordDeclaredOne, ContentModel.PROP_DESCRIPTION, recordMetadata);
        cantEditProperty(recordDeclaredOne, RecordsManagementModel.PROP_LOCATION, recordMetadata);

        // test undeclared record with edit declared record metadata capability
        cantEditProperty(recordOne, ContentModel.PROP_DESCRIPTION, declaredRecordMetadata);
        cantEditProperty(recordOne, RecordsManagementModel.PROP_LOCATION, declaredRecordMetadata);
        // test declared record with edit declared record metadata capability
        cantEditProperty(recordDeclaredOne, ContentModel.PROP_DESCRIPTION, declaredRecordMetadata);
        canEditProperty(recordDeclaredOne, RecordsManagementModel.PROP_LOCATION, declaredRecordMetadata);

    }

    private void cantEditProperty(final NodeRef nodeRef, final QName property, String user) throws Exception
    {
        boolean failure = false;
        try
        {
            doTestInTransaction(new VoidTest()
            {
                @Override
                public void runImpl() throws Exception
                {
                    nodeService.setProperty(nodeRef, property, GUID.generate());
                }

            }, user);
        }
        catch (Throwable exception)
        {
            // expected
            failure = true;
        }

        // assert fail not failure
        if (!failure)
        {
            fail("Property should not have been editable.");
        }
    }

    private void canEditProperty(final NodeRef nodeRef, final QName property, String user) throws Exception
    {
        doTestInTransaction(new VoidTest()
        {
            @Override
            public void runImpl() throws Exception
            {
                nodeService.setProperty(nodeRef, property, GUID.generate());
            }
        }, user);
    }

    /**
     * RM-4611 - integration test for policies for record declaration
     * @see RecordService#createRecord(org.alfresco.service.cmr.repository.NodeRef,
     *      org.alfresco.service.cmr.repository.NodeRef)
     */
    private boolean beforeRecordDeclaration = false;
    private boolean onRecordDeclaration = false;

    public void testPolicyNotificationForRecordDeclaration() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {

            @Override
            public Void run()
            {
                assertFalse(recordService.isRecord(dmDocument));

                BehaviourDefinition<ClassBehaviourBinding> beforeRecordDeclarationBehaviour = policyComponent.bindClassBehaviour(
                        RecordsManagementPolicies.BEFORE_RECORD_DECLARATION, ContentModel.TYPE_CONTENT,
                        new JavaBehaviour(RecordServiceImplTest.this, "beforeRecordDeclaration", NotificationFrequency.EVERY_EVENT));
                BehaviourDefinition<ClassBehaviourBinding> onRecordDeclarationBehaviour = policyComponent.bindClassBehaviour(
                        RecordsManagementPolicies.ON_RECORD_DECLARATION, ASPECT_RECORD,
                        new JavaBehaviour(RecordServiceImplTest.this, "onRecordDeclaration", NotificationFrequency.EVERY_EVENT));

                assertFalse(beforeRecordDeclaration);
                assertFalse(onRecordDeclaration);

                recordService.createRecord(filePlan, dmDocument);

                assertTrue(beforeRecordDeclaration);
                assertTrue(onRecordDeclaration);

                assertTrue(recordService.isRecord(dmDocument));

                policyComponent.removeClassDefinition(beforeRecordDeclarationBehaviour);
                policyComponent.removeClassDefinition(onRecordDeclarationBehaviour);

                return null;
            }
        }, dmCollaborator);
    }

    @Override
    public void beforeRecordDeclaration(NodeRef nodeRef)
    {
        assertEquals(nodeRef, dmDocument);
        beforeRecordDeclaration = true;
    }

    @Override
    public void onRecordDeclaration(NodeRef nodeRef)
    {
        assertEquals(nodeRef, dmDocument);
        onRecordDeclaration = true;
    }

    /**
     * RM-5180 - integration test for policies for record rejection
     * @see RecordService#rejectRecord(org.alfresco.service.cmr.repository.NodeRef)
     */
    private boolean beforeRecordRejection = false;
    private boolean onRecordRejection = false;

    @Override
    public void beforeRecordRejection(NodeRef nodeRef)
    {
        assertEquals(nodeRef, dmDocument);
        beforeRecordRejection = true;
    }

    @Override
    public void onRecordRejection(NodeRef nodeRef)
    {
        assertEquals(nodeRef, dmDocument);
        onRecordRejection = true;
    }

    public void testPolicyNotificationForRecordRejection() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {

            @Override
            public Void run()
            {
                assertFalse(recordService.isRecord(dmDocument));

                BehaviourDefinition<ClassBehaviourBinding> beforeRecordRejectionBehaviour = policyComponent.bindClassBehaviour(
                        RecordsManagementPolicies.BEFORE_RECORD_REJECTION, ContentModel.TYPE_CONTENT,
                        new JavaBehaviour(RecordServiceImplTest.this, "beforeRecordRejection", NotificationFrequency.EVERY_EVENT));
                BehaviourDefinition<ClassBehaviourBinding> onRecordRejectionBehaviour = policyComponent.bindClassBehaviour(
                        RecordsManagementPolicies.ON_RECORD_REJECTION, ContentModel.TYPE_CONTENT,
                        new JavaBehaviour(RecordServiceImplTest.this, "onRecordRejection", NotificationFrequency.EVERY_EVENT));

                recordService.createRecord(filePlan, dmDocument);

                assertFalse(beforeRecordRejection);
                assertFalse(onRecordRejection);
                assertTrue(recordService.isRecord(dmDocument));

                recordService.rejectRecord(dmDocument, "test reasons");

                assertTrue(beforeRecordRejection);
                assertTrue(onRecordRejection);
                assertFalse(recordService.isRecord(dmDocument));

                policyComponent.removeClassDefinition(beforeRecordRejectionBehaviour);
                policyComponent.removeClassDefinition(onRecordRejectionBehaviour);

                return null;
            }
        }, dmCollaborator);
    }
}
