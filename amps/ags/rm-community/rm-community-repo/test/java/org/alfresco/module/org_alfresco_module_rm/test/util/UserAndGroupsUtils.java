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

package org.alfresco.module.org_alfresco_module_rm.test.util;

import static org.junit.Assert.assertNotNull;

import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.role.Role;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.springframework.context.ApplicationContext;

/**
 * Test utils class containing methods for managing user, groups and roles
 *
 * @author Ana Manolache
 * @since 2.6
 */
public class UserAndGroupsUtils
{
    protected FilePlanRoleService filePlanRoleService;
    protected AuthorityService authorityService;

    /**
     * @param applicationContext the application context
     */
    public UserAndGroupsUtils(ApplicationContext applicationContext)
    {
        filePlanRoleService = (FilePlanRoleService) applicationContext.getBean("FilePlanRoleService");
        authorityService = (AuthorityService) applicationContext.getBean("AuthorityService");
    }

    /**
     * Add a user to an RM role
     *
     * @param userName the username of the user to add to the role
     * @param role the role to add the user to
     */
    public void addUserToRole(NodeRef filePlan, String userName, RMRole role)
    {
        // Find the authority for the given role
        Role roleObj = filePlanRoleService.getRole(filePlan, role.getGroupName());
        assertNotNull("Notification role " + role.getGroupName() + " could not be retrieved", roleObj);
        String roleGroup = roleObj.getRoleGroupName();
        assertNotNull("Notification role group " + roleGroup + " can not be null.", roleGroup);

        // Add user to notification role group
        authorityService.addAuthority(roleGroup, userName);
    }

    /**
     * An enum of RM Roles
     */
    public enum RMRole
    {
        RM_ADMINISTRATOR("Administrator"),
        RM_MANAGER("RecordsManager"),
        RM_POWER_USER("PowerUser"),
        RM_SECURITY_OFFICER("SecurityOfficer"),
        RM_USER("User");

        private String groupName;

        private RMRole(String groupName)
        {
            this.groupName = groupName;
        }

        public String getGroupName()
        {
            return this.groupName;
        }
    }
}
