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

import java.util.HashSet;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.role.Role;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;

/**
 * File plan role service unit test
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public class FilePlanRoleServiceImplTest extends BaseRMTestCase
{
    @Override
    protected boolean isUserTest()
    {
        return true;
    }

    public void testGetAllRolesContainerGroup() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            public Void run()
            {
                String allRolesGroup = filePlanRoleService.getAllRolesContainerGroup(filePlan);
                assertNotNull(allRolesGroup);

                return null;
            }
        });
    }

    public void testGetRoles() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            public Void run()
            {
                Set<Role> roles = filePlanRoleService.getRoles(filePlan);
                assertNotNull(roles);
                assertTrue(roles.size() != 0);

                Set<Role> rolesIncludingSystemRoles = filePlanRoleService.getRoles(filePlan, true);
                assertNotNull(rolesIncludingSystemRoles);
                assertTrue(roles.size() != 0);
                assertTrue(roles.size() == rolesIncludingSystemRoles.size());

                Set<Role> rolesWithoutSystemRoles = filePlanRoleService.getRoles(filePlan, false);
                assertNotNull(rolesWithoutSystemRoles);
                assertTrue(rolesWithoutSystemRoles.size() != 0);
                assertTrue(rolesIncludingSystemRoles.size() > rolesWithoutSystemRoles.size());
                assertTrue(rolesIncludingSystemRoles.size() == rolesWithoutSystemRoles.size() + FilePlanRoleService.SYSTEM_ROLES.size());

                return null;
            }
        });
    }

    public void testRolesByUser() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            public Void run()
            {
                Set<Role> roles = filePlanRoleService.getRolesByUser(filePlan, rmUserName);
                assertNotNull(roles);
                assertEquals(1, roles.size());

                Set<Role> rolesIncludingSystemRoles = filePlanRoleService.getRolesByUser(filePlan, rmUserName, true);
                assertNotNull(rolesIncludingSystemRoles);
                assertEquals(1, rolesIncludingSystemRoles.size());
                assertEquals(roles.size(), rolesIncludingSystemRoles.size());

                return null;
            }
        });
    }

    public void testGetRole() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            public Void run()
            {
                Role role = filePlanRoleService.getRole(filePlan, FilePlanRoleService.ROLE_POWER_USER);
                assertNotNull(role);
                assertEquals(FilePlanRoleService.ROLE_POWER_USER, role.getName());

                role = filePlanRoleService.getRole(filePlan, "donkey");
                assertNull(role);

                return null;
            }
        });
    }

    public void testExistsRole() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            public Void run()
            {
                assertTrue(filePlanRoleService.existsRole(filePlan, FilePlanRoleService.ROLE_POWER_USER));
                assertFalse(filePlanRoleService.existsRole(filePlan, "donkey"));

                return null;
            }
        });
    }

    public void testCreateUpdateDeleteRole() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            public Void run()
            {
                assertFalse(filePlanRoleService.existsRole(filePlan, "Michelle Holt"));

                Set<Capability> caps = new HashSet<>(2);
                caps.add(capabilityService.getCapability(RMPermissionModel.ACCESS_AUDIT));
                caps.add(capabilityService.getCapability(RMPermissionModel.ADD_MODIFY_EVENT_DATES));

                Role role = filePlanRoleService.createRole(filePlan, "Michelle Holt", "Michelle Holt", caps);
                assertNotNull(role);
                assertEquals("Michelle Holt", role.getName());
                assertEquals(2, role.getCapabilities().size());

                assertTrue(filePlanRoleService.existsRole(filePlan, "Michelle Holt"));

                caps.add(capabilityService.getCapability(RMPermissionModel.AUTHORIZE_ALL_TRANSFERS));

                role = filePlanRoleService.updateRole(filePlan, "Michelle Holt", "Michelle Wetherall", caps);
                assertNotNull(role);
                assertEquals("Michelle Holt", role.getName());
                assertEquals(3, role.getCapabilities().size());

                assertTrue(filePlanRoleService.existsRole(filePlan, "Michelle Holt"));

                filePlanRoleService.deleteRole(filePlan, "Michelle Holt");

                assertFalse(filePlanRoleService.existsRole(filePlan, "Michelle Holt"));

                return null;
            }
        });
    }

    /**
     * {@link FilePlanRoleService#assignRoleToAuthority(org.alfresco.service.cmr.repository.NodeRef, String, String)}
     * {@link FilePlanRoleService#getAuthorities(org.alfresco.service.cmr.repository.NodeRef, String)
     */
    public void testAuthorityAssignment() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            public Void run()
            {
                Set<Role> roles = filePlanRoleService.getRolesByUser(filePlan, rmUserName);
                assertNotNull(roles);
                assertEquals(1, roles.size());

                Set<String> authorities = filePlanRoleService.getUsersAssignedToRole(filePlan, FilePlanRoleService.ROLE_RECORDS_MANAGER);
                assertNotNull(authorities);
                assertEquals(1, authorities.size());

                authorities = filePlanRoleService.getGroupsAssignedToRole(filePlan, FilePlanRoleService.ROLE_RECORDS_MANAGER);
                assertNotNull(authorities);
                assertEquals(0, authorities.size());

                authorities = filePlanRoleService.getAllAssignedToRole(filePlan, FilePlanRoleService.ROLE_RECORDS_MANAGER);
                assertNotNull(authorities);
                assertEquals(1, authorities.size());

                filePlanRoleService.assignRoleToAuthority(filePlan, FilePlanRoleService.ROLE_RECORDS_MANAGER, rmUserName);

                roles = filePlanRoleService.getRolesByUser(filePlan, rmUserName);
                assertNotNull(roles);
                assertEquals(2, roles.size());

                authorities = filePlanRoleService.getUsersAssignedToRole(filePlan, FilePlanRoleService.ROLE_RECORDS_MANAGER);
                assertNotNull(authorities);
                assertEquals(2, authorities.size());

                authorities = filePlanRoleService.getGroupsAssignedToRole(filePlan, FilePlanRoleService.ROLE_RECORDS_MANAGER);
                assertNotNull(authorities);
                assertEquals(0, authorities.size());

                authorities = filePlanRoleService.getAllAssignedToRole(filePlan, FilePlanRoleService.ROLE_RECORDS_MANAGER);
                assertNotNull(authorities);
                assertEquals(2, authorities.size());


                return null;
            }
        });
    }
}
