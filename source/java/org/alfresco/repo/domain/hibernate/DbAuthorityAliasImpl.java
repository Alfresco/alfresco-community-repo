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
package org.alfresco.repo.domain.hibernate;

import java.io.Serializable;

import org.alfresco.repo.domain.DbAuthority;
import org.alfresco.repo.domain.DbAuthorityAlias;
import org.hibernate.Session;

public class DbAuthorityAliasImpl implements DbAuthorityAlias, Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = -774180120537804154L;
    private Long id;
    private Long version;
    private DbAuthority authority;
    private DbAuthority alias;
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append("DbAuthorityAliasImpl")
          .append("[ id=").append(id)
          .append(", version=").append(version)
          .append(", authority=").append(authority)
          .append(", alias=").append(alias)
          .append("]");
        return sb.toString();
    }
    
    @Override
    public int hashCode()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((alias == null) ? 0 : alias.hashCode());
        result = PRIME * result + ((authority == null) ? 0 : authority.hashCode());
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
        final DbAuthorityAliasImpl other = (DbAuthorityAliasImpl) obj;
        if (alias == null)
        {
            if (other.alias != null)
                return false;
        }
        else if (!alias.equals(other.alias))
            return false;
        if (authority == null)
        {
            if (other.authority != null)
                return false;
        }
        else if (!authority.equals(other.authority))
            return false;
        return true;
    }

    public DbAuthority getAlias()
    {
        return alias;
    }

    public DbAuthority getAuthority()
    {
        return authority;
    }

    public Long getId()
    {
        return id;
    }

    public Long getVersion()
    {
        return version;
    }

    public void setAlias(DbAuthority alias)
    {
        this.alias = alias;
    }

    public void setAuthority(DbAuthority authority)
    {
        this.authority = authority;
    }

    /**
     * For Hibernate use
     */
    @SuppressWarnings("unused")
    private void setId(Long id)
    {
        this.id = id;
    }
    
    /**
     * For Hibernate use
     */
    @SuppressWarnings("unused")
    private void setVersion(Long version)
    {
        this.version = version;
    }
    
    
    /**
     * Helper method to find an authority alias based on the authority and alias
     * 
     * @param session the Hibernate session to use
     * @param authority the authority name
     * @return Returns an existing instance or null if not found
     */
    public static DbAuthorityAlias find(Session session, String authority, String alias)
    {
        // TODO: Needs to use a query 
        throw new UnsupportedOperationException("TODO");
    }
}
