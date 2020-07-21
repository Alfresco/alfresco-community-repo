/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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
package org.alfresco.repo.domain.node;

/**
 * Node Range bean including an initial fromNodeId and a final fromNodeId
 * 
 * @author aborroy
 *
 */
public class NodeRangeEntity
{
    
    private Long fromNodeId;
    private Long toNodeId;
    
    /**
     * @return the fromNodeId
     */
    public Long getFromNodeId()
    {
        return fromNodeId;
    }
    /**
     * @param fromNodeId the fromNodeId to set
     */
    public void setFromNodeId(Long fromNodeId)
    {
        this.fromNodeId = fromNodeId;
    }
    /**
     * @return the toNodeId
     */
    public Long getToNodeId()
    {
        return toNodeId;
    }
    /**
     * @param toNodeId the toNodeId to set
     */
    public void setToNodeId(Long toNodeId)
    {
        this.toNodeId = toNodeId;
    }

}
