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
package org.alfresco.repo.domain.permissions;

import java.io.Serializable;

import org.alfresco.util.EqualsHelper;


/**
 * Entity for <b>alf_permission</b> persistence.
 * 
 * @author janv
 * @since 3.4
 */
public class PermissionEntity implements Permission, Serializable
{
    private static final long serialVersionUID = 8219087288749688965L;
    
    private Long id;
    private Long version;
    private Long typeQnameId;
    private String name;
    
    /**
     * Default constructor
     */
    public PermissionEntity()
    {
    }
    
    public PermissionEntity(Long typeQnameId, String name)
    {
        this.typeQnameId = typeQnameId;
        this.name = name;
    }
    
    public Long getId()
    {
        return id;
    }
    
    public void setId(Long id)
    {
        this.id = id;
    }
    
    public Long getVersion()
    {
        return version;
    }
    
    public void setVersion(Long version)
    {
        this.version = version;
    }
    
    public void incrementVersion()
    {
        if (this.version >= Long.MAX_VALUE)
        {
            this.version = 0L;
        }
        else
        {
            this.version++;
        }
    }
    
    public Long getTypeQNameId()
    {
        return typeQnameId;
    }
    
    public void setTypeQNameId(Long typenameId)
    {
        this.typeQnameId = typenameId;
    }
    
    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    @Override
    public int hashCode()
    {
        return typeQnameId.hashCode() + (37 * name.hashCode());
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj instanceof PermissionEntity)
        {
            PermissionEntity that = (PermissionEntity)obj;
            return (EqualsHelper.nullSafeEquals(this.typeQnameId, that.typeQnameId) &&
                    EqualsHelper.nullSafeEquals(this.name, that.name));
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("PermissionEntity")
          .append("[ ID=").append(id)
          .append(", version=").append(version)
          .append(", typeQnameId=").append(typeQnameId)
          .append(", name=").append(name)
          .append("]");
        return sb.toString();
    }
}
