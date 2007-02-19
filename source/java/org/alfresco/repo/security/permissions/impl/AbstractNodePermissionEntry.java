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

import org.alfresco.repo.security.permissions.NodePermissionEntry;


/**
 * This class provides common support for hash code and equality.
 * 
 * @author andyh
 */
public abstract class AbstractNodePermissionEntry implements
        NodePermissionEntry
{

    public AbstractNodePermissionEntry()
    {
        super();
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(200);
        sb.append("NodePermissionEntry")
          .append("[ node=").append(getNodeRef())
          .append(", entries=").append(getPermissionEntries())
          .append(", inherits=").append(inheritPermissions())
          .append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof AbstractNodePermissionEntry))
        {
            return false;
        }
        AbstractNodePermissionEntry other = (AbstractNodePermissionEntry) o;

        return this.getNodeRef().equals(other.getNodeRef())
                && (this.inheritPermissions() == other.inheritPermissions())
                && (this.getPermissionEntries().equals(other.getPermissionEntries()));
    }

    @Override
    public int hashCode()
    {
        return getNodeRef().hashCode();
    }
}
