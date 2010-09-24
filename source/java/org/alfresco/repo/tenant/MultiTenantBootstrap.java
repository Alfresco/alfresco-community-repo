/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

import java.util.List;

import org.alfresco.repo.admin.patch.AppliedPatch;
import org.alfresco.repo.admin.patch.PatchService;
import org.alfresco.repo.admin.patch.impl.MigrateAttrTenantsPatch;
import org.alfresco.util.PropertyCheck;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * This component is responsible for starting the enabled tenants (if MT is enabled).
 * 
 * @author Derek Hulley, janv
 */
public class MultiTenantBootstrap extends AbstractLifecycleBean
{
    private TenantAdminService tenantAdminService;
    private PatchService patchService;
    private MigrateAttrTenantsPatch migrateAttrTenantsPatch;
    
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
    
    public void setMigrateAttrTenantsPatch(MigrateAttrTenantsPatch migrateAttrTenantsPatch)
    {
        this.migrateAttrTenantsPatch = migrateAttrTenantsPatch;
    }
    
    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        PropertyCheck.mandatory(this, "tenantAdminService", tenantAdminService);
        PropertyCheck.mandatory(this, "patchService", patchService);
        
        if (tenantAdminService.isEnabled())
        {
            // Upgrade to 3.4 (chicken & egg)
            if (tenantAdminService.getAllTenants().size() == 0)
            {
                boolean applied = false;
                List<AppliedPatch> appliedPatches = patchService.getPatches(null, null);
                for (AppliedPatch appliedPatch : appliedPatches)
                {
                    if (appliedPatch.getId().equals("patch.migrateAttrTenants"))
                    {
                        applied = true;
                        break;
                    }
                }
                
                if (! applied)
                {
                    migrateAttrTenantsPatch.apply();
                }
            }
            
            tenantAdminService.startTenants();
        }
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        tenantAdminService.stopTenants();
    }
}
