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

package org.alfresco.module.org_alfresco_module_rm.security;

import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.model.security.ModelSecurityService;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Records management permission service interface
 * 
 * @author Roy Wetherall
 * 
 * @deprecated As of release 2.1, replaced by {@link ModelSecurityService}, {@link FilePlanRoleService} and {@link FilePlanPermissionService}
 */
public interface RecordsManagementSecurityService
{    
    /**
     * Creates the initial set of default roles for a root records management node
     * 
     * @param rmRootNode    root node
     * 
     * @deprecated As of release 2.1, operation no longer supported 
     */
    @Deprecated
    void bootstrapDefaultRoles(NodeRef rmRootNode);
    
    /**
     * Returns the name of the container group for all roles of a specified file
     * plan.
     * 
     * @param filePlan  file plan node reference
     * @return String   group name
     * 
     * @deprecated As of release 2.1, replaced by {@link FilePlanRoleService#getAllRolesContainerGroup(NodeRef)}
     */
    @Deprecated
    String getAllRolesContainerGroup(NodeRef filePlan);
    
    /**
     * Get all the available roles for the given records management root node
     * 
     * @param rmRootNode    root node
     * @return {@link Set}&lt;{@link Role}&gt;    all roles for a given root node
     * 
     * @deprecated As of release 2.1, replaced by {@link FilePlanRoleService#getRoles(NodeRef)}
     */
    @Deprecated
    Set<Role> getRoles(NodeRef rmRootNode);
    
    /**
     * Gets the roles for a given user
     * 
     * @param rmRootNode
     * @param user
     * @return
     * 
     * @deprecated As of release 2.1, replaced by {@link FilePlanRoleService#getRolesByUser(NodeRef, String)}
     */
    @Deprecated
    Set<Role> getRolesByUser(NodeRef rmRootNode, String user);
    
    /**
     * Get a role by name
     * 
     * @param rmRootNode
     * @param role
     * @return
     * 
     * @deprecated As of release 2.1, replaced by {@link FilePlanRoleService#getRole(NodeRef, String)}
     */
    @Deprecated
    Role getRole(NodeRef rmRootNode, String role);    
    
    /**
     * Indicate whether a role exists for a given records management root node
     * @param rmRootNode
     * @param role
     * @return
     * 
     * @deprecated As of release 2.1, replaced by {@link FilePlanRoleService#existsRole(NodeRef, String)}
     */
    @Deprecated
    boolean existsRole(NodeRef rmRootNode, String role);
    
    /**
     * Determines whether the given user has the RM Admin role
     * 
     * @param rmRootNode RM root node
     * @param user user name to check
     * @return true if the user has the RM Admin role, false otherwise
     * 
     * @deprecated As of release 2.1, replaced by {@link FilePlanRoleService#hasRMAdminRole(NodeRef, String)}
     */
    @Deprecated
    boolean hasRMAdminRole(NodeRef rmRootNode, String user);
    
    /**
     * Create a new role
     * 
     * @param rmRootNode
     * @param role
     * @param roleDisplayLabel
     * @param capabilities
     * @return
     * 
     * @deprecated As of release 2.1, replaced by {@link FilePlanRoleService#createRole(NodeRef, String, String, Set)}
     */
    @Deprecated
    Role createRole(NodeRef rmRootNode, String role, String roleDisplayLabel, Set<Capability> capabilities);
    
    /**
     * Update an existing role
     * 
     * @param rmRootNode
     * @param role
     * @param roleDisplayLabel
     * @param capabilities
     * @return
     * 
     * @deprecated As of release 2.1, replaced by {@link FilePlanRoleService#updateRole(NodeRef, String, String, Set)}
     */
    @Deprecated
    Role updateRole(NodeRef rmRootNode, String role, String roleDisplayLabel, Set<Capability> capabilities);
    
    /**
     * Delete a role
     * 
     * @param rmRootNode
     * @param role
     * 
     * @deprecated As of release 2.1, replaced by {@link FilePlanRoleService#deleteRole(NodeRef, String)}
     */
    @Deprecated
    void deleteRole(NodeRef rmRootNode, String role);
    
    /**
     * Assign a role to an authority
     * 
     * @param authorityName
     * @param rmRootNode
     * @param role
     * 
     * @deprecated As of release 2.1, replaced by {@link FilePlanRoleService#assignRoleToAuthority(NodeRef, String, String)}
     */
    @Deprecated
    void assignRoleToAuthority(NodeRef rmRootNode, String role, String authorityName);
    
    /**
     * Sets a permission on a RM object.  Assumes allow is true.  Cascades permission down to record folder.  
     * Cascades ReadRecord up to file plan.
     * 
     * @param nodeRef       node reference
     * @param authority     authority 
     * @param permission    permission
     * 
     * @deprecated As of release 2.1, replaced by {@link FilePlanPermissionService#setPermission(NodeRef, String, String)}
     */
    @Deprecated
    void setPermission(NodeRef nodeRef, String authority, String permission);
    
    /**
     * Deletes a permission from a RM object.  Cascades removal down to record folder.
     * 
     * @param nodeRef       node reference
     * @param authority     authority 
     * @param permission    permission
     * 
     * @deprecated As of release 2.1, replaced by {@link FilePlanPermissionService#deletePermission(NodeRef, String, String)}
     */
    @Deprecated
    void deletePermission(NodeRef nodeRef, String authority, String permission);
    
    /**
     * @return  {@link Set}&lt;{@link QName}&gt;  protected aspect names
     * @deprecated As of release 2.1, replaced by {@link ModelSecurityService#getProtectedAspects}
     */
    @Deprecated
    Set<QName> getProtectedAspects();
   
    /**
     * @return {@link Set}&lt;{@link QName}&gt;   protected properties
     * @deprecated  As of release 2.1, replaced by {@link ModelSecurityService#getProtectedProperties}
     */
    Set<QName> getProtectedProperties();
}
