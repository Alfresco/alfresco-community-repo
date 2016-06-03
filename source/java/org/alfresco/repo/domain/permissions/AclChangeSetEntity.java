package org.alfresco.repo.domain.permissions;

import org.alfresco.util.EqualsHelper;

/**
 * Entity for <b>alf_acl_change_set</b> persistence.
 * 
 * @author janv
 * @since 3.4
 */
public class AclChangeSetEntity implements AclChangeSet
{
    private Long id;
    private Long commitTimeMs;
    
    /**
     * Default constructor
     */
    public AclChangeSetEntity()
    {
    }
    
    public Long getId()
    {
        return id;
    }
    
    public void setId(Long id)
    {
        this.id = id;
    }
    
    public Long getCommitTimeMs()
    {
        return commitTimeMs;
    }

    public void setCommitTimeMs(Long commitTimeMs)
    {
        this.commitTimeMs = commitTimeMs;
    }

    @Override
    public int hashCode()
    {
        return (id == null ? 0 : id.hashCode());
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj instanceof AclChangeSetEntity)
        {
            AclChangeSetEntity that = (AclChangeSetEntity)obj;
            return (EqualsHelper.nullSafeEquals(this.id, that.id));
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
        sb.append("AclChangeSetEntity")
          .append("[ ID=").append(id)
          .append(", commitTimeMs=").append(commitTimeMs)
          .append("]");
        return sb.toString();
    }
}
