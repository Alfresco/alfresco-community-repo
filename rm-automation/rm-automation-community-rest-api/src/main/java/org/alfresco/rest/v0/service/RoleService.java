/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.rest.v0.service;

import java.util.HashSet;
import java.util.Set;

import org.alfresco.rest.rm.community.model.user.UserRoles;
import org.alfresco.rest.v0.RMRolesAndActionsAPI;
import org.alfresco.utility.data.DataUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Produces processed results from roles API calls
 *
 * @author Rodica Sutu
 * @since 2.6
 */
@Service
public class RoleService
{
    @Autowired
    private RMRolesAndActionsAPI rmRolesAndActionsAPI;

    @Autowired
    private DataUser dataUser;

    /**
     * Add capabilities to a role
     *
     * @param role         role to be updated
     * @param capabilities list of capabilities to be added
     */
    public void addCapabilitiesToRole(UserRoles role, Set<String> capabilities)
    {
        Set<String> roleCapabilities = new HashSet<>();
        roleCapabilities.addAll(rmRolesAndActionsAPI.getCapabilitiesForRole(dataUser.getAdminUser().getUsername(),
                dataUser.getAdminUser().getPassword(), role.roleId));
        capabilities.stream().forEach(cap -> roleCapabilities.add(cap));

        rmRolesAndActionsAPI.updateRole(dataUser.getAdminUser().getUsername(), dataUser.getAdminUser().getPassword(),
                role.roleId, role.displayName, roleCapabilities);
    }

    /**
     * Remove capabilities from a role
     *
     * @param role         role to be updated
     * @param capabilities list of capabilities to be removed
     */
    public void removeCapabilitiesFromRole(UserRoles role, Set<String> capabilities)
    {
        Set<String> roleCapabilities = rmRolesAndActionsAPI.getCapabilitiesForRole(dataUser.getAdminUser().getUsername(),
                dataUser.getAdminUser().getPassword(), role.roleId);
        roleCapabilities.removeAll(capabilities);
        rmRolesAndActionsAPI.updateRole(dataUser.getAdminUser().getUsername(), dataUser.getAdminUser().getPassword(),
                role.roleId, role.displayName, roleCapabilities);
    }
}
