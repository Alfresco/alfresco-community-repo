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
package org.alfresco.repo.security.person;

import org.alfresco.repo.security.permissions.impl.AccessPermissionImpl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;

import java.util.Map;
import java.util.Set;

public class PermissionsManagerImpl implements PermissionsManager
{

    /**
     * Set if permissions are inherited when nodes are created.
     */
    private Boolean inheritPermissions;

    /**
     * A set of permissions to set for the owner when a home folder is created
     */
    private Set<String> ownerPermissions;

    /**
     * General permissions to set on the node Map<(String)uid, Set<(String)permission>>.
     */
    private Map<String, Set<String>> permissions;

    /**
     * Permissions to set for the user - on create and reference.
     */
    private Set<String> userPermissions;

    /**
     * Clear existing permissions on new home folders (useful of created from a template.
     */
    private Boolean clearExistingPermissions;

    private OwnableService ownableService;

    private PermissionService permissionService;

    public boolean getInheritPermissions()
    {
        return inheritPermissions;
    }

    public void setInheritPermissions(boolean inheritPermissions)
    {
        this.inheritPermissions = inheritPermissions;
    }

    public Set<String> getOwnerPermissions()
    {
        return ownerPermissions;
    }

    public void setOwnerPermissions(Set<String> ownerPermissions)
    {
        this.ownerPermissions = ownerPermissions;
    }

    public Map<String, Set<String>> getPermissions()
    {
        return permissions;
    }

    public void setPermissions(Map<String, Set<String>> permissions)
    {
        this.permissions = permissions;
    }

    public Set<String> getUserPermissions()
    {
        return userPermissions;
    }

    public void setUserPermissions(Set<String> userPermissions)
    {
        this.userPermissions = userPermissions;
    }

    public boolean getClearExistingPermissions()
    {
        return clearExistingPermissions;
    }

    public void setClearExistingPermissions(boolean clearExistingPermissions)
    {
        this.clearExistingPermissions = clearExistingPermissions;
    }

    public void setOwnableService(OwnableService ownableService)
    {
        this.ownableService = ownableService;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public void setPermissions(NodeRef nodeRef, String owner, String user)
    {
        // Set to a specified owner
        if (owner != null)
        {
            ownableService.setOwner(nodeRef, owner);
        }

        // clear permissions - useful of not required from a template

        if ((clearExistingPermissions != null) && clearExistingPermissions.booleanValue())
        {
            permissionService.deletePermissions(nodeRef);
        }

        // inherit permissions

        if (inheritPermissions != null)
        {
            permissionService.setInheritParentPermissions(nodeRef, inheritPermissions.booleanValue());
        }

        // Set owner permissions

        if (ownerPermissions != null)
        {
            for (String permission : ownerPermissions)
            {
                permissionService.setPermission(nodeRef, PermissionService.OWNER_AUTHORITY, permission, true);
            }
        }

        // Add other permissions

        if (permissions != null)
        {
            for (String userForPermission : permissions.keySet())
            {
                Set<String> set = permissions.get(userForPermission);
                if (set != null)
                {
                    for (String permission : set)
                    {
                        permissionService.setPermission(nodeRef, userForPermission, permission, true);
                    }
                }
            }
        }

        // Add user permissions on create and reference

        if (userPermissions != null)
        {
            for (String permission : userPermissions)
            {
                permissionService.setPermission(nodeRef, user, permission, true);
            }
        }

    }

    public boolean validatePermissions(NodeRef nodeRef, String owner, String user)
    {
        if (owner != null)
        {
            String setOwner = ownableService.getOwner(nodeRef);
            if (!owner.equals(setOwner))
            {
                return false;
            }
        }

        // inherit permissions

        if (inheritPermissions != null)
        {
            if (inheritPermissions != permissionService.getInheritParentPermissions(nodeRef))
            {
                return false;
            }
        }

        Set<AccessPermission> setPermissions = permissionService.getAllSetPermissions(nodeRef);

        if (ownerPermissions != null)
        {
            for (String permission : ownerPermissions)
            {
                AccessPermission required = new AccessPermissionImpl(permission, AccessStatus.ALLOWED, PermissionService.OWNER_AUTHORITY, 0);
                if (!setPermissions.contains(required))
                {
                    return false;
                }
            }
        }

        // Add other permissions

        if (permissions != null)
        {
            for (String userForPermission : permissions.keySet())
            {
                Set<String> set = permissions.get(userForPermission);
                if (set != null)
                {
                    for (String permission : set)
                    {
                        AccessPermission required = new AccessPermissionImpl(permission, AccessStatus.ALLOWED, userForPermission, 0);
                        if (!setPermissions.contains(required))
                        {
                            return false;
                        }
                    }
                }
            }
        }

        if (userPermissions != null)
        {
            for (String permission : userPermissions)
            {
                AccessPermission required = new AccessPermissionImpl(permission, AccessStatus.ALLOWED, user, 0);
                if (!setPermissions.contains(required))
                {
                    return false;
                }
            }
        }

        // TODO: Check we have no extras if we should have cleared permissions ... ??

        return true;
    }

}
