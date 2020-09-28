/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.domain.usage;


/**
 * Usage Delta Implementation
 *
 */
public class UsageDeltaEntity implements UsageDelta
{
    private Long id;
    private Long version;
    
    private Long nodeId;
    private Long deltaSize; // +ve or -ve or 0 (in bytes)
    private Integer deltaCount;

    /**
     * Default constructor required
     */
    public UsageDeltaEntity()
    {
    }
    
    public UsageDeltaEntity(long nodeId, long deltaSize)
    {
        this.nodeId = nodeId;
        this.deltaSize = deltaSize;
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
    
    public Long getNodeId()
    {
        return nodeId;
    }
    
    public void setNodeId(Long nodeId)
    {
        this.nodeId = nodeId;
    }
    
    public Long getDeltaSize()
    {
        return deltaSize;
    }
    
    public void setDeltaSize(Long deltaSize)
    {
        this.deltaSize = deltaSize;
    }

    public Integer getDeltaCount()
    {
        return deltaCount;
    }

    public void setDeltaCount(Integer deltaCount)
    {
        this.deltaCount = deltaCount;
    }
}
