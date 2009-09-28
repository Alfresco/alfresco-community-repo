/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
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
