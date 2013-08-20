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
 * Entity bean for <b>avm_child_entries</b> table
 * 
 * @author janv
 * @since 3.2
 */
public class AVMChildEntryEntity implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Long parentNodeId;
    private String name;
    private String lowerName; // Derived from name for case insensitive lookups
    private Long childNodeId;
    
    public AVMChildEntryEntity()
    {
     // default constructor
    }
    
    public AVMChildEntryEntity(long parentNodeId, String name, long childNodeId)
    {
        this.parentNodeId = parentNodeId;
        this.name = name;
        this.lowerName = name == null ? null : name.toLowerCase();
        this.childNodeId = childNodeId;
    }
    
    public AVMChildEntryEntity(long parentNodeId, String name)
    {
        this.parentNodeId = parentNodeId;
        this.name = name;
        this.lowerName = name == null ? null : name.toLowerCase();
    }
    
    public AVMChildEntryEntity(long parentNodeId, long childNodeId)
    {
        this.parentNodeId = parentNodeId;
        this.childNodeId = childNodeId;
    }
    
    public Long getParentNodeId()
    {
        return parentNodeId;
    }
    
    public void setParentNodeId(Long parentNodeId)
    {
        this.parentNodeId = parentNodeId;
    }
    
    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
        this.lowerName = name == null ? null : name.toLowerCase();
    }
    
    public String getLowerName()
    {
        return lowerName;
    }

    public void setLowerName(String lowerName)
    {
        this.lowerName = lowerName;
    }

    public Long getChildId()
    {
        return childNodeId;
    }
    
    public void setChildNodeId(Long childNodeId)
    {
        this.childNodeId = childNodeId;
    }
    
    @Override
    public int hashCode()
    {
        return ((parentNodeId == null ? 0 : parentNodeId.hashCode()) +
                (name == null ? 0 : name.hashCode()) +
                (childNodeId == null ? 0 : childNodeId.hashCode()));
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj instanceof AVMChildEntryEntity)
        {
            AVMChildEntryEntity that = (AVMChildEntryEntity) obj;
            return (EqualsHelper.nullSafeEquals(this.parentNodeId, that.parentNodeId) &&
                    EqualsHelper.nullSafeEquals(this.name, that.name) &&
                    EqualsHelper.nullSafeEquals(this.childNodeId, that.childNodeId));
        }
        else
        {
            return false;
        }
    }
}
