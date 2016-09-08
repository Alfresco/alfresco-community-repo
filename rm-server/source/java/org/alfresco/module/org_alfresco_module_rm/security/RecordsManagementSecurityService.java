/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.security;

import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Records management permission service interface
 * 
 * @author Roy Wetherall
 */
public interface RecordsManagementSecurityService
{    
    /**
     * Get the set of aspect QNames which can not be added direct via the public node service;
     * they must be managed via the appropriate actions.
     * @return
     */
    Set<QName> getProtectedAspects();
    
    /**
     * Get the set of property QNames which can not be added, updated or removed direct via the public node service;
     * they must be managed via the appropriate actions.
     * @return
     */
    Set<QName> getProtectedProperties();
    
    /**
     * Creates the initial set of default roles for a root records management node
     * 
     * @param rmRootNode  
     */
    void bootstrapDefaultRoles(NodeRef rmRootNode);
    
    /**
     * Get all the available roles for the given records management root node
     * 
     * @param rmRootNode
     * @return
     */
    Set<Role> getRoles(NodeRef rmRootNode);
    
    /**
     * Gets the roles for a given user
     * 
     * @param rmRootNode
     * @param user
     * @return
     */
    Set<Role> getRolesByUser(NodeRef rmRootNode, String user);
    
    /**
     * Get a role by name
     * 
     * @param rmRootNode
     * @param role
     * @return
     */
    Role getRole(NodeRef rmRootNode, String role);    
    
    /**
     * Indicate whether a role exists for a given records management root node
     * @param rmRootNode
     * @param role
     * @return
     */
    boolean existsRole(NodeRef rmRootNode, String role);
    
    /**
     * Determines whether the given user has the RM Admin role
     * 
     * @param rmRootNode RM root node
     * @param user user name to check
     * @return true if the user has the RM Admin role, false otherwise
     */
    boolean hasRMAdminRole(NodeRef rmRootNode, String user);
    
    /**
     * Create a new role
     * 
     * @param rmRootNode
     * @param role
     * @param roleDisplayLabel
     * @param capabilities
     * @return
     */
    Role createRole(NodeRef rmRootNode, String role, String roleDisplayLabel, Set<Capability> capabilities);
    
    /**
     * Update an existing role
     * 
     * @param rmRootNode
     * @param role
     * @param roleDisplayLabel
     * @param capabilities
     * @return
     */
    Role updateRole(NodeRef rmRootNode, String role, String roleDisplayLabel, Set<Capability> capabilities);
    
    /**
     * Delete a role
     * 
     * @param rmRootNode
     * @param role
     */
    void deleteRole(NodeRef rmRootNode, String role);
    
    /**
     * Assign a role to an authority
     * 
     * @param authorityName
     * @param rmRootNode
     * @param role
     */
    void assignRoleToAuthority(NodeRef rmRootNode, String role, String authorityName);
    
    /**
     * Sets a permission on a RM object.  Assumes allow is true.  Cascades permission down to record folder.  
     * Cascades ReadRecord up to file plan.
     * 
     * @param nodeRef       node reference
     * @param authority     authority 
     * @param permission    permission
     */
    void setPermission(NodeRef nodeRef, String authority, String permission);
    
    /**
     * Deletes a permission from a RM object.  Cascades removal down to record folder.
     * 
     * @param nodeRef       node reference
     * @param authority     authority 
     * @param permission    permission
     */
    void deletePermission(NodeRef nodeRef, String authority, String permission);
}
