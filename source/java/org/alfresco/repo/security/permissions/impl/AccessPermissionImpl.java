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
    
    private int position;

    public AccessPermissionImpl(String permission, AccessStatus accessStatus, String authority, int position)
    {
        this.permission = permission;
        this.accessStatus = accessStatus;
        this.authority = authority;
        this.authorityType = AuthorityType.getAuthorityType(authority);
        this.position = position;
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

    

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((accessStatus == null) ? 0 : accessStatus.hashCode());
        result = prime * result + ((authority == null) ? 0 : authority.hashCode());
        result = prime * result + ((permission == null) ? 0 : permission.hashCode());
        result = prime * result + position;
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final AccessPermissionImpl other = (AccessPermissionImpl) obj;
        if (accessStatus == null)
        {
            if (other.accessStatus != null)
                return false;
        }
        else if (!accessStatus.equals(other.accessStatus))
            return false;
        if (authority == null)
        {
            if (other.authority != null)
                return false;
        }
        else if (!authority.equals(other.authority))
            return false;
        if (permission == null)
        {
            if (other.permission != null)
                return false;
        }
        else if (!permission.equals(other.permission))
            return false;
        if (position != other.position)
            return false;
        return true;
    }

    public int getPosition()
    {
        return position;
    }

    public boolean isInherited()
    {
       return (position > 0);
    }

    public boolean isSetDirectly()
    {
        return (position == 0);
    }
}