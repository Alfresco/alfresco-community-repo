package org.alfresco.repo.domain.node;

import java.io.Serializable;

/**
 * Bean to represent <tt>alf_transaction</tt> data.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class TransactionEntity implements Transaction, Serializable
{
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long version;
    private ServerEntity server;
    private String changeTxnId;
    private Long commitTimeMs;
    
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
          .append(", server=").append(server)
          .append(", changeTxnId=").append(changeTxnId)
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

    public Long getVersion()
    {
        return version;
    }

    public void setVersion(Long version)
    {
        this.version = version;
    }

    public ServerEntity getServer()
    {
        return server;
    }

    public void setServer(ServerEntity server)
    {
        this.server = server;
    }

    public String getChangeTxnId()
    {
        return changeTxnId;
    }

    public void setChangeTxnId(String changeTxnId)
    {
        this.changeTxnId = changeTxnId;
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
