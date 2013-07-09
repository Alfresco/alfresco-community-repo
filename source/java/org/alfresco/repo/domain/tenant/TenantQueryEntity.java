/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
