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