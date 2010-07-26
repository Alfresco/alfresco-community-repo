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
package org.alfresco.repo.security.permissions.noop;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.security.permissions.NodePermissionEntry;
import org.alfresco.repo.security.permissions.PermissionEntry;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.PermissionServiceSPI;
import org.alfresco.repo.security.permissions.impl.PermissionReferenceImpl;
import org.alfresco.repo.security.permissions.impl.SimpleNodePermissionEntry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionContext;
import org.alfresco.service.namespace.QName;


/**
 * Dummy implementation of Permissions Service
 *
 */
public class PermissionServiceNOOPImpl
    implements PermissionServiceSPI
{

    /* (non-Javadoc)
     * @see org.alfresco.repo.security.permissions.PermissionService#getOwnerAuthority()
     */
    public String getOwnerAuthority()
    {
        return OWNER_AUTHORITY;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.security.permissions.PermissionService#getAllAuthorities()
     */
    public String getAllAuthorities()
    {
        return ALL_AUTHORITIES;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.security.permissions.PermissionService#getAllPermission()
     */
    public String getAllPermission()
    {
        return ALL_PERMISSIONS;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.security.permissions.PermissionService#getPermissions(org.alfresco.service.cmr.repository.NodeRef)
     */
    public Set<AccessPermission> getPermissions(NodeRef nodeRef)
    {
        return Collections.<AccessPermission>emptySet();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.security.permissions.PermissionService#getAllPermissions(org.alfresco.service.cmr.repository.NodeRef)
     */
    public Set<AccessPermission> getAllSetPermissions(NodeRef nodeRef)
    {
        return Collections.<AccessPermission>emptySet();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.security.permissions.PermissionService#getSettablePermissions(org.alfresco.service.cmr.repository.NodeRef)
     */
    public Set<String> getSettablePermissions(NodeRef nodeRef)
    {
        return getSettablePermissions((QName)null);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.security.permissions.PermissionService#getSettablePermissions(org.alfresco.service.namespace.QName)
     */
    public Set<String> getSettablePermissions(QName type)
    {
        HashSet<String> permissions = new HashSet<String>();
        permissions.add(ALL_PERMISSIONS);
        return permissions;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.security.permissions.PermissionService#hasPermission(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.repo.security.permissions.PermissionReference)
     */
    public AccessStatus hasPermission(NodeRef nodeRef, String perm)
    {
        return AccessStatus.ALLOWED;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.security.permissions.PermissionService#deletePermissions(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void deletePermissions(NodeRef nodeRef)
    {
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.security.permissions.PermissionService#deletePermission(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, org.alfresco.repo.security.permissions.PermissionReference, boolean)
     */
    public void deletePermission(NodeRef nodeRef, String authority, String perm)
    {
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.security.permissions.PermissionService#setPermission(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, org.alfresco.repo.security.permissions.PermissionReference, boolean)
     */
    public void setPermission(NodeRef nodeRef, String authority, String perm, boolean allow)
    {
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.security.permissions.PermissionService#setInheritParentPermissions(org.alfresco.service.cmr.repository.NodeRef, boolean)
     */
    public void setInheritParentPermissions(NodeRef nodeRef, boolean inheritParentPermissions)
    {
    }

    /* (non-Javadoc)
    * @see org.alfresco.service.cmr.security.PermissionService#getInheritParentPermissions(org.alfresco.service.cmr.repository.NodeRef)
    */
   public boolean getInheritParentPermissions(NodeRef nodeRef)
   {
      // TODO Auto-generated method stub
      return true;
   }

   public void clearPermission(NodeRef nodeRef, String authority)
    {

    }

    // SPI

    public void deletePermission(PermissionEntry permissionEntry)
    {

    }

    public void deletePermissions(NodePermissionEntry nodePermissionEntry)
    {

    }

    public void deletePermissions(String recipient)
    {

    }

    public NodePermissionEntry explainPermission(NodeRef nodeRef, PermissionReference perm)
    {
       throw new UnsupportedOperationException();
    }

    public PermissionReference getAllPermissionReference()
    {
        return getPermissionReference(ALL_PERMISSIONS);
    }

    public String getPermission(PermissionReference permissionReference)
    {
        return permissionReference.toString();
    }

    public PermissionReference getPermissionReference(QName qname, String permissionName)
    {
        return PermissionReferenceImpl.getPermissionReference(qname, permissionName);
    }

    public PermissionReference getPermissionReference(String permissionName)
    {
        return PermissionReferenceImpl.getPermissionReference(QName.createQName("uri", "local"), permissionName);
    }

    public NodePermissionEntry getSetPermissions(NodeRef nodeRef)
    {
        return new SimpleNodePermissionEntry(nodeRef, true, Collections.<PermissionEntry>emptyList());
    }

    public Set<PermissionReference> getSettablePermissionReferences(NodeRef nodeRef)
    {
        return Collections.<PermissionReference>emptySet();
    }

    public Set<PermissionReference> getSettablePermissionReferences(QName type)
    {
        return Collections.<PermissionReference>emptySet();
    }

    public AccessStatus hasPermission(NodeRef nodeRef, PermissionReference perm)
    {
        return AccessStatus.ALLOWED;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.security.PermissionService#hasPermission(java.lang.Long, java.lang.String, java.lang.String)
     */
    public AccessStatus hasPermission(Long aclID, PermissionContext context,
                                      String permission)
    {
        return AccessStatus.ALLOWED;
    }

    public void setPermission(NodePermissionEntry nodePermissionEntry)
    {
        
    }

    public void setPermission(PermissionEntry permissionEntry)
    {
        
    }

    public Map<NodeRef, Set<AccessPermission>> getAllSetPermissionsForCurrentUser()
    {
        return Collections.<NodeRef, Set<AccessPermission>>emptyMap();
    }

    public Map<NodeRef, Set<AccessPermission>> getAllSetPermissionsForAuthority(String authority)
    {
        return Collections.<NodeRef, Set<AccessPermission>>emptyMap();
    }

    public Set<NodeRef> findNodesByAssignedPermissionForCurrentUser(String permission, boolean allow, boolean includeContainingAuthorities, boolean exactPermissionMatch)
    {
        return Collections.<NodeRef>emptySet();
    }

    public Set<NodeRef> findNodesByAssignedPermission(String authority, String permission, boolean allow, boolean includeContainingAuthorities, boolean exactPermissionMatch)
    {
        return Collections.<NodeRef>emptySet();
    }

    public void clearPermission(StoreRef storeRef, String authority)
    {
        
    }

    public void deletePermission(StoreRef storeRef, String authority, String permission)
    {
        
    }

    public void deletePermissions(StoreRef storeRef)
    {
        
    }

    public void setPermission(StoreRef storeRef, String authority, String permission, boolean allow)
    {
        
    }

    public Set<AccessPermission> getAllSetPermissions(StoreRef storeRef)
    {
        return Collections.<AccessPermission>emptySet();
    }

    public NodePermissionEntry getSetPermissions(StoreRef storeRef)
    {
        return new SimpleNodePermissionEntry(null, true, Collections.<PermissionEntry>emptyList());
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.security.PermissionService#canRead(java.lang.Long)
     */
    public AccessStatus hasReadPermission(NodeRef nodeRef)
    {
        return AccessStatus.ALLOWED;
    }

	@Override
	public Set<String> getAuthorisations() {
		// TODO Auto-generated method stub
		return new HashSet<String>();
	}
}
