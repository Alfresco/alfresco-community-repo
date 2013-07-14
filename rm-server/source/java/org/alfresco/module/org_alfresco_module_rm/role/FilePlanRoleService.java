/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.role;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Role service interface
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public interface FilePlanRoleService
{
    /** Default role names */
    public static final String ROLE_USER                = "User";
    public static final String ROLE_POWER_USER          = "PowerUser";
    public static final String ROLE_SECURITY_OFFICER    = "SecurityOfficer";
    public static final String ROLE_RECORDS_MANAGER     = "RecordsManager";
    public static final String ROLE_ADMIN               = "Administrator";
    public static final String ROLE_EXTENDED_READERS    = "ExtendedReaders";
    public static final String ROLE_EXTENDED_WRITERS    = "ExtendedWriters";

    /** System roles */
    public static final List<String> SYSTEM_ROLES = Arrays.asList(
        ROLE_EXTENDED_READERS,
        ROLE_EXTENDED_WRITERS
    );

    /**
     * Returns the name of the container group for all roles of a specified file
     * plan.
     *
     * @param filePlan  file plan node reference
     * @return String   group name
     */
    String getAllRolesContainerGroup(NodeRef filePlan);

    /**
     * Get all the available roles for the given records management root node
     * (includes also the system roles)
     *
     * @param filePlan  file plan
     * @return
     */
    Set<Role> getRoles(NodeRef filePlan);

    /**
     * Get all the available roles for the given records management root node.
     * System roles can be filtered
     *
     * @param filePlan  file plan
     * @param includeSystemRoles system roles
     * @return
     */
    Set<Role> getRoles(NodeRef filePlan, boolean includeSystemRoles);

    /**
     * Gets the roles for a given user
     * (includes also the system roles)
     *
     * @param filePlan  file plan
     * @param user      user
     * @return
     */
    Set<Role> getRolesByUser(NodeRef filePlan, String user);

    /**
     * Gets the roles for a given user.
     * System roles can be filtered
     *
     * @param filePlan  file plan
     * @param user      user
     * @param includeSystemRoles system roles
     * @return
     */
    Set<Role> getRolesByUser(NodeRef filePlan, String user, boolean includeSystemRoles);

    /**
     * Get a role by name
     *
     * @param filePlan  file plan
     * @param role      role
     * @return
     */
    Role getRole(NodeRef filePlan, String role);

    /**
     * Indicate whether a role exists for a given records management root node
     * @param filePlan  file plan
     * @param role      role
     * @return
     */
    boolean existsRole(NodeRef filePlan, String role);

    /**
     * Determines whether the given user has the RM Admin role
     *
     * @param filePlan  filePlan
     * @param user user name to check
     * @return true if the user has the RM Admin role, false otherwise
     */
    boolean hasRMAdminRole(NodeRef filePlan, String user);

    /**
     * Create a new role
     *
     * @param filePlan  file plan
     * @param role
     * @param roleDisplayLabel
     * @param capabilities
     * @return
     */
    Role createRole(NodeRef filePlan, String role, String roleDisplayLabel, Set<Capability> capabilities);

    /**
     * Update an existing role
     *
     * @param filePlan  file plan
     * @param role
     * @param roleDisplayLabel
     * @param capabilities
     * @return
     */
    Role updateRole(NodeRef filePlan, String role, String roleDisplayLabel, Set<Capability> capabilities);

    /**
     * Delete a role
     *
     * @param filePlan  file plan
     * @param role      role
     */
    void deleteRole(NodeRef filePlan, String role);

    /**
     * Gets all the users that have been directly assigned to a role.
     *
     * @param filePlan  file plan
     * @param role      role
     * @return {@link Set}<{@link String}>  set of users
     */
    Set<String> getUsersAssignedToRole(NodeRef filePlan, String role);

    /**
     * Gets all the groups that have been directly assigned to a role.
     *
     * @param filePlan  file plan
     * @param role      role
     * @return {@link Set}<{@link String}>  set of groups
     */
    Set<String> getGroupsAssignedToRole(NodeRef filePlan, String role);

    /**
     * Gets all the groups and users that have been directly assigned to a role.
     *
     * @param filePlan  file plan
     * @param role      role
     * @return {@link Set}<{@link String}>  set of groups and users
     */
    Set<String> getAllAssignedToRole(NodeRef filePlan, String role);

    /**
     * Assign a role to an authority
     *
     * @param filePlan      file plan
     * @param role          role
     * @param authorityName authority name
     */
    void assignRoleToAuthority(NodeRef filePlan, String role, String authorityName);


    /**
     * Unassign a role from an authority
     *
     * @param filePlan      file plan
     * @param role          role
     * @param authorityName authority name
     */
    void unassignRoleFromAuthority(NodeRef filePlan, String role, String authorityName);
}
