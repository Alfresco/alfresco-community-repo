package org.alfresco.opencmis;

import org.alfresco.repo.tenant.NetworksService;
import org.alfresco.repo.tenant.TenantAdminService;

/**
 * Override factory for OpenCMIS service objects - for public api
 * 
 * @author steveglover
 * @author janv
 * @since PublicApi1.0
 */
public class PublicApiAlfrescoCmisServiceFactory extends AlfrescoCmisServiceFactory
{
    private TenantAdminService tenantAdminService;
    private NetworksService networksService;

    public void setNetworksService(NetworksService networksService)
    {
		this.networksService = networksService;
	}

	public void setTenantAdminService(TenantAdminService tenantAdminService)
    {
        this.tenantAdminService = tenantAdminService;
    }
    
    @Override
    protected AlfrescoCmisService getCmisServiceTarget(CMISConnector connector)
    {
        return new PublicApiAlfrescoCmisService(connector, tenantAdminService, networksService);
    }
}
