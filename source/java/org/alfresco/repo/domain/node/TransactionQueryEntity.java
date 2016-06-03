package org.alfresco.repo.domain.node;

import java.util.List;

/**
 * Bean to represent carry query parameters for <tt>alf_transaction</tt>.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class TransactionQueryEntity
{
    private Long id;
    private Long minId;
    private Long maxId;
    private Long minCommitTime;
    private Long maxCommitTime;
    private List<Long> includeTxnIds;
    private List<Long> excludeTxnIds;
    private Long excludeServerId;
    private Boolean ascending;
    private Long typeQNameId;
    private Long storeId;
    
    /**
     * Required default constructor
     */
    public TransactionQueryEntity()
    {
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("TransactionQueryEntity")
          .append("[ id=").append(id)
          .append(", minId=").append(minId)
          .append(", maxId=").append(maxId)
          .append(", minCommitTime=").append(minCommitTime)
          .append(", maxCommitTime=").append(maxCommitTime)
          .append(", includeTxnIds=").append(includeTxnIds)
          .append(", excludeTxnIds=").append(excludeTxnIds)
          .append(", excludeServerId=").append(excludeServerId)
          .append(", ascending=").append(ascending)
          .append(", typeQNameId=").append(typeQNameId)
          .append(", storeId=").append(storeId)
          .append("]");
        return sb.toString();
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getMinId()
    {
        return minId;
    }

    public void setMinId(Long minId)
    {
        this.minId = minId;
    }

    public Long getMaxId()
    {
        return maxId;
    }

    public void setMaxId(Long maxId)
    {
        this.maxId = maxId;
    }

    public Long getMinCommitTime()
    {
        return minCommitTime;
    }

    public void setMinCommitTime(Long minCommitTime)
    {
        this.minCommitTime = minCommitTime;
    }

    public Long getMaxCommitTime()
    {
        return maxCommitTime;
    }

    public void setMaxCommitTime(Long maxCommitTime)
    {
        this.maxCommitTime = maxCommitTime;
    }

    public List<Long> getIncludeTxnIds()
    {
        return includeTxnIds;
    }

    public void setIncludeTxnIds(List<Long> includeTxnIds)
    {
        this.includeTxnIds = includeTxnIds;
    }

    public List<Long> getExcludeTxnIds()
    {
        return excludeTxnIds;
    }

    public void setExcludeTxnIds(List<Long> excludeTxnIds)
    {
        this.excludeTxnIds = excludeTxnIds;
    }

    public Long getExcludeServerId()
    {
        return excludeServerId;
    }

    public void setExcludeServerId(Long excludeServerId)
    {
        this.excludeServerId = excludeServerId;
    }

    public Boolean getAscending()
    {
        return ascending;
    }

    public void setAscending(Boolean ascending)
    {
        this.ascending = ascending;
    }

    public Long getTypeQNameId()
    {
        return typeQNameId;
    }

    public void setTypeQNameId(Long typeQNameId)
    {
        this.typeQNameId = typeQNameId;
    }

    public Long getStoreId()
    {
        return storeId;
    }

    public void setStoreId(Long storeId)
    {
        this.storeId = storeId;
    }
}
