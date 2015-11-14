/* 
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */

package org.alfresco.repo.virtual.store;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.QName;

/**
 * Configuration bean for virtual references overridden permissions.
 *
 * @author Dinuta Silviu
 */
public class VirtualUserPermissions
{
    private Set<String> allowVirtualNodes = Collections.emptySet();

    private Set<String> allowVirtualNodesFull = Collections.emptySet();

    private Set<String> denyVirtualNodes = Collections.emptySet();

    private Set<String> denyVirtualNodesFull = Collections.emptySet();

    private Set<String> denyReadonlyVirtualNodes = Collections.emptySet();

    private Set<String> denyReadonlyVirtualNodesFull = Collections.emptySet();

    private Set<String> allowQueryNodes = Collections.emptySet();

    private Set<String> allowQueryNodesFull = Collections.emptySet();

    private Set<String> denyQueryNodes = Collections.emptySet();

    private Set<String> denyQueryNodesFull = Collections.emptySet();

    private QName permissionTypeQName = ContentModel.TYPE_BASE;

    public VirtualUserPermissions()
    {

    }

    public VirtualUserPermissions(VirtualUserPermissions userPermissions)
    {
        this.allowVirtualNodes = new HashSet<>(userPermissions.allowVirtualNodes);
        this.denyVirtualNodes = new HashSet<>(userPermissions.denyVirtualNodes);
        this.allowQueryNodes = new HashSet<>(userPermissions.allowQueryNodes);
        this.denyQueryNodes = new HashSet<>(userPermissions.denyQueryNodes);
        this.denyReadonlyVirtualNodes = new HashSet<>(userPermissions.denyReadonlyVirtualNodes);
        init();
    }

    private Set<String> asFullNamePermissions(Set<String> permissions)
    {
        Set<String> fullPermissions = new HashSet<String>();
        for (String s : permissions)
        {
            fullPermissions.add(permissionTypeQName + "." + s);
        }

        return fullPermissions;
    }

    public void init()
    {
        this.allowQueryNodesFull = asFullNamePermissions(allowQueryNodes);
        this.allowVirtualNodesFull = asFullNamePermissions(allowVirtualNodes);
        this.denyQueryNodesFull = asFullNamePermissions(denyQueryNodes);
        this.denyVirtualNodesFull = asFullNamePermissions(denyVirtualNodes);
        this.denyReadonlyVirtualNodesFull = asFullNamePermissions(denyReadonlyVirtualNodes);
    }

    public QName getPermissionTypeQName()
    {
        return this.permissionTypeQName;
    }

    public AccessStatus hasVirtualNodePermission(String permission, boolean readonly)
    {
        if (readonly)
        {
            if (denyReadonlyVirtualNodesFull.contains(permission) || denyReadonlyVirtualNodes.contains(permission))
            {
                return AccessStatus.DENIED;
            }
        }

        if (denyVirtualNodesFull.contains(permission) || denyVirtualNodes.contains(permission))
        {
            return AccessStatus.DENIED;
        }
        else if (allowVirtualNodesFull.contains(permission) || allowVirtualNodes.contains(permission))
        {
            return AccessStatus.ALLOWED;
        }
        else
        {
            return AccessStatus.UNDETERMINED;
        }
    }

    public AccessStatus hasQueryNodePermission(String permission)
    {
        if (denyQueryNodesFull.contains(permission) || denyQueryNodes.contains(permission))
        {
            return AccessStatus.DENIED;
        }
        else if (allowQueryNodesFull.contains(permission) || allowQueryNodes.contains(permission))
        {
            return AccessStatus.ALLOWED;
        }
        else
        {
            return AccessStatus.UNDETERMINED;
        }
    }

    public Set<String> getAllowVirtualNodes()
    {
        return this.allowVirtualNodes;
    }

    public void setAllowVirtualNodes(Set<String> allowFolders)
    {
        this.allowVirtualNodes = allowFolders;
    }

    public Set<String> getDenyVirtualNodes()
    {
        return this.denyVirtualNodes;
    }

    public void setDenyVirtualNodes(Set<String> denyFolders)
    {
        this.denyVirtualNodes = denyFolders;
    }

    public void setDenyReadonlyVirtualNodes(Set<String> denyReadonlyVirtualNodes)
    {
        this.denyReadonlyVirtualNodes = denyReadonlyVirtualNodes;
    }

    public Set<String> getDenyReadonlyVirtualNodes()
    {
        return this.denyReadonlyVirtualNodes;
    }

    public Set<String> getAllowQueryNodes()
    {
        return this.allowQueryNodes;
    }

    public void setAllowQueryNodes(Set<String> allowDocuments)
    {
        this.allowQueryNodes = allowDocuments;
    }

    public Set<String> getDenyQueryNodes()
    {
        return this.denyQueryNodes;
    }

    public void setDenyQueryNodes(Set<String> denyDocuments)
    {
        this.denyQueryNodes = denyDocuments;
    }
}