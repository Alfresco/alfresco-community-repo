/*
 * #%L
 * Alfresco Data model classes
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
package org.alfresco.service.cmr.security;

import java.util.Set;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.Auditable;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;

/**
 * The public API for a permission service The implementation may be changed in the application configuration
 *
 * @author Andy Hind
 */
@AlfrescoPublicApi
public interface PermissionService
{
    /**
     * Prefixes used for authorities of type role. This is intended for external roles, e.g. those set by ACEGI
     * implementations It is only used for admin at the moment - which is done outside the usual permission assignments
     * at the moment. It could be a dynamic authority.
     */
    public static final String ROLE_PREFIX = "ROLE_";

    /**
     * Prefix used for authorities of type group.
     */
    public static final String GROUP_PREFIX = "GROUP_";

    /**
     * The group that contains everyone except guest.
     */
    public static final String ALL_AUTHORITIES = "GROUP_EVERYONE";

    /**
     * The dynamic authority used for ownership
     */
    public static final String OWNER_AUTHORITY = "ROLE_OWNER";

    /**
     * The dynamic authority used for the ownership of locks.
     */
    public static final String LOCK_OWNER_AUTHORITY = "ROLE_LOCK_OWNER";

    /**
     * The admin authority - currently a role.
     */
    public static final String ADMINISTRATOR_AUTHORITY = "ROLE_ADMINISTRATOR";

    /**
     * The guest authority
     */
    public static final String GUEST_AUTHORITY = "ROLE_GUEST";

    /**
     * The permission for all - not defined in the model. Repsected in the code.
     */
    public static final String ALL_PERMISSIONS = "All";

    // Constants for permissions/permission groups defined in the standard permission model.

    public static final String FULL_CONTROL = "FullControl";

    public static final String READ = "Read";

    public static final String WRITE = "Write";

    public static final String DELETE = "Delete";

    public static final String ADD_CHILDREN = "AddChildren";

    public static final String READ_PROPERTIES = "ReadProperties";

    public static final String READ_CHILDREN = "ReadChildren";

    public static final String WRITE_PROPERTIES = "WriteProperties";

    public static final String DELETE_NODE = "DeleteNode";

    public static final String DELETE_CHILDREN = "DeleteChildren";

    public static final String CREATE_CHILDREN = "CreateChildren";

    public static final String LINK_CHILDREN = "LinkChildren";

    public static final String DELETE_ASSOCIATIONS = "DeleteAssociations";

    public static final String READ_ASSOCIATIONS = "ReadAssociations";

    public static final String CREATE_ASSOCIATIONS = "CreateAssociations";

    public static final String READ_PERMISSIONS = "ReadPermissions";

    public static final String CHANGE_PERMISSIONS = "ChangePermissions";

    public static final String EXECUTE = "Execute";

    public static final String READ_CONTENT = "ReadContent";

    public static final String WRITE_CONTENT = "WriteContent";

    public static final String EXECUTE_CONTENT = "ExecuteContent";

    public static final String TAKE_OWNERSHIP = "TakeOwnership";

    public static final String SET_OWNER = "SetOwner";

    public static final String COORDINATOR = "Coordinator";

    public static final String CONTRIBUTOR = "Contributor";

    public static final String EDITOR = "Editor";

    public static final String CONSUMER = "Consumer";

    public static final String LOCK = "Lock";

    public static final String UNLOCK = "Unlock";

    public static final String CHECK_OUT = "CheckOut";

    public static final String CHECK_IN = "CheckIn";

    public static final String CANCEL_CHECK_OUT = "CancelCheckOut";

    public static final String ASPECTS = "Aspects";

    public static final String PROPERTIES = "Properties";

    /**
     * Get the Owner Authority
     *
     * @return the owner authority
     */
    @Auditable
    public String getOwnerAuthority();
    
    /**
     * Get the All Authorities
     *
     * @return the All authorities
     */
    @Auditable
    public String getAllAuthorities();

    /**
     * Get the All Permission
     *
     * @return the All permission
     */
    @Auditable
    public String getAllPermission();

    /**
     * Get all the AccessPermissions that are granted/denied to the current authentication for the given node
     *
     * @param nodeRef -
     *            the reference to the node
     * @return the set of allowed permissions
     */
    @Auditable(parameters = { "nodeRef" })
    public Set<AccessPermission> getPermissions(NodeRef nodeRef);

    /**
     * Get all the AccessPermissions that are set for anyone for the given node
     *
     * @param nodeRef -
     *            the reference to the node
     * @return the set of allowed permissions
     */
    @Auditable(parameters = { "nodeRef" })
    public Set<AccessPermission> getAllSetPermissions(NodeRef nodeRef);

    /**
     * Get the permissions that can be set for a given node
     */
    @Auditable(parameters = { "nodeRef" })
    public Set<String> getSettablePermissions(NodeRef nodeRef);

    /**
     * Get the permissions that can be set for a given type
     *
     * @return - set of permissions
     */
    @Auditable(parameters = { "type" })
    public Set<String> getSettablePermissions(QName type);

    /**
     * Check that the given authentication has a particular permission for the given node. (The default behaviour is to
     * inherit permissions)
     *
     * @return - access status
     */
    @Auditable(parameters = { "nodeRef", "permission" })
    public AccessStatus hasPermission(NodeRef nodeRef, String permission);

    /**
     * Check if read permission is allowed on an acl (optimised)
     *
     * caveats:
     * doesn't take into account dynamic authorities/groups
     * doesn't take into account node types/aspects for permissions
     * 
     * @param nodeRef -
     *            the reference to the node
     * @return access status
     */
    @Auditable(parameters = { "nodeRef" })
    public AccessStatus hasReadPermission(NodeRef nodeRef);

    /**
     * Get the readers associated with a given ACL
     * 
     * @param aclId                 the low-level ACL ID
     * @return                      set of authorities with read permission on the ACL
     */
    @Auditable(parameters = { "aclId" })
    public Set<String> getReaders(Long aclId);
    
    /**
     * Get the denied authorities associated with a given ACL
     * 
     * @param aclId                 the low-level ACL ID
     * @return                      set of authorities denied permission on the ACL
     */
    @Auditable(parameters = { "aclId" })
    public Set<String> getReadersDenied(Long aclId);
    
    /**
     * Check if a permission is allowed on an acl.
     * @return the access status
     */
    @Auditable(parameters = { "aclID", "context", "permission" })
    public AccessStatus hasPermission(Long aclID, PermissionContext context, String permission);

    /**
     * Delete all the permission assigned to the node
     */
    @Auditable(parameters = { "nodeRef" })
    public void deletePermissions(NodeRef nodeRef);

    /**
     * Delete all permission for the given authority.
     *
     * @param authority
     *            (if null then this will match all authorities)
     */
    @Auditable(parameters = { "nodeRef", "authority" })
    public void clearPermission(NodeRef nodeRef, String authority);

    /**
     * Find and delete a access control entry by node, authentication and permission. It is possible to delete
     * <ol>
     * <li> a specific permission;
     * <li> all permissions for an authority (if the permission is null);
     * <li> entries for all authorities that have a specific permission (if the authority is null); and
     * <li> all permissions set for the node (if both the permission and authority are null).
     * </ol>
     *
     * @param nodeRef
     *            the node that the entry applies to
     * @param authority
     *            the authority recipient (if null then this will match all authorities)
     * @param permission
     *            the entry permission (if null then this will match all permissions)
     */
    @Auditable(parameters = { "nodeRef", "authority", "permission" })
    public void deletePermission(NodeRef nodeRef, String authority, String permission);

    /**
     * Set a specific permission on a node.
     */
    @Auditable(parameters = { "nodeRef", "authority", "permission", "allow" })
    public void setPermission(NodeRef nodeRef, String authority, String permission, boolean allow);

    /**
     * Set the global inheritance behaviour for permissions on a node.
     */
    @Auditable(parameters = { "nodeRef", "inheritParentPermissions" })
    public void setInheritParentPermissions(NodeRef nodeRef, boolean inheritParentPermissions);

    /**
    * Set the global inheritance behavior for permissions on a node. If the operation takes 
    * too long and asyncCall parameter set accordingly, fixed ACLs method will be asynchronously called.
    * 
    * @param nodeRef                           node for which inheritance will be set.
    * @param inheritParentPermissions          <tt>true</tt> to inherit parent permissions, <tt>false</tt> otherwise.
    * @param asyncCall                         <tt>true</tt> if fixed ACLs should be asynchronously set when operation execution takes too long,
    *                                          <tt>false</tt> to execute synchronously regardless of execution time.
    */
    @Auditable(parameters = { "nodeRef", "inheritParentPermissions", "asyncCall" })
    public void setInheritParentPermissions(NodeRef nodeRef, boolean inheritParentPermissions, boolean asyncCall);

    /**
     * Return the global inheritance behaviour for permissions on a node.
     */
    @Auditable(parameters = { "nodeRef" })
    public boolean getInheritParentPermissions(NodeRef nodeRef);

   
    /**
     * Add a permission mask to a store
     */
    @Auditable(parameters = { "storeRef", "authority", "permission", "allow" })
    public void setPermission(StoreRef storeRef, String authority, String permission, boolean allow);
    
    /**
     * Remove part of a permission mask on a store
     */
    @Auditable(parameters = { "storeRef", "authority", "permission" })
    public void deletePermission(StoreRef storeRef, String authority, String permission);
    
    /**
     * Clear all permission masks for an authority on a store 
     */
    @Auditable(parameters = { "storeRef", "authority" })
    public void clearPermission(StoreRef storeRef, String authority);
    
    /**
     * Remove all permission mask on a store
     */
    @Auditable(parameters = { "storeRef" })
    public void deletePermissions(StoreRef storeRef);
    
    
    /**
     * Get all the AccessPermissions that are set for anyone for the given node
     *
     * @param storeRef -
     *            the reference to the store
     * @return the set of allowed permissions
     */
    @Auditable(parameters = { "storeRef" })
    public Set<AccessPermission> getAllSetPermissions(StoreRef storeRef);
    
    /**
     * Get the set of authorities for currently authenticated user
     * 
     * @return              a set of authorities applying to the currently-authenticated user
     */
    public Set<String> getAuthorisations();
}
