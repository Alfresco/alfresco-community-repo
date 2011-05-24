package org.alfresco.repo.domain.solr;

/**
 * Bean to represent SOLR transaction data.
 * 
 * @since 4.0
 */
public class TransactionEntity implements Transaction
{
    private Long id;
    private Long commitTimeMs;
    private int updates;
    private int deletes;
    
    /**
     * Required default constructor
     */
    public TransactionEntity()
    {
    }
        
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("TransactionEntity")
          .append("[ ID=").append(id)
          .append(", updates=").append(updates)
          .append(", deletes=").append(deletes)
          .append(", commitTimeMs=").append(commitTimeMs)
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

    public int getUpdates()
    {
        return updates;
    }

    public void setUpdates(int updates)
    {
        this.updates = updates;
    }

    public int getDeletes()
    {
        return deletes;
    }

    public void setDeletes(int deletes)
    {
        this.deletes = deletes;
    }

    public Long getCommitTimeMs()
    {
        return commitTimeMs;
    }

    public void setCommitTimeMs(Long commitTimeMs)
    {
        this.commitTimeMs = commitTimeMs;
    }
}

