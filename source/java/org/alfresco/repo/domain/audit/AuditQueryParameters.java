package org.alfresco.repo.domain.audit;

import java.util.Date;

/**
 * Query parameters for <b>alf_audit_entry</b> table.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class AuditQueryParameters
{
    private boolean forward;
    private Long auditAppNameId;
    private Long auditUserId;
    private Long auditFromId;
    private Long auditToId;
    private Long auditFromTime;
    private Long auditToTime;
    private Long searchKeyId;
    private Long searchValueId;
    
    public AuditQueryParameters()
    {
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("AuditEntryParameters")
          .append("[ forward=").append(forward)
          .append(", auditAppNameId=").append(auditAppNameId)
          .append(", auditUserId=").append(auditUserId)
          .append(", auditFromId=").append(auditFromId == null ? null : auditFromId)
          .append(", auditToId=").append(auditToId == null ? null : auditToId)
          .append(", auditFromTime=").append(auditFromTime == null ? null : new Date(auditFromTime))
          .append(", auditToTime=").append(auditToTime == null ? null : new Date(auditToTime))
          .append(", searchKeyId=").append(searchKeyId)
          .append(", searchValueId=").append(searchValueId)
          .append("]");
        return sb.toString();
    }

    public boolean isForward()
    {
        return forward;
    }

    public void setForward(boolean forward)
    {
        this.forward = forward;
    }
    
    public boolean isForwardTrue()
    {
        return true;
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

    public Long getAuditFromTime()
    {
        return auditFromTime;
    }

    public Long getAuditFromId()
    {
        return auditFromId;
    }

    public void setAuditFromId(Long auditFromId)
    {
        this.auditFromId = auditFromId;
    }

    public Long getAuditToId()
    {
        return auditToId;
    }

    public void setAuditToId(Long auditToId)
    {
        this.auditToId = auditToId;
    }

    public void setAuditFromTime(Long from)
    {
        this.auditFromTime = from;
    }

    public Long getAuditToTime()
    {
        return auditToTime;
    }

    public void setAuditToTime(Long to)
    {
        this.auditToTime = to;
    }

    public Long getSearchKeyId()
    {
        return searchKeyId;
    }

    public void setSearchKeyId(Long searchKeyId)
    {
        this.searchKeyId = searchKeyId;
    }

    public Long getSearchValueId()
    {
        return searchValueId;
    }

    public void setSearchValueId(Long searchValueId)
    {
        this.searchValueId = searchValueId;
    }
    
    /**
     * @return              Returns <tt>true</tt> if this object includes a key- or value-based search
     */
    public boolean isKeyOrValueSearch()
    {
        return searchKeyId != null || searchValueId != null;
    }
}
