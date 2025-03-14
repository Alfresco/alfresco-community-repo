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
                && EqualsHelper.nullSafeEquals(this.getAuthority(), other.getAuthority()) && EqualsHelper.nullSafeEquals(this.getAccessStatus(), other.getAccessStatus());
    }

    @Override
    public int hashCode()
    {
        int hashCode = 0;
        if (getNodeRef() != null)
        {
            getNodeRef().hashCode();
        }
        if (getPermissionReference() != null)
        {
            hashCode = hashCode * 37 + getPermissionReference().hashCode();
        }
        if (getAuthority() != null)
        {
            hashCode = hashCode * 37 + getAuthority().hashCode();
        }
        if (getAccessStatus() != null)
        {
            hashCode = hashCode * 37 + getAccessStatus().hashCode();
        }
        return hashCode;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(200);
        sb.append("PermissionEntry").append("[ authority=").append(getAuthority()).append(", permission=").append(getPermissionReference()).append(", access=").append(
                getAccessStatus()).append("]");
        return sb.toString();
    }

}
