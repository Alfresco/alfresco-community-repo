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
package org.alfresco.rest.v0.service;

import static lombok.AccessLevel.PROTECTED;
import static org.springframework.http.HttpStatus.OK;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import org.alfresco.rest.core.RestAPIFactory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.user.UserPermissions;
import org.alfresco.rest.rm.community.model.user.UserRoles;
import org.alfresco.rest.v0.RMRolesAndActionsAPI;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUserAIS;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;
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
    @Getter (value = PROTECTED)
    private RMRolesAndActionsAPI rmRolesAndActionsAPI;

    @Autowired
    @Getter (value = PROTECTED)
    private DataUserAIS dataUser;

    @Autowired
    @Getter (value = PROTECTED)
    private RestAPIFactory restAPIFactory;

    /**
     * Get the capabilities for a role
     *
     * @param roleName the role name
     * @return the list of capabilities
     */
    public Set<String> getRoleCapabilities(String roleName)
    {
        return getRmRolesAndActionsAPI().getCapabilitiesForRole(getDataUser().getAdminUser().getUsername(),
                getDataUser().getAdminUser().getPassword(), roleName);
    }

    /**
     * Add capabilities to a role
     *
     * @param role         role to be updated
     * @param capabilities list of capabilities to be added
     */
    public void addCapabilitiesToRole(UserRoles role, Set<String> capabilities)
    {
        final Set<String> roleCapabilities = new HashSet<>(getRoleCapabilities(role.roleId));
        roleCapabilities.addAll(capabilities);

        getRmRolesAndActionsAPI().updateRole(getDataUser().getAdminUser().getUsername(), getDataUser().getAdminUser().getPassword(),
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
        final Set<String> roleCapabilities = getRoleCapabilities(role.roleId);
        roleCapabilities.removeAll(capabilities);
        getRmRolesAndActionsAPI().updateRole(getDataUser().getAdminUser().getUsername(), getDataUser().getAdminUser().getPassword(),
                role.roleId, role.displayName, roleCapabilities);
    }

    /**
     * Assign permission on a record category and give the user RM role
     *
     * @param user           the user to assign rm role and permissions
     * @param categoryId     the id of the category to assign permissions for
     * @param userPermission the permissions to be assigned to the user
     * @param userRole       the rm role to be assigned to the user
     */
    public void assignUserPermissionsOnCategoryAndRMRole(UserModel user, String categoryId, UserPermissions userPermission,
                                                         String userRole)
    {
        getRestAPIFactory().getRMUserAPI().addUserPermission(categoryId, user, userPermission);
        getRmRolesAndActionsAPI().assignRoleToUser(getDataUser().getAdminUser().getUsername(), getDataUser().getAdminUser().getPassword(),
                user.getUsername(), userRole);
    }

    /**
     * Helper method to create a test user with rm role
     *
     * @param userRole the rm role
     * @return the created user model
     */
    public UserModel createUserWithRMRole(String userRole)
    {
        final UserModel rmUser = getDataUser().createRandomTestUser();
        getRestAPIFactory().getRMUserAPI().assignRoleToUser(rmUser.getUsername(), userRole);
        getRestAPIFactory().getRmRestWrapper().assertStatusCodeIs(OK);
        return rmUser;
    }

    /**
     * Helper method to create a test user with rm role and permissions over the record category
     *
     * @param userRole       the rm role
     * @param userPermission the permissions over the record category
     * @param recordCategory the category on which user has permissions
     * @return the created user model
     */
    public UserModel createUserWithRMRoleAndCategoryPermission(String userRole, RecordCategory recordCategory,
                                                                  UserPermissions userPermission)
    {
        return createUserWithRMRoleAndRMNodePermission(userRole, recordCategory.getId(), userPermission);
    }

    /**
     * Helper method to create a user with rm role and permissions on the node ref
     *
     * @param userRole       the rm role
     * @param userPermission the permissions over the rm node
     * @param componentId the node id to grant rm permission
     * @return the created user model
     */
    public UserModel createUserWithRMRoleAndRMNodePermission(String userRole, String componentId,
                                                               UserPermissions userPermission)
    {
        final UserModel rmUser = createUserWithRMRole(userRole);
        getRestAPIFactory().getRMUserAPI().addUserPermission(componentId, rmUser, userPermission);
        getRestAPIFactory().getRmRestWrapper().assertStatusCodeIs(OK);
        return rmUser;
    }

    /**
     * Helper method to create a  user with rm role and permissions over the recordCategory and collaborator role
     * in collaboration site
     *
     * @param siteModel collaboration site
     * @param recordCategory  the category  on which permission should be given
     * @param userRole       the rm role
     * @param userPermission the permissions over the recordCategory
     * @return the created user model
     */
    public UserModel createCollaboratorWithRMRoleAndPermission(SiteModel siteModel, RecordCategory recordCategory,
                                                                UserRoles userRole, UserPermissions userPermission)
    {
        return createUserWithSiteRoleRMRoleAndPermission(siteModel, UserRole.SiteCollaborator, recordCategory.getId(),
                                                        userRole, userPermission);
    }

    /**
     * Helper method to create a test user with a rm role and permissions over a rm component and a role
     * in collaboration site
     *
     * @param siteModel      collaboration site
     * @param userSiteRole   user role in the collaboration site
     * @param rmNodeId       rm node id to grant rm permission
     * @param userRole       the rm role
     * @param userPermission the permissions over the rmNodeId
     * @return the created user model
     */
    public UserModel createUserWithSiteRoleRMRoleAndPermission(SiteModel siteModel, UserRole userSiteRole,
                                                               String rmNodeId, UserRoles userRole,
                                                               UserPermissions userPermission)
    {
        final UserModel rmUser = createUserWithRMRoleAndRMNodePermission(userRole.roleId, rmNodeId,
                userPermission);
        getDataUser().addUserToSite(rmUser, siteModel, userSiteRole);
        return rmUser;
    }
}
