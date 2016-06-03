package org.alfresco.repo.domain.tenant;

/**
 * Entity for <b>alf_tenant</b> queries.
 * 
 * @author Derek Hulley
 * @since 4.2
 */
public class TenantQueryEntity
{
    private String tenantDomain;
    private String tenantName;
    private Boolean enabled;
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("TenantQueryEntity")
          .append("[ tenantDomain=").append(tenantDomain)
          .append(", tenantName=").append(tenantName)
          .append(", enabled=").append(enabled)
          .append("]");
        return sb.toString();
    }
    
    /** Framework usage only */
    @SuppressWarnings("unused")
    private String getTenantDomain()
    {
        return tenantDomain;
    }
    
    public void setTenantDomain(String tenantDomain)
    {
        this.tenantDomain = tenantDomain;
    }

    /** Framework usage only */
    @SuppressWarnings("unused")
    private String getTenantName()
    {
        return tenantName;
    }

    public void setTenantName(String tenantName)
    {
        this.tenantName = tenantName;
    }

    /** Framework usage only */
    @SuppressWarnings("unused")
    private Boolean getEnabled()
    {
        return enabled;
    }

    public void setEnabled(Boolean enabled)
    {
        this.enabled = enabled;
    }
}
