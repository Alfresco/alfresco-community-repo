package org.alfresco.repo.domain.audit;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.domain.propval.PropertyIdSearchRow;

/**
 * Results bean for <b>alf_audit_entry</b> table.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class AuditQueryResult
{
    private Long auditEntryId;
    private Long auditAppNameId;
    private Long auditUserId;
    private long auditTime;
    private Long auditValuesId;
    private List<PropertyIdSearchRow> auditValueRows;
    private Map<String, Serializable> auditValue;
    
    public AuditQueryResult()
    {
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("AuditEntryResult")
          .append("[ auditEntryId=").append(auditEntryId)
          .append(", auditAppNameId=").append(auditAppNameId)
          .append(", auditUserId=").append(auditUserId)
          .append(", auditTime").append(new Date(auditTime))
          .append(", auditValuesId=").append(auditValuesId)
          .append(", auditValueRows=").append(auditValueRows == null ? null : auditValueRows.size())
          .append(", auditValue=").append(auditValue)
          .append("]");
        return sb.toString();
    }

    public Long getAuditEntryId()
    {
        return auditEntryId;
    }

    public void setAuditEntryId(Long entryId)
    {
        this.auditEntryId = entryId;
    }

    public Long getAuditAppNameId()
    {
        return auditAppNameId;
    }

    public void setAuditAppNameId(Long auditAppNameId)
    {
        this.auditAppNameId = auditAppNameId;
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

    public void setAuditTime(long time)
    {
        this.auditTime = time;
    }

    public Long getAuditValuesId()
    {
        return auditValuesId;
    }

    public void setAuditValuesId(Long auditValuesId)
    {
        this.auditValuesId = auditValuesId;
    }

    public List<PropertyIdSearchRow> getAuditValueRows()
    {
        return auditValueRows;
    }

    public void setAuditValueRows(List<PropertyIdSearchRow> auditValueRows)
    {
        this.auditValueRows = auditValueRows;
    }

    public Map<String, Serializable> getAuditValue()
    {
        return auditValue;
    }

    public void setAuditValue(Map<String, Serializable> auditValue)
    {
        this.auditValue = auditValue;
    }
}
