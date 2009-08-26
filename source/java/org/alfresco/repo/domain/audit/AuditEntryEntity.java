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

/**
 * Entity bean for <b>alf_audit_entry</b> table.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class AuditEntryEntity
{
    private Long id;
    private Long auditSessionId;
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
          .append(", auditSessionId=").append(auditSessionId)
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

    public Long getAuditSessionId()
    {
        return auditSessionId;
    }

    public void setAuditSessionId(Long auditSessionId)
    {
        this.auditSessionId = auditSessionId;
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
