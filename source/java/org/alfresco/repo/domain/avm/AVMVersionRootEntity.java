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
package org.alfresco.repo.domain.avm;

import java.io.Serializable;

import org.alfresco.util.EqualsHelper;

/**
 * Entity bean for <b>avm_version_roots</b> table
 * 
 * @author janv
 * @since 3.2
 */
public class AVMVersionRootEntity implements Serializable
{
    private static final long serialVersionUID = -3373271203895368258L;
    
    private Long id;
    private Integer version;
    private Long storeId;
    private Long rootNodeId;
    private Long createdDate;
    private String creator;
    private String tag;
    private String description;
    
    public Long getId()
    {
        return id;
    }
    
    public void setId(Long id)
    {
        this.id = id;
    }
    
    public Integer getVersion()
    {
        return version;
    }
    
    public void setVersion(Integer version)
    {
        this.version = version;
    }
    
        public Long getRootNodeId()
    {
        return rootNodeId;
    }
    
    public void setRootNodeId(Long rootNodeId)
    {
        this.rootNodeId = rootNodeId;
    }
    
    public Long getStoreId()
    {
        return storeId;
    }
    
    public void setStoreId(Long storeId)
    {
        this.storeId = storeId;
    }
    
    public Long getCreatedDate()
    {
        return createdDate;
    }
    
    public void setCreatedDate(Long createdDate)
    {
        this.createdDate = createdDate;
    }
    
    public String getCreator()
    {
        return creator;
    }
    
    public void setCreator(String creator)
    {
        this.creator = creator;
    }
    
    public String getTag()
    {
        return tag;
    }
    
    public void setTag(String tag)
    {
        this.tag = tag;
    }
    
    public String getDescription()
    {
        return description;
    }
    
    public void setDescription(String description)
    {
        this.description = description;
    }
    
    @Override
    public int hashCode()
    {
        return (id == null ? 0 : id.hashCode());
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj instanceof AVMVersionRootEntity)
        {
            AVMVersionRootEntity that = (AVMVersionRootEntity) obj;
            return (EqualsHelper.nullSafeEquals(this.id, that.id));
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
        sb.append("AVMVersionRootEntity")
          .append("[ ID=").append(id)
          .append(", version=").append(version)
          .append(", storeId=").append(storeId)
          .append(", rootNodeId=").append(rootNodeId)
          .append(", createdDate=").append(createdDate)
          .append(", creator=").append(creator)
          .append(", tag=").append(tag)
          .append(", description=").append(description)
          .append("]");
        return sb.toString();
    }
}
