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
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.alfresco.repo.domain.DbPermission;
import org.alfresco.repo.domain.DbPermissionKey;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;
import org.hibernate.Session;

/**
 * The persisted class for permissions.
 * 
 * @author andyh
 */
public class DbPermissionImpl implements DbPermission, Serializable
{
    private static final long serialVersionUID = -6352566900815035461L;

    private Long id;
    private Long version;
    private Long typeQNameId;
    private String name;

    private transient ReadLock refReadLock;
    private transient WriteLock refWriteLock;
    private transient QName typeQName;

    public DbPermissionImpl()
    {
        super();
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        refReadLock = lock.readLock();
        refWriteLock = lock.writeLock();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append("DbPermissionImpl")
          .append("[ id=").append(id)
          .append(", version=").append(version)
          .append(", typeQName=").append(typeQNameId)
          .append(", name=").append(getName())
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
        if (!(o instanceof DbPermission))
        {
            return false;
        }
        DbPermission other = (DbPermission) o;
        return (EqualsHelper.nullSafeEquals(typeQNameId, other.getTypeQNameId()))
                && (EqualsHelper.nullSafeEquals(name, other.getName())
                        );
    }

    @Override
    public int hashCode()
    {
        return typeQNameId.hashCode() + (37 * name.hashCode());
    }

    public QName getTypeQName(QNameDAO qnameDAO)
    {
        refReadLock.lock();
        try
        {
            if (typeQName != null)
            {
                return typeQName;
            }
        }
        finally
        {
            refReadLock.unlock();
        }
        refWriteLock.lock();
        try
        {
            typeQName = qnameDAO.getQName(typeQNameId).getSecond();
            return typeQName;
        }
        finally
        {
            refWriteLock.unlock();
        }
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

    public Long getTypeQNameId()
    {
        return typeQNameId;
    }

    public void setTypeQNameId(Long typeQNameId)
    {
        refWriteLock.lock();
        try
        {
            this.typeQNameId = typeQNameId;
            this.typeQName = null;
        }
        finally
        {
            refWriteLock.unlock();
        }
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public DbPermissionKey getKey()
    {
        return new DbPermissionKey(typeQNameId, name);
    }

    /**
     * Helper method to find a permission based on its natural key
     * 
     * @param session
     *            the Hibernate session to use
     * @param qname
     *            the type qualified name
     * @param name
     *            the name of the permission
     * @return Returns an existing instance or null if not found
     */
    public static DbPermission find(Session session, QName qname, String name)
    {
        // Query query = session
        // .getNamedQuery(PermissionsDaoComponentImpl.QUERY_GET_PERMISSION)
        // .setString("permissionTypeQName", qname.toString())
        // .setString("permissionName", name);
        // return (DbPermission) query.uniqueResult();
        throw new UnsupportedOperationException("TODO");
    }
}
