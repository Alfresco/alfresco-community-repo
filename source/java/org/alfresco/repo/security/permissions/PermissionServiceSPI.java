/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.security.permissions;

import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
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
     * @param nodeRef
     * @return
     */
    public Set<PermissionReference> getSettablePermissionReferences(QName type);
    
    /**
     * Get the permissions that can be set for a given type
     * 
     * @param nodeRef
     * @return
     */
    public Set<PermissionReference> getSettablePermissionReferences(NodeRef nodeRef);

    /**
     * Get the permissions that have been set on the given node (it knows
     * nothing of the parent permissions)
     * 
     * @param nodeRef
     * @return
     */
    public NodePermissionEntry getSetPermissions(NodeRef nodeRef);

    /**
     * Check that the given authentication has a particular permission for the
     * given node. (The default behaviour is to inherit permissions)
     * 
     * @param nodeRef
     * @param perm
     * @return
     */
    public AccessStatus hasPermission(NodeRef nodeRef, PermissionReference perm);

    /**
     * Where is the permission set that controls the behaviour for the given
     * permission for the given authentication to access the specified name.
     * 
     * @param nodeRef
     * @param auth
     * @param perm
     * @return
     */
    public NodePermissionEntry explainPermission(NodeRef nodeRef, PermissionReference perm);
    
    /**
     * Delete the permissions defined by the nodePermissionEntry
     * @param nodePermissionEntry
     */
    public void deletePermissions(NodePermissionEntry nodePermissionEntry);
    
    /**
     * Delete a single permission entry
     * @param permissionEntry
     */
    public void deletePermission(PermissionEntry permissionEntry);

    /**
     * Add or set a permission entry on a node.
     * 
     * @param permissionEntry
     */
    public void setPermission(PermissionEntry permissionEntry);
    
    /**
     * Set the permissions on a node.
     * 
     * @param nodePermissionEntry
     */
    public void setPermission(NodePermissionEntry nodePermissionEntry);

    /**
     * Get the permission reference for the given data type and permission name.
     * 
     * @param qname - may be null if the permission name is unique
     * @param permissionName
     * @return
     */
    public PermissionReference getPermissionReference(QName qname, String permissionName);
    
    /**
     * Get the permission reference by permission name.
     *
     * @param permissionName
     * @return
     */
    public PermissionReference getPermissionReference(String permissionName);
    
    
    /**
     * Get the string that can be used to identify the given permission reference.
     * 
     * @param permissionReference
     * @return
     */
    public String getPermission(PermissionReference permissionReference);
    
    public void deletePermissions(String recipient);
}
