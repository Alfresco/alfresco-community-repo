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
 */
public class PermissionServiceNOOPImpl implements PermissionServiceSPI
{
    @Override
    public String getOwnerAuthority()
    {
        return OWNER_AUTHORITY;
    }

    @Override
    public String getAllAuthorities()
    {
        return ALL_AUTHORITIES;
    }

    @Override
    public String getAllPermission()
    {
        return ALL_PERMISSIONS;
    }

    @Override
    public Set<AccessPermission> getPermissions(NodeRef nodeRef)
    {
        return Collections.<AccessPermission>emptySet();
    }

    @Override
    public Set<AccessPermission> getAllSetPermissions(NodeRef nodeRef)
    {
        return Collections.<AccessPermission>emptySet();
    }

    @Override
    public Set<String> getSettablePermissions(NodeRef nodeRef)
    {
        return getSettablePermissions((QName)null);
    }

    @Override
    public Set<String> getSettablePermissions(QName type)
    {
        HashSet<String> permissions = new HashSet<String>();
        permissions.add(ALL_PERMISSIONS);
        return permissions;
    }

    @Override
    public AccessStatus hasPermission(NodeRef nodeRef, String perm)
    {
        return AccessStatus.ALLOWED;
    }

    @Override
    public void deletePermissions(NodeRef nodeRef)
    {
        // Do Nothing.
    }

    @Override
    public void deletePermission(NodeRef nodeRef, String authority, String perm)
    {
        // Do Nothing.
    }

    @Override
    public void setPermission(NodeRef nodeRef, String authority, String perm, boolean allow)
    {
        // Do Nothing.
    }

    @Override
    public void setInheritParentPermissions(NodeRef nodeRef, boolean inheritParentPermissions)
    {
        // Do Nothing.
    }

    @Override
    public boolean getInheritParentPermissions(NodeRef nodeRef)
    {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void clearPermission(NodeRef nodeRef, String authority)
    {
        // Do Nothing.
    }

    @Override
    public void deletePermission(PermissionEntry permissionEntry)
    {
        // Do Nothing.
    }

    @Override
    public void deletePermissions(NodePermissionEntry nodePermissionEntry)
    {
        // Do Nothing.
    }

    @Override
    public void deletePermissions(String recipient)
    {
        // Do Nothing.
    }

    @Override
    public NodePermissionEntry explainPermission(NodeRef nodeRef, PermissionReference perm)
    {
       throw new UnsupportedOperationException();
    }

    @Override
    public PermissionReference getAllPermissionReference()
    {
        return getPermissionReference(ALL_PERMISSIONS);
    }

    @Override
    public String getPermission(PermissionReference permissionReference)
    {
        return permissionReference.toString();
    }

    @Override
    public PermissionReference getPermissionReference(QName qname, String permissionName)
    {
        return PermissionReferenceImpl.getPermissionReference(qname, permissionName);
    }

    @Override
    public PermissionReference getPermissionReference(String permissionName)
    {
        return PermissionReferenceImpl.getPermissionReference(QName.createQName("uri", "local"), permissionName);
    }

    @Override
    public NodePermissionEntry getSetPermissions(NodeRef nodeRef)
    {
        return new SimpleNodePermissionEntry(nodeRef, true, Collections.<PermissionEntry>emptyList());
    }

    @Override
    public Set<PermissionReference> getSettablePermissionReferences(NodeRef nodeRef)
    {
        return Collections.<PermissionReference>emptySet();
    }

    @Override
    public Set<PermissionReference> getSettablePermissionReferences(QName type)
    {
        return Collections.<PermissionReference>emptySet();
    }

    @Override
    public AccessStatus hasPermission(NodeRef nodeRef, PermissionReference perm)
    {
        return AccessStatus.ALLOWED;
    }

    @Override
    public AccessStatus hasPermission(Long aclID, PermissionContext context, String permission)
    {
        return AccessStatus.ALLOWED;
    }

    @Override
    public Set<String> getReaders(Long aclId)
    {
        return Collections.emptySet();
    }

    @Override
    public void setPermission(NodePermissionEntry nodePermissionEntry)
    {
        // Do Nothing.
    }

    @Override
    public void setPermission(PermissionEntry permissionEntry)
    {
        // Do Nothing.
    }

    @Override
    public void clearPermission(StoreRef storeRef, String authority)
    {
        // Do Nothing.
    }

    @Override
    public void deletePermission(StoreRef storeRef, String authority, String permission)
    {
        // Do Nothing.
    }

    @Override
    public void deletePermissions(StoreRef storeRef)
    {
        // Do Nothing.
    }

    @Override
    public void setPermission(StoreRef storeRef, String authority, String permission, boolean allow)
    {
        // Do Nothing.
    }

    @Override
    public Set<AccessPermission> getAllSetPermissions(StoreRef storeRef)
    {
        return Collections.emptySet();
    }

    @Override
    public NodePermissionEntry getSetPermissions(StoreRef storeRef)
    {
        return new SimpleNodePermissionEntry(null, true, Collections.<PermissionEntry>emptyList());
    }

    @Override
    public AccessStatus hasReadPermission(NodeRef nodeRef)
    {
        return AccessStatus.ALLOWED;
    }

    @Override
	public Set<String> getAuthorisations()
	{
		return new HashSet<String>();
	}
}
