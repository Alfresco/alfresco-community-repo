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


/**
 * Entity bean for <b>avm_child_entries</b> table
 * 
 * @author janv
 * @since 3.2
 */

public class AVMNodeChildEntryEntity
{
    public static final Long CONST_LONG_ZERO = new Long(0L);
    
    private Long parentNodeId;
    private String name;
    private Long childNodeId;
    
    public AVMNodeChildEntryEntity()
    {
     // default constructor
    }
    
    /*
    public AVMNodeChildEntryEntity(Long parentNodeId, String name, Long childNodeId)
    {
        this.parentNodeId = parentNodeId;
        this.name = name;
        this.childNodeId = childNodeId;
    }
    */
    
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
    }
    
    public Long getChildId()
    {
        return childNodeId;
    }
    
    public void setChildNodeId(Long childNodeId)
    {
        this.childNodeId = childNodeId;
    }
}
