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

import org.alfresco.repo.domain.DbAuthority;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

/**
 * The persisted class for authorities.
 * 
 * @author andyh
 */
public class DbAuthorityImpl implements DbAuthority, Serializable
{
    private static final long serialVersionUID = -5582068692208928127L;

    private static Log logger = LogFactory.getLog(DbAuthorityImpl.class);

    private Long id;

    private Long version;

    private String authority;

    private Long crc;

    public DbAuthorityImpl()
    {
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append("DbAuthorityImpl").append("[ id=").append(id).append(", version=").append(version).append(", authority=").append(authority).append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof DbAuthority))
        {
            return false;
        }
        DbAuthority other = (DbAuthority) o;
        return this.getAuthority().equals(other.getAuthority());
    }

    @Override
    public int hashCode()
    {
        return getAuthority().hashCode();
    }

    public Long getId()
    {
        return id;
    }

    @SuppressWarnings("unused")
    private void setId(Long id)
    {
        this.id = id;
    }

    @SuppressWarnings("unused")
    public void setCrc(Long crc)
    {
        this.crc = crc;
    }

    public Long getVersion()
    {
        return version;
    }

    public Long getCrc()
    {
        return crc;
    }

    /**
     * For Hibernate use
     */
    @SuppressWarnings("unused")
    private void setVersion(Long version)
    {
        this.version = version;
    }

    public String getAuthority()
    {
        return authority;
    }

    public void setAuthority(String authority)
    {
        this.authority = authority;
    }

    /**
     * Helper method to find an authority based on its natural key
     * 
     * @param session
     *            the Hibernate session to use
     * @param authority
     *            the authority name
     * @return Returns an existing instance or null if not found
     */
    public static DbAuthority find(Session session, String authority)
    {
        // TODO: Needs to use a query
        throw new UnsupportedOperationException("TODO");
    }
}
