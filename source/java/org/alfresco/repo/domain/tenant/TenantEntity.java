/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.domain.tenant;

import java.io.Serializable;

import org.alfresco.util.EqualsHelper;


/**
 * Entity for <b>alf_tenant</b> persistence.
 * 
 * @author janv
 * @since 4.0 (thor)
 */
public class TenantEntity implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Long version;
    private String tenantDomain;
    private String tenantName;
    private Boolean enabled;
    private String contentRoot; // root folder path or url
    private String dbUrl;
    
    /**
     * Default constructor
     */
    /* package */ TenantEntity()
    {
    }
    
    public TenantEntity(String tenantDomain)
    {
        this.tenantDomain = tenantDomain;
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
    
    public String getTenantDomain()
    {
        return tenantDomain;
    }
    
    /* package */ void setTenantDomain(String tenantDomain)
    {
        this.tenantDomain = tenantDomain;
    }

    public String getTenantName()
    {
        return tenantName;
    }

    public void setTenantName(String tenantName)
    {
        this.tenantName = tenantName;
    }

    public Boolean getEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public String getContentRoot()
    {
        return contentRoot;
    }

    public void setContentRoot(String contentRoot)
    {
        this.contentRoot = contentRoot;
    }

    public String getDbUrl()
    {
        return dbUrl;
    }

    public void setDbUrl(String dbUrl)
    {
        this.dbUrl = dbUrl;
    }

    
    @Override
    public int hashCode()
    {
        return (tenantDomain == null ? 0 : tenantDomain.hashCode());
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj instanceof TenantEntity)
        {
            TenantEntity that = (TenantEntity)obj;
            return (EqualsHelper.nullSafeEquals(this.tenantDomain.toLowerCase(), that.tenantDomain.toLowerCase()));
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
        sb.append("TenantEntity")
          .append("[ tenantDomain=").append(tenantDomain)
          .append(", version=").append(version)
          .append(", enabled=").append(enabled)
          .append(", contentRoot=").append(contentRoot)
          .append(", dbUrl=").append(dbUrl)
          .append(", tenantName=").append(tenantName)
          .append("]");
        return sb.toString();
    }
}
