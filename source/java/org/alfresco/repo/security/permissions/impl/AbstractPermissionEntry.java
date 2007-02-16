/*
 * Copyright (C) 2005 Alfresco, Inc.
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

import org.alfresco.repo.security.permissions.PermissionEntry;
import org.alfresco.util.EqualsHelper;

/**
 * This class provides common support for hash code and equality.
 * 
 * @author andyh
 */
public abstract class AbstractPermissionEntry implements PermissionEntry
{

    public AbstractPermissionEntry()
    {
        super();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof AbstractPermissionEntry))
        {
            return false;
        }
        AbstractPermissionEntry other = (AbstractPermissionEntry) o;
        return EqualsHelper.nullSafeEquals(this.getNodeRef(), other.getNodeRef())
                && EqualsHelper.nullSafeEquals(this.getPermissionReference(), other.getPermissionReference())
                && EqualsHelper.nullSafeEquals(this.getAuthority(), other.getAuthority())
                && EqualsHelper.nullSafeEquals(this.getAccessStatus(), other.getAccessStatus());
    }

    @Override
    public int hashCode()
    {
        int hashCode = getNodeRef().hashCode();
        if (getPermissionReference() != null)
        {
            hashCode = hashCode * 37 + getPermissionReference().hashCode();
        }
        if (getAuthority() != null)
        {
            hashCode = hashCode * 37 + getAuthority().hashCode();
        }
        if(getAccessStatus() != null)
        {
           hashCode = hashCode * 37 + getAccessStatus().hashCode();
        }
        return hashCode;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(200);
        sb.append("PermissionEntry")
          .append("[ authority=").append(getAuthority())
          .append(", permission=").append(getPermissionReference())
          .append(", access=").append(getAccessStatus())
          .append("]");
        return sb.toString();
    }
    

}
