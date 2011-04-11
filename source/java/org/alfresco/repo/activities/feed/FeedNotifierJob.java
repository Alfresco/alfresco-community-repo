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
package org.alfresco.repo.activities.feed;

import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

/**
 * Executes scheduled feed email notifier quartz-job - refer to scheduled-jobs-context.xml
 * 
 * @since 3.5
 */
public class FeedNotifierJob implements Job
{
    private static final String KEY_FEED_NOTIFIER = "feedNotifier";
    private static final String KEY_TENANT_ADMIN_SERVICE = "tenantAdminService";
    
    /**
     * Calls the feed notifier to do its work
     */
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        JobDataMap jobData = context.getJobDetail().getJobDataMap();
        
        final FeedNotifier feedNotifier = (FeedNotifier)jobData.get(KEY_FEED_NOTIFIER);
        final TenantAdminService tenantAdminService = (TenantAdminService)jobData.get(KEY_TENANT_ADMIN_SERVICE);
        
        Long repeatInterval = null;
        Trigger trigger = context.getTrigger();
        if (trigger instanceof SimpleTrigger)
        {
            repeatInterval = ((SimpleTrigger)trigger).getRepeatInterval();
        }
        
        final int repeatIntervalMins = new Long(repeatInterval == null ? 0L : repeatInterval / 1000 / 60).intValue();
        
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                feedNotifier.execute(repeatIntervalMins);
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
        
        if ((tenantAdminService != null) && tenantAdminService.isEnabled())
        {
            List<Tenant> tenants = tenantAdminService.getAllTenants();
            for (Tenant tenant : tenants)
            {
                String tenantDomain = tenant.getTenantDomain();
                AuthenticationUtil.runAs(new RunAsWork<Object>()
                {
                    public Object doWork() throws Exception
                    {
                        feedNotifier.execute(repeatIntervalMins);
                        return null;
                    }
                }, tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain));
            }
        }
    }
}
