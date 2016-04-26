package org.alfresco.repo.domain.permissions;


/**
 * Entity for <b>alf_authority</b> persistence.
 * 
 * @author janv
 * @since 3.4
 */
public class AuthorityEntity implements Authority
{
    private Long id;
    private Long version;
    private String authority;
    private Long crc;
    
    /**
     * Default constructor
     */
    public AuthorityEntity()
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
    
    public void incrementVersion()
    {
        if (this.version >= Long.MAX_VALUE)
        {
            this.version = 0L;
        }
        else
        {
            this.version++;
        }
    }
    
    public String getAuthority()
    {
        return authority;
    }
    
    public void setAuthority(String authority)
    {
        this.authority = authority;
    }
    
    public Long getCrc()
    {
        return crc;
    }
    
    public void setCrc(Long crc)
    {
        this.crc = crc;
    }
    
    @Override
    public int hashCode()
    {
        return getAuthority().hashCode();
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj instanceof AuthorityEntity)
        {
            AuthorityEntity that = (AuthorityEntity)obj;
            return (this.getAuthority().equals(that.getAuthority()));
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
        sb.append("AuthorityEntity")
          .append("[ ID=").append(id)
          .append(", version=").append(version)
          .append(", authority=").append(authority)
          .append(", crc=").append(crc)
          .append("]");
        return sb.toString();
    }
}
