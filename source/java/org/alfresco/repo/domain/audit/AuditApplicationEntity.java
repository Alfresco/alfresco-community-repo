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

/**
 * Entity bean for <b>alf_audit_application</b> table.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class AuditApplicationEntity
{
    private Long id;
    private short version;
    private Long applicationNameId;
    private Long auditModelId;
    private Long disabledPathsId;
    
    public AuditApplicationEntity()
    {
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("AuditApplicationEntity")
          .append("[ ID=").append(id)
          .append(", version=").append(version)
          .append(", applicationNameId=").append(applicationNameId)
          .append(", auditModelId=").append(auditModelId)
          .append(", disabledPathsId=").append(disabledPathsId)
          .append("]");
        return sb.toString();
    }
    
    public void incrementVersion()
    {
        if (version >= Short.MAX_VALUE)
        {
            this.version = 0;
        }
        else
        {
            this.version++;
        }
    }
    
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public short getVersion()
    {
        return version;
    }

    public void setVersion(short version)
    {
        this.version = version;
    }

    public Long getApplicationNameId()
    {
        return applicationNameId;
    }

    public void setApplicationNameId(Long applicationNameId)
    {
        this.applicationNameId = applicationNameId;
    }

    public Long getAuditModelId()
    {
        return auditModelId;
    }

    public void setAuditModelId(Long auditModelId)
    {
        this.auditModelId = auditModelId;
    }

    public Long getDisabledPathsId()
    {
        return disabledPathsId;
    }

    public void setDisabledPathsId(Long disabledPathsId)
    {
        this.disabledPathsId = disabledPathsId;
    }
}
