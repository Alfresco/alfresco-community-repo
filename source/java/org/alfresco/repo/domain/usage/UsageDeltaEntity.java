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
