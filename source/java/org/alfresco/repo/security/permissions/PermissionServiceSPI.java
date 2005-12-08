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
