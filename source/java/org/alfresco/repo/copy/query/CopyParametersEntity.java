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
package org.alfresco.repo.copy.query;

import java.util.List;

/**
 * Bean class to carry canned query parameters
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public class CopyParametersEntity
{
    private Long originalNodeId;
    private Long originalAssocTypeId;
    private Long namePropId;
    private Long copyParentNodeId;
    private List<Long> copyAspectIdsToIgnore;
    
    public Long getOriginalNodeId()
    {
        return originalNodeId;
    }
    public void setOriginalNodeId(Long originalNodeId)
    {
        this.originalNodeId = originalNodeId;
    }
    public Long getOriginalAssocTypeId()
    {
        return originalAssocTypeId;
    }
    public void setOriginalAssocTypeId(Long originalAssocTypeId)
    {
        this.originalAssocTypeId = originalAssocTypeId;
    }
    public Long getNamePropId()
    {
        return namePropId;
    }
    public void setNamePropId(Long namePropId)
    {
        this.namePropId = namePropId;
    }
    public Long getCopyParentNodeId()
    {
        return copyParentNodeId;
    }
    public void setCopyParentNodeId(Long copyParentNodeId)
    {
        this.copyParentNodeId = copyParentNodeId;
    }
    public List<Long> getCopyAspectIdsToIgnore()
    {
        return copyAspectIdsToIgnore;
    }
    public void setCopyAspectIdsToIgnore(List<Long> copyAspectIdsToIgnore)
    {
        this.copyAspectIdsToIgnore = copyAspectIdsToIgnore;
    }
    public boolean getTrue()
    {
        return true;
    }
}