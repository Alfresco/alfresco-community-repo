/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.security.permissions;

import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;

/**
 * The public API for a permission service
 * 
 * The implementation may be changed in the application configuration
 * 
 * @author Andy Hind
 */
public interface PermissionServiceSPI extends PermissionService
{
    /**
     * Get the All Permission
     * 
     * @return the All permission
     */
    public PermissionReference getAllPermissionReference();

    /**
     * Get the permissions that can be set for a given type
     * 
     * @param type
     *            QName
     * @return the set of permissions
     */
    public Set<PermissionReference> getSettablePermissionReferences(QName type);

    /**
     * Get the permissions that can be set for a given type
     * 
     * @param nodeRef
     *            NodeRef
     * @return the set of permissions
     */
    public Set<PermissionReference> getSettablePermissionReferences(NodeRef nodeRef);

    /**
     * Get the permissions that have been set on the given node (it knows nothing of the parent permissions)
     * 
     * @param nodeRef
     *            NodeRef
     * @return the node permission entry
     */
    public NodePermissionEntry getSetPermissions(NodeRef nodeRef);

    /**
     * Check that the given authentication has a particular permission for the given node. (The default behaviour is to inherit permissions)
     * 
     * @param nodeRef
     *            NodeRef
     * @param perm
     *            PermissionReference
     * @return the access status
     */
    public AccessStatus hasPermission(NodeRef nodeRef, PermissionReference perm);

    /**
     * Where is the permission set that controls the behaviour for the given permission for the given authentication to access the specified name.
     * 
     * @param nodeRef
     *            NodeRef
     * @param perm
     *            PermissionReference
     * @return the node permission entry
     */
    public NodePermissionEntry explainPermission(NodeRef nodeRef, PermissionReference perm);

    /**
     * Delete the permissions defined by the nodePermissionEntry
     * 
     * @param nodePermissionEntry
     *            NodePermissionEntry
     */
    public void deletePermissions(NodePermissionEntry nodePermissionEntry);

    /**
     * Delete a single permission entry
     * 
     * @param permissionEntry
     *            PermissionEntry
     */
    public void deletePermission(PermissionEntry permissionEntry);

    /**
     * Add or set a permission entry on a node.
     *
     * @param permissionEntry
     *            PermissionEntry
     */
    public void setPermission(PermissionEntry permissionEntry);

    /**
     * Set the permissions on a node.
     * 
     * @param nodePermissionEntry
     *            NodePermissionEntry
     */
    public void setPermission(NodePermissionEntry nodePermissionEntry);

    /**
     * Get the permission reference for the given data type and permission name.
     * 
     * @param qname
     *            - may be null if the permission name is unique
     * @param permissionName
     *            String
     * @return the permission reference
     */
    public PermissionReference getPermissionReference(QName qname, String permissionName);

    /**
     * Get the permission reference by permission name.
     *
     * @param permissionName
     *            String
     * @return the permission reference
     */
    public PermissionReference getPermissionReference(String permissionName);

    /**
     * Get the string that can be used to identify the given permission reference.
     * 
     * @param permissionReference
     *            PermissionReference
     * @return the permission short name
     */
    public String getPermission(PermissionReference permissionReference);

    /**
     * Delete permissions for the given recipient.
     * 
     * @param recipient
     *            String
     */
    public void deletePermissions(String recipient);

    /**
     * Get the permissions set for the store
     * 
     * @param storeRef
     *            StoreRef
     * @return - the node permission entry
     */
    public NodePermissionEntry getSetPermissions(StoreRef storeRef);
}
