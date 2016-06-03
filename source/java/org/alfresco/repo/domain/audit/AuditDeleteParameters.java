package org.alfresco.repo.domain.audit;

import java.util.Date;
import java.util.List;

/**
 * Deletion parameters for <b>alf_audit_entry</b> table.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class AuditDeleteParameters
{
    private Long auditApplicationId;
    private Long auditFromTime;
    private Long auditToTime;
    private List<Long> auditEntryIds;
    
    public AuditDeleteParameters()
    {
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("AuditDeleteParameters")
          .append("[ auditApplicationId=").append(auditApplicationId)
          .append(", auditFromTime").append(auditFromTime == null ? null : new Date(auditFromTime))
          .append(", auditToTime").append(auditToTime == null ? null : new Date(auditToTime))
          .append(", auditEntryIds").append(auditEntryIds == null ? null : auditEntryIds.size())
          .append("]");
        return sb.toString();
    }

    public Long getAuditApplicationId()
    {
        return auditApplicationId;
    }

    public void setAuditApplicationId(Long auditApplicationId)
    {
        this.auditApplicationId = auditApplicationId;
    }

    public Long getAuditFromTime()
    {
        return auditFromTime;
    }

    public void setAuditFromTime(Long auditFromTime)
    {
        this.auditFromTime = auditFromTime;
    }

    public Long getAuditToTime()
    {
        return auditToTime;
    }

    public void setAuditToTime(Long auditToTime)
    {
        this.auditToTime = auditToTime;
    }

    public List<Long> getAuditEntryIds()
    {
        return auditEntryIds;
    }

    public void setAuditEntryIds(List<Long> auditEntryIds)
    {
        this.auditEntryIds = auditEntryIds;
    }
}
