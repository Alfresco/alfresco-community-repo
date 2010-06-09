/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.security.permissions.impl;

import org.alfresco.repo.security.permissions.AccessControlListProperties;
import org.alfresco.repo.security.permissions.NodePermissionEntry;
import org.alfresco.repo.security.permissions.PermissionEntry;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;

/**
 * The API for accessing persisted Alfresco permissions.
 * 
 * @author andyh
 */
public interface PermissionsDaoComponent 
{
    /**
     * Get the permissions that have been set on a given node.
     * 
     * @return the node permission entry
     */
    public NodePermissionEntry getPermissions(NodeRef nodeRef);

    /**
     * Delete the access control list and all access control entries for the node.
     * 
     * @param nodeRef
     *            the node for which to delete permission
     */
    public void deletePermissions(NodeRef nodeRef);

    /**
     * Remove all permissions for the specified authority
     */
    public void deletePermissions(String authority);

    /**
     * Delete permission entries for the given node and authority
     * 
     * @param nodeRef
     *            the node to query against
     * @param authority
     *            the specific authority to query against
     */
    public void deletePermissions(NodeRef nodeRef, String authority);

    /**
     * Delete as single permission entry, if a match is found. This deleted one permission on the node. It does not
     * affect the persistence of any other permissions.
     * 
     * @param nodeRef
     *            the node with the access control list
     * @param authority
     *            the specific authority to look for
     * @param permission
     *            the permission to look for
     */
    public void deletePermission(NodeRef nodeRef, String authority, PermissionReference permission);

    /**
     * Set a permission on a node. If the node has no permissions set then a default node permission (allowing
     * inheritance) will be created to contain the permission entry.
     */
    public void setPermission(NodeRef nodeRef, String authority, PermissionReference perm, boolean allow);

    /**
     * Create a persisted permission entry given and other representation of a permission entry.
     */
    public void setPermission(PermissionEntry permissionEntry);

    /**
     * Create a persisted node permission entry given a template object from which to copy.
     */
    public void setPermission(NodePermissionEntry nodePermissionEntry);

    /**
     * Set the inheritance behaviour for permissions on a given node.
     */
    public void setInheritParentPermissions(NodeRef nodeRef, boolean inheritParentPermissions);

    /**
     * Return the inheritance behaviour for permissions on a given node.
     * 
     * @return inheritParentPermissions
     */
    public boolean getInheritParentPermissions(NodeRef nodeRef);

    /**
     * Delete entries from a permission mask on a store by authority
     */
    public void deletePermissions(StoreRef storeRef, String authority);

    /**
     * Remove part of a permission mask from a store
     */
    public void deletePermission(StoreRef storeRef, String authority, PermissionReference perm);

    /**
     * Remove all permission masks from a store
     * 
     */
    public void deletePermissions(StoreRef storeRef);

    /**
     * Set part of a permission mask on a store.
     */
    public void setPermission(StoreRef storeRef, String authority, PermissionReference permission, boolean allow);

    /**
     * Get permission masks set on a store
     * 
     * @return the node permission entry
     */
    public NodePermissionEntry getPermissions(StoreRef storeRef);

    /**
     * Get the properties for the access control list
     * 
     * @return the properties for the access control list
     */
    public AccessControlListProperties getAccessControlListProperties(NodeRef nodeRef);
}
