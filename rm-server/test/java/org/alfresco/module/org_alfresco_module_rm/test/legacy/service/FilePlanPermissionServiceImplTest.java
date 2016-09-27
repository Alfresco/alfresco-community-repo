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
package org.alfresco.module.org_alfresco_module_rm.test.legacy.service;

import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
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
     * test set/delete permissions on file plan
     */
    public void testSetDeletePermissionFilePlan() throws Exception
    {
        String userName = createTestUser();

        assertPermissions(userName,
                          AccessStatus.ALLOWED,     // fileplan read
                          AccessStatus.DENIED,      // fileplan file
                          AccessStatus.DENIED,      // category read
                          AccessStatus.DENIED,      // category file
                          AccessStatus.DENIED,      // record folder read
                          AccessStatus.DENIED,      // record folder file
                          AccessStatus.DENIED,      // record read
                          AccessStatus.DENIED);     // record file

        setPermission(filePlan, userName, RMPermissionModel.FILING);

        assertPermissions(userName,
                AccessStatus.ALLOWED,       // fileplan read
                AccessStatus.ALLOWED,       // fileplan file
                AccessStatus.ALLOWED,       // category read
                AccessStatus.ALLOWED,       // category file
                AccessStatus.ALLOWED,       // record folder read
                AccessStatus.ALLOWED,       // record folder file
                AccessStatus.ALLOWED,       // record read
                AccessStatus.ALLOWED);      // record file

        deletePermission(filePlan, userName, RMPermissionModel.FILING);

        assertPermissions(userName,
                AccessStatus.ALLOWED,     // fileplan read
                AccessStatus.DENIED,      // fileplan file
                AccessStatus.DENIED,      // category read
                AccessStatus.DENIED,      // category file
                AccessStatus.DENIED,      // record folder read
                AccessStatus.DENIED,      // record folder file
                AccessStatus.DENIED,      // record read
                AccessStatus.DENIED);     // record file

        //what happens if we try and remove READ for a normal user on the file plan ???
        deletePermission(filePlan, userName, RMPermissionModel.READ_RECORDS);

        // nothing .. user still has read on file plan .. only removing the user from all roles will remove read on file plan
        assertPermissions(userName,
                AccessStatus.ALLOWED,     // fileplan read
                AccessStatus.DENIED,      // fileplan file
                AccessStatus.DENIED,      // category read
                AccessStatus.DENIED,      // category file
                AccessStatus.DENIED,      // record folder read
                AccessStatus.DENIED,      // record folder file
                AccessStatus.DENIED,      // record read
                AccessStatus.DENIED);     // record file
    }

    /**
     * Test set/delete permission on record categorty
     */
    public void testSetDeletePermissionRecordCategory() throws Exception
    {
        String userName = createTestUser();

        assertPermissions(userName,
                          AccessStatus.ALLOWED,     // fileplan read
                          AccessStatus.DENIED,      // fileplan file
                          AccessStatus.DENIED,      // category read
                          AccessStatus.DENIED,      // category file
                          AccessStatus.DENIED,      // record folder read
                          AccessStatus.DENIED,      // record folder file
                          AccessStatus.DENIED,      // record read
                          AccessStatus.DENIED);     // record file

        setPermission(rmContainer, userName, RMPermissionModel.FILING);

        assertPermissions(userName,
                          AccessStatus.ALLOWED,     // fileplan read
                          AccessStatus.DENIED,      // fileplan file
                          AccessStatus.ALLOWED,      // category read
                          AccessStatus.ALLOWED,      // category file
                          AccessStatus.ALLOWED,      // record folder read
                          AccessStatus.ALLOWED,      // record folder file
                          AccessStatus.ALLOWED,      // record read
                          AccessStatus.ALLOWED);     // record file

        deletePermission(rmContainer, userName, RMPermissionModel.FILING);

        assertPermissions(userName,
                AccessStatus.ALLOWED,     // fileplan read
                AccessStatus.DENIED,      // fileplan file
                AccessStatus.DENIED,      // category read
                AccessStatus.DENIED,      // category file
                AccessStatus.DENIED,      // record folder read
                AccessStatus.DENIED,      // record folder file
                AccessStatus.DENIED,      // record read
                AccessStatus.DENIED);     // record file
    }

    /**
     * Test set/delete permission on record folder
     */
    public void testSetDeletePermissionRecordFolder() throws Exception
    {
        String userName = createTestUser();

        assertPermissions(userName,
                          AccessStatus.ALLOWED,     // fileplan read
                          AccessStatus.DENIED,      // fileplan file
                          AccessStatus.DENIED,      // category read
                          AccessStatus.DENIED,      // category file
                          AccessStatus.DENIED,      // record folder read
                          AccessStatus.DENIED,      // record folder file
                          AccessStatus.DENIED,      // record read
                          AccessStatus.DENIED);     // record file

        setPermission(rmFolder, userName, RMPermissionModel.FILING);

        assertPermissions(userName,
                          AccessStatus.ALLOWED,     // fileplan read
                          AccessStatus.DENIED,      // fileplan file
                          AccessStatus.ALLOWED,      // category read
                          AccessStatus.DENIED,      // category file
                          AccessStatus.ALLOWED,      // record folder read
                          AccessStatus.ALLOWED,      // record folder file
                          AccessStatus.ALLOWED,      // record read
                          AccessStatus.ALLOWED);     // record file

        deletePermission(rmFolder, userName, RMPermissionModel.FILING);

        assertPermissions(userName,
                          AccessStatus.ALLOWED,     // fileplan read
                          AccessStatus.DENIED,      // fileplan file
                          AccessStatus.ALLOWED,     // category read
                          AccessStatus.DENIED,      // category file
                          AccessStatus.DENIED,      // record folder read
                          AccessStatus.DENIED,      // record folder file
                          AccessStatus.DENIED,      // record read
                          AccessStatus.DENIED);     // record file
    }

    /**
     * Test set/delete permission on record
     */
    public void testSetDeletePermissionRecord() throws Exception
    {
        String userName = createTestUser();

        assertPermissions(userName,
                          AccessStatus.ALLOWED,     // fileplan read
                          AccessStatus.DENIED,      // fileplan file
                          AccessStatus.DENIED,      // category read
                          AccessStatus.DENIED,      // category file
                          AccessStatus.DENIED,      // record folder read
                          AccessStatus.DENIED,      // record folder file
                          AccessStatus.DENIED,      // record read
                          AccessStatus.DENIED);     // record file

        setPermission(recordOne, userName, RMPermissionModel.FILING);

        assertPermissions(userName,
                          AccessStatus.ALLOWED,     // fileplan read
                          AccessStatus.DENIED,      // fileplan file
                          AccessStatus.ALLOWED,      // category read
                          AccessStatus.DENIED,      // category file
                          AccessStatus.ALLOWED,      // record folder read
                          AccessStatus.DENIED,      // record folder file
                          AccessStatus.ALLOWED,      // record read
                          AccessStatus.ALLOWED);     // record file

        deletePermission(recordOne, userName, RMPermissionModel.FILING);

        assertPermissions(userName,
                          AccessStatus.ALLOWED,     // fileplan read
                          AccessStatus.DENIED,      // fileplan file
                          AccessStatus.ALLOWED,      // category read
                          AccessStatus.DENIED,      // category file
                          AccessStatus.ALLOWED,      // record folder read
                          AccessStatus.DENIED,      // record folder file
                          AccessStatus.DENIED,      // record read
                          AccessStatus.DENIED);     // record file
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
                AccessStatus.ALLOWED,     // fileplan read
                AccessStatus.DENIED,      // fileplan file
                AccessStatus.DENIED,      // category read
                AccessStatus.DENIED,      // category file
                AccessStatus.DENIED,      // record folder read
                AccessStatus.DENIED,      // record folder file
                AccessStatus.DENIED,      // record read
                AccessStatus.DENIED);     // record file
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
                AccessStatus.ALLOWED,     // fileplan read
                AccessStatus.DENIED,      // fileplan file
                AccessStatus.DENIED,      // category read
                AccessStatus.DENIED,      // category file
                AccessStatus.DENIED,      // record folder read
                AccessStatus.DENIED,      // record folder file
                AccessStatus.DENIED,      // record read
                AccessStatus.DENIED);     // record file
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
                AccessStatus.ALLOWED,     // fileplan read
                AccessStatus.DENIED,      // fileplan file
                AccessStatus.DENIED,      // category read
                AccessStatus.DENIED,      // category file
                AccessStatus.DENIED,      // record folder read
                AccessStatus.DENIED,      // record folder file
                AccessStatus.DENIED,      // record read
                AccessStatus.DENIED);     // record file
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
                AccessStatus.ALLOWED,     // fileplan read
                AccessStatus.DENIED,      // fileplan file
                AccessStatus.ALLOWED,      // category read
                AccessStatus.DENIED,      // category file
                AccessStatus.ALLOWED,      // record folder read
                AccessStatus.ALLOWED,      // record folder file
                AccessStatus.ALLOWED,      // record read
                AccessStatus.ALLOWED);     // record file
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
                AccessStatus.ALLOWED,     // fileplan read
                AccessStatus.DENIED,      // fileplan file
                AccessStatus.ALLOWED,      // category read
                AccessStatus.DENIED,      // category file
                AccessStatus.DENIED,      // record folder read
                AccessStatus.DENIED,      // record folder file
                AccessStatus.DENIED,      // record read
                AccessStatus.DENIED);     // record file
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
                AccessStatus.ALLOWED,     // fileplan read
                AccessStatus.DENIED,      // fileplan file
                AccessStatus.ALLOWED,      // category read
                AccessStatus.DENIED,      // category file
                AccessStatus.ALLOWED,      // record folder read
                AccessStatus.DENIED,      // record folder file
                AccessStatus.ALLOWED,      // record read
                AccessStatus.ALLOWED);     // record file
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
                AccessStatus.ALLOWED,     // fileplan read
                AccessStatus.DENIED,      // fileplan file
                AccessStatus.ALLOWED,      // category read
                AccessStatus.DENIED,      // category file
                AccessStatus.ALLOWED,      // record folder read
                AccessStatus.ALLOWED,      // record folder file
                AccessStatus.DENIED,      // record read
                AccessStatus.DENIED);     // record file
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
                AccessStatus.ALLOWED,     // fileplan read
                AccessStatus.DENIED,      // fileplan file
                AccessStatus.ALLOWED,      // category read
                AccessStatus.DENIED,      // category file
                AccessStatus.DENIED,      // record folder read
                AccessStatus.DENIED,      // record folder file
                AccessStatus.ALLOWED,      // record read
                AccessStatus.ALLOWED);     // record file
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
                AccessStatus.ALLOWED,     // fileplan read
                AccessStatus.DENIED,      // fileplan file
                AccessStatus.ALLOWED,      // category read
                AccessStatus.DENIED,      // category file
                AccessStatus.ALLOWED,      // record folder read
                AccessStatus.DENIED,      // record folder file
                AccessStatus.ALLOWED,      // record read
                AccessStatus.ALLOWED);     // record file
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(otherFolder, RMPermissionModel.READ_RECORDS));
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

}
