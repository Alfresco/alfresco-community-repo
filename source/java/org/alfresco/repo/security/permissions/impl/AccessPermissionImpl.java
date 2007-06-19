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
package org.alfresco.repo.security.permissions.impl;

import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityType;

/**
 * Standard implementation for access permission info
 * @author andyh
 *
 */
public class AccessPermissionImpl implements AccessPermission
{
    private String permission;

    private AccessStatus accessStatus;

    private String authority;

    private AuthorityType authorityType;

    public AccessPermissionImpl(String permission, AccessStatus accessStatus, String authority)
    {
        this.permission = permission;
        this.accessStatus = accessStatus;
        this.authority = authority;
        this.authorityType = AuthorityType.getAuthorityType(authority);
    }

    public String getPermission()
    {
        return permission;
    }

    public AccessStatus getAccessStatus()
    {
        return accessStatus;
    }

    public String getAuthority()
    {
        return authority;
    }

    public AuthorityType getAuthorityType()
    {
        return authorityType;
    }

    @Override
    public String toString()
    {
        return accessStatus + " " + this.permission + " - " + this.authority + " (" + this.authorityType + ")";
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof AccessPermissionImpl))
        {
            return false;
        }
        AccessPermissionImpl other = (AccessPermissionImpl) o;
        return this.getPermission().equals(other.getPermission())
                && (this.getAccessStatus() == other.getAccessStatus() && (this.getAccessStatus().equals(other
                        .getAccessStatus())));
    }

    @Override
    public int hashCode()
    {
        return ((authority.hashCode() * 37) + permission.hashCode()) * 37 + accessStatus.hashCode();
    }
}