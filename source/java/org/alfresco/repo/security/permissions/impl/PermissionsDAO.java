/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.security.permissions.impl;

import org.alfresco.repo.security.permissions.NodePermissionEntry;
import org.alfresco.repo.security.permissions.PermissionEntry;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * The API for accessing persisted Alfresco permissions.
 * 
 * @author andyh
 */
public interface PermissionsDAO
{
    /**
     * Get the permissions that have been set on a given node.
     * 
     * @param nodeRef
     * @return
     */
    public NodePermissionEntry getPermissions(NodeRef nodeRef);

    /**
     * Delete all the permissions on a given node.
     * The node permission and all the permission entries it contains will be deleted.
     * 
     * @param nodeRef
     */
    public void deletePermissions(NodeRef nodeRef);

    /**
     * Delete all the permissions on a given node.
     * The node permission and all the permission entries it contains will be deleted.
     * 
     * @param nodePermissionEntry
     */
    public void deletePermissions(NodePermissionEntry nodePermissionEntry);

    
    /**
     * Delete as single permission entry.
     * This deleted one permission on the node. It does not affect the persistence of any other permissions.
     * 
     * @param permissionEntry
     */
    public void deletePermissions(PermissionEntry permissionEntry);

    /**
     * 
     * Delete as single permission entry, if a match is found.
     * This deleted one permission on the node. It does not affect the persistence of any other permissions.
     * 
     * @param nodeRef
     * @param authority
     * @param perm
     * @param allow
     */
    public void deletePermissions(NodeRef nodeRef, String authority, PermissionReference perm,  boolean allow);

    /**
     * Set a permission on a node.
     * If the node has no permissions set then a default node permission (allowing inheritance) will be created to
     * contain the permission entry.
     * 
     * @param nodeRef
     * @param authority
     * @param perm
     * @param allow
     */
    public void setPermission(NodeRef nodeRef, String authority, PermissionReference perm, boolean allow);

    /**
     * Create a persisted permission entry given and other representation of a permission entry.
     * 
     * @param permissionEntry
     */
    public void setPermission(PermissionEntry permissionEntry);

    /**
     * Create a persisted node permission entry given a template object from which to copy.
     * 
     * @param nodePermissionEntry
     */
    public void setPermission(NodePermissionEntry nodePermissionEntry);

    /**
     * Set the inheritance behaviour for permissions on a given node.
     * 
     * @param nodeRef
     * @param inheritParentPermissions
     */
    public void setInheritParentPermissions(NodeRef nodeRef, boolean inheritParentPermissions);

    /**
     * Return the inheritance behaviour for permissions on a given node.
     * 
     * @param nodeRef
     * @return inheritParentPermissions
     */
    public boolean getInheritParentPermissions(NodeRef nodeRef);
    
    /**
     * Clear all the permissions set for a given authentication
     * 
     * @param nodeRef
     * @param authority
     */
    public void clearPermission(NodeRef nodeRef, String authority);
    
    /**
     * Remove all permissions for the specvified authority
     * @param authority
     */
    public void deleteAllPermissionsForAuthority(String authority);

}
