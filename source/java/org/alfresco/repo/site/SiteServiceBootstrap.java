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
package org.alfresco.repo.site;

import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.service.cmr.site.SiteService;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * Warms up site zone / authority caches before the first access to a user dashboard
 * 
 * @author dward
 */
public class SiteServiceBootstrap extends AbstractLifecycleBean
{
    private SiteService siteService;
    private TenantAdminService tenantAdminService;

    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }

    public void setTenantAdminService(TenantAdminService tenantAdminService)
    {
        this.tenantAdminService = tenantAdminService;
    }

    /*
     * (non-Javadoc)
     * @seeorg.springframework.extensions.surf.util.AbstractLifecycleBean#onBootstrap(org.springframework.context.
     * ApplicationEvent)
     */
    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                siteService.listSites("a");
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());

        if (tenantAdminService.isEnabled())
        {
            List<Tenant> tenants = tenantAdminService.getAllTenants();
            for (Tenant tenant : tenants)
            {
                AuthenticationUtil.runAs(new RunAsWork<Object>()
                {
                    public Object doWork() throws Exception
                    {
                        siteService.listSites("a");
                        return null;
                    }
                }, tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenant.getTenantDomain()));
            }
        }
    }

    /*
     * (non-Javadoc)
     * @seeorg.springframework.extensions.surf.util.AbstractLifecycleBean#onShutdown(org.springframework.context.
     * ApplicationEvent)
     */
    @Override
    protected void onShutdown(ApplicationEvent event)
    {
    }
}
