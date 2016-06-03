package org.alfresco.repo.tenant;

import org.alfresco.repo.admin.patch.PatchService;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.util.PropertyCheck;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * This component is responsible for starting the enabled tenants (if MT is enabled).
 * 
 * @author janv
 */
public class MultiTenantBootstrap extends AbstractLifecycleBean
{
    private TenantAdminService tenantAdminService;
    private PatchService patchService;
    private DescriptorService descriptorService;
    
    /**
     * @param tenantAdminService        the service that will perform the bootstrap
     */
    public void setTenantAdminService(TenantAdminService tenantAdminService)
    {
        this.tenantAdminService = tenantAdminService;
    }
    
    public void setPatchService(PatchService patchService)
    {
        this.patchService = patchService;
    }
    
    public void setDescriptorService(DescriptorService descriptorService)
    {
        this.descriptorService = descriptorService;
    }
    
    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        PropertyCheck.mandatory(this, "tenantAdminService", tenantAdminService);
        PropertyCheck.mandatory(this, "patchService", patchService);
        PropertyCheck.mandatory(this, "descriptorService", descriptorService);

        // TODO: Is it really necessary to count the tenants?
        if (tenantAdminService.getAllTenants().size() > 0)
        {
            tenantAdminService.startTenants();
        }
    }
    
    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        tenantAdminService.stopTenants();
    }
}
