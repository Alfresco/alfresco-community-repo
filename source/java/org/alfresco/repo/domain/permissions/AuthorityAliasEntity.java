package org.alfresco.repo.domain.permissions;

import org.alfresco.util.EqualsHelper;


/**
 * Entity for <b>alf_authority_alias</b> persistence.
 * 
 * @author janv
 * @since 3.4
 */
public class AuthorityAliasEntity implements AuthorityAlias
{
    private Long id;
    private Long version;
    private Long authId;
    private Long aliasId;
    
    /**
     * Default constructor
     */
    public AuthorityAliasEntity()
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
    
    public Long getVersion()
    {
        return version;
    }
    
    public void setVersion(Long version)
    {
        this.version = version;
    }
    
    public Long getAuthId()
    {
        return authId;
    }
    
    public void setAuthId(Long authId)
    {
        this.authId = authId;
    }
    
    public Long getAliasId()
    {
        return aliasId;
    }
    
    public void setAliasId(Long aliasId)
    {
        this.aliasId = aliasId;
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
        else if (obj instanceof AuthorityAliasEntity)
        {
            AuthorityAliasEntity that = (AuthorityAliasEntity)obj;
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
        sb.append("AuthorityAliasEntity")
          .append("[ ID=").append(id)
          .append(", version=").append(version)
          .append(", authId=").append(authId)
          .append(", aliasId=").append(aliasId)
          .append("]");
        return sb.toString();
    }
}
