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
     * @param tenant
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
     * @param username
     * @param networkId
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
