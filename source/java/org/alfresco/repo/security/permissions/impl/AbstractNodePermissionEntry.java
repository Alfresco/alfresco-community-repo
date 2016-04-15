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
