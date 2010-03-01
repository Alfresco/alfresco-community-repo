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


/**
 * Entity bean for <b>avm_history_links</b> table
 * 
 * @author janv
 * @since 3.2
 */
public class AVMHistoryLinkEntity implements Serializable
{
    private static final long serialVersionUID = 1578072747215533879L;
    
    private Long ancestorNodeId;
    private Long descendentNodeId;
    
    public AVMHistoryLinkEntity()
    {
        // default constructor
    }
    public AVMHistoryLinkEntity(Long ancestorNodeId, Long descendentNodeId)
    {
        this.ancestorNodeId = ancestorNodeId;
        this.descendentNodeId = descendentNodeId;
    }
    
    public Long getDescendentNodeId()
    {
        return descendentNodeId;
    }
    
    public void setDescendentNodeId(Long descendentNodeId)
    {
        this.descendentNodeId = descendentNodeId;
    }
    
    public Long getAncestorNodeId()
    {
        return ancestorNodeId;
    }
    
    public void setAncestorNodeId(Long ancestorNodeId)
    {
        this.ancestorNodeId = ancestorNodeId;
    }
}
