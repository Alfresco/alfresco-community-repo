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
