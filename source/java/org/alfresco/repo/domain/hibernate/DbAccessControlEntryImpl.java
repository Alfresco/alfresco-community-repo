/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.domain.hibernate;

import java.io.Serializable;

import org.alfresco.repo.domain.DbAccessControlEntry;
import org.alfresco.repo.domain.DbAccessControlEntryContext;
import org.alfresco.repo.domain.DbAuthority;
import org.alfresco.repo.domain.DbPermission;
import org.alfresco.repo.domain.DbPermissionKey;
import org.alfresco.repo.security.permissions.ACEType;
import org.hibernate.Session;

/**
 * Persisted permission entries
 * 
 * @author andyh
 */
public class DbAccessControlEntryImpl implements DbAccessControlEntry, Serializable
{
    private static final long serialVersionUID = -418837862334064582L;

    private Long id;

    private Long version;

    /** The permission to which this applies (non null - all is a special string) */
    private DbPermission permission;

    /** The recipient to which this applies (non null - all is a special string) */
    private DbAuthority authority;

    /** Is this permission allowed? */
    private boolean allowed;

    private int aceType;

    private DbAccessControlEntryContext context;

    public DbAccessControlEntryImpl()
    {
        super();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append("DbAccessControlEntryImpl")
          .append("[ id=").append(id)
          .append(", version=").append(version)
          .append(", permission=").append(permission.getKey())
          .append(", authority=").append(authority.getAuthority())
          .append(", allowed=").append(allowed)
          .append(", aceType=").append(ACEType.getACETypeFromId(aceType))
          .append(", context=").append(context)
          .append("]");
        return sb.toString();
    }

    @Override
    public int hashCode()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + aceType;
        result = PRIME * result + (allowed ? 1231 : 1237);
        result = PRIME * result + ((authority == null) ? 0 : authority.hashCode());
        result = PRIME * result + ((context == null) ? 0 : context.hashCode());
        result = PRIME * result + ((permission == null) ? 0 : permission.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final DbAccessControlEntryImpl other = (DbAccessControlEntryImpl) obj;
        if (aceType != other.aceType)
            return false;
        if (allowed != other.allowed)
            return false;
        if (authority == null)
        {
            if (other.authority != null)
                return false;
        }
        else if (!authority.equals(other.authority))
            return false;
        if (context == null)
        {
            if (other.context != null)
                return false;
        }
        else if (!context.equals(other.context))
            return false;
        if (permission == null)
        {
            if (other.permission != null)
                return false;
        }
        else if (!permission.equals(other.permission))
            return false;
        return true;
    }
    

    public Long getId()
    {
        return id;
    }

    /**
     * For Hibernate use
     */
    @SuppressWarnings("unused")
    private void setId(Long id)
    {
        this.id = id;
    }

    public Long getVersion()
    {
        return version;
    }

    /**
     * For Hibernate use
     */
    @SuppressWarnings("unused")
    private void setVersion(Long version)
    {
        this.version = version;
    }

    public DbPermission getPermission()
    {
        return permission;
    }

    public void setPermission(DbPermission permissionReference)
    {
        this.permission = permissionReference;
    }

    public DbAuthority getAuthority()
    {
        return authority;
    }

    public void setAuthority(DbAuthority recipient)
    {
        this.authority = recipient;
    }

    public boolean isAllowed()
    {
        return allowed;
    }

    public void setAllowed(boolean allowed)
    {
        this.allowed = allowed;
    }

    public ACEType getAceType()
    {
        return ACEType.getACETypeFromId(aceType);
    }

    public void setAceType(ACEType aceType)
    {
        this.aceType = aceType.getId();
    }

    
    @SuppressWarnings("unused")
    private void setApplies(int applies)
    {
        this.aceType = applies;
    }
    
    @SuppressWarnings("unused")
    private int getApplies()
    {
        return aceType;
    }
    
    
    public DbAccessControlEntryContext getContext()
    {
        return context;
    }

    public void setContext(DbAccessControlEntryContext context)
    {
        this.context = context;
    }

    public void delete()
    {
        throw new UnsupportedOperationException("TODO");
    }
    


    public static DbAccessControlEntry find(Session session, ACEType type, boolean allow, String authority, DbPermissionKey permissionKey)
    {
        // Query query = session
        // .getNamedQuery(PermissionsDaoComponentImpl.QUERY_GET_PERMISSION)
        // .setString("permissionTypeQName", qname.toString())
        // .setString("permissionName", name);
        // return (DbPermission) query.uniqueResult();
        throw new UnsupportedOperationException("TODO");
    }
}
