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
 * Entity bean for <b>avm_merge_links</b> table
 * 
 * @author janv
 * @since 3.2
 */
public class AVMMergeLinkEntity
{
    private Long mergeFromNodeId;
    private Long mergeToNodeId;
    
    public AVMMergeLinkEntity()
    {
        // default constructor
    }
    
    public AVMMergeLinkEntity(Long mergeFromNodeId, Long mergeToNodeId)
    {
        this.mergeFromNodeId = mergeFromNodeId;
        this.mergeToNodeId = mergeToNodeId;
    }
    
    public Long getMergeFromNodeId()
    {
        return mergeFromNodeId;
    }
    
    public void setMergeFromNodeId(Long mergeFromNodeId)
    {
        this.mergeFromNodeId = mergeFromNodeId;
    }
    
    public Long getMergeToNodeId()
    {
        return mergeToNodeId;
    }
    
    public void setMergeToNodeId(Long mergeToNodeId)
    {
        this.mergeToNodeId = mergeToNodeId;
    }
}
