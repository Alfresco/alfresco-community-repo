/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
