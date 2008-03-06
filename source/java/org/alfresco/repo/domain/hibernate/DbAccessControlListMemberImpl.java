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

import org.alfresco.repo.domain.DbAccessControlEntry;
import org.alfresco.repo.domain.DbAccessControlList;
import org.alfresco.repo.domain.DbAccessControlListMember;
import org.hibernate.Session;

/**
 * Hibernate support to store acl-acxe entries
 */
public class DbAccessControlListMemberImpl implements DbAccessControlListMember, Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long version;

    private DbAccessControlList acl;

    private DbAccessControlEntry ace;

    private int position;

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append("DbAccessControlListMemberImpl").append("[ id=").append(id).append(", version=").append(version).append(", acl=").append(acl).append(", ace=").append(ace)
                .append(", position=").append(position).append("]");
        return sb.toString();
    }

    @Override
    public int hashCode()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((ace == null) ? 0 : ace.hashCode());
        result = PRIME * result + ((acl == null) ? 0 : acl.hashCode());
        result = PRIME * result + position;
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
        final DbAccessControlListMemberImpl other = (DbAccessControlListMemberImpl) obj;
        if (ace == null)
        {
            if (other.ace != null)
                return false;
        }
        else if (!ace.equals(other.ace))
            return false;
        if (acl == null)
        {
            if (other.acl != null)
                return false;
        }
        else if (!acl.equals(other.acl))
            return false;
        if (position != other.position)
            return false;
        return true;
    }

    public DbAccessControlEntry getAccessControlEntry()
    {
        return ace;
    }

    public DbAccessControlList getAccessControlList()
    {
        return acl;
    }

    public Long getId()
    {
        return id;
    }

    public int getPosition()
    {
        return position;
    }

    public Long getVersion()
    {
        return version;
    }

    public void setAccessControlEntry(DbAccessControlEntry ace)
    {
        this.ace = ace;
    }

    public void setAccessControlList(DbAccessControlList acl)
    {
        this.acl = acl;
    }

    public void setPosition(int position)
    {
        this.position = position;
    }

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
     * 
     * @param session
     * @param acl => can be null - implies all entries that match ace
     * @param ace => can be null - implies all entries that match acl
     * @param position => -1 is all positions
     * 
     * Note: both acl and ace may not be null;
     * 
     * @return
     */
    public static DbAccessControlListMember find(Session session, DbAccessControlList acl, DbAccessControlEntry ace, int position)
    {
        // TODO: Needs to use a query
        throw new UnsupportedOperationException("TODO");
    }

}
