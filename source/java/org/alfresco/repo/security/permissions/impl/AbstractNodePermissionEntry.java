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
package org.alfresco.repo.security.permissions.impl;

import org.alfresco.repo.security.permissions.NodePermissionEntry;
import org.alfresco.util.EqualsHelper;

/**
 * This class provides common support for hash code and equality.
 * 
 * @author andyh
 */
public abstract class AbstractNodePermissionEntry implements NodePermissionEntry
{

    public AbstractNodePermissionEntry()
    {
        super();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(200);
        sb.append("NodePermissionEntry").append("[ node=").append(getNodeRef()).append(", entries=").append(getPermissionEntries()).append(", inherits=").append(
                inheritPermissions()).append("]");
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

        return EqualsHelper.nullSafeEquals(this.getNodeRef(), other.getNodeRef()) &&
               EqualsHelper.nullSafeEquals(this.inheritPermissions(), other.inheritPermissions()) && 
               EqualsHelper.nullSafeEquals(this.getPermissionEntries(), other.getPermissionEntries());
    }

    @Override
    public int hashCode()
    {
        if (getNodeRef() != null)
        {
            return getNodeRef().hashCode();
        }
        else
        {
            return 0;
        }
    }
}
