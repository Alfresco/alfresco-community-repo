package org.alfresco.repo.domain.audit;

import java.util.Date;

/**
 * Entity bean for <b>alf_audit_entry</b> table.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class AuditEntryEntity
{
    private Long id;
    private Long auditApplicationId;
    private Long auditUserId;
    private long auditTime;
    private Long auditValuesId;
    
    public AuditEntryEntity()
    {
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("AuditEntryEntity")
          .append("[ ID=").append(id)
          .append(", auditApplicationId=").append(auditApplicationId)
          .append(", auditTime").append(new Date(auditTime))
          .append(", auditValuesId=").append(auditValuesId)
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

    public Long getAuditApplicationId()
    {
        return auditApplicationId;
    }

    public void setAuditApplicationId(Long auditSessionId)
    {
        this.auditApplicationId = auditSessionId;
    }

    public Long getAuditUserId()
    {
        return auditUserId;
    }

    public void setAuditUserId(Long auditUserId)
    {
        this.auditUserId = auditUserId;
    }

    public long getAuditTime()
    {
        return auditTime;
    }

    public void setAuditTime(long auditTime)
    {
        this.auditTime = auditTime;
    }

    public Long getAuditValuesId()
    {
        return auditValuesId;
    }

    public void setAuditValuesId(Long auditValuesId)
    {
        this.auditValuesId = auditValuesId;
    }
}
