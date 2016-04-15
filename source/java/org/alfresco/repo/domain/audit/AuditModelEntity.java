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
