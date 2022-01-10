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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityType;
import org.springframework.extensions.webscripts.GUID;

/**
 * File plan permission service unit test
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public class FilePlanPermissionServiceImplTest extends BaseRMTestCase
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#isUserTest()
     */
    @Override
    protected boolean isUserTest()
    {
        return true;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#isRecordTest()
     */
    @Override
    protected boolean isRecordTest()
    {
        return true;
    }

    /**
     * Helper to create test user
     */
    private String createTestUser()
    {
        return doTestInTransaction(new Test<String>()
        {
            @Override
            public String run()
            {
                String userName = GUID.generate();
                createPerson(userName);
                filePlanRoleService.assignRoleToAuthority(filePlan, FilePlanRoleService.ROLE_USER, userName);
                return userName;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    /**
     * Helper to set permission
     */
    private void setPermission(final NodeRef nodeRef, final String userName, final String permission)
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                filePlanPermissionService.setPermission(nodeRef, userName, permission);
                return null;
            }
        });
    }

    /**
     * Helper to delete permission
     */
    private void deletePermission(final NodeRef nodeRef, final String userName, final String permission)
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                filePlanPermissionService.deletePermission(nodeRef, userName, permission);
                return null;
            }
        });
    }

    /**
     * Test set/delete permissions on file plan
     */
    public void testSetDeletePermissionFilePlan() throws Exception
    {
        String userName = createTestUser();

        assertPermissions(userName,
                AccessStatus.ALLOWED,       // fileplan read
                AccessStatus.DENIED,        // fileplan file
                AccessStatus.DENIED,        // category read
                AccessStatus.DENIED,        // category file
                AccessStatus.DENIED,        // record folder read
                AccessStatus.DENIED,        // record folder file
                AccessStatus.DENIED,        // record read
                AccessStatus.DENIED);       // record file

        setPermission(filePlan, userName, RMPermissionModel.FILING);

        assertPermissions(userName,
                AccessStatus.ALLOWED,       // fileplan read
                AccessStatus.ALLOWED,       // fileplan file
                AccessStatus.DENIED,        // category read
                AccessStatus.DENIED,        // category file
                AccessStatus.DENIED,        // record folder read
                AccessStatus.DENIED,        // record folder file
                AccessStatus.DENIED,        // record read
                AccessStatus.DENIED);       // record file

        deletePermission(filePlan, userName, RMPermissionModel.FILING);

        assertPermissions(userName,
                AccessStatus.ALLOWED,       // fileplan read
                AccessStatus.DENIED,        // fileplan file
                AccessStatus.DENIED,        // category read
                AccessStatus.DENIED,        // category file
                AccessStatus.DENIED,        // record folder read
                AccessStatus.DENIED,        // record folder file
                AccessStatus.DENIED,        // record read
                AccessStatus.DENIED);       // record file

        //what happens if we try and remove READ for a normal user on the file plan ???
        deletePermission(filePlan, userName, RMPermissionModel.READ_RECORDS);

        // nothing .. user still has read on file plan .. only removing the user from all roles will remove read on file plan
        assertPermissions(userName,
                AccessStatus.ALLOWED,       // fileplan read
                AccessStatus.DENIED,        // fileplan file
                AccessStatus.DENIED,        // category read
                AccessStatus.DENIED,        // category file
                AccessStatus.DENIED,        // record folder read
                AccessStatus.DENIED,        // record folder file
                AccessStatus.DENIED,        // record read
                AccessStatus.DENIED);       // record file
    }

    /**
     * Test set/delete permission on record categorty
     */
    public void testSetDeletePermissionRecordCategory() throws Exception
    {
        String userName = createTestUser();

        assertPermissions(userName,
                AccessStatus.ALLOWED,       // fileplan read
                AccessStatus.DENIED,        // fileplan file
                AccessStatus.DENIED,        // category read
                AccessStatus.DENIED,        // category file
                AccessStatus.DENIED,        // record folder read
                AccessStatus.DENIED,        // record folder file
                AccessStatus.DENIED,        // record read
                AccessStatus.DENIED);       // record file

        setPermission(rmContainer, userName, RMPermissionModel.FILING);

        assertPermissions(userName,
                AccessStatus.ALLOWED,       // fileplan read
                AccessStatus.DENIED,        // fileplan file
                AccessStatus.ALLOWED,       // category read
                AccessStatus.ALLOWED,       // category file
                AccessStatus.ALLOWED,       // record folder read
                AccessStatus.ALLOWED,       // record folder file
                AccessStatus.ALLOWED,       // record read
                AccessStatus.ALLOWED);      // record file

        deletePermission(rmContainer, userName, RMPermissionModel.FILING);

        assertPermissions(userName,
                AccessStatus.ALLOWED,       // fileplan read
                AccessStatus.DENIED,        // fileplan file
                AccessStatus.DENIED,        // category read
                AccessStatus.DENIED,        // category file
                AccessStatus.DENIED,        // record folder read
                AccessStatus.DENIED,        // record folder file
                AccessStatus.DENIED,        // record read
                AccessStatus.DENIED);       // record file
    }

    /**
     * Test set/delete permission on record folder
     */
    public void testSetDeletePermissionRecordFolder() throws Exception
    {
        String userName = createTestUser();

        assertPermissions(userName,
                AccessStatus.ALLOWED,       // fileplan read
                AccessStatus.DENIED,        // fileplan file
                AccessStatus.DENIED,        // category read
                AccessStatus.DENIED,        // category file
                AccessStatus.DENIED,        // record folder read
                AccessStatus.DENIED,        // record folder file
                AccessStatus.DENIED,        // record read
                AccessStatus.DENIED);       // record file

        setPermission(rmFolder, userName, RMPermissionModel.FILING);

        assertPermissions(userName,
                AccessStatus.ALLOWED,       // fileplan read
                AccessStatus.DENIED,        // fileplan file
                AccessStatus.DENIED,        // category read
                AccessStatus.DENIED,        // category file
                AccessStatus.ALLOWED,       // record folder read
                AccessStatus.ALLOWED,       // record folder file
                AccessStatus.ALLOWED,       // record read
                AccessStatus.ALLOWED);      // record file

        deletePermission(rmFolder, userName, RMPermissionModel.FILING);

        assertPermissions(userName,
                AccessStatus.ALLOWED,       // fileplan read
                AccessStatus.DENIED,        // fileplan file
                AccessStatus.DENIED,        // category read
                AccessStatus.DENIED,        // category file
                AccessStatus.DENIED,        // record folder read
                AccessStatus.DENIED,        // record folder file
                AccessStatus.DENIED,        // record read
                AccessStatus.DENIED);       // record file
    }

    /**
     * Test set/delete permission on record
     */
    public void testSetDeletePermissionRecord() throws Exception
    {
        String userName = createTestUser();

        assertPermissions(userName,
                AccessStatus.ALLOWED,       // fileplan read
                AccessStatus.DENIED,        // fileplan file
                AccessStatus.DENIED,        // category read
                AccessStatus.DENIED,        // category file
                AccessStatus.DENIED,        // record folder read
                AccessStatus.DENIED,        // record folder file
                AccessStatus.DENIED,        // record read
                AccessStatus.DENIED);       // record file

        setPermission(recordOne, userName, RMPermissionModel.FILING);

        assertPermissions(userName,
                AccessStatus.ALLOWED,       // fileplan read
                AccessStatus.DENIED,        // fileplan file
                AccessStatus.DENIED,        // category read
                AccessStatus.DENIED,        // category file
                AccessStatus.DENIED,        // record folder read
                AccessStatus.DENIED,        // record folder file
                AccessStatus.ALLOWED,       // record read
                AccessStatus.ALLOWED);      // record file

        deletePermission(recordOne, userName, RMPermissionModel.FILING);

        assertPermissions(userName,
                AccessStatus.ALLOWED,       // fileplan read
                AccessStatus.DENIED,        // fileplan file
                AccessStatus.DENIED,        // category read
                AccessStatus.DENIED,        // category file
                AccessStatus.DENIED,        // record folder read
                AccessStatus.DENIED,        // record folder file
                AccessStatus.DENIED,        // record read
                AccessStatus.DENIED);       // record file
    }

    public void testMoveRecord() throws Exception
    {
        String userOne = createTestUser();
        String userTwo = createTestUser();
        String userThree = createTestUser();

        final NodeRef otherFolder = doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {
                return recordFolderService.createRecordFolder(rmContainer, "otherFolder");
            }
        });

        assertPermissions(userOne,
                AccessStatus.ALLOWED,       // fileplan read
                AccessStatus.DENIED,        // fileplan file
                AccessStatus.DENIED,        // category read
                AccessStatus.DENIED,        // category file
                AccessStatus.DENIED,        // record folder read
                AccessStatus.DENIED,        // record folder file
                AccessStatus.DENIED,        // record read
                AccessStatus.DENIED);       // record file

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(otherFolder, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(otherFolder, RMPermissionModel.FILING));
                return null;
            }
        }, userOne);

        assertPermissions(userTwo,
                AccessStatus.ALLOWED,       // fileplan read
                AccessStatus.DENIED,        // fileplan file
                AccessStatus.DENIED,        // category read
                AccessStatus.DENIED,        // category file
                AccessStatus.DENIED,        // record folder read
                AccessStatus.DENIED,        // record folder file
                AccessStatus.DENIED,        // record read
                AccessStatus.DENIED);       // record file

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(otherFolder, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(otherFolder, RMPermissionModel.FILING));
                return null;
            }
        }, userTwo);

        assertPermissions(userThree,
                AccessStatus.ALLOWED,       // fileplan read
                AccessStatus.DENIED,        // fileplan file
                AccessStatus.DENIED,        // category read
                AccessStatus.DENIED,        // category file
                AccessStatus.DENIED,        // record folder read
                AccessStatus.DENIED,        // record folder file
                AccessStatus.DENIED,        // record read
                AccessStatus.DENIED);       // record file

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(otherFolder, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(otherFolder, RMPermissionModel.FILING));
                return null;
            }
        }, userThree);

        setPermission(rmFolder, userOne, RMPermissionModel.FILING);
        setPermission(otherFolder, userTwo, RMPermissionModel.FILING);
        setPermission(recordOne, userThree, RMPermissionModel.FILING);

        assertPermissions(userOne,
                AccessStatus.ALLOWED,       // fileplan read
                AccessStatus.DENIED,        // fileplan file
                AccessStatus.DENIED,        // category read
                AccessStatus.DENIED,        // category file
                AccessStatus.ALLOWED,       // record folder read
                AccessStatus.ALLOWED,       // record folder file
                AccessStatus.ALLOWED,       // record read
                AccessStatus.ALLOWED);      // record file

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(otherFolder, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(otherFolder, RMPermissionModel.FILING));
                return null;
            }
        }, userOne);

        assertPermissions(userTwo,
                AccessStatus.ALLOWED,       // fileplan read
                AccessStatus.DENIED,        // fileplan file
                AccessStatus.DENIED,        // category read
                AccessStatus.DENIED,        // category file
                AccessStatus.DENIED,        // record folder read
                AccessStatus.DENIED,        // record folder file
                AccessStatus.DENIED,        // record read
                AccessStatus.DENIED);       // record file

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(otherFolder, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(otherFolder, RMPermissionModel.FILING));
                return null;
            }
        }, userTwo);

        assertPermissions(userThree,
                AccessStatus.ALLOWED,       // fileplan read
                AccessStatus.DENIED,        // fileplan file
                AccessStatus.DENIED,        // category read
                AccessStatus.DENIED,        // category file
                AccessStatus.DENIED,        // record folder read
                AccessStatus.DENIED,        // record folder file
                AccessStatus.ALLOWED,       // record read
                AccessStatus.ALLOWED);      // record file

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(otherFolder, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(otherFolder, RMPermissionModel.FILING));
                return null;
            }
        }, userThree);

        // move the record!
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                fileFolderService.move(recordOne, otherFolder, "movedRecord.txt");
                return null;
            }
        });

        assertPermissions(userOne,
                AccessStatus.ALLOWED,       // fileplan read
                AccessStatus.DENIED,        // fileplan file
                AccessStatus.DENIED,        // category read
                AccessStatus.DENIED,        // category file
                AccessStatus.ALLOWED,       // record folder read
                AccessStatus.ALLOWED,       // record folder file
                AccessStatus.DENIED,        // record read
                AccessStatus.DENIED);       // record file

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(otherFolder, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(otherFolder, RMPermissionModel.FILING));
                return null;
            }
        }, userOne);

        assertPermissions(userTwo,
                AccessStatus.ALLOWED,       // fileplan read
                AccessStatus.DENIED,        // fileplan file
                AccessStatus.DENIED,        // category read
                AccessStatus.DENIED,        // category file
                AccessStatus.DENIED,        // record folder read
                AccessStatus.DENIED,        // record folder file
                AccessStatus.ALLOWED,       // record read
                AccessStatus.ALLOWED);      // record file

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(otherFolder, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(otherFolder, RMPermissionModel.FILING));
                return null;
            }
        }, userTwo);

        assertPermissions(userThree,
                AccessStatus.ALLOWED,       // fileplan read
                AccessStatus.DENIED,        // fileplan file
                AccessStatus.DENIED,        // category read
                AccessStatus.DENIED,        // category file
                AccessStatus.DENIED,        // record folder read
                AccessStatus.DENIED,        // record folder file
                AccessStatus.ALLOWED,       // record read
                AccessStatus.ALLOWED);      // record file

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(otherFolder, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(otherFolder, RMPermissionModel.FILING));
                return null;
            }
        }, userThree);

    }

    /**
     * Helper to assert permissions for passed user
     */
    private void assertPermissions(final String userName, final AccessStatus ... accessStatus)
    {
        assertEquals(8, accessStatus.length);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals("Everyone who has a role has read permissions on the file plan",
                             accessStatus[0], permissionService.hasPermission(filePlan, RMPermissionModel.READ_RECORDS));
                assertEquals(accessStatus[1], permissionService.hasPermission(filePlan, RMPermissionModel.FILING));

                assertEquals(accessStatus[2], permissionService.hasPermission(rmContainer, RMPermissionModel.READ_RECORDS));
                assertEquals(accessStatus[3], permissionService.hasPermission(rmContainer, RMPermissionModel.FILING));

                assertEquals(accessStatus[4], permissionService.hasPermission(rmFolder, RMPermissionModel.READ_RECORDS));
                assertEquals(accessStatus[5], permissionService.hasPermission(rmFolder, RMPermissionModel.FILING));

                assertEquals(accessStatus[6], permissionService.hasPermission(recordOne, RMPermissionModel.READ_RECORDS));
                assertEquals(accessStatus[7], permissionService.hasPermission(recordOne, RMPermissionModel.FILING));

                return null;
            }
        }, userName);
    }

    /**
     * Helper to assert permissions for the passed user
     */
    private void assertPermissionsWithInheritance(
            final String userName,
            final NodeRef subCategory,
            final NodeRef folder,
            final NodeRef record,
            final AccessStatus ... accessStatus)
    {
        assertEquals(16, accessStatus.length);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(accessStatus[0], permissionService.hasPermission(filePlan, RMPermissionModel.READ_RECORDS));
                assertEquals(accessStatus[1], permissionService.hasPermission(filePlan, RMPermissionModel.FILING));

                assertEquals(accessStatus[2], permissionService.hasPermission(transfersContainer, RMPermissionModel.READ_RECORDS));
                assertEquals(accessStatus[3], permissionService.hasPermission(transfersContainer, RMPermissionModel.FILING));

                assertEquals(accessStatus[4], permissionService.hasPermission(holdsContainer, RMPermissionModel.READ_RECORDS));
                assertEquals(accessStatus[5], permissionService.hasPermission(holdsContainer, RMPermissionModel.FILING));

                assertEquals(accessStatus[6], permissionService.hasPermission(unfiledContainer, RMPermissionModel.READ_RECORDS));
                assertEquals(accessStatus[7], permissionService.hasPermission(unfiledContainer, RMPermissionModel.FILING));

                assertEquals(accessStatus[8], permissionService.hasPermission(rmContainer, RMPermissionModel.READ_RECORDS));
                assertEquals(accessStatus[9], permissionService.hasPermission(rmContainer, RMPermissionModel.FILING));

                assertEquals(accessStatus[10], permissionService.hasPermission(subCategory, RMPermissionModel.READ_RECORDS));
                assertEquals(accessStatus[11], permissionService.hasPermission(subCategory, RMPermissionModel.FILING));

                assertEquals(accessStatus[12], permissionService.hasPermission(folder, RMPermissionModel.READ_RECORDS));
                assertEquals(accessStatus[13], permissionService.hasPermission(folder, RMPermissionModel.FILING));

                assertEquals(accessStatus[14], permissionService.hasPermission(record, RMPermissionModel.READ_RECORDS));
                assertEquals(accessStatus[15], permissionService.hasPermission(record, RMPermissionModel.FILING));

                return null;
            }
        }, userName);
    }

    public void testFilePlanComponentInheritance()
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                // Inheritance is turned off for file plan, transfer, holds, unfiled records and root categories
                // it is turned on for sub categories, record folders and records
                assertFalse(permissionService.getInheritParentPermissions(filePlan));
                assertFalse(permissionService.getInheritParentPermissions(filePlanService.getTransferContainer(filePlan)));
                assertFalse(permissionService.getInheritParentPermissions(filePlanService.getHoldContainer(filePlan)));
                assertFalse(permissionService.getInheritParentPermissions(unfiledContainer));
                assertFalse(permissionService.getInheritParentPermissions(rmContainer));
                assertTrue(permissionService.getInheritParentPermissions(recordFolderService.createRecordFolder(rmContainer, "subCategory")));
                assertTrue(permissionService.getInheritParentPermissions(rmFolder));
                assertTrue(permissionService.getInheritParentPermissions(recordOne));

                return null;
            }
        }, ADMIN_USER);
    }

    public void testRolesSetByDefault()
    {
        NodeRef subCategory = filePlanService.createRecordCategory(rmContainer, "subCategory1");
        NodeRef folder = recordFolderService.createRecordFolder(subCategory, "rmFolder1");
        NodeRef record = utils.createRecord(folder, "record1.txt");

        // Admin user has read/filing permissions on file plan, transfer, hold, unfiled records, root categories, sub categories, folders and records
        assertPermissionsWithInheritance(ADMIN_USER, subCategory, folder, record,
                AccessStatus.ALLOWED,       // fileplan read
                AccessStatus.ALLOWED,       // fileplan file
                AccessStatus.ALLOWED,       // transfer read
                AccessStatus.ALLOWED,       // transfer file
                AccessStatus.ALLOWED,       // holds read
                AccessStatus.ALLOWED,       // holds file
                AccessStatus.ALLOWED,       // unfiled records file
                AccessStatus.ALLOWED,       // unfiled records file
                AccessStatus.ALLOWED,       // root category read
                AccessStatus.ALLOWED,       // root category file
                AccessStatus.ALLOWED,       // sub category read
                AccessStatus.ALLOWED,       // sub category file
                AccessStatus.ALLOWED,       // folder read
                AccessStatus.ALLOWED,       // folder file
                AccessStatus.ALLOWED,       // record read
                AccessStatus.ALLOWED);      // record file

        // Test user has read permissions on file plan, transfer, hold and unfiled records as the user will be added in the all records management roles
        // which has read permissions on those nodes by default
        assertPermissionsWithInheritance(createTestUser(), subCategory, folder, record,
                AccessStatus.ALLOWED,       // fileplan read
                AccessStatus.DENIED,        // fileplan file
                AccessStatus.ALLOWED,       // transfer read
                AccessStatus.DENIED,        // transfer file
                AccessStatus.ALLOWED,       // holds read
                AccessStatus.DENIED,        // holds file
                AccessStatus.ALLOWED,       // unfiled records file
                AccessStatus.DENIED,        // unfiled records file
                AccessStatus.DENIED,        // root category read
                AccessStatus.DENIED,        // root category file
                AccessStatus.DENIED,        // sub category read
                AccessStatus.DENIED,        // sub category file
                AccessStatus.DENIED,        // folder read
                AccessStatus.DENIED,        // folder file
                AccessStatus.DENIED,        // record read
                AccessStatus.DENIED);       // record file
    }

    public void testAddUserToContainers()
    {
        NodeRef subCategory = filePlanService.createRecordCategory(rmContainer, "subCategory2");
        NodeRef folder = recordFolderService.createRecordFolder(subCategory, "rmFolder2");
        NodeRef record = utils.createRecord(folder, "record2.txt");

        // The user1 will have read permissions on the file plan
        // and read permissions on transfer, hold and unfiled records as the user will be in the all records management users role
        String user1 = createTestUser();
        setPermission(filePlan, user1, RMPermissionModel.READ_RECORDS);
        assertPermissionsWithInheritance(user1, subCategory, folder, record,
                AccessStatus.ALLOWED,       // fileplan read
                AccessStatus.DENIED,        // fileplan file
                AccessStatus.ALLOWED,       // transfer read
                AccessStatus.DENIED,        // transfer file
                AccessStatus.ALLOWED,       // holds read
                AccessStatus.DENIED,        // holds file
                AccessStatus.ALLOWED,       // unfiled records file
                AccessStatus.DENIED,        // unfiled records file
                AccessStatus.DENIED,        // root category read
                AccessStatus.DENIED,        // root category file
                AccessStatus.DENIED,        // sub category read
                AccessStatus.DENIED,        // sub category file
                AccessStatus.DENIED,        // folder read
                AccessStatus.DENIED,        // folder file
                AccessStatus.DENIED,        // record read
                AccessStatus.DENIED);       // record file

        // The user2 will have read and filing permissions on the transfer container
        // and read permissions on file plan, hold and unfiled records as the user will be in the all records management users role
        String user2 = createTestUser();
        setPermission(transfersContainer, user2, RMPermissionModel.FILING);
        assertPermissionsWithInheritance(user2, subCategory, folder, record,
                AccessStatus.ALLOWED,       // fileplan read
                AccessStatus.DENIED,        // fileplan file
                AccessStatus.ALLOWED,       // transfer read
                AccessStatus.ALLOWED,       // transfer file
                AccessStatus.ALLOWED,       // holds read
                AccessStatus.DENIED,        // holds file
                AccessStatus.ALLOWED,       // unfiled records file
                AccessStatus.DENIED,        // unfiled records file
                AccessStatus.DENIED,        // root category read
                AccessStatus.DENIED,        // root category file
                AccessStatus.DENIED,        // sub category read
                AccessStatus.DENIED,        // sub category file
                AccessStatus.DENIED,        // folder read
                AccessStatus.DENIED,        // folder file
                AccessStatus.DENIED,        // record read
                AccessStatus.DENIED);       // record file

        // The user3 will have read permissions on file plan, transfer, hold and unfiled records
        String user3 = createTestUser();
        setPermission(holdsContainer, user3, RMPermissionModel.READ_RECORDS);
        assertPermissionsWithInheritance(user3, subCategory, folder, record,
                AccessStatus.ALLOWED,       // fileplan read
                AccessStatus.DENIED,        // fileplan file
                AccessStatus.ALLOWED,       // transfer read
                AccessStatus.DENIED,        // transfer file
                AccessStatus.ALLOWED,       // holds read
                AccessStatus.DENIED,        // holds file
                AccessStatus.ALLOWED,       // unfiled records file
                AccessStatus.DENIED,        // unfiled records file
                AccessStatus.DENIED,        // root category read
                AccessStatus.DENIED,        // root category file
                AccessStatus.DENIED,        // sub category read
                AccessStatus.DENIED,        // sub category file
                AccessStatus.DENIED,        // folder read
                AccessStatus.DENIED,        // folder file
                AccessStatus.DENIED,        // record read
                AccessStatus.DENIED);       // record file

        // The user4 will have read permissions on file plan, transfer, hold
        // and read and filing permissions on unfiled records container
        String user4 = createTestUser();
        setPermission(unfiledContainer, user4, RMPermissionModel.FILING);
        assertPermissionsWithInheritance(user4, subCategory, folder, record,
                AccessStatus.ALLOWED,       // fileplan read
                AccessStatus.DENIED,        // fileplan file
                AccessStatus.ALLOWED,       // transfer read
                AccessStatus.DENIED,        // transfer file
                AccessStatus.ALLOWED,       // holds read
                AccessStatus.DENIED,        // holds file
                AccessStatus.ALLOWED,       // unfiled records file
                AccessStatus.ALLOWED,       // unfiled records file
                AccessStatus.DENIED,        // root category read
                AccessStatus.DENIED,        // root category file
                AccessStatus.DENIED,        // sub category read
                AccessStatus.DENIED,        // sub category file
                AccessStatus.DENIED,        // folder read
                AccessStatus.DENIED,        // folder file
                AccessStatus.DENIED,        // record read
                AccessStatus.DENIED);       // record file

        // The user5 will read permissions on the root category
        // as the inheritance is turned on for the sub category the user will have also read permissions on sub category, folder and record
        // and also read permissions on file plan, transfer, hold and unfiled records
        String user5 = createTestUser();
        setPermission(rmContainer, user5, RMPermissionModel.READ_RECORDS);
        assertPermissionsWithInheritance(user5, subCategory, folder, record,
                AccessStatus.ALLOWED,       // fileplan read
                AccessStatus.DENIED,        // fileplan file
                AccessStatus.ALLOWED,       // transfer read
                AccessStatus.DENIED,        // transfer file
                AccessStatus.ALLOWED,       // holds read
                AccessStatus.DENIED,        // holds file
                AccessStatus.ALLOWED,       // unfiled records file
                AccessStatus.DENIED,        // unfiled records file
                AccessStatus.ALLOWED,       // root category read
                AccessStatus.DENIED,        // root category file
                AccessStatus.ALLOWED,       // sub category read
                AccessStatus.DENIED,        // sub category file
                AccessStatus.ALLOWED,       // folder read
                AccessStatus.DENIED,        // folder file
                AccessStatus.ALLOWED,       // record read
                AccessStatus.DENIED);       // record file

        // The user6 will read and filing permissions on the sub category
        // as the inheritance is turned on the user will have also read and filing permissions on folder and record
        // and also read permissions on file plan, transfer, hold and unfiled records
        String user6 = createTestUser();
        setPermission(subCategory, user6, RMPermissionModel.FILING);
        assertPermissionsWithInheritance(user6, subCategory, folder, record,
                AccessStatus.ALLOWED,       // fileplan read
                AccessStatus.DENIED,        // fileplan file
                AccessStatus.ALLOWED,       // transfer read
                AccessStatus.DENIED,        // transfer file
                AccessStatus.ALLOWED,       // holds read
                AccessStatus.DENIED,        // holds file
                AccessStatus.ALLOWED,       // unfiled records file
                AccessStatus.DENIED,        // unfiled records file
                AccessStatus.DENIED,        // root category read
                AccessStatus.DENIED,        // root category file
                AccessStatus.ALLOWED,       // sub category read
                AccessStatus.ALLOWED,       // sub category file
                AccessStatus.ALLOWED,       // folder read
                AccessStatus.ALLOWED,       // folder file
                AccessStatus.ALLOWED,       // record read
                AccessStatus.ALLOWED);      // record file

        // The user7 will read permissions on the folder
        // as the inheritance is turned on the user will have also read on record
        // and also read permissions on file plan, transfer, hold and unfiled records
        String user7 = createTestUser();
        setPermission(folder, user7, RMPermissionModel.READ_RECORDS);
        assertPermissionsWithInheritance(user7, subCategory, folder, record,
                AccessStatus.ALLOWED,       // fileplan read
                AccessStatus.DENIED,        // fileplan file
                AccessStatus.ALLOWED,       // transfer read
                AccessStatus.DENIED,        // transfer file
                AccessStatus.ALLOWED,       // holds read
                AccessStatus.DENIED,        // holds file
                AccessStatus.ALLOWED,       // unfiled records file
                AccessStatus.DENIED,        // unfiled records file
                AccessStatus.DENIED,        // root category read
                AccessStatus.DENIED,        // root category file
                AccessStatus.DENIED,        // sub category read
                AccessStatus.DENIED,        // sub category file
                AccessStatus.ALLOWED,       // folder read
                AccessStatus.DENIED,        // folder file
                AccessStatus.ALLOWED,       // record read
                AccessStatus.DENIED);       // record file

        // The user8 will read and filing permissions on the record
        // and also read permissions on file plan, transfer, hold and unfiled records
        String user8 = createTestUser();
        setPermission(record, user8, RMPermissionModel.FILING);
        assertPermissionsWithInheritance(user8, subCategory, folder, record,
                AccessStatus.ALLOWED,       // fileplan read
                AccessStatus.DENIED,        // fileplan file
                AccessStatus.ALLOWED,       // transfer read
                AccessStatus.DENIED,        // transfer file
                AccessStatus.ALLOWED,       // holds read
                AccessStatus.DENIED,        // holds file
                AccessStatus.ALLOWED,       // unfiled records file
                AccessStatus.DENIED,        // unfiled records file
                AccessStatus.DENIED,        // root category read
                AccessStatus.DENIED,        // root category file
                AccessStatus.DENIED,        // sub category read
                AccessStatus.DENIED,        // sub category file
                AccessStatus.DENIED,        // folder read
                AccessStatus.DENIED,        // folder file
                AccessStatus.ALLOWED,       // record read
                AccessStatus.ALLOWED);      // record file
    }

    public void testAccessPermissionOnSingleRecordWithSeveralUsers()
    {
        final NodeRef subCategory = filePlanService.createRecordCategory(rmContainer, "subCategory3");
        final NodeRef folder = recordFolderService.createRecordFolder(subCategory, "rmFolder3");
        final NodeRef record = utils.createRecord(folder, "record3.txt");

        String user1 = createTestUser();
        String user2 = createTestUser();

        setPermission(rmContainer, user1, RMPermissionModel.READ_RECORDS);

        // user1 will have access to file plan, root category and because of inheritance sub category, folder and record
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(filePlan, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(rmContainer, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(subCategory, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(folder, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(record, RMPermissionModel.READ_RECORDS));

                return null;
            }
        }, user1);

        // user2 will have access to file plan
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(filePlan, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(rmContainer, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(subCategory, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(folder, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(record, RMPermissionModel.READ_RECORDS));

                return null;
            }
        }, user2);
    }

    public void testDenyPermissionsOnRecordsWithSeveralUsers()
    {
        final NodeRef subCategory = filePlanService.createRecordCategory(rmContainer, "subCategory4");
        final NodeRef folder = recordFolderService.createRecordFolder(subCategory, "rmFolder4");
        final NodeRef record4 = utils.createRecord(folder, "record4.txt");
        final NodeRef record5 = utils.createRecord(folder, "record5.txt");

        String user1 = createTestUser();
        String user2 = createTestUser();

        setPermission(rmContainer, user1, RMPermissionModel.READ_RECORDS);
        setPermission(rmContainer, user2, RMPermissionModel.READ_RECORDS);

        permissionService.setInheritParentPermissions(record4, false);
        permissionService.setInheritParentPermissions(record5, false);

        setPermission(record4, user1, RMPermissionModel.READ_RECORDS);
        setPermission(record5, user1, RMPermissionModel.READ_RECORDS);

        // user1 will have access to file plan, root category and because of inheritance sub category, folder, record4 and record5
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(filePlan, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(rmContainer, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(subCategory, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(folder, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(record4, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(record5, RMPermissionModel.READ_RECORDS));

                return null;
            }
        }, user1);

        // user2 will have access to file plan, root category and because of inheritance sub category and folder
        // user2 won't have access to the records as the inheritance is set to false
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(filePlan, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(rmContainer, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(subCategory, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(folder, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(record4, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(record5, RMPermissionModel.READ_RECORDS));

                return null;
            }
        }, user2);
    }

    public void testMoveRootCategoryIntoAnotherRootCategory()
    {
        final NodeRef category5 = filePlanService.createRecordCategory(filePlan, "category5");
        final NodeRef category6 = filePlanService.createRecordCategory(filePlan, "category6");

        assertFalse(permissionService.getInheritParentPermissions(category5));
        assertFalse(permissionService.getInheritParentPermissions(category6));

        final String user1 = createTestUser();
        final String user2 = createTestUser();

        setPermission(category5, user1, RMPermissionModel.READ_RECORDS);
        setPermission(category6, user2, RMPermissionModel.FILING);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(category5, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(category5, RMPermissionModel.FILING));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(category6, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(category6, RMPermissionModel.FILING));

                return null;
            }
        }, user1);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(category5, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(category5, RMPermissionModel.FILING));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(category6, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(category6, RMPermissionModel.FILING));

                return null;
            }
        }, user2);

        final NodeRef movedCategory5 = doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run() throws Exception
            {
                return fileFolderService.move(category5, category6, null).getNodeRef();
            }
        });

        assertFalse(permissionService.getInheritParentPermissions(movedCategory5));
        assertFalse(permissionService.getInheritParentPermissions(category6));

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(movedCategory5, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(movedCategory5, RMPermissionModel.FILING));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(category6, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(category6, RMPermissionModel.FILING));

                return null;
            }
        }, user1);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(movedCategory5, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(movedCategory5, RMPermissionModel.FILING));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(category6, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(category6, RMPermissionModel.FILING));

                return null;
            }
        }, user2);
    }

    public void testPermissionsForMovedRecord()
    {
        final NodeRef category7 = filePlanService.createRecordCategory(filePlan, "category7");
        final NodeRef folder7 = recordFolderService.createRecordFolder(category7, "rmFolder7");
        final NodeRef record7 = utils.createRecord(folder7, "record7.txt");

        final NodeRef category8 = filePlanService.createRecordCategory(filePlan, "category8");
        final NodeRef folder8 = recordFolderService.createRecordFolder(category8, "rmFolder8");
        final NodeRef record8 = utils.createRecord(folder8, "record8.txt");

        final String user1 = createTestUser();
        final String user2 = createTestUser();
        final String user3 = createTestUser();

        setPermission(folder7, user1, RMPermissionModel.FILING);
        setPermission(record8, user2, RMPermissionModel.READ_RECORDS);
        setPermission(category7, user3, RMPermissionModel.FILING);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(category7, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(category7, RMPermissionModel.FILING));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(folder7, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(folder7, RMPermissionModel.FILING));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(record7, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(record7, RMPermissionModel.FILING));

                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(category8, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(category8, RMPermissionModel.FILING));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(folder8, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(folder8, RMPermissionModel.FILING));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(record8, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(record8, RMPermissionModel.FILING));

                return null;
            }
        }, user1);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(category7, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(category7, RMPermissionModel.FILING));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(folder7, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(folder7, RMPermissionModel.FILING));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(record7, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(record7, RMPermissionModel.FILING));

                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(category8, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(category8, RMPermissionModel.FILING));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(folder8, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(folder8, RMPermissionModel.FILING));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(record8, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(record8, RMPermissionModel.FILING));

                return null;
            }
        }, user2);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(category7, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(category7, RMPermissionModel.FILING));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(folder7, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(folder7, RMPermissionModel.FILING));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(record7, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(record7, RMPermissionModel.FILING));

                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(category8, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(category8, RMPermissionModel.FILING));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(folder8, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(folder8, RMPermissionModel.FILING));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(record8, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(record8, RMPermissionModel.FILING));

                return null;
            }
        }, user3);

        final NodeRef movedRecord8 = doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run() throws Exception
            {
                return fileFolderService.move(record8, folder7, null).getNodeRef();
            }
        });

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(category7, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(category7, RMPermissionModel.FILING));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(folder7, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(folder7, RMPermissionModel.FILING));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(record7, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(record7, RMPermissionModel.FILING));

                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(category8, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(category8, RMPermissionModel.FILING));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(folder8, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(folder8, RMPermissionModel.FILING));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(movedRecord8, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(movedRecord8, RMPermissionModel.FILING));

                return null;
            }
        }, user1);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(category7, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(category7, RMPermissionModel.FILING));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(folder7, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(folder7, RMPermissionModel.FILING));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(record7, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(record7, RMPermissionModel.FILING));

                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(category8, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(category8, RMPermissionModel.FILING));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(folder8, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(folder8, RMPermissionModel.FILING));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(movedRecord8, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(movedRecord8, RMPermissionModel.FILING));

                return null;
            }
        }, user2);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(category7, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(category7, RMPermissionModel.FILING));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(folder7, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(folder7, RMPermissionModel.FILING));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(record7, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(record7, RMPermissionModel.FILING));

                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(category8, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(category8, RMPermissionModel.FILING));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(folder8, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(folder8, RMPermissionModel.FILING));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(movedRecord8, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(movedRecord8, RMPermissionModel.FILING));

                return null;
            }
        }, user3);
    }

    public void testSpecialRoles()
    {
        final NodeRef category9 = filePlanService.createRecordCategory(filePlan, "category9");
        final NodeRef subCategory9 = filePlanService.createRecordCategory(category9, "subCategory9");
        final NodeRef folder9 = recordFolderService.createRecordFolder(subCategory9, "rmFolder9");
        final NodeRef record9 = utils.createRecord(folder9, "record9.txt");

        assertExistenceOfSpecialRolesAndPermissions(category9);

        assertExistenceOfSpecialRolesAndPermissions(subCategory9);
        // After setting the permissions off the special roles should be still available as they will be added to the node automatically
        permissionService.setInheritParentPermissions(subCategory9, false);
        assertExistenceOfSpecialRolesAndPermissions(subCategory9);
        permissionService.setInheritParentPermissions(subCategory9, true);
        assertExistenceOfSpecialRolesAndPermissions(subCategory9);

        assertExistenceOfSpecialRolesAndPermissions(folder9);
        permissionService.setInheritParentPermissions(folder9, false);
        assertExistenceOfSpecialRolesAndPermissions(folder9);
        permissionService.setInheritParentPermissions(folder9, true);
        assertExistenceOfSpecialRolesAndPermissions(folder9);

        assertExistenceOfSpecialRolesAndPermissions(record9);
        permissionService.setInheritParentPermissions(record9, false);
        assertExistenceOfSpecialRolesAndPermissions(record9);
        permissionService.setInheritParentPermissions(record9, true);
        assertExistenceOfSpecialRolesAndPermissions(record9);
    }

    private void assertExistenceOfSpecialRolesAndPermissions(NodeRef node)
    {
        Map<String, String> accessPermissions = new HashMap<>();
        Set<AccessPermission> permissions = permissionService.getAllSetPermissions(node);
        // FIXME!!!
        //assertEquals(3, permissions.size());

        for (AccessPermission permission : permissions)
        {
            accessPermissions.put(permission.getAuthority(),  permission.getPermission());
        }

        String adminRole = authorityService.getName(AuthorityType.GROUP, FilePlanRoleService.ROLE_ADMIN + filePlan.getId());
        assertTrue(accessPermissions.containsKey(adminRole));
        assertEquals(RMPermissionModel.FILING, accessPermissions.get(adminRole));
    }

    public void testMoveSubCategoryIntoFilePlan()
    {
        final NodeRef rootCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());
        final NodeRef subCategory = filePlanService.createRecordCategory(rootCategory, GUID.generate());

        assertFalse(permissionService.getInheritParentPermissions(rootCategory));
        assertTrue(permissionService.getInheritParentPermissions(subCategory));

        final NodeRef movedSubCategory = doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run() throws Exception
            {
                return fileFolderService.move(subCategory, filePlan, null).getNodeRef();
            }
        });

        assertFalse(permissionService.getInheritParentPermissions(rootCategory));
        assertFalse(permissionService.getInheritParentPermissions(movedSubCategory));
    }
}
