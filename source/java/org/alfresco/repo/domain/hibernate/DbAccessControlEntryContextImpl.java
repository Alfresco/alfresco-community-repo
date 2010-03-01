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

import org.alfresco.repo.domain.DbAccessControlEntryContext;

public class DbAccessControlEntryContextImpl implements DbAccessControlEntryContext, Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = -4479587461724827683L;

    private String classContext;
    
    private String kvpContext;
    
    private String propertyContext;
    
    private Long id;
    
    private Long version;

    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append("DbAccessControlEntryContextImpl").append("[ id=").append(id).append(", version=").append(version).append(", classContext=").append(classContext).append(
                ", kvpContext=").append(kvpContext).append(", propertyContext=").append(propertyContext);
        return sb.toString();
    }
    
    @Override
    public int hashCode()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((classContext == null) ? 0 : classContext.hashCode());
        result = PRIME * result + ((kvpContext == null) ? 0 : kvpContext.hashCode());
        result = PRIME * result + ((propertyContext == null) ? 0 : propertyContext.hashCode());
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
        final DbAccessControlEntryContextImpl other = (DbAccessControlEntryContextImpl) obj;
        if (classContext == null)
        {
            if (other.classContext != null)
                return false;
        }
        else if (!classContext.equals(other.classContext))
            return false;
        if (kvpContext == null)
        {
            if (other.kvpContext != null)
                return false;
        }
        else if (!kvpContext.equals(other.kvpContext))
            return false;
        if (propertyContext == null)
        {
            if (other.propertyContext != null)
                return false;
        }
        else if (!propertyContext.equals(other.propertyContext))
            return false;
        return true;
    }

    public String getClassContext()
    {
      return classContext;
    }

    public Long getId()
    {
       return id;
    }

    public String getKvpContext()
    {
        return kvpContext;
    }

    public String getPropertyContext()
    {
       return propertyContext;
    }

    public Long getVersion()
    {
        return version;
    }

    public void setClassContext(String classContext)
    {
        this.classContext = classContext;
    }

    public void setKvpContext(String kvpContext)
    {
       this.kvpContext = kvpContext;

    }

    public void setPropertyContext(String propertyContext)
    {
        this.propertyContext = propertyContext;

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

}
