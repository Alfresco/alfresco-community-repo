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


/**
 * Entity for <b>alf_ace_context</b> persistence.
 * 
 * @author janv
 * @since 3.4
 */
public class AceContextEntity implements AceContext
{
    private Long id;
    private Long version;
    private String classContext;
    private String propertyContext;
    private String kvpContext;
    
    /**
     * Default constructor
     */
    public AceContextEntity()
    {
    }
    
    public AceContextEntity(String classContext, String propertyContext, String kvpContext)
    {
        this.classContext = classContext;
        this.propertyContext = propertyContext;
        this.kvpContext = kvpContext;
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
    
    public String getClassContext()
    {
        return classContext;
    }
    
    public void setClassContext(String classContext)
    {
        this.classContext = classContext;
    }
    
    public String getPropertyContext()
    {
        return propertyContext;
    }
    
    public void setPropertyContext(String propertyContext)
    {
        this.propertyContext = propertyContext;
    }
    
    public String getKvpContext()
    {
        return kvpContext;
    }
    
    public void setKvpContext(String kvpContext)
    {
        this.kvpContext = kvpContext;
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
        {
            return true;
        }
        
        if (obj == null)
        {
            return false;
        }
        
        if (getClass() != obj.getClass())
        {
            return false;
        }
        
        final AceContextEntity other = (AceContextEntity) obj;
        if (classContext == null)
        {
            if (other.classContext != null)
                return false;
        }
        else if (!classContext.equals(other.classContext))
        {
            return false;
        }
        
        if (kvpContext == null)
        {
            if (other.kvpContext != null)
            {
                return false;
            }
        }
        else if (!kvpContext.equals(other.kvpContext))
        {
            return false;
        }
        
        if (propertyContext == null)
        {
            if (other.propertyContext != null)
            {
                return false;
            }
        }
        else if (!propertyContext.equals(other.propertyContext))
        {
            return false;
        }
        
        return true;
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("AceContextEntity")
          .append("[ ID=").append(id)
          .append(", version=").append(version)
          .append(", classContext=").append(classContext)
          .append(", propertyContext=").append(propertyContext)
          .append(", kvpContext=").append(kvpContext)
          .append("]");
        return sb.toString();
    }
}
