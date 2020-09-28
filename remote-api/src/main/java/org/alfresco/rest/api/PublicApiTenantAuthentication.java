/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.api;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.web.auth.TenantAuthentication;

/**
 * Authenticate current user against specified tenant (Enterprise)
 * 
 * @author steveglover
 */
public class PublicApiTenantAuthentication implements TenantAuthentication
{
    private TenantAdminService tenantAdminService;
    
    public void setTenantAdminService(TenantAdminService service)
    {
        this.tenantAdminService = service;
    }
    
    /**
     * Determine whether tenant exists and enabled
     * 
     * @param tenant String
     * @return  true => it exists, no it doesn't
     */
    public boolean tenantExists(final String tenant)
    {
        if (tenant == null || TenantService.DEFAULT_DOMAIN.equalsIgnoreCase(tenant))
        {
            return true;
        }
        
        return AuthenticationUtil.runAsSystem(new RunAsWork<Boolean>()
        {
            public Boolean doWork() throws Exception
            {
                return tenantAdminService.existsTenant(tenant) && tenantAdminService.isEnabled();
            }
        });
    }

    /**
     * Authenticate user against network/tenant.
     * 
     * @param username String
     * @param networkId String
     * @return  true => authenticated, false => not authenticated
     */
    public boolean authenticateTenant(String username, String networkId)
    {
    	boolean authenticated = false;

    	String userNetworkId = tenantAdminService.getUserDomain(username);
    	if(userNetworkId == null || userNetworkId.equals(TenantService.DEFAULT_DOMAIN))
    	{
    		if(networkId.equalsIgnoreCase(TenantUtil.DEFAULT_TENANT) || networkId.equalsIgnoreCase(TenantUtil.SYSTEM_TENANT))
    		{
    			authenticated = true;
    		}
    	}
    	else
    	{
    		if(networkId.equalsIgnoreCase(TenantUtil.DEFAULT_TENANT))
    		{
    			networkId = userNetworkId;
    		}
    		
    		if(userNetworkId.equalsIgnoreCase(networkId))
        	{
        		authenticated = tenantAdminService.isEnabledTenant(networkId);
        	}
    	}

        return authenticated; 
    }
}
