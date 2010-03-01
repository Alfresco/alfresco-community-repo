/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.domain.audit;

import org.alfresco.util.EqualsHelper;

/**
 * Entity bean for <b>alf_audit_model</b> table.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class AuditModelEntity
{
    private Long id;
    private Long contentDataId;
    private long contentCrc;
    
    public AuditModelEntity()
    {
    }
    
    @Override
    public int hashCode()
    {
        return (int) contentCrc;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj instanceof AuditModelEntity)
        {
            AuditModelEntity that = (AuditModelEntity) obj;
            return EqualsHelper.nullSafeEquals(this.id, that.id);
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("AuditModelEntity")
          .append("[ ID=").append(id)
          .append(", contentDataId=").append(contentDataId)
          .append(", contentCrc=").append(contentCrc)
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

    public Long getContentDataId()
    {
        return contentDataId;
    }

    public void setContentDataId(Long contentDataId)
    {
        this.contentDataId = contentDataId;
    }

    public long getContentCrc()
    {
        return contentCrc;
    }

    public void setContentCrc(long contentCrc)
    {
        this.contentCrc = contentCrc;
    }
}
