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

import java.util.Date;

import org.alfresco.util.Pair;

/**
 * Query parameters for <b>alf_audit_entry</b> table.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class AuditQueryParameters
{
    private Long auditEntryId;
    private Pair<String, Long> auditAppNameCrcPair;
    private Pair<String, Long> auditUserCrcPair;
    private Long auditFromTime;
    private Long auditToTime;
    
    public AuditQueryParameters()
    {
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("AuditEntryParameters")
          .append("[ auditEntryId=").append(auditEntryId)
          .append(", auditAppNameCrcPair=").append(auditAppNameCrcPair)
          .append(", auditUserCrcPair=").append(auditUserCrcPair)
          .append(", auditFromTime").append(new Date(auditFromTime))
          .append(", auditToTime").append(new Date(auditToTime))
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

    public String getAuditAppNameShort()
    {
        return auditAppNameCrcPair == null ? null : auditAppNameCrcPair.getFirst();
    }

    public Long getAuditAppNameCrc()
    {
        return auditAppNameCrcPair == null ? null : auditAppNameCrcPair.getSecond();
    }

    public void setAuditAppNameCrcPair(Pair<String, Long> appNameCrcPair)
    {
        this.auditAppNameCrcPair = appNameCrcPair;
    }

    public String getAuditUserShort()
    {
        return auditUserCrcPair == null ? null : auditUserCrcPair.getFirst();
    }

    public Long getAuditUserCrc()
    {
        return auditUserCrcPair == null ? null : auditUserCrcPair.getSecond();
    }

    public void setAuditUserCrcPair(Pair<String, Long> userCrcPair)
    {
        this.auditUserCrcPair = userCrcPair;
    }

    public Long getAuditFromTime()
    {
        return auditFromTime;
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
}
