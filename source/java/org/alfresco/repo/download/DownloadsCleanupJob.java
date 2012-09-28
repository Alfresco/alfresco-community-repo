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
package org.alfresco.repo.download;

import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.service.cmr.download.DownloadService;
import org.joda.time.DateTime;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Executes the clean up of download nodes.
 *  
 * @author Alex Miller
 */
public class DownloadsCleanupJob implements Job
{

    private static final String KEY_DOWNLOAD_SERVICE = "downloadService";
    private static final String KEY_TENANT_ADMIN_SERVICE = "tenantAdminService";
    private static final String KEY_MAX_AGE = "maxAgeInMinutes";
    

    /*
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        JobDataMap jobData = context.getJobDetail().getJobDataMap();
        
        // extract the services and max age to use
        final DownloadService downloadService = (DownloadService)jobData.get(KEY_DOWNLOAD_SERVICE);
        final TenantAdminService tenantAdminService = (TenantAdminService)jobData.get(KEY_TENANT_ADMIN_SERVICE);
        final int maxAgeInMinutes = Integer.parseInt((String)jobData.get(KEY_MAX_AGE));
        
        final DateTime before = new DateTime().minusMinutes(maxAgeInMinutes);

        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                downloadService.deleteDownloads(before.toDate());
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
        
        if ((tenantAdminService != null) && tenantAdminService.isEnabled())
        {
            List<Tenant> tenants = tenantAdminService.getAllTenants();
            for (Tenant tenant : tenants)
            {
                TenantUtil.runAsSystemTenant(new TenantRunAsWork<Object>()
                {
                    public Object doWork() throws Exception
                    {
                        downloadService.deleteDownloads(before.toDate());
                        return null;
                    }
                }, tenant.getTenantDomain());
            }
        }

    }

}
