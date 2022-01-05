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

package org.alfresco.rest.rm.community.rmroles;

import static java.util.Collections.singleton;

import static com.google.common.collect.Sets.newHashSet;

import static org.alfresco.rest.rm.community.base.TestData.RM_ROLES;
import static org.alfresco.rest.rm.community.model.user.UserRoles.ROLE_RM_USER;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.user.UserCapabilities;
import org.alfresco.rest.v0.RMRolesAndActionsAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

/**
 * API tests of RM roles.
 *
 * @author Tom Page
 * @since 2.7
 */
public class RMRolesTests extends BaseRMRestTest
{
    /** A list of capabilities. */
    private static final java.util.HashSet<String> CAPABILITIES = newHashSet(UserCapabilities.VIEW_RECORDS_CAP, UserCapabilities.DECLARE_RECORDS_CAP);
    /** The API for managing RM roles and capabilities. */
    @Autowired
    private RMRolesAndActionsAPI rmRolesAndActionsAPI;

    /** Check that the roles API returns the default RM roles. */
    @Test(description = "Check the default RM roles exist.")
    public void checkRMRolesExist()
    {
        Set<String> configuredRoles = rmRolesAndActionsAPI
                    .getConfiguredRoles(getAdminUser().getUsername(), getAdminUser().getPassword());
        RM_ROLES.forEach(role -> assertTrue("Could not found role " + role, configuredRoles.contains(role)));
    }

    /** Check that the RM user has the capability to view and declare records. */
    @Test(description = "Check the capabilities for the RM user.")
    public void checkCapabilitiesForUser()
    {
        Set<String> capabilities = rmRolesAndActionsAPI
                    .getCapabilitiesForRole(getAdminUser().getUsername(), getAdminUser().getPassword(), ROLE_RM_USER
                            .roleId);
        assertEquals("Unexpected capabilities found for RM User.", capabilities, CAPABILITIES);
    }

    /** Check that a new role can be created and retrieved. */
    @Test(description = "Create a new role.")
    public void createNewRole()
    {
        String roleName = generateTestPrefix(RMRolesTests.class) + "newName";

        // Call the endpoint under test.
        rmRolesAndActionsAPI.createRole(getAdminUser().getUsername(), getAdminUser().getPassword(), roleName,
                    "New Role Label", CAPABILITIES);

        Set<String> actualCapabilities = rmRolesAndActionsAPI
                    .getCapabilitiesForRole(getAdminUser().getUsername(), getAdminUser().getPassword(), roleName);
        assertEquals("Unexpected capabilities found for RM User.", actualCapabilities, CAPABILITIES);
    }

    /** Check that a role can be edited. */
    @Test(description = "Update a role.")
    public void updateRole()
    {
        String roleName = generateTestPrefix(RMRolesTests.class) + "Name";
        rmRolesAndActionsAPI.createRole(getAdminUser().getUsername(), getAdminUser().getPassword(), roleName, "Label",
                    singleton(UserCapabilities.VIEW_RECORDS_CAP));

        // Call the endpoint under test.
        rmRolesAndActionsAPI.updateRole(getAdminUser().getUsername(), getAdminUser().getPassword(), roleName,
                    "Updated Label", singleton(UserCapabilities.DECLARE_RECORDS_CAP));

        Set<String> actualCapabilities = rmRolesAndActionsAPI
                    .getCapabilitiesForRole(getAdminUser().getUsername(), getAdminUser().getPassword(), roleName);
        assertEquals("Unexpected capabilities for edited RM User.", actualCapabilities, singleton(UserCapabilities.DECLARE_RECORDS_CAP));
    }
}
