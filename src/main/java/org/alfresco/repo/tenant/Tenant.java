/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.tenant;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * Tenant
 *
 */
@AlfrescoPublicApi
public class Tenant
{
    private String tenantDomain;
    
    private boolean enabled = false;
    
    private String rootContentStoreDir = null; // if configured - can be null

    // from Thor - unused
    private String dbUrl = null;

    
    public Tenant(String tenantDomain, boolean enabled, String rootContentStoreDir, String dbUrl)
    {
        this.tenantDomain = tenantDomain;
        this.enabled = enabled;
        this.rootContentStoreDir = rootContentStoreDir;
        this.dbUrl = dbUrl;
    }

    public String getTenantDomain()
    {
        return tenantDomain;
    }

    public boolean isEnabled()
    {
        return enabled;
    }
    
    public String getRootContentStoreDir()
    {
        return rootContentStoreDir;
    }
    
    public String getDbUrl()
    {
        return dbUrl;
    }

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((tenantDomain == null) ? 0 : tenantDomain.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tenant other = (Tenant) obj;
		if (tenantDomain == null) {
			if (other.tenantDomain != null)
				return false;
		} else if (!tenantDomain.equals(other.tenantDomain))
			return false;
		return true;
	}
}
